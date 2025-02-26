package com.megacitycab.mega_city_cab.servlets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.dbcp2.BasicDataSource;
import org.json.simple.JSONObject;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static com.megacitycab.mega_city_cab.config.Security.isValidAdminJWT;
import static com.megacitycab.mega_city_cab.util.JsonPasser.jsonPasser;

@WebServlet(name = "vehicleServlet", value = "/vehicle")
public class VehicleServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action.equals("all")) {
            getAllVehicles(req, resp);
        }else if (action.equals("by-user")){
            getAllVehiclesbyVehicleId(req, resp);
        }
    }

    private void getAllVehiclesbyVehicleId(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        Jws<Claims> claims = isValidAdminJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");

        try {
            JSONObject json = jsonPasser(req);

            int vehicleId = Integer.parseInt(json.get("vehicleId").toString());

            Connection connection = ds.getConnection();
            PreparedStatement statement = connection.prepareStatement("select * from vehicle where vehicleId=?");
            statement.setObject(1,vehicleId);
            ResultSet resultSet = statement.executeQuery();
            JsonArrayBuilder driversArray = Json.createArrayBuilder();
            while (resultSet.next()) {
                JsonObjectBuilder drivers = Json.createObjectBuilder();
                drivers.add("vehicleId",resultSet.getInt(1));
                drivers.add("VehicleNumber",resultSet.getString(2));
                drivers.add("vehicleType",resultSet.getInt(3));
                drivers.add("passengerCount",resultSet.getString(4));
                drivers.add("pricePerKm", resultSet.getString(5));
                drivers.add("vehicleBrand",resultSet.getString(6));
                drivers.add("status",resultSet.getString(7));
                drivers.add("vehicleModel",resultSet.getString(8));

                driversArray.add(drivers);
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

    private void getAllVehicles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM vehicle");
                ResultSet resultSet = statement.executeQuery();
                JsonArrayBuilder driversArray = Json.createArrayBuilder();
                while (resultSet.next()) {
                    JsonObjectBuilder vehicles = Json.createObjectBuilder();
                    vehicles.add("vehicleId",resultSet.getInt(1));
                    vehicles.add("vehicleNumber",resultSet.getString(2));
                    vehicles.add("vehicleType",resultSet.getString(3));
                    vehicles.add("passengerCount",resultSet.getInt(4));
                    vehicles.add("pricePerKm", resultSet.getDouble(5));
                    vehicles.add("vehicleBrand",resultSet.getString(6));
                    vehicles.add("status",resultSet.getString(7));
                    vehicles.add("vehicleModel",resultSet.getString(8));
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

            try {
                JSONObject json = jsonPasser(req);

                // Debugging: Print the entire JSON payload
                System.out.println("Received JSON: " + json.toString());

                String vehicleNumber = (String) json.get("vehicleNumber");
                String vehicleType = (String) json.get("vehicleType");
                int passengerCount = Integer.parseInt((String) json.get("passengerCount"));
                double pricePerKm = Double.parseDouble((String) json.get("pricePerKm"));
                String vehicleBrand = (String) json.get("vehicleBrand");
                String vehicleModel = (String) json.get("vehicleModel");
                String status = (String) json.get("status"); // Ensure the key is "status"

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
                    PreparedStatement pstm = connection.prepareStatement("select * from vehicle where vehicleNumber=?");
                    pstm.setObject(1, vehicleNumber);
                    ResultSet rst = pstm.executeQuery();

                    if (rst.next()) {
                        Integer id1 = rst.getInt(1);
                        if (id1 != null) {
                            // Driver already exists
                            response.add("message", "Driver already exists");
                            response.add("code", 409); // 409 Conflict
                            resp.setStatus(409);
                            writer.print(response.build());
                            writer.close();
                            return;
                        }
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    response.add("message", "Database error");
                    response.add("code", 500);
                    resp.setStatus(500);
                    writer.print(response.build());
                    writer.close();
                    return;
                }

                try {
                    connection = ds.getConnection();
                    PreparedStatement pstm = connection.prepareStatement("insert into vehicle values(?,?,?,?,?,?,?,?)");
                    pstm.setObject(1, 0); // Assuming driveriD is auto-generated
                    pstm.setObject(2, vehicleNumber);
                    pstm.setObject(3, vehicleType);
                    pstm.setObject(4, passengerCount);
                    pstm.setObject(5, pricePerKm);
                    pstm.setObject(6, vehicleBrand);
                    pstm.setObject(7, status);
                    pstm.setObject(8, vehicleModel);
                    int i = pstm.executeUpdate();

                    if (i > 0) {
                        // Successfully inserted
                        response.add("message", "Driver added successfully");
                        response.add("code", 201);
                        resp.setStatus(201);
                    } else {
                        // Insert failed
                        response.add("message", "Failed to add driver");
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
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}
