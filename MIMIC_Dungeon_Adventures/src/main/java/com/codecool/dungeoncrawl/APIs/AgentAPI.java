package com.codecool.dungeoncrawl.APIs;

import com.codecool.dungeoncrawl.agent.GLog;
import com.codecool.dungeoncrawl.gui.Main;
import com.codecool.dungeoncrawl.logic.actors.Boss;
import org.json.JSONException;
import org.json.JSONObject;

import static com.codecool.dungeoncrawl.agent.skill_library.basic_skills.Navigate.navigate;
import static com.codecool.dungeoncrawl.gui.Main.map;

public class AgentAPI {

    private static final int COUNTER_INIT = 500;
    private static int counter = COUNTER_INIT;

    public static void handle(String command) throws JSONException {

        if (command.equals("m") || command.equals("s") || command.equals("b") || command.equals("l") || command.equals("k")) {
            JSONObject commandJSON = new JSONObject();
            commandJSON.put("msgType", "command");
            commandJSON.put("command", command);
            Main.gameServer.broadcast(commandJSON.toString());

        } else {
            GLog.e("Invalid Command " + command);
        }
        Main.isAgentNext = true;
    }

    public static boolean handle(String action, int[] tile) throws JSONException {
        boolean res = false;
        counter = COUNTER_INIT;
        Main.isAgentNext = false;

        if (action.equals("moveto")) {
            res = navigate(map.getCell(tile[0], tile[1]));

        } else if (action.equals("attack")) {
            if (Main.level == 2) {

                // Find the boss
                int[] bossTile = new int[2];
                for (int i = 0; i < map.getWidth(); i++) {
                    for (int j = 0; j < map.getHeight(); j++) {
                        if (map.getCell(i, j).getActor() != null && map.getCell(i, j).getActor() instanceof Boss) {
                            bossTile[0] = i;
                            bossTile[1] = j;
                            break;
                        }
                    }
                }


                res = navigate( map.getCell(bossTile[0], bossTile[1]) ); // Attack the boss

            } else {
                res = navigate(map.getCell(tile[0], tile[1]));
            }

        } else if (action.equals("pickup")) {
            res = map.getPlayer().pickUpItem();

        } else {
            GLog.e("Invalid Action " + action);
        }

        while (counter > 0) {
            counter--;
        }

        Main.isAgentNext = true;
        return res;
    }
}
