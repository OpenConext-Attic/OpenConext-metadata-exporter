INSERT INTO `janus__connection` (`id`, `revisionNr`, `name`, `type`)
VALUES
  (1, 2, 'https://mock-sp', 'saml20-sp'),
  (2, 2, 'https://default-sp', 'saml20-sp'),
  (3, 2, 'http://mock-idp', 'saml20-idp'),
  (4, 2, 'https://default-idp', 'saml20-idp');

INSERT INTO `janus__connectionRevision` (`id`, `eid`, `entityid`, `revisionid`, `state`, `type`, `allowedall`, `active`, `arp_attributes`)
VALUES
  (1, 1, 'https://mock-sp', 0, 'prodaccepted', 'saml20-sp', 'yes', 'yes', 'N;'),
  (2, 1, 'https://mock-sp', 1, 'prodaccepted', 'saml20-sp', 'yes', 'yes', 'N;'),
  (3, 1, 'https://mock-sp', 2, 'prodaccepted', 'saml20-sp', 'yes', 'yes', 'a:4:{s:38:"urn:mace:dir:attribute-def:displayName";a:1:{i:0;s:1:"*";}s:31:"urn:mace:dir:attribute-def:mail";a:1:{i:0;s:1:"*";}s:37:"urn:mace:dir:attribute-def:isMemberOf";a:1:{i:0;s:22:"urn:collab:org:surf.nl";}s:30:"urn:mace:dir:attribute-def:uid";a:1:{i:0;s:1:"*";}}'),
  (4, 2, 'https://default-sp', 0, 'prodaccepted', 'saml20-sp', 'yes', 'yes', 'N;'),
  (5, 2, 'https://default-sp', 1, 'prodaccepted', 'saml20-sp', 'yes', 'yes', 'N;'),
  (6, 2, 'https://default-sp', 2, 'prodaccepted', 'saml20-sp', 'yes', 'yes', 'a:0:{}'),
  (7, 3, 'https://mock-idp', 0, 'prodaccepted', 'saml20-idp', 'no', 'yes', null),
  (8, 3, 'https://mock-idp', 1, 'prodaccepted', 'saml20-idp', 'no', 'yes', null),
  (9, 3, 'https://mock-idp', 2, 'prodaccepted', 'saml20-idp', 'no', 'yes', null),
  (10, 4, 'https://default-idp', 0, 'prodaccepted', 'saml20-idp', 'yes', 'yes', 'N;'),
  (11, 4, 'https://default-idp', 1, 'prodaccepted', 'saml20-idp', 'yes', 'yes', 'N;'),
  (12, 4, 'https://default-idp', 2, 'prodaccepted', 'saml20-idp', 'yes', 'yes', 'N;');

INSERT INTO `janus__metadata` (`connectionRevisionId`, `key`, `value`)
VALUES
  (2, 'name:en', 'Old Mock SP'),
  (2, 'name:nl', 'Old Mock SP'),
  (2, 'description:en', 'Old Mock SP description'),
  (2, 'description:nl', 'Old Mock SP description'),
  (3, 'name:en', 'Mock SP'),
  (3, 'name:nl', 'Mock SP'),
  (3, 'description:en', 'Mock SP description'),
  (3, 'description:nl', 'Mock SP description'),
  (3, 'coin:policy_enforcement_decision_required', '1'),
  (3, 'coin:attribute_aggregation_required', '0'),
  (3, 'coin:institution_id', 'mock'),
  (3, 'contacts:0:contactType', 'technical'),
  (3, 'contacts:0:emailAddress', 'technical@openconext.org'),
  (3, 'contacts:0:givenName', 'Support'),
  (3, 'contacts:0:surName', 'OpenConext'),
  (3, 'contacts:1:contactType', 'support'),
  (3, 'contacts:1:emailAddress', 'support@openconext.org'),
  (3, 'contacts:1:givenName', 'Support'),
  (3, 'contacts:1:surName', 'OpenConext'),
  (3, 'contacts:2:contactType', 'administrative'),
  (3, 'contacts:2:emailAddress', 'administrative@openconext.org'),
  (3, 'contacts:2:givenName', 'Support'),
  (3, 'contacts:2:surName', 'OpenConext'),
  (6, 'name:en', 'Default SP'),
  (6, 'name:nl', 'Default SP'),
  (6, 'description:en', 'Default SP description'),
  (6, 'description:nl', 'Default SP description'),
  (6, 'coin:ss:idp_visible_only', '1'),
  (9, 'name:en', 'Mock IDP'),
  (9, 'name:nl', 'Mock IDP'),
  (9, 'description:en', 'Mock IDP description'),
  (9, 'description:nl', 'Mock IDP description'),
  (9, 'coin:institution_id', 'mock'),
  (12, 'name:en', 'Default IDP'),
  (12, 'name:nl', 'Default IDP'),
  (12, 'description:en', 'Default IDP description'),
  (12, 'description:nl', 'Default IDP description');

INSERT INTO `janus__allowedConnection` (`connectionRevisionId`, `remoteeid`)
VALUES
  (9, 2),
  (9, 1);

