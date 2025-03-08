package com.megacitycab.mega_city_cab.servlets;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.dbcp2.BasicDataSource;
import org.json.simple.JSONObject;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Date;
import java.util.Objects;

import static com.megacitycab.mega_city_cab.config.Security.*;
import static com.megacitycab.mega_city_cab.util.AESEncryption.decrypt;
import static com.megacitycab.mega_city_cab.util.AESEncryption.encrypt;
import static com.megacitycab.mega_city_cab.util.JsonPasser.jsonPasser;

@WebServlet(name = "bookingServlet", value = "/booking")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //addBooking(req, resp);

        Jws<Claims> claims = isValidUserJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        if (claims != null) {
            Object role = claims.getBody().get("role");
            Object id = claims.getBody().get("userID");

            if (id == null) {
                response.add("message", "Unauthorized Request");
                response.add("code", 403);
                resp.setStatus(403);
                writer.print(response.build());
                writer.close();
                return; // Exit the method if unauthorized
            }

            BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");

            try {
                JSONObject json = jsonPasser(req);

                // Debugging: Print the entire JSON payload
                System.out.println("Received JSON: " + json.toString());


                Double amount = Double.parseDouble ((String) json.get("amount"));
                String bookingDate = (String) json.get("bookingDate");
                String bookingTime = (String) json.get("bookingTime");
                String dropLocation = (String) json.get("dropLocation");
                String pickupLocation = (String) json.get("pickupLocation");
                Double totalKm = Double.parseDouble ((String) json.get("totalKm"));
                int customerId = Integer.parseInt ((String) json.get("customerId"));
                int driverId = Integer.parseInt ((String) json.get("driverId"));
                int vehicleId = Integer.parseInt ((String) json.get("vehicleId"));
                String status = (String) json.get("status");

                // Debugging: Print the status value
                System.out.println("Status: " + status);

                if (status == null) {
                    response.add("message", "Status field is missing or incorrect");
                    response.add("code", 400);
                    resp.setStatus(400);
                    writer.print(response.build());
                    writer.close();
                    return;
                }

                Connection connection = null;
                PreparedStatement pstmCheck = null;
                PreparedStatement pstmInsertUser = null;
                ResultSet generatedKeys = null;

                HttpSession session = req.getSession();


                try {
                    connection = ds.getConnection();

                    PreparedStatement pstm = connection.prepareStatement(
                            "INSERT INTO booking (bookingId, amount, bookingDate, bookingTime, dropLocation, pickupLocation, totalKm, customerId, driverId, vehicleId, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    pstm.setInt(1, 0);
                    pstm.setDouble(2, amount);
                    pstm.setString(3, bookingDate);
                    pstm.setString(4, bookingTime);
                    pstm.setString(5, dropLocation);
                    pstm.setString(6, pickupLocation);
                    pstm.setDouble(7, totalKm);
                    pstm.setInt(8, customerId);
                    pstm.setInt(9, driverId);
                    pstm.setInt(10, vehicleId);
                    pstm.setString(11, status);


                    int i = pstm.executeUpdate();

                    if (i > 0) {
                        // Successfully inserted
                        response.add("message", "Booking added successfully");
                        response.add("code", 201);
                        resp.setStatus(201);
                    } else {
                        // Insert failed
                        response.add("message", "Failed to add booking");
                        response.add("code", 500);
                        resp.setStatus(500);
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    response.add("message", "Database error");
                    response.add("code", 500);
                    resp.setStatus(500);
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace(); // Log the exception for debugging
                response.add("message", "Internal server error");
                response.add("code", 500);
                resp.setStatus(500);
            } finally {
                writer.print(response.build());
                writer.close();
            }

        } else {
            // Invalid JWT
            response.add("message", "Invalid JWT");
            response.add("code", 401);
            resp.setStatus(401);
            writer.print(response.build());
            writer.close();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jws<Claims> claims = isValidUserJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        if (claims != null) {
            Object role = claims.getBody().get("role");
            Object id = claims.getBody().get("userID");

            if (id == null) {
                response.add("message", "Unauthorized Request");
                response.add("code", 403);
                resp.setStatus(403);
                writer.print(response.build());
                writer.close();
                return;
            }

            BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");

            try {
                JSONObject json = jsonPasser(req);

                // Debugging: Print the entire JSON payload
                System.out.println("Received JSON: " + json.toString());

                int bookingId = Integer.parseInt(json.get("bookingId").toString());
                Double amount = Double.parseDouble(json.get("amount").toString());
                String bookingDate = json.get("bookingDate").toString();
                String bookingTime = json.get("bookingTime").toString();
                String dropLocation = json.get("dropLocation").toString();
                String pickupLocation = json.get("pickupLocation").toString();
                Double totalKm = Double.parseDouble(json.get("totalKm").toString());
                int customerId = Integer.parseInt(json.get("customerId").toString());
                int driverId = Integer.parseInt(json.get("driverId").toString());
                int vehicleId = Integer.parseInt(json.get("vehicleId").toString());
                String status = json.get("status").toString();

                // Debugging: Print the status value
                System.out.println("Status: " + status);

                if (status == null) {
                    response.add("message", "Status field is missing or incorrect");
                    response.add("code", 400);
                    resp.setStatus(400);
                    writer.print(response.build());
                    writer.close();
                    return;
                }

                Connection connection = null;
                PreparedStatement pstmCheck = null;
                PreparedStatement pstmUpdate = null;

                try {
                    connection = ds.getConnection();

                    // Check if the booking exists
                    pstmCheck = connection.prepareStatement("SELECT * FROM booking WHERE bookingId = ?");
                    pstmCheck.setInt(1, bookingId);
                    ResultSet rst = pstmCheck.executeQuery();

                    if (!rst.next()) {
                        // Booking does not exist
                        response.add("message", "Booking not found");
                        response.add("code", 404); // 404 Not Found
                        resp.setStatus(404);
                        writer.print(response.build());
                        writer.close();
                        return;
                    }

                    // Update the booking details
                    pstmUpdate = connection.prepareStatement(
                            "UPDATE booking SET amount = ?, bookingDate = ?, bookingTime = ?, dropLocation = ?, " +
                                    "pickupLocation = ?, totalKm = ?, customerId = ?, driverId = ?, vehicleId = ?, status = ? " +
                                    "WHERE bookingId = ?"
                    );

                    pstmUpdate.setDouble(1, amount);
                    pstmUpdate.setString(2, bookingDate);
                    pstmUpdate.setString(3, bookingTime);
                    pstmUpdate.setString(4, dropLocation);
                    pstmUpdate.setString(5, pickupLocation);
                    pstmUpdate.setDouble(6, totalKm);
                    pstmUpdate.setInt(7, customerId);
                    pstmUpdate.setInt(8, driverId);
                    pstmUpdate.setInt(9, vehicleId);
                    pstmUpdate.setString(10, status);
                    pstmUpdate.setInt(11, bookingId);

                    int i = pstmUpdate.executeUpdate();

                    if (i > 0) {
                        // Successfully updated
                        response.add("message", "Booking updated successfully");
                        response.add("code", 200);
                        resp.setStatus(200);
                    } else {
                        // Update failed
                        response.add("message", "Failed to update booking");
                        response.add("code", 500);
                        resp.setStatus(500);
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    response.add("message", "Database error");
                    response.add("code", 500);
                    resp.setStatus(500);
                } finally {
                    if (pstmCheck != null) try { pstmCheck.close(); } catch (SQLException e) { e.printStackTrace(); }
                    if (pstmUpdate != null) try { pstmUpdate.close(); } catch (SQLException e) { e.printStackTrace(); }
                    if (connection != null) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
                }

            } catch (Exception e) {
                e.printStackTrace(); // Log the exception for debugging
                response.add("message", "Internal server error");
                response.add("code", 500);
                resp.setStatus(500);
            } finally {
                writer.print(response.build());
                writer.close();
            }

        } else {
            // Invalid JWT
            response.add("message", "Invalid JWT");
            response.add("code", 401);
            resp.setStatus(401);
            writer.print(response.build());
            writer.close();
        }
    }
}
