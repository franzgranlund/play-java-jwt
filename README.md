# JWT Authentication In Play Framework 2.6 Java

This project shows how to do JSON Web Token authentication in 
[Play Framework 2.6](https://www.playframework.com) using Java. It is meant as a starting point
when implementing JWT authentication in your own REST application.

JWT verification is done with the excellent library 
[Java JWT provided by Auth0](https://github.com/auth0/java-jwt) but can easily be used with other 
libraries such as [jose.4.j](https://bitbucket.org/b_c/jose4j/wiki/Home).

The project uses the `Authentication: Bearer` authentication method.

## Requirements and usage

This project makes use of new features found in Play 2.6, like route modifiers and attributes. It
provides two different ways of doing authentication using JWT, directly in a Controller or by using a 
Filter.

The JWT specific code used can be found in `app/jwt` directory. The classes can be used directly as
they are, but specific details about your JWT and the verification should be configured in 
`JwtValidatorImpl.java` and `VerifiedJwtImpl.java`.

### JWT authentication in Controllers

A convenient helper injected to the controller makes it easy to verify the JWT token. The error
can be found in `res.left` and the verified JWT in `res.right`. VerifiedJwt should be customized 
to fit your project.

````java
@Inject
private JwtControllerHelper jwtControllerHelper;

public Result requiresJwt() {
    return jwtControllerHelper.verify(request(), res -> {
        if (res.left.isPresent()) {
            return forbidden(res.left.get().toString());
        }

        VerifiedJwt verifiedJwt = res.right.get();
        Logger.debug("{}", verifiedJwt);
        return ok("access granted");
    });
}
````

### JWT authentication using a Filter

The filter version makes use of Plays route modifiers and attributes. The filter `app/jwt/filter/JwtFiler` 
can be used as any other filter in Play. 

The filter automatically puts the VerifiedJwt object in a request attribute. This means that we 
can retrieve the object in our controller and start using it.

````java
public Result requiresJwtViaFilter() {
    Optional<VerifiedJwt> oVerifiedJwt = request().attrs().getOptional(Attrs.VERIFIED_JWT);
    return oVerifiedJwt.map(jwt -> {
        Logger.debug(jwt.toString());
        return ok("access granted via filter");
    }).orElse(forbidden("eh, no verified jwt found"));
}
````

A filter intercepts all incoming requests, therefore enabling this filter will require 
a JWT on every single request. In some cases this is not desired, for example when authenticating with 
a username and password (to get the token). 
`JwtFilter` supports route modifiers to disable JWT checking on incoming requests. If 
`JwtFilter` finds the modifier `+ noJwtFilter` in `routes`, then the filter will be skipped.

````text
# An example controller that does not require JWT
+ noJwtFilter
GET     /                           controllers.HomeController.generateSignedToken
````