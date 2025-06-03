/**
 * Convert the status to a prompt
 * @param {JSON} status The status of the bot in the game
 * @param inventory The inventory of the bot
 * @param {String} prefix The prefix of the prompt
 * @returns {String} The prompt of the status
 */
function status2Prompt(status, inventory, prefix="") {
    let res= "";

    res += `${prefix}Biome: ${status.biome}\n`;
    res += `${prefix}Time: ${status.timeOfDay}\n`;
    res += `${prefix}Nearby blocks: ${Array.from(status.blocks).join(', ')}\n`;
    res += `${prefix}Nearby entities (nearest to farthest): ${JSON.stringify(status.entities)}\n`;
    res += `${prefix}Health: ${status.health}\n`;
    res += `${prefix}Hunger: ${status.food}\n`;
    res += `${prefix}Position: ${JSON.stringify(status.position)}\n`;
    res += `${prefix}Equipment: ${JSON.stringify(status.equipment)}\n`;
    res += `${prefix}Inventory(${Object.keys(inventory).length}/36): ${JSON.stringify(inventory)}\n`;

    return res;
}

module.exports = {
    status2Prompt,
};