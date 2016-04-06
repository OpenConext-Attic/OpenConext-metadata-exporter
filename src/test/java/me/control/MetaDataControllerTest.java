package me.control;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;

import me.AbstractIntegrationTest;

public class MetaDataControllerTest extends AbstractIntegrationTest {

  @Test
  public void testMetaDataIdps() throws Exception {
    List<Map<String, Object>> idps = fetchMetaData("/identity-providers.json");

    assertEquals(2, idps.size());
    assertJson("json/expected_identity_providers.json", idps);
  }

  @Test
  public void testMetaDataSps() throws Exception {
    List<Map<String, Object>> sps = fetchMetaData("/service-providers.json");

    assertEquals(2, sps.size());
    assertJson("json/expected_service_providers.json", sps);
  }

  @Test
  public void testIdpOnly() throws Exception {
    List<Map<String, Object>> sps = fetchMetaData("/service-providers.json");
    Map<String, Object> defaultSp = sps.stream().filter(map -> map.get("entityid").equals("https://default-sp")).findFirst().get();
    Object idpVisibleOnly = defaultSp.get("coin:ss:idp_visible_only");

    assertEquals(idpVisibleOnly, "1");
  }

  @Test
  public void testMetaDataSpsWithUrlResource() throws Exception {
    String json = IOUtils.toString(new UrlResource("http://localhost:" + port + "/service-providers.json") {
      @Override
      public InputStream getInputStream() throws IOException {
        URLConnection con = this.getURL().openConnection();
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode("metadata.client:secret".getBytes()));
        con.setRequestProperty(AUTHORIZATION, basicAuth);
        con.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return con.getInputStream();
      }
    }.getInputStream());
    String expected = IOUtils.toString(new ClassPathResource("json/expected_service_providers_raw.json").getInputStream());

    assertEquals(expected, json);
  }

  @Test
  public void testNotModified() throws Exception {
    ZonedDateTime gmt = ZonedDateTime.now(ZoneId.of("GMT")).minusMinutes(60);
    doTestModified(gmt, OK, "/identity-providers.json");
    doTestModified(gmt, OK, "/service-providers.json");
  }

  @Test
  public void testIncludeNonProdSps() throws Exception {
    List<Map<String, Object>> idps = fetchMetaData("/identity-providers.json?includeTest=true");

    assertEquals(3, idps.size());
  }

  @Test
  public void testIncludeNonProdIdps() throws Exception {
    List<Map<String, Object>> sps = fetchMetaData("/service-providers.json?includeTest=true");

    assertEquals(3, sps.size());
  }

  @Test
  public void testModifiedAfter() throws Exception {
    ZonedDateTime gmt = ZonedDateTime.now(ZoneId.of("GMT")).plusMinutes(60);

    doTestModified(gmt, NOT_MODIFIED, "/identity-providers.json");
    doTestModified(gmt, NOT_MODIFIED, "/service-providers.json");
  }

  @Test
  public void illegalDateForModifiedSinceHeaderShouldBeIgnored() {
    RequestEntity<Void> request = RequestEntity
      .head(URI.create(String.format("http://localhost:%d/service-providers.json", port)))
      .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
      .header(IF_MODIFIED_SINCE, "Mon, 06 Apr 2016 8:00:00 GMT")
      .build();

    HttpStatus statusCode = restTemplate.exchange(request, String.class).getStatusCode();

    assertThat(statusCode, is(OK));
  }

  @Test
  public void notADateForModifiedSinceHeaderShouldBeIgnored() {
    RequestEntity<Void> request = RequestEntity
      .head(URI.create(String.format("http://localhost:%d/service-providers.json", port)))
      .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
      .header(IF_MODIFIED_SINCE, "blabla")
      .build();

    HttpStatus statusCode = restTemplate.exchange(request, String.class).getStatusCode();

    assertThat(statusCode, is(OK));
  }

  @Test
  public void noModifiedSinceHeaderShouldGetAnOk() {
    RequestEntity<Void> request = RequestEntity
      .head(URI.create(String.format("http://localhost:%d/service-providers.json", port)))
      .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
      .build();

    HttpStatus statusCode = restTemplate.exchange(request, String.class).getStatusCode();

    assertThat(statusCode, is(OK));
  }


  private void doTestModified(ZonedDateTime modifiedSince, HttpStatus expectedStatusCode, String path) {
    RequestEntity<Void> request = RequestEntity
        .head(URI.create("http://localhost:" + port + path))
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .ifModifiedSince(modifiedSince.toInstant().toEpochMilli()).build();

    HttpStatus statusCode = restTemplate.exchange(request, String.class).getStatusCode();

    assertEquals(expectedStatusCode, statusCode);
  }

  private List<Map<String, Object>> fetchMetaData(String path) throws URISyntaxException {
    RequestEntity<Void> request = RequestEntity
        .get(URI.create(String.format("http://localhost:%d%s", port, path)))
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .build();

    return restTemplate.exchange(request, new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
  }

  private void assertJson(String expectedJsonPath, Object object) throws IOException {
    String expected = IOUtils.toString(new ClassPathResource(expectedJsonPath).getInputStream());
    String actual = objectWriter.writeValueAsString(object);

    assertEquals(expected, actual);
  }

}