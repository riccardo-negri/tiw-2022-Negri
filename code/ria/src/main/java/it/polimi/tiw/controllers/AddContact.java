package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.dao.AccountDAO;
import it.polimi.tiw.dao.ContactDAO;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/add-contact")
public class AddContact extends AbstractServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (it.polimi.tiw.beans.User) session.getAttribute("user");

        // get parameters
        String accountToAdd = request.getParameter("id");

        // check existence fo every parameter, I don't care here if the accountIdToGoBack is valid because it will be handled on the Account page
        if (!it.polimi.tiw.utils.ParameterValidator.validate(accountToAdd)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Required parameters missing");
            return;
        }

        // check if account to add is a number
        int accountToAddID;
        try {
            accountToAddID = Integer.parseInt(accountToAdd);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Required parameters not valid");
            return;
        }

        // check if account to add exists
        AccountDAO accountDAO = new AccountDAO(connection);
        Account account;
        try {
            account = accountDAO.getAccountFromID(accountToAddID);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve transaction information");
            return;
        }
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Account to add does not exist");
            return;
        }

        // check if the accountToAdd is not already in the contacts
        ContactDAO contactDAO = new ContactDAO(connection);
        boolean isAlreadyInTheContacts;
        try {
            isAlreadyInTheContacts = contactDAO.isAccountInContacts(user.id(), accountToAddID);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to retrieve contact information");
            return;
        }
        if (isAlreadyInTheContacts) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The account is already in the contacts");
            return;
        }

        // add the contact
        try {
            contactDAO.addContact(user.id(), Integer.parseInt(accountToAdd));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // everything went well
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

}
