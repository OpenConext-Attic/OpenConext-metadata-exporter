package me.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collector;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class JavaScriptErrorController {

  private final static Logger LOG = LoggerFactory.getLogger(JavaScriptErrorController.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final List<Map<String, String>> clients;

  @Autowired
  @SuppressWarnings("unchecked")
  public JavaScriptErrorController(@Value("${javascript.error.registrations.config}") Resource errorRegistrationsConfigPath) throws IOException {
    this.clients = objectMapper.readValue(errorRegistrationsConfigPath.getInputStream(), List.class);
  }

  @RequestMapping(value = "/jsError", method = RequestMethod.POST)
  public ResponseEntity<Void> reportError(@RequestBody Map<String, Object> payload) throws JsonProcessingException, UnknownHostException {
    String clientId = (String) payload.getOrDefault("clientId", "");
    String accessToken = (String) payload.getOrDefault("accessToken", "");
    Optional<Map<String, String>> client = this.clients.stream()
        .filter(map ->
            !clientId.isEmpty() && map.get("clientId").equals(clientId) && !accessToken.isEmpty() && map.get("accessToken").equals(accessToken))
        .collect(singletonOptionalCollector());
    if (client.isPresent()) {
      payload.put("dateTime", new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").format(new Date()));
      payload.put("machine", InetAddress.getLocalHost().getHostName());
      payload.remove("accessToken");
      String msg = objectMapper.writeValueAsString(payload);
      LOG.error(msg, new RuntimeException(msg));
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  private <T> Collector<T, List<T>, Optional<T>> singletonOptionalCollector() {
    return Collector.of(
        ArrayList::new, List::add, (left, right) -> left, list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0))
    );
  }

}
