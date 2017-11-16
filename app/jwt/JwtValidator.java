package jwt;

import play.libs.F;

public interface JwtValidator {
    enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }

    F.Either<Error, VerifiedJwt> verify(String token);
}
