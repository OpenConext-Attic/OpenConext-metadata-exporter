package me.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessNotAllowedException extends RuntimeException {

  public AccessNotAllowedException(String message) {
    super(message);
  }
}
