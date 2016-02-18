package me.repository;

import me.model.ArpUnserializer;
import me.model.EntityState;
import me.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class ServiceRegistryRepository {

  private final JdbcTemplate jdbcTemplate;

  private final ArpUnserializer arpUnserializer = new ArpUnserializer();


  @Autowired
  public ServiceRegistryRepository(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public List<Map<String, Object>> getEntities(EntityState state, EntityType type) {
    return jdbcTemplate.query("SELECT CONNECTION.id, CONNECTION.revisionNr FROM janus__connection AS CONNECTION " +
            "INNER JOIN janus__connectionRevision AS CONNECTION_REVISION ON CONNECTION_REVISION.eid = CONNECTION.id " +
            "AND CONNECTION_REVISION.revisionid = CONNECTION.revisionNr WHERE CONNECTION_REVISION.active = 'yes' AND " +
            "CONNECTION_REVISION.state=? AND CONNECTION.type=?",
        new String[]{state.getState(), type.getType()},
        (rs, rowNum) -> getEntity(rs.getLong("id"), rs.getLong("revisionNr"))
    );
  }

  private Map<String, Object> getEntity(Long eid, Long revisionid) {
    return jdbcTemplate.queryForObject("SELECT entityid, allowedall, arp_attributes FROM janus__connectionRevision " +
            "WHERE  eid = ? AND revisionid = ?",
        new Long[]{eid, revisionid},
        (rs, rowNum) -> {
          Map<String, Object> entity = new LinkedHashMap<>();
          entity.put("entityid", rs.getString("entityid"));
          entity.put("allowedall", rs.getString("allowedall"));
          addArp(entity, rs.getString("arp_attributes"));
          addMetaData(entity, eid, revisionid);
          addAllowedEntities(entity, eid, revisionid);
          return entity;
        });

  }

  private void addArp(Map<String, Object> entity, String arp) {
    Optional<Collection<String>> attributes = arpUnserializer.unserialize(arp);
    if (attributes.isPresent()) {
      entity.put("attributes",attributes.get());
    }
  }

  private void addMetaData(Map<String, Object> entity, Long eid, Long revisionid) {
    jdbcTemplate.query("SELECT METADATA.`key`, METADATA.`value` FROM janus__connectionRevision AS CONNECTION_REVISION " +
            "INNER JOIN janus__metadata AS METADATA ON METADATA.connectionRevisionId = CONNECTION_REVISION.id " +
            "WHERE CONNECTION_REVISION.eid = ? AND CONNECTION_REVISION.revisionid = ?",
        new Long[]{eid, revisionid},
        (rs) -> {
          parseMetaData(entity, rs.getString("key"), rs.getString("value"));
        }
    );
  }

  private void addAllowedEntities(Map<String, Object> entity, Long eid, Long revisionid) {
    List<String> allowedEntities = jdbcTemplate.queryForList("SELECT ALLOWED_CONNECTION.name AS entityid " +
            "FROM janus__connectionRevision AS CONNECTION_REVISION " +
            "INNER JOIN `janus__allowedConnection` a ON a.connectionRevisionId = CONNECTION_REVISION.id INNER JOIN " +
            "janus__connection AS ALLOWED_CONNECTION ON ALLOWED_CONNECTION.id = a.remoteeid WHERE " +
            "CONNECTION_REVISION.eid = ? AND CONNECTION_REVISION.revisionid = ?",
        new Long[]{eid, revisionid},
        String.class
    );
    entity.put("allowedEntities", allowedEntities);
  }

  private void parseMetaData(Map<String, Object> entity, String key, String value) {
    if (StringUtils.hasText(value)) {
      entity.put(key, value);
    }
  }
}
