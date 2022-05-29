package it.polimi.tiw.controllers;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.Serial;
import java.sql.Connection;

public abstract class AbstractServlet extends HttpServlet {
    @Serial
    protected static final long serialVersionUID = 1L;
    protected Connection connection = null;
    protected TemplateEngine templateEngine;

    public void init() throws ServletException {
        connection = it.polimi.tiw.utils.ConnectionHandler.getConnection(getServletContext());

        //thymeleaf
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }
}
