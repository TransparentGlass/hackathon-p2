package com.team.translator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class Translator {

    public HttpURLConnection establishConnection() throws Exception {
        URI uri = URI.create("http://127.0.0.1:5000/translate");
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        return connection;
    }

    public String translate(String text, String source, String target) {
    try {
        HttpURLConnection connection = establishConnection();
        connection.setRequestProperty("Accept", "application/json");

        JsonObject jsonInput = new JsonObject();
        jsonInput.addProperty("q", text);
        jsonInput.addProperty("source", source);
        jsonInput.addProperty("target", target);
        jsonInput.addProperty("format", "text");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = connection.getResponseCode();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                status == 200 ? connection.getInputStream() : connection.getErrorStream(),
                "utf-8"
            )
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }
        br.close();

        System.out.println("Raw response: " + response);

        // JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        // if (jsonResponse.has("translatedText")) {
        //     String translatedText = jsonResponse.get("translatedText").getAsString();
        //     System.out.println("Translated: " + translatedText);
        //     return translatedText;
        // } else {
        //     return "Error response: " + response;
        // }


        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        String translatedText = jsonResponse.get("translatedText").getAsString();
        return translatedText;


    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}


}
