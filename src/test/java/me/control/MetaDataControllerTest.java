package me.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import me.AbstractIntegrationTest;
import me.Application;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.test.context.jdbc.SqlConfig.ErrorMode.FAIL_ON_ERROR;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Transactional
@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/init.sql", "classpath:sql/seed.sql"},
    config = @SqlConfig(errorMode = FAIL_ON_ERROR, transactionMode = ISOLATED))
public class MetaDataControllerTest extends AbstractIntegrationTest {

  @Test
  public void testMetaDataIdps() throws Exception {
    List<Map<String, Object>> idps = fetchMetatData("/identity-providers.json");
    assertJson("json/expected_identity_providers.json", idps);
  }

  @Test
  public void testMetaDataSps() throws Exception {
    List<Map<String, Object>> sps = fetchMetatData("/service-providers.json");
    assertJson("json/expected_service_providers.json", sps);
  }

  @Test
  public void testIfModified() throws Exception {
    HttpHeaders headers = new PrePopulatedJsonHttpHeaders(new String[]{
        HttpHeaders.IF_MODIFIED_SINCE, RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")).minusMinutes(60))
    });
    RequestEntity requestEntity = new RequestEntity(headers, HEAD, new URI("http://localhost:" + port + "/identity-providers.json"));
    HttpStatus statusCode = restTemplate.exchange(requestEntity, String.class).getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);
  }

  private List<Map<String, Object>> fetchMetatData(String path) throws URISyntaxException {
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