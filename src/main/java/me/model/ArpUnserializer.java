package me.model;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArpUnserializer {

  private final Pattern arpPattern = Pattern.compile("[\\{\\}]s:\\d*:\"([^\"]+)\"");
  private final String noArp = "N;";

  public Optional<Collection<String>> unserialize(String arp) {
    if (StringUtils.isEmpty(arp) || noArp.equals(arp)) {
      return Optional.empty();
    }
    Collection<String> result = new ArrayList<>();
    Matcher matcher = arpPattern.matcher(arp);
    while (matcher.find()) {
      result.add(matcher.group(1));
    }
      Set<String> attributes = result.stream()
          .filter(attribute -> attribute.startsWith("urn"))
          .collect(Collectors.toSet());

      List<String> sorted = new ArrayList<>(attributes);
      Collections.sort(sorted);

      return Optional.of(sorted);
  }
}
