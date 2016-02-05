# OpenConext-metadata-exporter

[![Build Status](https://travis-ci.org/oharsta/OpenConext-metadata-exporter.svg)](https://travis-ci.org/oharsta/OpenConext-metadata-exporter)
[![codecov.io](https://codecov.io/github/oharsta/OpenConext-metadata-exporter/coverage.svg)](https://codecov.io/github/oharsta/OpenConext-metadata-exporter)

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
