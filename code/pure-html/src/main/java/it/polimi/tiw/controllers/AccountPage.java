package it.polimi.tiw.controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.beans.Account;
import it.polimi.tiw.beans.Transaction;
import it.polimi.tiw.dao.TransactionDAO;
import it.polimi.tiw.dao.AccountDAO;

@WebServlet("/account")
public class AccountPage extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());

        //thymeleaf
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // get and check params
        Integer accountID = null;
        try {
            accountID = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
            return;
        }


        TransactionDAO transactionDAO = new TransactionDAO(connection);
        AccountDAO accountDAO = new AccountDAO(connection);
        Account account;
        List<Transaction> userTransactions;
        try {
            userTransactions = transactionDAO.findTransactions(accountID);
            account = accountDAO.getAccountFromID(accountID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("name", user.name());
        ctx.setVariable("surname", user.surname());
        ctx.setVariable("user", user);
        ctx.setVariable("transactions", userTransactions);
        List<Transaction> lastMonthTransactions = new ArrayList<>();
        List<Transaction> lastYearTransactions = new ArrayList<>();
        List<Transaction> previousTransactions = new ArrayList<>();
        for (Transaction t : userTransactions) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LocalDate date;
            try {
                String timestamp = t.timestamp().toString();
                date = df.parse(timestamp.substring(0, timestamp.length()-2)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if(date.isAfter(LocalDate.now().minusMonths(1))) {
                lastMonthTransactions.add(t);
            } else if (date.isAfter(LocalDate.now().minusMonths(12))) {
                lastYearTransactions.add(t);
            }
            else {
                previousTransactions.add(t);
            }
        }
        ctx.setVariable("lastMonthTransactions", lastMonthTransactions);
        ctx.setVariable("lastYearTransactions", lastYearTransactions);
        ctx.setVariable("previousTransactions", previousTransactions);
        ctx.setVariable("account", account);
        ctx.setVariable("localDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        String path = "/templates/account.html";
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}