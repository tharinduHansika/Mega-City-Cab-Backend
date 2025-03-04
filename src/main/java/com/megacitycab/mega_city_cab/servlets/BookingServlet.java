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

import static com.megacitycab.mega_city_cab.config.Security.createJWT;
import static com.megacitycab.mega_city_cab.config.Security.isValidAdminJWT;
import static com.megacitycab.mega_city_cab.util.AESEncryption.decrypt;
import static com.megacitycab.mega_city_cab.util.AESEncryption.encrypt;
import static com.megacitycab.mega_city_cab.util.JsonPasser.jsonPasser;

@WebServlet(name = "bookingServlet", value = "/booking")
public class BookingServlet extends HttpServlet {

}
