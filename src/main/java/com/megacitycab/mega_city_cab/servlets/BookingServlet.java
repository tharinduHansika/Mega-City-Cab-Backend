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
import java.util.*;
import java.util.Date;

import static com.megacitycab.mega_city_cab.config.Security.*;
import static com.megacitycab.mega_city_cab.util.AESEncryption.decrypt;
import static com.megacitycab.mega_city_cab.util.AESEncryption.encrypt;
import static com.megacitycab.mega_city_cab.util.JsonPasser.jsonPasser;

@WebServlet(name = "bookingServlet", value = "/booking")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action.equals("all")) {
            getAllBookings(req, resp);
        } else if (action.equals("by-id")) {
            getBookingById(req, resp);
        }
    }

private void getAllBookings(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
            return;
        }

        BasicDataSource ds = (BasicDataSource) getServletContext().getAttribute("ds");
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM booking");
            ResultSet resultSet = statement.executeQuery();
            JsonArrayBuilder bookingsArray = Json.createArrayBuilder();

            while (resultSet.next()) {
                JsonObjectBuilder booking = Json.createObjectBuilder();
                booking.add("bookingId", resultSet.getInt("bookingId"));
                booking.add("amount", resultSet.getDouble("amount"));
                booking.add("bookingDate", resultSet.getString("bookingDate"));
                booking.add("bookingTime", resultSet.getString("bookingTime"));
                booking.add("dropLocation", resultSet.getString("dropLocation"));
                booking.add("pickupLocation", resultSet.getString("pickupLocation"));
                booking.add("totalKm", resultSet.getDouble("totalKm"));
                booking.add("userEmail", resultSet.getString("userEmail"));
                booking.add("driverId", resultSet.getInt("driverId"));
                booking.add("vehicleId", resultSet.getInt("vehicleId"));
                booking.add("status", resultSet.getString("status"));

                bookingsArray.add(booking);
            }

            response.add("data", bookingsArray);
            response.add("message", "success");
            response.add("code", 200);
            resp.setStatus(200);
        } catch (Exception e) {
            e.printStackTrace();
            response.add("message", "Internal server error");
            response.add("code", 500);
            resp.setStatus(500);
        }

        writer.print(response.build());
        writer.close();
    }
}
   /* private void getBookingById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
            Connection connection = null;

            try {
                JSONObject json = jsonPasser(req);
                int bookingId = Integer.parseInt(json.get("bookingId").toString());

                connection = ds.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM booking WHERE bookingId = ?");
                statement.setInt(1, bookingId);
                ResultSet resultSet = statement.executeQuery();
                JsonArrayBuilder bookingsArray = Json.createArrayBuilder();
                while (resultSet.next()) {
                    JsonObjectBuilder booking = Json.createObjectBuilder();
                    booking.add("bookingId", resultSet.getInt("bookingId"));
                    booking.add("amount", resultSet.getDouble("amount"));
                    booking.add("bookingDate", resultSet.getString("bookingDate"));
                    booking.add("bookingTime", resultSet.getString("bookingTime"));
                    booking.add("dropLocation", resultSet.getString("dropLocation"));
                    booking.add("pickupLocation", resultSet.getString("pickupLocation"));
                    booking.add("totalKm", resultSet.getDouble("totalKm"));
                    booking.add("userEmail", resultSet.getString("userEmail"));
                    booking.add("driverId", resultSet.getInt("driverId"));
                    booking.add("vehicleId", resultSet.getInt("vehicleId"));
                    booking.add("status", resultSet.getString("status"));

                    bookingsArray.add(booking);
                }
                response.add("data", bookingsArray);
                response.add("message", "success");
                response.add("code", 200);
                writer.print(response.build());
                writer.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                response.add("message", "Internal server error: " + e.getMessage());
                response.add("code", 500);
                writer.print(response.build());
                writer.close();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }*/
   private void getBookingById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
           Connection connection = null;

           try {
               // Read bookingId from query parameters
               int bookingId = Integer.parseInt(req.getParameter("bookingId"));

               connection = ds.getConnection();
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM booking WHERE bookingId = ?");
               statement.setInt(1, bookingId);
               ResultSet resultSet = statement.executeQuery();
               JsonArrayBuilder bookingsArray = Json.createArrayBuilder();
               while (resultSet.next()) {
                   JsonObjectBuilder booking = Json.createObjectBuilder();
                   booking.add("bookingId", resultSet.getInt("bookingId"));
                   booking.add("amount", resultSet.getDouble("amount"));
                   booking.add("bookingDate", resultSet.getString("bookingDate"));
                   booking.add("bookingTime", resultSet.getString("bookingTime"));
                   booking.add("dropLocation", resultSet.getString("dropLocation"));
                   booking.add("pickupLocation", resultSet.getString("pickupLocation"));
                   booking.add("totalKm", resultSet.getDouble("totalKm"));
                   booking.add("userEmail", resultSet.getString("userEmail"));
                   booking.add("driverId", resultSet.getInt("driverId"));
                   booking.add("vehicleId", resultSet.getInt("vehicleId"));
                   booking.add("status", resultSet.getString("status"));

                   bookingsArray.add(booking);
               }
               response.add("data", bookingsArray);
               response.add("message", "success");
               response.add("code", 200);
               writer.print(response.build());
               writer.close();
               connection.close();
           } catch (Exception e) {
               e.printStackTrace();
               response.add("message", "Internal server error: " + e.getMessage());
               response.add("code", 500);
               writer.print(response.build());
               writer.close();
           } finally {
               if (connection != null) {
                   try {
                       connection.close();
                   } catch (SQLException e) {
                       e.printStackTrace();
                   }
               }
           }
       }
   }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jws<Claims> claims = isValidUserJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        if (claims == null) {
            response.add("message", "Invalid JWT");
            response.add("code", 401);
            resp.setStatus(401);
            writer.print(response.build());
            writer.close();
            return;
        }

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
            System.out.println("Received JSON: " + json.toString());

            Object amountObj = json.get("amount");
            double amount = (amountObj instanceof Number) ?
                    ((Number) amountObj).doubleValue() :
                    Double.parseDouble(amountObj.toString());

            String bookingDate = (String) json.get("bookingDate");
            String bookingTime = (String) json.get("bookingTime");
            String dropLocation = (String) json.get("dropLocation");
            String pickupLocation = (String) json.get("pickupLocation");

            Object totalKmtObj = json.get("totalKm");
            double totalKm = (totalKmtObj instanceof Number) ?
                    ((Number) totalKmtObj).doubleValue() :
                    Double.parseDouble(totalKmtObj.toString());

            String userEmail = (String) json.get("userEmail");

            Object driverIdObj = json.get("driverId");
            int driverId = (driverIdObj instanceof Number) ?
                    ((Number) driverIdObj).intValue() :
                    Integer.parseInt(driverIdObj.toString());

            Object vehicleIdObj = json.get("vehicleId");
            int vehicleId = (vehicleIdObj instanceof Number) ?
                    ((Number) vehicleIdObj).intValue() :
                    Integer.parseInt(vehicleIdObj.toString());

            String status = (String) json.get("status");
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
            try {
                connection = ds.getConnection();
                connection.setAutoCommit(false);  // Start transaction

                // Validate driver availability
                int assignedDriverId = getValidDriverId(connection, driverId);
                if (assignedDriverId == -1) {
                    response.add("message", "No available drivers");
                    response.add("code", 400);
                    resp.setStatus(400);
                    writer.print(response.build());
                    writer.close();
                    connection.rollback();
                    return;
                }

                // Validate vehicle availability
                int assignedVehicleId = getValidVehicleId(connection, vehicleId);
                if (assignedVehicleId == -1) {
                    response.add("message", "No available vehicle");
                    response.add("code", 400);
                    resp.setStatus(400);
                    writer.print(response.build());
                    writer.close();
                    connection.rollback();
                    return;
                }

                PreparedStatement pstm = connection.prepareStatement(
                        "INSERT INTO booking (bookingId, amount, bookingDate, bookingTime, dropLocation, pickupLocation, totalKm, userEmail, driverId, vehicleId, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                pstm.setInt(1, 0);
                pstm.setDouble(2, amount);
                pstm.setString(3, bookingDate);
                pstm.setString(4, bookingTime);
                pstm.setString(5, dropLocation);
                pstm.setString(6, pickupLocation);
                pstm.setDouble(7, totalKm);
                pstm.setString(8, userEmail);
                pstm.setInt(9, assignedDriverId);
                pstm.setInt(10, assignedVehicleId);
                pstm.setString(11, status);

                int i = pstm.executeUpdate();

                if (i > 0) {
                    // Retrieve the generated booking ID
                    ResultSet generatedKeys = pstm.getGeneratedKeys();
                    int bookingId = -1;
                    if (generatedKeys.next()) {
                        bookingId = generatedKeys.getInt(1);
                    }

                    // Update driver status
                    String updateQuery = "UPDATE driver SET status = 'busy' WHERE driverId = ?";
                    PreparedStatement updatePstm = connection.prepareStatement(updateQuery);
                    updatePstm.setInt(1, assignedDriverId);
                    updatePstm.executeUpdate();

                    // Update vehicle status
                    String updateVehicleQuery = "UPDATE vehicle SET status='busy' WHERE vehicleId=?";
                    PreparedStatement updateVehiclePstm = connection.prepareStatement(updateVehicleQuery);
                    updateVehiclePstm.setInt(1, assignedVehicleId);
                    updateVehiclePstm.executeUpdate();

                    connection.commit();

                    response.add("message", "Booking added successfully");
                    response.add("bookingId", bookingId);
                    response.add("amount", amount);
                    response.add("bookingDate", bookingDate);
                    response.add("bookingTime", bookingTime);
                    response.add("dropLocation", dropLocation);
                    response.add("pickupLocation", pickupLocation);
                    response.add("totalKm", totalKm);
                    response.add("userEmail", userEmail);
                    response.add("driverId", assignedDriverId);
                    response.add("vehicleId", assignedVehicleId);
                    response.add("status", status);
                    resp.setStatus(201);
                } else {
                    connection.rollback();
                    response.add("message", "Failed to add booking");
                    response.add("code", 500);
                    resp.setStatus(500);
                }

            } catch (SQLException throwables) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                throwables.printStackTrace();
                response.add("message", "Database error: " + throwables.getMessage());
                response.add("code", 500);
                resp.setStatus(500);
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.add("message", "Internal server error: " + e.getMessage());
            response.add("code", 500);
            resp.setStatus(500);
        } finally {
            writer.print(response.build());
            writer.close();
        }
    }

    private int getValidDriverId(Connection connection, int requestedDriverId) throws SQLException {
        String query = "SELECT driverId FROM driver WHERE driverId = ? AND status = 'available'";

        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, requestedDriverId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("driverId");  // Return requested driver if available
                }
            }
        }

        // If requested driver not available, get any available driver
        return returnAvailable(connection);
    }

    private int getValidVehicleId(Connection connection, int requestedVehicleId) throws SQLException {
        String query = "SELECT vehicleId FROM vehicle WHERE vehicleId = ? AND status = 'available'";

        try (PreparedStatement pstm = connection.prepareStatement(query)) {
            pstm.setInt(1, requestedVehicleId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("vehicleId");  // Return requested vehicle if available
                }
            }
        }

        // If requested vehicle not available, get any available vehicle
        return returnAvailable(connection);
    }
    private int returnAvailable(Connection connection) throws SQLException {
        List<Integer> availableDriverIds = new ArrayList<>();
        String query = "SELECT driverId FROM driver WHERE status = 'available'";

        try (PreparedStatement pstm = connection.prepareStatement(query);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                availableDriverIds.add(rs.getInt("driverId"));
            }
        }

        if (availableDriverIds.isEmpty()) {
            return -1; // No available drivers
        }

        Random random = new Random();
        return availableDriverIds.get(random.nextInt(availableDriverIds.size()));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jws<Claims> claims = isValidAdminJWT(req, resp);
        JsonObjectBuilder response = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();
        resp.setContentType("application/json");

        if (claims == null) {
            response.add("message", "Invalid JWT");
            response.add("code", 401);
            resp.setStatus(401);
            writer.print(response.build());
            writer.close();
            return;
        }

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
            System.out.println("Received JSON: " + json.toString());

            int bookingId = Integer.parseInt(json.get("bookingId").toString());
            Double amount = Double.parseDouble(json.get("amount").toString());
            String bookingDate = json.get("bookingDate").toString();
            String bookingTime = json.get("bookingTime").toString();
            String dropLocation = json.get("dropLocation").toString();
            String pickupLocation = json.get("pickupLocation").toString();
            Double totalKm = Double.parseDouble(json.get("totalKm").toString());
            String  userEmail = json.get("userEmail").toString();
            int driverId = Integer.parseInt(json.get("driverId").toString());
            int vehicleId = Integer.parseInt(json.get("vehicleId").toString());
            String status = json.get("status").toString();

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
                connection.setAutoCommit(false); // Start transaction

                // Check if the booking exists
                pstmCheck = connection.prepareStatement("SELECT * FROM booking WHERE bookingId = ?");
                pstmCheck.setInt(1, bookingId);
                ResultSet rst = pstmCheck.executeQuery();

                if (!rst.next()) {
                    // Booking does not exist
                    response.add("message", "Booking not found");
                    response.add("code", 404);
                    resp.setStatus(404);
                    writer.print(response.build());
                    writer.close();
                    return;
                }

                // Get the current driverId and vehicleId from the booking
                int currentDriverId = rst.getInt("driverId");
                int currentVehicleId = rst.getInt("vehicleId");

                // Update the booking details
                pstmUpdate = connection.prepareStatement(
                        "UPDATE booking SET amount = ?, bookingDate = ?, bookingTime = ?, dropLocation = ?, " +
                                "pickupLocation = ?, totalKm = ?, userEmail = ?, driverId = ?, vehicleId = ?, status = ? " +
                                "WHERE bookingId = ?"
                );

                pstmUpdate.setDouble(1, amount);
                pstmUpdate.setString(2, bookingDate);
                pstmUpdate.setString(3, bookingTime);
                pstmUpdate.setString(4, dropLocation);
                pstmUpdate.setString(5, pickupLocation);
                pstmUpdate.setDouble(6, totalKm);
                pstmUpdate.setString(7, userEmail);
                pstmUpdate.setInt(8, driverId);
                pstmUpdate.setInt(9, vehicleId);
                pstmUpdate.setString(10, status);
                pstmUpdate.setInt(11, bookingId);

                int i = pstmUpdate.executeUpdate();

                if (i > 0) {
                    // Update driver and vehicle statuses based on the new booking status
                    if ("completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                        // Free up the current driver and vehicle
                        updateDriverStatus(connection, currentDriverId, "available");
                        updateVehicleStatus(connection, currentVehicleId, "available");
                    } else if ("active".equalsIgnoreCase(status)) {
                        // Assign the new driver and vehicle
                        updateDriverStatus(connection, driverId, "busy");
                        updateVehicleStatus(connection, vehicleId, "busy");
                    }

                    connection.commit(); // Commit the transaction
                    response.add("message", "Booking updated successfully");
                    response.add("code", 200);
                    resp.setStatus(200);
                } else {
                    connection.rollback(); // Rollback the transaction
                    response.add("message", "Failed to update booking");
                    response.add("code", 500);
                    resp.setStatus(500);
                }

            } catch (SQLException throwables) {
                if (connection != null) {
                    try {
                        connection.rollback(); // Rollback on error
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                throwables.printStackTrace();
                response.add("message", "Database error: " + throwables.getMessage());
                response.add("code", 500);
                resp.setStatus(500);
            } finally {
                if (pstmCheck != null) try { pstmCheck.close(); } catch (SQLException e) { e.printStackTrace(); }
                if (pstmUpdate != null) try { pstmUpdate.close(); } catch (SQLException e) { e.printStackTrace(); }
                if (connection != null) try { connection.setAutoCommit(true); connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.add("message", "Internal server error: " + e.getMessage());
            response.add("code", 500);
            resp.setStatus(500);
        } finally {
            writer.print(response.build());
            writer.close();
        }
    }
    private void updateDriverStatus(Connection connection, int driverId, String status) {
        PreparedStatement pstm = null;
        try {
            String query = "UPDATE driver SET status = ? WHERE driverId = ?";
            pstm = connection.prepareStatement(query);
            pstm.setString(1, status);
            pstm.setInt(2, driverId);

            int rowsUpdated = pstm.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Driver status updated successfully for driverId: " + driverId);
            } else {
                System.out.println("No driver found with driverId: " + driverId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating driver status: " + e.getMessage());
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateVehicleStatus(Connection connection, int vehicleId, String status) {
        PreparedStatement pstm = null;
        try {
            String query = "UPDATE vehicle SET status = ? WHERE vehicleId = ?";
            pstm = connection.prepareStatement(query);
            pstm.setString(1, status);
            pstm.setInt(2, vehicleId);

            int rowsUpdated = pstm.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Vehicle status updated successfully for vehicleId: " + vehicleId);
            } else {
                System.out.println("No vehicle found with vehicleId: " + vehicleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating vehicle status: " + e.getMessage());
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
/*    private void updateVehicleStatus(Connection connection, int vehicleId) {
        PreparedStatement pstm = null;
        try {
            // Prepare the SQL query to update the vehicle status
            String query = "UPDATE vehicle SET status = 'available' WHERE vehicleId = ?";
            pstm = connection.prepareStatement(query);
            pstm.setInt(1, vehicleId);

            // Execute the update
            int rowsUpdated = pstm.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Vehicle status updated successfully for vehicleId: " + vehicleId);
            } else {
                System.out.println("No vehicle found with vehicleId: " + vehicleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating vehicle status: " + e.getMessage());
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateDriverStatus(Connection connection, int driverId) {
        PreparedStatement pstm = null;
        try {
            // Prepare the SQL query to update the driver status
            String query = "UPDATE driver SET status = 'available' WHERE driverId = ?";
            pstm = connection.prepareStatement(query);
            pstm.setInt(1, driverId);

            // Execute the update
            int rowsUpdated = pstm.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Driver status updated successfully for driverId: " + driverId);
            } else {
                System.out.println("No driver found with driverId: " + driverId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating driver status: " + e.getMessage());
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

}
