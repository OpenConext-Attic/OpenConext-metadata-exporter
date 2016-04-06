package me.control;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.stream.Collectors.toList;
import static org.springframework.scheduling.support.TaskUtils.decorateTaskWithErrorHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.model.EntityState;
import me.model.EntityType;
import me.repository.ServiceRegistryRepository;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class MetaDataController {

  private final static Logger LOG = LoggerFactory.getLogger(MetaDataController.class);
  private final static ZoneId GMT = ZoneId.of("GMT");

  private ServiceRegistryRepository serviceRegistryRepository;

  private List<Map<String, Object>> serviceProviders;
  private List<Map<String, Object>> identityProviders;

  private ZonedDateTime serviceProvidersLastUpdated = ZonedDateTime.now(GMT);
  private ZonedDateTime identityProvidersLastUpdated = ZonedDateTime.now(GMT);

  @Autowired
  public MetaDataController(ServiceRegistryRepository serviceRegistryRepository, @Value("${metadata.refresh.minutes}") int period) {
    this.serviceRegistryRepository = serviceRegistryRepository;
    newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        decorateTaskWithErrorHandler(this::refreshMetadata, t -> LOG.error("Exception in refreshMetadata task", t), true),
        1, period, TimeUnit.MINUTES);
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/identity-providers.json")
  public ResponseEntity<Void> identityProvidersHead(HttpServletRequest request) {
    return isModified(request, identityProvidersLastUpdated);
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/service-providers.json")
  public ResponseEntity<Void> serviceProvidersHead(HttpServletRequest request) {
    return isModified(request, serviceProvidersLastUpdated);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/identity-providers.json")
  public List<Map<String, Object>> identityProviders(@RequestParam(value = "includeTest", defaultValue = "false", required = false) boolean includeTest) {
    assureData(this.identityProviders);
    if (!includeTest) {
      return filterNonProdProviders(this.identityProviders);
    }
    return this.identityProviders;
  }

  private List<Map<String, Object>> filterNonProdProviders(List<Map<String, Object>> providers) {
    return providers.stream().filter(map -> map.getOrDefault("state", "not-include").equals(EntityState.PROD.getState())).collect(toList());
  }

  @RequestMapping(method = RequestMethod.GET, value = "/service-providers.json")
  public List<Map<String, Object>> serviceProviders(@RequestParam(value = "includeTest", defaultValue = "false", required = false) boolean includeTest) {
    assureData(this.serviceProviders);
    if (!includeTest) {
      return filterNonProdProviders(this.serviceProviders);
    }
    return this.serviceProviders;
  }

  private void refreshMetadata() {
    long start = System.currentTimeMillis();

    LOG.info("Start refreshing metadata");

    List<Map<String, Object>> newServiceProviders = serviceRegistryRepository.getEntities(EntityType.SP);
    List<Map<String, Object>> newIdentityProviders = serviceRegistryRepository.getEntities(EntityType.IDP);

    boolean spEquals = newServiceProviders.equals(this.serviceProviders);
    this.serviceProviders = newServiceProviders;

    boolean idpEquals = newIdentityProviders.equals(this.identityProviders);
    this.identityProviders = newIdentityProviders;

    this.serviceProvidersLastUpdated = spEquals ? this.serviceProvidersLastUpdated : ZonedDateTime.now(GMT);
    LOG.info("ServiceProviders metadata has changed: " + !spEquals);

    this.identityProvidersLastUpdated = idpEquals ? this.identityProvidersLastUpdated : ZonedDateTime.now(GMT);
    LOG.info("IdentityProviders metadata has changed: " + !idpEquals);

    LOG.info("Finished refreshing metadata in " + (System.currentTimeMillis() - start) + " ms");
  }

  private ResponseEntity<Void> isModified(HttpServletRequest request, ZonedDateTime lastUpdated) {
    HttpStatus statusCode = getModifiedSinceHeader(request)
        .map(modifiedSince -> lastUpdated.isBefore(modifiedSince) ? HttpStatus.NOT_MODIFIED : HttpStatus.OK)
        .orElse(HttpStatus.OK);

    return ResponseEntity.status(statusCode).build();
  }

  private Optional<ZonedDateTime> getModifiedSinceHeader(HttpServletRequest request) {
    String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);

    if (!StringUtils.hasText(ifModifiedSince)) {
      return Optional.empty();
    }

    try {
      return Optional.of(ZonedDateTime.from(RFC_1123_DATE_TIME.parse(ifModifiedSince)));
    } catch (DateTimeParseException e) {
      // could not parse the modified since header, pretend it wasn't there
      return Optional.empty();
    }
  }

  //we need to assure we don't get called to deliver metadata when the timer has not been hit
  private void assureData(Object metaData) {
    if (metaData == null) {
      this.refreshMetadata();
    }
  }

}
