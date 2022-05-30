package it.polimi.tiw.controllers;

import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.ContactDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/profile")
public class ProfilePage extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (it.polimi.tiw.beans.User) session.getAttribute("user");

        //TODO check that all these are safe
        String accountID = request.getParameter("id");
        String transactionID = request.getParameter("transaction");

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/templates/profile.html";
        if ("null".equals(accountID)) {
            accountID = null;
        }
        if ("null".equals(transactionID)) {
            transactionID = null;
        }
        ctx.setVariable("accountID", accountID);
        ctx.setVariable("transactionID", transactionID);
        ctx.setVariable("user", user);
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
