Local testing and deployment
============================

One Gradle build produces the same JAR for both environments. Spring profiles
select the configuration at runtime:
  src/main/resources/application-local.properties
  src/main/resources/application-deploy.properties

LOCAL TESTING (default when running from IntelliJ or Gradle)

Normal IntelliJ runs load the existing .env and use its working Railway PG*
connection, just as before the profile split. No Docker setup is required.
Migrations are disabled by default so startup does not change that schema.
Run Group04FlowerApiSpringBootApplication or ./gradlew bootRun.

OPTIONAL: SEPARATE LOCAL DATABASE

1. Start a separate local PostgreSQL database:
     docker compose --env-file .env.local.example up -d db
   It listens at localhost:5433 and stores data in the postgres_data volume.
   Flyway creates the tables when the local app starts for the first time.

2. Copy .env.local.example to .env.local to enable the isolated database.
   .env.local is git-ignored and uses Java properties syntax (no quoted values).
   Leave LOCAL_DB_HOST=localhost and LOCAL_DB_PORT=5433 for the database above.
   If you change database credentials or the host port, also update the db
   service in docker-compose.yml to match.
   Remove .env.local to return to the Railway connection in .env.

3. Run Group04FlowerApiSpringBootApplication in IntelliJ, or:
     ./gradlew bootRun --args='--spring.profiles.active=local'
   Clear any old deployment environment variables from your IntelliJ run config.
   The default port is 8080; use PORT=8081 in .env.local if 8080 is occupied.

4. Check http://localhost:8080/api/v1/health and /api/v1/flowers.
   A new database contains no flowers until you add data.

To run both the app and database in Docker instead:
  docker compose --env-file .env.local.example up --build -d
The API container explicitly selects local and connects to the db service.
For real Google sign-in, use a valid GOOGLE_WEB_CLIENT_ID in a git-ignored
local env file and pass that file with --env-file when starting Compose.

Authentication remains enabled in both environments. Public flower GETs and
health checks need no token; protected endpoints still need a Google ID token.
The local placeholder client ID is only suitable for testing public routes
and tests that mock JWTs.

DEPLOYMENT (Railway or another host)

Build once:
  ./gradlew bootJar

Set the following in the host's environment/secret settings:
  SPRING_PROFILES_ACTIVE=deploy
  PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD
  GOOGLE_WEB_CLIENT_ID
  PORT (optional; defaults to 8080)

The Docker image selects deploy by default. For a JAR deployment, set the
profile as above or pass --spring.profiles.active=deploy to java -jar.
Use .env.example as a variable reference; it contains no real credentials.
The deploy profile requires PostgreSQL TLS and has no credential defaults.
It does not import .env or .env.local. The local profile does load .env.
DATABASE_URL is not used by this JDBC setup;
PGPASSWORD is the authoritative database password.

DEPLOY_MIGRATIONS_ENABLED defaults to false for the existing Railway schema.
For a new empty deployment database, explicitly set it to true to initialize
the schema. For an existing database without Flyway history, review and
baseline the schema separately before enabling migrations; do not enable
automatic baselining blindly.

Normal local runs use .env's PG* variables. Optional LOCAL_DB_* settings
override them when using a separate database. Enable LOCAL_MIGRATIONS_ENABLED
only for that isolated database. Do not select both local and deploy together.
Spring's explicit SPRING_DATASOURCE_* overrides still take precedence; remove
those overrides from local run configurations. Never commit .env/.env.local
or copy their secrets into application-*.properties. Both files are excluded
from the Docker build context.

RENDER

The render.yaml Blueprint describes the Docker web service. For the existing
group-04-flower-api-spring-boot service, apply the same settings in its dashboard
instead of creating a second service. Select this repository and the branch
containing the current code, Docker runtime, Dockerfile path ./Dockerfile,
and health check /api/v1/health. Remove any old Docker command override so the
Dockerfile entrypoint runs the JAR with the deploy profile.

Set SPRING_PROFILES_ACTIVE=deploy and the PG* variables listed above in Render's
Environment settings. Use database credentials reachable from Render, not
localhost or Docker's db hostname. GOOGLE_WEB_CLIENT_ID must equal the Android
app's default_web_client_id (the Google web OAuth client ID). PORT=8080 is
supported; Render forwards public HTTPS to the container's configured port.
Keep DEPLOY_MIGRATIONS_ENABLED=false for the existing database. For a new empty
database, enable it deliberately to create the schema before testing flowers.
Do not upload .env or put database passwords into render.yaml.

Commit and push these changes, then manually deploy that commit on the existing
Render service. Confirm the logs show the deploy profile and successful startup.
Open requests/deployment.http in IntelliJ and run its requests. Health and
flowers must return 200; auth-debug without a token returns 401; a blank sign-in
token returns 400. If public routes or the blank-token request return 401, verify
Render's deployed commit, branch, and environment overrides.

Android uses https://group-04-flower-api-spring-boot.onrender.com/ as its base URL.
Rebuild/install the app after changing this URL. It needs internet access, with
no USB connection or adb reverse. If Render is waking an idle service, wait for
the health request to succeed before testing sign-in.

POST /api/auth/google verifies a Google ID token; it returns subject, issuer,
audience, email and emailVerified. It does not issue a separate session token.
Android stores the verified Google ID token and uses it as a bearer token for
protected routes; expired tokens require a fresh Google sign-in. A failed API
request must be shown as a failed login, not accepted as an offline test user.
