package com.github.txmy.translations;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();


    public static JsonArray getJsonArray(Credentials credentials, String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "X - translations updater");
        connection.setRequestProperty("Authorization", "Bearer " + credentials.token());

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        InputStream stream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream);

        JsonArray array = GSON.fromJson(reader, JsonArray.class);
        connection.disconnect();

        return array;
    }

    public static JsonObject getJsonObject(Credentials credentials, String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "X - translations updater");
        connection.setRequestProperty("Authorization", "Bearer " + credentials.token());

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        InputStream stream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream);

        JsonObject object = GSON.fromJson(reader, JsonObject.class);
        connection.disconnect();

        return object;
    }


}
