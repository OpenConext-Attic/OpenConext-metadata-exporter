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

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class MetaDataController {

  private final static Logger LOG = LoggerFactory.getLogger(MetaDataController.class);
  public static final ZoneId GMT = ZoneId.of("GMT");

  private ServiceRegistryRepository serviceRegistryRepository;

  private ObjectMapper objectMapper = new ObjectMapper();

  private String serviceProvidersJson;
  private String identityProvidersJson;

  private ZonedDateTime serviceProvidersLastUpdated = ZonedDateTime.now(GMT);
  private ZonedDateTime identityProvidersLastUpdated = ZonedDateTime.now(GMT);

  @Autowired
  public MetaDataController(ServiceRegistryRepository serviceRegistryRepository, @Value("${metadata.refresh.minutes}") int period) {
    this.serviceRegistryRepository = serviceRegistryRepository;
    newScheduledThreadPool(1).scheduleAtFixedRate(this::refreshMetadata, 1, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/identity-providers.json")
  public ResponseEntity identityProvidersHead(HttpServletRequest request) {
    return isModified(request, identityProvidersLastUpdated);
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/service-providers.json")
  public ResponseEntity serviceProvidersHead(HttpServletRequest request) {
    return isModified(request, serviceProvidersLastUpdated);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/identity-providers.json")
  public String identityProviders() {
    assureData(this.identityProvidersJson);
    return this.identityProvidersJson;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/service-providers.json")
  public String serviceProviders() {
    assureData(this.serviceProvidersJson);
    return this.serviceProvidersJson;
  }

  private void refreshMetadata() {
    List<Map<String, Object>> serviceProviders = serviceRegistryRepository.getEntities(EntityState.PROD, EntityType.SP);
    List<Map<String, Object>> identityProviders = serviceRegistryRepository.getEntities(EntityState.PROD, EntityType.IDP);

    try {
      String newServiceProviders = objectMapper.writeValueAsString(serviceProviders);
      boolean spEquals = newServiceProviders.equals(this.serviceProvidersJson);
      this.serviceProvidersJson = newServiceProviders;

      String newIdentityProviders = objectMapper.writeValueAsString(identityProviders);
      boolean idpEquals = newIdentityProviders.equals(this.identityProvidersJson);
      this.identityProvidersJson = newIdentityProviders;

      this.serviceProvidersLastUpdated = spEquals ? this.serviceProvidersLastUpdated : ZonedDateTime.now(GMT);
      LOG.info("Refreshed Metadata. ServiceProviders metadata has changed: " + !spEquals);

      this.identityProvidersLastUpdated = idpEquals ? this.identityProvidersLastUpdated : ZonedDateTime.now(GMT);
      LOG.info("Refreshed Metadata. IdentityProviders metadata has changed: " + !idpEquals);
    } catch (JsonProcessingException e) {
      LOG.error("Exception in parsing JSON", e);
    }
  }

  private ResponseEntity isModified(HttpServletRequest request, ZonedDateTime lastUpdated) {
    HttpStatus statusCode = HttpStatus.OK;
    String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
    if (StringUtils.hasText(ifModifiedSince)) {
      TemporalAccessor temporal = RFC_1123_DATE_TIME.parse(ifModifiedSince);
      if (lastUpdated.isBefore(ZonedDateTime.from(temporal))) {
        statusCode = HttpStatus.NOT_MODIFIED;
      }
    }
    return new ResponseEntity(statusCode);
  }

  private void assureData(String metaData) {
    if (metaData == null) {
      this.refreshMetadata();
    }
  }


}
