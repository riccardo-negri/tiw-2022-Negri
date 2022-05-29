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

@WebServlet("/transaction-outcome")
public class Outcome extends AbstractServlet {

    /*
     *  with attribute "id=X" if the transaction went well
     *  with attribute "failed=X" if the requirements where not fulfilled, X represents the reason of failure
     *  with attribute "added-contact=true" if the contact was successfully added
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String id = (String) request.getParameter("id");
        String failed = (String) request.getParameter("failed");
        String addedContact = (String) request.getParameter("added-contact");

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
            try {
                ctx.setVariable("transaction", transactionDAO.getTransactionFromID(transactionID));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            //
        } else if (failed != null && !failed.equals("")) { // enter here if the transaction was NOT successful
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
