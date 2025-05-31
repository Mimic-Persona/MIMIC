package com.codecool.dungeoncrawl.agent.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {

    /**
     * Converts a JSONArray to an int array.
     *
     * @param jsonArray the JSONArray to convert
     * @return the converted int array
     * @throws JSONException if the JSONArray contains non-integer values
     */
    public static int[] jsonArrayToIntArray(JSONArray jsonArray) throws JSONException {
        int[] intArray = new int[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            intArray[i] = jsonArray.getInt(i);
        }

        return intArray;
    }

    /**
     * Converts a JSON file to a Dictionary.
     *
     * @param filePath the path to the JSON file
     * @return a Dictionary containing the key-value pairs from the JSON file
     */
    public static JSONObject jsonFileToDictionary(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return new JSONObject(content);  // ✅ correct for JSON objects
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
