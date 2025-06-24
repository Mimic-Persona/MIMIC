// This function is used to start combating with the mob given by the combating tasks.
// You should call this function once you are provided with the task for start combating.

async function startCombating(bot, mcData) {
    // Create a combat environment
    await createCombatEnv(bot, mcData);

    // Summon monsters claimed by the ultimate goal
    await summonMonsters(bot, mcData, config.MONSTER_TYPE);

    await bot.chat("Mobs summoned.")

    // Equip all the good stuff, including armor and sword, and fight the monsters until you kill them or die
    await killMonsters(bot, mcData, config.MONSTER_TYPE, 1);
}
