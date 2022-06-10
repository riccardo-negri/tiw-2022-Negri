package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ParameterValidator;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/registration")
@MultipartConfig
public class Registration extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // obtain every parameter
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String passwordControl = request.getParameter("password-control");

        // check existence of every parameter
        if (!ParameterValidator.validate(name)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No name inserted!");
            return;
        }
        if (!ParameterValidator.validate(surname)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No surname inserted!");
            return;
        }
        if (!ParameterValidator.validate(username)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No username inserted!");
            return;
        }
        if (!ParameterValidator.validate(email)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No email inserted!");
            return;
        }
        if (!ParameterValidator.validate(password)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No password inserted!");
            return;
        }
        if (!ParameterValidator.validate(passwordControl)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No password-control Inserted!");
            return;
        }

        // check if the values are correctly formatted
        if (!name.matches("[a-zA-Z]+") || !surname.matches("[a-zA-Z]+") || !username.matches("[a-zA-Z]+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Name, surname and username must be only letters!");
            return;
        }
        Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(email);
        if (!matcher.find()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The inserted email is not valid!");
            return;
        }
        if (!password.equals(passwordControl)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The passwords don't match!");
            return;
        }

        // check that the values are within the allowed size (45)
        if (name.length() > 45) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Name too long!");
            return;
        }
        if (surname.length() > 45) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Surname too long!");
            return;
        }
        if (username.length() > 45) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Username too long!");
            return;
        }
        if (email.length() > 45) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Email too long!");
            return;
        }

        // check if username is available
        UserDAO userDao = new UserDAO(connection);
        try {
            if (userDao.existsUserWithUsername(username)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("The username is already taken!");
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // check if email is available
        try {
            if (userDao.existsUserWithEmail(email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("The email is already taken!");
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // create the account
        try {
            userDao.createUser(username, email, password, name, surname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

}