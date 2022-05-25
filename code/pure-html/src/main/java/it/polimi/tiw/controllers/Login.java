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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.User;

@WebServlet("/login")
public class Login extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

        //thymeleaf
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/templates/index.html";
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
            String path = "/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        if (password == null || password.equals("")) {
            ctx.setVariable("errorMsg", "No Password Inserted!");
            String path = "/templates/index.html";
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
            path = "/templates/index.html";
            templateEngine.process(path, ctx, response.getWriter());
        } else {
            request.getSession().setAttribute("user", user);
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