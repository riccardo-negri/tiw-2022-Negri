package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.beans.Contact;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.dao.ContactDAO;

import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/get-account")
public class GetAccount extends AbstractServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // get and check parameter
        int accountID;
        try {
            accountID = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect value for accountID");
            return;
        }

        // check that accountID exists and that it is owned by the current user
        AccountDAO accountDAO = new AccountDAO(connection);
        Account account;
        try {
            account = accountDAO.getAccountFromID(accountID);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve account information");
            return;
        }
        if (account == null || account.user() != user.id()) { // the account id doesn't exist or if it doesn't belong to the user
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("You don't have the rights to access this account");
            return;
        }

        // get transactions related to the account and contacts of the user
        List<Transaction> accountTransactions;
        List<Contact> contacts;
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        ContactDAO contactDAO = new ContactDAO(connection);
        try {
            accountTransactions = transactionDAO.findTransactions(accountID);
            contacts = contactDAO.findContactsFromUserID(user.id());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // split the transactions based on time frames
        List<Transaction> lastMonthTransactions = new ArrayList<>();
        List<Transaction> lastYearTransactions = new ArrayList<>();
        List<Transaction> previousTransactions = new ArrayList<>();
        for (Transaction t : accountTransactions) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LocalDate date;
            try {
                String timestamp = t.timestamp().toString();
                date = df.parse(timestamp.substring(0, timestamp.length() - 2)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if (date.isAfter(LocalDate.now().minusMonths(1))) {
                lastMonthTransactions.add(t);
            } else if (date.isAfter(LocalDate.now().minusMonths(12))) {
                lastYearTransactions.add(t);
            } else {
                previousTransactions.add(t);
            }
        }

        // TODO check support for surnames like ...è
        String json = new Gson().toJson(Arrays.asList(account, contacts, lastMonthTransactions, lastYearTransactions, previousTransactions));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}