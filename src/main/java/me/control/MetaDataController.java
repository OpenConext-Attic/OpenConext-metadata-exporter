package me.control;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.stream.Collectors.toList;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.model.EntityState;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class MetaDataController {

  @Autowired
  private MetaDataRefresher metaDataRefresher;

  @RequestMapping(method = RequestMethod.HEAD, value = "/identity-providers.json")
  public ResponseEntity<Void> identityProvidersHead(HttpServletRequest request) {
    HttpStatus status = isModified(request, metaDataRefresher::isIdentityProvidersModified);

    return ResponseEntity.status(status).build();
  }

  @RequestMapping(method = RequestMethod.HEAD, value = "/service-providers.json")
  public ResponseEntity<Void> serviceProvidersHead(HttpServletRequest request) {
    HttpStatus status = isModified(request, metaDataRefresher::isServiceProvidersModified);

    return ResponseEntity.status(status).build();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/identity-providers.json")
  public List<Map<String, Object>> identityProviders(@RequestParam(value = "includeTest", defaultValue = "false", required = false) boolean includeTest) {
    List<Map<String,Object>> identityProviders = metaDataRefresher.getIdentityProviders();

    if (!includeTest) {
      return filterNonProdProviders(identityProviders);
    }
    return identityProviders;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/service-providers.json")
  public List<Map<String, Object>> serviceProviders(@RequestParam(value = "includeTest", defaultValue = "false", required = false) boolean includeTest) {
    List<Map<String, Object>> serviceProviders = metaDataRefresher.getServiceProviders();
    if (!includeTest) {
      return filterNonProdProviders(serviceProviders);
    }
    return serviceProviders;
  }

  private List<Map<String, Object>> filterNonProdProviders(List<Map<String, Object>> providers) {
    return providers.stream().filter(map -> map.getOrDefault("state", "not-include").equals(EntityState.PROD.getState())).collect(toList());
  }

  private HttpStatus isModified(HttpServletRequest request, Function<ZonedDateTime, Boolean> isModified) {
    return  getModifiedSinceHeader(request).map(isModified).orElse(true) ? HttpStatus.OK : HttpStatus.NOT_MODIFIED;
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

}
