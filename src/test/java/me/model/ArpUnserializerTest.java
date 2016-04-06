package me.model;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

public class ArpUnserializerTest {

  private ArpUnserializer subject = new ArpUnserializer();

  @Test
  public void testUnserializeSimpleArp() throws Exception {
    String arp = "a:1:{s:55:\"urn:mace:terena.org:attribute-def:schacHomeOrganization\";a:1:{i:0;s:1:\"*\";}";
    Collection<String> attributes = subject.unserialize(arp).get();
    assertEquals(asList("urn:mace:terena.org:attribute-def:schacHomeOrganization"), attributes);
  }

  @Test
  public void testUnserialize() throws Exception {
    String arp = "a:6:{s:49:\"urn:mace:dir:attribute-def:eduPersonPrincipalName\";a:1:{i:0;s:1:\"*\";}s:38:\"" +
        "urn:mace:dir:attribute-def:displayName\";a:1:{i:0;s:1:\"*\";}s:29:" +
        "\"urn:mace:dir:attribute-def:cn\";a:1:{i:0;s:1:\"*\";}s:31:" +
        "\"urn:mace:dir:attribute-def:mail\";a:1:{i:0;s:1:\"*\";}s:55:" +
        "\"urn:mace:terena.org:attribute-def:schacHomeOrganization\";a:1:{i:0;s:1:\"*\";}s:47:" +
        "\"urn:mace:dir:attribute-def:eduPersonAffiliation\";a:1:{i:0;s:1:\"*\";}}";
    Collection<String> attributes = subject.unserialize(arp).get();
    assertEquals(asList(
        "urn:mace:dir:attribute-def:eduPersonPrincipalName",
        "urn:mace:dir:attribute-def:displayName",
        "urn:mace:dir:attribute-def:cn",
        "urn:mace:dir:attribute-def:mail",
        "urn:mace:terena.org:attribute-def:schacHomeOrganization",
        "urn:mace:dir:attribute-def:eduPersonAffiliation"), attributes);
  }

  @Test
  public void testUnserializeNullArp() throws Exception {
    Optional<Collection<String>> attributes = subject.unserialize(null);
    assertFalse(attributes.isPresent());
  }

  @Test
  public void testUnserializeEmptyArp() throws Exception {
    Collection<String> attributes = subject.unserialize(" ").get();
    assertEquals(0, attributes.size());
  }

  @Test
  public void testUnserializePhpNullArp() throws Exception {
    Optional<Collection<String>> attributes = subject.unserialize("N;");
    assertFalse(attributes.isPresent());
  }
}