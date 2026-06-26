#!/usr/bin/env bash
set -euo pipefail

DB_NAME="${DB_NAME:-live_borrow_sample}"
DB_USERNAME="${DB_USERNAME:-ycf}"
DB_PASSWORD="${DB_PASSWORD:-ycf012!}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"

mysql -uroot -p < sql/local_mysql_setup.sql
mvn -q org.flywaydb:flyway-maven-plugin:migrate \
  -Dflyway.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false" \
  -Dflyway.user="${DB_USERNAME}" \
  -Dflyway.password="${DB_PASSWORD}" \
  -Dflyway.locations="classpath:db/migration"
