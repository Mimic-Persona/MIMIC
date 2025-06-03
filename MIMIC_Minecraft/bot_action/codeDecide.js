const BOT_LOG_MSG = "bot_action.codeDecide:log";

const fs = require("fs");
const callOpenAI = require("../bridge/open_ai");


function statusToDeciderInput(status, inventory){
    let newStatus = "";
    newStatus += "Biome: " + status.biome + "\n";
    newStatus += "Time: " + status.timeOfDay + "\n";
    newStatus += "Nearby blocks: {" + Array.from(status.blocks).join(', ') + "}\n";
    newStatus += "Nearby entities (nearest to farthest): " + JSON.stringify(status.entities) + "\n";
    newStatus += "Health: " + status.health + "\n";
    newStatus += "Hunger: " + status.food + "\n";
    newStatus += "Position: " + JSON.stringify(status.position) + "\n";
    newStatus += "Equipment: " + JSON.stringify(status.equipment) + "\n";
    newStatus += "Inventory (" + Object.keys(inventory).length + "/36): " + JSON.stringify(inventory);
    return newStatus;
}

function planToDeciderInput(plan){
    let newPlan = "";
    newPlan += "Task: " + plan.task + "\n";
    // newPlan += "Plan reason: " + plan.reason;
    return newPlan;
}

function addRelatedMemory(memoryStream, context, plan){
    let relatedEvents = "";
    let relatedErrors = "";

    let events = memoryStream.getRelevantMemories(plan.subject, plan.verb, plan.object, plan.biome, "event");
    let errors = memoryStream.errors;

    for (let eventKey in events){
        relatedEvents += JSON.stringify(events[eventKey].eventSummary()) + "\n";
    }

    for (let errKey in errors){
        relatedEvents += JSON.stringify(errors[errKey].errorSummary()) + "\n";
    }

    context = context.replace("{Related_Events}", "Past Trails: " + relatedEvents);
    context = context.replace("{Related_Errors}", "Past Errors: " + relatedErrors);
    return context;
}

/**
 *
 * @param memoryStream
 * @param status
 * @param inventory
 * @param personality
 * @param plan
 * @param code
 * @param skillsProvided
 * @param mcData
 * @returns {Promise<boolean>}
 */
async function codeDecide(memoryStream,
                          status, inventory, personality,
                          plan, code, skillsProvided,
                          mcData) {

    const persaContext = fs.readFileSync("./context/personalities/" + personality + ".txt", 'utf8');
    const persaExampleContext = fs.readFileSync("./context/personalities/" + personality + "_examples" + ".txt", 'utf8');
    let currStatus = statusToDeciderInput(status, inventory);
    let currPlan = planToDeciderInput(plan);

    let context = fs.readFileSync("./context/code_decide_prompt.txt", 'utf8');

    // Add in persona prompts
    context = context.replace("{Personalities}", persaContext);

    context = context.replace("{Personalities_Examples}", persaExampleContext);

    // Add in current status
    context = context.replace("{Current_Status}", currStatus);

    // Add in current plan
    context = context.replace("{Current_Plan}", currPlan);

    // Add in current code
    context = context.replace("{Current_Code}", "Code:" + code.code);

    // Add in skills provided
    // context = context.replace("{Skills_Provided}", "Helper functions that can be used: " + code.skills);

    // Add in related events and all errors
    context = addRelatedMemory(memoryStream, context, plan);

    let newDecision = await callOpenAI(context, "", BOT_LOG_MSG);

    if (!newDecision) {
        console.log(BOT_LOG_MSG, "OpenAI response was empty. Ignore.");
        return false;
    }

    let myDecision = JSON.parse(newDecision);
    let reasoning = myDecision.reasoning;
    let decision = myDecision.decision;
    let critique = myDecision.critique;

    // If current plan is passed
    if (decision){
        return true;
    }

    // Add the Bad Plan
    const newMemory = await memoryStream.addMemory("badPlan", false,
        0, 0, 0,
        plan.task, plan.subject, plan.verb, plan.object, plan.biome,
        currStatus, plan.reason, reasoning, "", code.code, code.skills, critique, "");

    console.log(BOT_LOG_MSG, "Bad Plan:", JSON.stringify(newMemory.summary()));
    return false;
}

module.exports = {
    codeDecide,
};