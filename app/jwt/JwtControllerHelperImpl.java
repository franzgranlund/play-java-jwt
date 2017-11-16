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

package jwt;

import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

import static play.mvc.Results.forbidden;

public class JwtControllerHelperImpl implements JwtControllerHelper {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String ERR_AUTHORIZATION_HEADER = "ERR_AUTHORIZATION_HEADER";
    private JwtValidator jwtValidator;

    @Inject
    public JwtControllerHelperImpl(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Result verify(Http.Request request, Function<F.Either<JwtValidator.Error, VerifiedJwt>, Result> f) {
        Optional<String> authHeader =  request.getHeaders().get(HEADER_AUTHORIZATION);

        if (!authHeader.filter(ah -> ah.contains(BEARER)).isPresent()) {
            Logger.error("f=JwtControllerHelperImpl, event=verify, error=authHeaderNotPresent");
            return forbidden(ERR_AUTHORIZATION_HEADER);
        }

        String token = authHeader.map(ah -> ah.replace(BEARER, "")).orElse("");
        return f.apply(jwtValidator.verify(token));
    }
}
