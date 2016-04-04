package me.control;

import me.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.POST;

public class JavaScriptErrorControllerTest extends AbstractIntegrationTest {

  @Test
  public void testReportErrorWithUnknownClient() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("clientId", "unknown");
    body.put("accessToken", "secret");
    doTestReportJsError(body, 404);
  }

  @Test
  public void testReportErrorWithNoClient() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("accessToken", "secret");
    doTestReportJsError(body, 404);
  }

  @Test
  public void testReportErrorWithWrongAccessToken() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("clientId", "attribute-aggregator");
    body.put("accessToken", "wrong");
    doTestReportJsError(body, 404);
  }

  @Test
  public void testReportErrorWithNoAccessToken() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("clientId", "attribute-aggregator");
    doTestReportJsError(body, 404);
  }

  @Test
  public void testReportError() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("clientId", "attribute-aggregator");
    body.put("accessToken", "secret");
    doTestReportJsError(body, 200);
  }

  private void doTestReportJsError(Map<String, String> body, int expectedStatus) throws URISyntaxException {
    ResponseEntity<Void> response = new TestRestTemplate().exchange(new RequestEntity(body, headers, POST, new URI("http://localhost:" + port + "/jsError")), Void.class);
    assertEquals(expectedStatus, response.getStatusCode().value());
  }
}