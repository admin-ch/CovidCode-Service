# HA-AuthCode-Generation-Service (CovidCode-Service)
HA-AuthCode-Generation-Service is an authorization code generation service for the CovidCode-UI and the proximity tracing app.

## Swagger-UI
Swagger-UI is running on http://localhost:8113/swagger-ui.html.

## PostgreSQL database
To start up the application locally, run a new PostgreSQL 11+ database on port 3113. Use the profile "local" to run the application.
The other profiles run the script afterMigrate to reassign the owner of the objects.

## JWT Generation
- JWT generation uses a custom generator with library JJWT.

## Lombok
Project uses Lombok. Configure your IDE with lombok plugin.
 
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
