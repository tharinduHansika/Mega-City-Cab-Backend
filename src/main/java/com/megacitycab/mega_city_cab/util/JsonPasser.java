package com.megacitycab.mega_city_cab.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class JsonPasser {
    public static JSONObject jsonPasser(HttpServletRequest req) throws IOException {
        String jsonBody = new BufferedReader(new InputStreamReader(req.getInputStream())).lines().collect(
                Collectors.joining("\n"));

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(jsonBody);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}

