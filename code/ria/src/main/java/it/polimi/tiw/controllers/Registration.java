package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.context.WebContext;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ParameterValidator;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/registration")
public class Registration extends AbstractServlet {
    final String path = "/WEB-INF/templates/registration.html";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        // obtain every parameter
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String passwordControl = request.getParameter("password-control");

        // check existence of every parameter
        if (!ParameterValidator.validate(name)) {
            ctx.setVariable("errorMsg", "No name inserted!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (!ParameterValidator.validate(surname)) {
            ctx.setVariable("errorMsg", "No surname inserted!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (!ParameterValidator.validate(username)) {
            ctx.setVariable("errorMsg", "No username inserted!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (!ParameterValidator.validate(email)) {
            ctx.setVariable("errorMsg", "No email inserted!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (!ParameterValidator.validate(password)) {
            ctx.setVariable("errorMsg", "No password inserted!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (!ParameterValidator.validate(passwordControl)) {
            ctx.setVariable("errorMsg", "No password-control Inserted!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        // check if the values are correctly formatted
        if (!name.matches("[a-zA-Z]+") || !surname.matches("[a-zA-Z]+") || !username.matches("[a-zA-Z]+")) {
            ctx.setVariable("errorMsg", "Name, surname and username must be only letters!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(email);
        if (!matcher.find()) {
            ctx.setVariable("errorMsg", "The inserted email is not valid!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (!password.equals(passwordControl)) {
            ctx.setVariable("errorMsg", "The passwords don't match!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        // check that the values are within the allowed size (45)
        if (name.length() > 45) {
            ctx.setVariable("errorMsg", "Name too long!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (surname.length() > 45) {
            ctx.setVariable("errorMsg", "Surname too long!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (username.length() > 45) {
            ctx.setVariable("errorMsg", "Username too long!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        if (email.length() > 45) {
            ctx.setVariable("errorMsg", "Email too long!");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        // check if username is available
        UserDAO userDao = new UserDAO(connection);
        try {
            if (userDao.existsUserWithUsername(username)) {
                ctx.setVariable("errorMsg", "The username is already taken!");
                templateEngine.process(path, ctx, response.getWriter());
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // check if email is available
        try {
            if (userDao.existsUserWithEmail(email)) {
                ctx.setVariable("errorMsg", "The email is already taken!");
                templateEngine.process(path, ctx, response.getWriter());
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

        String target = "/login?registered=true";
        String path = getServletContext().getContextPath();
        response.sendRedirect(path + target);
    }

}