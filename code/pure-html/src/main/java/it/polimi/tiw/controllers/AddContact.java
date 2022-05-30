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

import it.polimi.tiw.dao.ContactDAO;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.beans.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/add-contact")
public class AddContact extends AbstractServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request,response);
    }


    /*
    *   Parameters:
    *   - account (ID of the account to add to the contact list of the user)
    *   - origin (Account ID for the page to go back to)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (it.polimi.tiw.beans.User) session.getAttribute("user");

        //TODO check that all these are safe
        String account = request.getParameter("account");
        String origin = request.getParameter("origin");

        //TODO check that the sender has the money and that the receiver username owns the receiver account

        // execute
        ContactDAO contactDAO = new ContactDAO(connection);
        try {
            contactDAO.addContact(user.id(), Integer.parseInt(account));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Return view
        String path = getServletContext().getContextPath() + "/account?id=" + origin;
        response.sendRedirect(path);
    }

}
