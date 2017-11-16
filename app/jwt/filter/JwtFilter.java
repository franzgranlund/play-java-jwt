package jwt.filter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.inject.Inject;
import akka.stream.Materializer;
import jwt.JwtValidator;
import jwt.VerifiedJwt;
import play.Logger;
import play.libs.F;
import play.mvc.*;
import play.routing.HandlerDef;
import play.routing.Router;

import static play.mvc.Results.forbidden;

public class JwtFilter extends Filter {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String ROUTE_MODIFIER_NO_JWT_FILTER_TAG = "noJwtFilter";
    private static final String ERR_AUTHORIZATION_HEADER = "ERR_AUTHORIZATION_HEADER";
    private JwtValidator jwtValidator;

    @Inject
    public JwtFilter(Materializer mat, JwtValidator jwtValidator) {
        super(mat);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> nextFilter, Http.RequestHeader requestHeader) {
        HandlerDef handler = requestHeader.attrs().get(Router.Attrs.HANDLER_DEF);
        List<String> modifiers = handler.getModifiers();

        if (modifiers.contains(ROUTE_MODIFIER_NO_JWT_FILTER_TAG)) {
            return nextFilter.apply(requestHeader);
        }

        Optional<String> authHeader =  requestHeader.getHeaders().get(HEADER_AUTHORIZATION);

        if (!authHeader.filter(ah -> ah.contains(BEARER)).isPresent()) {
            Logger.error("f=JwtFilter, error=authHeaderNotPresent");
            return CompletableFuture.completedFuture(forbidden(ERR_AUTHORIZATION_HEADER));
        }

        String token = authHeader.map(ah -> ah.replace(BEARER, "")).orElse("");
        F.Either<jwt.JwtValidator.Error, VerifiedJwt> res = jwtValidator.verify(token);

        if (res.left.isPresent()) {
            return CompletableFuture.completedFuture(forbidden(res.left.get().toString()));
        }

        return nextFilter.apply(requestHeader.withAttrs(requestHeader.attrs().put(Attrs.VERIFIED_JWT, res.right.get())));
    }
}