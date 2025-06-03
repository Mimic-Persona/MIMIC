const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {GLog} = require("../utils/GLog");


const BOT_LOG_MSG = "bot_action.planDecompose:log";
const BOT_ERR_MSG = "bot_action.planDecompose:error";

/**
 * Transfer the given info into the wanted format for the plan decomposer
 * @param {{}} inventory The inventory of the bot
 * @param {{}} plan The current plan
 * @param {{}} status
 * @returns {string}
 */
function infoToInput(inventory, plan, status){
    let input = "";
    input += "Equipment: " + JSON.stringify(status.equipment) + "\n";
    input += "Inventory (" + Object.keys(inventory).length + "/36): " + JSON.stringify(inventory) + "\n";
    input += "Task: " + plan.task;
    return input;
}

/**
 *
 * @param {{}} inventory The inventory of the bot
 * @param {{}} plan The current plan
 * @param {{}} status
 * @returns
 */
async function planDecompose(inventory, plan, status) {
    const context = fs.readFileSync("./context/plan_decompose_prompt.txt", 'utf8');

    const input = infoToInput(inventory, plan, status);

    let subGoals = await callOpenAI(context, input, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!subGoals) {
        GLog.c(`${BOT_LOG_MSG} OpenAI response was empty. Ignore.`)
        return null;
    }

    return JSON.parse(subGoals);
}

module.exports = {
    planDecompose,
};