const {status2Prompt} = require("../bridge/client");
const {sendMessage} = require("../bridge/sendMessage");

const BOT_LOG_MSG = "bot_action.planDecide:log";

/**
 *  Decide the plan based on some basic rules:
 *  1. If it is a single plan with single object and a single action.
 *  2. If the plan exists in the badPlans, if same plan exists the 3rd time, accept it.
 *  3. If the plan follows the preference of current personality.
 *
 * @param memoryStream The memory stream
 * @param status The current status of the agent
 * @param personality The personality of the agent
 * @param plan The current plan to decide
 * @returns {{reasoning: string, decision: boolean, critique: string}}
 */
function decideWithRule(memoryStream, status, personality, plan){

    // 1. If current object type is more than one
    if (plan.item1 !== null && (plan.item1.includes(" and ") || plan.item1.includes(",") || plan.item1.includes(" or ")|| plan.item1.includes("&")) ||
        plan.item2 !== null && (plan.item2.includes(" and ") || plan.item2.includes(",") || plan.item2.includes(" or ")|| plan.item2.includes("&"))) {
        return {
            reasoning: "The object is more than 1.",
            decision: false,
            critique: "Try to make the object be only one type or try to do another task."
        }
    }

    // 2. If the plan exists in the badPlans, if same plan exists the 3rd time, accept it.
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

    // TODO: 3. If the plan follows the preference of current personality.

    return {
        reasoning: "",
        decision: true,
        critique: ""
    }
}

async function planDecide(socket, memoryStream, status, personality, plan) {

    let currStatus = status2Prompt(status);

    // If current plan is rejected, add current plan as a bad plan memory
    // First planDecide with basic rules
    let ruleDecision = decideWithRule(memoryStream, status, personality, plan);

    if (ruleDecision.decision === false){
        // Add the Bad Plan
        const newMemory = await memoryStream.addMemory("badPlan", false,
            0, 0, 0,
            plan.task, plan.action, plan.tile, plan.item1, plan.item2,
            currStatus, plan.reasoning, ruleDecision.reasoning, "", "", "", ruleDecision.critique, "");

        sendMessage(socket, `${BOT_LOG_MSG} Rule Decision Answer: ${JSON.stringify(ruleDecision)}`);
        sendMessage(socket, `${BOT_LOG_MSG} Bad Plan: ${JSON.stringify(newMemory.summary())}`);
        return false;

    } else {
        sendMessage(socket, `${BOT_LOG_MSG} Rule Decision Answer: ${JSON.stringify(ruleDecision)}`);
        return true;
    }
}

module.exports = {
    decideWithRule,
    planDecide,
};