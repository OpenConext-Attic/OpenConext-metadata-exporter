# OpenConext-metadata-exporter

[![Build Status](https://travis-ci.org/OpenConext/OpenConext-metadata-exporter.svg)](https://travis-ci.org/OpenConext/OpenConext-metadata-exporter)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-metadata-exporter/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-metadata-exporter)

OpenConext Metadata Exporter

## [Getting started](#getting-started)

### [System Requirements](#system-requirements)

- Java 8
- Maven 3
- MySQL 5.5

### [Create database](#create-database)

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE srlocal DEFAULT CHARACTER SET latin1;
grant all on srlocal.* to 'root'@'localhost';
```

## [Building and running](#building-and-running)

### [The metadata-exporter](#metadata-exporter)

This project uses Spring Boot and Maven. To run locally, type:

```bash
mvn spring-boot:run
```

When developing, it's convenient to just execute the applications main-method, which is in [Application](src/main/java/me/Application.java).

## [Miscellaneous](#miscellaneous)

### [cUrl](#curl)

The secured endpoints can be accessed at:

```bash
curl -i -H "Content-Type: application/json" --user metadata.client:secret http://localhost:8080/identity-providers.json
curl -i -H "Content-Type: application/json" --user metadata.client:secret http://localhost:8080/service-providers.json

curl -i -I -H "Content-Type: application/json" --user metadata.client:secret http://localhost:8080/identity-providers.json
curl -i -I -H "Content-Type: application/json" --user metadata.client:secret http://localhost:8080/service-providers.json

curl -i -I -H "Content-Type: application/json" -H "If-Modified-Since: Sat, 25 Feb 2017 09:14:11 GMT" --user metadata.client:secret http://localhost:8080/identity-providers.json
curl -i -I -H "Content-Type: application/json" -H "If-Modified-Since: Sat, 25 Feb 2017 09:14:11 GMT" --user metadata.client:secret http://localhost:8080/service-providers.json
```
