package com.megacitycab.mega_city_cab.servlets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.dbcp2.BasicDataSource;
import org.json.simple.JSONObject;

import javax.json.Json;
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

import static com.megacitycab.mega_city_cab.config.Security.isValidAdminJWT;
import static com.megacitycab.mega_city_cab.util.JsonPasser.jsonPasser;

@WebServlet(name = "driverServlet", value = "/driver")
public class DriverServlet extends HttpServlet {
    

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
}
