const WebSocket = require('ws');
const dotenv = require("dotenv");
const config = require('../../../../../../../../config.json');
dotenv.config();

const {MemoryStream} = require("../memory_system/MemoryStream");
const {SkillManager} = require("../skill_library/SkillManager");
const {sendMessage} = require("./sendMessage");

const {bottomUpActions} = require("./bottomUpActions");
const {topDownActions} = require("./topDownActions");
const {hybridActions} = require("./hybridActions");

const BOT_LOG_MSG = "bridge.agentClient:log";
const BOT_CHAT_MSG = "bridge.agentClient:chat";
const BOT_ERR_MSG ="bridge.agentClient:error";


// TODO: Define the constants before running the experiment
// Take the PERSONALITY from the config.json file
const PERSONALITY = config.MIMIC_PERSONALITY;

const COLLECTION_NAME = "Example";

const DURATION = 60000 * 60 * 3; // Duration in milliseconds

const RETRIEVE_IS_BOTH = true;
const BASIC_SKILL_PATH = `./src/main/java/com/codecool/dungeoncrawl/agent/skill_library/basic_skills_prompts/`;
const SKILL_ROOT_PATH = `./src/main/java/com/codecool/dungeoncrawl/agent/skill_library/skill_${COLLECTION_NAME}/`;
const MEMORY_ROOT_PATH = `./src/main/java/com/codecool/dungeoncrawl/agent/memory_system/${COLLECTION_NAME}/`;
const SKILL_IMPORT_PATH = `.src.main.java.com.codecool.dungeoncrawl.agent.skill_library.skill_${COLLECTION_NAME}`;
const IS_INHERIT = config.IS_CONTINUE;   // If continue the old memories
const TIMEOUT = 10 * 60000;   // Code timeout in milliseconds (10 mins)


const PLANNER_TYPE = "hybrid"; // "topDown", "bottomUp", "hybrid"
const PLANNER_SWITCH_COND = "H"; // "S", "D", "H"
const THRESHOLD_S = 200; // The number of skills as a threshold for changing the planner type
const THRESHOLD_D = 50; // The number of times repeated tasks can continuously appear as a threshold for "D"

let isBottomUp = true;
let dCounter = 0; // The number of times repeated tasks appears continuously

const socket = new WebSocket(`ws://localhost:${config.PORT}`);

const skillManager = new SkillManager(socket, SKILL_ROOT_PATH, SKILL_IMPORT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
skillManager.init();

const memoryStream = new MemoryStream(socket, MEMORY_ROOT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
memoryStream.init();

let startTime;

socket.onopen = function(event) {
    sendMessage(socket, `${BOT_LOG_MSG} Agent connected to WebSocket server.`);
    startTime = Date.now();
};

socket.onmessage = async function (event) {
    console.log(`========================================= ${event.data} =========================================`);
    let msg = JSON.parse(event.data);

    if (msg.msgType === "command") {
        if (msg.command === 'm') {
            sendMessage(socket, `${BOT_LOG_MSG} Fetching all the memories...`);
            await memoryStream.printAllMemories(BOT_LOG_MSG);

        } else if (msg.command === 's') {
            sendMessage(socket, `${BOT_LOG_MSG} Fetching all the skills...`);
            await skillManager.printAllSkills(BOT_LOG_MSG);
        }

        // Ignore the in-game command
        if (msg.command !== 'b') {
            sendMessage(socket, `${BOT_LOG_MSG} Press 'B' to start B-Agent. Press 'M' to fetch all memories. Press 'S' to fetch all skills.`);
            return;
        }

        // TODO: Find a way to reactivate the on message instead of infinite loop
        while (socket.readyState === WebSocket.OPEN && Date.now() - startTime < DURATION) {
            if (PLANNER_TYPE === "bottomUp") {
                await bottomUpActions(socket, skillManager, memoryStream, PERSONALITY, RETRIEVE_IS_BOTH, TIMEOUT);

            } else if (PLANNER_TYPE === "topDown") {
                await topDownActions(socket, skillManager, memoryStream, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT);

            } else {
                let hybridOut = await hybridActions(socket, skillManager, memoryStream, PERSONALITY, RETRIEVE_IS_BOTH, SKILL_ROOT_PATH, TIMEOUT,
                    dCounter, isBottomUp, PLANNER_SWITCH_COND, THRESHOLD_D, THRESHOLD_S);

                dCounter = hybridOut.cnt;
                isBottomUp = hybridOut.isBottomUp;
            }
        }
    }

    else if (msg.msgType === "feedback" || msg.msgType === "status") {
        resolve(msg);
    }

    else if (msg.msgType === "TERMINATED") {
        socket.close();
    }

};

socket.onclose = function (event) {
    console.log(`${BOT_LOG_MSG} B-Agent disconnected to WebSocket server.`);
};