import com.google.inject.AbstractModule;
import jwt.JwtControllerHelper;
import jwt.JwtControllerHelperImpl;
import jwt.JwtValidator;
import jwt.JwtValidatorImpl;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(JwtValidator.class).to(JwtValidatorImpl.class).asEagerSingleton();
        bind(JwtControllerHelper.class).to(JwtControllerHelperImpl.class).asEagerSingleton();
    }
}
