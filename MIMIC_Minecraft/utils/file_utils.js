const fs = require('fs');
const path = require("path");
const fsp = require('fs').promises;


/**
 * Load the skills as a plain text from the given path.
 * @param input_names The skill files needed to be read; if is null, read all files in the folder
 * @param path The path to the folder contains the skill files
 * @param isJSFile If the file is code file (.js); or the file is description file (.txt)
 * @returns {string}
 */
async function loadSkills(input_names=null, path, isJSFile=true) {
    if (input_names === null) {
        const skill_names = fs.readdirSync(path);

        for (let i in skill_names){
            if (skill_names[i].endsWith(".js")) {
                skill_names[i] = skill_names[i].slice(0, -3);
            }

            if (skill_names[i].endsWith(".txt")) {
                skill_names[i] = skill_names[i].slice(0, -4);
            }
        }
        input_names = skill_names
    }

    let skills = ""
    if (isJSFile) {
        for (let i in input_names){
            skills += await fsp.readFile(path + input_names[i] + ".js", 'utf8') + "\n";
        }
    } else {
        for (let i in input_names){
            skills += await fsp.readFile(path + input_names[i] + ".txt", 'utf8') + "\n";
        }
    }

    return skills;
}

async function writeFile(path, content, log_msg, err_msg) {
    console.log(log_msg, `Writing ${path}...`);

    try {
        await fsp.writeFile(path, content);
        console.log(log_msg, `"${path}" written successfully`);
    } catch (err) {
        console.log(err_msg, `Error writing "${path}":`, err);
    }
}

async function loadFile(path, err_msg){
    try {
        return await fsp.readFile(path, 'utf8');
    } catch (err) {
        console.log(err_msg, err);
    }
}

function listFiles(path, err_msg) {
    try {
        return fs.readdirSync(path);
    } catch (err) {
        console.log(err_msg, err);
    }
}

function mkdir(rootPath, folderName, log_msg, err_msg){
    try {
        if (!fs.existsSync(path.join(rootPath, folderName))) {
            fs.mkdirSync(path.join(rootPath, folderName));
            console.log(log_msg, `folder "${folderName}" created in ${rootPath}.`);
        }
    } catch (err) {
        console.log(err_msg, err);
    }
}

async function writeJSON(path, json, log_msg, err_msg) {
    console.log(log_msg, `Writing ${path}...`);

    try {
        await fsp.writeFile(path, JSON.stringify(json));
        console.log(log_msg, `"${path}" written successfully`);
    } catch (err) {
        console.log(err_msg, `Error writing "${path}":`, err);
    }
}

module.exports = {
    loadSkills,
    writeFile,
    loadFile,
    mkdir,
    listFiles,
    writeJSON,
};