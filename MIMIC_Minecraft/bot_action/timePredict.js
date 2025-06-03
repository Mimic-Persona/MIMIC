const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {GLog} = require("../utils/GLog");
const {planDecompose} = require("./planDecompose");
const {status2Prompt} = require("../bridge/client");

const BOT_LOG_MSG = "bot_action.timePredict:log";
const BOT_ERR_MSG = "bot_action.timePredict:error";


function statusToTimePredictInput(status, inventory, plan, expectation, code, subGoals) {
    let newStatus = status2Prompt(status, inventory);

    newStatus += "task: " + plan.task + "\n";

    let goals = [];
    for (let goal of subGoals) {
        goals.push(goal.task);
    }
    newStatus += "subGoals: " + goals.toString() + "\n";

    newStatus += "expectation: " + JSON.stringify(expectation) + "\n";
    newStatus += "code: " + code.code + "\n";

    return newStatus;
}

/**
 * Predict the time needed to complete the given plan
 * @param personality The personality of the bot
 * @param status The current status of the bot
 * @param inventory The current inventory of the bot
 * @param plan The plan to be executed
 * @param expectation The expectation of the plan
 * @param code The code for this plan
 * @param isBottomUp If True, the bot will decompose the plan; Otherwise, subGoals is required
 * @param subGoals The subGoals of the plan
 * @returns {Promise<{reasoning: *, time: number}|null>} The reasoning and the time needed to complete the plan
 */
async function timePredict(personality, status, inventory, plan, expectation, code, isBottomUp=true, subGoals=null) {

    const persaContext = fs.readFileSync(`./context/personalities/${personality}.txt`, 'utf8');

    let context = fs.readFileSync("./context/time_predict_prompt.txt", 'utf8');

    context = context.replace("{Personalities}", persaContext);

    // Create subGoals if is in bottom-up mode; otherwise, subGoals is required
    while (!subGoals) {
        if (isBottomUp) {
            subGoals = await planDecompose(inventory, plan, status);
        } else {
            if (!subGoals) {
                GLog.c(`${BOT_ERR_MSG} subGoals is required for top-down time prediction.`);
                return null;
            }
        }
    }

    let currStatus = statusToTimePredictInput(status, inventory, plan, expectation, code, subGoals);

    let time = await callOpenAI(context, currStatus, BOT_LOG_MSG, "gpt-4o", false, true, true, true);

    if (!time) {
        GLog.c(`${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);
        return null;
    }

    time = time.slice(time.indexOf('{'), time.indexOf('}') + 1);

    let myTime = JSON.parse(time);

    return {
        reasoning: myTime.reasoning,
        time: parseInt(myTime.time),
    };
}

module.exports = {
    timePredict,
};