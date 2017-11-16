import org.junit.Before;
import org.junit.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.test.WithServer;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;

public class JwtFunctionalTest extends WithServer {
    String token = null;

    @Before
    public void before() throws IOException, ExecutionException {
        WSResponse response = fetch("/");
        assertEquals(OK, response.getStatus());
        token = response.getBody().replace("signed token: ", "");
        assertNotNull(token);
    }

    private WSResponse fetch(String url) throws IOException, ExecutionException {
        try (WSClient ws = play.test.WSTestClient.newClient(play.api.test.Helpers.testServerPort())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            return stage.toCompletableFuture().get();
        } catch (InterruptedException e) {
            fail("should not fail");
            return null;
        }
    }

    private WSResponse fetchWithToken(String url, String token) throws IOException, ExecutionException {
        try (WSClient ws = play.test.WSTestClient.newClient(play.api.test.Helpers.testServerPort())) {
            CompletionStage<WSResponse> stage = ws.url(url).addHeader("Authorization", "Bearer " + token).get();
            return stage.toCompletableFuture().get();
        } catch (InterruptedException e) {
            fail("should not fail");
            return null;
        }
    }

    @Test
    public void testRequiresJwt() throws Exception {
        String url = "/requires-jwt";
        assertEquals(FORBIDDEN, fetch(url).getStatus());
        assertEquals(OK, fetchWithToken(url, token).getStatus());
    }

    @Test
    public void testRequiresJwtViaFilter() throws Exception {
        String url = "/requires-jwt-via-filter";
        assertEquals(FORBIDDEN, fetch(url).getStatus());
        assertEquals(OK, fetchWithToken(url, token).getStatus());
    }
}