package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/login")
public class Login extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            response.sendRedirect("home");
            return;
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/templates/index.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    /**
     * validate the login, if it is not correct just forward on himself
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        // obtain and escape params
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.equals("")) {
            ctx.setVariable("errorMsg", "No ID Inserted!");
            String path = "/WEB-INF/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        if (password == null || password.equals("")) {
            ctx.setVariable("errorMsg", "No Password Inserted!");
            String path = "/WEB-INF/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        // query db to authenticate for user
        UserDAO userDao = new UserDAO(connection);
        User user;
        try {
            user = userDao.checkUserLogin(username, password);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Not Possible to check credentials");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message
        String path;
        if (user == null) {
            ctx.setVariable("errorMsg", "Incorrect username or password");
            path = "/WEB-INF/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
        } else {
            request.getSession().setAttribute("user", user); //TODO set as session atteribute the ID so it is more scalable
            String target = "/home";
            path = getServletContext().getContextPath();
            response.sendRedirect(path + target);
        }
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}