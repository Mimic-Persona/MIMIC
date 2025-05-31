const {decideWithRule} = require("../bot_action/planDecide");
const {MemoryStream} = require("../memory_system/MemoryStream");
const {Skill, SkillManager} = require("../skill_library/SkillManager");
const {mkdir} = require("../utils/file_utils");
const fs = require("fs");
const path = require("path");

let mcData;

const config = require('../../../../../../../../../config.json');
const OPENAI_API_KEY = config.OPENAI_API_KEY;

const { ChromaClient, OpenAIEmbeddingFunction} = require("chromadb");
const {codeDescription} = require("../bot_action/skillDescription");
const {planDecompose} = require("../bot_action/planDecompose");

async function skill_test(){
    const skillManager = new SkillManager("./skill_library/skill_test/", "test", "aggressive", true);

    await skillManager.init();
    console.log(await skillManager.getCount());

    let res1 = await skillManager.addSkill("killPig", "kill a pig1", "async function killCow(bot, mcData) {\n" +
                                    "  await killMob(bot, \"cow\");\n" +
                                    "}", "aggressive");
    console.log(await skillManager.getCount());

    let res2 = await skillManager.addSkill("killPig", "kill a pig2", "async function killPig(bot, mcData) {\n" +
                                    "  await killMob(bot, \"pig\");\n" +
                                    "}", "aggressive");
    console.log(await skillManager.getCount());

    // console.log(res1);
    // console.log(res2);

    let skills = await skillManager.retrieveSkills("pig");
    console.log(skills);
}

// skill_test();

async function memory_test(){
    const memoryStream = new MemoryStream("./memory_system/test/", "test", "aggressive", true);

    await memoryStream.init();
    // console.log(await memoryStream.getCount());

    // let res1 = await memoryStream.addMemory("event", true, 0, 0, 0,
    //     "kill 1 cow.", "cow", "kill", "i", "", "status1status1status1status1status1status1status1status1status1status1status1status1status1status1",
    //     "", "", "", "", "", "", "");
    //
    // let res2 = await memoryStream.addMemory("badPlan", false, 0, 0, 0,
    //     "kill 1 cow.", "cow", "kill", "i", "", "status2status2status2status2status2status2status2status2status2status2status2status2status2status2status2status2",
    //     "", "", "", "", "", "", "");
    //
    // let res3 = await memoryStream.addMemory("error", false, 0, 0, 0,
    //     "kill 1 cow.", "cow", "kill", "i", "", "status3status3status3status3status3status3status3status3status3status3status3status3status3status3status3",
    //     "", "", "", "", "", "", "");

    // console.log(res1);
    // console.log(res2);
    // console.log(res3);

    // await memoryStream.printAllMemories("HERE:");

    let memories = await memoryStream.retrieveMemories("wood", false);
    console.log(memories);
}

async function test() {
    const client = new ChromaClient();
    const embedder = new OpenAIEmbeddingFunction({ openai_api_key: OPENAI_API_KEY })

    let collection = await client.createCollection({
        name: "hi11231321322",
        embeddingFunction: embedder,
    });
}

async function parse_test() {
    let myPlan = {
        reasoning: "I want to explore more and find the entrance to the next level of the dungeon. I should move to the tile [4, 21] since it is a door and might lead to new areas.",
        task: "Act [4, 21]",
        action: "act",
        tile: [4, 21],
        item1: "null",
        item2: "null"
    }

    return JSON.stringify(myPlan)
}

console.log(parse_test());

