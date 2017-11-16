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

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;

@Singleton
public class JwtValidatorImpl implements JwtValidator {
    private String secret;
    private JWTVerifier verifier;

    @Inject
    public JwtValidatorImpl(Config config) throws UnsupportedEncodingException {
        this.secret = config.getString("play.http.secret.key");

        Algorithm algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm)
                .withIssuer("ThePlayApp")
                .build();
    }

    @Override
    public F.Either<Error, VerifiedJwt> verify(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            VerifiedJwtImpl verifiedJwt = new VerifiedJwtImpl(jwt);
            return F.Either.Right(verifiedJwt);
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            Logger.error("f=JwtValidatorImpl, event=verify, exception=JWTVerificationException, msg={}",
                    exception.getMessage());
            return F.Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }
}
