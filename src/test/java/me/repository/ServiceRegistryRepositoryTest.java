package me.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.Application;
import me.model.EntityState;
import me.model.EntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import java.util.List;
import java.util.Map;

import static org.springframework.test.context.jdbc.SqlConfig.ErrorMode.FAIL_ON_ERROR;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@Transactional
@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/init.sql", "classpath:sql/seed.sql"},
    config = @SqlConfig(errorMode = FAIL_ON_ERROR, transactionMode = ISOLATED))

public class ServiceRegistryRepositoryTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private DataSource dataSource;
  private ServiceRegistryRepository subject;

  @Before
  public void before() {
    this.subject = new ServiceRegistryRepository(dataSource);
  }

  @Test
  public void testMetaDataIdps() throws JsonProcessingException {
    List<Map<String, Object>> entities = this.subject.getEntities(EntityState.PROD, EntityType.IDP);
    String json = objectMapper.writeValueAsString(entities);
    System.out.println(json);
  }

  @Test
  public void testMetaDataSps() throws JsonProcessingException {
    List<Map<String, Object>> entities = this.subject.getEntities(EntityState.PROD, EntityType.SP);
    String json = objectMapper.writeValueAsString(entities);
    System.out.println(json);
  }
}