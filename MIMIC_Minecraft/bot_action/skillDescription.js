const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {GLog} = require("../utils/GLog");

const BOT_LOG_MSG = "bot_action.codeDescription:log";

/**
 * Get the description of the given skill code
 * @param code The code to get the description
 * @returns {Promise<string|null>} The description of the code
 */
async function codeDescription(code) {

    let context = fs.readFileSync("./context/skill_description_prompt.txt", 'utf8');

    let description = await callOpenAI(context, code, BOT_LOG_MSG, "gpt-4o", false, true, true, false);

    if (!description) {
        GLog.c(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    return description;
}

module.exports = {
    codeDescription,
};