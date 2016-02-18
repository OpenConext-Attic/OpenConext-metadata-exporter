package me.model;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    return Optional.of(result);
  }
}
