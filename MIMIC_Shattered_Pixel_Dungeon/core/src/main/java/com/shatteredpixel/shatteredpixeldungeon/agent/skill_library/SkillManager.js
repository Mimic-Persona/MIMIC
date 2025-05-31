const { listFiles, mkdir, writeFile, loadFile, loadSkills} = require("../utils/file_utils");
const {ChromaClient, OpenAIEmbeddingFunction} = require("chromadb");
const {sendMessage} = require("../bridge/sendMessage");

const config = require('../../../../../../../../../config.json');
const OPENAI_API_KEY = config.OPENAI_API_KEY;

const BOT_LOG_MSG = "skill_library.SkillManager:log";
const BOT_ERR_MSG = "skill_library.SkillManager:error";

class Skill {
    /**
     *
     * @param {String} name The name of the skill
     * @param {String} description The description of the skill
     * @param {String} code The code of the skill
     */
    constructor(name, description, code) {
        this.name = name;
        this.description = description;
        this.code = code;
    }

    /**
     * Summarize the info of current skill
     * @returns {Promise<{code: String, name: String, description: String}>}
     */
    async summary() {
        return {
            name: this.name,
            description: this.description,
            code: this.code
        }
    }
}


class SkillManager {
    /**
     * SkillManager help manage the skill library
     * @param socket
     * @param {String} rootPath The root path to store the skills1
     * @param {String} collectionName
     * @param {String} persona The persona used for the agent
     * @param {boolean} isInherit If the skill manager is inheriting the previous skill library
     * @param {number} retrieverTopN
     * @returns {Promise<void>}
     */
    constructor(socket, rootPath, collectionName, persona, isInherit = false, retrieverTopN = 5) {
        this.rootPath = rootPath;
        this.collectionName = collectionName;
        this.persona = persona;
        this.retrieverTopN = retrieverTopN;
        this.isInherit = isInherit;

        this.skillCount = 0;
        this.skills = {};

        this.client = new ChromaClient();
        this.embedder = new OpenAIEmbeddingFunction({ openai_api_key: OPENAI_API_KEY });

        this.socket = socket;
    }

    /**
     * Initialize the folders and the vectordb
     * @returns {Promise<void>}
     */
    async init() {
        // JUSTIFY HERE WHEN TESTING
        this.skills_import = await loadFile("./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/skill_library/skills_import.txt", BOT_ERR_MSG);

        // Create the folders
        mkdir(this.rootPath, "", BOT_LOG_MSG, BOT_ERR_MSG);
        mkdir(this.rootPath, this.persona, BOT_LOG_MSG, BOT_ERR_MSG);
        mkdir(this.rootPath, `${this.persona}/code`, BOT_LOG_MSG, BOT_ERR_MSG);
        mkdir(this.rootPath, `${this.persona}/description`, BOT_LOG_MSG, BOT_ERR_MSG);
        // mkdir(this.rootPath, `${this.persona}/vectordb`, BOT_LOG_MSG, BOT_ERR_MSG);

        // Delete the collection might be created before
        try {
            await this.client.deleteCollection({name: `${this.persona}_skill_collection_${this.collectionName}`});
        } catch (e) {
            if (!e.message.includes("not be found")) throw e;
        }

        // Create the new collection
        this.vectorStore = await this.client.createCollection({
            name: `${this.persona}_skill_collection_${this.collectionName}`,
            embeddingFunction: this.embedder,
        });

        // Do inheritance if needed
        if (this.isInherit) {
            try {
                await this.inheritHistory();
                sendMessage(this.socket, `${BOT_LOG_MSG} ${this.persona}_skill_collection_${this.collectionName} fetched successfully.`);
            } catch (err) {
                sendMessage(this.socket, `${BOT_ERR_MSG} Error fetching ${this.persona}_skill_collection_${this.collectionName}: ${err}`);
            }
        } else {
            sendMessage(this.socket, `${BOT_LOG_MSG} ${this.persona}_skill_collection_${this.collectionName} created successfully.`);
        }
    }

    async getCount() {
        return {
            libraryCount: this.skillCount,
            vectorDBCount: await this.vectorStore.count(),
        }
    }

    /**
     * Inherit the history from existed skill library
     * @returns {Promise<void>}
     */
    async inheritHistory() {
        let fileNames = listFiles(`${this.rootPath}/${this.persona}/code`);

        let names = [];
        for (let name of fileNames) {
            names.push(name.replace(".js", ""));
        }

        let codes = [];
        for (let name of names) {
            let code = await loadSkills([name], `${this.rootPath}/${this.persona}/code/`);
            code = code.slice(code.indexOf("const {Vec3} = require(\"vec3\");") + 31, code.indexOf("module.exports")).trim();
            codes.push(code);
        }

        let descriptions = [];
        for (let name of names) {
            let description = await loadSkills([name], `${this.rootPath}/${this.persona}/description/`, false);
            descriptions.push(description);
        }

        for (let i in names) {
            await this.addSkill(names[i], descriptions[i], codes[i], this.persona, true);
        }
    }

    /**
     * Add the skill into skill library
     * @param {String} name The name of the skill
     * @param {String} description The description of the skill
     * @param {String} code The code of the skill
     * @param {String} persona The persona of current agent
     * @param {boolean} isInheriting If it is currently inheriting the previous skill library, skip the file writing part
     * @param relatedSkills
     * @returns
     */
    async addSkill(name, description, code, persona, isInheriting=false, relatedSkills=[]) {
        // TODO: New skills are now rewriting the previous
        const newSkill = new Skill(name, description, code);

        if (!Object.keys(this.skills).includes(name))
            this.skillCount += 1;

        this.skills[name] = newSkill;

        if (!isInheriting) {
            await writeFile(`${this.rootPath}/${persona}/description/${name}.txt`,
            `async function ${name}(bot, mcData) {\n\t// ${description}\n};`, BOT_LOG_MSG, BOT_ERR_MSG);

            // Add imports for related skills into code
            let RSImports = "";
            for (let skl of relatedSkills) {
                if (skl !== name)
                    RSImports += `const {${skl}} = require ("./${skl}.js");\n\n`
            }

            await writeFile(`${this.rootPath}/${persona}/code/${name}.js`,
                this.skills_import + RSImports + code + `\nmodule.exports = ${name};`, BOT_LOG_MSG, BOT_ERR_MSG);
        }

        if (await this.vectorStore.add({
            ids: [name],
            metadatas: [{ skillName: name }],
            documents: [description],
        }))
            return name;

        return false;
    }

    /**
     * Retrieve the skills using vectordb
     * @param {String} query The query for the vectordb
     * @param isNameOnly
     * @param {boolean} isPrint If the answer of the query should be printed
     * @returns {Promise<*[]>}
     */
    async retrieveSkills(query, isNameOnly=true, isPrint=true) {
        const results = await this.vectorStore.query({
            nResults: this.retrieverTopN,
            queryTexts: [query],
        });

        let skills = [];
        let skillNames = [];

        for (let i = 0; i < results.ids[0].length; i++) {
            skills.push(this.skills[results.ids[0][i]]);
        }

        for (let i = 0; i < results.ids[0].length; i++) {
            skillNames.push(this.skills[results.ids[0][i]].name);
        }

        if (isPrint)
            sendMessage(this.socket, `${BOT_LOG_MSG} Skills Retrieved by "${query}": ${skillNames}`);

        if (isNameOnly) return skillNames;
        return skills;
    }

    async clean() {
        await this.client.deleteCollection({name: `${this.persona}_skill_collection_${this.collectionName}`});
    }

    async printAllSkills(logName) {
        const skillNames = [];
        for (let id in this.skills) {
            skillNames.push(this.skills[id].name);
        }
        sendMessage(this.socket, `${logName} The number is: ${await JSON.stringify(this.getCount())} \nThe skills are:\n ${JSON.stringify(skillNames)}`);
    }
}

module.exports = {
    Skill,
    SkillManager,
};