package com.megacitycab.mega_city_cab.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.util.Date;
import java.util.Objects;

public class Security {

    private static final String SECRET_KEY = "megacitycab";

    public static String createJWT(String type, int userID) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTime = currentTimeMillis + (10 * 60 * 60 * 1000); // 10 hour

        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(type)
                .claim("userID", userID)
                .claim("role", type)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(expirationTime))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public static Jws<Claims> isValidUserJWT(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        try {

            String authHeader = req.getHeader("Authorization");

            System.out.println(authHeader);

            resp.setContentType("application/json");

            if (authHeader != null) {
                // Extract the token from the Authorization header
                String token = authHeader.substring(7); // Remove "Bearer " prefix

                Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token);
                Jws<Claims> claims = getIDFromJWT(token);
                Object role = claims.getBody().get("role");

                if (Objects.equals(role, "User")){
                    return getIDFromJWT(token);
                }
            }
            response.add("message", "Unauthorized Request");
            response.add("code", 403);
            resp.setStatus(403);

            writer.print(response.build());
            writer.close();
            return null;
        } catch (Exception e) {


            response.add("message", "Unauthorized Request");
            response.add("code", 403);
            resp.setStatus(403);

            writer.print(response.build());
            writer.close();
        }
        response.add("message", "Unauthorized Request");
        response.add("code", 403);
        resp.setStatus(403);

        writer.print(response.build());
        writer.close();
        return null;
    }

    public static Jws<Claims> isValidAdminJWT(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        try {

            String authHeader = req.getHeader("Authorization");
            resp.setContentType("application/json");

            if (authHeader != null) {
                // Extract the token from the Authorization header
                String token = authHeader.substring(7); // Remove "Bearer " prefix

                Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token);
                Jws<Claims> claims = getIDFromJWT(token);
                Object role = claims.getBody().get("role");

                if (Objects.equals(role, "Admin")){
                    return getIDFromJWT(token);
                }
            }
            response.add("message", "Unauthorized Request");
            response.add("code", 403);
            resp.setStatus(403);

            writer.print(response.build());
            writer.close();
            return null;
        } catch (Exception e) {


            response.add("message", "Unauthorized Request");
            response.add("code", 403);
            resp.setStatus(403);

            writer.print(response.build());
            writer.close();
        }
        response.add("message", "Unauthorized Request");
        response.add("code", 403);
        resp.setStatus(403);

        writer.print(response.build());
        writer.close();
        return null;
    }

    public static Jws<Claims> getIDFromJWT(String jwt) {
        return Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(jwt);
    }

}
