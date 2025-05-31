const fs = require("fs");
const {status2Prompt, actAndFeedback} = require("../bridge/client");
const callOpenAI = require("../bridge/open_ai");
const {sendMessage} = require("../bridge/sendMessage");


const BOT_LOG_MSG = "LLMBaseline.LLMPlannerAgent:log";
const BOT_ERR_MSG = "LLMBaseline.LLMPlannerAgent:error";


/**
 * Transfer the given status into the wanted format for the planner
 * @param {JSON} status The status
 * @param pastPlans The past plans
 * @returns {string} The status in the wanted format for the planner
 */
async function statusToPlanInput(status, pastPlans=null) {
    let newStatus = status2Prompt(status);

    if (pastPlans !== null) {
        newStatus += "Related tasks did before: " + pastPlans;
    }

    return newStatus;
}

/**
 * Do the plan for the next task
 * @param socket The WebSocket connection
 * @param status The current status of SPD
 * @param pastPlans The past plans
 * @returns {Promise<{task: *, reasoning: (string|*), tile: any, action: *, object}|null>} The plan for the next task
 */
async function LLMPlan(socket, status, pastPlans=null) {
    let context;
    let prefix = "";

    if (pastPlans !== null) {
        prefix = "M";
    }

    context = fs.readFileSync(`./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/llm_${prefix}agent_plan_prompt.txt`, 'utf8');

    let currStatus = await statusToPlanInput(status, pastPlans);

    let newPlan = await callOpenAI(socket, context, currStatus, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newPlan) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);
        return null;
    }

    newPlan = newPlan.slice(newPlan.indexOf('{'), newPlan.indexOf('}') + 1);

    let myPlan = JSON.parse(newPlan);

    return {
        reasoning: myPlan.reasoning,
        task: myPlan.task,
        action: myPlan.action,
        tile: myPlan.tile,
        item1: myPlan.item1,
        item2: myPlan.item2,
        waitTurns: myPlan.waitTurns,
    }
}

/**
 * Do the actions for the bot
 * @param socket The WebSocket connection
 * @param status The current status of SPD
 * @param pastPlans The past plans
 * @returns {Promise<{task: any, reasoning: any, tile: any, action: any, object: any}>} The plan for this action
 */
async function LLMPlannerAgentActions(socket, status, pastPlans=null) {
    let myPlan = await LLMPlan(socket, status, pastPlans);

    while (!myPlan) {
        myPlan = await LLMPlan(socket, status, pastPlans);
        sendMessage(socket, `${BOT_ERR_MSG} Plan is NULL.`);
    }

    // Send the action plan
    const feedback = await actAndFeedback(socket, myPlan)
        .then(function(response) {
            // Handle the server's response
            return response;
        })

        .catch(function(error) {
            sendMessage(socket, `${BOT_ERR_MSG} Error when acting: ${error}`);
        });

    sendMessage(socket, `${BOT_LOG_MSG} Feedback received from server: ${JSON.stringify(feedback)}`);

    return myPlan;
}


module.exports = {
    LLMAgentActions: LLMPlannerAgentActions,
};