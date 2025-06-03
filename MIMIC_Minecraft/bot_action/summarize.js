/**
 * Biome: The biome after the task execution.
 * Time: The current time.
 * Nearby blocks: The surrounding blocks. These blocks are not collected yet. However, this is useful for some placing or planting tasks.
 * Nearby entities (nearest to farthest): The surrounding entities. These entities can move around. However, this is useful for some resource collecting tasks.
 * Health: My current health.
 * Hunger: My current hunger level. For eating task, if my hunger level is 20.0, then I successfully ate the food.
 * Position: My current position.
 * Equipment: My final equipment. For crafting tasks, I sometimes equip the crafted item.
 * Inventory (xx/36): My final inventory. For mining, collecting, and smelting tasks, you only need to check inventory.
 * Task: The objective I need to accomplish.
 */

const BOT_LOG_MSG = "bot_action.summarize:log";

const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");
const {loadSkills} = require("../utils/file_utils");
const {status2Prompt} = require("../bridge/client");
const {GLog} = require("../utils/GLog");
const BASIC_SKILL_PATH = "./skill_library/basic_skills_prompts/";

/**
 * Transfer the given status into the wanted format for the event summarizer with difference version
 * @param expectation The expectation to the current plan
 * @param status The status in Minecraft
 * @param inventory The inventory of the bot
 * @param plan The current plan
 * @param previousStatus The previous status in MC before the action
 * @param previousInventory The previous inventory of the bot before the action
 * @param isTimeOut If the code is timeout
 * @param botMsg
 * @returns {string} The new status for the event summarization
 */
function statusToEventSummarizeDifferenceInput(expectation, status, inventory, plan, previousStatus, previousInventory, isTimeOut, botMsg){
    let equipmentChanged = [];
    for (let i in status.equipment) {
        equipmentChanged.push("From " + previousStatus.equipment[i] + " to " + status.equipment[i]);
    }

    let inventoryChanged = {};
    for(let key in inventory) {
        if (previousInventory[key]) {
            inventoryChanged[key] = inventory[key] - previousInventory[key];
        }
        else{
            inventoryChanged[key] = inventory[key];
        }
    }

    for(let key in previousInventory) {
        if (!inventory[key]) {
            inventoryChanged[key] = 0 - previousInventory[key];
        }
    }

    let newStatus = "";
    newStatus += "Biome: " + status.biome + "\n";
    newStatus += "Time: " + status.timeOfDay + "\n";
    newStatus += "Nearby blocks: {" + Array.from(status.blocks).join(', ') + "}\n";
    newStatus += "Nearby entities (nearest to farthest): " + JSON.stringify(status.entities) + "\n";
    newStatus += "Health Changed: " + previousStatus.health + " to " + status.health + "\n";
    newStatus += "Hunger Changed: " + previousStatus.food + " to " + status.food + "\n";
    newStatus += "Position: " + JSON.stringify(status.position) + "\n";
    newStatus += "Equipment Changed: " + JSON.stringify(equipmentChanged) + "\n";
    newStatus += "Inventory (" + Object.keys(inventory).length + "/36): " + JSON.stringify(inventory) + "\n";
    newStatus += "Inventory Changed(" + Object.keys(previousInventory).length + "/36 to " + Object.keys(inventory).length + "/36): " + JSON.stringify(inventoryChanged) + "\n";
    newStatus += "Task: " + plan.task + "\n";
    newStatus += "Expectation: " + expectation + "\n";
    newStatus += "Is Time Out: " + isTimeOut + "\n";
    newStatus += "Game Log: " + botMsg;
    return newStatus;
}

/**
 * Transfer the given status into the wanted format for the event summarizer
 * @param {string} expectation The expectation to the current plan
 * @param {{}} status The status in Minecraft
 * @param {{}} inventory The inventory of the bot
 * @param {{}} plan The current plan
 * @param {{}} previousStatus The previous status in MC before the action
 * @param {{}} previousInventory The previous inventory of the bot before the action
 * @param {boolean} isPrevious If the input to be transferred is previous status
 * @returns {string}
 */
function statusToEventSummarizeInput(expectation, status, inventory, plan, previousStatus, previousInventory, isPrevious=false){
    let newStatus = status2Prompt(status, inventory);

    if (!isPrevious){
        newStatus += "Previous Equipment: " + JSON.stringify(previousStatus.equipment) + "\n";
        newStatus += "Previous Inventory (" + Object.keys(previousInventory).length + "/36): " + JSON.stringify(previousInventory) + "\n";
    }

    newStatus += "Task: " + plan.task + "\n";
    newStatus += "Expectation: " + expectation;
    return newStatus;
}

/**
 * Transfer the given status into the wanted format for the error summarizer
 * @param status The status in Minecraft
 * @param inventory The inventory of the bot
 * @param plan The current plan
 * @param code The current code generated
 * @param errorMsg The error message generated
 * @param isTimeOut
 * @param botMsg
 * @returns {string}
 */
function statusToErrorSummarizeInput(status, inventory, plan, code, errorMsg, isTimeOut, botMsg){
    let newStatus = status2Prompt(status, inventory);
    newStatus += "Task: " + plan.task + "\n";
    newStatus += "Code: " + code + "\n";
    newStatus += "Error: " + errorMsg + "\n";
    newStatus += "Is Time Out: " + isTimeOut + "\n";
    newStatus += "Game Log: " + botMsg;
    return newStatus;
}

/**
 * Summarize the given event
 * @param memoryStream
 * @param memoryType
 * @param previousStatusInput
 * @param newStatusInput
 * @param plan
 * @param decision
 * @param code
 * @param skills
 * @returns {Promise<Memory|null>}
 */
async function doEventSummary(memoryStream, memoryType,
                         previousStatusInput, newStatusInput,
                         plan, decision, code, skills) {

    let context = fs.readFileSync("./context/event_summarize_difference_prompt.txt", 'utf8');

    let newSummary = await callOpenAI(context, newStatusInput, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newSummary) {
        GLog.c(`${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);

        return await memoryStream.addMemory(memoryType, true,
            0, 0, 0,
            plan.task, plan.subject, plan.verb, plan.object, plan.biome,
            previousStatusInput, plan.reason, "", "", code, skills, "", "");
    }

    let mySummary = JSON.parse(newSummary);
    let reasoning = mySummary.reasoning;
    let isSuccess = mySummary.success;
    let critique = mySummary.critique;

    // TODO: Use decision.reason?
    return await memoryStream.addMemory(memoryType, isSuccess,
        0, 0, 0,
        plan.task, plan.subject, plan.verb, plan.object, plan.biome,
        previousStatusInput, plan.reason, "", reasoning, code, skills, critique, "");
}

/**
 *  Summerize the given error
 * @param basic_skills
 * @param memoryStream
 * @param memoryType
 * @param previousStatusInput
 * @param newStatusInput
 * @param statusToStore
 * @param plan
 * @param decision
 * @param code
 * @param skills
 * @param errorMsg
 * @returns {Promise<Memory|null>}
 */
async function doErrorSummary(basic_skills, memoryStream, memoryType,
                              previousStatusInput, newStatusInput, statusToStore,
                              plan, decision, code, skills, errorMsg) {

    let context = fs.readFileSync("./context/error_summarize_prompt.txt", 'utf8');
    const BS = loadSkills(basic_skills, BASIC_SKILL_PATH);
    context = context.replace("{Skills}", BS);


    let newSummary = await callOpenAI(context, newStatusInput, BOT_LOG_MSG, "gpt-4");

    if (!newSummary) {
        GLog.c(`${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);

        return await memoryStream.addMemory(memoryType, false,
            0, 0, 0,
            plan.task, plan.subject, plan.verb, plan.object, plan.biome,
            statusToStore, plan.reason, "", "", code, skills, "", errorMsg);
    }

    let mySummary = JSON.parse(newSummary);
    let reasoning = mySummary.reasoning;
    let isSuccess = mySummary.success;
    let critique = "You should check if you have any infinite loop in your code, and then try to do it again. Or try on another task. " + mySummary.critique;

    return await memoryStream.addMemory(memoryType, isSuccess,
        0, 0, 0,
        plan.task, plan.subject, plan.verb, plan.object, plan.biome,
        statusToStore, plan.reason, "", reasoning, code, skills, critique, errorMsg);
}

/**
 * Do the summary for current event / error and store it in the given MemoryStream as a Memory
 * @param {[string]} basic_skills The basic skills provided to the bot
 * @param memoryStream The MemoryStream used to store the Memory
 * @param memoryType The type of the Memory, input should be 'event' / 'error'
 * @param previousStatus The status before the plan is acted
 * @param previousInventory The inventory before the plan is acted
 * @param newStatus The status after the plan is acted
 * @param newInventory The inventory after the plan is acted
 * @param plan The current plan by the Planner
 * @param expectation The expectation to the current plan
 * @param decision The current decision by the Decider
 * @param code The current code generated by the CodeGeneration
 * @param errorMsg The error message created after running the code
 * @param isTimeOut If the code is timeout
 * @returns {Promise<Memory|null>}
 */
async function summarize(basic_skills, memoryStream, memoryType,
                         previousStatus, previousInventory,
                         newStatus, newInventory,
                         plan, expectation, decision, code, botMsg, errorMsg, isTimeOut) {

    // Transfer the expectation to string
    let myExpectation = "";

    for (let item of expectation.item){
        myExpectation += expectation.quantity.toString() + " " + item + " or ";
    }

    myExpectation = myExpectation.slice(0, -4);

    if (memoryType === "event"){
        const previousEventStatusInput = statusToEventSummarizeInput(myExpectation, previousStatus, previousInventory, plan, {}, {}, true);
        const newEventStatusInput = statusToEventSummarizeDifferenceInput(myExpectation, newStatus, newInventory, plan, previousStatus, previousInventory, isTimeOut, botMsg);

        return await doEventSummary(memoryStream, memoryType,
            previousEventStatusInput, newEventStatusInput,
            plan, decision, code.code, code.skills, botMsg);

    } else{
        const previousErrorStatusInput = statusToErrorSummarizeInput(previousStatus, previousInventory, plan, code.code, errorMsg, isTimeOut, botMsg);
        const newErrorStatusInput = statusToErrorSummarizeInput(newStatus, newInventory, plan, code.code, errorMsg, isTimeOut, botMsg);
        const statusToStore = statusToEventSummarizeInput(myExpectation, previousStatus, previousInventory, plan, {}, {}, true);

        return await doErrorSummary(basic_skills, memoryStream, memoryType,
            previousErrorStatusInput, newErrorStatusInput, statusToStore,
            plan, decision, code.code, code.skills, errorMsg);
    }

}

module.exports = {
    statusToEventSummarizeInput,
    statusToErrorSummarizeInput,
    summarize,
};