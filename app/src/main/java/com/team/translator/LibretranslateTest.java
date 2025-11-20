package com.team.translator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LibreTranslateTest {

    public static void main(String[] args) {
        try {
            // Point to your self-hosted LibreTranslate server
            URi url = new URL("http://localhost:5000/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // JSON payload
            JsonObject jsonInput = new JsonObject();
            jsonInput.addProperty("q", "Hello world");
            jsonInput.addProperty("source", "en");
            jsonInput.addProperty("target", "es");
            jsonInput.addProperty("format", "text");

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8")
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            br.close();

            // Parse JSON
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            String translatedText = jsonResponse.get("translatedText").getAsString();

            // Print result
            System.out.println("Translated: " + translatedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

