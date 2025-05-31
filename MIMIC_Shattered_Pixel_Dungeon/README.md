![logo](https://shatteredpixel.com/assets/images/SHPD-Desc/title.gif)

# Game Description

[Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/) is an open-source traditional roguelike dungeon crawler with randomized levels and enemies, and hundreds of items to collect and use. It's based on the [source code of Pixel Dungeon](https://github.com/00-Evan/pixel-dungeon-gradle), by [Watabou](https://www.watabou.ru).

Shattered Pixel Dungeon currently compiles for Android, iOS, and Desktop platforms.

This game is a modified version dedicated to MIMIC, check the original game from https://github.com/00-Evan/shattered-pixel-dungeon on [**v2.4.0**](https://github.com/00-Evan/shattered-pixel-dungeon/compare/v2.4.0...3.1.0-BETA)

# How to Run MIMIC

## Prerequisites
- This game supports running on Windows, Android, and IOS. But the MIMIC agent is only supported on Windows, and the game is only tested on Windows.

## IMPORTANT
Make sure your working directory is set to the `MIMIC` directory, as all commands and configurations will be based on this path.

## Navigate to this directory
   ```bash
   cd ./MIMIC_Shattered_Pixel_Dungeon
   ```

## Install Node.js and Dependencies
1. Make sure you have [Node.js](https://nodejs.org/) installed on your machine.

2. Open a terminal and navigate to the `./MIMIC_Shattered_Pixel_Dungeon/core/src/main` directory.
    ```bash
    cd ./MIMIC_Shattered_Pixel_Dungeon
    cd ./core/src/main
    ```
   
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
1. Taking `config.json.example` file located in the `./MIMIC_Shattered_Pixel_Dungeon` directory as an example to create the `config.json` file located in the same place.

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

1. Ensure a [JDK (Java Development Kit)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) is installed on your computer. (Java 21 is tested)

2. Navigate to the `./MIMIC_Shattered_Pixel_Dungeon` directory.

    ```bash
    cd ./MIMIC_Shattered_Pixel_Dungeon
    ```
   
3. Make sure your working directory is set to the `MIMIC` directory, as all commands and configurations will be based on this path.

4. Run the following command to start the game in debug mode:

    ```bash
    ./gradlew desktop:debug
    ```

5. Click on the "Enter the Dungeon" button and then the "New Game" to start choosing the character. Choose Warrior as the character and then click on the "Start" button to start the game.
   - To be noticed, in the experiment, only the Warrior character is used for fairness and consistency.
   - Please only choose the Warrior character, as MIMIC is only tested with this Warrior character.

6. Before running any tools, make sure you are in the game. And you should see the following output in the terminal:

    ```
    > Task :desktop:debug
   [Controllers] added manager for application, 1 managers active
   [GAME] @@ You descend to floor 1 of the dungeon.
   Log file appended successfully.
   Log file appended successfully.
   $$ Game Server Opened!
   [GAME] $$ Game Server Opened!
   Log file appended successfully.
   SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
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
	at com.shatteredpixel.shatteredpixeldungeon.agent.reporter.JacocoReporter.dumpData(JacocoReporter.java:304)
	...
```

## Run MIMIC
1. Once you are in the game, press `B`, and you should see a pop-up window asking you to "Enter your command." At the same time, you should see the following message in your terminal:
   ```
   > Task :desktop:debug
   [Controllers] added manager for application, 1 managers active
   [GAME] @@ You descend to floor 1 of the dungeon.
   Log file appended successfully.
   Log file appended successfully.
   $$ Game Server Opened!
   [GAME] $$ Game Server Opened!
   Log file appended successfully.
   SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
   $$ MIMIC Mode Started with XXX milliseconds!
   [GAME] $$ MIMIC Mode Started with XXX milliseconds!
   Log file appended successfully.
   ```

2. open a terminal and navigate to the `./MIMIC_Shattered_Pixel_Dungeon` directory.

    ```bash
    cd ./MIMIC_Shattered_Pixel_Dungeon
    ```

3. Run the following command to start MIMIC:

    ```bash
    node ./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/bridge/agentClient.js
    ```

4. After running the command, you should see the following output:

    ```
    bridge.agentClient:log Agent connected to WebSocket server.
   skill_library.SkillManager:log achievement_skill_collection_Example created successfully.
   memory_stream.MemoryStream:log Writing ./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/memory_system/Example//{MIMIC_PERSONALITY}/{MIMIC_PERSONALITY}.json...
   memory_stream.MemoryStream:log "./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/memory_system/Example//{MIMIC_PERSONALITY}/{MIMIC_PERSONALITY}.json" written successfully
   memory_stream.MemoryStream:log {MIMIC_PERSONALITY}_memory_collectionR_Example created successfully.
   memory_stream.MemoryStream:log {MIMIC_PERSONALITY}_memory_collectionP_Example created successfully.
    ```

5. Now, MIMIC is running and ready to interact with the game. You can start MIMIC by enter the command `1` in the pop-up window and press "Set" button to start MIMIC.

## [Option 1] Run MIMIC-P Baseline
1. Once you are in the game, press `L`, and you should see a pop-up window asking you to "Enter your command." At the same time, you should see the following message in your terminal:
   ```
   > Task :desktop:debug
   [Controllers] added manager for application, 1 managers active
   Current working directory: D:\McGill\Graduated Study\MIMIC\MIMIC_Shattered_Pixel_Dungeon\desktop
   Current working directory: D:\McGill\Graduated Study\MIMIC\MIMIC_Shattered_Pixel_Dungeon\desktop
   SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
   [GAME] @@ You descend to floor 1 of the dungeon.
   Log file appended successfully.
   Log file appended successfully.
   $$ Game Server Opened!
   [GAME] $$ Game Server Opened!
   Log file appended successfully.
   $$ MIMIC Mode Started with XXX milliseconds!
   [GAME] $$ MIMIC Mode Started with XXX milliseconds!
   Log file appended successfully.
   ```

2. open a terminal and navigate to the `./MIMIC_Shattered_Pixel_Dungeon` directory.

    ```bash
    cd ./MIMIC_Shattered_Pixel_Dungeon
    ```

3. Run the following command to start MIMIC-P:

    ```bash
    node ./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/LLMBaseline/LLMPlannerAgentClient.js
    ```

4. After running the command, you should see the following output:

    ```
    LLMPlannerBaseline.LLMPlannerAgent:log LLMPlannerAgent connected to WebSocket server.
    ```

5. Now, MIMIC-P is running and ready to interact with the game. You can start MIMIC by enter the command `l` in the pop-up window and press "Set" button to start MIMIC.


## [Option 2] Run MIMIC-P+S Baseline
1. Once you are in the game, press `L`, and you should see a pop-up window asking you to "Enter your command." At the same time, you should see the following message in your terminal:
   ```
   > Task :desktop:debug
   [Controllers] added manager for application, 1 managers active
   Current working directory: D:\McGill\Graduated Study\MIMIC\MIMIC_Shattered_Pixel_Dungeon\desktop
   Current working directory: D:\McGill\Graduated Study\MIMIC\MIMIC_Shattered_Pixel_Dungeon\desktop
   SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
   SLF4J: Defaulting to no-operation (NOP) logger implementation
   SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
   [GAME] @@ You descend to floor 1 of the dungeon.
   Log file appended successfully.
   Log file appended successfully.
   $$ Game Server Opened!
   [GAME] $$ Game Server Opened!
   Log file appended successfully.
   $$ MIMIC Mode Started with XXX milliseconds!
   [GAME] $$ MIMIC Mode Started with XXX milliseconds!
   Log file appended successfully.
   ```

2. open a terminal and navigate to the `./MIMIC_Shattered_Pixel_Dungeon` directory.

    ```bash
    cd ./MIMIC_Shattered_Pixel_Dungeon
    ```

3. Run the following command to start MIMIC-P+S:

    ```bash
    node ./core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/agent/LLMBaseline/LLMPlannerSummarizerAgentClient.js
    ```

4. After running the command, you should see the following output:

    ```
    LLMBaseline.LLMPlannerSummarizerAgent:log LLMPlannerSummarizerAgent connected to WebSocket server.
    ```

5. Now, MIMIC-P+S is running and ready to interact with the game. You can start MIMIC by enter the command `k` in the pop-up window and press "Set" button to start MIMIC.


## [Option 3] Run Monkey Baselines
1. Once you are in the game, Monkey is ready to interact with the game. You can start Monkey by pressing the key `N` on your keyboard while playing the game.

2. Set the "IS_SMART_MONKEY" in your `config.json` file to `true` if you want to use the Smart Monkey baseline, or `false` if you want to use the Dumb Monkey baseline.

## IMPORTANT:
If the running failed due to socket bad connection, try to check if the GameServer is enabled in the game from the terminal / game log. If not, you can toggle it by pressing `G` on your keyboard while playing the game. Note, pressing `L` or `B` will also toggle the GameServer on, so you can press `L` or `B` to toggle it on as well.

# Some Code Information
All messages used in the game can be found [here](core/src/main/assets/messages).

All buffs can be found [here](core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/buffs).

All hero's information can be found [here](core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero).
- Remember, we are only using the character [Warrior](core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/abilities/warrior).

All Information about mobs can be found [here](core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs).