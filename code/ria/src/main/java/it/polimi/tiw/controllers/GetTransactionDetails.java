package it.polimi.tiw.controllers;

import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.ContactDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.utils.ParameterValidator;

import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/get-transaction-details")
public class GetTransactionDetails extends AbstractServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String id = request.getParameter("id");

        if (!ParameterValidator.validate(id)) { // enter here if the transaction was successful
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Required parameters missing");
            return;
        }

        // check if ID is a valid number
        int transactionID;
        try {
            transactionID = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        // check if the transaction exists and if it is owned by the session user
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        Transaction transaction;
        try {
            transaction = transactionDAO.getTransactionFromID(transactionID);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve transaction information");
            return;
        }
        if (transaction == null || transaction.sender().id() != user.id()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Transaction not existent or executed by another user");
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
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve account information");
            return;
        }

        String json = new Gson().toJson(Arrays.asList(transaction, isAccountInContacts, accountOrigin, accountDestination));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
