package org.ihtsdo.otf.mapping.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.mapping.helpers.PfsParameterJpa;
import org.ihtsdo.otf.mapping.helpers.SearchResultList;
import org.ihtsdo.otf.mapping.helpers.SearchResultListJpa;
import org.ihtsdo.otf.mapping.jpa.services.ContentServiceJpa;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.rf2.Description;
import org.ihtsdo.otf.mapping.rf2.Relationship;
import org.ihtsdo.otf.mapping.rf2.jpa.RelationshipList;
import org.ihtsdo.otf.mapping.services.ContentService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for content service
 */
@Path("/content")
@Api(value = "/content", description = "Operations to retrieve RF2 content.")
@Produces({
	MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@SuppressWarnings("static-method")
public class ContentServiceRest {

	/** The terminology versions. */
	private Map<String, String> terminologyLatestVersions = null;


	/**
	 * Instantiates an empty {@link ContentServiceRest}.
	 */
	public ContentServiceRest() {

		// TODO: wire this to metadata service (getTerminologyLatestVesrions)
		terminologyLatestVersions = new HashMap<>();
		terminologyLatestVersions.put("SNOMEDCT", "20140131");
	}

	/**
	 * Returns the concept for id, terminology, and terminology version
	 * @param terminologyId the terminology id
	 * @param terminology the concept terminology
	 * @param terminologyVersion the terminology version
	 * @return the concept
	 */
    @GET
	@Path("/concept/{terminology}/{version}/id/{terminologyId}")
	@ApiOperation(value = "Find concept by id, version, and terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public Concept getConceptForId(
		@ApiParam(value = "ID of concept to fetch", required = true) @PathParam("terminologyId") String terminologyId,
		@ApiParam(value = "Concept terminology", required = true) @PathParam("terminology") String terminology,
		@ApiParam(value = "Concept terminology version", required = true) @PathParam("version") String terminologyVersion) {
		
		Logger.getLogger(ContentServiceJpa.class).info("RESTful call (Content): /concept/" + terminology + "/" + terminologyVersion + "/id/" + terminologyId);
		
		
		try {
			ContentService contentService = new ContentServiceJpa();
			Concept c = contentService.getConcept(terminologyId, terminology, terminologyVersion);
			
			// Make sure to read descriptions and relationships (prevents serialization error)
			for (Description d : c.getDescriptions()) { d.getLanguageRefSetMembers(); }
			for (Relationship r : c.getRelationships()) { r.getDestinationConcept(); }
			
			contentService.close();
			return c;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
	}
	
	/**
	 * Returns the inverse relationships for a concept (currently not marked for serialization in Concept)
	 * 
	 * @param terminologyId the id
	 * @param terminology the concept terminology
	 * @param terminologyVersion the concept terminologyVersion
	 * @return the concept
	 */
	@GET
	@Path("/concept/{terminology}/{version}/id/{terminologyId}/inverseRelationships")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public RelationshipList getInverseRelationshipsForConcept(
		@ApiParam(value = "ID of concept to fetch", required = true) @PathParam("terminologyId") String terminologyId,
		@ApiParam(value = "Concept terminology", required = true) @PathParam("terminology") String terminology,
		@ApiParam(value = "Concept terminology version", required = true) @PathParam("version") String terminologyVersion) {
		
		Logger.getLogger(ContentServiceJpa.class).info("RESTful call (Content): /concept/" + terminology + "/" + terminologyVersion + "/id/" + terminologyId + "/inverseRelationships");
		
		
		try {
			ContentService contentService = new ContentServiceJpa();
			Concept c = contentService.getConcept(terminologyId, terminology, terminologyVersion);
		
			RelationshipList relationshipList = new RelationshipList();
			relationshipList.setRelationships(c.getInverseRelationships());
					
			contentService.close();
			return relationshipList;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
	}
	

	/**
	 * Returns the concept for id, terminology. Looks in the latest version of the
	 * terminology.
	 * 
	 * @param terminologyId the id
	 * @param terminology the concept terminology
	 * @return the concept
	 */
	@GET
	@Path("/concept/{terminology}/id/terminologyId")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public Concept getConceptForId(
		@ApiParam(value = "ID of concept to fetch", required = true) @PathParam("terminologyId") String terminologyId,
		@ApiParam(value = "Concept terminology", required = true) @PathParam("terminology") String terminology) {
		
		
		Logger.getLogger(ContentServiceJpa.class).info("RESTful call (Content): /concept/" + terminology + "/id/" + terminologyId);
		
		try {
			ContentService contentService = new ContentServiceJpa();
			Concept c = contentService.getConcept(terminologyId, terminology, terminologyLatestVersions.get(terminology));
			c.getDescriptions();
			c.getRelationships();
			contentService.close();
			return c;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
		
	}
	
	/**
	 * Returns the concept for search string.
	 *
	 * @param searchString the lucene search string
	 * @return the concept for id
	 */
	@GET
	@Path("/concept/query/{string}")
	@ApiOperation(value = "Find concepts by search query", notes = "Returns concepts that are related to search query.", response = String.class)
	public SearchResultList findConcepts (
		@ApiParam(value = "lucene search string", required = true) @PathParam("string") String searchString) {
		
		Logger.getLogger(ContentServiceJpa.class).info("RESTful call (Content): /concept/query/" + searchString);
		try {
			ContentService contentService = new ContentServiceJpa();
			SearchResultList sr = contentService.findConcepts(searchString, new PfsParameterJpa());
			contentService.close();
			return sr;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Returns the descendants of a concept as mapped by relationships and inverse relationships
	 * @param terminologyId the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @return the search result list
	 */
	@GET
	@Path("/concept/{terminology}/{version}/id/{terminologyId}/descendants")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public SearchResultList findConceptDescendants(
		@ApiParam(value = "ID of concept to fetch descendants for", required = true) @PathParam("terminologyId") String terminologyId,
		@ApiParam(value = "Concept terminology", required = true) @PathParam("terminology") String terminology,
		@ApiParam(value = "Concept terminology version", required = true) @PathParam("version") String terminologyVersion) {
		
		
		Logger.getLogger(ContentServiceJpa.class).info("RESTful call (Content): /concept/" + terminology + "/" + terminologyVersion + "/id/" + terminologyId + "/descendants");
		try {
			ContentService contentService = new ContentServiceJpa();
			
			SearchResultList results = contentService.findDescendants(terminologyId, terminology,
				terminologyVersion, new Long("116680003")); // TODO Change this to metadata reference
		
			contentService.close();
			return results;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Returns the immediate children of a concept given terminology information
	 * @param id the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @return the search result list
	 */
	@GET
	@Path("/concept/{terminology}/{version}/id/{id:[0-9][0-9]*}/children")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public SearchResultList findConceptChildren(
		@ApiParam(value = "ID of concept to fetch descendants for", required = true) @PathParam("id") Long id,
		@ApiParam(value = "Concept terminology", required = true) @PathParam("terminology") String terminology,
		@ApiParam(value = "Concept terminology version", required = true) @PathParam("version") String terminologyVersion) {
		
		
		Logger.getLogger(ContentServiceJpa.class).info("RESTful call (Content): /concept/" + terminology + "/" + terminologyVersion + "/id/" + id.toString() + "/descendants");
		try {
			ContentService contentService = new ContentServiceJpa();
			
			SearchResultList results = contentService.findChildren(id.toString(), terminology,
				terminologyVersion, new Long("116680003")); // TODO Change this to metadata reference
		
			contentService.close();
			return results;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	// FOR TESTING ONLY!!
	/**
	 * Returns the immediate children of a concept given terminology information
	 * @return the search result list
	 */
	@GET
	@Path("/concept/treePositions")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public SearchResultList computeTreePositions() {
	
		
		try {
			ContentService contentService = new ContentServiceJpa();
			contentService.setTransactionPerOperation(true);
			
			contentService.computeTreePositions("SNOMEDCT",
				"20140131", "116680003", "138875005"); 
			/**Set<TreePosition> results = contentService.computeTreePositions("SNOMEDCT",
					"20140131", new Long("116680003"), new Long("371772001"));*/
			contentService.close();
			return new SearchResultListJpa();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Clears tree positions.
	 *
	 * @return the search result list
	 */
	@GET
	@Path("/concept/treePositions/clear")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public SearchResultList clearTreePositions() {
	
		
		try {
			ContentService contentService = new ContentServiceJpa();
			
			contentService.clearTreePositions("SNOMEDCT",
				"20140131"); 
		
			contentService.close();
			return new SearchResultListJpa();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Finds tree positions for concept.
	 *
	 * @return the search result list
	 */
	@GET
	@Path("/concept/treePositions/concept")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public SearchResultList findTreePositionsForConcept() {
			
		try {
			ContentService contentService = new ContentServiceJpa();
			
			SearchResultList results = contentService.findTreePositionsForConcept("118234003", "SNOMEDCT",
				"20140131"); 
			contentService.close();
			return results;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Finds descendants from tree postions.
	 *
	 * @return the search result list
	 */
	@GET
	@Path("/concept/treePositions/descendantfind")
	@ApiOperation(value = "Find concept by id, terminology", notes = "Returns a concept in either xml json given a concept id, terminology - assumes latest terminology version.", response = Concept.class)
	@Produces({
			MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
	})
	public SearchResultList findDescendantsFromTreePostions() {
			
		try {
			ContentService contentService = new ContentServiceJpa();
			Logger.getLogger(this.getClass()).info("start");
			SearchResultList results = contentService.findDescendantsFromTreePostions("110091001", "SNOMEDCT",
				"20140131"); 
			contentService.close();

			Logger.getLogger(this.getClass()).info("end");
			return results;
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
}
