Local testing and a new Render deployment
========================================

One Gradle build produces the same JAR for both environments. Runtime profiles:
  src/main/resources/application-local.properties
  src/main/resources/application-deploy.properties

LOCAL TESTING

For an isolated database and API:
  docker compose --env-file .env.local.example up --build -d
  http://localhost:8080/api/v1/health
  http://localhost:8080/api/v1/flowers

For IntelliJ or Gradle, start only the database:
  docker compose --env-file .env.local.example up -d db
Copy .env.local.example to .env.local, then run the application or ./gradlew bootRun.
The database listens on localhost:5433. The API listens on port 8080.
The local profile imports .env and .env.local as Java properties (no quotes).
LOCAL_DB_* overrides select the isolated database; without them, PG* selects
an external database. Keep migrations disabled for an existing unmanaged schema.
A new database starts empty. The API returns JSON, with no website interface.

NEW RENDER WEB SERVICE

1. Choose the database before creating the web service.
   PostgreSQL must be reachable from Render and support TLS. Either provision a
   new empty PostgreSQL database or deliberately reuse your existing database.
   A new web service does not imply a new database. This Blueprint creates only
   the web service, not a database. Never use localhost or Docker's db hostname
   as PGHOST in Render. For Render Postgres, use the database host and credentials
   for the same region; for another provider, use its reachable/public endpoint.

2. Commit and push the reviewed changes to the repository branch to deploy.
   Create a NEW Blueprint from render.yaml, or create a Docker Web Service with
   Dockerfile path ./Dockerfile and Docker context . and copy these settings.
   The Blueprint service is named flower-api. Choose a unique name if occupied.
   Leave the Docker command override empty so the image entrypoint is used.

3. Set the following in Render Environment settings (not in Git or this chat):
     SPRING_PROFILES_ACTIVE=deploy
     PORT=8080
     PGHOST=<database host>
     PGPORT=<database port, usually 5432>
     PGDATABASE=<database name>
     PGUSER=<database user>
     PGPASSWORD=<database password>
     GOOGLE_WEB_CLIENT_ID=<Google OAuth WEB client ID>
     DEPLOY_MIGRATIONS_ENABLED=true OR false (see next step)
   render.yaml uses sync: false to request these values on Blueprint creation.
   Later Blueprint updates do not re-prompt; change values in the dashboard.
   .env.example is a reference only. The deploy profile does not import .env
   or .env.local, and Docker excludes them. DATABASE_URL, NODE_ENV and AUTH0_*
   are not used. Remove stale SPRING_DATASOURCE_* environment overrides because
   they can bypass the intended PG* settings. Do not combine deploy and local.

4. For a NEW EMPTY database set DEPLOY_MIGRATIONS_ENABLED=true. Flyway creates
   the schema and records V1 in flyway_schema_history. Leave it true on later
   deployments of that Flyway-managed database so future migrations can run.
   For an EXISTING database without Flyway history, use false until its schema
   has been reviewed and baselined separately. Do not enable automatic
   baselining or point migrations at an unreviewed shared database.
   Missing required deployment variables fail startup without logging secrets.

5. Use /api/v1/health as the Render health check. Successful startup should
   report the deploy profile, database connection, and (for a new database)
   a successful Flyway migration. Check the deployed commit/branch in Render.
   /api/v1/health must return 200 with database=connected.
   /actuator/health must return 200 with status=UP.
   /api/v1/flowers must return 200, initially with items=[].
   A database failure prevents the service from passing its health check.
   Connection pooling is limited to 5 connections per application instance.

6. Change the Android base URL to the NEW https://<service>.onrender.com/ URL
   and rebuild/install the app. Use HTTPS on Render and HTTP for local testing.
   Android emulator access to the host is http://10.0.2.2:8080/; a physical device
   needs a reachable host address or adb reverse. localhost on a phone refers
   to the phone itself. Configure Android cleartext HTTP only for local debug.

GOOGLE SIGN-IN AND API BEHAVIOR

GOOGLE_WEB_CLIENT_ID must match Android's default_web_client_id, the Google web
OAuth client ID used as the ID-token audience. Android also needs its correct
package name and signing certificate fingerprints configured in Google.
No Google client secret is required by this backend. Tokens must be fresh;
use real Google sign-in to verify signature, audience, issuer and expiry.

POST /api/auth/google with {"idToken":"<fresh Google ID token>"} verifies the
Google token and registers a non-admin database user on first sign-in using
its verified email and stable Google subject (stored as users.provider_id).
The response includes userId, subject, issuer, audience, email, emailVerified.
An existing different identity with the same email returns 409; accounts are
never linked by email alone. Existing legacy users need reviewed subject
mapping/account linking before they can sign in as those accounts.

The endpoint does not issue a separate session token. Android sends the same
Google ID token as Authorization: Bearer <token> on every protected request.
Expired tokens require a fresh Google sign-in. The API is stateless and does
not authenticate with cookies or X-User-Id. No CSRF token is needed.

GET /api/v1/users/me resolves the verified Google subject, ignoring X-User-Id.
It returns 404 if sign-in has not registered that identity yet.
Flower creation derives userId from the signed-in user. userId is now optional
in request bodies; an explicitly different userId returns 403. Flower updates,
deletes and tag changes require ownership; missing or unowned flowers return
404. Clients cannot transfer ownership by changing the request's userId.

Public: GET /, /api/v1/health, /actuator/health, /api/v1/flowers and
/api/v1/flowers/{id}. Public flower responses include notes and location;
keep that product behavior in mind before storing private information.
Protected: quotes, users/me, auth-debug, health subroutes, and all data writes.
Any authenticated user may add shared quotes. No admin-only moderation or
rate limiting is implemented. CORS is not configured for browser frontends;
the Android client does not need CORS. Revisit this if adding a web frontend.

VERIFICATION

  ./gradlew test bootJar

Tests use isolated settings and mocked Google JWTs, never your .env secrets.
Controller/security tests cover authentication, user-header spoofing, bearer
writes without CSRF, and ownership. To additionally run real PostgreSQL/TLS
migration, registration and ownership tests, use a DISPOSABLE EMPTY database:
  FLOWER_POSTGRES_TEST=true FLOWER_TEST_DB_HOST=127.0.0.1 \
    FLOWER_TEST_DB_PORT=5434 FLOWER_TEST_DB_NAME=flower_deploy_test \
    FLOWER_TEST_DB_USER=floweruser FLOWER_TEST_DB_PASSWORD=<password> \
    ./gradlew test --rerun-tasks
The database must support TLS. Never target a shared/production database here.
These tests run migrations and roll back their test data.

requests/deployment.http contains public and authenticated smoke checks.
Set baseUrl to the new Render URL. Define idToken in a LOCAL, git-ignored
http-client.private.env.json for protected checks. Never commit a real token.
Real Google sign-in and Render network/database reachability must still be
verified after deployment; mocked JWTs do not prove Google client setup.

HOSTING CONSIDERATIONS

The Blueprint currently selects a free web instance. Free Render web services
sleep after inactivity; account for startup delays in the Android app. Render
free Postgres databases expire, so choose an appropriate database plan and
backup strategy for persistent data. Review provider limits before deployment.
Docker builds use one Gradle worker and bounded memory; Java runtime reserves
headroom for native memory on small instances. If Docker Desktop is killed for
memory on your computer, inspect available RAM rather than changing database
credentials. Render build capacity must also be sufficient for Gradle.

Render references:
  https://render.com/docs/blueprint-spec#prompting-for-secret-values
  https://render.com/docs/free
