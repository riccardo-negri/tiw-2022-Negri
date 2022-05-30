package it.polimi.tiw.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.controllers.AbstractServlet;

import java.io.IOException;
import java.sql.SQLException;

import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.ContactDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.beans.Account;

@WebServlet("/transaction-outcome")
public class Outcome extends AbstractServlet {

    /*
     *  with attribute "id=X" if the transaction went well
     *  with attribute "failed=X&origin=Y" if the requirements where not fulfilled, X represents the reason of failure and Y the account ID where we come from
     *  with attribute "added-contact=true" if the contact was successfully added
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String id = (String) request.getParameter("id");
        String failed = (String) request.getParameter("failed");
        String origin = request.getParameter("origin");

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/templates/transaction-outcome.html";

        if (id != null && !id.equals("")) { // enter here if the transaction was successful
            if (failed != null && !failed.equals("")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Got id and failed parameters at the same time");
                return;
            }

            int transactionID;
            try {
                transactionID = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
                return;
            }

            TransactionDAO transactionDAO = new TransactionDAO(connection);
            Transaction transaction;
            try {
                transaction = transactionDAO.getTransactionFromID(transactionID);
                ctx.setVariable("transaction", transaction);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            User user = (it.polimi.tiw.beans.User) session.getAttribute("user");
            ContactDAO contactDAO = new ContactDAO(connection);
            AccountDAO accountDAO = new AccountDAO(connection);
            Account accountDestination;
            Account accountOrigin;
            try {
                accountDestination = accountDAO.getAccountFromCode(transaction.destination());
                accountOrigin = accountDAO.getAccountFromCode(transaction.origin());
                ctx.setVariable("addToContact", contactDAO.isAccountInContacts(user.id(), accountDestination.id()));
                ctx.setVariable("origin", accountOrigin);
                ctx.setVariable("destination", accountDestination);
                ctx.setVariable("user", user);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } else if (failed != null && !failed.equals("")) { // enter here if the transaction was NOT successful
            int originID;
            try {
                originID = Integer.parseInt(origin);
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
                return;
            }
            ctx.setVariable("account", originID);
            ctx.setVariable("failMessage", failed);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters missing");
        }

        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
