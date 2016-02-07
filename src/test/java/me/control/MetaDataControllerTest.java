package me.control;

import me.AbstractIntegrationTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;

public class MetaDataControllerTest extends AbstractIntegrationTest {

  @Test
  public void testMetaDataIdps() throws Exception {
    List<Map<String, Object>> idps = fetchMetaData("/identity-providers.json");
    assertJson("json/expected_identity_providers.json", idps);
  }

  @Test
  public void testMetaDataSps() throws Exception {
    List<Map<String, Object>> sps = fetchMetaData("/service-providers.json");
    assertJson("json/expected_service_providers.json", sps);
  }

  @Test
  public void testMetaDataSpsWithUrlResource() throws Exception {
    String json = IOUtils.toString(new UrlResource("http://localhost:" + port + "/service-providers.json") {
      @Override
      public InputStream getInputStream() throws IOException {
        URLConnection con = this.getURL().openConnection();
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode("metadata.client:secret".getBytes()));
        con.setRequestProperty("Authorization", basicAuth);
        con.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
          return con.getInputStream();
        } catch (IOException ex) {
          if (con instanceof HttpURLConnection) {
            ((HttpURLConnection) con).disconnect();
          }
          throw ex;
        }
      }
    }.getInputStream());
    String expected = IOUtils.toString(new ClassPathResource("json/expected_service_providers_raw.json").getInputStream());
    assertEquals(expected, json);
  }

  @Test
  public void testNotModified() throws Exception {
    ZonedDateTime gmt = ZonedDateTime.now(ZoneId.of("GMT")).minusMinutes(60);
    doTestModified(gmt, HttpStatus.OK, "/identity-providers.json");
    doTestModified(gmt, HttpStatus.OK, "/service-providers.json");
  }

  @Test
  public void testModifiedAfter() throws Exception {
    ZonedDateTime gmt = ZonedDateTime.now(ZoneId.of("GMT")).plusMinutes(60);
    doTestModified(gmt, HttpStatus.NOT_MODIFIED, "/identity-providers.json");
    doTestModified(gmt, HttpStatus.NOT_MODIFIED, "/service-providers.json");
  }

  private void doTestModified(ZonedDateTime modifiedSince, HttpStatus expectedStatusCode, String path) throws URISyntaxException {
    HttpHeaders headers = new PrePopulatedJsonHttpHeaders(new String[]{
        HttpHeaders.IF_MODIFIED_SINCE, RFC_1123_DATE_TIME.format(modifiedSince)
    });
    RequestEntity requestEntity = new RequestEntity(headers, HEAD, new URI("http://localhost:" + port + path));
    HttpStatus statusCode = restTemplate.exchange(requestEntity, String.class).getStatusCode();
    assertEquals(expectedStatusCode, statusCode);
  }

  private List<Map<String, Object>> fetchMetaData(String path) throws URISyntaxException {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + path));
    return restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<Map<String, Object>>>() {
    }).getBody();
  }

  private void assertJson(String expectedJsonPath, Object object) throws IOException {
    String expected = IOUtils.toString(new ClassPathResource(expectedJsonPath).getInputStream());
    String actual = objectWriter.writeValueAsString(object);
    assertEquals(expected, actual);
  }

}