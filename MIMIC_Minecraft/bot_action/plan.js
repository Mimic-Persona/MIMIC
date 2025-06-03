// Biome: ...
// Time: ...
// Nearby blocks: ...
// Other blocks that are recently seen: ...
// Nearby entities (nearest to farthest): ...
// Health: Higher than 15 means I'm healthy.
// Hunger: Higher than 15 means I'm not hungry.
// Position: ...
// Equipment: If I have better armor in my inventory, you should ask me to equip it.
// Inventory (xx/36): ...
// Chests: You can ask me to deposit or take items from these chests. There also might be some unknown chest, you should ask me to open and check items inside the unknown chest.

// Expectation: Based on the information I listed above and the task you provide, the expected change of the status after the task has been achieved.
// Expectation:{
//     Biome: ...
//     Time: ...
//     Nearby entities (nearest to farthest): ...
//     Health: ...
//     Hunger: ...
//     Position: ...
//     Equipment: ...
//     Inventory (11/36): {... ,'wood_log': 4}
//     }
//  Since a wood_log should be collected, the Inventory should have one more wood_log than before.

/**
 * EXAMPLES
 *
 * Here's a wrong example response which you should not produce:
 * Reasoning: ...
 * Task: Collect more dirt and stone for building and crafting.
 * Subject: I
 * Verb: Collect
 * Object: dirt and stone
 * This example is wrong because the task does not follow the concise format mentioned before. Also, the object for this task is more than two whereas the object has to be a single one for the task. The correct one can be:
 * Reasoning: ...
 * Task: Collect 1 dirt.
 * Subject: I
 * Verb: Collect
 * Object: dirt
 *
 * Here's a wrong example response which you should not produce:
 * Reasoning: ...
 * Task: Mine 10 stone.
 * Subject: I
 * Verb: Mine
 * Object: 10 stone
 * This example is wrong because the output "Object" should be presented only by the phrase without quantity. The correct one should be:
 * Reasoning: ...
 * Task: Mine 10 stone.
 * Subject: I
 * Verb: Mine
 * Object: stone
 *
 * Here's a wrong example response which you should not produce:
 * Reasoning: ...
 * Task: Kill a pig.
 * Subject: I
 * Verb: Kill
 * Object: Pig
 * This example is wrong because the task does not follow the concise format mentioned before. The quantity should be in number. The correct one can be:
 * Reasoning: ...
 * Task: Kill 1 pig.
 * Subject: I
 * Verb: Collect
 * Object: dirt
 *
 * Here's a wrong example response which you should not produce:
 * Reasoning: ...
 * Task: Kill 2 sheep, collect 2 white wool and 2 mutton.
 * Subject: I
 * Verb: collect
 * Object: mutton
 * This example is wrong because the task proposes multiple tasks at the same time. The correct one can be:
 * Reasoning: ...
 * Task: collect 2 mutton.
 * Subject: I
 * Verb: collect
 * Object: mutton
 *
 * Here's a wrong example response which you should not produce:
 * Reasoning: ...
 * Task: Collect 10 birch logs and 10 oak logs.
 * Subject: I
 * Verb: Collect
 * Object: birch logs and oak logs
 * This example is wrong because the task proposes multiple tasks at the same time. The correct one can be:
 * Reasoning: ...
 * Task: Collect 10 birch logs.
 * Subject: I
 * Verb: Collect
 * Object: birch log
 *
 *
 *
 *
 *
 *
 * status: the environment had in Minecraft when the task was generated, decideReason: the reason why this task is rejected,
 */

const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {status2Prompt} = require("../bridge/client");
const {GLog} = require("../utils/GLog");

const BOT_LOG_MSG = "bot_action.plan:log";

/**
 * Transfer the given status into the wanted format for the planner
 * @param memoryStream The memory stream
 * @param status The current status of Minecraft
 * @param inventory The current inventory of the bot
 * @param latestBadPlans The latest bad plans to be avoided
 * @param isBoth If True, retrieve top 5 of each measure (R & P); Otherwise, retrieve top 10 of measure U (= αR + βP)
 * @param planPeaks
 * @returns {Promise<String>} The status in the wanted format for the planner
 */
async function statusToPlanInput(memoryStream, status, inventory, latestBadPlans, isBoth, planPeaks) {
    let newStatus = status2Prompt(status, inventory);
    let badPlans = "";
    let relatedTasks = "";
    let preferredTasks = "";

    for (let bp in latestBadPlans) {
        badPlans += JSON.stringify(latestBadPlans[bp].badPlanSummary()) + "\n";
    }

    let relatedMemories = await memoryStream.retrieveMemories(JSON.stringify(status), isBoth);

    let pastRecentTasks = await memoryStream.retrievePastRecentMemories();

    // If it is only returning memories based on Utility
    if (!isBoth) {
        for (let id of relatedMemories) {
            if (memoryStream.memories[id]) {
                relatedTasks += JSON.stringify(memoryStream.memories[id].planSummary()) + "\n";
            }
        }

        newStatus += "Related tasks did before: " + relatedTasks +'\n';
        newStatus += "Past Recent tasks: " + pastRecentTasks +'\n';
        newStatus += "Past rejected tasks: " + badPlans;
        return newStatus;
    }

    // If it is returning memories based on Relevance and Preference separately
    for (let id of relatedMemories.R) {
        if (memoryStream.memories[id]) {
            relatedTasks += JSON.stringify(memoryStream.memories[id].planSummary()) + "\n";
        }
    }

    for (let id of relatedMemories.P) {
        if (memoryStream.memories[id]) {
            preferredTasks += JSON.stringify(memoryStream.memories[id].planSummary()) + "\n";
        }
    }

    newStatus += "Related tasks did before: " + relatedTasks +'\n';
    newStatus += "Past Recent tasks: " + pastRecentTasks +'\n';
    newStatus += "Preferred tasks by the personality you have: " + preferredTasks +'\n';
    newStatus += "Past rejected tasks: " + badPlans;

    return newStatus;
}

/**
 * Do the plan for the next task
 * @param finalGoal The finalGoal of the bot
 * @param memoryStream
 * @param status The current status of Minecraft
 * @param inventory Current inventory of the bot
 * @param personality The pre-defined personality for the agent to act
 * @param latestBadPlans The latest bad plans to be avoided
 * @param {boolean} retrieveMethod If True, retrieve top 5 of each measure (R & P); Otherwise, retrieve top 10 of measure U (= αR + βP)
 * @param prefix The prefix of the plan prompt
 * @param planPeaks
 * @returns {Promise<{reason: string, task: string, subject: string, biome: string, verb: string, object: string}>}
 */
async function plan(finalGoal, memoryStream, status, inventory, personality, latestBadPlans, retrieveMethod=false, prefix="bottomUp", planPeaks=null) {
    const persaContext = fs.readFileSync(`./context/personalities/${personality}.txt`, 'utf8');
    const persaExampleContext = fs.readFileSync(`./context/personalities/${personality}_examples.txt`, 'utf8');
    const finalGoalContext = fs.readFileSync(`./context/task_suit/${finalGoal}.txt`, 'utf8');

    let context;

    if (retrieveMethod)
        context = fs.readFileSync(`./context/${prefix}_plan_prompt_RP.txt`, 'utf8');
    else
        context = fs.readFileSync(`./context/${prefix}_plan_prompt_U.txt`, 'utf8');

    context = context.replace("{Personalities}", persaContext);
    context = context.replace("{Personalities_Examples}", persaExampleContext);
    context = context.replace("{Task_Name}", finalGoal);
    context = context.replace("{Task_Description}", finalGoalContext);

    let currStatus = await statusToPlanInput(memoryStream, status, inventory, latestBadPlans, retrieveMethod, planPeaks);

    let newPlan = await callOpenAI(context, currStatus, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newPlan) {
        GLog.c(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    newPlan = newPlan.slice(newPlan.indexOf('{'), newPlan.indexOf('}') + 1);

    let myPlan = JSON.parse(newPlan);

    return {
        reasoning: myPlan.reasoning,
        task: myPlan.task.toLowerCase(),
        subject: myPlan.subject.toLowerCase(),
        verb: myPlan.verb.toLowerCase(),
        object: myPlan.object.toLowerCase(),
    }
}

module.exports = {
    plan,
};