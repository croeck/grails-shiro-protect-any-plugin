Shiro Protect Any - Grails Plugin
===

This grails plugin allows to require authentication for any non-controller URLs.

It was designed with the purpose of forcing authentication for the */monitoring* URL of the [JavaMelody Grails Plugin](http://grails.org/plugin/grails-melody).

**The plugin is still untested and does not cover the full functionality of the Apache Shiro authentication process!**

Getting Started
---
By default, the plugin does not apply any additional filter and therefore remains inactive.
To activate the plugin, at least one URL must be provided within your _Config_.

**Configuration example:**  
_Require authentication for the JavaMelody page_

	security {
		shiro {
			// default shiro configuration
			redirectUrl = "/auth/unauthorized"
			
			// now the actual plugin configuration
			shiroAnyProtector {
		    	// specify all non-controller URLs that shall require authentication
    			urls = ["/monitoring"]
			}
		}
	}


TODOs
---
* Exclude all controller URLs to prevent duplicate filtering
* Utilize the 'onNotAuthenticated' action of custom filters (if defined)
* Add basic tests

Contribution Guide
---
Fork & Send Pull Request

License
---

This plugin is licensed under the terms of the [Apache License, Version 2.0][Apache License, Version 2.0].
[Apache License, Version 2.0]: http://www.apache.org/licenses/LICENSE-2.0.html