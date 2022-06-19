package it.polimi.tiw.filters;

import org.thymeleaf.TemplateEngine;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

public abstract class AbstractFilter implements Filter {
    protected TemplateEngine templateEngine;

    public void init(FilterConfig filterConfig) {
    }
}
