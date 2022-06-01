package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.beans.Contact;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.dao.ContactDAO;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/account")
public class AccountPage extends AbstractServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // get and check parameter
        int accountID;
        try {
            accountID = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect value for id");
            return;
        }

        // check that accountID exists and that it is owned by the current user
        AccountDAO accountDAO = new AccountDAO(connection);
        Account account;
        try {
            account = accountDAO.getAccountFromID(accountID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (account == null || account.user() != user.id()) { // redirect to homepage if the account id doesn't exist or if it doesn't belong to the user
            String path = getServletContext().getContextPath() + "/home";
            response.sendRedirect(path);
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

        // set the context
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("user", user);
        ctx.setVariable("account", account);
        ctx.setVariable("transactions", accountTransactions);
        ctx.setVariable("contacts", contacts);
        ctx.setVariable("lastMonthTransactions", lastMonthTransactions);
        ctx.setVariable("lastYearTransactions", lastYearTransactions);
        ctx.setVariable("previousTransactions", previousTransactions);
        ctx.setVariable("localDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        String path = "/WEB-INF/templates/account.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}