# HA-AuthCode-Generation-Service (CovidCode-Service)
HA-AuthCode-Generation-Service is an authorization code generation service for the CovidCode-UI and the proximity tracing app.

## Swagger-UI
http://localhost:8113/swagger-ui.html

## PostgreSQL database
To start up the application locally, run a new PostgreSQL 11+ database on port 3113.

## JWT Generation
- Use Profile "keycloak-token-provider" to generate a jwt with keycloak (it requires keycloak)
- Default generation uses a custom generator with library JJWT

## Lombok
 Project uses Lombok. Configure your IDE with lombok plugin.
