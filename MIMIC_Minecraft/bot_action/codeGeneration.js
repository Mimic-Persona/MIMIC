const BOT_LOG_MSG = "bot_action.codeGeneration:log";

const {loadSkills} = require("../utils/file_utils");
const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {status2Prompt} = require("../bridge/client");

/**
 * Transfer the given status into the wanted format for the code generation
 * @param {JSON} status The current status of the agent
 * @param {JSON} inventory The current inventory of the agent
 * @param {String} task The task to be achieved
 * @param {String} code The code from the last round
 * @param {String} errMsg The execution error
 * @param {String} botMsg The game log
 * @param {String} critique The critique of the last round
 * @returns {string} The new status for the code generation
 */
function statusToCodeInput(status, inventory, task, code, errMsg, botMsg, critique){
    let newStatus = status2Prompt(status, inventory);

    newStatus += "code from the last round: " + code + "\n";
    newStatus += "execution error: " + errMsg + "\n";
    newStatus += "chat log: " + botMsg + "\n";
    newStatus += "task: " + task + "\n";
    newStatus += "critique: " + critique;

    return newStatus;
}

/**
 * Generate the code according to the given task
 * @param memoryStream
 * @param skillManager
 * @param BASIC_SKILL_PATH
 * @param SKILLS_PATH
 * @param {string} task The task to be achieved
 * @param {string[]} basic_skills The given basic skills to achieve the task
 * @param status
 * @param inventory
 * @param previousCode
 * @param errMsg
 * @param chatLog
 * @param critique
 * @returns {Promise<{explain, code, plan}>} The code generated by the Codex
 */
async function codeGeneration(memoryStream, skillManager, BASIC_SKILL_PATH, SKILLS_PATH,
                              task, basic_skills, status, inventory,
                              previousCode="", errMsg="", chatLog="", critique="") {

    const BS = await loadSkills(basic_skills, BASIC_SKILL_PATH);

    const relatedSkills = await skillManager.retrieveSkills(task);
    // console.log(relatedSkills);

    let RS = await loadSkills(relatedSkills, SKILLS_PATH);
    // console.log(RS);
    RS = RS.replaceAll(skillManager.skills_import, '\n');

    // TODO: Remove the skills imports when adding to the context
    let context = fs.readFileSync("./context/code_generation_prompt.txt", 'utf8');
    context = context.replace("{Basic_Skills}", BS);
    context = context.replace("{Skills}", RS);

    let currStatus = statusToCodeInput(status, inventory, task, previousCode, errMsg, chatLog, critique)

    // FIXME: Bad Request sometimes because of large context
    let result = await callOpenAI(context, currStatus, BOT_LOG_MSG, "gpt-4o", false, true, true, false);

    if (!result) {
        console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return null;
    }

    // Extract the answers
    let explain = result.slice(result.indexOf("Explain (if applicable):") + 24, result.indexOf("Function Name:")).trim();
    let name = result.slice(result.indexOf("Function Name:") + 14, result.indexOf("Programs Used:")).trim();
    let skills = result.slice(result.indexOf("Programs Used:") + 14, result.indexOf("Design:")).trim().split(', ');
    let design = result.slice(result.indexOf("Design:") + 7, result.indexOf("Code:")).trim();
    let code = result.slice(result.indexOf("```javascript") + 13, result.indexOf("```", result.indexOf("```") + 1)).trim();

    return {
        explain: explain,
        name: name,
        skills: skills,
        relatedSkills: relatedSkills,
        design: design,
        code: code
    };
}


module.exports = codeGeneration;