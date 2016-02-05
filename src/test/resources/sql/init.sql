CREATE TABLE `janus__connection` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `revisionNr` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `type` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `user` int(11) DEFAULT NULL,
  `ip` char(39) COLLATE utf8_unicode_ci DEFAULT NULL
)
  ENGINE = InnoDB DEFAULT CHARSET = latin1 COLLATE = latin1_general_cs;

CREATE TABLE `janus__connectionRevision` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `eid` int(11) NOT NULL,
  `entityid` text NOT NULL,
  `revisionid` int(11) NOT NULL,
  `state` text,
  `type` text,
  `expiration` char(25) DEFAULT NULL,
  `metadataurl` text,
  `metadata_valid_until` datetime DEFAULT NULL,
  `metadata_cache_until` datetime DEFAULT NULL,
  `allowedall` char(25) NOT NULL DEFAULT 'yes',
  `manipulation` mediumtext,
  `user` int(11) DEFAULT NULL,
  `created` char(25) DEFAULT NULL,
  `ip` char(39) DEFAULT NULL,
  `parent` int(11) DEFAULT NULL,
  `revisionnote` text,
  `active` char(3) NOT NULL DEFAULT 'yes',
  `arp_attributes` text,
  `notes` text
  -- CONSTRAINT `FK_72BCD7F24FBDA576` FOREIGN KEY (`eid`) REFERENCES `janus__connection` (`id`) ON DELETE CASCADE,
)
  ENGINE = InnoDB DEFAULT CHARSET = latin1 COLLATE = latin1_general_cs;

CREATE TABLE `janus__metadata` (
  `connectionRevisionId` int(11) NOT NULL,
  `key` varchar(255) NOT NULL DEFAULT '',
  `value` text NOT NULL,
  `created` char(25) DEFAULT NULL ,
  `ip` char(39) DEFAULT NULL,
    PRIMARY KEY (`connectionRevisionId`,`key`)
  -- CONSTRAINT `FK_3CEF9AA549045D9` FOREIGN KEY (`connectionRevisionId`) REFERENCES `janus__connectionRevision` (`id`) ON DELETE CASCADE
)
  ENGINE = InnoDB DEFAULT CHARSET = latin1 COLLATE = latin1_general_cs;


CREATE TABLE `janus__allowedConnection` (
  `connectionRevisionId` int(11) NOT NULL,
  `remoteeid` int(11) NOT NULL,
  `created` char(25) DEFAULT NULL,
  `ip` char(39) DEFAULT NULL,
  PRIMARY KEY (`connectionRevisionId`,`remoteeid`)
  -- CONSTRAINT `FK_B71F875B3C2FCD2` FOREIGN KEY (`remoteeid`) REFERENCES `janus__connection` (`id`) ON DELETE CASCADE,
  -- CONSTRAINT `FK_B71F875B549045D9` FOREIGN KEY (`connectionRevisionId`) REFERENCES `janus__connectionRevision` (`id`) ON DELETE CASCADE
)
  ENGINE = InnoDB DEFAULT CHARSET = latin1 COLLATE = latin1_general_cs;