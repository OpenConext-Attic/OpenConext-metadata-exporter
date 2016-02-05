package me.model;

public enum EntityState {

  PROD("prodaccepted"),
  ACC("testaccepted");

  private final String state;

  EntityState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }
}
