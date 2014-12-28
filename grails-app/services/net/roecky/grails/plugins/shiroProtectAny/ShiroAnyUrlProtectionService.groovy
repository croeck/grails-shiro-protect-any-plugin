/*
 * Copyright 2014 Cedric Röck.
 *
 * Copyright of the original Apache Shiro code:
 * 2007 Peter Ledbrook
 * 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
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
 *
 */
package net.roecky.grails.plugins.shiroProtectAny

import org.apache.shiro.web.subject.WebSubject
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class ShiroAnyUrlProtectionService implements ShiroAnyUrlProtection {

    def grailsApplication
    def shiroSecurityManager
    LinkGenerator grailsLinkGenerator

    boolean accessControl(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        // If required, check that the user is authenticated.
        // Workaround, as described in https://github.com/pledbrook/grails-shiro/issues/15
        def subject = new WebSubject.Builder(shiroSecurityManager, request, response).buildWebSubject()

        if (subject.principal == null || !subject.authenticated) {
            // Default behaviour is to redirect to the login page.
            // We start by building the target URI from the request's
            // 'forwardURI', which is the URL specified by the
            // browser.
            def targetUri = new StringBuilder(request.forwardURI[request.contextPath.size()..-1])
            def query = request.queryString
            if (query) {
                if (!query.startsWith('?')) {
                    targetUri << '?'
                }
                targetUri << query
            }

            def redirectUri = grailsApplication.config?.security?.shiro?.redirect?.uri
            if (redirectUri) {
                response.sendRedirect(redirectUri + "?targetUri=${targetUri.encodeAsURL()}")
            } else {
                response.sendRedirect(
                        grailsLinkGenerator.link(
                            controller: "auth",
                            action: "login",
                            params: [targetUri: targetUri.toString()]))
            }
            return false
        }

        def controllerName = grailsApplication.mainContext.grailsUrlMappingsHolder.match(request.forwardURI).params.controller
        def actionName = grailsApplication.mainContext.grailsUrlMappingsHolder.match(request.forwardURI).params.action
        def id = grailsApplication.mainContext.grailsUrlMappingsHolder.match(request.forwardURI).params.id

        // Check that the user has the required permission for the target controller/action.
        def permString = new StringBuilder()
        permString << controllerName << ':' << (actionName ?: "index")

        // Add the ID if it's in the web parameters.
        if (id) permString << ':' << id

        def isPermitted = subject.isPermitted(permString.toString())
        if (!isPermitted) {
            // Default behaviour is to redirect to the 'unauthorized' page.
            response.sendRedirect(grailsLinkGenerator.link(controller: "auth", action: "unauthorized"))
            return false
        } else {
            return true
        }
    }
}
