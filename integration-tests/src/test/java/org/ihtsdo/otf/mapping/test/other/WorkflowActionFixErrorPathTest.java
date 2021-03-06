package org.ihtsdo.otf.mapping.test.other;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.ihtsdo.otf.mapping.helpers.MapRefsetPattern;
import org.ihtsdo.otf.mapping.helpers.MapUserRole;
import org.ihtsdo.otf.mapping.helpers.RelationStyle;
import org.ihtsdo.otf.mapping.helpers.ValidationResult;
import org.ihtsdo.otf.mapping.helpers.ValidationResultJpa;
import org.ihtsdo.otf.mapping.helpers.WorkflowAction;
import org.ihtsdo.otf.mapping.helpers.WorkflowStatus;
import org.ihtsdo.otf.mapping.helpers.WorkflowType;
import org.ihtsdo.otf.mapping.jpa.MapProjectJpa;
import org.ihtsdo.otf.mapping.jpa.MapRecordJpa;
import org.ihtsdo.otf.mapping.jpa.MapUserJpa;
import org.ihtsdo.otf.mapping.jpa.handlers.WorkflowFixErrorPathHandler;
import org.ihtsdo.otf.mapping.jpa.services.ContentServiceJpa;
import org.ihtsdo.otf.mapping.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.mapping.model.MapProject;
import org.ihtsdo.otf.mapping.model.MapRecord;
import org.ihtsdo.otf.mapping.model.MapUser;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.mapping.services.ContentService;
import org.ihtsdo.otf.mapping.services.WorkflowService;
import org.ihtsdo.otf.mapping.workflow.TrackingRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for workflow actions fix error path.
 */
@Ignore("Workflow integration testing outdated after workflow revision")
public class WorkflowActionFixErrorPathTest {

  // the content
  /** The concept. */
  private static Concept concept;

  // the mapping objects
  /** The loader. */
  private static MapUser viewer;

  /** The specialist. */
  private static MapUser specialist;

  /** The lead. */
  private static MapUser lead;

  /** The loader. */
  private static MapUser loader;

  /** The lead record. */
  private static MapRecord revisionRecord;

  /** The spec record. */
  private static MapRecord specRecord;

  /** The lead record. */
  private static MapRecord leadRecord;

  /** The map project. */
  private static MapProject mapProject;

  // the tracking record
  /** The tracking record. */
  private static TrackingRecord trackingRecord;

  // the services
  /** The content service. */
  private static ContentService contentService;

  /** The workflow service. */
  private static WorkflowService workflowService;

  // the workflow handler
  /** The handler. */
  private static WorkflowFixErrorPathHandler handler;

  /**
   * Inits the.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void init() throws Exception {

    // instantiate the services
    contentService = new ContentServiceJpa();
    workflowService = new WorkflowServiceJpa();

    // instantiate the workflow handler
    handler = new WorkflowFixErrorPathHandler();

    // ensure database is clean
    for (Concept c : contentService.getConcepts().getIterable())
      contentService.removeConcept(c.getId());

    for (MapProject mp : workflowService.getMapProjects().getIterable())
      workflowService.removeMapProject(mp.getId());

    for (MapUser mu : workflowService.getMapUsers().getIterable()) {
      if (!mu.getUserName().equals("guest")
          && !mu.getUserName().equals("loader")
          && !mu.getUserName().equals("qa")) {
        workflowService.removeMapUser(mu.getId());
      }
    }

    for (TrackingRecord tr : workflowService.getTrackingRecords().getIterable())
      workflowService.removeTrackingRecord(tr.getId());

    concept = new ConceptJpa();
    concept.setActive(true);
    concept.setDefaultPreferredName("Test Concept");
    concept.setDefinitionStatusId(0L);
    concept.setEffectiveTime(new Date());
    concept.setModuleId(0L);
    concept.setTerminology("sourceTerminology");
    concept.setTerminologyVersion("sourceTerminologyVersion");
    concept.setTerminologyId("1");
    contentService.addConcept(concept);

    // instantiate and add the users
    viewer = new MapUserJpa();
    viewer.setApplicationRole(MapUserRole.VIEWER);
    viewer.setEmail("none");
    viewer.setName("Viewer");
    viewer.setUserName("view");
    workflowService.addMapUser(viewer);

    specialist = new MapUserJpa();
    specialist.setApplicationRole(MapUserRole.VIEWER);
    specialist.setEmail("none");
    specialist.setName("Specialist");
    specialist.setUserName("spec");
    workflowService.addMapUser(specialist);

    lead = new MapUserJpa();
    lead.setApplicationRole(MapUserRole.VIEWER);
    lead.setEmail("none");
    lead.setName("Lead");
    lead.setUserName("lead");
    workflowService.addMapUser(lead);

    loader = workflowService.getMapUser("loader");

    // instantiate the project
    mapProject = new MapProjectJpa();
    mapProject.setSourceTerminology("sourceTerminology");
    mapProject.setSourceTerminologyVersion("sourceTerminologyVersion");
    mapProject.setDestinationTerminology("destinationTerminology");
    mapProject
        .setDestinationTerminologyVersion("destinationTerminologyVersion");
    mapProject.setGroupStructure(false);
    mapProject.setMapRefsetPattern(MapRefsetPattern.ExtendedMap);
    mapProject.setMapRelationStyle(RelationStyle.MAP_CATEGORY_STYLE);
    mapProject.setName("Test Project");
    mapProject.setPropagatedFlag(false);
    mapProject
        .setProjectSpecificAlgorithmHandlerClass("org.ihtsdo.otf.mapping.jpa.handlers.ICD10ProjectSpecificAlgorithmHandler");
    mapProject.setPublic(true);
    mapProject.setRefSetId("refsetId");
    mapProject.setRuleBased(true);
    mapProject.setWorkflowType(WorkflowType.REVIEW_PROJECT);
    mapProject.addMapSpecialist(specialist);
    mapProject.addMapLead(lead);
    mapProject.addScopeConcept("1");
    workflowService.addMapProject(mapProject);

    // compute the workflow
    workflowService.computeWorkflow(mapProject);

  }

  /**
   * Test specialist editing state.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSpecialistEditingState() throws Exception {

    // same test for both NEW and EDITING_IN_PROGRESS
    for (WorkflowStatus status : Arrays.asList(WorkflowStatus.NEW,
        WorkflowStatus.EDITING_IN_PROGRESS)) {

      // clear existing records
      clearMapRecords();

      // create revision and specialist record
      revisionRecord = createRecord(loader, WorkflowStatus.REVISION);
      workflowService.addMapRecord(revisionRecord);

      specRecord = createRecord(specialist, status);
      workflowService.addMapRecord(specRecord);

      // compute workflow
      getTrackingRecord();

      // Test: assign viewer
      ValidationResult result = testAllActionsForUser(viewer);

      // all actions except cancel should fail
      for (WorkflowAction action : WorkflowAction.values()) {
        switch (action) {
          case ASSIGN_FROM_INITIAL_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case ASSIGN_FROM_SCRATCH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case CANCEL:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case CREATE_QA_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case FINISH_EDITING:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case PUBLISH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case SAVE_FOR_LATER:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case UNASSIGN:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          default:
            break;

        }
      }

      // Test: Specialist
      result = testAllActionsForUser(specialist);

      // all actions but SAVE_FOR_LATER, FINISH_EDITING, UNASSIGN should fail
      for (WorkflowAction action : WorkflowAction.values()) {
        switch (action) {
          case ASSIGN_FROM_INITIAL_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case ASSIGN_FROM_SCRATCH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case CANCEL:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case CREATE_QA_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case FINISH_EDITING:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case PUBLISH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case SAVE_FOR_LATER:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case UNASSIGN:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          default:
            break;

        }
      }

      // Test: assign lead
      result = testAllActionsForUser(lead);

      // all actions but CANCEL should fail
      for (WorkflowAction action : WorkflowAction.values()) {
        switch (action) {
          case ASSIGN_FROM_INITIAL_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case ASSIGN_FROM_SCRATCH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case CANCEL:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case CREATE_QA_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case FINISH_EDITING:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case PUBLISH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case SAVE_FOR_LATER:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case UNASSIGN:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          default:
            break;

        }
      }
    }

  }

  /**
   * Test specialist finished state.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSpecialistFinishedState() throws Exception {

    // clear existing records
    clearMapRecords();

    // create revision and specialist record
    revisionRecord = createRecord(loader, WorkflowStatus.REVISION);
    workflowService.addMapRecord(revisionRecord);

    specRecord = createRecord(specialist, WorkflowStatus.REVIEW_NEEDED);
    workflowService.addMapRecord(specRecord);

    // compute workflow
    getTrackingRecord();

    // Test: assign viewer
    ValidationResult result = testAllActionsForUser(viewer);

    // all actions except cancel should fail
    for (WorkflowAction action : WorkflowAction.values()) {
      switch (action) {
        case ASSIGN_FROM_INITIAL_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case ASSIGN_FROM_SCRATCH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case CANCEL:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CREATE_QA_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case FINISH_EDITING:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case PUBLISH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case SAVE_FOR_LATER:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case UNASSIGN:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        default:
          break;

      }
    }

    // Test: Specialist
    result = testAllActionsForUser(specialist);

    // all actions but CANCEL, SAVE_FOR_LATER, FINISH_EDITING, UNASSIGN should
    // fail
    for (WorkflowAction action : WorkflowAction.values()) {
      switch (action) {
        case ASSIGN_FROM_INITIAL_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case ASSIGN_FROM_SCRATCH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case CANCEL:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CREATE_QA_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case FINISH_EDITING:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case PUBLISH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case SAVE_FOR_LATER:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case UNASSIGN:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        default:
          break;

      }
    }

    // Test: assign lead
    result = testAllActionsForUser(lead);

    // all actions but CANCEL and ASSIGN_FROM_SCRATCH should fail
    for (WorkflowAction action : WorkflowAction.values()) {
      switch (action) {
        case ASSIGN_FROM_INITIAL_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case ASSIGN_FROM_SCRATCH:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CANCEL:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CREATE_QA_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case FINISH_EDITING:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case PUBLISH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case SAVE_FOR_LATER:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case UNASSIGN:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        default:
          break;

      }
    }

  }

  /**
   * Test lead editing state.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLeadEditingState() throws Exception {

    for (WorkflowStatus status : Arrays.asList(WorkflowStatus.REVIEW_NEW,
        WorkflowStatus.REVIEW_IN_PROGRESS)) {

      // clear existing records
      clearMapRecords();

      // create revision, specialist, and lead record
      revisionRecord = createRecord(loader, WorkflowStatus.REVISION);
      workflowService.addMapRecord(revisionRecord);

      specRecord = createRecord(specialist, WorkflowStatus.REVIEW_NEEDED);
      workflowService.addMapRecord(specRecord);

      leadRecord = createRecord(lead, status);
      workflowService.addMapRecord(leadRecord);

      // compute workflow
      getTrackingRecord();

      // Test: assign viewer
      ValidationResult result = testAllActionsForUser(viewer);

      // all actions except cancel should fail
      for (WorkflowAction action : WorkflowAction.values()) {
        switch (action) {
          case ASSIGN_FROM_INITIAL_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case ASSIGN_FROM_SCRATCH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case CANCEL:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case CREATE_QA_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case FINISH_EDITING:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case PUBLISH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case SAVE_FOR_LATER:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case UNASSIGN:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          default:
            break;

        }
      }

      // Test: Specialist
      result = testAllActionsForUser(specialist);

      // all actions but CANCEL should fail
      for (WorkflowAction action : WorkflowAction.values()) {
        switch (action) {
          case ASSIGN_FROM_INITIAL_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case ASSIGN_FROM_SCRATCH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case CANCEL:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case CREATE_QA_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case FINISH_EDITING:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case PUBLISH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case SAVE_FOR_LATER:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case UNASSIGN:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          default:
            break;

        }
      }

      // Test: assign lead
      result = testAllActionsForUser(lead);

      // all actions but CANCEL, SAVE_FOR_LATER, FINISH_EDITING, UNASSIGN should
      // fail
      for (WorkflowAction action : WorkflowAction.values()) {
        switch (action) {
          case ASSIGN_FROM_INITIAL_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case ASSIGN_FROM_SCRATCH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case CANCEL:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case CREATE_QA_RECORD:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case FINISH_EDITING:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case PUBLISH:
            assertTrue(result.getErrors().contains(action.toString()));
            break;
          case SAVE_FOR_LATER:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          case UNASSIGN:
            assertTrue(result.getMessages().contains(action.toString()));
            break;
          default:
            break;

        }
      }
    }

  }

  /**
   * Test lead finished state.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLeadFinishedState() throws Exception {

    // clear existing records
    clearMapRecords();

    // create revision, specialist, and lead record
    revisionRecord = createRecord(loader, WorkflowStatus.REVISION);
    workflowService.addMapRecord(revisionRecord);

    specRecord = createRecord(specialist, WorkflowStatus.REVIEW_NEEDED);
    workflowService.addMapRecord(specRecord);

    leadRecord = createRecord(lead, WorkflowStatus.REVIEW_RESOLVED);
    workflowService.addMapRecord(leadRecord);

    // compute workflow
    getTrackingRecord();

    // Test: assign viewer
    ValidationResult result = testAllActionsForUser(viewer);

    // all actions except cancel should fail
    for (WorkflowAction action : WorkflowAction.values()) {
      switch (action) {
        case ASSIGN_FROM_INITIAL_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case ASSIGN_FROM_SCRATCH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case CANCEL:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CREATE_QA_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case FINISH_EDITING:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case PUBLISH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case SAVE_FOR_LATER:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case UNASSIGN:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        default:
          break;

      }
    }

    // Test: Specialist
    result = testAllActionsForUser(specialist);

    // all actions but CANCEL should fail
    for (WorkflowAction action : WorkflowAction.values()) {
      switch (action) {
        case ASSIGN_FROM_INITIAL_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case ASSIGN_FROM_SCRATCH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case CANCEL:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CREATE_QA_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case FINISH_EDITING:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case PUBLISH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case SAVE_FOR_LATER:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case UNASSIGN:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        default:
          break;

      }
    }

    // Test: assign lead
    result = testAllActionsForUser(lead);

    // all actions but CANCEL, SAVE_FOR_LATER, FINISH_EDITING, UNASSIGN, PUBLISH
    // should fail
    for (WorkflowAction action : WorkflowAction.values()) {
      switch (action) {
        case ASSIGN_FROM_INITIAL_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case ASSIGN_FROM_SCRATCH:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case CANCEL:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case CREATE_QA_RECORD:
          assertTrue(result.getErrors().contains(action.toString()));
          break;
        case FINISH_EDITING:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case PUBLISH:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case SAVE_FOR_LATER:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        case UNASSIGN:
          assertTrue(result.getMessages().contains(action.toString()));
          break;
        default:
          break;

      }
    }

  }

  /**
   * Cleanup.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void cleanup() throws Exception {

    workflowService.clearWorkflowForMapProject(mapProject);

    if (revisionRecord != null)
      workflowService.removeMapRecord(revisionRecord.getId());
    if (specRecord != null)
      workflowService.removeMapRecord(specRecord.getId());
    if (leadRecord != null)
      workflowService.removeMapRecord(leadRecord.getId());

    workflowService.removeMapProject(mapProject.getId());
    workflowService.removeMapUser(specialist.getId());
    workflowService.removeMapUser(lead.getId());
    workflowService.close();

    contentService.removeConcept(concept.getId());
    contentService.close();

  }

  /**
   * Returns the tracking record.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void getTrackingRecord() throws Exception {
    workflowService.computeWorkflow(mapProject);
    Thread.sleep(1000);
    trackingRecord = workflowService.getTrackingRecord(mapProject, concept);
  }

  /**
   * Clear map records.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void clearMapRecords() throws Exception {
    for (MapRecord mr : workflowService.getMapRecords().getIterable()) {
      workflowService.removeMapRecord(mr.getId());
    }
    revisionRecord = null;
    specRecord = null;
    leadRecord = null;
    Thread.sleep(500);
  }

  /**
   * Test all actions for user.
   *
   * @param user the user
   * @return the validation result
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ValidationResult testAllActionsForUser(MapUser user) throws Exception {
    ValidationResult result = new ValidationResultJpa();

    for (WorkflowAction action : WorkflowAction.values()) {
      ValidationResult actionResult =
          handler.validateTrackingRecordForActionAndUser(trackingRecord,
              action, user);
      if (actionResult.isValid()) {
        result.addMessage(action.toString());
      } else {
        result.addError(action.toString());
      }
    }
    return result;
  }

  /**
   * Creates the record.
   *
   * @param user the user
   * @param status the status
   * @return the map record
   */
  @SuppressWarnings("static-method")
  private MapRecord createRecord(MapUser user, WorkflowStatus status) {
    MapRecord record = new MapRecordJpa();

    record.setConceptId(concept.getTerminologyId());
    record.setConceptName(concept.getDefaultPreferredName());
    record.setLastModified(new Date().getTime());
    record.setLastModifiedBy(user);
    record.setMapProjectId(mapProject.getId());
    record.setOwner(user);
    record.setTimestamp(new Date().getTime());
    record.setWorkflowStatus(status);
    return record;
  }

}
