# judy-service
`judy` is a Spring Boot Java service designed as a backend for a web app.

## Building
Java 21 LTS is used with Gradle as the primary build tool. Spotless is applied during compile and checked at build time. Tests requiring a database use Testcontainers for PostgreSQL.

An `.env` file is used for sensitive values. `SUPERADMIN_PASSWORD_HASH` is injected by Spring from the environment; it should be a pre-computed BCrypt hash of the raw password the SuperAdmin account will use to authenticate. `spring-dotenv` allows the use of a `.env` file, which is not committed to the repo, to supply environment variables via a file.

* Test: `./gradlew test`
* Run: `./gradlew bootRun`
  * Note that Postgres must be running. A `docker-compose.yml` is provided with defaults that match the `application.yml`
* Build: `./gradlew clean build`
