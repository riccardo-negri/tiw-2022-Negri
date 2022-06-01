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
import it.polimi.tiw.utils.ParameterValidator;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/transaction-outcome")
public class TransactionOutcomePage extends AbstractServlet {

    /*
     *  with attribute "id=X" if the transaction went well
     *  with attribute "failed=X&origin=Y" if the requirements where not fulfilled, X represents the reason of failure and Y the account ID where we come from
     *  with attribute "added-contact=true" if the contact was successfully added
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String id = request.getParameter("id");
        String failed = request.getParameter("failed");
        String origin = request.getParameter("origin");

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        if (ParameterValidator.validate(id)) { // enter here if the transaction was successful
            if (ParameterValidator.validate(failed)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Got id and failed parameters at the same time");
                return;
            }

            // check if ID is a valid number
            int transactionID;
            try {
                transactionID = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
                return;
            }

            // check if the transaction exists and if it is owned by the session user
            TransactionDAO transactionDAO = new TransactionDAO(connection);
            Transaction transaction;
            try {
                transaction = transactionDAO.getTransactionFromID(transactionID);

            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Not possible to retrieve transaction information");
                return;
            }
            if (transaction == null || transaction.sender().id() != user.id()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Transaction not existent or executed by another user");
                return;
            }

            ContactDAO contactDAO = new ContactDAO(connection);
            AccountDAO accountDAO = new AccountDAO(connection);
            Account accountDestination;
            Account accountOrigin;
            boolean isAccountInContacts;
            try {
                accountDestination = accountDAO.getAccountFromCode(transaction.destination());
                accountOrigin = accountDAO.getAccountFromCode(transaction.origin());
                isAccountInContacts = contactDAO.isAccountInContacts(user.id(), accountDestination.id());
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Not possible to retrieve account information");
                return;
            }

            ctx.setVariable("transaction", transaction);
            ctx.setVariable("addToContact", isAccountInContacts);
            ctx.setVariable("origin", accountOrigin);
            ctx.setVariable("destination", accountDestination);
            ctx.setVariable("originID", accountOrigin.id());

        } else if (ParameterValidator.validate(failed)) { // enter here if the transaction was NOT successful
            // check if originID is a number
            int originID;
            try {
                originID = Integer.parseInt(origin);
            } catch (NumberFormatException | NullPointerException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect originID values");
                return;
            }
            ctx.setVariable("originID", originID);
            ctx.setVariable("failMessage", failed);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters missing");
            return;
        }

        ctx.setVariable("user", user);
        String path = "/WEB-INF/templates/transaction-outcome.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
