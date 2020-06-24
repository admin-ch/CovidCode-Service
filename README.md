# HA-AuthCode-Generation-Service (CovidCode-Service)
HA-AuthCode-Generation-Service is an authorization code generation service for the CovidCode-UI and the proximity tracing app.

# Reproducible Builds
In order to have reproducible builds the io.github.zlika maven plugin is used. It replaces all timestamp with the timestamp of the last commit, and orders the entries in the JAR alphabetically. The github action then computes the sha256sum of the resulting JAR and adds the output as an build artifact.

# Developer Instructions

## Initial setup

Do this once:

1. Install a JDK (tested with Oracle JDK v11 and OpenjDK 1.8.0)
1. [Install Maven](https://maven.apache.org/install.html)
1. Install [Docker](https://docs.docker.com/get-docker/) and [docker-compose](https://docs.docker.com/compose/install/)
1. Check out [CovidCode-UI](https://github.com/admin-ch/CovidCode-UI) in another directory

## Development Cycle

Do this at the beginning of your session:
1. Run <pre>docker-compose up -d
docker-compose logs -f</pre> and wait for the logs to become quiescent
1. Run CovidCode-UI in another window (`ng serve`)

To run manual tests, you can run CovidCode-Service with the `local`
and `keycloak-local` Spring profiles using the following command:
```
mvn compile exec:java
```
(or the equivalent using your IDE's Maven functionality, if you
require access to a debugger)

To run the test suite:
```
mvn verify
```

To perform a clean build, and run the test suite with full code coverage
and upload the data to a locally-running SonarQube:
```
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify sonar:sonar
```
SonarQube results are thereafter visible at http://localhost:9000/

To tear down the development support environment (but retain its state on-disk):
```
docker-compose down
```

To wipe everything:
```
docker-compose down
docker volume rm covidcode_dbdata
mvn clean
```

## Swagger-UI
Swagger-UI is running on http://localhost:8113/swagger-ui.html.

## Local KeyCloak instance

If CovidCode-Service is being run as suggested above, it will perform
authentication and access control against an OIDC / OAuth server
running on http://localhost:8180/ (and so will CovidCode-UI in its
default development configuration).

The credentials for the KeyCloak administrator are visible in
docker-compose.yml in section `keycloak:`. Additionally, KeyCloak is
automatically pre-populated with a `bag-pts` realm, containing a
`doctor` account (password `doctor`) that enjoys access to both
CovidCode-UI and CovidCode-Service.

## PostgreSQL database

docker-compose runs a new PostgreSQL database on port 3113 and takes
care of setting it up. The superuser credentials are in
`docker-compose.yml`.

The "local" Spring profile should be used to run the application (see above).
The other profiles run the script afterMigrate to reassign the owner of the objects.

### Dockerfile
The docker file is provided only to run the application locally without DB. This configuration starts a PostgreSQL 11 on port 3113.  
Docker Official Image from https://hub.docker.com/_/postgres.

## JWT Generation
JWT generation uses a custom generator with library JJWT.

## Lombok
Project uses Lombok. Configure your IDE with lombok plugin.

## Security
The API is secured and a valid JWT should be provided. Note that these 2 values are needed  
- ctx:USER
- the audience must be set to ha-authcodegeneration
 
## Configuration
These parameters can be configured. You can find example values in application-local.yml.

The validity of the generated JWT:  
authcodegeneration.jwt.token-validity

The issuer to set in the generated JWT:  
authcodegeneration.jwt.issuer

The private key to sign the generated JWT:  
authcodegeneration.jwt.privateKey

The Prometheus actuator endpoint is secured with username and password:  
authcodegeneration.monitor.prometheus.user  
authcodegeneration.monitor.prometheus.password

The allowed origin configuration for the authcode generation:  
ha-authcode-generation-service.allowed-origin
