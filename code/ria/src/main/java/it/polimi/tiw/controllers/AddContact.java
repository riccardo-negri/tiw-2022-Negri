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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Parameters:
     * - account (ID of the account to add to the contact list of the user)
     * - origin (Account ID for the page to go back to)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (it.polimi.tiw.beans.User) session.getAttribute("user");

        // get parameters
        String accountIdToGoBackTo = request.getParameter("origin");
        String accountToAdd = request.getParameter("account");

        // check existence fo every parameter, I don't care here if the accountIdToGoBack is valid because it will be handled on the Account page
        if (!it.polimi.tiw.utils.ParameterValidator.validate(accountIdToGoBackTo) || !it.polimi.tiw.utils.ParameterValidator.validate(accountToAdd)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters missing");
            return;
        }

        // check if account to add is a number
        int accountToAddID;
        try {
            accountToAddID = Integer.parseInt(accountToAdd);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters not valid");
            return;
        }

        // check if account to add exists
        AccountDAO accountDAO = new AccountDAO(connection);
        Account account;
        try {
            account = accountDAO.getAccountFromID(accountToAddID);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Not possible to retrieve transaction information");
            return;
        }
        if (account == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Account to add does not exist");
            return;
        }

        // check if the accountToAdd is not already in the contacts
        ContactDAO contactDAO = new ContactDAO(connection);
        boolean isAlreadyInTheContacts;
        try {
            isAlreadyInTheContacts = contactDAO.isAccountInContacts(user.id(), accountToAddID);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Not possible to retrieve contact information");
            return;
        }
        if (isAlreadyInTheContacts) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The account is already in the contacts");
            return;
        }

        // add the contact
        try {
            contactDAO.addContact(user.id(), Integer.parseInt(accountToAdd));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // everything went well, can go back to the account page
        String path = getServletContext().getContextPath() + "/account?id=" + accountIdToGoBackTo;
        response.sendRedirect(path);
    }

}
