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

@WebServlet(name = "driverServlet", value = "/driver")
public class DriverServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //getAllDrivers(req, resp);
        //getAllDriversbyDriverId(req, resp);

        String action = req.getParameter("action");
        if (action.equals("all")) {
            getAllDrivers(req, resp);
        }else if (action.equals("by-user")){
            getAllVehiclesbyVehicleId(req, resp);
        }
    }

    private void getAllVehiclesbyVehicleId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Jws<Claims> claims = isValidAdminJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");



        BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");

        try {
            JSONObject json = jsonPasser(req);

            int driverId = Integer.parseInt(json.get("driverId").toString());

            Connection connection = ds.getConnection();
            PreparedStatement statement = connection.prepareStatement("select * from driver where driverId=?");
            statement.setObject(1,driverId);
            ResultSet resultSet = statement.executeQuery();
            JsonArrayBuilder driversArray = Json.createArrayBuilder();
            while (resultSet.next()) {
                JsonObjectBuilder drivers = Json.createObjectBuilder();
                drivers.add("driverId",resultSet.getInt(1));
                drivers.add("name",resultSet.getString(2));
                drivers.add("age",resultSet.getInt(3));
                drivers.add("email",resultSet.getString(4));
                drivers.add("licenseNumber", resultSet.getString(5));
                drivers.add("nicNumber",resultSet.getString(6));
                drivers.add("phoneNumber",resultSet.getString(7));
                drivers.add("homeAddress",resultSet.getString(8));
                drivers.add("status", resultSet.getString(9));

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

    private void getAllDrivers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM driver");
                ResultSet resultSet = statement.executeQuery();
                JsonArrayBuilder driversArray = Json.createArrayBuilder();
                while (resultSet.next()) {
                    JsonObjectBuilder drivers = Json.createObjectBuilder();
                    drivers.add("driverId",resultSet.getInt(1));
                    drivers.add("name",resultSet.getString(2));
                    drivers.add("age",resultSet.getInt(3));
                    drivers.add("email",resultSet.getString(4));
                    drivers.add("licenseNumber", resultSet.getString(5));
                    drivers.add("nicNumber",resultSet.getString(6));
                    drivers.add("phoneNumber",resultSet.getString(7));
                    drivers.add("homeAddress",resultSet.getString(8));
                    drivers.add("status", resultSet.getString(9));

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

                String name = (String) json.get("name");
                int age = Integer.parseInt((String) json.get("age"));
                String email = (String) json.get("email");
                String licenseNumber = (String) json.get("licenseNumber");
                String nicNumber = (String) json.get("nicNumber");
                String phoneNumber = (String) json.get("phoneNumber");
                String homeAddress = (String) json.get("homeAddress");
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
                    PreparedStatement pstm = connection.prepareStatement("select * from driver where licenseNumber=?");
                    pstm.setObject(1, licenseNumber);
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
                    PreparedStatement pstm = connection.prepareStatement("insert into driver values(?,?,?,?,?,?,?,?,?)");
                    pstm.setObject(1, 0); // Assuming driveriD is auto-generated
                    pstm.setObject(2, name);
                    pstm.setObject(3, age);
                    pstm.setObject(4, email);
                    pstm.setObject(5, licenseNumber);
                    pstm.setObject(6, nicNumber);
                    pstm.setObject(7, phoneNumber);
                    pstm.setObject(8, homeAddress);
                    pstm.setObject(9, status); // Ensure status is correctly set
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

                int driverId = Integer.parseInt(json.get("driverId").toString());
                // Assuming driverID is provided in the JSON
                String name = (String) json.get("name");
                int age = Integer.parseInt((String) json.get("age"));
                String email = (String) json.get("email");
                String licenseNumber = (String) json.get("licenseNumber");
                String nicNumber = (String) json.get("nicNumber");
                String phoneNumber = (String) json.get("phoneNumber");
                String homeAddress = (String) json.get("homeAddress");
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
                PreparedStatement pstmUpdateDriver = null;

                try {
                    connection = ds.getConnection();

                    // Check if the driver exists
                    pstmCheck = connection.prepareStatement("SELECT * FROM driver WHERE driverId = ?");
                    pstmCheck.setInt(1, driverId);
                    ResultSet rst = pstmCheck.executeQuery();

                    if (!rst.next()) {
                        // Driver does not exist
                        response.add("message", "Driver not found");
                        response.add("code", 404); // 404 Not Found
                        resp.setStatus(404);
                        writer.print(response.build());
                        writer.close();
                        return;
                    }

                    // Update the driver's information
                    pstmUpdateDriver = connection.prepareStatement(
                            "UPDATE driver SET name = ?, age = ?, email = ?, licenseNumber = ?, nicNumber = ?, phoneNumber = ?, homeAddress = ?, status = ? WHERE driverID = ?");
                    pstmUpdateDriver.setString(1, name);
                    pstmUpdateDriver.setInt(2, age);
                    pstmUpdateDriver.setString(3, email);
                    pstmUpdateDriver.setString(4, licenseNumber);
                    pstmUpdateDriver.setString(5, nicNumber);
                    pstmUpdateDriver.setString(6, phoneNumber);
                    pstmUpdateDriver.setString(7, homeAddress);
                    pstmUpdateDriver.setString(8, status);
                    pstmUpdateDriver.setInt(9, driverId);

                    int i = pstmUpdateDriver.executeUpdate();

                    if (i > 0) {
                        // Successfully updated
                        response.add("message", "Driver updated successfully");
                        response.add("code", 200);
                        resp.setStatus(200);
                    } else {
                        // Update failed
                        response.add("message", "Failed to update driver");
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
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jws<Claims> claims = isValidAdminJWT(req, resp);
        if (!Objects.equals(claims, null)) {
            JsonObjectBuilder response = Json.createObjectBuilder();
            PrintWriter writer = resp.getWriter();
            resp.setContentType("application/json");

            int driverId = Integer.parseInt(req.getParameter("driverId"));
            BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");
            Connection connection = null;
            try {
                connection = ds.getConnection();
                connection.setAutoCommit(false); // Start transaction

                PreparedStatement pstm = connection.prepareStatement("DELETE FROM driver WHERE driverId=?");
                pstm.setObject(1, driverId);
                int i1 = pstm.executeUpdate();

                if (i1 > 0) {
                    response.add("message", "Driver delete success");
                    response.add("code", 200);
                } else {
                    response.add("message", "driver not found");
                    response.add("code", 404);
                }

            } catch (SQLException throwables) {
                if (connection != null) {
                    try {
                        // If any SQL exception occurs, rollback the transaction
                        connection.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                throwables.printStackTrace();
                response.add("message", "you cant change anything");
                response.add("code", 400);
                resp.setStatus(400);
                writer.print(response.build());
            } finally {
                if (connection != null) {
                    try {
                        // Restore auto-commit mode and close the connection
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                writer.close();
            }
        }
    }
}
