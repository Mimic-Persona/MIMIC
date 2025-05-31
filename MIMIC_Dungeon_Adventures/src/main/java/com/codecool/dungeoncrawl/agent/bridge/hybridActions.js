const {bottomUpActions} = require("./bottomUpActions");
const {topDownActions} = require("./topDownActions");

/**
 * Do the hybrid actions for the bot
 * @param socket The WebSocket connection
 * @param skillManager The skill manager
 * @param memoryStream The memory stream
 * @param PERSONALITY The personality of the bot
 * @param RETRIEVE_IS_BOTH The flag to retrieve both preferred and related memories
 * @param SKILL_ROOT_PATH The root path for the skills
 * @param TIMEOUT The timeout for the code execution
 * @param {int} cnt The number of times repeated tasks appear continuously
 * @param isBottomUp The flag to indicate if the planner is bottom-up
 * @param switchCondition If "D", it is switched based on the diversity of the plan; if "S", it is switched based on the number of plans we have in the skill library; if "H", it is hybrid.
 * @param thresholdD The number of times repeated tasks can continuously appear as threshold for "D"
 * @param thresholdS The number of skills as threshold for changing the planner type
 * @returns {Promise<{isBottomUp: boolean, cnt: int}>}
 */
async function hybridActions(socket, skillManager, memoryStream,
                             PERSONALITY,
                             RETRIEVE_IS_BOTH, SKILL_ROOT_PATH,
                             TIMEOUT,
                             cnt, isBottomUp = true,
                             switchCondition="S", thresholdD=20, thresholdS=30) {

    let changed = false;

    // Switch to top down if having enough skills
    if (isBottomUp && (switchCondition === "S" || switchCondition === "H") &&
        (memoryStream.memoryCount >= thresholdS && memoryStream.memoryCount < thresholdS + 10)) {
        isBottomUp = false;
        changed = true;
    }

    if (isBottomUp) {
        let newTask = await bottomUpActions(socket, skillManager, memoryStream, PERSONALITY, RETRIEVE_IS_BOTH, TIMEOUT);

        cnt += 1;
        // if task is new, reset the counter
        if (!memoryStream.hasTask(newTask)) {
            cnt = 0;
        }

    } else {
        let subTasks = await topDownActions(socket, skillManager, memoryStream, PERSONALITY, RETRIEVE_IS_BOTH, SKILL_ROOT_PATH, TIMEOUT);

        for (const subTask of subTasks) {
            cnt += 1;
            // if task is new, reset the counter
            if (!memoryStream.hasTask(subTask)) {
                cnt = 0;
            }
        }
    }

    // if the repeated task appears for larger than thresholdD, switch
    if (!changed && (switchCondition === "D" || switchCondition === "H") && (cnt >= thresholdD)) {
        isBottomUp = !isBottomUp;
    }

    return {
        isBottomUp: isBottomUp,
        cnt: cnt,
    };
}

module.exports = {
    hybridActions,
};