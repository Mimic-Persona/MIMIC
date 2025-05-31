const BOT_LOG_MSG = "bot_action.summarize:log";

const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {loadSkills} = require("../utils/file_utils");
const {status2Prompt} = require("../bridge/client");
const {sendMessage} = require("../bridge/sendMessage");
const BASIC_SKILL_PATH = "../skill_library/basic_skills_prompts/";

/**
 * Transfer the given status into the wanted format for the event summarization
 * @param previousStatusInput The previous status input
 * @param newStatusInput The new status input
 * @param task The current task from the Planner
 * @param reason The reasoning of the current task
 * @param expectation The expectation to the current plan
 * @param logMsg The log message created after running the plan
 * @returns {string} The status input for the event summarization
 */
function getEventStatusInput(previousStatusInput, newStatusInput, task, reason, expectation, logMsg) {
    // let res = previousStatusInput + "\n";
    // res += newStatusInput + "\n";
    let res = "Task: " + task + "\n";
    res += "Reason: " + reason + "\n";
    res += "Log Message: " + logMsg;
    return res;
}

/**
 * Summarize the given event
 * @param socket
 * @param memoryStream
 * @param memoryType
 * @param previousStatusInput
 * @param newStatusInput
 * @param plan
 * @param expectation
 * @param decision
 * @param code
 * @param logMsg
 * @returns {Promise<Memory|null>}
 */

async function doEventSummary(socket, memoryStream, memoryType,
                              previousStatusInput, newStatusInput,
                              plan, expectation, decision, code,
                              logMsg) {

    let context = fs.readFileSync("./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/event_summarize_prompt.txt", 'utf8');

    let statusInput = getEventStatusInput(previousStatusInput, newStatusInput, plan.task, plan.reasoning, expectation, logMsg);

    let newSummary = await callOpenAI(socket, context, statusInput, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newSummary) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);

        return await memoryStream.addMemory(memoryType, true,
            0, 0, 0,
            plan.task, plan.action, plan.tile, plan.item1, plan.item2,
            previousStatusInput, plan.reasoning, "", "", code, "", "", "");
    }

    let mySummary = JSON.parse(newSummary);
    let reasoning = mySummary.reasoning;
    let isSuccess = mySummary.success;
    let critique = mySummary.critique;

    return await memoryStream.addMemory(memoryType, isSuccess,
        0, 0, 0,
        plan.task, plan.action, plan.tile, plan.item1, plan.item2,
        previousStatusInput.replaceAll("previous ", ""), plan.reasoning, "", reasoning, code, "", critique, "");
}

/**
 *
 * @param newErrorStatusInput
 * @param task
 * @param reason
 * @param logMsg
 * @param errorMsg
 * @returns {string}
 */
function getErrorStatusInput(newErrorStatusInput, task, reason, logMsg, errorMsg) {
    let res = newErrorStatusInput;
    res += "Task: " + task + "\n";
    res += "Reason: " + reason + "\n";
    res += "Log Message: " + logMsg + "\n";
    res += "Error Message: " + errorMsg;
    return res;
}

/**
 * Summarize the given error
 * @param socket
 * @param memoryStream
 * @param memoryType
 * @param previousErrorStatusInput
 * @param newErrorStatusInput
 * @param plan
 * @param decision
 * @param code
 * @param logMsg
 * @param errorMsg
 * @returns {Promise<Memory|null>}
 */
async function doErrorSummary(socket, memoryStream, memoryType,
                              previousErrorStatusInput, newErrorStatusInput,
                              plan, decision, code, logMsg, errorMsg) {

    let context = fs.readFileSync("./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/context/error_summarize_prompt.txt", 'utf8');

    let statusInput = getErrorStatusInput(newErrorStatusInput, plan.task, plan.reasoning, logMsg, errorMsg);

    let newSummary = await callOpenAI(socket, context, statusInput, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newSummary) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);

        return await memoryStream.addMemory(memoryType, false,
            0, 0, 0,
            plan.task, plan.action, plan.tile, plan.item1, plan.item2,
            previousErrorStatusInput, plan.reasoning, "", "", code, "", "", errorMsg);
    }

    let mySummary = JSON.parse(newSummary);
    let reasoning = mySummary.reasoning;
    let isSuccess = mySummary.success;
    let critique = mySummary.critique;

    return await memoryStream.addMemory(memoryType, isSuccess,
        0, 0, 0,
        plan.task, plan.action, plan.tile, plan.item1, plan.item2,
        previousErrorStatusInput, plan.reasoning, "", reasoning, code, "", critique, errorMsg);
}

/**
 * Do the summary for current event / error and store it in the given MemoryStream as a Memory
 * @param socket
 * @param basic_skills The basic skills provided to the bot
 * @param memoryStream The MemoryStream used to store the Memory
 * @param memoryType The type of the Memory, input should be 'event' / 'error'
 * @param previousStatus The status before the plan is acted
 * @param newStatus The status after the plan is acted
 * @param plan The current plan by the Planner
 * @param expectation The expectation to the current plan
 * @param decision The current decision by the Decider
 * @param code The current code generated by the CodeGeneration
 * @param logMsg The log message created after running the code
 * @param errorMsg The error message created after running the code
 * @param isTimeOut If the code is timeout
 * @returns {Promise<Memory|null>}
 */
async function summarize(socket, basic_skills, memoryStream, memoryType,
                         previousStatus, newStatus,
                         plan, expectation, decision, code, logMsg, errorMsg, isTimeOut) {

    if (memoryType === "event") {
        const previousEventStatusInput = status2Prompt(previousStatus, "previous ");
        const newEventStatusInput = status2Prompt(newStatus, "new ");

        return await doEventSummary(socket, memoryStream, memoryType,
            previousEventStatusInput, newEventStatusInput,
            plan, expectation, decision, code, logMsg);

    } else {
        const previousErrorStatusInput = status2Prompt(previousStatus);
        const newErrorStatusInput = status2Prompt(newStatus);

        return await doErrorSummary(socket, memoryStream, memoryType,
            previousErrorStatusInput, newErrorStatusInput,
            plan, decision, code, logMsg, errorMsg);
    }
}

module.exports = {
    summarize,
};