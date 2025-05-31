package com.codecool.dungeoncrawl.agent.reporter;

import com.codecool.dungeoncrawl.gui.Main;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import static com.codecool.dungeoncrawl.agent.utils.JsonUtils.jsonFileToDictionary;

public class ReporterClient extends WebSocketClient {

    // TODO: Define the constants before running the experiment
    // {"Achievement", "Adrenaline", "Aggression", "Caution", "Completion", "Curiosity", "Efficiency"}
    public static JSONObject config = jsonFileToDictionary("./MIMIC_Dungeon_Adventures/config.json");
    public static String PREFIX;
    public static ReporterClient reporterClient;
    public static boolean IS_IN_EXP;

    static {
        assert config != null;
        try {
            PREFIX = config.getString("REPORT_PREFIX");
            reporterClient = new ReporterClient(config.getInt("PORT"));
            IS_IN_EXP = config.getBoolean("IS_IN_EXP");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static final long INTERVAL = 1000; // 1 second
    public static final int STOP_COUNT = 1000;

    public static int actionCounter = -1;
    public static String action = "";
    public static String env = "";

    public static int stopCounter = 0;
    public static int prevActionCounter = 0;

    public ReporterClient(int port) {
        super(URI.create("ws://localhost:" + port));
    }

    public static boolean checkStop() {
        return stopCounter >= STOP_COUNT;
    }


    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        // Fetching the Status for the client
        try {
            JSONObject msg = new JSONObject(message);

            if (msg.get("msgType").equals("report")) {
                if (this.isOpen()) {
                    actionCounter = (int) msg.get("actionCounter");
                    action = (String) msg.get("action");
                    env = (String) msg.get("env");

                    JacocoReporter.report(actionCounter + 1);
                }

                if (checkStop()) {
                    this.send("TERMINATED");
                    this.close();
                    reporterClient.close();
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed. Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    public static void main(String[] args) {
        Timer timer = new Timer();

        if (!Main.BY_ACTION) {
            timer.schedule(new TimerTask() {

                int rowNum = 0;

                @Override
                public void run() {
                    try {

                        if (checkStop()) {
                            timer.cancel();
                            return;
                        }

                        rowNum = JacocoReporter.report(rowNum);

                        if (System.getenv("ACTION_COUNTER") != null) {
                            actionCounter = Integer.parseInt(System.getenv("ACTION_COUNTER"));
                            return;
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, INTERVAL);
        } else {
            try {
                reporterClient.setConnectionLostTimeout(0);
                reporterClient.connectBlocking();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
