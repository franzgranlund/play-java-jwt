/*
 * MIT License
 *
 * Copyright (c) 2017 Franz Granlund
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        if (requestHeader.attrs().containsKey(Router.Attrs.HANDLER_DEF)) {
            HandlerDef handler = requestHeader.attrs().get(Router.Attrs.HANDLER_DEF);
            List<String> modifiers = handler.getModifiers();

            if (modifiers.contains(ROUTE_MODIFIER_NO_JWT_FILTER_TAG)) {
                return nextFilter.apply(requestHeader);
            }
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