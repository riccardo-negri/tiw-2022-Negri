package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/home")
public class HomePage extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }

        AccountDAO accountDAO = new AccountDAO(connection);
        List<Account> accounts = new ArrayList<>();
        User user;
        try {
            user = (User) session.getAttribute("user");
            accounts = accountDAO.findAccountsWithLastActivity(user.id());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover accounts");
            return;
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("accounts", accounts);
        ctx.setVariable("name", user.name());
        ctx.setVariable("surname", user.surname());
        String path = "/WEB-INF/templates/home.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}