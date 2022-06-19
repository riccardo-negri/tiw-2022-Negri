package it.polimi.tiw.controllers;

import org.thymeleaf.TemplateEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.tiw.utils.ConnectionHandler;

public abstract class AbstractServlet extends HttpServlet {
    protected Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
