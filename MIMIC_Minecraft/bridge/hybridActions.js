const {bottomUpActions} = require("./bottomUpActions");
const {topDownActions} = require("./topDownActions");

/**
 *
 * @param finalGoal
 * @param mcData
 * @param bot
 * @param skillManager
 * @param memoryStream
 * @param basic_skills
 * @param PERSONALITY
 * @param RETRIEVE_IS_BOTH
 * @param BASIC_SKILL_PATH
 * @param SKILL_ROOT_PATH
 * @param TIMEOUT
 * @param {int} cnt
 * @param isBottomUp
 * @param switchCondition If "D", it is switched based on the diversity of the plan; if "S", it is switched based on the number of plans we have in the skill library; if "H", it is hybrid.
 * @param thresholdD The number of times repeated tasks can continuously appear as threshold for "D"
 * @param thresholdS The number of skills as threshold for changing the planner type
 * @returns {Promise<{isBottomUp: boolean, cnt: int}>}
 */
async function hybridActions(finalGoal, mcData, bot, skillManager, memoryStream,
                             basic_skills,
                             PERSONALITY, RETRIEVE_IS_BOTH,
                             BASIC_SKILL_PATH, SKILL_ROOT_PATH,
                             TIMEOUT,
                             cnt, isBottomUp = true,
                             switchCondition="S", thresholdD=5, thresholdS=5) {

    let changed = false;

    // Switch to top down if having enough skills
    if (isBottomUp && (switchCondition === "S" || switchCondition === "H") &&
        (skillManager.skillCount >= thresholdS && skillManager.skillCount < thresholdS + 5)) {
        isBottomUp = false;
        changed = true;
    }

    if (isBottomUp) {
        let newTask = await bottomUpActions(finalGoal, mcData, bot, skillManager, memoryStream, basic_skills, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT);

        cnt += 1;
        // if the task is new, reset the counter
        const tasks = await memoryStream.getTasks(newTask); // Resolve the promise
        if (!tasks.has(newTask)) {
            cnt = 0;
        }

    } else {
        let subTasks = await topDownActions(finalGoal, mcData, bot, skillManager, memoryStream, basic_skills, PERSONALITY, RETRIEVE_IS_BOTH, BASIC_SKILL_PATH, SKILL_ROOT_PATH, TIMEOUT);

        for (const subTask of subTasks) {
            cnt += 1;
            // if the task is new, reset the counter
            const tasks = await memoryStream.getTasks(subTask); // Resolve the promise
            if (!tasks.has(subTask)) {
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