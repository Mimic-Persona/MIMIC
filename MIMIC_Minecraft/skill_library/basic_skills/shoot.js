// shoot 1 pig with a bow: shoot(bot, mcData, "bow", "pig");
async function shoot(bot, mcData, weapon, target) {
    const validWeapons = [
        "bow",
        "crossbow",
        "snowball",
        "ender_pearl",
        "egg",
        "splash_potion",
        "trident",
    ];
    if (!validWeapons.includes(weapon)) {
        bot.chat(`${weapon} is not a valid weapon for shooting`);
        return;
    }

    const weaponItem = mcData.itemsByName[weapon];
    if (!bot.inventory.findInventoryItem(weaponItem.id, null)) {
        bot.chat(`No ${weapon} in inventory for shooting`);
        return;
    }

    const targetEntity = bot.nearestEntity(
        (entity) =>
            entity.name === target
    );
    if (!targetEntity) {
        bot.chat(`No ${target} nearby`);
        return;
    }
    bot.hawkEye.autoAttack(targetEntity, weapon);
    bot.on('auto_shot_stopped', (target) => {
    })
}

module.exports = shoot;
