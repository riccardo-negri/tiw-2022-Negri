package it.polimi.tiw.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.tiw.utils.ConnectionHandler;

public abstract class AbstractServlet extends HttpServlet {
    protected Connection connection = null;
    protected TemplateEngine templateEngine;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
