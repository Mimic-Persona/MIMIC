package com.shatteredpixel.shatteredpixeldungeon.agent.reporter;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static com.shatteredpixel.shatteredpixeldungeon.agent.utils.JsonUtils.jsonFileToDictionary;

public class ReporterClient extends WebSocketClient {

    // TODO: Define the constants before running the experiment
    // "Achievement", "Adrenaline", "Aggression", "Caution", "Completion", "Curiosity", "Efficiency"3
    public static final long INTERVAL = 1000; // 1 second
    public static final int STOP_COUNT = Integer.MAX_VALUE;
    public static double DIFF_BOUNDARY = 0.5;

    public static String action = "";
    public static String env = "";

    public static int stopCounter = 0;
    public static int prevActionCounter = 0;
    public static int actionCounter = Dungeon.actionCounter;
    public static List<Double> totalDiffs = new ArrayList<>(Arrays.asList(0d,0d,0d,0d,0d,0d));

    public static JSONObject config = jsonFileToDictionary("../config.json");
    public static final String PREFIX;
    public static ReporterClient reporterClient;

    static {
        assert config != null;
        try {
            PREFIX = config.getString("REPORT_PREFIX");
            reporterClient = new ReporterClient(config.getInt("PORT"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

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

                    this.send("Coverage Finished: " + (actionCounter));
                }

                if (checkStop()) {
                    this.send("TERMINATED");
                    this.close();
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

        if (!Dungeon.BY_ACTION) {
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

                // Keep the client running
                while (true) {
                    // Sleep the main thread to prevent it from exiting
                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
