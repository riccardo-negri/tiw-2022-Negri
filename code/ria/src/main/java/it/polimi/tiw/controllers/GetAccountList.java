package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AccountDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;

import it.polimi.tiw.controllers.AbstractServlet;

@WebServlet("/get-account-list")
public class GetAccountList extends AbstractServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        AccountDAO accountDAO = new AccountDAO(connection);
        List<Account> accounts;
        User user;
        try {
            user = (User) session.getAttribute("user");
            accounts = accountDAO.findAccountsWithLastActivity(user.id());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().println("Not possible to recover accounts");
            return;
        }

        String json = new Gson().toJson(accounts);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}