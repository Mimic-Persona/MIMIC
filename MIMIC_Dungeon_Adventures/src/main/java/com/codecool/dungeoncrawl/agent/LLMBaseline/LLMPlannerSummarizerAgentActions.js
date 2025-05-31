const fs = require("fs");
const {status2Prompt, actAndFeedback, getStatus} = require("../bridge/client");
const callOpenAI = require("../bridge/open_ai");
const {sendMessage} = require("../bridge/sendMessage");


const BOT_LOG_MSG = "LLMBaseline.LLMPlannerSummarizerAgent:log";
const BOT_ERR_MSG = "LLMBaseline.LLMPlannerSummarizerAgent:error";


/**
 * Transfer the given status into the wanted format for the planner
 * @param {JSON} status The status
 * @param pastPlans The past plans
 * @param pastSummary The past summary
 * @returns {string} The status in the wanted format for the planner
 */
async function statusToPlanInput(status, pastSummary, pastPlans=null) {
    let newStatus = status2Prompt(status);

    newStatus += "Last task: " + JSON.stringify(pastSummary);

    if (pastPlans !== null) {
        newStatus += "Related tasks did before: " + pastPlans;
    }

    return newStatus;
}

/**
 * Do the plan for the next task
 * @param socket The WebSocket connection
 * @param status The current status of DA
 * @param pastSummary The past summary
 * @param pastPlans The past plans
 * @returns {Promise<{task: *, reasoning: (string|*), tile: any, action: *, object}|null>} The plan for the next task
 */
async function LLMPlan(socket, status, pastSummary, pastPlans=null) {
    let context;

    context = fs.readFileSync(`./src/main/java/com/codecool/dungeoncrawl/agent/context/llm_agent_Splan_prompt.txt`, 'utf8');

    let currStatus = await statusToPlanInput(status, pastSummary, pastPlans);

    let newPlan = await callOpenAI(socket, context, currStatus, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newPlan) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);
        return null;
    }

    newPlan = newPlan.slice(newPlan.indexOf('{'), newPlan.indexOf('}') + 1);

    let myPlan = JSON.parse(newPlan);

    return {
        reasoning: myPlan.reasoning,
        task: myPlan.task,
        action: myPlan.action,
        tile: myPlan.tile,
        object: myPlan.object,
    }
}

async function statusToSummarizerInput(status, pastPlans=null) {
    let newStatus = status2Prompt(status);

    if (pastPlans !== null) {
        newStatus += "Related tasks did before: " + pastPlans;
    }

    return newStatus;
}

/**
 * Transfer the given status into the wanted format for the event summarization
 * @param task The current task from the Planner
 * @param logMsg The log message created after running the plan
 * @returns {string} The status input for the event summarization
 */
function getEventStatusInput(task, logMsg) {
    let res = "Task: " + task + "\n";
    res += "Log Message: " + logMsg + "\n";
    return res;
}

/**
 * Summarize the given event
 * @param socket The WebSocket connection
 * @param memoryType The type of the Memory, input should be 'event' / 'error'
 * @param plan The current plan by the Planner
 * @param logMsg The log message created after running the code
 * @returns {Promise<{success: null, critique: string}|{success: (*|null|Event), critique: (*|string)}>}
 */
async function doEventSummary(socket, memoryType, plan, logMsg) {

    let context = fs.readFileSync("./src/main/java/com/codecool/dungeoncrawl/agent/context/llm_agent_event_summarize_prompt.txt", 'utf8');

    let statusInput = getEventStatusInput(plan.task, logMsg);

    let newSummary = await callOpenAI(socket, context, statusInput, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newSummary) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);

        return {
            "task": plan.task,
            "success": null,
            "critique": ""
        }
    }

    let mySummary = JSON.parse(newSummary);
    let isSuccess = mySummary.success;
    let critique = mySummary.critique;

    return {
        "task": plan.task,
        "success": isSuccess,
        "critique": critique
    }
}

/**
 *
 * @param newErrorStatusInput The new status input
 * @param task The current task from the Planner
 * @param logMsg The log message created after running the code
 * @param errorMsg The error message created after running the code
 * @returns {string} The status input for the error summarization
 */
function getErrorStatusInput(newErrorStatusInput, task, logMsg, errorMsg) {
    let res = newErrorStatusInput;
    res += "Task: " + task + "\n";
    res += "Log Message: " + logMsg + "\n";
    res += "Error Message: " + errorMsg + "\n";
    return res;
}

/**
 * Summarize the given error
 * @param socket The WebSocket connection
 * @param memoryType The type of the Memory, input should be 'event' / 'error'
 * @param newErrorStatusInput The new status input
 * @param plan The current plan by the Planner
 * @param logMsg The log message created after running the code
 * @param errorMsg The error message created after running the code
 * @returns {Promise<{success: null, critique: string}|{success: boolean, critique: (*|string)}>}
 */
async function doErrorSummary(socket, memoryType,
                              newErrorStatusInput, plan,
                              logMsg, errorMsg) {

    let context = fs.readFileSync("./src/main/java/com/codecool/dungeoncrawl/agent/context/llm_agent_error_summarize_prompt.txt", 'utf8');

    let statusInput = getErrorStatusInput(newErrorStatusInput, plan.task, logMsg, errorMsg);

    let newSummary = await callOpenAI(socket, context, statusInput, BOT_LOG_MSG, "gpt-4o", false, true);

    if (!newSummary) {
        sendMessage(socket, `${BOT_LOG_MSG} OpenAI response was empty. Ignore.`);

        return {
            "task": plan.task,
            "success": null,
            "critique": ""
        }
    }

    let mySummary = JSON.parse(newSummary);
    let critique = mySummary.critique;

    return {
        "task": plan.task,
        "success": false,
        "critique": critique
    }
}

/**
 * Summarize the given event or error
 * @param socket The WebSocket connection
 * @param memoryType The type of the Memory, input should be 'event' / 'error'
 * @param newStatus The current status of DA
 * @param plan The current plan by the Planner
 * @param logMsg The log message created after running the code
 * @param errorMsg The error message created after running the code
 * @returns {Promise<{success: null, critique: string}|{success: boolean, critique: (*|string)}|{success: (*|Event|null), critique: (*|string)}>}
 */
async function LLMSummarize(socket, memoryType,
                            newStatus, plan,
                            logMsg, errorMsg) {

    const newStatusInput = status2Prompt(newStatus, "new ");

    if (memoryType === "event") {
        return await doEventSummary(socket, memoryType, plan, logMsg);
    } else {
        return await doErrorSummary(socket, memoryType, newStatusInput, plan, logMsg, errorMsg);
    }
}

/**
 * Do the actions for the bot
 * @param socket The WebSocket connection
 * @param prevSummary
 * @param pastPlans The past plans
 * @returns {Promise<{task: any, reasoning: any, tile: any, action: any, object: any}>} The plan for this action
 */
async function LLMPlannerSummarizerAgentActions(socket, prevSummary, pastPlans=null) {
    const oldStatus = await getStatus(socket)
        .then(function(response) {
            // Handle the server's response
            return response;
        })

        .catch(function(error) {
            sendMessage(socket, `${BOT_ERR_MSG} Error when fetching status: ${error}`);
        });

    sendMessage(socket, `${BOT_LOG_MSG} Status: ${JSON.stringify(oldStatus)}`);

    let myPlan = await LLMPlan(socket, oldStatus, prevSummary, pastPlans);

    while (!myPlan) {
        myPlan = await LLMPlan(socket, oldStatus, pastPlans);
        sendMessage(socket, `${BOT_ERR_MSG} Plan is NULL.`);
    }

    // Send the action plan
    const feedback = await actAndFeedback(socket, myPlan)
        .then(function(response) {
            // Handle the server's response
            return response;
        })

        .catch(function(error) {
            sendMessage(socket, `${BOT_ERR_MSG} Error when acting: ${error}`);
        });

    sendMessage(socket, `${BOT_LOG_MSG} Feedback received from server: ${JSON.stringify(feedback)}`);

    // Handle the feedback
    let bot_msg = feedback.logs;
    let err_msg = feedback.errors;
    let memoryType = feedback.errors === "" ? "event" : "error";

    const newStatus = await getStatus(socket)
        .then(function(response) {
            // Handle the server's response
            return response;
        })

        .catch(function(error) {
            sendMessage(socket, `${BOT_ERR_MSG} Error when fetching status: ${error}`);
        });

    // Summarize the action
    let summary = await LLMSummarize(socket, memoryType, newStatus, myPlan, bot_msg, err_msg);

    return {
        "myPlan": myPlan,
        "summary": summary,
    };
}


module.exports = {
    LLMAgentActions: LLMPlannerSummarizerAgentActions,
};