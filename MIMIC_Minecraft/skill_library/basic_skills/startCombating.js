const config = require("../../config.json");
const killMonsters = require("./killMonsters");

async function startCombating(bot, mcData) {
    // kill potential mobs
    await bot.chat("/difficulty peaceful")

    // env set
    await bot.chat(`/fill ~${-15} ${100} ~${-15} ~${15} ${110} ~${15} minecraft:sea_lantern`);
    await bot.chat(`/fill ~${-14} ${101} ~${-14} ~${14} ${109} ~${14} minecraft:air`);
    await bot.chat(`/tp @p ~ ${101} ~`);

    // set difficulty to easy for summoning mobs
    await bot.chat("/difficulty easy")

    // wait for the difficulty to change
    await new Promise((resolve) => setTimeout(resolve, 100));

    await bot.chat("Summoning mob...")
    function getRandomNumber(r) {
        const sign = Math.random() < 0.5 ? -1 : 1;
        const randomValue = Math.random() * r;
        return sign * (randomValue + r);
    }

    for (let i = 0; i < 1; i++) {
        let x = getRandomNumber(5);
        let z = getRandomNumber(5);
        await bot.chat(`/summon minecraft:${config.MONSTER_TYPE} ~${x} ~1 ~${z}`);
    }
    await bot.chat("Mobs summoned.")

    await killMonsters(bot, mcData, config.MONSTER_TYPE, 1);

    await bot.chat("/difficulty peaceful");
}

module.exports = startCombating;