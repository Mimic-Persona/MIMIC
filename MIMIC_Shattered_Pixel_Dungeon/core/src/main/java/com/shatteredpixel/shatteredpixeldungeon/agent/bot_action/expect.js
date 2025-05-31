const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");

const BOT_LOG_MSG = "bot_action.expect:log";

/**
 *
 * @param socket
 * @param task The current task from the Planner
 * @returns {Promise<{item: (string|*), quantity: (number|*), reasoning: (string|*)}>}
 */
async function expect(socket, task) {

    let context = fs.readFileSync("./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/expect_prompt.txt", 'utf8');

    let expectation = await callOpenAI(socket, context, "Task: " + task, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!expectation) {
        console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    let myExpectation = JSON.parse(expectation);

    return{
        reasoning: myExpectation.reasoning,
        change: myExpectation.change
    }
}

module.exports = {
    expect,
};