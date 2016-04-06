package me.web;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;

import org.junit.Test;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import me.AbstractIntegrationTest;
import me.control.PrePopulatedJsonHttpHeaders;

public class BasicAuthenticationFilterTest extends AbstractIntegrationTest {

  @Test
  public void testAuthenticationWithBadCredentials() throws Exception {
    doTest(new TestRestTemplate("metadata.client", "bogus"), headers);
  }

  @Test
  public void testAuthenticationWithoutHeaders() throws Exception {
    doTest(new TestRestTemplate(), headers);
  }

  @Test
  public void testUnprotectedHealth() throws Exception {
    ResponseEntity<Map> response = new TestRestTemplate().exchange(new RequestEntity<Void>(headers, GET, new URI("http://localhost:" + port + "/health")), Map.class);
    assertEquals(200, response.getStatusCode().value());
    assertEquals("UP", response.getBody().get("status"));
  }

  @Test
  public void testAuthenticationWithWrongHeader() throws Exception {
    doTest(new TestRestTemplate(), new PrePopulatedJsonHttpHeaders(
        new String[]{HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("bogus".getBytes())}));
  }

  private void doTest(RestTemplate restTemplate, HttpHeaders headers) throws URISyntaxException {
    ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(headers, GET, new URI("http://localhost:" + port + "/identity-providers.json")), String.class);
    assertEquals(401, response.getStatusCode().value());
  }

}