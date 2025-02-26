package com.megacitycab.mega_city_cab.servlets;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.dbcp2.BasicDataSource;
import org.json.simple.JSONObject;

import javax.json.Json;
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

import static com.megacitycab.mega_city_cab.config.Security.createJWT;
import static com.megacitycab.mega_city_cab.config.Security.isValidAdminJWT;
import static com.megacitycab.mega_city_cab.util.AESEncryption.decrypt;
import static com.megacitycab.mega_city_cab.util.AESEncryption.encrypt;
import static com.megacitycab.mega_city_cab.util.JsonPasser.jsonPasser;

@WebServlet(name = "userServlet", value = "/user")
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        // Hello
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>vftycvf</h1>");
        out.println("</body></html>");

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action.equals("login")) {
            userLogin(req, resp);
        } else if (action.equals("register")) {
            userRegister(req, resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jws<Claims> claims = isValidAdminJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        if(claims != null) {
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

            try (Connection connection = ds.getConnection()) {
                Integer id1 = Integer.valueOf(req.getParameter("id"));

                // Step 1: Delete from user_has_role first
                PreparedStatement deleteUserRoleStatement = connection.prepareStatement(
                        "DELETE FROM user_has_role WHERE user_id = ?"
                );
                deleteUserRoleStatement.setObject(1, id1);
                deleteUserRoleStatement.executeUpdate();

                // Step 2: Delete the user
                PreparedStatement deleteUserStatement = connection.prepareStatement(
                        "DELETE FROM user WHERE id = ?"
                );
                deleteUserStatement.setObject(1, id1);
                int i = deleteUserStatement.executeUpdate();

                if (i > 0) {
                    response.add("message", "success");
                    response.add("code", 200);
                } else {
                    response.add("message", "User not found");
                    response.add("code", 404);
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log the exception for debugging
                response.add("message", "Internal server error");
                response.add("code", 500);
            } finally {
                writer.print(response.build());
                writer.close();
            }
        } else {
            response.add("message", "Unauthorized Request");
            response.add("code", 403);
            resp.setStatus(403);
            writer.print(response.build());
            writer.close();
        }
    }

    private void userRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        JSONObject json = jsonPasser(req);

        String name = (String) json.get("name");
        String password = (String) json.get("password");
        String email = (String) json.get("email");
        Long roleId = (Long) json.get("roleId");

        Connection connection = null;
        PreparedStatement pstmCheck = null;
        PreparedStatement pstmInsertUser = null;
        PreparedStatement pstmInsertRole = null;
        ResultSet rst = null;
        ResultSet generatedKeys = null;

        try {
            BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");
            connection = ds.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Check if the user already exists
            pstmCheck = connection.prepareStatement("SELECT * FROM user WHERE email=?");
            pstmCheck.setString(1, email);
            rst = pstmCheck.executeQuery();

            if (rst.next()) {
                response.add("message", "Username already exists");
                response.add("code", 404);
                writer.print(response.build());
                writer.close();
                return;
            }

            // Insert new user
            pstmInsertUser = connection.prepareStatement(
                    "INSERT INTO user (id, name, email, password) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            pstmInsertUser.setInt(1, 0);
            pstmInsertUser.setString(2, name);
            pstmInsertUser.setString(3, email);
            pstmInsertUser.setString(4, encrypt(password));

            int affectedRows = pstmInsertUser.executeUpdate();
            generatedKeys = pstmInsertUser.getGeneratedKeys();

            if (affectedRows == 0 || !generatedKeys.next()) {
                throw new SQLException("User registration failed, no ID obtained.");
            }

            int userId = generatedKeys.getInt(1);

            // Check if the roleId exists
            pstmCheck = connection.prepareStatement("SELECT id FROM role WHERE id = ?");
            pstmCheck.setLong(1, roleId);
            rst = pstmCheck.executeQuery();
            if (!rst.next()) {
                response.add("message", "Invalid role ID");
                response.add("code", 400);
                writer.print(response.build());
                writer.close();
                return;
            }

            // Insert role mapping
            pstmInsertRole = connection.prepareStatement(
                    "INSERT INTO user_has_role (id, role_id, user_id) VALUES (?, ?, ?)"
            );
            pstmInsertRole.setInt(1, 0);
            pstmInsertRole.setLong(2, roleId);
            pstmInsertRole.setInt(3, userId);
            pstmInsertRole.executeUpdate();

            // Commit transaction
            connection.commit();

            response.add("message", "Success");
            response.add("code", 201);
            writer.print(response.build());
            writer.close();

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback on failure
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            response.add("message", "Registration failed");
            response.add("code", 500);
            writer.print(response.build());
            writer.close();

        } finally {
            // Close resources
            try { if (rst != null) rst.close(); } catch (SQLException ignored) {}
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException ignored) {}
            try { if (pstmCheck != null) pstmCheck.close(); } catch (SQLException ignored) {}
            try { if (pstmInsertUser != null) pstmInsertUser.close(); } catch (SQLException ignored) {}
            try { if (pstmInsertRole != null) pstmInsertRole.close(); } catch (SQLException ignored) {}
            try { if (connection != null) connection.setAutoCommit(true); connection.close(); } catch (SQLException ignored) {}
        }
    }

    private void userLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");
        System.out.println(response);

//        String userName = req.getParameter("userName");
//        String userPassword = req.getParameter("password");

        JSONObject json = jsonPasser(req);

        String email = (String) json.get("email");
        String userPassword = (String) json.get("password");


        try {
            BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");
            Connection connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("select * from user where email=?");
            pstm.setObject(1, email);
            ResultSet rst = pstm.executeQuery();

            while (rst.next()) {
                Integer id = rst.getInt(1);
                if (!Objects.equals(id, null)) {
                    String password = rst.getString(4);
                    try {
                        String decrypt = decrypt(password);
                        if (userPassword.equals(decrypt)) {

                            PreparedStatement pstm1 = connection.prepareStatement("select r.role\n" +
                                    "from user_has_role uhr\n" +
                                    "         left join role r on r.id = uhr.role_id\n" +
                                    "where uhr.user_id = ?");
                            pstm1.setObject(1,id);
                            ResultSet resultSet = pstm1.executeQuery();
                            if (resultSet.next()) {
                                String jwt = createJWT(resultSet.getString(1), rst.getInt(1));

                                JsonObjectBuilder nestedObject = Json.createObjectBuilder();
                                nestedObject.add("jwt", jwt);
                                nestedObject.add("name", rst.getString(3));
                                nestedObject.add("role", resultSet.getString(1));

                                response.add("data", nestedObject);
                                response.add("message", "success");
                                response.add("code", 200);

                                System.out.println("nestedObject"+nestedObject.toString());

                                writer.println(response.build());
                                writer.close();
                            }
                        } else {
                            response.add("message", "invalid username or password");
                            response.add("code", 404);
                            writer.println(response.build());
                            writer.close();
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
