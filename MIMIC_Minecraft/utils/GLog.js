const fs = require('fs');
const path = require('path');
const config = require('../config.json');

// Function to append text to a file, creating the directory if it doesn't exist
function appendToFile(filePath, text) {
    const dirPath = path.dirname(filePath);

    // Check if the directory exists, if not, create it
    if (!fs.existsSync(dirPath)) {
        fs.mkdirSync(dirPath, { recursive: true });
    }

    fs.appendFileSync(filePath, text + '\n', { encoding: 'utf8' });
}

class GLog {
    static TAG = "GAME";
    static HIGHLIGHT = "@@ ";
    static CONNECTION = "$$ ";
    static AGENT_ERROR = "!! ";
    static NEW_LINE = "\n";

    static formatterOptions = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    };

    static formatLocalDateTime() {
        const date = new Date();
        return date.toISOString().split('.')[0].replace(/:/g, '-').replace('T', '_');
    }

    static OUT_PATH = path.join('..', 'log', 'MC', `${config.TASK}`, `${config.TASK}_${config.REPORT_PREFIX}_${GLog.formatLocalDateTime()}.log`);
    static isFirstLog = true;
    static BOT_MSG = "";
    static ERR_MSG = "";

    static i(text, ...args) {
        if (text.startsWith(GLog.AGENT_ERROR)) {
            GLog.ERR_MSG += text.substring(3) + GLog.NEW_LINE;
        } else if (!text.startsWith(GLog.CONNECTION)) {
            GLog.BOT_MSG += text.substring(3) + GLog.NEW_LINE;
        }

        console.log(text);

        if (GLog.isFirstLog) {
            appendToFile(
                GLog.OUT_PATH,
                "======================================================================== " + new Date().toISOString() + " ========================================================================"
            );
            GLog.isFirstLog = false;
        }
        appendToFile(GLog.OUT_PATH, new Date().toISOString() + "\t" + text);
    }

    static h(text, ...args) {
        GLog.i(GLog.HIGHLIGHT + text, ...args);
    }

    static c(text, ...args) {
        GLog.i(GLog.CONNECTION + text, ...args);
    }

    static e(text, ...args) {
        GLog.i(GLog.AGENT_ERROR + text, ...args);
    }

    static resetBotMsg() {
        GLog.BOT_MSG = "";
    }

    static resetErrMsg() {
        GLog.ERR_MSG = "";
    }
}

module.exports = {
    GLog,
    appendToFile,
};