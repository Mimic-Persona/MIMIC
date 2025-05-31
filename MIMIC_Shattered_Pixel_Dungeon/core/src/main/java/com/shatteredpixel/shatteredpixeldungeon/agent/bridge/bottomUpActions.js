// Imports for actions
const {plan} = require("../bot_action/plan");
const {planDecide} = require("../bot_action/planDecide");
const {summarize} = require("../bot_action/summarize");
const {getStatus, actAndFeedback} = require("./client");
const {sendMessage} = require("./sendMessage");

const BOT_LOG_MSG = "bridge.bottomUpActions:log";
const BOT_ERR_MSG ="bridge.bottomUpActions:error";

/**
 * Do the bottom-up actions for the bot
 * @param socket The WebSocket connection
 * @param skillManager The skill manager
 * @param memoryStream The memory stream
 * @param PERSONALITY The personality of the bot
 * @param RETRIEVE_IS_BOTH The flag to retrieve both preferred and related memories
 * @param TIMEOUT The timeout for the code execution
 * @returns {Promise<{reason: string, task: string, subject: string, biome: string, verb: string, object: string}>}
 */
async function bottomUpActions(socket, skillManager, memoryStream,
                               PERSONALITY, RETRIEVE_IS_BOTH,
                               TIMEOUT) {

    memoryStream.clearLatestBadPlans(); // Clear bad plans before new run

    const previousStatus = await getStatus(socket)
        .then(function(response) {
            // Handle the server's response
            return response;
        })

        .catch(function(error) {
            sendMessage(socket, `${BOT_ERR_MSG} Error when fetching status: ${error}`);
        });

    sendMessage(socket, `${BOT_LOG_MSG} Previous Status: ${JSON.stringify(previousStatus)}`);

    // Reset the decision every time
    let planDecision = false;
    let myPlan;

    while (!planDecision){
        myPlan = await plan(socket, memoryStream, previousStatus, PERSONALITY, memoryStream.latestBadPlans, RETRIEVE_IS_BOTH, "bottomUp");

        if (myPlan === null) {
            sendMessage(socket, `${BOT_ERR_MSG} Plan is NULL.`);
            continue;
        }

        planDecision = await planDecide(socket, memoryStream, previousStatus, PERSONALITY, myPlan);
        if (planDecision === null) {
            sendMessage(socket, `${BOT_ERR_MSG} planDecision is NULL.`);
            planDecision = false;
        }
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

    //FIXME: not updated
    const newStatus = await getStatus(socket)
        .then(function(response) {
            // Handle the server's response
            return response;
        })

        .catch(function(error) {
            sendMessage(socket, `${BOT_ERR_MSG} Error when fetching status: ${error}`);
        });

    // Summarize the action
    let newMemory = await summarize(socket, "", memoryStream, memoryType,
        previousStatus, newStatus,
        myPlan, "", "", "", bot_msg, err_msg, false);

    sendMessage(socket, `${BOT_LOG_MSG} newMemory: ${JSON.stringify(newMemory)}`);

    return myPlan;
}

module.exports = {
    bottomUpActions,
};