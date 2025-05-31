const WebSocket = require('ws');
const dotenv = require("dotenv");
dotenv.config();

const {MemoryStream} = require("../memory_system/MemoryStream");
const {SkillManager} = require("../skill_library/SkillManager");
const {sendMessage} = require("./sendMessage");
const {runAndFeedback} = require("./client");
const config = require('../../../../../../../../config.json');

const BOT_LOG_MSG = "bridge.agentClient:log";

const COLLECTION_NAME = "test";
const RETRIEVE_IS_BOTH = true;
const BASIC_SKILL_PATH = `../skill_library/basic_skills_prompts/`;
const SKILL_ROOT_PATH = `../skill_library/skill_${COLLECTION_NAME}/`;
const MEMORY_ROOT_PATH = `../memory_system/${COLLECTION_NAME}/`;
const SKILL_IMPORT_PATH = `com.codecool.dungeoncrawl.agent.skill_library.skill_${COLLECTION_NAME}`;
const IS_INHERIT = false;   // If continue the old memories

const PERSONALITY = "aggressive";

const OUT_DIR = `/skill_${COLLECTION_NAME}/${PERSONALITY}/`;

const socket = new WebSocket(`ws://localhost:${config.PORT}`);

const skillManager = new SkillManager(socket, SKILL_ROOT_PATH, SKILL_IMPORT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
skillManager.init();

const memoryStream = new MemoryStream(socket, MEMORY_ROOT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
memoryStream.init();

socket.onopen = function(event) {
    sendMessage(socket, `${BOT_LOG_MSG} Agent connected to WebSocket server.`);
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

        if (socket.readyState === WebSocket.OPEN) {
            let code = {
                className: "NavigationTest",
                relatedSkills: ["NavigationTest"],
                code: "public class NavigationTest {\n" +
                    "    public static void navigationTest() {\n" +
                    "        Navigate.navigateIgnoreMobs(map.getNearestMob().getCell());\n" +
                    "    }\n" +
                    "\n" +
                    "    public static void main(String[] args) {\n" +
                    "        navigationTest();\n" +
                    "    }\n" +
                    "}",
                timeout: 5,
                outDir: OUT_DIR
            };

            const feedback = await runAndFeedback(socket, code)
                .then(function(response) {
                    // Handle the server's response
                    return response;
                })

                .catch(function(error) {
                    sendMessage(socket, `${BOT_ERR_MSG} Error when acting: ${error}`);
                });

            sendMessage(socket, `${BOT_LOG_MSG} Feedback received from server: ${JSON.stringify(feedback)}`);

            let newSkill = await skillManager.addSkill(code.className, "", code.code, PERSONALITY, false, code.relatedSkills);
            sendMessage(socket, `${BOT_LOG_MSG} Skill Added: ${newSkill}`);
        }
    }

    else if (msg.msgType === "feedback" || msg.msgType === "status") {
        resolve(msg);
    }

};

socket.onclose = function (event) {
    console.log(`${BOT_LOG_MSG} Agent disconnected to WebSocket server.`);
};