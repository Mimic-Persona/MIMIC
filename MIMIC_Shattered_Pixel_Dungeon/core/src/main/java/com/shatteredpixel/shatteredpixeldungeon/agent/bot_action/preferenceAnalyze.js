const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {writeFile} = require("../utils/file_utils");

const BOT_LOG_MSG = "bot_action.preferenceAnalyze:log";
const BOT_ERR_MSG = "bot_action.preferenceAnalyze:error";

/**
 *
 * @param socket The WebSocket connection
 * @param {String} memoryRootPath The root path of the memory system
 * @param {String} persona The persona of the agent
 * @param {String} memory The memory for creating the analysis
 * @param memoryID
 */
async function preferenceAnalyze(socket, memoryRootPath, persona, memory, memoryID) {
    const MEMORY_PATH = memoryRootPath + `${persona}/analysis/`

    // JUSTIFY HERE WHEN TESTING
    const persaContext = fs.readFileSync(`./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/personalities/${persona}.txt`, 'utf8');

    let context = fs.readFileSync("./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/preference_analyze_prompt.txt", 'utf8');
    context = context.replace("{Personalities}", persaContext);

    let analysis = await callOpenAI(socket, context, memory, BOT_LOG_MSG, "gpt-4o", false, true, true, false);

    // let analysis = "TEST";

    if (!analysis) {
        console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    await writeFile(MEMORY_PATH + `id${memoryID}.txt`, analysis, BOT_LOG_MSG, BOT_ERR_MSG);

    return analysis;
}

module.exports = {
    preferenceAnalyze,
};