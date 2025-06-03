const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {GLog} = require("../utils/GLog");

const BOT_LOG_MSG = "bot_action.expect:log";

/**
 *
 * @param task The current task from the Planner
 * @returns {Promise<{item: (string|*), quantity: (number|*), reasoning: (string|*)}>}
 */
async function expect(task) {

    let context = fs.readFileSync("./context/expect_prompt.txt", 'utf8');

    let expectation = await callOpenAI(context, "Task: " + task, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!expectation) {
        GLog.c(`${BOT_LOG_MSG} OpenAI response was empty. Ignore.`)
        return null;
    }

    let myExpectation = JSON.parse(expectation);

    return{
        reasoning: myExpectation.reasoning,
        quantity: myExpectation.quantity,
        item: myExpectation.item,
    }
}

module.exports = {
    expect,
};