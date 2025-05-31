const {sendMessage} = require("./sendMessage");

/**
 * Get the status of the bot
 * @param socket The WebSocket connection
 * @returns {Promise<JSON>} The status of the hero in the game
 */
async function getStatus(socket) {
    sendMessage(socket, "GetStatus");

    return new Promise(function(resolve) {
        let timeout = setTimeout(() => {
            // Timeout after 10 seconds and send another message
            sendMessage(socket, "No status response received within 10 seconds.");
            sendMessage(socket, "GetStatus");

        }, 10000); // 10 seconds

        socket.onmessage = async function(event) {
            let msg = JSON.parse(event.data);

            if (msg.msgType === "status") {
                clearTimeout(timeout); // Clear the timeout if the message is received in time
                resolve(msg);
            }
        };
    });
}

/**
 * Send action request and get the feedback from the server
 * @param socket The WebSocket connection
 * @param plan The plan to act
 * @returns {Promise<JSON>}
 */
async function actAndFeedback(socket, plan) {
    sendMessage(socket, `ACTION: ${JSON.stringify(plan)}`);

    return new Promise(function(resolve) {
        let timeout = setTimeout(() => {
            // Timeout after 10 seconds and send another message
            sendMessage(socket, "No feedback response received within 10 seconds.");
            sendMessage(socket, `ACTION: ${JSON.stringify(plan)}`);

        }, 10000); // 10 seconds

        socket.onmessage = async function(event) {
            let msg = JSON.parse(event.data);

            if (msg.msgType === "feedback") {
                clearTimeout(timeout); // Clear the timeout if the message is received in time
                resolve(msg);
            }
        };
    });
}

/**
 * Send code running request and get the feedback from the server
 * @param socket The WebSocket connection
 * @param code The code to be compiled and run
 * @returns {Promise<JSON>}
 */
async function runAndFeedback(socket, code) {
    sendMessage(socket, `ACTION: ${JSON.stringify(code)}`);

    return new Promise(function(resolve) {
        socket.onmessage = async function (event) {
            let msg = JSON.parse(event.data);

            if (msg.msgType === "feedback") {
                resolve(msg);
            }
        };
    });
}

/**
 * Convert the status to a prompt
 * @param {JSON} status The status of the hero in the game
 * @param {String} prefix The prefix of the prompt
 * @returns {String} The prompt of the status
 */
function status2Prompt(status, prefix="") {
    let res= "";

    // Hero Status
    res += `${prefix}health: ${status.health}/${status.maxHealth}\n`;
    res += `${prefix}level: ${status.level}\n`;
    res += `${prefix}experience: ${status.experience}/${status.maxExperience}\n`;
    res += `${prefix}strength: ${status.strength}\n`;
    res += `${prefix}gold: ${status.gold}\n`;
    res += `${prefix}hero position in xy: [${status.heroPositionInXY}]\n`;
    res += `${prefix}buffs/debuffs: ${JSON.stringify(status.buffs)}\n`;

    // Hero Talents
    res += `${prefix}free talent points: ${JSON.stringify(status.freeTalentPoints)}\n`;
    res += `${prefix}talents: ${JSON.stringify(status.currTalents)}\n`;

    // Hero Equipment
    res += `${prefix}equipments: ${JSON.stringify(status.equipments)}\n`;

    // Hero Inventory
    res += `${prefix}inventory: ${JSON.stringify(status.items)}\n`;
    res += `${prefix}keys: ${JSON.stringify(status.keys)}\n`;

    // Environment
    res += `${prefix}depth: ${status.depth}\n`;
    res += `${prefix}environment: ${JSON.stringify(status.environment)}\n`;

    return res;
}

module.exports = {
    getStatus,
    status2Prompt,
    actAndFeedback,
    runAndFeedback,
};