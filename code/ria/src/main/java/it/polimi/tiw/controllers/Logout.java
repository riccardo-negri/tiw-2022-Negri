package it.polimi.tiw.controllers;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/logout")
public class Logout extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        // invalidate session if present
        if (session != null) {
            session.invalidate();
        }

        String path = getServletContext().getContextPath();
        response.sendRedirect(path);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
