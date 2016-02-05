package me.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.model.EntityState;
import me.model.EntityType;
import me.repository.ServiceRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class MetaDataController {

  private final static Logger LOG = LoggerFactory.getLogger(MetaDataController.class);

  private ServiceRegistryRepository serviceRegistryRepository;

  private ObjectMapper objectMapper = new ObjectMapper();

  private String serviceProvidersJson;
  private String identityProvidersJson;

  @Autowired
  public MetaDataController(ServiceRegistryRepository serviceRegistryRepository, @Value("${metadata.refresh.minutes}") int period) {
    this.serviceRegistryRepository = serviceRegistryRepository;
    newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshMetadata, period, period, TimeUnit.MINUTES);
    refreshMetadata();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/identity-providers.json")
  public String identityProviders() {
    return this.identityProvidersJson;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/service-providers.json")
  public String serviceProviders() {
    return this.serviceProvidersJson;
  }

  private void refreshMetadata() {
    List<Map<String, Object>> serviceProviders = serviceRegistryRepository.getEntities(EntityState.PROD, EntityType.SP);
    List<Map<String, Object>> identityProviders = serviceRegistryRepository.getEntities(EntityState.PROD, EntityType.IDP);

    try {
      this.serviceProvidersJson = objectMapper.writeValueAsString(serviceProviders);
      this.identityProvidersJson = objectMapper.writeValueAsString(identityProviders);
    } catch (JsonProcessingException e) {
      LOG.error("Exception in parsing JSON", e);
    }
  }


}
