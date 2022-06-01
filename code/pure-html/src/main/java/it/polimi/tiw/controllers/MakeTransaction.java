package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.dao.TransactionDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/make-transaction")
public class MakeTransaction extends AbstractServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request,response);
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //TODO check that all these are safe
        HttpSession session = request.getSession();
        String originCode = request.getParameter("origin-code");
        String receiverUsername = request.getParameter("receiver-username");
        String destinationCode = request.getParameter("destination-code");
        String reason = request.getParameter("reason");
        int amount = Integer.parseInt(request.getParameter("amount"));

        //TODO check that the sender has the money and that the receiver username owns the receiver account

        // get the account IDs
        AccountDAO accountDAO = new AccountDAO(connection);
        int origin;
        int destination;
        try {
            origin = accountDAO.getAccountFromCode(originCode).id();
            destination = accountDAO.getAccountFromCode(destinationCode).id();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // execute the transaction
        TransactionDAO transactionDAO = new TransactionDAO(connection);
        int transactionID;
        try {
            transactionID = transactionDAO.addTransaction(amount, reason, origin, destination);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Return view
        String path = getServletContext().getContextPath() + "/transaction-outcome" + "?id=" + transactionID;
        response.sendRedirect(path);
    }

}
