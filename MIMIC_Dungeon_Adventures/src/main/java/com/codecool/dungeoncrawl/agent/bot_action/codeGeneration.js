const BOT_LOG_MSG = "bot_action.codeGeneration:log";

const {loadSkills} = require("../utils/file_utils");
const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {status2Prompt} = require("../bridge/client");
const {sendMessage} = require("../bridge/sendMessage");

/**
 * Transfer the given status into the wanted format for the code generation
 * @param {JSON} status The current status of the agent
 * @param {String} code The code from the last round
 * @param {String} botMsg The game log
 * @param {String} errMsg The execution error
 * @param {String} task The task to be achieved
 * @param {String} critique The critique of the last round
 * @returns {string} The new status for the code generation
 */
function statusToCodeInput(status, code, botMsg, errMsg, task, critique){
    let newStatus = status2Prompt(status);

    newStatus += "code from the last round: " + code + "\n";
    newStatus += "execution error: " + errMsg + "\n";
    newStatus += "game log: " + botMsg + "\n";
    newStatus += "task: " + task + "\n";
    newStatus += "critique: " + critique;

    return newStatus;
}

/**
 * Generate the code for the given task
 * @param socket The WebSocket connection
 * @param skillManager The skill manager
 * @param BASIC_SKILL_PATH The basic skill path
 * @param SKILLS_PATH The root path for the skill library
 * @param task The task to be achieved
 * @param basic_skills The basic skills
 * @param status The current status of the agent
 * @param previousCode The code from the last round
 * @param errMsg The execution error
 * @param botMsg The game log
 * @param critique The critique of the last round
 * @returns {Promise<{explain: string, skills: string[], relatedSkills: *[], code: string, design: string, className: string, timout: number}|null>}
 */
async function codeGeneration(socket, skillManager, BASIC_SKILL_PATH, SKILLS_PATH,
                              task, basic_skills, status,
                              previousCode="", errMsg="", botMsg="", critique="") {

    const BS = await loadSkills(basic_skills, BASIC_SKILL_PATH, false);

    const relatedSkills = await skillManager.retrieveSkills(task);

    let RS = await loadSkills(relatedSkills, SKILLS_PATH);
    // console.log(RS);
    RS = RS.replaceAll(skillManager.skills_import, '\n');

    // TODO: Remove the skills imports when adding to the context
    let context = fs.readFileSync("./src/main/java/com/codecool/dungeoncrawl/agent/context/code_generation_prompt.txt", 'utf8');
    context = context.replace("{Basic_Skills}", BS);
    context = context.replace("{Skills}", RS);

    let currStatus = statusToCodeInput(status, previousCode, botMsg, errMsg, task, critique);

    // FIXME: Bad Request sometimes because of large context
    let result = await callOpenAI(socket, context, currStatus, BOT_LOG_MSG, "gpt-4o", false, true, true, false);

    if (!result) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);
        return null;
    }

    // Extract the answers
    let explain = result.slice(result.indexOf("Explain (if applicable):") + 24, result.indexOf("Function Name:")).trim();
    let className = result.slice(result.indexOf("Function Name:") + 14, result.indexOf("Programs Used:")).trim();
    let skills = result.slice(result.indexOf("Programs Used:") + 14, result.indexOf("Design:")).trim().split(', ');
    let design = result.slice(result.indexOf("Design:") + 7, result.indexOf("Code:\n```java")).trim();
    let code = result.slice(result.indexOf("```java") + 7, result.indexOf("```", result.indexOf("```") + 1)).trim();

    return {
        explain: explain,
        className: className,
        skills: skills,
        relatedSkills: relatedSkills,
        design: design,
        code: code,
        timeout: 5,
    };
}

module.exports = {
    codeGeneration,
};