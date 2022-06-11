package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.utils.ParameterValidator;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/make-transaction")
public class MakeTransaction extends AbstractServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().println("Operation not allowed");
        return;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        AccountDAO accountDAO = new AccountDAO(connection);
        UserDAO userDAO = new UserDAO(connection);

        // obtain every parameter
        String beneficiaryUsername = request.getParameter("beneficiary-username");
        String destinationCode = request.getParameter("destination-code");
        String reason = request.getParameter("reason");
        String rawAmount = request.getParameter("amount");
        String originCode = request.getParameter("origin-code");

        // check for origin parameter, so it can be used later in the redirect
        // check existence of origin account
        if (!ParameterValidator.validate(originCode)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing value for origin-code");
            return;
        }
        // check validity of origin account
        if (!(originCode.matches("\\d{12}"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Origin account not well formatted, this request was not legit");
            return;
        }
        // check that the origin account is owned by the session user
        Account originAccount;
        try {
            originAccount = accountDAO.getAccountFromCode(originCode);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve account information");
            return;
        }
        if (originAccount == null || originAccount.user() != user.id()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Origin account not allowed, this request was not legit");
            return;
        }

        // check existence of every parameter
        if (!ParameterValidator.validate(beneficiaryUsername)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing value for beneficiary-username");
            return;
        }
        if (!ParameterValidator.validate(destinationCode)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing value for destination-code");
            return;
        }
        if (!ParameterValidator.validate(reason)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing value for reason");
            return;
        }
        if (!ParameterValidator.validate(rawAmount)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing value for amount");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(rawAmount);
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Amount must be a number");
            return;
        }

        // strip the values
        beneficiaryUsername = beneficiaryUsername.strip();
        destinationCode = destinationCode.strip().replace(" ", "");
        reason = reason.strip();

        // check if the values are correctly formatted
        if (!beneficiaryUsername.matches("[a-zA-Z]+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Beneficiary name not correct.");
            return;
        }
        if (!(destinationCode.matches("\\d{12}") && destinationCode.length() == 12)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Beneficiary account not correct.");
            return;
        }

        // check that the reason is within the allowed length (255)
        if (reason.length() > 255) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Reason was too long.");
            return;
        }

        // check that the origin account has enough money to make the transaction
        if (originAccount.balance() < amount) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("You don't have enough money to execute the transaction.");
            return;
        }

        // check that the destination user exists
        User destinationUser;
        try {
            destinationUser = userDAO.getUserFromUsername(beneficiaryUsername);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve user information");
            return;
        }
        if (destinationUser == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Beneficiary username is not correct.");
            return;
        }

        // check that the destination account is owned by the user
        Account destinationAccount;
        try {
            destinationAccount = accountDAO.getAccountFromCode(destinationCode);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve account information");
            return;
        }
        if (destinationAccount == null || destinationAccount.user() != destinationUser.id()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Beneficiary account code is not correct, account does not exist or it's not owned by the beneficiary indicated.");
            return;
        }

        // check that sender and receiver account are not the same account
        if (originAccount.id() == destinationAccount.id()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("You can not send money to yourself.");
            return;
        }

        // execute the transaction
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        int transactionID;
        try {
            transactionID = transactionDAO.addTransaction(amount, reason, originAccount.id(), destinationAccount.id());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to insert the transaction in the database, retry later");
            return;
        }

        // everything went well
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(transactionID);
    }

}
