const {status2Prompt} = require("../bridge/client");
const fs = require("fs");
const {GLog} = require("../utils/GLog");

const BOT_LOG_MSG = "bot_action.planDecide:log";

/**
 * Convert the status to the decider input
 * @param status The status of the bot in the game
 * @param inventory The inventory of the bot
 * @returns {String} The prompt of the status
 */
function statusToDeciderInput(status, inventory){
    return status2Prompt(status, inventory);
}

function planToDeciderInput(plan){
    let newPlan = "";
    newPlan += "Task: " + plan.task + "\n";
    // newPlan += "Plan reason: " + plan.reason;
    return newPlan;
}

function addRelatedMemory(memoryStream, context, plan){
    let relatedEvents = "";
    let relatedErrors = "";

    let events = memoryStream.getRelevantMemories(plan.subject, plan.verb, plan.object, plan.biome, "event");
    let errors = memoryStream.errors;

    for (let eventKey in events){
        relatedEvents += JSON.stringify(events[eventKey].eventSummary(false)) + "\n";
    }

    for (let errKey in errors){
        relatedEvents += JSON.stringify(errors[errKey].errorSummary(false)) + "\n";
    }

    context = context.replace("{Related_Events}", "Past Trails: " + relatedEvents);
    context = context.replace("{Related_Errors}", "Past Errors: " + relatedErrors);
    return context;
}

/**
 *  Decide the plan based on some basic rules:
 *  1. If it is a single plan with a single object and a single action.
 *  2. If a current object can cause error in mining (bug in Mineflayer)
 *  3. If a current object is unreachable or unable to collect (grass or leaves)
 *  4. If the plan exists in the badPlans, if the same plan exists the third time, accept it.
 *  5. If the plan follows the preference of current personality.
 *
 * @param memoryStream The memory stream used for storing the memory
 * @param status The previous status before running the plan
 * @param inventory The previous inventory before running the plan
 * @param personality The persona used for the bot
 * @param plan The current generated plan
 * @param skillsProvided The skills provided by the skill library
 * @param mcData All the minecraft data
 * @returns {{reasoning: string, decision: boolean, critique: string}}
 */
function decideWithRule(memoryStream,
                        status, inventory, personality,
                        plan, skillsProvided,
                        mcData){

    // 1. If a current object type is more than one
    if (plan.object.includes(" and ") || plan.object.includes(",") || plan.object.includes(" or ")|| plan.object.includes("&")) {
        return {
            reasoning: "The object is more than 1.",
            decision: false,
            critique: "Try to make the object be only one type or try to do another task."
        }
    }

    // // TODO: A bug in mineflayer, cannot mine stone / ore
    // // 2. If current object can cause error in mining
    // if ((plan.verb === "mine" || plan.verb === "collect" || plan.verb === "obtain" || plan.verb === "craft") &&
    //     (plan.object.includes("stone") || plan.object.includes("ore") || plan.object.includes("coal")) || plan.object.includes("copper") || plan.object.includes("iron")) {
    //     return {
    //         reasoning: "The object cannot be mined based on current skills.",
    //         decision: false,
    //         critique: "Try to find another object to collect or another task to do."
    //     }
    // }

    // 3. If a current object is unreachable
    if ((plan.verb === "mine" || plan.verb === "collect" || plan.verb === "obtain") && (plan.object.includes("grass") || plan.object.includes("leaves") || plan.object.includes("leaf") || plan.object.includes("vine") || plan.object.includes("fern"))) {
        return {
            reasoning: "The object is unreachable or useless to mine.",
            decision: false,
            critique: "Try to find another object to collect or another task to do."
        }
    }

    // 4. If the plan exists in the badPlans, if the same plan exists the 3rd time, accept it.
    let count = 0;
    for (let bp in memoryStream.latestBadPlans)
        if (plan.task.toLowerCase().trim() === memoryStream.latestBadPlans[bp].task.toLowerCase().trim()) count++;


    if (count > 0 && count < 3)
        return{
            reasoning: "The task was rejected before in current status.",
            decision: false,
            critique: "Try to make another task."
    };

    if (count >= 3)
        return{
            reasoning: "The task appears more than 3 times in 1 cycle. Give it a try!",
            decision: true,
            critique: "Pass"
    };

    // TODO: 5. If the plan follows the preference of current personality.

    // // Check the skills used are provided
    // for(let skill of skillsUsed){
    //     if(skillsProvided.includes(skill) !== true){
    //         return {
    //             reasoning: "The skills needed for doing this task has not been learnt by me.",
    //             decision: false,
    //             critique: "Try to create a simpler task that using different skill which would have higher successful rate to be achieved by me."
    //         }
    //     }
    // }

    // // If the current object is an entity
    // if(mcData.entitiesByName[plan.object] !== undefined){
    //     // Check the entity if it is around
    //     if(status.entities.hasOwnProperty(plan.object) !== true){
    //         return {
    //             reasoning: "The object in this task is an entity which is not given in the 'Nearby entities', which can not be achieved",
    //             decision: false,
    //             critique: "Try to change the object to another block that is listed in the 'Nearby entities' or try to do another task."
    //         }
    //     }
    // }

    // // If the current object is a block
    // if(mcData.blocksByName[plan.object] !== undefined){
    //     // Check the block if it is around
    //     if(status.blocks.has(plan.object) !== true){
    //         return {
    //             reasoning: "The object in this task is an entity which is not given in the 'Nearby blocks', which can not be achieved",
    //             decision: false,
    //             critique: "Try to change the object to another block that is listed in the 'Nearby blocks' or try to do another task."
    //         }
    //     }
    // }

    return {
        reasoning: "",
        decision: true,
        critique: ""
    }
}

/**
 * Decide the plan based on some basic rules:
 * @param memoryStream The memory stream used for storing the memory
 * @param status The previous status before running the plan
 * @param inventory The previous inventory before running the plan
 * @param personality The persona used for the bot
 * @param plan The current generated plan
 * @param skillsProvided The skills provided by the skill library
 * @param mcData All the minecraft data
 * @returns {Promise<boolean>} The decision of the plan
 */
async function planDecide(memoryStream,
                          status, inventory, personality,
                          plan, skillsProvided,
                          mcData) {

    const persaContext = fs.readFileSync("./context/personalities/" + personality + ".txt", 'utf8');
    const persaExampleContext = fs.readFileSync("./context/personalities/" + personality + "_examples" + ".txt", 'utf8');
    let currStatus = statusToDeciderInput(status, inventory);
    let currPlan = planToDeciderInput(plan);

    // If current plan is rejected, add current plan as a bad plan memory
    // First planDecide with basic rules
    let ruleDecision = decideWithRule(memoryStream,
        status, inventory, personality,
        plan, skillsProvided,
        mcData);

    if (ruleDecision.decision === false){
        // Add the Bad Plan
        const newMemory = await memoryStream.addMemory("badPlan", false,
            0, 0, 0,
            plan.task, plan.subject, plan.verb, plan.object, plan.biome,
            currStatus, plan.reason, ruleDecision.reasoning, "", "", "", ruleDecision.critique, "");

        GLog.c(`${BOT_LOG_MSG} \nRule Decision Answer:\n ${JSON.stringify(ruleDecision)}`);
        GLog.c(`${BOT_LOG_MSG} Bad Plan: ${JSON.stringify(newMemory.summary())}`);
        return false;

    } else {
        GLog.c(`${BOT_LOG_MSG} \nRule Decision Answer:\n ${JSON.stringify(ruleDecision)}`);
        return true;
    }

    // if (ruleDecision.critique === "Pass"){
    //     console.log(BOT_LOG_MSG, "\nRule Decision Answer:\n", ruleDecision);
    //     return true;
    // }
    //
    // let context = fs.readFileSync("./context/plan_decide_prompt.txt", 'utf8');
    //
    // // Add in persona prompts
    // context = context.replace("{Personalities}", persaContext);
    //
    // context = context.replace("{Personalities_Examples}", persaExampleContext);
    //
    // // Add in current status
    // context = context.replace("{Current_Status}", currStatus);
    //
    // // Add in current plan
    // context = context.replace("{Current_Plan}", currPlan);
    //
    // // Add in skills provided
    // // context = context.replace("{Skills_Provided}", "Helper functions that can be used: " + skillsProvided);
    //
    // // Add in related events and all errors
    // context = addRelatedMemory(memoryStream, context, plan);
    //
    // let newDecision = await callOpenAI(context, "", BOT_LOG_MSG, "gpt-3.5-turbo-instruct");
    //
    // if (!newDecision) {
    //     console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
    //     return true;
    // }
    //
    // let myDecision = JSON.parse(newDecision);
    // let reasoning = myDecision.reasoning;
    // let decision = myDecision.decision;
    // let critique = myDecision.critique;
    //
    // // If current plan is passed
    // if (decision){
    //     return true;
    // }
    //
    // // Add the Bad Plan
    // const newMemory = await memoryStream.addMemory("badPlan", false,
    //     0, 0, 0,
    //     plan.task, plan.subject, plan.verb, plan.object, plan.biome,
    //     currStatus, plan.reason, reasoning, "", "", "", critique, "");
    //
    // console.log(BOT_LOG_MSG, "Bad Plan:", JSON.stringify(newMemory.summary()));
    // return false;
}

module.exports = {
    decideWithRule,
    planDecide,
};