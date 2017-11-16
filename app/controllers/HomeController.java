package controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import jwt.JwtControllerHelper;
import jwt.VerifiedJwt;
import jwt.filter.Attrs;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

public class HomeController extends Controller {
    @Inject
    private JwtControllerHelper jwtControllerHelper;

    @Inject
    private Config config;

    public Result generateSignedToken() throws UnsupportedEncodingException {
        String secret = config.getString("play.http.secret.key");

        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                .withIssuer("ThePlayApp")
                .withClaim("user_id", 5)
                .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(10).toInstant()))
                .sign(algorithm);

        return ok("signed token: " + token);
    }

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

    public Result requiresJwtViaFilter() {
        Optional<VerifiedJwt> oVerifiedJwt = request().attrs().getOptional(Attrs.VERIFIED_JWT);
        return oVerifiedJwt.map(jwt -> {
            Logger.debug(jwt.toString());
            return ok("access granted via filter");
        }).orElse(forbidden("eh, no verified jwt found"));
    }
}
