package com.codecool.dungeoncrawl.agent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.codecool.dungeoncrawl.APIs.AgentAPI;
import com.codecool.dungeoncrawl.APIs.status.Status;
import com.codecool.dungeoncrawl.gui.Main;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.codecool.dungeoncrawl.APIs.status.Environment.updateVisitedMap;
import static com.codecool.dungeoncrawl.agent.utils.JsonUtils.jsonArrayToIntArray;
import static com.codecool.dungeoncrawl.agent.utils.ReadFileAsString.readFileAsString;

public class GameServer extends WebSocketServer {

    public static final String BOT_MSG = "agent.GameServer: ";
    public static final String ERR_MSG = "agent.GameServer:error: ";
    public static String SKILLS_IMPORT;

    static {
        try {
            SKILLS_IMPORT = readFileAsString("./MIMIC_Dungeon_Adventures/src/main/java/com/codecool/dungeoncrawl/agent/skill_library/skills_import.txt") + '\n';

        } catch (IOException e) {
            GLog.e(ERR_MSG + "Error in reading SkillsImport.java: " + e.getMessage());
        }
    }

    private final Path OUT_ROOT_PATH = Paths.get("./src/main/java/com/codecool/dungeoncrawl/agent/skill_library/");
    private final String SKILLS_IMPORT_PATH = "com.codecool.dungeoncrawl.agent.skill_library";

    public GameServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        GLog.c(BOT_MSG + "New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        GLog.c(BOT_MSG + "Closed connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        GLog.c(BOT_MSG + message);
        updateVisitedMap();

        // Fetching the Status for the client
        if (message.equals("GetStatus")) {
            JSONObject status;

            try {
                status = Status.getStatus();
                status.put("msgType", "status");
                conn.send(status.toString());

            } catch (JSONException e) {
                GLog.e(ERR_MSG + "Error in getting status: " + e.getMessage());
            }
        }

        // Try to handle the action commands from the client
        else if (message.startsWith("ACTION: ")) {
            JSONObject newPlan = null;

            try {
                newPlan = new JSONObject(message.replace("ACTION: ", ""));

            } catch (JSONException e) {
                GLog.e(ERR_MSG + "Error in creating JSONObject: " + e.getMessage());
            }

            GLog.resetBotMsg();
            GLog.resetErrMsg();

            try {
                assert newPlan != null;
                AgentAPI.handle(newPlan.get("action").toString().toLowerCase(), jsonArrayToIntArray((JSONArray) newPlan.get("tile")));

            } catch (JSONException e) {
                GLog.e("Error in handling the action: " + e.getMessage());
            }

            // Send the logs, errors and status to the client
            JSONObject feedback = new JSONObject();

            try {
                feedback.put("msgType", "feedback");
                feedback.put("logs", GLog.BOT_MSG);
                feedback.put("errors", GLog.ERR_MSG);

            } catch (JSONException e) {
                GLog.e("Error in creating feedback: " + e.getMessage());
            }

            conn.send(feedback.toString());

        } else if (message.equals("TERMINATED")) {
            GLog.c(BOT_MSG + "Terminating the connection...");

            // Send the logs, errors and status to the client
            JSONObject termination = new JSONObject();

            try {
                termination.put("msgType", "TERMINATED");

            } catch (JSONException e) {
                GLog.e("Error in creating termination: " + e.getMessage());
            }

            conn.send(termination.toString());

            conn.close();

            try {
                Main.gameServer.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Main.isAgentNext = false;
            Main.isInMonkeyMode = false;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}
