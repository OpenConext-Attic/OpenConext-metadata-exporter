package me;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import me.control.PrePopulatedJsonHttpHeaders;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true, value = "flyway.enabled=true")
public abstract class AbstractIntegrationTest {

  protected static ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

  @Value("${local.server.port}")
  protected int port;

  protected static TestRestTemplate restTemplate = new TestRestTemplate("metadata.client", "secret");

  protected static HttpHeaders headers = new PrePopulatedJsonHttpHeaders();

}