/*
 * Copyright 2014 Cedric Röck.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.roecky.grails.plugins.shiroProtectAny.filters;

import net.roecky.grails.plugins.shiroProtectAny.ShiroAnyUrlProtection;
import net.roecky.grails.plugins.shiroProtectAny.ShiroAnyUrlProtectionService;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.context.ServletContextHolder;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.springframework.context.ApplicationContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link ShiroAnyUrlProtectionFilter} ...
 */
public class ShiroAnyUrlProtectionFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ShiroAnyUrlProtectionFilter.class);

    // grails service that asserts the authentication
    private ShiroAnyUrlProtection shiroAnyUrlProtection;

    public void init(final FilterConfig filterConfig) throws ServletException {
        // resolve the service bean
        ApplicationContext context = (ApplicationContext)ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
        this.shiroAnyUrlProtection = (ShiroAnyUrlProtectionService) context.getBean("shiroAnyUrlProtectionService");
    }

    public void destroy() {
        // nothing required to be done here
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Invoked filter for '" + req.getRequestURL());
        }

        // each (!) filtered call requires a valid authentication and permissions
        if(shiroAnyUrlProtection.accessControl(req, res, req.getSession())) {
            // found valid permission, continue the request
            chain.doFilter (request, response);
        }
    }

}
