package me.control;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.stereotype.Service;

import me.model.EntityType;
import me.repository.ServiceRegistryRepository;

@Service
public class MetaDataRefresher implements HealthIndicator {

  private final static Logger LOG = LoggerFactory.getLogger(MetaDataController.class);
  private final static ZoneId GMT = ZoneId.of("GMT");

  private ServiceRegistryRepository serviceRegistryRepository;

  private int refreshInMintes;

  private volatile List<Map<String, Object>> serviceProviders;
  private volatile List<Map<String, Object>> identityProviders;

  private volatile ZonedDateTime lastRefreshCheck = ZonedDateTime.now(GMT);
  private volatile ZonedDateTime serviceProvidersLastUpdated = ZonedDateTime.now(GMT);
  private volatile ZonedDateTime identityProvidersLastUpdated = ZonedDateTime.now(GMT);

  @Autowired
  public MetaDataRefresher(ServiceRegistryRepository serviceRegistryRepository, @Value("${metadata.refresh.minutes}") int period) {
    this.serviceRegistryRepository = serviceRegistryRepository;
    this.refreshInMintes = period;
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        TaskUtils.decorateTaskWithErrorHandler(this::refreshMetadata, t -> LOG.error("Exception in refreshMetadata task", t), true),
        1, period, TimeUnit.MINUTES);
  }

  public boolean isIdentityProvidersModified(ZonedDateTime modifiedSince) {
   return identityProvidersLastUpdated.isAfter(modifiedSince);
  }

  public boolean isServiceProvidersModified(ZonedDateTime modifiedSince) {
    return serviceProvidersLastUpdated.isAfter(modifiedSince);
  }

  public List<Map<String, Object>> getServiceProviders() {
    if (serviceProviders == null) {
      refreshMetadata();
    }
    return Collections.unmodifiableList(serviceProviders);
  }

  public List<Map<String, Object>> getIdentityProviders() {
    if (identityProviders == null) {
      refreshMetadata();
    }
    return Collections.unmodifiableList(identityProviders);
  }

  private void refreshMetadata() {
    long start = System.currentTimeMillis();

    LOG.info("Start refreshing metadata");

    this.lastRefreshCheck = ZonedDateTime.now(GMT);

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

  @Override
  public Health health() {
    Health.Builder healthBuilder = lastRefreshCheck.plusMinutes(refreshInMintes + 2).isBefore(ZonedDateTime.now(GMT)) ? Health.down() : Health.up();

    return healthBuilder
        .withDetail("lastRefreshCheck", lastRefreshCheck.format(DateTimeFormatter.ISO_DATE_TIME))
        .withDetail("serviceProvidersLastUpdated", serviceProvidersLastUpdated.format(DateTimeFormatter.ISO_DATE_TIME))
        .withDetail("identityProviderLastUpdated", identityProvidersLastUpdated.format(DateTimeFormatter.ISO_DATE_TIME))
        .build();
  }

}
