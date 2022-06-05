package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.User;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/error")
public class ErrorPage extends AbstractServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        // check if the user is already logged in, if not redirect to the login page
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect("login");
            return;
        }

        User user = (User) session.getAttribute("user");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        Class exceptionClass = (Class) request.getAttribute("javax.servlet.error.exception_type");
        Integer code = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        String servletName = (String) request.getAttribute("javax.servlet.error.servlet_name");

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        ctx.setVariable("user", user);
        ctx.setVariable("exception", exception);
        ctx.setVariable("exceptionClass", exceptionClass);
        ctx.setVariable("code", code);
        ctx.setVariable("errorMessage", errorMessage);
        ctx.setVariable("requestUri", requestUri);
        ctx.setVariable("servletName", servletName);

        String path = "/WEB-INF/templates/error.html";
        templateEngine.process(path, ctx, response.getWriter());

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }

}