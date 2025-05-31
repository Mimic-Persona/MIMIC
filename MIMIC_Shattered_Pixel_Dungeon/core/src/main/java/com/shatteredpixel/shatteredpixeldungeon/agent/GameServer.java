package com.shatteredpixel.shatteredpixeldungeon.agent;

import java.net.InetSocketAddress;

import com.shatteredpixel.shatteredpixeldungeon.APIs.AgentAPI;
import com.shatteredpixel.shatteredpixeldungeon.APIs.status.Status;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameServer extends WebSocketServer {
    public static final String BOT_MSG = "agent.GameServer: ";
    public static final String ERR_MSG = "agent.GameServer:error: ";

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

        if (message.equals("GetStatus")) {
            JSONObject status = Status.getStatus();
            status.put("msgType", "status");
            conn.send(status.toString());
        }
        else if (message.startsWith("ACTION: ")) {
            GLog.c("======================================================================== Action #" + (Dungeon.agentActionCounter + 2) + " ========================================================================");

            // Handle the actions
            JSONObject newPlan = new JSONObject(message.replace("ACTION: ", ""));


            // Cast tile - the JSONArray to an int array
            int[] tile = null;

            if (newPlan.get("action").toString().equals("act") ||
                newPlan.get("action").toString().equals("throw")) {
                JSONArray jsonArray = (JSONArray) newPlan.get("tile");

                tile = new int[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    tile[i] = jsonArray.getInt(i);
                }
            }

            GLog.resetBotMsg();
            GLog.resetErrMsg();

            GameScene.isUpdated = false;

            AgentAPI.handle(
                    newPlan.get("action").toString().toLowerCase(),
                    tile,
                    newPlan.get("item1").toString(),
                    newPlan.get("item2").toString(),
                    Integer.parseInt(newPlan.get("waitTurns").toString()));

            Dungeon.toReport = true;

        } else if (message.equals("TERMINATED")) {
            GLog.c(BOT_MSG + "Terminating the connection...");

            // Send the logs, errors and status to the client
            JSONObject termination = new JSONObject();

            termination.put("msgType", "TERMINATED");

            conn.send(termination.toString());

            conn.close();

            try {
                Dungeon.gameServer.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Dungeon.isAgentNext = false;
            Dungeon.isInMonkeyMode = false;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}
