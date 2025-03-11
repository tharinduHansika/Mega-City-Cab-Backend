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

@WebServlet(name = "paymentServlet", value = "/payment")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action.equals("all")) {
            getAllPayments(req, resp);
        } else if (action.equals("by-id")) {
            //getAllVehiclesbyVehicleId(req, resp);
        }
    }

    private void getAllPayments(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Jws<Claims> claims = isValidAdminJWT(req, resp);
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
            Connection connection = null;

            try {
                connection = ds.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM payment");
                ResultSet resultSet = statement.executeQuery();
                JsonArrayBuilder driversArray = Json.createArrayBuilder();
                while (resultSet.next()) {
                    JsonObjectBuilder vehicles = Json.createObjectBuilder();
                    vehicles.add("paymentId",resultSet.getInt(1));
                    vehicles.add("bookingId",resultSet.getInt(2));
                    vehicles.add("customerId",resultSet.getInt(3));
                    vehicles.add("vehicleId",resultSet.getInt(4));
                    vehicles.add("amount", resultSet.getDouble(5));
                    vehicles.add("paymentDate",resultSet.getString(6));
                    vehicles.add("paymentTime",resultSet.getString(7));
                    vehicles.add("status",resultSet.getString(8));
                    driversArray.add(vehicles);
                }
                response.add("data", driversArray);
                response.add("message", "success");
                response.add("code", 200);
                writer.print(response.build());
                writer.close();
                connection.close();


            }catch(Exception e){
                e.printStackTrace(); // Log the exception for debugging
                response.add("message", "Internal server error");
                response.add("code", 500);
            } finally{
                writer.print(response.build());
                writer.close();

            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

                Object bookingIdObj = json.get("bookingId");
                int bookingId;
                if (bookingIdObj instanceof Number) {
                    bookingId = ((Number) bookingIdObj).intValue();
                } else {
                    bookingId = Integer.parseInt(bookingIdObj.toString());
                }

                Object customerIdObj = json.get("customerId");
                int customerId;
                if (customerIdObj instanceof Number) {
                    customerId = ((Number) customerIdObj).intValue();
                } else {
                    customerId = Integer.parseInt(customerIdObj.toString());
                }

                Object vehicleIdObj = json.get("vehicleId");
                int vehicleId;
                if (vehicleIdObj instanceof Number) {
                    vehicleId = ((Number) vehicleIdObj).intValue();
                } else {
                    vehicleId = Integer.parseInt(vehicleIdObj.toString());
                }

                Object amountObj = json.get("amount");
                double amount;
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).doubleValue();
                } else {
                    amount = Double.parseDouble(amountObj.toString());
                }

                String paymentDate = (String) json.get("paymentDate");
                String paymentTime = (String) json.get("paymentTime");
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
                PreparedStatement pstm = null;

                try {
                    connection = ds.getConnection();

                    pstm = connection.prepareStatement(
                            "INSERT INTO payment (bookingId, customerId, vehicleId, amount, paymentDate, paymentTime, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    pstm.setInt(1, bookingId);
                    pstm.setInt(2, customerId);
                    pstm.setInt(3, vehicleId);
                    pstm.setDouble(4, amount);
                    pstm.setString(5, paymentDate);
                    pstm.setString(6, paymentTime);
                    pstm.setString(7, status);

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
                    if (pstm != null) {
                        try {
                            pstm.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
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


//    @Override
//    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Jws<Claims> claims = isValidAdminJWT(req, resp);
//        JsonObjectBuilder response = Json.createObjectBuilder();
//        PrintWriter writer = resp.getWriter();
//        resp.setContentType("application/json");
//
//        if (claims != null) {
//            Object role = claims.getBody().get("role");
//            Object id = claims.getBody().get("userID");
//
//            if (id == null) {
//                response.add("message", "Unauthorized Request");
//                response.add("code", 403);
//                resp.setStatus(403);
//                writer.print(response.build());
//                writer.close();
//                return; // Exit the method if unauthorized
//            }
//
//            BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");
//
//            try {
//                JSONObject json = jsonPasser(req);
//
//                // Debugging: Print the entire JSON payload
//                System.out.println("Received JSON: " + json.toString());
//
//                int paymentId = Integer.parseInt(json.get("paymentId").toString());
//                // Assuming paymentId is provided in the JSON
//                String vehicleNumber = (String) json.get("vehicleNumber");
//                String vehicleType = (String) json.get("vehicleType");
//
//
//                Object passengerCountObj = json.get("passengerCount");
//                int passengerCount;
//                if (passengerCountObj instanceof Number) {
//                    passengerCount = ((Number) passengerCountObj).intValue();
//                } else {
//                    passengerCount = Integer.parseInt(passengerCountObj.toString());
//                }
//
//                Object pricePerKmObj = json.get("pricePerKm");
//                double pricePerKm;
//                if (pricePerKmObj instanceof Number) {
//                    pricePerKm = ((Number) pricePerKmObj).doubleValue();
//                } else {
//                    pricePerKm = Double.parseDouble(pricePerKmObj.toString());
//                }
//
//
//
//                String vehicleBrand = (String) json.get("vehicleBrand");
//                String status = (String) json.get("status");
//                String vehicleModel = (String) json.get("vehicleModel");
//
//                // Debugging: Print the status value
//                System.out.println("Status: " + status);
//
//                if (status == null) {
//                    response.add("message", "Status field is missing or incorrect");
//                    response.add("code", 400);
//                    resp.setStatus(400);
//                    writer.print(response.build());
//                    writer.close();
//                    return;
//                }
//
//                Connection connection = null;
//                PreparedStatement pstmCheck = null;
//                PreparedStatement pstmUpdateDriver = null;
//
//                try {
//                    connection = ds.getConnection();
//
//                    // Check if the driver exists
//                    pstmCheck = connection.prepareStatement("SELECT * FROM vehicle WHERE vehicleId = ?");
//                    pstmCheck.setInt(1, vehicleId);
//                    ResultSet rst = pstmCheck.executeQuery();
//
//                    if (!rst.next()) {
//                        // Driver does not exist
//                        response.add("message", "vehicle not found");
//                        response.add("code", 404); // 404 Not Found
//                        resp.setStatus(404);
//                        writer.print(response.build());
//                        writer.close();
//                        return;
//                    }
//
//                    // Update the driver's information
//                    pstmUpdateDriver = connection.prepareStatement(
//                            "UPDATE vehicle SET vehicleNumber = ?, vehicleType = ?, passengerCount = ?, pricePerKm = ?, vehicleBrand = ?, status = ?, vehicleModel = ? WHERE vehicleId = ?");
//
//                    pstmUpdateDriver.setString(1, vehicleNumber);
//                    pstmUpdateDriver.setString(2, vehicleType);
//                    pstmUpdateDriver.setInt(3, passengerCount);
//                    pstmUpdateDriver.setDouble(4, pricePerKm);
//                    pstmUpdateDriver.setString(5, vehicleBrand);
//                    pstmUpdateDriver.setString(6, status);
//                    pstmUpdateDriver.setString(7, vehicleModel);
//                    pstmUpdateDriver.setInt(8, vehicleId);
//
//                    int i = pstmUpdateDriver.executeUpdate();
//
//                    if (i > 0) {
//                        // Successfully updated
//                        response.add("message", "Driver updated successfully");
//                        response.add("code", 200);
//                        resp.setStatus(200);
//                    } else {
//                        // Update failed
//                        response.add("message", "Failed to update driver");
//                        response.add("code", 500);
//                        resp.setStatus(500);
//                    }
//
//                } catch (SQLException throwables) {
//                    throwables.printStackTrace();
//                    response.add("message", "Database error");
//                    response.add("code", 500);
//                    resp.setStatus(500);
//                } finally {
//                    if (connection != null) {
//                        try {
//                            connection.close();
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace(); // Log the exception for debugging
//                response.add("message", "Internal server error");
//                response.add("code", 500);
//                resp.setStatus(500);
//            } finally {
//                writer.print(response.build());
//                writer.close();
//            }
//
//        } else {
//            // Invalid JWT
//            response.add("message", "Invalid JWT");
//            response.add("code", 401);
//            resp.setStatus(401);
//            writer.print(response.build());
//            writer.close();
//        }
//    }

}
