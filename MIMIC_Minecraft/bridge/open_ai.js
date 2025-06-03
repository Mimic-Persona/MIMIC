const OpenAI = require("openai");
const fetch = require("isomorphic-fetch");
const {GLog} = require("../utils/GLog");
const config = require("../config.json");

// OpenAI API Key:
const OPENAI_API_KEY = config.OPENAI_API_KEY;

const openai = new OpenAI({ apiKey: OPENAI_API_KEY });

const AI_LOG_MSG = "bridge.open_ai:log";
const AI_ERR_MSG = "bridge.open_ai:error";
const EOL = "\n";

/**
 * Call the OpenAI API
 * @param context The context for the AI
 * @param input The input for the AI
 * @param LogMsg The log message
 * @param model gpt-3.5-turbo-instruct; gpt-3.5-turbo; gpt-4o
 * @param printInput Print the input
 * @param printContext Print the context
 * @param printAnswer Print the answer
 * @param isInJSON Is the answer in JSON format
 * @returns {Promise<string>} The answer from the AI
 */
async function callOpenAI(context, input, LogMsg,
                          model="gpt-3.5-turbo-instruct",
                          printInput=false, printContext=false, printAnswer=true,
                          isInJSON=true) {
    if (!OPENAI_API_KEY) {
        GLog.e(`${AI_ERR_MSG} ERROR: CODEX_API_KEY is required.`);
        process.exit(1);
    }

    let body;
    let URL;
    let answer;

    if (printContext) GLog.c(`${LogMsg} Context: ${context}${EOL} ${input}${EOL}`);
    if (printInput) GLog.c(`${LogMsg} Input: ${input}${EOL}`);

    let isQualified = false;

    while (!isQualified) {
        // If is the completion GPT
        if (model === "gpt-3.5-turbo-instruct") {
            body = {
                model: model,
                prompt: `${context}${EOL} ${input}${EOL}`,
                max_tokens: 1000,
                temperature: 0,
                // stop: STOP_WORD,
                n: 1,
            };
            URL = "https://api.openai.com/v1/completions";
        }
        // If is the ChatGPT
        else {
            body = {
                model: model,
                messages: [{role: "user", content: `${context}${EOL} ${input}${EOL}`}],
                temperature: 0,
                // stop: STOP_WORD,
                n: 1,
            };
            URL = "https://api.openai.com/v1/chat/completions";
        }

        let response = null;
        while (response === null || response.status === 429) {
            response = await fetch(URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${OPENAI_API_KEY}`,
                },
                body: JSON.stringify(body),
            });

            if (!response.ok) {
                GLog.e(`${AI_ERR_MSG} api response failed with status: ${response.statusText}`);

                if (response.status === 429) {
                    const retryAfter = response.headers.get('Retry-After');
                    const backoff = retryAfter ? parseInt(retryAfter, 10) * 1000 : 1000;

                    GLog.c(`${AI_LOG_MSG} Waiting for ${backoff / 1000} second to resend...`);
                    await new Promise(resolve => setTimeout(resolve, backoff));
                    continue;
                }
                return null;
            }
        }

        response = await response.json();

        GLog.c(`${LogMsg} request: ${response.id}`);
        GLog.c(`${LogMsg} model: ${response.model}`);

        // extract answer from response
        if (model === "gpt-3.5-turbo-instruct") {
            answer = await response.choices
                .map((choice) => choice.text)
                .join("\n");
        } else {
            answer = await response.choices
                .map((choice) => choice.message.content)
                .join("\n");
        }

        answer = answer.trim();
        if (printAnswer) GLog.c(`${LogMsg} answer: ${answer}`);

        if (isInJSON) {
            try {
                isQualified = true;
                answer = answer.replace("```json", "").replace("```", "");
                JSON.parse(answer);
            } catch (e) {
                GLog.e(`${AI_ERR_MSG} ${e}`);
                isQualified = false;
            }
        } else {
            isQualified = true;
        }
    }

    return answer;
}

module.exports = callOpenAI;