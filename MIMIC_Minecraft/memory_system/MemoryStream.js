const { ChromaClient, OpenAIEmbeddingFunction } = require("chromadb");
const { listFiles, mkdir, writeFile, loadFile, loadSkills, writeJSON } = require("../utils/file_utils");
const {preferenceAnalyze} = require("../bot_action/preferenceAnalyze");
const {GLog} = require("../utils/GLog");

const config = require('../config.json');
const OPENAI_API_KEY = config.OPENAI_API_KEY;
const CHROMA_DB_PORT = config.CHROMA_DB_PORT;

const BOT_LOG_MSG = "memory_stream.MemoryStream:log";
const BOT_ERR_MSG = "memory_stream.MemoryStream:error";
const INF = Math.pow(10, 1000);

class Memory{
    constructor(memoryID, memoryType, isSuccess,
                timeCreated, timeExpired, lastAccessed,
                task, subject, verb, object, biome,
                previousStatus, planReason, decideReason, summarizeReason, code, skills, critique, errorMessage) {

        // Basic Info
        this.memoryID = memoryID;
        this.memoryType = memoryType;   // event / thought / error feedback
        this.isSuccess = isSuccess;     // If it is event, if it is a success

        // Time related
        this.timeCreated = timeCreated;
        this.timeExpired = timeExpired;
        this.lastAccessed = lastAccessed;

        // Relevance parameters
        this.task = task;
        this.subject = subject;
        this.verb = verb;
        this.object = object;
        this.biome = biome;

        // Preference parameters
        this.preference = -1;

        // Details
        this.previousStatus = previousStatus;
        this.planReason = planReason;
        this.decideReason = decideReason;
        this.summarizeReason = summarizeReason;
        this.code = code;
        this.skills = skills;
        this.critique = critique;
        this.errorMessage = errorMessage;
    }

    /**
     * Get the S.P.O.B summary of this Memory
     * @returns {(*|string)[]} The S.P.O.B of this Memory
     */
    svob_summary(){
        return [this.subject, this.verb, this.object, this.biome];
    }

    planSummary(){
        return {
            task: this.task,
            isSuccess: this.isSuccess,
            critique: this.critique,
        }
    }

    eventSummary(codeWanted = true){
        if (codeWanted)
            return{
            task: this.task,
            code: this.code,
            previousStatus: this.previousStatus,
            isSuccess: this.isSuccess,
            critique: this.critique,
            };

        return{
            task: this.task,
            previousStatus: this.previousStatus,
            isSuccess: this.isSuccess,
            critique: this.critique,
        }
    }

    errorSummary(codeWanted = true){
        if (codeWanted)
            return{
            task: this.task,
            code: this.code,
            previousStatus: this.previousStatus,
            errorMessage: this.errorMessage,
            critique: this.critique,
            };

        return{
            task: this.task,
            previousStatus: this.previousStatus,
            errorMessage: this.errorMessage,
            critique: this.critique,
        }
    }

    badPlanSummary(){
        return{
            task: this.task,
            // previousStatus: this.previousStatus,
            // decideReason: this.decideReason,
            critique: this.critique,

        }
    }

    summaryForPA(){
        return{
            isSuccess: this.isSuccess,
            task: this.task,
            previousStatus: this.previousStatus,
            planReason: this.planReason,
            decideReason: this.decideReason,
            summarizeReason: this.summarizeReason,
            skills: this.skills,
            critique: this.critique,
        }
    }

    summary(){
        return{
            memoryID: this.memoryID,
            memoryType: this.memoryType,
            isSuccess: this.isSuccess,
            timeCreated: this.timeCreated,
            timeExpired: this.timeExpired,
            lastAccessed: this.lastAccessed,
            task: this.task,
            subject: this.subject,
            verb: this.verb,
            object: this.object,
            biome: this.biome,
            previousStatus: this.previousStatus,
            planReason: this.planReason,
            decideReason: this.decideReason,
            summarizeReason: this.summarizeReason,
            code: this.code,
            skills: this.skills,
            critique: this.critique,
            errorMessage: this.errorMessage,
        }
    }
}

/**
 * MemoryStream stores all the Memory created.
 */
class MemoryStream {
    /**
     *
     * @param {String} rootPath The root path for the Memory Stream, should be something like `./memory_system/${COLLECTION_NAME}/`
     * @param {String} collectionName The name of the collection to be created
     * @param {String} persona The agent persona
     * @param {boolean} isInherit If it is an inheritance from the previous memory stream
     * @param {String} similarityFunction The similarity function used to query the data from Chroma; Default: "cosine", Alternative: "l2" (Squared L2), "ip" (Inner product)
     * @param {int} relevanceTopN The top n relevant memories selected when retrieving
     * @param {int} preferenceTopN The top n preferred memories selected when retrieving
     * @param {int} topN The top n selected for sending to Planner
     */
    constructor(rootPath, collectionName, persona, isInherit = false, similarityFunction = "cosine", relevanceTopN = 20, preferenceTopN = 20, topN = 5) {
        this.rootPath = rootPath;
        this.collectionName = collectionName;
        this.persona = persona;
        this.relevanceTopN = relevanceTopN;
        this.preferenceTopN = preferenceTopN;
        this.topN = topN;
        this.isInherit = isInherit;
        this.similarityFunction = similarityFunction;

        this.memoryCount = 0;
        this.memories = {};
        this.events = {};
        this.badPlans = {};
        this.errors = {};

        this.preferenceOrder = new Map();

        this.sequenceEvent = [];
        this.sequenceBadPlans = [];
        this.latestBadPlans = [];

        this.embedder = new OpenAIEmbeddingFunction({ openai_api_key: OPENAI_API_KEY });

        this.client = new ChromaClient({
            path: `http://localhost:${CHROMA_DB_PORT}`,
            embeddingFunction: this.embedder,
        });
    }

    /**
     * Initialize the folders and the vectordb
     * @returns {Promise<void>}
     */
    async init() {
        // JUSTIFY HERE WHEN TESTING
        this.personaDescription = await loadFile(`./context/personalities/${this.persona}.txt`, BOT_ERR_MSG);

        // Create the folders
        mkdir(this.rootPath, "");
        mkdir(this.rootPath, this.persona);
        mkdir(this.rootPath, `${this.persona}/analysis`);

        // Define these somewhere earlier (e.g., in constructor or init)
        const tenant = "default_tenant";
        const database = "default_database";

        // Delete collections if they exist
        try {
            await this.client.deleteCollection({
                name: `${this.persona}_memory_collection_${this.collectionName}`,
                tenant,
                database,
            });
            await this.client.deleteCollection({
                name: `${this.persona}_memory_collectionR_${this.collectionName}`,
                tenant,
                database,
            });
            await this.client.deleteCollection({
                name: `${this.persona}_memory_collectionP_${this.collectionName}`,
                tenant,
                database,
            });
        } catch (e) {
            if (!e.message.includes("not be found")) throw e;
        }

        try {
            this.vectorStoreR = await this.client.createCollection({
                name: `${this.persona}_memory_collectionR_${this.collectionName}`,
                tenant,
                database,
                embeddingFunction: this.embedder,
                metadata: { "hnsw:space": this.similarityFunction },
            });
        } catch (e) {
            if (e.message.includes("already exists")) {
                this.vectorStoreR = await this.client.getCollection({
                    name: `${this.persona}_memory_collectionR_${this.collectionName}`,
                    tenant,
                    database,
                });
                this.vectorStoreR.embeddingFunction = this.embedder;
            } else {
                throw e;
            }
        }

        try {
            this.vectorStoreP = await this.client.createCollection({
                name: `${this.persona}_memory_collectionP_${this.collectionName}`,
                tenant,
                database,
                embeddingFunction: this.embedder,
                metadata: { "hnsw:space": this.similarityFunction },
            });
        } catch (e) {
            if (e.message.includes("already exists")) {
                this.vectorStoreP = await this.client.getCollection({
                    name: `${this.persona}_memory_collectionP_${this.collectionName}`,
                    tenant,
                    database,
                });
                this.vectorStoreP.embeddingFunction = this.embedder;
            } else {
                throw e;
            }
        }

        // Do inheritance if needed
        if (this.isInherit) {
            try {
                await this.inheritHistory();
                GLog.c(`${BOT_LOG_MSG} ${this.persona}_memory_collectionR_${this.collectionName} fetched successfully.`);
                GLog.c(`${BOT_LOG_MSG} ${this.persona}_memory_collectionP_${this.collectionName} fetched successfully.`);
            } catch (err) {
                GLog.e(`${BOT_ERR_MSG} Error fetching ${this.persona}_memory_collectionR_${this.collectionName}: ${err}`);
                GLog.e(`${BOT_ERR_MSG} Error fetching ${this.persona}_memory_collectionP_${this.collectionName}: ${err}`);
            }
        } else {
            await writeJSON(`${this.rootPath}/${this.persona}/${this.persona}.json`, this.memories,
                BOT_LOG_MSG, BOT_ERR_MSG);
            GLog.c(`${BOT_LOG_MSG} ${this.persona}_memory_collectionR_${this.collectionName} created successfully.`);
            GLog.c(`${BOT_LOG_MSG} ${this.persona}_memory_collectionP_${this.collectionName} created successfully.`);
        }
    }

    async getCount() {
        return {
            libraryCount: this.memoryCount,
            vectorDBRCount: await this.vectorStoreR.count(),
            vectorDBPCount: await this.vectorStoreP.count(),
            eventCount: Object.keys(this.events).length,
            badPlanCount: Object.keys(this.badPlans).length,
            errorCount: Object.keys(this.errors).length,
        }
    }

    // Get all the prev tasks, remove the given task once since this is used for diversity, and the task is added before the checking
    async getTasks(task) {
        const tasks = new Set();
        let flag = false;

        for (let key in this.events) {
            if (!flag && this.events[key].task === task) {
                flag = true;
                continue;
            }

            tasks.add(this.events[key].task);
        }

        for (let key in this.errors) {
            if (!flag && this.errors[key].task === task) {
                flag = true;
                continue;
            }

            tasks.add(this.errors[key].task);
        }

        return tasks;
    }

    /**
     * Inherit the history from existed skill library
     * @returns {Promise<void>}
     */
    async inheritHistory() {
        let memories = await loadFile(`${this.rootPath}/${this.persona}/${this.persona}.json`, BOT_ERR_MSG);
        memories = JSON.parse(memories);

        for (let i of Object.keys(memories)) {
            let analysis = await loadFile(`${this.rootPath}/${this.persona}/analysis/id${i}.txt`, BOT_ERR_MSG);
            await this.addMemory(memories[i].memoryType, memories[i].isSuccess,
                memories[i].timeCreated, memories[i].timeExpired, memories[i].lastAccessed,
                memories[i].task, memories[i].subject, memories[i].verb, memories[i].object, memories[i].biome,
                memories[i].previousStatus, memories[i].planReason, memories[i].decideReason, memories[i].summarizeReason,
                memories[i].code, memories[i].skills, memories[i].critique, memories[i].errorMessage, analysis, true);
        }
    }


    /**
     * Add a new memory into the MemoryStream.
     * @param {string} memoryType The memory type to be stored (event / badPlan / error)
     * @param {boolean} isSuccess If the Memory is an event, shows if this event is a success; If the Memory is not an event, the input should be false
     * @param {number} timeCreated The time when the Memory is created
     * @param {number} timeExpired The time when the Memory should be deleted
     * @param {number} lastAccessed The time when the Memory is accessed last time
     * @param {string} task The task given in this Memory
     * @param {string} subject The subject of the Memory
     * @param {string} verb The verb of the Memory
     * @param {string} object The object of the Memory
     * @param {string} biome The biome of the Memory
     * @param {string} previousStatus The environment in Minecraft before running the code
     * @param {string} planReason The reason why this plan is given out
     * @param {string} decideReason The reason why this plan is approved or rejected
     * @param {string} summarizeReason The reason why this action is done successfully or not
     * @param {string} code The code of this Memory used
     * @param {string} skills The skills of this Memory used
     * @param {string} critique The critique for improving the next plan or action
     * @param {string} errorMessage The error message, empty if not an error
     * @param {string} analysis The memory analysis
     * @param {boolean} isInheriting If it is currently inheriting the previous memory stream, skip the file writing part
     * @returns {Promise<Memory|null>}
     */
    async addMemory(memoryType, isSuccess,
                    timeCreated, timeExpired, lastAccessed,
                    task, subject, verb, object, biome,
                    previousStatus, planReason, decideReason, summarizeReason,
                    code, skills, critique, errorMessage, analysis = "", isInheriting = false) {

        const memoryID = this.memoryCount;

        // TODO: Add into the library; Add into the collection, status as page content, task as metadata, id as id.
        const newMemory = new Memory(memoryID, memoryType, isSuccess,
            timeCreated, timeExpired, lastAccessed,
            task, subject, verb, object, biome,
            previousStatus, planReason, decideReason, summarizeReason, code, skills, critique, errorMessage);

        this.memories[memoryID] = newMemory;

        if (!isInheriting) {
            await writeJSON(`${this.rootPath}/${this.persona}/${this.persona}.json`,
                this.memories, BOT_LOG_MSG, BOT_ERR_MSG);

            // Do analysis
            analysis = await preferenceAnalyze(this.rootPath, this.persona, JSON.stringify(newMemory.summaryForPA()), newMemory.memoryID);
        }

        if (memoryType === "event") {
            this.events[memoryID] = newMemory;
            this.sequenceEvent.push(newMemory);

        } else if (memoryType === "badPlan") {
            this.badPlans[memoryID] = newMemory;
            this.sequenceBadPlans.push(newMemory);
            this.latestBadPlans.push(newMemory);

        } else {
            this.errors[memoryID] = newMemory;
        }

        this.memoryCount++;

        let addRFeedBack = await this.vectorStoreR.add({
            ids: [(this.memoryCount - 1).toString()],
            metadatas: [{ memoryID: (this.memoryCount - 1).toString(), task: task, memoryType: memoryType}],
            documents: [previousStatus],
        })

        if (!addRFeedBack) {
            GLog.e(BOT_ERR_MSG, `Failed to store memory ${(this.memoryCount - 1).toString()} in vectorStoreR.`)
        }

        let addPFeedBack = await this.vectorStoreP.add({
            ids: [(this.memoryCount - 1).toString()],
            metadatas: [{ memoryID: (this.memoryCount - 1).toString(), task: task, memoryType: memoryType}],
            documents: [analysis],
        })

        if (!addPFeedBack) {
            GLog.e(BOT_ERR_MSG, `Failed to store memory ${(this.memoryCount - 1).toString()} in vectorStoreP.`)
        }

        // We don't consider badPlans
        if (memoryType === "badPlan") {
            newMemory.preference = INF;
            return newMemory;
        }

        newMemory.preference = await this.getPreferenceValue(this.memoryCount - 1, analysis);
        this.preferenceOrder.set(memoryID, newMemory.preference);
        this.preferenceOrder = new Map([...this.preferenceOrder.entries()].sort((a, b) => a[1] - b[1]));

        return newMemory;
    }

    /**
     * Get all the Memories stored in the MemoryStream with the same S.P.O.B. as the inputted ones
     * (Subject and Object considered as the same thing when considering the relevancy).
     * @param {string} subject The subject of the Memory
     * @param {string} verb The verb of the Memory
     * @param {string} object The object of the Memory
     * @param {string} biome The biome of the Memory
     * @param {string} memoryType The memory type wanted to be searched
     * @returns {{id: Memory}} A dict of the related memories with id as key and Memory as value
     */
    getRelevantMemories(subject, verb, object, biome, memoryType="") {
        const memories = {};

        for(const id in this.memories){
            if(memoryType !== "" && this.memories[id].memoryType !== memoryType){
                continue;
            }

            const mySvob = this.memories[id].svob_summary();

            if(mySvob[0] === object ||
                mySvob[1] === verb ||
                mySvob[2] === subject || mySvob[2] === object ||
                mySvob[3] === biome){
                memories[id] = this.memories[id];
            }
        }
        return memories;
    }

    /**
     * Retrieve the memories using vectordb
     * @param {String} query The query for the vectordb
     * @param {number} alpha Default 0.5
     * @param {number} beta Default 1
     * @param {number} numNeeded The number of output expected (MUST BE EVEN)
     * @param {boolean} isBoth If True, output top 5 of each measure (R & P); Otherwise, output top 10 of measure U (= αR + βP)
     * @param {boolean} isIDOnly If True, output the memoryIDs ; Otherwise, output the retrieved results
     * @param {boolean} isPrint If the answer of the query should be printed
     * @returns {Promise<{P: unknown[], R: ID[]}|unknown[]|{P: [], R: []}>}
     */
    // TODO: Possible post-retrieve: Reordering based on Diversity, Putting the best document at the beginning and end of the context window
    async retrieveMemories(query, isBoth=false, alpha=0.5, beta=1, numNeeded=10, isIDOnly=true, isPrint=true) {
        if (isBoth) {
            // Get the top Rs and Ps
            const topR = await this.vectorStoreR.query({
                nResults: numNeeded/2,
                queryTexts: [query],
                where: {"$or": [{"memoryType": "event"}, {"memoryType": "error"}]},
            });

            const topRIDs = topR.ids[0];

            // Convert them into array of memories
            let topRMemories = [];
            for (let i = 0; i < topRIDs.length; i++) {
                topRIDs[i] = parseInt(topRIDs[i]);
                topRMemories.push(this.memories[topRIDs[i]]);
            }


            const topPMap = new Map(Array.from(this.preferenceOrder).slice(0, numNeeded/2));
            const topPIDs = Array.from(topPMap.keys());
            const topPMemories = [];
            const topP_PValues = Array.from(topPMap.values());
            for (let id of topPIDs) {
                topPMemories.push(this.memories[id]);
            }

            // Out put the top numNeeded / 2 retrieve memories from both measures
            if (isPrint){
                GLog.c(BOT_LOG_MSG, `Memories Retrieved by "${query}":\n\t${topRIDs}\nThe R values are: ${topR.distances[0]}`);
                GLog.c(BOT_LOG_MSG, `Memories Retrieved by Preference: ${topPIDs}\nThe P values are: ${topP_PValues}`);
            }

            // Return memories from both measures
            if (isIDOnly) return {
                R: topRIDs,
                P: topPIDs,
            };

            return {
                R: topRMemories,
                P: topPMemories,
            };
        }

        let UValueMap = new Map();

        // Get the top Rs and Ps
        const topR = await this.vectorStoreR.query({
            nResults: numNeeded,
            queryTexts: [query],
            where: {"$or": [{"memoryType": "event"}, {"memoryType": "error"}]},
        });

        const topRIDs = topR.ids[0];
        const topR_RValues = topR.distances[0];

        // Convert them into array of memories, calculate the U values of relevant memory
        // let topRMemories = [];
        const topR_PValues = [];
        const topR_UValues = [];
        for (let i = 0; i < topRIDs.length; i++) {
            topRIDs[i] = parseInt(topRIDs[i]);
            let id = topRIDs[i];
            let memory = this.memories[id];
            // topRMemories.push(memory);

            let R = topR_RValues[i];
            let P = memory.preference;
            let U = alpha * R + beta * P;
            UValueMap.set(id, U);

            topR_PValues.push(P);
            topR_UValues.push(U);
        }

        const topPMap = new Map(Array.from(this.preferenceOrder).slice(0, numNeeded));
        const topPIDs = Array.from(topPMap.keys());
        const topPMemories = [];
        const topP_PValues = Array.from(topPMap.values());

        // Remove the repeated ids in PIDs to save time calculating Rs of P memories
        for (let id of topPIDs) {
            if (topRIDs.includes(id)) {
                const index = topPIDs.indexOf(id);
                topPIDs.splice(index, 1);
            }
        }

        // Calculate the U values of each preferred memory
        const topP_RValues = await this.getRelevanceValues(topPIDs, query);
        const topP_UValues = [];
        for (let i = 0; i < topPIDs.length; i++) {
            topPIDs[i] = parseInt(topPIDs[i]);
            let id = topPIDs[i];
            let memory = this.memories[i];

            let R = topP_RValues[i];
            let P = topP_PValues[i];
            let U = alpha * R + beta * P;
            UValueMap.set(id, U);

            topPMemories.push(memory);
            topP_UValues.push(U);
        }

        // Get the first 10 Usability value
        UValueMap = new Map([...UValueMap.entries()].sort((a, b) => a[1] - b[1]));
        const topUMap = new Map(Array.from(UValueMap).slice(0, numNeeded));
        const topUIDs = Array.from(topUMap.keys());
        const topUMemories = Array.from(topUMap.entries());

        // Out put the top numNeeded retrieve memories from both measures
        if (isPrint){
            GLog.c(`${BOT_LOG_MSG} Memories Retrieved by \n\t"${query}":\n\t${topRIDs}\nThe R values are: ${topR_RValues}\nThe P values are: ${topR_PValues}\nThe U values are: ${topR_UValues}`);
            GLog.c(`${BOT_LOG_MSG} Memories Retrieved by Preference: ${topPIDs}\nThe R values are: ${topP_RValues}\nThe P values are: ${topP_PValues}\nThe U values are: ${topP_UValues}`);
        }

        // Return memories
        if (isIDOnly) return topUIDs;
        return topUMemories;
    }

    /**
     * Retrieve the past recent memories
     * @param numNeeded The number of memories needed; Default 5
     * @returns {Promise<[]>} The list of the past recent memories
     */
    async retrievePastRecentMemories(numNeeded=5) {
        let memories = this.sequenceEvent.slice(-numNeeded);
        let results = [];

        for (let memory of memories) {
            results.push(JSON.stringify(memory.planSummary()));
        }

        return results;
    }

    /**
     * Get the Relevance values of an array of memory ids
     * @param ids The memory ids asking for R values
     * @param query The query for calculating R values
     * @returns {Promise<[]>}
     */
    async getRelevanceValues(ids, query){
        let RValues = [];

        for (let id of ids) {
            id = id.toString();

            const relRes = await this.vectorStoreR.query({
                nResults: 1,
                queryTexts: [query],
                where: { "memoryID": id },
            });

            RValues.push(relRes.distances[0][0]);
        }

        return RValues;
    }

    /**
     * Get the preference value of the memory
     * @param {int} id The id of the memory to be calculated
     * @param {String} analysis The analysis of the memory
     */
    // FIXME: Better to directly calculate using functions instead of vectorDB; tried but got different values (imprecise)
    async getPreferenceValue(id, analysis) {
        analysis.replaceAll('\n', '');
        let m = id.toString();

        const relRes = await this.vectorStoreP.query({
            nResults: 1,
            queryTexts: [this.personaDescription],
            where: { "memoryID": m },
        });

        GLog.c(`${BOT_LOG_MSG} id: ${id}, relResID: ${relRes.ids[0][0]}, Preference: ${relRes.distances[0][0]}`);

        return relRes.distances[0][0];
    }

    /**
     * Clear the latest bad plans
     */
    clearLatestBadPlans() {
        this.latestBadPlans = [];
    }

    async printAllMemories(logName) {
        GLog.c(`${logName} The number is: ${JSON.stringify(await this.getCount())}\nThe memories are:\n${JSON.stringify(this.memories)}`);
    }

    async clean() {
        await this.client.deleteCollection({name: `${this.persona}_memory_collection_${this.collectionName}`});
    }

}

module.exports = {
    Memory,
    MemoryStream,
};