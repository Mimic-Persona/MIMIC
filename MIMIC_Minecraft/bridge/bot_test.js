/**
 * This bot example show how to direct a bot to collect a specific block type
 * or a group of nearby blocks of that type.
 */

const mineflayer = require('mineflayer')
const {GLog} = require("../utils/GLog");
const {plan} = require("../bot_action/plan");
const {SkillManager} = require("../skill_library/SkillManager");
const {MemoryStream} = require("../memory_system/MemoryStream");
const getStatus = require("../env/mc_env/status");
const getInventory = require("../env/mc_env/getInventory");
const {planDecide} = require("../bot_action/planDecide");
const collectBlock = require('mineflayer-collectblock').plugin
const startCombating = require("../skill_library/basic_skills/startCombating");

const BOT_LOG_MSG = "bridge.mc_bot:log";
const BOT_CHAT_MSG = "bridge.mc_bot:chat";
const BOT_ERR_MSG ="bridge.mc_bot:error";

// RANDOM SEED 11111
const COLLECTION_NAME = "tt";
const RETRIEVE_IS_BOTH = true;
const BASIC_SKILL_PATH = "./skill_library/basic_skills_prompts/";
const SKILL_ROOT_PATH = `./skill_library/skill_${COLLECTION_NAME}/`;
const MEMORY_ROOT_PATH = `./memory_system/${COLLECTION_NAME}/`;
const IS_INHERIT = false;   // If continue the old memories
const TIMEOUT = 10 * 60000;   // Code timeout in milliseconds (10 mins)

const PERSONALITY = "aggressive";
// const PERSONALITY = "conservative";

const PLANNER_TYPE = "bottomUp"; // "topDown", "bottomUp", "hybrid"
const PLANNER_SWITCH_COND = "H"; // "S", "D", "H"
const THRESHOLD_S = 10; // The number of skills as a threshold for changing the planner type
const THRESHOLD_D = 10; // The number of times repeated tasks can continuously appear as a threshold for "D"

let isBottomUp = true;
let dCounter = 0; // The number of times repeated tasks appears continuously

const basic_skills = ["craftItem", "exploreUntil", "killMob", "mineBlock", "mineflayer", "placeItem", "smeltItem"];

let mcData;
let memoryType = "event";
let bot_msg = "";
let err_msg = "";

const bot = mineflayer.createBot({
    username: "test",
    host: "localhost",
    port: 58509,
    verbose: true,
})

// const skillManager = new SkillManager(SKILL_ROOT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
// skillManager.init();
//
// const memoryStream = new MemoryStream(MEMORY_ROOT_PATH, COLLECTION_NAME, PERSONALITY, IS_INHERIT);
// memoryStream.init();

bot.loadPlugin(collectBlock)

bot.once('spawn', () => {
    mcData = require('minecraft-data')(bot.version)

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

    GLog.c("Hi");
})

// bot.on('chat', async (username, message) => {
//     memoryStream.clearLatestBadPlans(); // Clear bad plans before new run
//
//     const previousStatus = getStatus(bot);
//     const previousInventory = await getInventory(bot);
//
//     let myPlan = await plan("survive_for_1_day", memoryStream, previousStatus, previousInventory, PERSONALITY, memoryStream.latestBadPlans, RETRIEVE_IS_BOTH);
//
//     let planDecision = await planDecide(memoryStream, previousStatus, previousInventory, PERSONALITY, myPlan, basic_skills, mcData);
// })


bot.on('chat', async (username, message) => {
    if (username !== bot.username) {
        await startCombating(bot, mcData);
    }
})