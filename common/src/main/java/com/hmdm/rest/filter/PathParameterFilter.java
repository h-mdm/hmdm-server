package com.hmdm.rest.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PathParameterFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // Path parameters (';' or '%3B') are rejected: Guice matches its filters against the raw URI,
        // while Tomcat and Jersey strip them, so /rest/private;x/update would skip the
        // filters bound to /rest/private/* (authentication!) and still reach /private/update.
        if (requestURI.indexOf(';') != -1 || requestURI.toUpperCase().contains("%3B")) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
