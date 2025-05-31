const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");

const BOT_LOG_MSG = "bot_action.codeDescription:log";

/**
 * Get the description of the given skill code
 * @param socket The WebSocket connection
 * @param code The code to get the description
 * @returns {Promise<string|null>} The description of the code
 */
async function codeDescription(socket, code) {

    let context = fs.readFileSync("./src/main/java/com/codecool/dungeoncrawl/agent/context/skill_description_prompt.txt", 'utf8');

    let description = await callOpenAI(socket, context, code, BOT_LOG_MSG, "gpt-4o", false, true, true, false);

    if (!description) {
        console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    return description;
}

module.exports = {
    codeDescription,
};