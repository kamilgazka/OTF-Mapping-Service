/*
 * 
 */
package org.ihtsdo.otf.mapping.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.mapping.helpers.PfsParameter;
import org.ihtsdo.otf.mapping.helpers.PfsParameterJpa;
import org.ihtsdo.otf.mapping.helpers.SearchResult;
import org.ihtsdo.otf.mapping.helpers.SearchResultJpa;
import org.ihtsdo.otf.mapping.helpers.SearchResultList;
import org.ihtsdo.otf.mapping.helpers.SearchResultListJpa;
import org.ihtsdo.otf.mapping.helpers.WorkflowStatus;
import org.ihtsdo.otf.mapping.jpa.MapRecordList;
import org.ihtsdo.otf.mapping.jpa.services.ContentServiceJpa;
import org.ihtsdo.otf.mapping.jpa.services.MappingServiceJpa;
import org.ihtsdo.otf.mapping.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.mapping.model.MapProject;
import org.ihtsdo.otf.mapping.model.MapRecord;
import org.ihtsdo.otf.mapping.model.MapUser;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.services.ContentService;
import org.ihtsdo.otf.mapping.services.MappingService;
import org.ihtsdo.otf.mapping.services.WorkflowService;
import org.ihtsdo.otf.mapping.workflow.WorkflowTrackingRecord;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * The Workflow Services REST package
 */
@Path("/workflow")
@Api(value = "/workflow", description = "Operations supporting workflow.")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@SuppressWarnings("static-method")
public class WorkflowServiceRest {

	/**
	 * Instantiates an empty {@link WorkflowServiceRest}.
	 */
	public WorkflowServiceRest() {
	  // do nothing
	}


	/**
	 * Compute workflow.
	 *
	 * @param mapProjectId the map project id
	 */
	@POST
	@Path("/project/id/{id:[0-9][0-9]*}")
	@ApiOperation(value = "Compute workflow for project by id", notes = "Computes workflow given a project id.")
	public void computeWorkflow(
			@ApiParam(value = "Id of map project to fetch", required = true) @PathParam("id") Long mapProjectId) {

		Logger.getLogger(WorkflowServiceRest.class).info("RESTful call (Workflow): /project/id/" + mapProjectId.toString());

		try {
			MappingService mappingService = new MappingServiceJpa();
			MapProject mapProject = mappingService.getMapProject(mapProjectId);
			mappingService.close();
			WorkflowService workflowService = new WorkflowServiceJpa();
			workflowService.computeWorkflow(mapProject);
			workflowService.close();
			return;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Finds available work for the specified map project and user.
	 *
	 * @param mapProjectId the map project id
	 * @param userId the user id
	 * @param pfsParameter the paging parameter
	 * @return the search result list
	 */
	@POST
	@Path("/work/projectId/{id:[0-9][0-9]*}/userId/{userid:[0-9][0-9]*}")
	@ApiOperation(value = "Get available work.", notes = "Returns available work for a given user on a given map project.", response = SearchResultList.class)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public SearchResultList getAvailableWork(
			@ApiParam(value = "Id of map project", required = true) @PathParam("id") Long mapProjectId, 
			@ApiParam(value = "Id of map user", required = true) @PathParam("userid") Long userId,
			@ApiParam(value = "Paging/filtering/sorting parameter object", required = true) PfsParameterJpa pfsParameter) {
		try {

			// retrieve the project and user
			MappingService mappingService = new MappingServiceJpa();

			MapProject project = mappingService.getMapProject(mapProjectId);
			MapUser user = mappingService.getMapUser(userId);

			mappingService.close();

			// retrieve the workflow and tracking records
			WorkflowService workflowService = new WorkflowServiceJpa();
	
			SearchResultList trackingRecordsList = workflowService.findAvailableWork(project, user, pfsParameter);

			workflowService.close();

			return trackingRecordsList;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Finds available work for the specified map project and user.
	 *
	 * @param mapProjectId the map project id
	 * @param userId the user id
	 * @return the search result list
	 */
	@GET
	@Path("/work/id/{id:[0-9][0-9]*}/user/{userid:[0-9][0-9]*}")
	@ApiOperation(value = "Find available work.", notes = "Returns available work for a given user on a given map project.", response = SearchResultList.class)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public SearchResultList findAvailableWork(
			@ApiParam(value = "Id of map project", required = true) @PathParam("id") Long mapProjectId, 
			@ApiParam(value = "Id of map user", required = true) @PathParam("userid") Long userId) {
		try {
			MappingService mappingService = new MappingServiceJpa();
			MapProject project = mappingService.getMapProject(mapProjectId);
			MapUser user = mappingService.getMapUser(userId);
			mappingService.close();

			WorkflowService workflowService = new WorkflowServiceJpa();

			SearchResultList searchResultList = new SearchResultListJpa();

			/** call getWorkflow and get the tracking records for unmapped in scope concepts.*/
			Set<WorkflowTrackingRecord> trackingRecords = workflowService.getTrackingRecordsForUnmappedInScopeConcepts(project);
			for (WorkflowTrackingRecord trackingRecord : trackingRecords) {
				/** don't add cases where there are 2 assigned users already */
				/** don't add cases where this specialist is already an assigned user */	
				if (trackingRecord.getAssignedUsers().size() >= 2 ||
						trackingRecord.getAssignedUsers().contains(user)) {
					continue; 
				} else {
					SearchResult searchResult = new SearchResultJpa();
					searchResult.setTerminology(trackingRecord.getTerminology());
					searchResult.setTerminologyId(trackingRecord.getTerminologyId());
					searchResult.setTerminologyVersion(trackingRecord.getTerminologyVersion());
					searchResult.setValue(trackingRecord.getDefaultPreferredName());
					searchResultList.addSearchResult(searchResult);
				}   	
			}
			workflowService.close();

			return searchResultList;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Assign user to concept.
	 *
	 * @param mapProjectId the map project id
	 * @param terminologyId the terminology id
	 * @param userName the user name
	 * @return the map record
	 */
	@POST
	@Path("/assign/projectId/{id}/concept/{terminologyId}/user/{userName}")
	@ApiOperation(value = "Assign user to concept.", notes = "Assigns the given user to the given concept.", response = Response.class)
	@Produces({
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public MapRecord assignUserToConcept(
			@ApiParam(value = "Id of map project", required = true) @PathParam("id") String mapProjectId, 
			@ApiParam(value = "Id of concept", required = true) @PathParam("terminologyId") String terminologyId,
			@ApiParam(value = "String userName of user", required = true) @PathParam("userName") String userName) {
		try {
			WorkflowService workflowService = new WorkflowServiceJpa();
			MappingService mappingService = new MappingServiceJpa();
			ContentService contentService = new ContentServiceJpa();

			MapProject project = mappingService.getMapProject(new Long(mapProjectId));
			MapUser user = mappingService.getMapUser(userName);
			Concept concept = contentService.getConcept(terminologyId, project.getSourceTerminology(), 
					project.getSourceTerminologyVersion());

			MapRecord mapRecord = workflowService.assignUserToConcept(project, concept, user);

			mappingService.close();
			workflowService.close();
			contentService.close();

			return mapRecord;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Assign user to concept.
	 *
	 * @param mapProjectId the map project id
	 * @param terminologyId the terminology id
	 * @param recordId the record id
	 * @param userName the user name
	 * @return the response
	 */
	@GET
	@Path("/assign/id/{id}/concept/{terminologyId}/record/{recordId}/user/{userName}")
	@ApiOperation(value = "Assign user to concept.", notes = "Assigns the given user to the given concept.", response = Response.class)
	@Produces({
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public Response assignUserToConcept(
			@ApiParam(value = "Id of map project", required = true) @PathParam("id") String mapProjectId, 
			@ApiParam(value = "Id of concept", required = true) @PathParam("terminologyId") String terminologyId, 
			@ApiParam(value = "Id of map record", required = true) @PathParam("recordId") String recordId,
			@ApiParam(value = "String userName of user", required = true) @PathParam("userName") String userName) {
		try {
			WorkflowService workflowService = new WorkflowServiceJpa();
			MappingService mappingService = new MappingServiceJpa();
			ContentService contentService = new ContentServiceJpa();
			MapProject project = mappingService.getMapProject(new Long(mapProjectId));
			MapUser user = mappingService.getMapUser(userName);
			MapRecord record = mappingService.getMapRecord(new Long(recordId));
			Concept concept = contentService.getConcept(terminologyId, project.getSourceTerminology(), 
					project.getSourceTerminologyVersion());

			workflowService.assignUserToConcept(project, concept, record, user);


			mappingService.close();
			workflowService.close();
			contentService.close();


		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		return null;
	}

	/**
	 * Assign batch to user.
	 *
	 * @param mapProjectId the map project id
	 * @param userName the user name
	 * @param terminologyIds the terminology ids
	 * @return the map record list
	 */
	@POST
	@Path("/assign/batch/projectId/{projectId}/user/{userName}")
	@ApiOperation(value = "Assign user to batch of concepts.", notes = "Assigns the given user to the given concept.", response = Response.class)
	@Produces({
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public Response assignBatchToUser(
			@ApiParam(value = "Id of map project", required = true) @PathParam("projectId") String mapProjectId, 
			@ApiParam(value = "String userName of user", required = true) @PathParam("userName") String userName, 
			@ApiParam(value = "List of terminology ids to be assigned", required = true) List<String> terminologyIds) {

		Logger.getLogger(WorkflowServiceRest.class).info("RESTful call: assignBatchToUser");
		
		WorkflowService workflowService = new WorkflowServiceJpa();
		MappingService mappingService = new MappingServiceJpa();
		ContentService contentService = new ContentServiceJpa();


		try {
			MapProject project = mappingService.getMapProject(new Long(mapProjectId));
			MapUser user = mappingService.getMapUser(userName);

			for (String terminologyId : terminologyIds) {
				Concept concept = contentService.getConcept(terminologyId, project.getSourceTerminology(), 
						project.getSourceTerminologyVersion());

				workflowService.assignUserToConcept(project, concept, user);

				
			}

			mappingService.close();
			workflowService.close();
			contentService.close();

			return null;

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Unassign user to concept.
	 *
	 * @param mapProjectId the map project id
	 * @param terminologyId the terminology id
	 * @param userName the user name
	 */
	@POST
	@Path("/unassign/projectId/{projectId}/conceptId/{terminologyId}/user/{userName}")
	@ApiOperation(value = "Unssign user from a concept.", notes = "Assigns the given user to the given concept.", response = Response.class)
	@Produces({
		MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public void unassignUserToConcept(
			@ApiParam(value = "Id of map project", required = true) @PathParam("projectId") String mapProjectId, 
			@ApiParam(value = "Id of concept", required = true) @PathParam("terminologyId") String terminologyId,
			@ApiParam(value = "String userName of user", required = true) @PathParam("userName") String userName) {
		try {
			WorkflowService workflowService = new WorkflowServiceJpa();
			MappingService mappingService = new MappingServiceJpa();
			ContentService contentService = new ContentServiceJpa();

			MapProject project = mappingService.getMapProject(new Long(mapProjectId));
			MapUser user = mappingService.getMapUser(userName);
			Concept concept = contentService.getConcept(terminologyId, project.getSourceTerminology(), 
					project.getSourceTerminologyVersion());

			workflowService.unassignUserFromConcept(project, concept, user);
			
			mappingService.close();
			workflowService.close();
			contentService.close();


		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	 /**
 	 * Sets the workflow to editing done.
 	 *
 	 * @param mapRecordId the workflow to editing done
 	 */
 	@GET
  @Path("/set/done/{id:[0-9][0-9]*}")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })	
	@ApiOperation(value = "Set record to editing done", notes = "Updates the map record and sets workflow to editing done.")
  public boolean setWorkflowToEditingDone(
		@ApiParam(value = "Id of map record to fetch", required = true) @PathParam("id") Long mapRecordId) {

    try {

      MappingService mappingService = new MappingServiceJpa();
      MapRecord mapRecord = mappingService.getMapRecord(mapRecordId);
      
    	mapRecord.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
      
      mappingService.updateMapRecord(mapRecord);
      mappingService.close();
      return true;
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }

  }

	/**
	 * Sets the workflow to editing in progress.
	 *
	 * @param mapRecordId the workflow to editing in progress
	 */
	@GET
  @Path("/set/inProgress/{id:[0-9][0-9]*}")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })	
	@ApiOperation(value = "Set record to editing done", notes = "Updates the map record and sets workflow to editing in progress.")
  public boolean setWorkflowToEditingInProgress(
		@ApiParam(value = "Id of map record to fetch", required = true) @PathParam("id") Long mapRecordId) {

    try {

      MappingService mappingService = new MappingServiceJpa();
      MapRecord mapRecord = mappingService.getMapRecord(mapRecordId);
      
    	mapRecord.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
      
      mappingService.updateMapRecord(mapRecord);
      mappingService.close();
      return true;
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }

  }
  /*
	*//**
	 * Returns the records assigned to user.
	 *
	 * @param mapProjectId the map project id
	 * @param userName the user name
	 * @return the records assigned to user
	 *//*
	@GET
	@Path("/assigned/id/{id}/user/{user}")
	@ApiOperation(value = "Returns records assigned to given user.", notes = "Returns work assigned to a given user.", response = MapRecordList.class)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })	
	public MapRecordList getRecordsAssignedToUser(
			@ApiParam(value = "Id of map project", required = true) @PathParam("id") String mapProjectId, 
			@ApiParam(value = "UserName of user", required = true) @PathParam("user")  String userName) {
		MapRecordList assigned = new MapRecordList();
		try {
			WorkflowService workflowService = new WorkflowServiceJpa();
			MappingService mappingService = new MappingServiceJpa();
			MapProject project = mappingService.getMapProject(new Long(mapProjectId));
			MapUser user = mappingService.getMapUser(userName);

			Set<MapRecord> mapRecords = workflowService.getMapRecordsAssignedToUser(project, user);
			List<MapRecord> mapRecordsList = new ArrayList<>(mapRecords);
			assigned.setMapRecords(mapRecordsList);

			mappingService.close();
			workflowService.close();
			return assigned;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}*/
	
	/**
	 * Returns the paged/filtered records assigned to user.
	 *
	 * @param mapProjectId the map project id
	 * @param userName the user name
	 * @return the records assigned to user
	 */
	@POST
	@Path("/assigned/id/{id}/user/{user}")
	@ApiOperation(value = "Returns records assigned to given user.", notes = "Returns work assigned to a given user.", response = SearchResultList.class)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })	
	public SearchResultList findRecordsAssignedToUser(
			@ApiParam(value = "Id of map project", required = true) @PathParam("id") String mapProjectId, 
			@ApiParam(value = "UserName of user", required = true) @PathParam("user")  String userName,
			@ApiParam(value = "Paging/sorting/filtering object", required = true) PfsParameterJpa pfsParameter) {
	
		try {
			WorkflowService workflowService = new WorkflowServiceJpa();
			MappingService mappingService = new MappingServiceJpa();
			
			MapProject project = mappingService.getMapProject(new Long(mapProjectId));
			MapUser user = mappingService.getMapUser(userName);
			SearchResultList results = workflowService.findMapRecordsAssignedToUser(project, user, pfsParameter);

			mappingService.close();
			workflowService.close();
			return results;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}


}