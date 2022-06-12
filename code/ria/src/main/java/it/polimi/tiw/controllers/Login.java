package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import it.polimi.tiw.utils.ParameterValidator;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/login")
@MultipartConfig
public class Login extends AbstractServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // obtain and escape params
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (!ParameterValidator.validate(username)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No ID inserted!");
            return;
        }

        if (!ParameterValidator.validate(password)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No password inserted!");
            return;
        }

        // query db to authenticate the user
        UserDAO userDao = new UserDAO(connection);
        User user;
        try {
            user = userDao.checkUserLogin(username, password);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not Possible to check credentials");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Incorrect username or password");
            return;
        }

        request.getSession().setAttribute("user", user);

        String json = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

}