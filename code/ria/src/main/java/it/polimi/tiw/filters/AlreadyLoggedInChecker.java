package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.filters.AbstractFilter;

public class AlreadyLoggedInChecker extends AbstractFilter {

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        // check if the user is already logged in, if so redirect to the home page
        if (!(session.isNew() || session.getAttribute("user") == null)) {
            res.sendRedirect("home");
            return;
        }

        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

}