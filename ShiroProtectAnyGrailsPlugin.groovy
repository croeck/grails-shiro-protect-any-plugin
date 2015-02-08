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
 */

import grails.plugin.webxml.FilterManager
import net.roecky.grails.plugins.shiroProtectAny.filters.ShiroAnyUrlProtectionFilter

class ShiroProtectAnyGrailsPlugin {

    def version = "0.1.0"
    def grailsVersion = "2.2 > *"
    def loadAfter = ["shiro"]
    def title = "Shiro Protect Any Plugin"
    def author = "Cedric Röck"
    def authorEmail = "cedric.roeck@gmail.com"
    def description = 'Allows to setup authentication for static resource calls against Apache Shiro.'
    def license = "APACHE"
    def issueManagement = [ system: "GitHub", url: "https://github.com/croeck/grails-shiro-protect-any-plugin/issues" ]
    def scm = [ url: "https://github.com/croeck/grails-shiro-protect-any-plugin" ]

    def doWithWebDescriptor = { webXml ->
        println '\nConfiguring Shiro Protect Any Plugin filters ...'

        // URLs that shall require authentication
        def urls = []

        application.config.security?.shiro?.shiroAnyProtector?.urls?.each { url ->
            if(url.startsWith("/*") || url.startsWith("*")) {
                log.warn("Not allowed to add wildcard Shiro Protect Any Filter '$url' to prevent duplicate authentication filters")
            } else {
                urls << url
            }
        }

        log.info("Applying Shiro Protect Any Filter to $urls")

        // do not apply any filter if no URL has been specified
        if(urls) {
            // filter specific URLs (if specified) or fallback and filter each request
            def shiroAnyUrlProtectionFilter = [
                    name:'ShiroAnyUrlProtectionFilter',
                    filterClass:ShiroAnyUrlProtectionFilter.name,
                    urlPatterns:urls
            ]
            def filtersToAdd = [shiroAnyUrlProtectionFilter]
            log.debug("Collected ${filtersToAdd.size()} filters to add within the ShiroProtectAnyGrailsPlugin")

            def filters = webXml.filter[0]
            filters + {
                filtersToAdd.each { f ->
                    log.info "Adding filter: ${f.name} with class ${f.filterClass} and init-params: ${f.params}"
                    filter {
                        'filter-name'(f.name)
                        'filter-class'(f.filterClass)
                        f.params?.each { k, v ->
                            'init-param' {
                                'param-name'(k)
                                'param-value'(v.toString())
                            }
                        }
                    }
                }
            }
            // and finally also add the filter mappings
            def mappings = webXml.'filter-mapping'[0]
            mappings + {
                filtersToAdd.each { f ->
                    f.urlPatterns?.each { p ->
                        log.info "Adding url pattern ${p} for filter ${f.name}"
                        'filter-mapping' {
                            'filter-name'(f.name)
                            'url-pattern'(p)
                            f.dispatchers.each { dispatcher it }
                        }
                    }
                }
            }
        }
    }

    // make sure the filter fires before the other filters, e.g. the javamelody plugin filter
    def getWebXmlFilterOrder() {
        [ ShiroAnyUrlProtectionFilter: FilterManager.DEFAULT_POSITION - 500 ]
    }
}
