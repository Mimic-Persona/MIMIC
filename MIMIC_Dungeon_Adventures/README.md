![logo](https://user-images.githubusercontent.com/106514178/227779667-51a0dbef-5e22-4cd8-8ba9-002bb418530f.png)

# Game Description

🗡️ Welcome to **Dungeon Adventures**, a project inspired by roguelike games. Roguelike is an exciting and challenging game genre in which players take on the role of a character exploring a procedurally generated dungeon.

🎯 The goal of the game is to reach the end of the dungeon while fighting off dangerous monsters and collecting treasure along the way, all while uncovering the truth about the main character. **Get ready for an epic adventure!**

🌟 The game consists of two levels where the hero must face a large number of monsters. By collecting items, the hero's abilities increase. After completing the labyrinth, the hero must face a boss. If he defeat the boss, he will be teleported to a room with crowns where he will be put to the test of wit and must find the crown that stands out.

💪 **So, traveler, you have no choice! You need to try our game. Let's do this!**

This game is a modified version dedicated to MIMIC, check the original game from https://github.com/stelmaszczykadrian/Dungeon-Adventures

## Game States
Notice, the game states are defined by the researchers according to the understanding to the game and the code.
They are not necessarily the same as the game states intentionally done by the original game developers since the original game states were not provided in the original game repository.
As a result, these game states are subject to change in the future, and none of them is used in the evaluation of MIMIC.

![DA Game States](../images/DA/DA_Game_States.png)

# How to Run MIMIC

## Prerequisites
- This game only supports running on Windows

## IMPORTANT
Make sure your working directory is set to the `MIMIC` directory, as all commands and configurations will be based on this path.

## Navigate to this directory
   ```bash
   cd ./MIMIC_Dungeon_Adventures
   ```

## Install Node.js and Dependencies
1. Make sure you have [Node.js](https://nodejs.org/) installed on your machine.
2. Open a terminal and navigate to the `./MIMIC_Dungeon_Adventures` directory.
3. Install the required dependencies by running the following command:

    ```bash
    npm install chromadb@1.10.5
    npm install
    ```

## Run ChromaDB in Docker

1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) on your machine.

2. Launch Docker Desktop and ensure it is running.

3. Pull the ChromaDB Docker image and set the configuration to run it without tenant support:

    ```bash
    docker pull chromadb/chroma:latest
    docker run -p 8000:8000 chromadb/chroma --enable-tenant=False
    ```

4. Run the ChromaDB Docker container with the following command:

    ```bash
    docker run -d -p 8000:8000 --name chromadb chromadb/chroma
    ```

5. Make sure the ChromaDB server is running by checking the logs in Docker Desktop.

## Configure the Settings
1. Taking `config.json.example` file located in the `./MIMIC_Dungeon_Adventures` directory as an example to create the `config.json` file located in the same place.
2. Set the "OPENAI_API_KEY" to your OpenAI API key, which you can obtain from the [OpenAI website](https://platform.openai.com/settings/organization/api-keys).
   - If you don't have an OpenAI API key, you can sign up for free and get one.
   - Make sure to keep your API key secure and do not share it publicly.
   - This key is required for MIMIC to interact with the OpenAI API and generate responses.
   - This key should look like `sk-XXX...` or `sk-proj-XXX...`
3. Set the "MIMIC_PERSONALITY" to the desired personality of MIMIC. You can choose from the following options:
   - "achievement"
   - "adrenaline"
   - "aggression"
   - "caution"
   - "completion"
   - "curiosity"
   - "efficiency"
4. Set the "PORT" to the desired port number for the server to run on. The default is 8080.
5. Set the "IS_SMART_MONKEY" to `true` if you want to use the Smart Monkey baseline, or `false` if you want to use the Dumb Monkey baseline.
6. Set the "IS_IN_EXP" to `false` ALL THE TIME. This is a functionality that is only used for the experiment and should not be changed by the user.

## Run the Game and Start the Server
1. Run `./src/main/java/com/codecool/dungeoncrawl/App.java` file to start the game.
2. Make sure your working directory is set to the `MIMIC` directory, as all commands and configurations will be based on this path.
3. Click on the "New Adventure" button and then the "Start the Game" to start the game and server.
4. Before running any tools, make sure you are in the game as shown in screenshot 1. And you should see the following output in the terminal:

    ```
    SLF4J: No SLF4J providers were found.
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.
   $$ Agent Mode Enabled!
   Log file appended successfully.
   Log file appended successfully.
   Log file appended successfully.
    ```
   
## IMPORTANT

Please ignore the following error messages, since they are related to the Jacoco code coverage tool and do not affect the functionality of MIMIC or the game:
```
java.net.ConnectException: Connection refused: connect
	at java.base/sun.nio.ch.Net.connect0(Native Method)
	at java.base/sun.nio.ch.Net.connect(Net.java:589)
	at java.base/sun.nio.ch.Net.connect(Net.java:578)
	at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:583)
	at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:327)
	at java.base/java.net.Socket.connect(Socket.java:752)
	at java.base/java.net.Socket.connect(Socket.java:687)
	at java.base/java.net.Socket.<init>(Socket.java:556)
	at java.base/java.net.Socket.<init>(Socket.java:357)
	at org.jacoco.core.tools.ExecDumpClient.tryConnect(ExecDumpClient.java:144)
	at org.jacoco.core.tools.ExecDumpClient.dump(ExecDumpClient.java:116)
	at org.jacoco.core.tools.ExecDumpClient.dump(ExecDumpClient.java:99)
	at com.codecool.dungeoncrawl.agent.reporter.JacocoReporter.dumpData(JacocoReporter.java:304)
	at com.codecool.dungeoncrawl.agent.reporter.JacocoReporter.dumpData(JacocoReporter.java:362)
	...
```

## Run MIMIC
1. Once you are in the game, open a terminal and navigate to the `./MIMIC_Dungeon_Adventures` directory.

   ```bash
    cd ./MIMIC_Dungeon_Adventures
   ```

2. Run the following command to start MIMIC:

    ```bash
    node ./src/main/java/com/codecool/dungeoncrawl/agent/bridge/agentClient.js
    ```

3. After running the command, you should see the following output:

    ```
    bridge.agentClient:log Agent connected to WebSocket server.
   skill_library.SkillManager:log {MIMIC_PERSONALITY}_skill_collection_Example created successfully.
   memory_stream.MemoryStream:log Writing ./src/main/java/com/codecool/dungeoncrawl/agent/memory_system/Example//{MIMIC_PERSONALITY}/{MIMIC_PERSONALITY}.json...
   memory_stream.MemoryStream:log "./src/main/java/com/codecool/dungeoncrawl/agent/memory_system/Example//{MIMIC_PERSONALITY}/{MIMIC_PERSONALITY}.json" written successfully
   memory_stream.MemoryStream:log {MIMIC_PERSONALITY}_memory_collectionR_Example created successfully.
   memory_stream.MemoryStream:log {MIMIC_PERSONALITY}_memory_collectionP_Example created successfully.
    ```

4. Now, MIMIC is running and ready to interact with the game. You can start MIMIC by pressing the key `B` on your keyboard while playing the game.

## [Option 1] Run MIMIC-P Baseline
1. Once you are in the game, open a terminal and navigate to the `./MIMIC_Dungeon_Adventures` directory.

   ```bash
    cd ./MIMIC_Dungeon_Adventures
   ```

2. Run the following command to start MIMIC-P:

    ```bash
    node ./src/main/java/com/codecool/dungeoncrawl/agent/LLMBaseline/LLMPlannerAgentClient.js
    ```

3. After running the command, you should see the following output:

    ```
    LLMBaseline.LLMPlannerAgent:log LLMPlannerAgent connected to WebSocket server.
    ```

4. Now, MIMIC-P is running and ready to interact with the game. You can start MIMIC-P by pressing the key `L` on your keyboard while playing the game.


## [Option 2] Run MIMIC-P+S Baseline
1. Once you are in the game, open a terminal and navigate to the `./MIMIC_Dungeon_Adventures` directory.

   ```bash
    cd ./MIMIC_Dungeon_Adventures
   ```

2. Run the following command to start MIMIC-P:

    ```bash
    node ./src/main/java/com/codecool/dungeoncrawl/agent/LLMBaseline/LLMPlannerSummarizerAgentClient.js
    ```

3. After running the command, you should see the following output:

    ```
    LLMBaseline.LLMPlannerSummarizerAgent:log LLMPlannerSummarizerAgent connected to WebSocket server.
    ```

4. Now, MIMIC-P+S is running and ready to interact with the game. You can start MIMIC-P+S by pressing the key `K` on your keyboard while playing the game.


## [Option 3] Run Monkey Baselines
1. Once you are in the game, Monkey is ready to interact with the game. You can start Monkey by pressing the key `R` on your keyboard while playing the game.

2. Set the "IS_SMART_MONKEY" in your `config.json` file to `true` if you want to use the Smart Monkey baseline, or `false` if you want to use the Dumb Monkey baseline.

# :rocket: Screenshots
![dungeon1](https://github.com/stelmaszczykadrian/Dungeon-Adventures/assets/106514178/1b4a7d3b-aab3-4505-ad88-d33bff9848fe)
<br>
![dungeon2](https://github.com/stelmaszczykadrian/Dungeon-Adventures/assets/106514178/aec45150-3835-4c2c-ac06-a11366fb0387)
<br>
![dungeon3](https://github.com/stelmaszczykadrian/Dungeon-Adventures/assets/106514178/228c324a-46cb-43ed-b295-7b7c0946fa1f)
<br>