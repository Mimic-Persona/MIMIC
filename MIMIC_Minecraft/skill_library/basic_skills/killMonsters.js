// Load the required modules
const { GoalBlock } = require('mineflayer-pathfinder').goals;

const equipSword = require('./equipSword');
const equipArmor = require('./equipArmor');
const killMob = require('./killMob');

async function killMonsters(bot, mcData, type = null, count = 1) {
  await bot.chat("/gamemode survival");
  isAlive = true;

  // Listen for bot's death
  bot.on('death', () => {
    bot.chat("I lost the combat.");
    isAlive = false;
    return false;
  });

  await equipSword(bot, mcData);
  await equipArmor(bot, mcData);

  for (i = 0; i < count; i++) {
    if (!isAlive) {
      return false;
    }
    // // Find the nearest monster
    // const monster = bot.nearestEntity(entity => {
    //   return entity.name === type && entity.position.distanceTo(bot.entity.position) < 32;
    // });

    // wait for the mob to spawn
    await new Promise((resolve) => setTimeout(resolve, 100));

    // Kill the mob using the sword
    await killMob(bot, type, 300);
    await bot.chat(`Killed a ${type}.`);
  }

  await bot.chat("I won the combat.");
  await bot.chat("/gamemode survival");
  return true;
}

module.exports = killMonsters;