const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");


const BOT_LOG_MSG = "bot_action.planDecompose:log";
const BOT_ERR_MSG = "bot_action.planDecompose:error";

/**
 * Transfer the given info into the wanted format for the plan decomposer
 * @param plan The current plan
 * @param status The status of the hero
 * @returns {string} The input for the plan decomposer
 */
function infoToInput(status, plan){
    let input = JSON.stringify(status);
    input += "Task: " + plan.task;
    return input;
}

/**
 * Decompose the plan
 * @param socket The WebSocket connection
 * @param plan The plan to decompose
 * @param status The status of the hero
 * @returns
 */
async function planDecompose(socket, status, plan) {
    const context = fs.readFileSync("./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/plan_decompose_prompt.txt", 'utf8');

    const input = infoToInput(status, plan);

    let subGoals = await callOpenAI(socket, context, input, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!subGoals) {
        console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    return JSON.parse(subGoals);
}

module.exports = {
    planDecompose,
};