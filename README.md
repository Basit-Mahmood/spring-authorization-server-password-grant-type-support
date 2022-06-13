There are three projects 

1) SpringAuthorizationServer
2) SpringResourceServer
3) SpringAuthorizationServerClient

The main classes are OAuth2ResourceOwnerPasswordAuthenticationConverter, OAuth2ResourceOwnerPasswordAuthenticationToken and 
OAuth2ResourceOwnerPasswordAuthenticationProvider. These classes are present in SpringAuthorizationServer in package oauth2.authentication. JwtUtils and OAuth2Utils are 
supported classes. Right now these classes are package protected in spring. That's why I just made a copy of those classes and use it. May be in future version of 
SpringAuthorizationServer these classes will make public so then there will be no need of these classes.

In SpringAuthorizationServerClient project. Class WebClientConfig has the configuration for password grant type support (contextAttributesMapper). Changes can be done according to need.

All are eclipse based gradle projects. All the settings are in application.properties file for all three projects. Like contextpath, port etc. SpringAuthorizationServer is 
using H2 database. 

These are the same projects offered by Spring. I just add the Password grant type in the AuthorizationServer and Client as Spring is not providing support 
for it becasue of OAuth2.1 draft.

This project is just showing how you can add custom grant type in the SpringAuthorizationServer. Like in my case I added password grant type support to use in my project. Changes
can be made according to need in the code. Right now it is using version 0.3.0 which is the latest version. Things can be change in upcoming versions of Spring authorization 
server. So if you update the version in future and have some problem then ask on the Spring forum. 

This project is just for demonstration purpose to add custom grant type.

All projects should be imported in eclipse fine. 

1) By default SpringAuthorizationServer will run on port 9000 with context path /springauthserver
2) SpringResourceServer will run on port 8090 with context path /springresourceserver
3) SpringAuthorizationServerClient will run on port 8080 with context path /springauthserverclient

The database scripts for SpringAuthorizationServer are present in database/scripts folder. The database settings are defined in application.proeprties.

The Urls are also configure in application.proeprties file

1) After running all three projects. Open the url http://127.0.0.1:8080/springauthserverclient   (This can be change if you change the properties file)
2) Already a user1 with password field is populated. Login with the user.
3) There will be three grant types. Client Credentials, Authorization Code and Password.
4) Click on password grant type and the result will come.
