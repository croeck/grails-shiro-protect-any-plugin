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

    // each (!) request that calls this method DOES require a valid authentication and permissions
    boolean accessControl(final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) {
        // Check that the user is authenticated.
        // Workaround for the security manager, see description: https://github.com/pledbrook/grails-shiro/issues/15
        def subject = new WebSubject.Builder(shiroSecurityManager, request, response).buildWebSubject()

        if (subject.principal == null || !subject.authenticated) {
            // Subject is not authorized:
            // By default redirect to the login page and build the target URI from the request's forwardURI
            def targetURI = new StringBuilder(request.forwardURI[request.contextPath.size()..-1])
            def query = request.queryString
            if (query) {
                if (!query.startsWith('?')) {
                    targetURI << '?'
                }
                targetURI << query
            }
            log.trace("Subject not authorized, redirect to '$targetURI'")

            // TODO as of now only the default 'onNotAuthenticated' action is supported
            // use the default shiro redirect URL
            def redirectURI = grailsApplication.config.security?.shiro?.redirect?.uri
            if (redirectURI) {
                response.sendRedirect(redirectURI + "?targetUri=${targetURI.encodeAsURL()}")
            } else {
                response.sendRedirect(
                        grailsLinkGenerator.link(
                            controller: "auth",
                            action: "login",
                            params: [targetUri: targetURI.toString()]))
            }
            return false
        }

        // the subject is authorized, now check the permissions...
        def forwardURIWithoutContext = request.forwardURI.replaceFirst(request.contextPath, "")

        def controllerName = grailsApplication.mainContext.grailsUrlMappingsHolder.match(forwardURIWithoutContext).params.controller
        def actionName = grailsApplication.mainContext.grailsUrlMappingsHolder.match(forwardURIWithoutContext).params.action
        def id = grailsApplication.mainContext.grailsUrlMappingsHolder.match(forwardURIWithoutContext).params.id

        // Check that the user has the required permission for the target controller/action.
        def permString = new StringBuilder()
        permString << controllerName

        if(actionName) {
            // only add an action if there is one, do NOT assume an 'index' action
            permString << ":$actionName"
        }
        if(id) {
            // Add the ID if it's in the web parameters.
            permString << ":$id"
        }

        if (!subject.isPermitted(permString.toString())) {
            log.trace("Subject is authorized, but permissions to '$permString' were NOT found!")
            // TODO as of now only the default 'onNotAuthenticated' action is supported
            // By default, redirect to the 'unauthorized' page.
            response.sendRedirect(grailsLinkGenerator.link(controller: "auth", action: "unauthorized"))
            return false
        } else {
            log.trace("Subject is authorized and permissions to '$permString' were found!")
            return true
        }
    }
}
