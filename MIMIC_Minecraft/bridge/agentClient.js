const dotenv = require("dotenv");
const config = require("../config.json");
dotenv.config();

const mineflayer = require("mineflayer");
const minecraftData = require("minecraft-data");

const {MemoryStream} = require("../memory_system/MemoryStream");
const {SkillManager} = require("../skill_library/SkillManager");

const {bottomUpActions} = require("./bottomUpActions");
const {topDownActions} = require("./topDownActions");
const {hybridActions} = require("./hybridActions");
const {GLog} = require("../utils/GLog");

const BOT_LOG_MSG = "bridge.mc_bot:log";
const BOT_CHAT_MSG = "bridge.mc_bot:chat";
const BOT_ERR_MSG = "bridge.mc_bot:error";

const { appendFileSync } = require("node:fs");
const fs = require("fs");
const getInventory = require("../env/mc_env/getInventory");
const startCombating = require("../skill_library/basic_skills/startCombating");

// TODO: Define the below constants before running the experiment
const PERSONALITY = config.MIMIC_PERSONALITY;

const COLLECTION_NAME = config.REPORT_PREFIX;

const IS_INHERIT = config.IS_CONTINUE;   // If continue the old memories

const FINAL_GOAL = config.TASK;

// RANDOM SEED 11111
const RETRIEVE_IS_BOTH = true;
const BASIC_SKILL_PATH = "./skill_library/basic_skills_prompts/";
const SKILL_ROOT_PATH = `./skill_library/skill_${COLLECTION_NAME}/`;
const MEMORY_ROOT_PATH = `./memory_system/${COLLECTION_NAME}/`;
const TIMEOUT = 10 * 60000;   // Code timeout in milliseconds (10 mins)

const PLANNER_PEAK = -1; // The number of times the planner can run in the planner-only mode; if -1, it runs with the whole workflow

const PLANNER_TYPE = "hybrid"; // "topDown", "bottomUp", "hybrid"
const PLANNER_SWITCH_COND = "H"; // "S", "D", "H"
const THRESHOLD_S = 10; // The number of skills as a threshold for changing the planner type
const THRESHOLD_D = 10; // The number of times repeated tasks can continuously appear as a threshold for "D"

let isBottomUp = true;
let dCounter = 0; // The number of times repeated tasks appears continuously

let mcData;
let memoryType = "event";
let bot_msg = "";
let err_msg = "";
global.isTerminated = false;
global.isRunning = false;
global.RUNNING_CODE = false;


// Parse the id and task from the ./conf/config.json file
const REPORT_PREFIX = config.REPORT_PREFIX;
const TASK = config.TASK;

// Set up the path to the log files
const CHAT_LOG_PATH = `../out/MC/${TASK}/${TASK}_${REPORT_PREFIX}_chat.log`;
const MOVE_LOG_PATH = `../out/MC/${TASK}/${TASK}_${REPORT_PREFIX}_move.log`;
const INVENTORY_LOG_PATH = `../out/MC/${TASK}/${TASK}_${REPORT_PREFIX}_inventory.log`;

const path = require("path");
const logDir = path.dirname(CHAT_LOG_PATH);
if (!fs.existsSync(logDir)) {
    fs.mkdirSync(logDir, { recursive: true });
}

// Create the log files only if not exist
appendFileSync(CHAT_LOG_PATH, "");
appendFileSync(MOVE_LOG_PATH, "");
appendFileSync(INVENTORY_LOG_PATH, "");

let startTimestamp;
let lastInventory = "{}";

const basic_skills = ["craftItem", "exploreUntil", "killMob", "mineBlock", "mineflayer", "placeItem", "smeltItem"];

// if (config.MONSTER_TYPE !== "") {
//     basic_skills.push("startCombating");
// }

const skillManager = new SkillManager(SKILL_ROOT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
skillManager.init();

const memoryStream = new MemoryStream(MEMORY_ROOT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
memoryStream.init();

const bot = mineflayer.createBot({
    username: PERSONALITY,
    host: config.MC_SERVER_HOST,
    port: config.MC_SERVER_PORT,
    verbose: true,
});

// on error
bot.on("error", (err) => {
    GLog.e(`${BOT_ERR_MSG} ${err.message}`);
    memoryType = "error";
    err_msg += err.message;
    memoryType = "error";
});

bot.on("login", async () => {
    GLog.c(`${BOT_LOG_MSG} Bot joined the game`);
    await bot.chat("/pause");
});

// Listen on bot move
bot.on('move', () => {
    if (FINAL_GOAL.includes("survive") || FINAL_GOAL.includes("combat")) {
        appendFileSync(MOVE_LOG_PATH, `${Date.now() - startTimestamp} - ${bot.time.timeOfDay} - ${bot.entity.position}\n`);
    } else {
        appendFileSync(MOVE_LOG_PATH, `${Date.now() - startTimestamp} - ${bot.entity.position}\n`);
    }
});

bot.on("chat", async (username, input) => {
    if (FINAL_GOAL.includes("survive") || FINAL_GOAL.includes("combat")) {
        fs.appendFileSync(CHAT_LOG_PATH, `${Date.now() - startTimestamp} - ${bot.time.timeOfDay} - ${username}: ${input}\n`);
    } else {
        fs.appendFileSync(CHAT_LOG_PATH, `${Date.now() - startTimestamp} - ${username}: ${input}\n`);
    }

    if (username === PERSONALITY){
        GLog.h(`${BOT_CHAT_MSG} ${username}: ${input}`);
        bot_msg += input + " ";
        // Store the temporary bot message to the txt file
        fs.appendFileSync(`./memory_system/temp_chat.txt`, input + " ");
        return;
    } else {
        console.log(`========================================= ${input} =========================================`);
        GLog.c(`${BOT_CHAT_MSG} ${username}: ${input}`);
    }
    if (input === 'm') {
        await bot.chat("Fetching all the memories...");
        await memoryStream.printAllMemories(BOT_LOG_MSG);
    }

    else if (input === 's') {
        await bot.chat("Fetching all the skills...");
        await skillManager.printAllSkills(BOT_LOG_MSG);
    }

    if (input === 'stop') {
        await bot.chat("Stopping the bot...");
        global.isTerminated = true;
        return;
    }

    // Ignore the in-game command
    if (input !== 'b' && input !== 'B') {
        await bot.chat("Enter B to start the bot.");
        return;
    }

    if (global.isRunning){
        await bot.chat("In processing...");
        return;
    }

    global.isRunning = true;
    global.isTerminated = false;

    while (!global.isTerminated) {
        if (PLANNER_PEAK >= 0) {
            await bottomUpActions(FINAL_GOAL, mcData, bot, skillManager, memoryStream, basic_skills, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT, PLANNER_PEAK);

        } else if (PLANNER_TYPE === "bottomUp") {
            await bottomUpActions(FINAL_GOAL, mcData, bot, skillManager, memoryStream, basic_skills, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT);

        } else if (PLANNER_TYPE === "topDown") {
            await topDownActions(FINAL_GOAL, mcData, bot, skillManager, memoryStream, basic_skills, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT);

        } else {
            let hybridOut = await hybridActions(FINAL_GOAL, mcData, bot, skillManager, memoryStream, basic_skills, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT,
                dCounter, isBottomUp, PLANNER_SWITCH_COND, THRESHOLD_D, THRESHOLD_S);

            dCounter = hybridOut.cnt;
            isBottomUp = hybridOut.isBottomUp;
        }
    }

    await bot.chat("Terminated!");
});

bot.on("physicTick", async () => {
    if (JSON.stringify(getInventory(bot)) !== lastInventory) {
        if (FINAL_GOAL.includes("survive") || FINAL_GOAL.includes("combat")) {
            fs.appendFileSync(INVENTORY_LOG_PATH, `${Date.now() - startTimestamp} - ${bot.time.timeOfDay} - ${JSON.stringify(getInventory(bot))}\n`);
        } else {
            fs.appendFileSync(INVENTORY_LOG_PATH, `${Date.now() - startTimestamp} - ${JSON.stringify(getInventory(bot))}\n`);
        }
    }
    lastInventory = JSON.stringify(getInventory(bot));

    if (FINAL_GOAL.includes("survive") && bot.time.day > 0) {
        await bot.chat("TERMINATED");
        bot.end();
    }

    // Start combating when the bot is not running the last term's task
    if (!global.RUNNING_CODE && !global.isTerminated && FINAL_GOAL.includes("combat") && bot.time.day > 0) {
        await bot.chat("/pause");
        await bot.chat("Start Combating");
        global.isTerminated = true;
        await startCombating(bot, mcData);
        bot.end();
    }
});

// TODO: Record one failure as it is dead
bot.once("spawn", () => {

    startTimestamp = Date.now();

    mcData = minecraftData(bot.version);
    GLog.c(`${BOT_LOG_MSG} Minecraft version: ${bot.version}`);
    GLog.c(`${BOT_LOG_MSG} Minecraft protocol version: ${bot.protocolVersion}`);

    // load all plugins
    const { pathfinder } = require("mineflayer-pathfinder");
    const tool = require("mineflayer-tool").plugin;
    const collectBlock = require("mineflayer-collectblock").plugin;
    const pvp = require("mineflayer-pvp").plugin;
    const minecraftHawkEye = require('minecrafthawkeye');

    bot.loadPlugin(pathfinder);
    bot.loadPlugin(tool);
    bot.loadPlugin(collectBlock);
    bot.loadPlugin(pvp);
    bot.loadPlugin(minecraftHawkEye);

    // bot._client.write("keep_alive", { keepAlive: false });

    bot.chat("/difficulty peaceful");
    bot.chat("/gamerule keepInventory true");

    if (!IS_INHERIT) {
        bot.chat("/time set 0");
    }


    if (FINAL_GOAL.includes("combat") || FINAL_GOAL.includes("survive")) {
        bot.chat("/gamerule doDaylightCycle true");
    } else {
        bot.chat("/gamerule doDaylightCycle false");
    }

});

// Log errors and kick reasons:
// TODO: Try to rerun the agent after quitting
bot.on("kicked", console.log);
bot.on("error", console.log);

// module.exports = {
//     _craftItemFailCount : 0,
//     _killMobFailCount : 0,
//     _mineBlockFailCount : 0,
//     _placeItemFailCount : 0,
//     _smeltItemFailCount : 0,
// };

module.exports = {
    bot_msg,
    err_msg,
};