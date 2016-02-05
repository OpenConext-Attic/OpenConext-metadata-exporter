package me.control;

import org.springframework.http.HttpHeaders;

import java.util.Arrays;

import static java.util.Arrays.asList;

public class PrePopulatedJsonHttpHeaders extends HttpHeaders {

  public PrePopulatedJsonHttpHeaders(String[]... headers) {
    super();
    this.add(HttpHeaders.CONTENT_TYPE, "application/json");
    asList(headers).forEach((header) -> this.add(header[0], header[1]));
  }
}
