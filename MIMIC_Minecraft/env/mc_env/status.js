function getStatus(bot) {
    // TODO: JUSTIFICATION
    return {
        health: bot.health,
        food: bot.food,
        saturation: bot.foodSaturation,
        oxygen: bot.oxygenLevel,
        position: bot.entity.position,
        velocity: bot.entity.velocity,
        yaw: bot.entity.yaw,
        pitch: bot.entity.pitch,
        onGround: bot.entity.onGround,
        equipment: getEquipment(bot),
        name: bot.entity.username,
        timeSinceOnGround: bot.entity.timeSinceOnGround,
        isInWater: bot.entity.isInWater,
        isInLava: bot.entity.isInLava,
        isInWeb: bot.entity.isInWeb,
        isCollidedHorizontally: bot.entity.isCollidedHorizontally,
        isCollidedVertically: bot.entity.isCollidedVertically,
        biome: bot.blockAt(bot.entity.position)
            ? bot.blockAt(bot.entity.position).biome.name
            : "None",
        entities: getEntities(bot),
        timeOfDay: getTime(bot),
        blocks: getSurroundingBlocks(bot, 16, 4, 16),
    };
}

function itemToObs(item) {
    if (!item) return null;
    return item.name;
}

function getTime(bot) {
    const timeOfDay = bot.time.timeOfDay;
    let time = "";
    if (timeOfDay < 1000) {
        time = "sunrise";
    } else if (timeOfDay < 6000) {
        time = "day";
    } else if (timeOfDay < 12000) {
        time = "noon";
    } else if (timeOfDay < 13000) {
        time = "sunset";
    } else if (timeOfDay < 18000) {
        time = "night";
    } else if (timeOfDay < 22000) {
        time = "midnight";
    } else {
        time = "sunrise";
    }
    return time;
}

    // For each item in equipment, if it exists, return the name of the item
    // otherwise return null
function getEquipment(bot) {
    const slots = bot.inventory.slots;
    const mainHand = bot.heldItem;
    return slots
        .slice(5, 9)
        .concat(mainHand, slots[45])
        .map(itemToObs);
}

function getEntities(bot) {
    const entities = bot.entities;
    if (!entities) return {};
    const mobs = {};
    for (const id in entities) {
        const entity = entities[id];
        if (!entity.displayName) continue;
        if (entity.name === "player" || entity.name === "item") continue;
        if (entity.position.distanceTo(bot.entity.position) < 32) {
            if (!mobs[entity.name]) {
                mobs[entity.name] = entity.position.distanceTo(
                    bot.entity.position
                );
            } else if (
                mobs[entity.name] >
                entity.position.distanceTo(bot.entity.position)
            ) {
                mobs[entity.name] = entity.position.distanceTo(
                    bot.entity.position
                );
            }
        }
    }
    return mobs;
}

function getSurroundingBlocks(bot, x_distance, y_distance, z_distance) {
    const surroundingBlocks = new Set();

    for (let x = -x_distance; x <= x_distance; x++) {
        for (let y = -y_distance; y <= y_distance; y++) {
            for (let z = -z_distance; z <= z_distance; z++) {
                const block = bot.blockAt(bot.entity.position.offset(x, y, z));

                if (block.position.distanceTo(bot.entity.position) > 32) continue;

                if (block && block.type !== 0) {
                    surroundingBlocks.add(block.name);
                }
            }
        }
    }
    // console.log(surroundingBlocks);
    return surroundingBlocks;
}

module.exports = getStatus;
