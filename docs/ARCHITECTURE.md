# Technology stack

Back-end is written in Java 8+ and is running on Tomcat 9. It uses PostgreSQL database.

Front-end uses JavaScript/AngularJS.

The project is build via Maven.

# Project structure

| Directory | Purpose |
|---|---|
| common | Core files (common models, persistence, abstractions, utilities) |
| jwt | JWT-based access (for REST API) |
| notification | Push notifications (MQTT, HTTP polling) |
| plugins | Plugins (each subdirectory is a separate plugin) |
| plugins/platform | Plugin platform (a code for managing and connecting plugins) |
| server | Main module |
| swagger | Swagger-based UI documentation |

# Source files

In each component:

| File | Purpose |
|---|---|
| pom.xml | Maven build file |
| src/main | Source files for the project |
| src/main/java | Java back-end |
| guice | Guice modules |
| persistence | Mybatis-based persistence layer (domain, mapper, DAO) |
| rest/json | DTO JSON |
| rest/...Resource.java | REST API based on javax.ws.rs |
| src/test/java | Unit tests |
| src/main/resources/liquibase/...changelog.xml | Database migration |
| src/main/webapp | Front-end JavaScript application |

Other files, in particular the contents of the target directory, should not be considered.

# Plugin architecture

## General structure

| File | Purpose |
|---|---|
| pom.xml | Maven build file for a plugin |
| src/main/java | Java-based back-end |
| src/main/resources/liquibase/\*changelog.xml | Migration file for this plugin |
| src/main/webapp | JavaScript-based front-end |

## Java back-end structure

| File | Purpose |
|---|---|
| guice/module | Guice initialization modules (getting config from ROOT.xml, liquibase, persistence, REST, task) |
| persistence/domain | Domain (models) |
| persistence/mapper | Mappers of Java to SQL query |
| persistence/\*DAO.java | DAO access layer |
| rest/filter | Filtering methods |
| rest/json | DTO |
| rest/\*Resource.java | REST API |
| \*PluginConfigurationImpl.java | Main plugin module |

## JavaScript front-end structure

| File | Purpose |
|---|---|
| \*.module.js | Main JS module for the plugin |
| i18n | Translation strings |
| views | Views for settings, functions, and modals |


                                                           

