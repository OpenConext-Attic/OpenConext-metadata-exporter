package me.repository;

import me.model.*;
import de.ailis.pherialize.Mixed;
import de.ailis.pherialize.Pherialize;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class ServiceRegistryRepository {

  private final JdbcTemplate jdbcTemplate;

  private final Pattern isNumber = Pattern.compile("^[0-9]+$");

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
          return entity;
        });

  }

  private void addArp(Map<String, Object> entity, String arp) {
    if (StringUtils.hasText(arp)) {
      Mixed unserialize = Pherialize.unserialize(arp);
      entity.put("attributes", unserialize == null ?
          emptyList() :
          unserialize.toArray().keySet().stream().map(obj -> ((Mixed) obj).getValue()).collect(toList()));
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

  @SuppressWarnings("unchecked")
  private void parseMetaData(Map<String, Object> entity, String key, String value) {
    //if there is a ':' in the key we will make sub maps for each underlying value
    if (StringUtils.hasText(value)) {
      if (key.contains(":")) {
        //if the first value after the first ":" is a number then we need to convert to a List
        String[] values = key.split(":");
        if (isNumber.matcher(values[1]).matches()) {
          entity.putIfAbsent(values[0], new ArrayList());
          List list = (List) entity.get(values[0]);
          int position = Integer.parseInt(values[1]);
          //for each missing position we need a HashMap
          IntStream.range(list.size(), position + 1).forEach(i -> {
            list.add(new LinkedHashMap<>());
          });
          ((Map) list.get(position)).put(values[2], value);
        } else {
          int length = values.length;
          for (int i = 0; i < length - 1; i++) {
            entity.putIfAbsent(values[i], new LinkedHashMap<>());
          }
          //now put the last value in the last Map
          ((Map) entity.get(values[length - 2])).put(values[length - 1], value);
        }
      } else {
        entity.put(key, value);
      }
    }
  }
}
