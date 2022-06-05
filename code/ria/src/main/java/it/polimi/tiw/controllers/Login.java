package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.context.WebContext;
import it.polimi.tiw.utils.ParameterValidator;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/login")
public class Login extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String registered = request.getParameter("registered");
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("registered", registered);
        String path = "/WEB-INF/templates/index.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        // obtain and escape params
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (!ParameterValidator.validate(username)) {
            ctx.setVariable("errorMsg", "No ID Inserted!");
            String path = "/WEB-INF/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        if (!ParameterValidator.validate(password)) {
            ctx.setVariable("errorMsg", "No Password Inserted!");
            String path = "/WEB-INF/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        // query db to authenticate the user
        UserDAO userDao = new UserDAO(connection);
        User user;
        try {
            user = userDao.checkUserLogin(username, password);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message
        String path;
        if (user == null) {
            ctx.setVariable("errorMsg", "Incorrect username or password");
            path = "/WEB-INF/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        request.getSession().setAttribute("user", user);
        String target = "/home";
        path = getServletContext().getContextPath();
        response.sendRedirect(path + target);
    }

}