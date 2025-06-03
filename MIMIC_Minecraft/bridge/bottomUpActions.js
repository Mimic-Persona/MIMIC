// Imports for Mineflayer codes
const mineflayer = require("mineflayer");
const minecraftData = require("minecraft-data");
const collectBlock = require("mineflayer-collectblock");
const { Vec3 } = require("vec3");
const pvp = require('mineflayer-pvp').plugin;

const {
        Movements,
        goals: {
            Goal,
            GoalBlock,
            GoalNear,
            GoalXZ,
            GoalNearXZ,
            GoalY,
            GoalGetToBlock,
            GoalLookAtBlock,
            GoalBreakBlock,
            GoalCompositeAny,
            GoalCompositeAll,
            GoalInvert,
            GoalFollow,
            GoalPlaceBlock,
        },
        pathfinder,
        Move,
        ComputedPath,
        PartiallyComputedPath,
        XZCoordinates,
        XYZCoordinates,
        SafeBlock,
        GoalPlaceBlockOptions,
    } = require("mineflayer-pathfinder");

const craftItem = require("../skill_library/basic_skills/craftItem");
const exploreUntil = require("../skill_library/basic_skills/exploreUntil");
const givePlacedItemBack = require("../skill_library/basic_skills/givePlacedItemBack");
const killMob = require("../skill_library/basic_skills/killMob");
const mineBlock = require("../skill_library/basic_skills/mineBlock");
const placeItem = require("../skill_library/basic_skills/placeItem");
const shoot = require("../skill_library/basic_skills/shoot");
const smeltItem = require("../skill_library/basic_skills/smeltItem");
const startCombating = require("../skill_library/basic_skills/startCombating");
const {
    getItemFromChest,
    depositItemIntoChest,
    checkItemInsideChest,
    moveToChest,
    listItemsInChest,
    closeChest,
    itemByName,
} = require("../skill_library/basic_skills/useChest");

// Imports for actions
const {plan} = require("../bot_action/plan");
const {planDecide} = require("../bot_action/planDecide");
const {expect} = require("../bot_action/expect");
const codeGeneration = require("../bot_action/codeGeneration");
const getStatus = require("../env/mc_env/status");
const getInventory = require("../env/mc_env/getInventory");
const {summarize} = require("../bot_action/summarize");
const {codeDescription} = require("../bot_action/skillDescription");
let {bot_msg, err_msg} = require("./agentClient");
const {GLog} = require("../utils/GLog");
const {timePredict} = require("../bot_action/timePredict");
const {SystemMessage} = require("@langchain/core/messages");

const config = require("../config.json");
const { readFileSync, writeFileSync } = require("node:fs");

const BOT_LOG_MSG = "bridge.mc_bot:log";
const BOT_CHAT_MSG = "bridge.mc_bot:chat";
const BOT_ERR_MSG ="bridge.mc_bot:error";

/**
 * Do the bottom-up actions for the bot
 * @param finalGoal
 * @param mcData The Minecraft data
 * @param bot The Mineflayer bot
 * @param skillManager The skill manager
 * @param memoryStream The memory stream
 * @param basic_skills The basic skills
 * @param PERSONALITY The personality of the bot
 * @param RETRIEVE_IS_BOTH The flag to retrieve both preferred and related memories
 * @param BASIC_SKILL_PATH The path to the basic skills
 * @param SKILL_ROOT_PATH The path to the skill root
 * @param TIMEOUT The timeout for the code execution
 * @param PLANNER_PEAK The number of times the planner can run in the planner-only mode; if -1, it runs with whole workflow
 * @returns {Promise<*>}
 */
async function bottomUpActions(finalGoal, mcData, bot, skillManager, memoryStream,
                               basic_skills,
                               PERSONALITY, RETRIEVE_IS_BOTH,
                               BASIC_SKILL_PATH, SKILL_ROOT_PATH,
                               TIMEOUT, PLANNER_PEAK=-1) {
    GLog.resetBotMsg();
    GLog.resetErrMsg();
    bot_msg = "";
    err_msg = "";
    memoryStream.clearLatestBadPlans(); // Clear bad plans before new run

    const previousStatus = getStatus(bot);
    const previousInventory = getInventory(bot);

    // Reset the decision every time
    let planDecision = false;
    let codeDecision = true;
    let myPlan;
    let code;
    let expectation;
    let timePrediction = null;
    let planPeaks = [];

    while (PLANNER_PEAK >= 0) {
        GLog.c(`${BOT_LOG_MSG} PLANNER_PEAK: ${PLANNER_PEAK}`);

        myPlan = await plan(finalGoal, memoryStream, previousStatus, previousInventory, PERSONALITY, memoryStream.latestBadPlans, false, "bottomUp", planPeaks);
        if (myPlan === null) {
            GLog.e(`${BOT_ERR_MSG} Plan is NULL.`);
            continue;
        }

        planPeaks.push(myPlan.task);
        PLANNER_PEAK -= 1;

        if (PLANNER_PEAK === 0) {
            GLog.c(`${BOT_LOG_MSG} Plans are: ${planPeaks}`);
            return myPlan;
        }
    }

    while (!planDecision || !codeDecision){
        myPlan = await plan(finalGoal, memoryStream, previousStatus, previousInventory, PERSONALITY, memoryStream.latestBadPlans, RETRIEVE_IS_BOTH);
        if (myPlan === null) {
            GLog.e(`${BOT_ERR_MSG} Plan is NULL.`);
            continue;
        }

        planDecision = await planDecide(memoryStream, previousStatus, previousInventory, PERSONALITY, myPlan, basic_skills, mcData);
        if (planDecision === null) {
            GLog.e(`${BOT_ERR_MSG} planDecision is NULL.`);
            planDecision = false;
            continue;
        }

        if (!planDecision){
            planDecision = false;
            continue;
        }

        expectation = await expect(myPlan.task);
        if (expectation === null) GLog.e(`${BOT_ERR_MSG} Expectation is NULL.`);

        // code = await codeGeneration(memoryStream, myPlan.task, basic_skills, previousStatus, previousInventory, myPlan);
        // // console.log(BOT_LOG_MSG, "code:", code);
        // if(code === null) continue;

        // codeDecision = await codeDecide(memoryStream, previousStatus, previousInventory, PERSONALITY, myPlan, code, basic_skills, mcData);
        // // console.log(BOT_LOG_MSG, "Code Decision:", codeDecision);
        // if(codeDecision === null || !codeDecision) codeDecision = false;
    }

    let newMemory = null;
    let count = 0;
    let isTimeOut = false;

    // Time out function
    const executeWithTimeout = async (dynamicFunction, timeout) => {
        let timeoutId; // Track the timeout ID
        const timeoutPromise = new Promise((resolve, reject) => {
            timeoutId = setTimeout(() => {
                clearTimeout(timeoutId);
                reject(new Error('Execution timed out'));
            }, timeout);
        });


        try {
            await Promise.race([dynamicFunction(), timeoutPromise]);
        } catch (err) {
            GLog.e(`${BOT_ERR_MSG} TIMEOUT: ${err.message}`);
            bot.chat(`TIMEOUT: ${err.message}`);
            isTimeOut = true;
        } finally {
            // Always clear the timeout, regardless of what happens
            clearTimeout(timeoutId);
        }
    };

    // Generate the code until it successfully finishes the task or tried more than 3 times
    while((newMemory === null || newMemory.isSuccess === false) && count <= 3){
        count++;
        let memoryType = "event";
        isTimeOut = false;

        // Generate the code
        if (newMemory === null) {
            code = await codeGeneration(memoryStream, skillManager, BASIC_SKILL_PATH, SKILL_ROOT_PATH + `${PERSONALITY}/code/`,
                myPlan.task, basic_skills, previousStatus, previousInventory);
        }
        else {
            code = await codeGeneration(memoryStream, skillManager, BASIC_SKILL_PATH, SKILL_ROOT_PATH + `${PERSONALITY}/code/`,
                myPlan.task, basic_skills, previousStatus, previousInventory,
                newMemory.code, newMemory.errorMessage, bot_msg, newMemory.critique);
            GLog.resetBotMsg();
            GLog.resetErrMsg();
            bot_msg = "";
            err_msg = "";
        }
        // If nothing generated, generate again
        if (code === null) continue;

        while (timePrediction === null) {
            timePrediction = await timePredict(PERSONALITY, previousStatus, previousInventory, myPlan, expectation, code);
        }

        // GLog.c(`${BOT_LOG_MSG} Time Prediction: ${timePrediction.time}`);

        if (global.isTerminated) {
            return myPlan;
        }

        // unPause the server
        await bot.chat(`/pause`);

        if (global.isTerminated) {
            await bot.chat(`/pause`);
            return myPlan;
        }

        // Try to run the generated code
        try {
            global.RUNNING_CODE = true;
            // WARNING: this is a very dangerous way to execute code!
            // Note: the code is executed in the context of the bot entity
            // past line 1: const ${code.name} = require('../skill_library/skills/code/${PERSONALITY}/${code.name}');
            const dynamicCodeExecution = async () => {
                await eval(`(async function inject() {
                    try {
                        ${code.code}
                        await ${code.name}(bot, mcData);
                    } catch (err) {
                        GLog.e(BOT_ERR_MSG + " error: " + err.message);
                        bot.chat(\`error: \${err.message}\`);
                        memoryType = "error";
                        err_msg += err.message;
                    }
                })()`);
            }

            await executeWithTimeout(dynamicCodeExecution, timePrediction.time);

        } catch (err) {
            GLog.e(`${BOT_ERR_MSG} error: ${err.message}`);
            bot.chat(`ERROR: ${err.message}`);
            memoryType = "error";
            err_msg += err.message;
        }

        // Read the bot_msg from `../memory_system/temp_chat.txt`
        bot_msg = await readFileSync(`./memory_system/temp_chat.txt`, "utf8");
        writeFileSync(`./memory_system/temp_chat.txt`, "");

        // Pause the server after 5 seconds
        await new Promise(resolve => setTimeout(resolve, 4000));
        await bot.chat(`/pause`);
        global.RUNNING_CODE = false;

        if (global.isTerminated) {
            return myPlan;
        }

        const newStatus = getStatus(bot);
        const newInventory = getInventory(bot);

        newMemory = await summarize(basic_skills, memoryStream, memoryType,
            previousStatus, previousInventory,
            newStatus, newInventory,
            myPlan, expectation, "", code, bot_msg, err_msg, isTimeOut);

        GLog.c(`${BOT_LOG_MSG} newMemory: ${JSON.stringify(newMemory)}`);

        // If the bot does not have a harvestable tool, do not try coding again
        if ( err_msg.includes("Bot does not have a harvestable tool") ) {
            break;
        }
    }

    // TODO: Should we store it only when exceeds the number of attempts?
    // Store the skills only when success
    // if (newMemory.errorMessage === ""){

        let description = await codeDescription(code.code);

        let newSkill = await skillManager.addSkill(code.name, description, code.code, PERSONALITY, false, code.relatedSkills);
        GLog.c(`${BOT_LOG_MSG} Skill Added: ${newSkill}`);
    // }

    return myPlan;
}

module.exports = {
    bottomUpActions,
};