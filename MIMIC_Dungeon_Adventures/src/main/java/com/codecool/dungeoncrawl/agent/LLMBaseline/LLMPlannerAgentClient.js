const WebSocket = require('ws');
const dotenv = require("dotenv");
dotenv.config();

const {LLMAgentActions} = require("./LLMPlannerAgentActions");
const {sendMessage} = require("../bridge/sendMessage");
const {getStatus} = require("../bridge/client");
const CircularBuffer = require("../utils/CircularBuffer");
const config = require('../../../../../../../../config.json');

const BOT_LOG_MSG = "LLMBaseline.LLMPlannerAgent:log";
const BOT_CHAT_MSG = "LLMBaseline.LLMPlannerAgent:chat";
const BOT_ERR_MSG ="LLMBaseline.LLMPlannerAgent:error";

const HAS_MEMORY = false;
const MEMORY_SIZE = 20;

const socket = new WebSocket(`ws://localhost:${config.PORT}`);

const pastPlans = new CircularBuffer(MEMORY_SIZE);

let startTime;
const DURATION = 60000 * 30 * 6 // Duration in milliseconds


socket.onopen = function(event) {
    sendMessage(socket, `${BOT_LOG_MSG} LLMPlannerAgent connected to WebSocket server.`);
    startTime = Date.now();
};


socket.onmessage = async function (event) {
    console.log(`========================================= ${event.data} =========================================`);
    let msg = JSON.parse(event.data);

    if (msg.msgType === "command") {
        // Ignore the in-game command
        if (msg.command !== 'l') {
            sendMessage(socket, `${BOT_LOG_MSG} Press 'L' to start the LLMPlannerAgent.`);
            return;
        }

        while (socket.readyState === WebSocket.OPEN && Date.now() - startTime < DURATION) {
            const status = await getStatus(socket)
                .then(function(response) {
                    // Handle the server's response
                    return response;
                })

                .catch(function(error) {
                    sendMessage(socket, `${BOT_ERR_MSG} Error when fetching status: ${error}`);
                });

            sendMessage(socket, `${BOT_LOG_MSG} Status: ${JSON.stringify(status)}`);

            let myPlan = await LLMAgentActions(socket, status, HAS_MEMORY ? pastPlans.getElements() : null);

            if (HAS_MEMORY) {
                pastPlans.add(myPlan.task);
            }
        }
        sendMessage(socket, `${BOT_LOG_MSG} Experiment Over.`);
    }

    else if (msg.msgType === "feedback" || msg.msgType === "status") {
        resolve(msg);
    }

};


socket.onclose = function (event) {
    console.log(`${BOT_LOG_MSG} LLMAgent disconnected to WebSocket server.`);
};