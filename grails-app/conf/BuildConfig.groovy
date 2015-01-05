grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'debug'

    repositories {
        grailsCentral()
    }

    plugins {
        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }

        compile (':shiro:1.2.1') {
            excludes([name: 'quartz', group: 'org.opensymphony.quartz'])
        }
    }
}
