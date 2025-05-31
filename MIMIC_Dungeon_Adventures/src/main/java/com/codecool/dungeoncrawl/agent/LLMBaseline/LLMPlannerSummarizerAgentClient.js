const WebSocket = require('ws');
const dotenv = require("dotenv");
dotenv.config();

const {LLMAgentActions} = require("./LLMPlannerSummarizerAgentActions");
const {sendMessage} = require("../bridge/sendMessage");
const CircularBuffer = require("../utils/CircularBuffer");
const config = require('../../../../../../../../config.json');

const BOT_LOG_MSG = "LLMBaseline.LLMPlannerSummarizerAgent:log";
const BOT_CHAT_MSG = "LLMBaseline.LLMPlannerSummarizerAgent:chat";
const BOT_ERR_MSG ="LLMBaseline.LLMPlannerSummarizerAgent:error";

const HAS_MEMORY = false;
const MEMORY_SIZE = 20;

const socket = new WebSocket(`ws://localhost:${config.PORT}`);

const pastPlans = new CircularBuffer(MEMORY_SIZE);
let summary = "";

let startTime;
const DURATION = 60000 * 30 * 6 // Duration in milliseconds


socket.onopen = function(event) {
    sendMessage(socket, `${BOT_LOG_MSG} LLMPlannerSummarizerAgent connected to WebSocket server.`);
    startTime = Date.now();
};


socket.onmessage = async function (event) {
    console.log(`========================================= ${event.data} =========================================`);
    let msg = JSON.parse(event.data);

    if (msg.msgType === "command") {
        // Ignore the in-game command
        if (msg.command !== 'k') {
            sendMessage(socket, `${BOT_LOG_MSG} Press 'K' to start the LLMPlannerSummarizerAgent.`);
            return;
        }

        while (socket.readyState === WebSocket.OPEN && Date.now() - startTime < DURATION) {

            let results = await LLMAgentActions(socket, summary, HAS_MEMORY ? pastPlans.getElements() : null);

            let myPlan = results.myPlan;
            summary = results.summary;

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