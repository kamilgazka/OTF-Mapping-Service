package org.ihtsdo.otf.mapping.services;

import java.util.Set;

import org.ihtsdo.otf.mapping.helpers.PfsParameter;
import org.ihtsdo.otf.mapping.helpers.SearchResultList;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.rf2.TreePosition;

/**
 * The interface for the content service.
 * 
 */
public interface ContentService {

	/**
	 * Closes the manager associated with service.y
	 * 
	 * @exception Exception the exception
	 */
	public void close() throws Exception;

	/**
	 * Returns the concept.
	 * 
	 * @param conceptId the concept id
	 * @return the concept
	 * @throws Exception if anything goes wrong
	 */
	public Concept getConcept(Long conceptId) throws Exception;

	/**
	 * Returns the concept matching the specified parameters.
	 * 
	 * @param terminologyId the concept id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminologyVersion
	 * @return the concept
	 * @throws Exception if anything goes wrong
	 */
	public Concept getConcept(String terminologyId, String terminology,
		String terminologyVersion) throws Exception;

	/**
	 * Returns the concept search results matching the query. Results can be
	 * paged, filtered, and sorted.
	 * 
	 * @param query the search string
	 * @param pfsParameter the paging, filtering, sorting parameter
	 * @return the search results for the search string
	 * @throws Exception if anything goes wrong
	 */
	public SearchResultList findConcepts(String query, PfsParameter pfsParameter)
		throws Exception;

	/**
	 * Returns {@link SearchResultList} for all concepts of the specified terminology.
	 *
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @return the search results for the search string
	 * @throws Exception if anything goes wrong
	 */
	public SearchResultList findAllConcepts(String terminology, String terminologyVersion)
		throws Exception;

	/**
	 * Gets the descendants of a concept.
	 *
	 * @param terminologyId the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @param typeId the type id
	 * @return the set of concepts
	 */
	public SearchResultList findDescendants(String terminologyId, String terminology,
			String terminologyVersion, Long typeId);

	/**
	 * Returns the descendants.
	 *
	 * @param terminologyId the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @param typeId the type id
	 * @return the descendants
	 */
	public Set<Concept> getDescendants(String terminologyId, String terminology,
			String terminologyVersion, Long typeId);

	/**
	 * Find children.
	 *
	 * @param terminologyId the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @param typeId the type id
	 * @return the search result list
	 */
	public SearchResultList findChildren(String terminologyId, String terminology,
			String terminologyVersion, Long typeId);
	
	/**
	 * Returns the tree positions for concept.
	 *
	 * @param terminologyId the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @return the tree positions for concept
	 */
	public SearchResultList getTreePositionsForConcept(String terminologyId, String terminology,
		String terminologyVersion);
	
	/**
	 * Returns the descendant tree positions for concept.
	 *
	 * @param terminologyId the terminology id
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @return the descendant tree positions for concept
	 */
	public SearchResultList getDescendantTreePositionsForConcept(String terminologyId, String terminology,
		String terminologyVersion);

	/**
	 * Clear tree positions.
	 *
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 */
	public void clearTreePositions(String terminology, String terminologyVersion);
	
	/**
	 * Compute tree positions.
	 *
	 * @param terminology the terminology
	 * @param terminologyVersion the terminology version
	 * @param typeId TODO
	 * @param rootId the root id
	 * @return the tree positions
	 */
	public Set<TreePosition> computeTreePositions(String terminology, String terminologyVersion, String typeId, String rootId); 

	/**
	 * Gets the transaction per operation.
	 *
	 * @return the transaction per operation
	 * @throws Exception the exception
	 */
	public boolean getTransactionPerOperation() throws Exception;
}
