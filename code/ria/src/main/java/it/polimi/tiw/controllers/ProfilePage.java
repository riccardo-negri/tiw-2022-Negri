package it.polimi.tiw.controllers;

import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ParameterValidator;
import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/profile")
public class ProfilePage extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (it.polimi.tiw.beans.User) session.getAttribute("user");

        // get parameters
        String accountID = request.getParameter("id");
        String transactionID = request.getParameter("transaction");

        // check only if they exist, if they're wrong they'll be handled in the respective pages
        if (!ParameterValidator.validate(accountID) || !ParameterValidator.validate(transactionID)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameters missing");
            return;
        }

        // reassign them to make to set ctx correctly
        if ("null".equals(accountID)) {
            accountID = null;
        }
        if ("null".equals(transactionID)) {
            transactionID = null;
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        ctx.setVariable("accountID", accountID);
        ctx.setVariable("transactionID", transactionID);
        ctx.setVariable("user", user);

        String path = "/WEB-INF/templates/profile.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
