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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.springframework.http.HttpMethod.HEAD;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class MetaDataController {

  private final static Logger LOG = LoggerFactory.getLogger(MetaDataController.class);
  public static final ZoneId GMT = ZoneId.of("GMT");

  private ServiceRegistryRepository serviceRegistryRepository;

  private ObjectMapper objectMapper = new ObjectMapper();

  private String serviceProvidersJson;
  private String identityProvidersJson;

  private ZonedDateTime lastUpdate = ZonedDateTime.now(GMT);

  @Autowired
  public MetaDataController(ServiceRegistryRepository serviceRegistryRepository, @Value("${metadata.refresh.minutes}") int period) {
    this.serviceRegistryRepository = serviceRegistryRepository;
    newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshMetadata, period, period, TimeUnit.MINUTES);
    refreshMetadata();
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/identity-providers.json")
  public ResponseEntity identityProvidersHead(HttpServletRequest request) {
    return isModified(request);
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/service-providers.json")
  public ResponseEntity serviceProvidersHead(HttpServletRequest request) {
    return isModified(request);
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
      this.lastUpdate = ZonedDateTime.now(GMT);
    } catch (JsonProcessingException e) {
      LOG.error("Exception in parsing JSON", e);
    }
  }

  private ResponseEntity isModified(HttpServletRequest request) {
    HttpStatus statusCode = HttpStatus.OK;
    String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
    if (StringUtils.hasText(ifModifiedSince)) {
      TemporalAccessor temporal = RFC_1123_DATE_TIME.parse(ifModifiedSince);
      if (lastUpdate.isBefore(ZonedDateTime.from(temporal))) {
        statusCode = HttpStatus.NOT_MODIFIED;
      }
    }
    return new ResponseEntity(statusCode);
  }

}
