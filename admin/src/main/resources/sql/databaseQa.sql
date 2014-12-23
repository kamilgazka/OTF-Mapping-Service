--
-- This script verifies all known assumptions about data conditions
--

--
-- Referential Integrity
--


--
-- Terminology / Terminology
--

--<property>
--<name>Validate concept default preferred names - SNOMEDCT</name>
--<value>
SELECT terminologyId, defaultPreferredName
FROM concepts a
WHERE terminology = 'SNOMEDCT' AND NOT EXISTS
  (SELECT * FROM descriptions b, language_refset_members c
   WHERE a.id = b.concept_id
     AND b.active = 1 AND c.active = 1
     AND b.id = c.description_id
     AND b.typeId='900000000000003001'
     AND c.refSetId = '900000000000509007'
     AND c.acceptabilityId = '900000000000548007'
     AND a.defaultPreferredName = b.term);
--</value>
--</property>

--<property>
--<name>Validate concept default preferred names - not in SNOMEDCT</name>
--<value>
SELECT terminologyId, defaultPreferredName
FROM concepts a
WHERE terminology != 'SNOMEDCT' AND NOT EXISTS
  (SELECT * FROM descriptions b
   WHERE a.id = b.concept_id
     AND a.defaultPreferredName = b.term);
--</value>
--</property>
     
--<property>
--<name>Validate active concepts have tree positions</name>
--<value>
SELECT terminologyId, defaultPreferredName
FROM concepts a
WHERE active = 1 AND terminology='SNOMEDCT' AND
  NOT EXISTS 
    (SELECT * FROM tree_positions b 
     WHERE a.terminologyId = b.terminologyId
       AND a.terminology = b.terminology
       AND a.terminologyVersion = b.terminologyVersion);
--</value>
--</property>


--
-- MapObjects / Terminology
--


--<property>
--<name>Validate map project source terminology exists</name>
--<value>
SELECT a.id, a.name
FROM map_projects a, a.sourceTerminology
WHERE NOT EXISTS
  (SELECT * FROM concepts b
   WHERE a.sourceTerminology = b.terminology
     AND a.sourceTerminologyVersion = b.terminologyVersion);
--</value>
--</property>

--<property>
--<name>Validate map project destination terminology exists</name>
--<value>
SELECT a.id, a.name, a.destinationTerminology
FROM map_projects a
WHERE NOT EXISTS
  (SELECT * FROM concepts b
   WHERE a.destinationTerminology = b.terminology
     AND a.destinationTerminologyVersion = b.terminologyVersion);
--</value>
--</property>

--<property>
--<name>Map project refSetId/refSetName exist in concepts</name>
--<value>
SELECT a.id, a.name, refSetId, refSetName
FROM map_projects a
WHERE NOT EXISTS
  (SELECT * FROM concepts b
   WHERE a.refSetId = b.terminologyId
     AND a.refSetName = b.defaultPreferredName);
--</value>
--</property>
   
--<property>
--<name>Map project scope concepts should exist</name>
--<value>
SELECT a.id, a.name
FROM map_projects a, map_projects_scope_concepts b
WHERE a.id = b.id 
  AND NOT EXISTS
    (SELECT * FROM concepts c
     WHERE b.scopeConcepts = c.terminologyId
       AND a.sourceTerminology = c.terminology
       AND a.sourceTerminologyVersion = c.terminologyVersion);
--</value>
--</property>


--<property>
--<name>Map project scope excluded concepts should exist</name>
--<value>
SELECT a.id, a.name
FROM map_projects a, map_projects_scope_excluded_concepts b
WHERE a.id = b.id 
  AND NOT EXISTS
    (SELECT * FROM concepts c
     WHERE b.scopeExcludedConcepts = c.terminologyId
       AND a.sourceTerminology = c.terminology
       AND a.sourceTerminologyVersion = c.terminologyVersion);
--</value>
--</property>

--<property>
--<name>Every map record should have a valid concept</name>
--<value>
SELECT b.id, a.conceptId
FROM map_records a, map_projects b
WHERE mapProjectId = b.id
  AND NOT EXISTS
    (SELECT * FROM concepts c 
     WHERE a.conceptId = c.terminologyId
       AND b.sourceTerminology = c.terminology
       AND b.sourceTerminologyVersion = c.terminologyVersion);      
--</value>
--</property>

--<property>
--<name>Every map entry should have a valid targetId (if not null or empty)</name>
--<value>
SELECT c.id, a.targetId
FROM map_entries a, map_records b, map_projects c
WHERE a.mapRecord_id = b.id 
  AND b.mapProjectId = c.id
  AND targetId != ''
  AND NOT EXISTS
    (SELECT * FROM concepts d 
     WHERE a.targetId = d.terminologyId
       AND c.destinationTerminology = d.terminology
       AND c.destinationTerminologyVersion = d.terminologyVersion);   
--</value>
--</property>

--
-- MapObjects / Feedback
--

--<property>
--<name>Every feedback should belong to a legitimate project</name>
--<value>
SELECT terminologyId, mapProjectId
FROM feedback_conversations
WHERE mapProjectId NOT IN (SELECT id FROM map_projects);
--</value>
--</property>

--<property>
--<name>Every feedback should belong to a legitimate record</name>
--<value>
SELECT terminologyId, mapProjectId, mapRecordId, terminology, terminologyVersion
FROM feedback_conversations a
WHERE NOT EXISTS
  (SELECT * FROM map_records_AUD b, map_projects c
   WHERE a.terminologyId = b.conceptId
     AND a.terminology = c.sourceTerminology
     AND a.terminologyVersion = c.sourceTerminologyVersion
     AND a.mapRecordId = b.id
     AND b.mapProjectId = c.id
     AND a.mapProjectId = b.mapProjectId);
--</value>
--</property>

--<property>
--<name>A map record should have only a single feedback conversation</name>
--<value>
SELECT mapProjectId, mapRecordId FROM feedback_conversations
GROUP BY mapProjectId, mapRecordId HAVING count(*)>1;
--</value>
--</property>

--
-- MapObjects / Reports
--

--<property>
--<name>No orphaned report definitions</name>
--<value>
SELECT id, name FROM report_definitions a
WHERE NOT EXISTS
  (SELECT * FROM map_projects_report_definitions b WHERE a.id = b.reportDefinitions_id);
--</value>
--</property>

-- Report result items should exist
--  Not necessarily true because concepts or map records could now be gone
  
--
-- MapObjects / MapObjects
--

--<property>
--<name>Map record origins should exist</name>
--<value>
SELECT * FROM map_records_origin_ids a
WHERE NOT EXISTS
  (SELECT * FROM map_records_AUD b
   WHERE a.originIds = b.id);
--</value>
--</property>


--<property>
--<name>No orphaned advice</name>
--<value>
SELECT id, name FROM map_advices a
WHERE NOT EXISTS
  (SELECT * FROM map_projects_map_advices b WHERE a.id = b.mapAdvices_id);
--</value>
--</property>

--<property>
--<name>No orphaned principles</name>
--<value>
SELECT id, name FROM map_principles a
WHERE NOT EXISTS
  (SELECT * FROM map_projects_map_principles b WHERE a.id = b.mapPrinciples_id);
--</value>
--</property>

--<property>
--<name>No orphaned relations</name>
--<value>
SELECT id, name FROM map_relations a
WHERE NOT EXISTS
  (SELECT * FROM map_projects_map_relations b WHERE a.id = b.mapRelations_id);
--</value>
--</property>

--<property>
--<name>No orphaned preset age ranges</name>
--<value>
SELECT id, name FROM map_age_ranges a
WHERE NOT EXISTS
  (SELECT * FROM map_projects_map_age_ranges b WHERE a.id = b.presetAgeRanges_id);
--</value>
--</property>

--<property>
--<name>No orphaned users</name>
--<value>
SELECT id, userName FROM map_users a
WHERE NOT EXISTS
  (SELECT * FROM map_projects_map_specialists b WHERE a.id = b.map_users_id
  UNION
  SELECT * FROM map_projects_map_leads b WHERE a.id = b.map_users_id
  UNION
  SELECT * FROM map_projects_map_administrators b WHERE a.id = b.map_users_id)
AND userName NOT IN ('default','guest','legacy','loader','qa');
--</value>
--</property>

--
-- Workflow / MapObjects
--

--<property>
--<name>Workflow exceptions have valid projects</name>
--<value>
SELECT * FROM workflow_exceptions a
WHERE NOT EXISTS
  (SELECT * FROM map_projects b WHERE b.id = a.mapProjectId);
--</value>
--</property>

--<property>
--<name>PUBLISHED or READY_FOR_PUBLICATION map records do not have tracking records</name>
--<value>
SELECT mapProjectId, id, workflowStatus
FROM map_records a
WHERE workflowStatus in ('PUBLISHED', 'READY_FOR_PUBLICATION')
  AND EXISTS
    (SELECT * FROM tracking_records b
     WHERE a.mapProjectId = b.mapProjectId
       AND a.conceptId = b.terminologyId);
--</value>
--</property>

--<property>
--<name>Non-PUBLISHED and non-READY_FOR_PUBLICATION map records always have a tracking record</name>
--<value>
SELECT mapProjectId, id, workflowStatus
FROM map_records a
WHERE workflowStatus not in ('PUBLISHED', 'READY_FOR_PUBLICATION')
  AND EXISTS
    (SELECT * FROM tracking_records b
     WHERE a.mapProjectId = b.mapProjectId
       AND a.conceptId = b.terminologyId);
--</value>
--</property>


--
-- Content validation
--

--<property>
--<name>Map entries with null instead of blank target code</name>
--<value>
SELECT b.conceptId, b.mapProjectId
FROM map_entries a, map_records b
WHERE a.mapRecord_id = b.id
  AND targetId is null;
--</value>
--</property>

--<property>
--<name>Null target has only acceptable map relations</name>
--<value>
SELECT d.id, c.conceptId, b.name
FROM map_entries a, map_relations b, map_records c, map_projects d
WHERE a.mapRelation_id = b.id
  AND a.mapRecord_id = c.id
  AND c.mapProjectId = d.id
  AND a. targetId = ''
  AND isAllowableForNullTarget != 1;
--</value>
--</property>

--<property>
--<name>Non null target has only acceptable map relations</name>
--<value>
SELECT d.id, c.conceptId, b.name
FROM map_entries a, map_relations b, map_records c, map_projects d
WHERE a.mapRelation_id = b.id
  AND a.mapRecord_id = c.id
  AND c.mapProjectId = d.id
  AND a. targetId != '' AND a.targetId IS NOT null
  AND isAllowableForNullTarget != 0;
--</value>
--</property>

--<property>
--<name>Null target has only acceptable map advices</name>
--<value>
SELECT e.id, d.conceptId, b.name 
FROM map_entries a, map_advices b, map_entries_map_advices c,
     map_records d, map_projects e
WHERE a.id = c.map_entries_id
  AND b.id = c.mapAdvices_id
  AND a.mapRecord_id = d.id
  AND d.mapProjectId = e.id
  AND a. targetId = ''
  AND b.isAllowableForNullTarget != 1;  
--</value>
--</property>

--<property>
--<name>Non null target has only acceptable map advices</name>
--<value>
SELECT e.id, d.conceptId, b.name 
FROM map_entries a, map_advices b, map_entries_map_advices c,
     map_records d, map_projects e
WHERE a.id = c.map_entries_id
  AND b.id = c.mapAdvices_id
  AND a.mapRecord_id = d.id
  AND d.mapProjectId = e.id
  AND a.targetId != '' AND a.targetId IS NOT NULL
  AND b.isAllowableForNullTarget != 0;  
--</value>
--</property>

