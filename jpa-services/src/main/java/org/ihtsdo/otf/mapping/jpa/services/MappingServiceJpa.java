package org.ihtsdo.otf.mapping.jpa.services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;
import org.hibernate.criterion.MatchMode;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.ihtsdo.otf.mapping.helpers.LocalException;
import org.ihtsdo.otf.mapping.helpers.MapAdviceList;
import org.ihtsdo.otf.mapping.helpers.MapAdviceListJpa;
import org.ihtsdo.otf.mapping.helpers.MapAgeRangeList;
import org.ihtsdo.otf.mapping.helpers.MapAgeRangeListJpa;
import org.ihtsdo.otf.mapping.helpers.MapPrincipleList;
import org.ihtsdo.otf.mapping.helpers.MapPrincipleListJpa;
import org.ihtsdo.otf.mapping.helpers.MapProjectList;
import org.ihtsdo.otf.mapping.helpers.MapProjectListJpa;
import org.ihtsdo.otf.mapping.helpers.MapRecordList;
import org.ihtsdo.otf.mapping.helpers.MapRecordListJpa;
import org.ihtsdo.otf.mapping.helpers.MapRelationList;
import org.ihtsdo.otf.mapping.helpers.MapRelationListJpa;
import org.ihtsdo.otf.mapping.helpers.MapUserList;
import org.ihtsdo.otf.mapping.helpers.MapUserListJpa;
import org.ihtsdo.otf.mapping.helpers.MapUserPreferencesList;
import org.ihtsdo.otf.mapping.helpers.MapUserPreferencesListJpa;
import org.ihtsdo.otf.mapping.helpers.MapUserRole;
import org.ihtsdo.otf.mapping.helpers.PfsParameter;
import org.ihtsdo.otf.mapping.helpers.PfsParameterJpa;
import org.ihtsdo.otf.mapping.helpers.ProjectSpecificAlgorithmHandler;
import org.ihtsdo.otf.mapping.helpers.SearchResult;
import org.ihtsdo.otf.mapping.helpers.SearchResultJpa;
import org.ihtsdo.otf.mapping.helpers.SearchResultList;
import org.ihtsdo.otf.mapping.helpers.SearchResultListJpa;
import org.ihtsdo.otf.mapping.helpers.TreePositionList;
import org.ihtsdo.otf.mapping.helpers.TreePositionListJpa;
import org.ihtsdo.otf.mapping.helpers.WorkflowPath;
import org.ihtsdo.otf.mapping.helpers.WorkflowStatus;
import org.ihtsdo.otf.mapping.jpa.MapAdviceJpa;
import org.ihtsdo.otf.mapping.jpa.MapAgeRangeJpa;
import org.ihtsdo.otf.mapping.jpa.MapEntryJpa;
import org.ihtsdo.otf.mapping.jpa.MapPrincipleJpa;
import org.ihtsdo.otf.mapping.jpa.MapProjectJpa;
import org.ihtsdo.otf.mapping.jpa.MapRecordJpa;
import org.ihtsdo.otf.mapping.jpa.MapRelationJpa;
import org.ihtsdo.otf.mapping.jpa.MapUserJpa;
import org.ihtsdo.otf.mapping.jpa.MapUserPreferencesJpa;
import org.ihtsdo.otf.mapping.model.MapAdvice;
import org.ihtsdo.otf.mapping.model.MapAgeRange;
import org.ihtsdo.otf.mapping.model.MapEntry;
import org.ihtsdo.otf.mapping.model.MapNote;
import org.ihtsdo.otf.mapping.model.MapPrinciple;
import org.ihtsdo.otf.mapping.model.MapProject;
import org.ihtsdo.otf.mapping.model.MapRecord;
import org.ihtsdo.otf.mapping.model.MapRelation;
import org.ihtsdo.otf.mapping.model.MapUser;
import org.ihtsdo.otf.mapping.model.MapUserPreferences;
import org.ihtsdo.otf.mapping.rf2.ComplexMapRefSetMember;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.rf2.TreePosition;
import org.ihtsdo.otf.mapping.services.ContentService;
import org.ihtsdo.otf.mapping.services.MappingService;
import org.ihtsdo.otf.mapping.services.MetadataService;
import org.ihtsdo.otf.mapping.services.WorkflowService;
import org.ihtsdo.otf.mapping.workflow.TrackingRecord;

/**
 * JPA implementation of the {@link MappingService}.
 */
public class MappingServiceJpa extends RootServiceJpa implements MappingService {

	/**
	 * Instantiates an empty {@link MappingServiceJpa}.
	 * 
	 * @throws Exception
	 */
	public MappingServiceJpa() throws Exception {
		super();
	}

	/**
	 * Close the manager when done with this service.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void close() throws Exception {
		if (manager.isOpen()) {
			manager.close();
		}
	}

	// //////////////////////////////////
	// MapProject
	// - getMapProjects
	// - getMapProject(Long id)
	// - getMapProject(String name)
	// - findMapProjects(String query)
	// - addMapProject(MapProject mapProject)
	// - updateMapProject(MapProject mapProject)
	// - removeMapProject(MapProject mapProject)
	// //////////////////////////////////

	/**
	 * Return map project for auto-generated id.
	 * 
	 * @param id
	 *            the auto-generated id
	 * @return the MapProject
	 */
	@Override
	public MapProject getMapProject(Long id) throws Exception {

		MapProject m = null;

		javax.persistence.Query query = manager
				.createQuery("select m from MapProjectJpa m where id = :id");
		query.setParameter("id", id);

		m = (MapProject) query.getSingleResult();
		m.getScopeConcepts().size();
		m.getScopeExcludedConcepts().size();
		m.getMapAdvices().size();
		m.getMapRelations().size();
		m.getMapLeads().size();
		m.getMapSpecialists().size();
		m.getMapPrinciples().size();
		m.getPresetAgeRanges().size();
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapProjectForRefSetId
	 * (java.lang.String)
	 */
	@Override
	public MapProject getMapProjectForRefSetId(String refSetId)
			throws Exception {

		MapProject m = null;

		javax.persistence.Query query = manager.createQuery(
				"select m from MapProjectJpa m where refSetId = :refSetId")
				.setParameter("refSetId", refSetId);

		m = (MapProject) query.getSingleResult();
		m.getScopeConcepts().size();
		m.getScopeExcludedConcepts().size();
		m.getMapAdvices().size();
		m.getMapRelations().size();
		m.getMapLeads().size();
		m.getMapSpecialists().size();
		m.getMapPrinciples().size();
		m.getPresetAgeRanges().size();
		return m;
	}

	/**
	 * Retrieve all map projects.
	 * 
	 * @return a List of MapProjects
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapProjectList getMapProjects() {

		List<MapProject> mapProjects = null;

		// construct query
		javax.persistence.Query query = manager
				.createQuery("select m from MapProjectJpa m");

		mapProjects = query.getResultList();

		// force instantiation of lazy collections
		for (MapProject mapProject : mapProjects) {
			handleMapProjectLazyInitialization(mapProject);
		}

		MapProjectListJpa mapProjectList = new MapProjectListJpa();
		mapProjectList.setMapProjects(mapProjects);
		mapProjectList.setTotalCount(mapProjects.size());
		return mapProjectList;
	}

	/**
	 * Query for MapProjects.
	 * 
	 * @param query
	 *            the query
	 * @param pfsParameter
	 *            the pfs parameter
	 * @return the list of MapProject
	 * @throws Exception
	 *             the exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SearchResultList findMapProjectsForQuery(String query,
			PfsParameter pfsParameter) throws Exception {

		SearchResultList s = new SearchResultListJpa();

		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(manager);

		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		Query luceneQuery;

		try {
			// construct luceneQuery based on URL format
			if (query.indexOf(':') == -1) { // no fields indicated
				MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
						Version.LUCENE_36, fieldNames.toArray(new String[0]),
						searchFactory.getAnalyzer(MapProjectJpa.class));
				queryParser.setAllowLeadingWildcard(false);
				luceneQuery = queryParser.parse(query);

			} else { // field:value
				QueryParser queryParser = new QueryParser(Version.LUCENE_36,
						"summary",
						searchFactory.getAnalyzer(MapProjectJpa.class));
				luceneQuery = queryParser.parse(query);
			}
		} catch (ParseException e) {
			throw new LocalException(
					"The specified search terms cannot be parsed.  Please check syntax and try again.");
		}

		List<MapProject> m;

		m = fullTextEntityManager.createFullTextQuery(luceneQuery,
				MapProjectJpa.class).getResultList();
		// if a parse exception, throw a local exception

		Logger.getLogger(this.getClass()).debug(
				Integer.toString(m.size()) + " map projects retrieved");

		for (MapProject mp : m) {
			s.addSearchResult(new SearchResultJpa(mp.getId(), mp.getRefSetId()
					.toString(), mp.getName()));
		}

		// Sort by ID
		s.sortBy(new Comparator<SearchResult>() {
			@Override
			public int compare(SearchResult o1, SearchResult o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});

		fullTextEntityManager.close();

		// closing fullTextEntityManager also closes manager, recreate
		manager = factory.createEntityManager();

		return s;

	}

	/**
	 * Add a map project.
	 * 
	 * @param mapProject
	 *            the map project
	 * @return the map project
	 */
	@Override
	public MapProject addMapProject(MapProject mapProject) throws Exception {

		// check that each user has only one role
		validateUserAndRole(mapProject);

		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			manager.persist(mapProject);
			tx.commit();

			return mapProject;
		} else {
			if (!tx.isActive()) {
				throw new IllegalStateException(
						"Error attempting to change data without an active transaction");
			}
			manager.persist(mapProject);
			return mapProject;
		}

	}

	/**
	 * Update a map project.
	 * 
	 * @param mapProject
	 *            the changed map project
	 */
	@Override
	public void updateMapProject(MapProject mapProject) throws Exception {

		// check that each user has only one role
		validateUserAndRole(mapProject);

		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			manager.merge(mapProject);
			tx.commit();
		} else {
			manager.merge(mapProject);
		}

	}

	/**
	 * Remove (delete) a map project.
	 * 
	 * @param mapProjectId
	 *            the map project to be removed
	 */
	@Override
	public void removeMapProject(Long mapProjectId) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			// first, remove the leads and specialists from this project
			tx.begin();
			MapProject mp = manager.find(MapProjectJpa.class, mapProjectId);
			mp.setMapLeads(null);
			mp.setMapSpecialists(null);
			tx.commit();

			// now remove the entry
			tx.begin();
			if (manager.contains(mp)) {
				manager.remove(mp);
			} else {
				manager.remove(manager.merge(mp));
			}
			tx.commit();
		} else {
			MapProject mp = manager.find(MapProjectJpa.class, mapProjectId);
			mp.setMapLeads(null);
			mp.setMapSpecialists(null);
			if (manager.contains(mp)) {
				manager.remove(mp);
			} else {
				manager.remove(manager.merge(mp));
			}
		}

	}

	// ///////////////////////////////////////////////////////////////
	// MapUser
	// - getMapUsers()
	// - getMapProjectsForUser(MapUser mapUser)
	// - findMapUsers(String query)
	// - addMapUser(MapUser mapUser)
	// - updateMapUser(MapUser mapUser)
	// - removeMapUser(Long id)
	// ///////////////////////////////////////////////////////////////

	/**
	 * Retrieve all map users.
	 * 
	 * @return a List of MapUsers
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapUserList getMapUsers() {

		List<MapUser> m = null;

		javax.persistence.Query query = manager
				.createQuery("select m from MapUserJpa m");

		m = query.getResultList();
		MapUserListJpa mapUserList = new MapUserListJpa();
		mapUserList.setMapUsers(m);
		mapUserList.setTotalCount(m.size());
		return mapUserList;
	}

	/**
	 * Return map specialist for auto-generated id.
	 * 
	 * @param id
	 *            the auto-generated id
	 * @return the MapSpecialist
	 */
	@Override
	public MapUser getMapUser(Long id) throws Exception {

		javax.persistence.Query query = manager
				.createQuery("select m from MapUserJpa m where id = :id");
		query.setParameter("id", id);
		return (MapUser) query.getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapUser(java.lang.String
	 * )
	 */
	@Override
	public MapUser getMapUser(String userName) throws Exception {

		javax.persistence.Query query = manager
				.createQuery("select m from MapUserJpa m where userName = :userName");
		query.setParameter("userName", userName);
		return (MapUser) query.getSingleResult();
	}

	/**
	 * Retrieve all map projects assigned to a particular map specialist.
	 * 
	 * @param mapUser
	 *            the map user
	 * @return a List of MapProjects
	 */
	@Override
	public MapProjectList getMapProjectsForMapUser(MapUser mapUser) {

		MapProjectList mpList = getMapProjects();
		List<MapProject> mpListReturn = new ArrayList<>();

		// iterate and check for presence of mapUser as specialist
		for (MapProject mp : mpList.getMapProjects()) {

			for (MapUser ms : mp.getMapSpecialists()) {
				if (ms.equals(mapUser)) {
					mpListReturn.add(mp);
				}
			}

			for (MapUser ms : mp.getMapLeads()) {
				if (ms.equals(mapUser)) {
					mpListReturn.add(mp);
				}
			}
		}

		// force instantiation of lazy collections
		for (MapProject mapProject : mpListReturn) {
			handleMapProjectLazyInitialization(mapProject);
		}

		MapProjectListJpa mapProjectList = new MapProjectListJpa();
		mapProjectList.setMapProjects(mpListReturn);
		mapProjectList.setTotalCount(mpListReturn.size());
		return mapProjectList;
	}

	/**
	 * Update a map specialist.
	 * 
	 * @param mapUser
	 *            the changed map user
	 */
	@Override
	public void updateMapUser(MapUser mapUser) {

		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			manager.merge(mapUser);
			tx.commit();
		} else {
			manager.merge(mapUser);
		}

	}

	/**
	 * Remove (delete) a map specialist.
	 * 
	 * @param mapUserId
	 *            the map user to be removed
	 */
	@Override
	public void removeMapUser(Long mapUserId) {

		tx = manager.getTransaction();

		// retrieve this map specialist
		MapUser mu = manager.find(MapUserJpa.class, mapUserId);

		// retrieve all projects on which this specialist appears
		List<MapProject> projects = getMapProjectsForMapUser(mu)
				.getMapProjects();

		if (getTransactionPerOperation()) {
			// remove specialist from all these projects
			tx.begin();
			for (MapProject mp : projects) {
				mp.removeMapLead(mu);
				mp.removeMapSpecialist(mu);
				manager.merge(mp);
			}
			tx.commit();

			// remove specialist
			tx.begin();
			if (manager.contains(mu)) {
				manager.remove(mu);
			} else {
				manager.remove(manager.merge(mu));
			}
			tx.commit();

		} else {
			for (MapProject mp : projects) {
				mp.removeMapLead(mu);
				mp.removeMapSpecialist(mu);
				manager.merge(mp);
			}
			if (manager.contains(mu)) {
				manager.remove(mu);
			} else {
				manager.remove(manager.merge(mu));
			}
		}

	}

	/**
	 * Add a map lead.
	 * 
	 * @param mapUser
	 *            the map lead
	 * @return the map user
	 */
	@Override
	public MapUser addMapUser(MapUser mapUser) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			manager.persist(mapUser);
			tx.commit();
		} else {
			manager.persist(mapUser);
		}

		return mapUser;
	}

	// //////////////////////////////////
	// MapRecord
	// //////////////////////////////////

	/**
	 * Retrieve all map records.
	 * 
	 * @return a List of MapRecords
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapRecordList getMapRecords() {
		List<MapRecord> mapRecords = null;
		// construct query
		javax.persistence.Query query = manager
				.createQuery("select m from MapRecordJpa m");
		// Try query
		mapRecords = query.getResultList();
		MapRecordListJpa mapRecordList = new MapRecordListJpa();

		for (MapRecord mr : mapRecordList.getIterable()) {
			this.handleMapRecordLazyInitialization(mr);
		}

		mapRecordList.setMapRecords(mapRecords);
		mapRecordList.setTotalCount(mapRecords.size());

		return mapRecordList;
	}

	/**
	 * Retrieve map record for given id.
	 * 
	 * @param id
	 *            the map record id
	 * @return the map record
	 */
	@Override
	public MapRecord getMapRecord(Long id) throws Exception {

		/*
		 * Try to retrieve the single expected result If zero or more than one
		 * result are returned, log error and set result to null
		 */

		MapRecord mapRecord = manager.find(MapRecordJpa.class, id);

		if (mapRecord != null)
			handleMapRecordLazyInitialization(mapRecord);

		Logger.getLogger(this.getClass()).debug(
				"Returning record_id... "
						+ ((mapRecord != null) ? mapRecord.getId().toString()
								: "null"));
		return mapRecord;
	}

	/**
	 * Retrieve map records for a lucene query.
	 * 
	 * @param query
	 *            the lucene query string
	 * @param pfsParameter
	 *            the pfs parameter
	 * @return a list of map records
	 * @throws Exception
	 *             the exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SearchResultList findMapRecordsForQuery(String query,
			PfsParameter pfsParameter) throws Exception {

		SearchResultList s = new SearchResultListJpa();

		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(manager);

		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		Query luceneQuery;

		try {

			// construct luceneQuery based on URL format
			if (query.indexOf(':') == -1) { // no fields indicated
				MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
						Version.LUCENE_36, fieldNames.toArray(new String[0]),
						searchFactory.getAnalyzer(MapRecordJpa.class));
				queryParser.setAllowLeadingWildcard(false);
				luceneQuery = queryParser.parse(query);

			} else { // field:value
				QueryParser queryParser = new QueryParser(Version.LUCENE_36,
						"summary",
						searchFactory.getAnalyzer(MapRecordJpa.class));
				luceneQuery = queryParser.parse(query);
			}

			List<MapRecord> mapRecords = fullTextEntityManager
					.createFullTextQuery(luceneQuery, MapRecordJpa.class)
					.getResultList();

			Logger.getLogger(this.getClass()).debug(
					Integer.toString(mapRecords.size())
							+ " map records retrieved");

			for (MapRecord mapRecord : mapRecords) {
				s.addSearchResult(new SearchResultJpa(mapRecord.getId(),
						mapRecord.getConceptId().toString(), mapRecord
								.getConceptName()));
			}

			// Sort by ID
			s.sortBy(new Comparator<SearchResult>() {
				@Override
				public int compare(SearchResult o1, SearchResult o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});

			fullTextEntityManager.close();

			// closing fullTextEntityManager also closes manager, recreate
			manager = factory.createEntityManager();

			return s;

		} catch (ParseException e) {
			throw new LocalException(
					"The specified search terms cannot be parsed.  Please check syntax and try again.");
		}
	}

	/**
	 * Add a map record.
	 * 
	 * @param mapRecord
	 *            the map record to be added
	 * @return the map record
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public MapRecord addMapRecord(MapRecord mapRecord) throws Exception {

		// check if user valid
		if (mapRecord.getOwner() == null) {
			throw new Exception("Map Record requires valid user in owner field");
		}

		// check if last modified by user valid
		if (mapRecord.getLastModifiedBy() == null) {
			throw new Exception(
					"Map Record requires valid user in lastModifiedBy field");
		}

		// set the map record of all elements of this record
		mapRecord.assignToChildren();

		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.persist(mapRecord);
			tx.commit();
			return mapRecord;
		} else {
			manager.persist(mapRecord);
			return mapRecord;
		}

	}

	/**
	 * Update a map record.
	 * 
	 * @param mapRecord
	 *            the map record to be updated
	 */
	@Override
	public void updateMapRecord(MapRecord mapRecord) {

		// update last modified timestamp
		mapRecord.setLastModified((new java.util.Date()).getTime());

		// first assign the map record to its children
		mapRecord.assignToChildren();

		if (getTransactionPerOperation()) {

			tx = manager.getTransaction();
			tx.begin();
			manager.merge(mapRecord);
			tx.commit();
		} else {
			manager.merge(mapRecord);
		}

	}

	/**
	 * Remove (delete) a map record by id.
	 * 
	 * @param id
	 *            the id of the map record to be removed
	 */
	@Override
	public void removeMapRecord(Long id) {

		tx = manager.getTransaction();

		// find the map record
		MapRecord m = manager.find(MapRecordJpa.class, id);

		if (getTransactionPerOperation()) {
			// delete the map record
			tx.begin();
			if (manager.contains(m)) {
				manager.remove(m);
			} else {
				manager.remove(manager.merge(m));
			}
			tx.commit();

		} else {
			if (manager.contains(m)) {
				manager.remove(m);
			} else {
				manager.remove(manager.merge(m));
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapRecordRevisions(
	 * java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MapRecordList getMapRecordRevisions(Long mapRecordId) {

		AuditReader reader = AuditReaderFactory.get(manager);
		List<MapRecord> revisions = reader.createQuery()

		// all revisions, returned as objects, not finding deleted entries
				.forRevisionsOfEntity(MapRecordJpa.class, true, false)

				// search by id
				.add(AuditEntity.id().eq(mapRecordId))

				// order by descending timestamp
				.addOrder(AuditEntity.property("timestamp").desc())

				// execute query
				.getResultList();

		// construct the map
		MapRecordListJpa mapRecordList = new MapRecordListJpa();
		mapRecordList.setMapRecords(revisions);
		for (MapRecord mapRecord : revisions) {
			handleMapRecordLazyInitialization(mapRecord);
		}
		mapRecordList.setTotalCount(revisions.size());
		return mapRecordList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getRecentlyEditedMapRecords
	 * (org.ihtsdo.otf.mapping.model.MapUser)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public MapRecordList getRecentlyEditedMapRecords(Long projectId,
			String userName, PfsParameter pfsParameter) throws Exception {

		MapUser user = getMapUser(userName);

		AuditReader reader = AuditReaderFactory.get(manager);
		PfsParameter localPfsParameter = pfsParameter;

		// if no pfsParameter supplied, construct a default one
		if (localPfsParameter == null)
			localPfsParameter = new PfsParameterJpa();

		// split the query restrictions
		if (localPfsParameter.getQueryRestriction() != null) {
			// do nothing
		}

		// construct the query
		AuditQuery query = reader.createQuery()

				// all revisions, returned as objects, finding deleted entries
				.forRevisionsOfEntity(MapRecordJpa.class, true, true)

				// add mapProjectId and owner as constraints
				.add(AuditEntity.property("mapProjectId").eq(projectId))
				.add(AuditEntity.relatedId("lastModifiedBy").eq(user.getId()))

				// exclude records with workflow status NEW
				.add(AuditEntity.property("workflowStatus").ne(
						WorkflowStatus.NEW));

		// if sort field specified
		if (localPfsParameter.getSortField() != null) {
			query.addOrder(AuditEntity.property(
					localPfsParameter.getSortField()).desc());

			// otherwise, sort by last modified (descending)
		} else {
			query.addOrder(AuditEntity.property("lastModified").desc());
		}
		// if paging request supplied, set first result and max results
		if (localPfsParameter.getStartIndex() != -1
				&& localPfsParameter.getMaxResults() != -1) {
			query.setFirstResult(localPfsParameter.getStartIndex())
					.setMaxResults(localPfsParameter.getMaxResults());

		}

		// if query terms specified, add
		if (pfsParameter.getQueryRestriction() != null) {
			String[] queryTerms = pfsParameter.getQueryRestriction().split(" ");
			query.add(AuditEntity.or(
					AuditEntity.property("conceptId").in(queryTerms),
					AuditEntity.property("conceptName").like(
							pfsParameter.getQueryRestriction(),
							MatchMode.ANYWHERE)));

		}

		// execute the query
		List<MapRecord> editedRecords = query.getResultList();

		// create the mapRecordList and set total size
		MapRecordListJpa mapRecordList = new MapRecordListJpa();
		// mapRecordList.setTotalCount(editedRecords.size());

		// only add one copy
		// TODO Decide whether or not to requery to get a full page of 10
		List<MapRecord> uniqueRecords = new ArrayList<>();
		for (MapRecord mapRecord : editedRecords) {
			boolean recordExists = false;
			for (MapRecord mr : uniqueRecords) {
				if (mr.getId().equals(mapRecord.getId()))
					recordExists = true;
			}
			if (recordExists == false)
				uniqueRecords.add(mapRecord);
		}

		// handle all lazy initializations
		for (MapRecord mapRecord : uniqueRecords) {
			handleMapRecordLazyInitialization(mapRecord);
		}
		mapRecordList.setMapRecords(uniqueRecords);
		return mapRecordList;
	}

	// //////////////////////////////////
	// Other query services
	// //////////////////////////////////

	// //////////////////////////////////////////////
	// Descendant services
	// //////////////////////////////////////////////

	/**
	 * Retrieve map records for a given terminology id.
	 * 
	 * @param terminologyId
	 *            the concept id
	 * @return the list of map records
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MapRecordList getMapRecordsForConcept(String terminologyId) {
		List<MapRecord> mapRecords = null;

		// construct query
		javax.persistence.Query query = manager
				.createQuery("select m from MapRecordJpa m where conceptId = :conceptId");

		// Try query
		query.setParameter("conceptId", terminologyId);
		mapRecords = query.getResultList();
		for (MapRecord mapRecord : mapRecords) {
			handleMapRecordLazyInitialization(mapRecord);
		}

		MapRecordListJpa mapRecordList = new MapRecordListJpa();
		mapRecordList.setMapRecords(mapRecords);
		mapRecordList.setTotalCount(mapRecords.size());
		return mapRecordList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapRecordsForMapProject
	 * (java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MapRecordList getMapRecordsForMapProject(Long mapProjectId)
			throws Exception {

		javax.persistence.Query query = manager
				.createQuery(
						"select m from MapRecordJpa m where mapProjectId = :mapProjectId")
				.setParameter("mapProjectId", mapProjectId);
		MapRecordList mapRecordList = new MapRecordListJpa();
		mapRecordList.setMapRecords(query.getResultList());
		return mapRecordList;
	}

	@Override
	public MapRecord getMapRecordForProjectAndConcept(Long mapProjectId,
			String terminologyId) throws Exception {

		MapRecord mapRecord = (MapRecord) manager
				.createQuery(
						"select m from MapRecordJpa m where mapProjectId = :mapProjectId and conceptId = :conceptId")
				.setParameter("mapProjectId", mapProjectId)
				.setParameter("conceptId", terminologyId).getSingleResult();

		return mapRecord;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapRecordsForMapProject
	 * (java.lang.Long, org.ihtsdo.otf.mapping.helpers.PfsParameter)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapRecordList getPublishedAndReadyForPublicationMapRecordsForMapProject(
			Long mapProjectId, PfsParameter pfsParameter) throws Exception {

		// construct basic query
		String full_query = constructMapRecordForMapProjectIdQuery(
				mapProjectId, pfsParameter == null ? new PfsParameterJpa()
						: pfsParameter);

		full_query += " AND (workflowStatus:'PUBLISHED' OR workflowStatus:'READY_FOR_PUBLICATION')";

		Logger.getLogger(MappingServiceJpa.class).info(full_query);

		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(manager);

		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		Query luceneQuery;

		// construct luceneQuery based on URL format

		org.hibernate.search.jpa.FullTextQuery ftquery = null;

		try {

			QueryParser queryParser = new QueryParser(Version.LUCENE_36,
					"summary", searchFactory.getAnalyzer(MapRecordJpa.class));
			luceneQuery = queryParser.parse(full_query);

			ftquery = fullTextEntityManager.createFullTextQuery(luceneQuery,
					MapRecordJpa.class);

			// if a parse exception, throw a local exception
		} catch (ParseException e) {
			throw new LocalException(
					"The specified search terms cannot be parsed.  Please check syntax and try again.");
		}

		// Sort Options -- in order of priority
		// (1) if a sort field is specified by pfs parameter, use it
		// (2) if a query has been specified, use nothing (lucene relevance
		// default)
		// (3) if a query has not been specified, sort by conceptId

		String sortField = "conceptId";
		if (pfsParameter != null && pfsParameter.getSortField() != null
				&& !pfsParameter.getSortField().isEmpty()) {
			ftquery.setSort(new Sort(new SortField(pfsParameter.getSortField(),
					SortField.STRING)));
		} else if (pfsParameter != null
				&& pfsParameter.getQueryRestriction() != null
				&& !pfsParameter.getQueryRestriction().isEmpty()) {
			// do nothing
		} else {
			ftquery.setSort(new Sort(new SortField(sortField, SortField.STRING)));
		}

		// get the results
		int totalCount;
		List<MapRecord> mapRecords = new ArrayList<>();

		totalCount = ftquery.getResultSize();

		if (pfsParameter != null) {
			ftquery.setFirstResult(pfsParameter.getStartIndex());
			ftquery.setMaxResults(pfsParameter.getMaxResults());
		}
		mapRecords = ftquery.getResultList();

		Logger.getLogger(this.getClass()).debug(
				Integer.toString(mapRecords.size()) + " records retrieved");

		for (MapRecord mapRecord : mapRecords) {
			handleMapRecordLazyInitialization(mapRecord);
		}

		// set the total count
		MapRecordListJpa mapRecordList = new MapRecordListJpa();
		mapRecordList.setTotalCount(totalCount);

		// extract the required sublist of map records
		mapRecordList.setMapRecords(mapRecords);

		return mapRecordList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapRecordsForMapProject
	 * (java.lang.Long, org.ihtsdo.otf.mapping.helpers.PfsParameter)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapRecordList getPublishedMapRecordsForMapProject(Long mapProjectId,
			PfsParameter pfsParameter) throws Exception {

		// construct basic query
		String full_query = constructMapRecordForMapProjectIdQuery(
				mapProjectId, pfsParameter == null ? new PfsParameterJpa()
						: pfsParameter);

		full_query += " AND workflowStatus:'PUBLISHED'";

		Logger.getLogger(MappingServiceJpa.class).info(full_query);

		FullTextEntityManager fullTextEntityManager = Search
				.getFullTextEntityManager(manager);

		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		Query luceneQuery;

		// construct luceneQuery based on URL format

		QueryParser queryParser = new QueryParser(Version.LUCENE_36, "summary",
				searchFactory.getAnalyzer(MapRecordJpa.class));

		try {
			luceneQuery = queryParser.parse(full_query);

			org.hibernate.search.jpa.FullTextQuery ftquery = fullTextEntityManager
					.createFullTextQuery(luceneQuery, MapRecordJpa.class);

			// Sort Options -- in order of priority
			// (1) if a sort field is specified by pfs parameter, use it
			// (2) if a query has been specified, use nothing (lucene relevance
			// default)
			// (3) if a query has not been specified, sort by conceptId

			String sortField = "conceptId";
			if (pfsParameter != null && pfsParameter.getSortField() != null
					&& !pfsParameter.getSortField().isEmpty()) {
				ftquery.setSort(new Sort(new SortField(pfsParameter
						.getSortField(), SortField.STRING)));
			} else if (pfsParameter != null
					&& pfsParameter.getQueryRestriction() != null
					&& !pfsParameter.getQueryRestriction().isEmpty()) {
				// do nothing
			} else {
				ftquery.setSort(new Sort(new SortField(sortField,
						SortField.STRING)));
			}

			// get the results
			int totalCount = ftquery.getResultSize();

			if (pfsParameter != null) {
				ftquery.setFirstResult(pfsParameter.getStartIndex());
				ftquery.setMaxResults(pfsParameter.getMaxResults());
			}
			List<MapRecord> mapRecords = ftquery.getResultList();

			Logger.getLogger(this.getClass()).debug(
					Integer.toString(mapRecords.size()) + " records retrieved");

			for (MapRecord mapRecord : mapRecords) {
				handleMapRecordLazyInitialization(mapRecord);
			}

			// set the total count
			MapRecordListJpa mapRecordList = new MapRecordListJpa();
			mapRecordList.setTotalCount(totalCount);

			// extract the required sublist of map records
			mapRecordList.setMapRecords(mapRecords);

			return mapRecordList;
		} catch (ParseException e) {
			throw new LocalException(
					"The specified search terms cannot be parsed.  Please check syntax and try again.");
		}

	}

	/**
	 * Helper function for map record query construction using both fielded
	 * terms and unfielded terms.
	 * 
	 * @param mapProjectId
	 *            the map project id for which queries are retrieved
	 * @param pfsParameter
	 *            the pfs parameter
	 * @return the full lucene query text
	 */
	private static String constructMapRecordForMapProjectIdQuery(
			Long mapProjectId, PfsParameter pfsParameter) {

		String full_query;

		// if no filter supplied, return query based on map project id only
		if (pfsParameter.getQueryRestriction() == null
				|| pfsParameter.getQueryRestriction().equals("")) {
			full_query = "mapProjectId:" + mapProjectId;
			return full_query;
		}

		// Pre-treatment: Find any lower-case boolean operators and set to
		// uppercase

		// //////////////////
		// Basic algorithm:
		//
		// 1) add whitespace breaks to operators
		// 2) split query on whitespace
		// 3) cycle over terms in split query to find quoted material, add each
		// term/quoted term to parsed terms\
		// a) special case: quoted term after a :
		// 3) cycle over terms in parsed terms
		// a) if an operator/parantheses, pass through unchanged (send to upper
		// case
		// for boolean)
		// b) if a fielded query (i.e. field:value), pass through unchanged
		// c) if not, construct query on all fields with this term

		// list of escape terms (i.e. quotes, operators) to be fed into query
		// untouched
		String escapeTerms = "\\+|\\-|\"|\\(|\\)";
		String booleanTerms = "and|AND|or|OR|not|NOT";

		// first cycle over the string to add artificial breaks before and after
		// control characters
		final String queryStr = (pfsParameter == null ? "" : pfsParameter
				.getQueryRestriction());

		String queryStr_mod = queryStr;
		queryStr_mod = queryStr_mod.replace("(", " ( ");
		queryStr_mod = queryStr_mod.replace(")", " ) ");
		queryStr_mod = queryStr_mod.replace("\"", " \" ");
		queryStr_mod = queryStr_mod.replace("+", " + ");
		queryStr_mod = queryStr_mod.replace("-", " - ");

		// remove any leading or trailing whitespace (otherwise first/last null
		// term
		// bug)
		queryStr_mod = queryStr_mod.trim();

		// split the string by white space and single-character operators
		String[] terms = queryStr_mod.split("\\s+");

		// merge items between quotation marks
		boolean exprInQuotes = false;
		List<String> parsedTerms = new ArrayList<>();
		// List<String> parsedTerms_temp = new ArrayList<String>();
		String currentTerm = "";

		// cycle over terms to identify quoted (i.e. non-parsed) terms
		for (int i = 0; i < terms.length; i++) {

			// if an open quote is detected
			if (terms[i].equals("\"")) {

				if (exprInQuotes == true) {

					// special case check: fielded term. Impossible for first
					// term to be
					// fielded.
					if (parsedTerms.size() == 0) {
						parsedTerms.add("\"" + currentTerm + "\"");
					} else {
						String lastParsedTerm = parsedTerms.get(parsedTerms
								.size() - 1);

						// if last parsed term ended with a colon, append this
						// term to the
						// last parsed term
						if (lastParsedTerm.endsWith(":") == true) {
							parsedTerms.set(parsedTerms.size() - 1,
									lastParsedTerm + "\"" + currentTerm + "\"");
						} else {
							parsedTerms.add("\"" + currentTerm + "\"");
						}
					}

					// reset current term
					currentTerm = "";
					exprInQuotes = false;

				} else {
					exprInQuotes = true;
				}

				// if no quote detected
			} else {

				// if inside quotes, continue building term
				if (exprInQuotes == true) {
					currentTerm = currentTerm == "" ? terms[i] : currentTerm
							+ " " + terms[i];

					// otherwise, add to parsed list
				} else {
					parsedTerms.add(terms[i]);
				}
			}
		}

		for (String s : parsedTerms) {
			Logger.getLogger(MappingServiceJpa.class).debug("  " + s);
		}

		// cycle over terms to construct query
		full_query = "";

		for (int i = 0; i < parsedTerms.size(); i++) {

			// if not the first term AND the last term was not an escape term
			// add whitespace separator
			if (i != 0 && !parsedTerms.get(i - 1).matches(escapeTerms)) {

				full_query += " ";
			}
			/*
			 * full_query += (i == 0 ? // check for first term "" : // -> if
			 * first character, add nothing
			 * parsedTerms.get(i-1).matches(escapeTerms) ? // check if last term
			 * was an escape character "": // -> if last term was an escape
			 * character, add nothing " "); // -> otherwise, add a separating
			 * space
			 */

			// if an escape character/sequence, add this term unmodified
			if (parsedTerms.get(i).matches(escapeTerms)) {

				full_query += parsedTerms.get(i);

				// else if a boolean character, add this term in upper-case form
				// (i.e.
				// lucene format)
			} else if (parsedTerms.get(i).matches(booleanTerms)) {

				full_query += parsedTerms.get(i).toUpperCase();

				// else if already a field-specific query term, add this term
				// unmodified
			} else if (parsedTerms.get(i).contains(":")) {

				full_query += parsedTerms.get(i);

				// otherwise, treat as unfielded query term
			} else {

				// open parenthetical term
				full_query += "(";

				// add fielded query for each indexed term, separated by OR
				Iterator<String> names_iter = fieldNames.iterator();
				while (names_iter.hasNext()) {
					full_query += names_iter.next() + ":" + parsedTerms.get(i);
					if (names_iter.hasNext())
						full_query += " OR ";
				}

				// close parenthetical term
				full_query += ")";
			}

			// if further terms remain in the sequence
			if (!(i == parsedTerms.size() - 1)) {

				// Add a separating OR iff:
				// - this term is not an escape character
				// - this term is not a boolean term
				// - next term is not a boolean term
				if (!parsedTerms.get(i).matches(escapeTerms)
						&& !parsedTerms.get(i).matches(booleanTerms)
						&& !parsedTerms.get(i + 1).matches(booleanTerms)) {

					full_query += " OR";
				}
			}
		}

		// add parantheses and map project constraint
		full_query = "(" + full_query + ")" + " AND mapProjectId:"
				+ mapProjectId;

		Logger.getLogger(MappingServiceJpa.class).debug(
				"Full query: " + full_query);

		return full_query;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#findConceptsInScope(org.
	 * ihtsdo.otf.mapping.model.MapProject)
	 */
	@Override
	public SearchResultList findConceptsInScope(Long mapProjectId,
			PfsParameter pfsParameter) throws Exception {
		Logger.getLogger(this.getClass()).info(
				"Find concepts in scope for " + mapProjectId);

		MapProject project = getMapProject(mapProjectId);
		SearchResultList conceptsInScope = new SearchResultListJpa();

		ContentService contentService = new ContentServiceJpa();

		String terminology = project.getSourceTerminology();
		String terminologyVersion = project.getSourceTerminologyVersion();

		// Avoid including the scope concepts themselves in the definition
		// if we are looking for descendants
		// e.g. "Clinical Finding" does not need to be mapped for SNOMED->ICD10
		if (!project.isScopeDescendantsFlag()) {
			Logger.getLogger(this.getClass()).info(
					"  Project not using scope descendants flag - "
							+ project.getScopeConcepts());
			for (String conceptId : project.getScopeConcepts()) {
				Concept c = contentService.getConcept(conceptId, terminology,
						terminologyVersion);
				if (c == null) {
					throw new Exception("Scope concept " + conceptId
							+ " does not exist.");
				}
				SearchResult sr = new SearchResultJpa();
				sr.setId(c.getId());
				sr.setTerminologyId(c.getTerminologyId());
				sr.setTerminology(c.getTerminology());
				sr.setTerminologyVersion(c.getTerminologyVersion());
				sr.setValue(c.getDefaultPreferredName());
				conceptsInScope.addSearchResult(sr);
			}
		}

		// Include descendants in scope.
		if (project.isScopeDescendantsFlag()) {
			Logger.getLogger(this.getClass()).info(
					"  Project using scope descendants flag");
			// for each scope concept, get descendants
			for (String terminologyId : project.getScopeConcepts()) {
				SearchResultList descendants = contentService
						.findDescendantConcepts(terminologyId, terminology,
								terminologyVersion, pfsParameter);

				Logger.getLogger(this.getClass()).info(
						"    Concept " + terminologyId + " has "
								+ descendants.getTotalCount()
								+ " descendants (" + descendants.getCount()
								+ " from getCount)");
				// cycle over descendants
				for (SearchResult sr : descendants.getSearchResults()) {
					conceptsInScope.addSearchResult(sr);
				}
			}
		}

		contentService.close();

		// get those excluded from scope
		SearchResultList excludedResultList = findConceptsExcludedFromScope(
				mapProjectId, pfsParameter);

		// remove those excluded from scope
		SearchResultList finalConceptsInScope = new SearchResultListJpa();
		for (SearchResult sr : conceptsInScope.getSearchResults()) {
			if (!excludedResultList.contains(sr)) {
				finalConceptsInScope.addSearchResult(sr);
			}
		}

		finalConceptsInScope.setTotalCount(finalConceptsInScope.getCount());
		Logger.getLogger(this.getClass()).info(
				"Finished getting scope concepts - "
						+ finalConceptsInScope.getTotalCount());

		/**
		 * PrintWriter writer = new PrintWriter("C:/data/inScopeConcepts.txt",
		 * "UTF-8"); for(SearchResult sr :
		 * finalConceptsInScope.getSearchResults()) {
		 * writer.println(sr.getTerminologyId()); } writer.close();
		 */

		return finalConceptsInScope;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#findUnmappedConceptsInScope
	 * (org.ihtsdo.otf.mapping.model.MapProject)
	 */
	@Override
	public SearchResultList findUnmappedConceptsInScope(Long mapProjectId,
			PfsParameter pfsParameter) throws Exception {
		Logger.getLogger(this.getClass()).info(
				"Find unmapped concepts in scope for " + mapProjectId);
		// Get in scope concepts
		SearchResultList conceptsInScope = findConceptsInScope(mapProjectId,
				pfsParameter);
		Logger.getLogger(this.getClass()).info(
				"  Project has " + conceptsInScope.getTotalCount()
						+ " concepts in scope");

		//
		// Look for concept ids that have publication records in the current
		// project
		//
		Set<String> mappedConcepts = new HashSet<>();
		for (MapRecord mapRecord : getMapRecordsForMapProject(mapProjectId)
				.getIterable()) {
			if (mapRecord.getWorkflowStatus().equals(WorkflowStatus.PUBLISHED)
					|| mapRecord.getWorkflowStatus().equals(
							WorkflowStatus.READY_FOR_PUBLICATION)) {
				mappedConcepts.add(mapRecord.getConceptId());
			}
		}

		//
		// Keep any search results that do not have mapped concepts
		//
		ContentService contentService = new ContentServiceJpa();
		SearchResultList unmappedConceptsInScope = new SearchResultListJpa();
		for (SearchResult sr : conceptsInScope.getSearchResults()) {

			if (!mappedConcepts.contains(sr.getTerminologyId())) {
				unmappedConceptsInScope.addSearchResult(sr);
			}

		}
		unmappedConceptsInScope.setTotalCount(unmappedConceptsInScope
				.getCount());
		contentService.close();

		Logger.getLogger(this.getClass()).info(
				"  Project has " + unmappedConceptsInScope.getTotalCount()
						+ " unmapped concepts in scope");

		return unmappedConceptsInScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#
	 * findMappedConceptsOutOfScopeBounds(java.lang.Long)
	 */
	@Override
	public SearchResultList findMappedConceptsOutOfScopeBounds(
			Long mapProjectId, PfsParameter pfsParameter) throws Exception {
		SearchResultList mappedConceptsOutOfBounds = new SearchResultListJpa();
		MapProject project = getMapProject(mapProjectId);
		List<MapRecord> mapRecordList = getMapRecordsForMapProject(mapProjectId)
				.getMapRecords();
		ContentService contentService = new ContentServiceJpa();

		for (MapRecord record : mapRecordList) {
			Concept c = contentService.getConcept(record.getConceptId(),
					project.getSourceTerminology(),
					project.getSourceTerminologyVersion());
			if (isConceptOutOfScopeBounds(c.getTerminologyId(), mapProjectId)) {
				SearchResult sr = new SearchResultJpa();
				sr.setId(c.getId());
				sr.setTerminologyId(c.getTerminologyId());
				sr.setTerminology(c.getTerminology());
				sr.setTerminologyVersion(c.getTerminologyVersion());
				sr.setValue(c.getDefaultPreferredName());
				mappedConceptsOutOfBounds.addSearchResult(sr);
			}

		}
		contentService.close();
		return mappedConceptsOutOfBounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#findConceptsExcludedFromScope
	 * (org.ihtsdo.otf.mapping.model.MapProject)
	 */
	@Override
	public SearchResultList findConceptsExcludedFromScope(Long mapProjectId,
			PfsParameter pfsParameter) throws Exception {
		SearchResultList conceptsExcludedFromScope = new SearchResultListJpa();

		ContentService contentService = new ContentServiceJpa();
		MapProject project = getMapProject(mapProjectId);
		String terminology = project.getSourceTerminology();
		String terminologyVersion = project.getSourceTerminologyVersion();

		// add specified excluded concepts
		for (String conceptId : project.getScopeExcludedConcepts()) {
			Concept c = contentService.getConcept(conceptId, terminology,
					terminologyVersion);
			if (c != null) {
				SearchResult sr = new SearchResultJpa();
				sr.setId(c.getId());
				sr.setTerminologyId(c.getTerminologyId());
				sr.setTerminology(c.getTerminology());
				sr.setTerminologyVersion(c.getTerminologyVersion());
				sr.setValue(c.getDefaultPreferredName());
				conceptsExcludedFromScope.addSearchResult(sr);
			}
		}

		// add descendant excluded concepts if indicated
		if (project.isScopeExcludedDescendantsFlag()) {

			// for each excluded scope concept, get descendants
			for (String terminologyId : project.getScopeExcludedConcepts()) {
				SearchResultList descendants = contentService
						.findDescendantConcepts(terminologyId, terminology,
								terminologyVersion, null);

				// cycle over descendants
				for (SearchResult sr : descendants.getSearchResults()) {
					conceptsExcludedFromScope.addSearchResult(sr);
				}
			}

		}

		contentService.close();
		conceptsExcludedFromScope.setTotalCount(conceptsExcludedFromScope
				.getCount());
		Logger.getLogger(this.getClass()).info(
				"Concepts excluded from scope "
						+ +conceptsExcludedFromScope.getTotalCount());
		return conceptsExcludedFromScope;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#isConceptInScope(org.ihtsdo
	 * .otf.mapping.rf2.Concept, org.ihtsdo.otf.mapping.model.MapProject)
	 */
	@Override
	public boolean isConceptInScope(String conceptId, Long mapProjectId)
			throws Exception {
		MapProject project = getMapProject(mapProjectId);
		// if directly matches preset scope concept return true
		for (String c : project.getScopeConcepts()) {
			if (c.equals(conceptId))
				return true;
		}
		// don't make contentService if no chance descendants meet conditions
		if (!project.isScopeDescendantsFlag()
				&& !project.isScopeDescendantsFlag())
			return false;

		ContentService contentService = new ContentServiceJpa();
		for (TreePosition tp : contentService.getTreePositionsWithDescendants(
				conceptId, project.getSourceTerminology(),
				project.getSourceTerminologyVersion()).getIterable()) {
			String ancestorPath = tp.getAncestorPath();
			if (project.isScopeExcludedDescendantsFlag()
					&& ancestorPath.contains(conceptId)) {
				continue;
			}
			if (project.isScopeDescendantsFlag()
					&& ancestorPath.contains(conceptId)) {
				contentService.close();
				return true;
			}
		}
		contentService.close();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#isConceptExcludedFromScope
	 * (org.ihtsdo.otf.mapping.rf2.Concept,
	 * org.ihtsdo.otf.mapping.model.MapProject)
	 */
	@Override
	public boolean isConceptExcludedFromScope(String conceptId,
			Long mapProjectId) throws Exception {
		MapProject project = getMapProject(mapProjectId);
		// if directly matches preset scope concept return true
		for (String c : project.getScopeExcludedConcepts()) {
			if (c.equals(conceptId))
				return true;
		}
		// don't make contentService if no chance descendants meet conditions
		if (!project.isScopeDescendantsFlag()
				&& !project.isScopeDescendantsFlag())
			return false;

		ContentService contentService = new ContentServiceJpa();
		for (TreePosition tp : contentService.getTreePositionsWithDescendants(
				conceptId, project.getSourceTerminology(),
				project.getSourceTerminologyVersion()).getIterable()) {
			String ancestorPath = tp.getAncestorPath();
			if (project.isScopeDescendantsFlag()
					&& ancestorPath.contains(conceptId)) {
				continue;
			}
			if (project.isScopeExcludedDescendantsFlag()
					&& ancestorPath.contains(conceptId)) {
				contentService.close();
				return true;
			}
		}
		contentService.close();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#isConceptOutOfScopeBounds
	 * (java.lang.String, java.lang.Long)
	 */
	@Override
	public boolean isConceptOutOfScopeBounds(String conceptId, Long mapProjectId)
			throws Exception {
		MapProject project = getMapProject(mapProjectId);
		// if directly matches preset scope concept return false
		for (String c : project.getScopeConcepts()) {
			if (c.equals(conceptId))
				return false;
		}

		ContentService contentService = new ContentServiceJpa();
		for (TreePosition tp : contentService.getTreePositionsWithDescendants(
				conceptId, project.getSourceTerminology(),
				project.getSourceTerminologyVersion()).getIterable()) {
			String ancestorPath = tp.getAncestorPath();
			if (project.isScopeDescendantsFlag()
					&& ancestorPath.contains(conceptId)) {
				return false;
			}
		}
		contentService.close();
		return true;
	}

	/**
	 * TODO: Is this used? Should require map project id
	 * 
	 * Given a concept, returns a list of descendant concepts that have no
	 * associated map record.
	 * 
	 * @param terminologyId
	 *            the terminology id
	 * @param terminology
	 *            the terminology
	 * @param terminologyVersion
	 *            the terminology version
	 * @param thresholdLlc
	 *            the maximum number of descendants a concept can have before it
	 *            is no longer considered a low-level concept (i.e. return an
	 *            empty list)
	 * @return the list of unmapped descendant concepts
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public SearchResultList findUnmappedDescendantsForConcept(
			String terminologyId, String terminology,
			String terminologyVersion, int thresholdLlc,
			PfsParameter pfsParameter) throws Exception {

		SearchResultList unmappedDescendants = new SearchResultListJpa();

		// get hierarchical rel
		MetadataService metadataService = new MetadataServiceJpa();
		Map<String, String> hierarchicalRelationshipTypeMap = metadataService
				.getHierarchicalRelationshipTypes(terminology,
						terminologyVersion);
		if (hierarchicalRelationshipTypeMap.keySet().size() > 1) {
			throw new IllegalStateException(
					"Map project source terminology has too many hierarchical relationship types - "
							+ terminology);
		}
		if (hierarchicalRelationshipTypeMap.keySet().size() < 1) {
			throw new IllegalStateException(
					"Map project source terminology has too few hierarchical relationship types - "
							+ terminology);
		}

		// get descendants -- no pfsParameter, want all results
		ContentService contentService = new ContentServiceJpa();
		SearchResultList descendants = contentService.findDescendantConcepts(
				terminologyId, terminology, terminologyVersion, null);

		// if number of descendants <= low-level concept threshold, treat as
		// high-level concept and report no unmapped
		if (descendants.getCount() <= thresholdLlc) {

			// cycle over descendants
			for (SearchResult sr : descendants.getSearchResults()) {

				// if descendant has no associated map records, add to list
				if (getMapRecordsForConcept(sr.getTerminologyId())
						.getTotalCount() == 0) {
					unmappedDescendants.addSearchResult(sr);
				}
			}
		}

		// close managers
		contentService.close();
		metadataService.close();

		return unmappedDescendants;

	}

	// //////////////////////////////
	// Services to be implemented //
	// //////////////////////////////

	// //////////////////////////
	// Addition services ///
	// //////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#addMapPrinciple(org.ihtsdo
	 * .otf.mapping.model.MapPrinciple)
	 */
	@Override
	public MapPrinciple addMapPrinciple(MapPrinciple mapPrinciple) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.persist(mapPrinciple);
			tx.commit();
		} else {
			manager.persist(mapPrinciple);
		}
		return mapPrinciple;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#addMapAdvice(org.ihtsdo.
	 * otf.mapping.model.MapAdvice)
	 */
	@Override
	public MapAdvice addMapAdvice(MapAdvice mapAdvice) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.persist(mapAdvice);
			tx.commit();
		} else {
			manager.persist(mapAdvice);
		}

		return mapAdvice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#addMapRelation(org.ihtsdo.
	 * otf.mapping.model.MapRelation)
	 */
	@Override
	public MapRelation addMapRelation(MapRelation mapRelation) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.persist(mapRelation);
			tx.commit();
		} else {
			manager.persist(mapRelation);
		}
		return mapRelation;
	}

	// //////////////////////////
	// Update services ///
	// //////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#updateMapPrinciple(org
	 * .ihtsdo .otf.mapping.model.MapPrinciple)
	 */
	@Override
	public void updateMapPrinciple(MapPrinciple mapPrinciple) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.merge(mapPrinciple);
			tx.commit();
			// manager.close();
		} else {
			manager.merge(mapPrinciple);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#updateMapAdvice(org.ihtsdo
	 * .otf.mapping.model.MapAdvice)
	 */
	@Override
	public void updateMapAdvice(MapAdvice mapAdvice) {
		if (getTransactionPerOperation()) {

			tx = manager.getTransaction();

			tx.begin();
			manager.merge(mapAdvice);
			tx.commit();
			// manager.close();
		} else {
			manager.merge(mapAdvice);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#updateMapRelation(org.
	 * ihtsdo .otf.mapping.model.MapRelation)
	 */
	@Override
	public void updateMapRelation(MapRelation mapRelation) {
		if (getTransactionPerOperation()) {

			tx = manager.getTransaction();

			tx.begin();
			manager.merge(mapRelation);
			tx.commit();
			// manager.close();
		} else {
			manager.merge(mapRelation);
		}
	}

	// //////////////////////////
	// Removal services ///
	// //////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#removeMapPrinciple(java
	 * .lang.Long)
	 */
	@Override
	public void removeMapPrinciple(Long mapPrincipleId) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			MapPrinciple mp = manager.find(MapPrincipleJpa.class,
					mapPrincipleId);
			if (manager.contains(mp)) {
				manager.remove(mp);
			} else {
				manager.remove(manager.merge(mp));
			}
			tx.commit();
		} else {
			MapPrinciple mp = manager.find(MapPrincipleJpa.class,
					mapPrincipleId);
			if (manager.contains(mp)) {
				manager.remove(mp);
			} else {
				manager.remove(manager.merge(mp));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#removeMapAdvice(java.lang
	 * .Long)
	 */
	@Override
	public void removeMapAdvice(Long mapAdviceId) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			MapAdvice ma = manager.find(MapAdviceJpa.class, mapAdviceId);
			if (manager.contains(ma)) {
				manager.remove(ma);
			} else {
				manager.remove(manager.merge(ma));
			}
			tx.commit();
		} else {
			MapAdvice ma = manager.find(MapAdviceJpa.class, mapAdviceId);
			if (manager.contains(ma)) {
				manager.remove(ma);
			} else {
				manager.remove(manager.merge(ma));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#removeMapRelation(java
	 * .lang.Long)
	 */
	@Override
	public void removeMapRelation(Long mapRelationId) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			MapRelation ma = manager.find(MapRelationJpa.class, mapRelationId);
			if (manager.contains(ma)) {
				manager.remove(ma);
			} else {
				manager.remove(manager.merge(ma));
			}
			tx.commit();
		} else {
			MapRelation ma = manager.find(MapRelationJpa.class, mapRelationId);
			if (manager.contains(ma)) {
				manager.remove(ma);
			} else {
				manager.remove(manager.merge(ma));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapPrinciple(java.lang
	 * .Long)
	 */
	@Override
	public MapPrinciple getMapPrinciple(Long id) throws Exception {

		javax.persistence.Query query = manager
				.createQuery("select m from MapPrincipleJpa m where id = :id");
		query.setParameter("id", id);
		return (MapPrinciple) query.getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#getMapPrinciples()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MapPrincipleList getMapPrinciples() {
		List<MapPrinciple> mapPrinciples = new ArrayList<>();

		javax.persistence.Query query = manager
				.createQuery("select m from MapPrincipleJpa m");

		// Try query
		mapPrinciples = query.getResultList();

		MapPrincipleListJpa mapPrincipleList = new MapPrincipleListJpa();
		mapPrincipleList.setMapPrinciples(mapPrinciples);
		mapPrincipleList.setTotalCount(mapPrinciples.size());
		return mapPrincipleList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#getMapAdvices()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MapAdviceList getMapAdvices() {
		List<MapAdvice> mapAdvices = new ArrayList<>();

		javax.persistence.Query query = manager
				.createQuery("select m from MapAdviceJpa m");

		// Try query
		mapAdvices = query.getResultList();
		MapAdviceListJpa mapAdviceList = new MapAdviceListJpa();
		mapAdviceList.setMapAdvices(mapAdvices);
		mapAdviceList.setTotalCount(mapAdvices.size());
		return mapAdviceList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#getMapRelations()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public MapRelationList getMapRelations() {
		List<MapRelation> mapRelations = new ArrayList<>();

		javax.persistence.Query query = manager
				.createQuery("select m from MapRelationJpa m");

		// Try query
		mapRelations = query.getResultList();
		mapRelations = query.getResultList();
		MapRelationListJpa mapRelationList = new MapRelationListJpa();
		mapRelationList.setMapRelations(mapRelations);
		mapRelationList.setTotalCount(mapRelations.size());

		return mapRelationList;
	}

	// ///////////////////////////////////////
	// / Services for Map Project Creation
	// ///////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#createMapRecordsForMapProject
	 * (java.lang.Long, org.ihtsdo.otf.mapping.helpers.WorkflowStatus)
	 */
	@Override
	public void createMapRecordsForMapProject(Long mapProjectId,
			WorkflowStatus workflowStatus) throws Exception {
		MapProject mapProject = getMapProject(mapProjectId);
		Logger.getLogger(MappingServiceJpa.class).info(
				"Called createMapRecordsForMapProject for project - "
						+ mapProjectId + " workflowStatus - " + workflowStatus);
		if (!getTransactionPerOperation()) {
			throw new IllegalStateException(
					"The application must let the service manage transactions for this method");
		}

		// get the loader user
		MapUser loaderUser = getMapUser("loader");

		// if loader user does not exist, add it
		if (loaderUser == null) {
			loaderUser = new MapUserJpa();
			loaderUser.setApplicationRole(MapUserRole.VIEWER);
			loaderUser.setUserName("loader");
			loaderUser.setName("Loader Record");
			loaderUser.setEmail("none");
			loaderUser = addMapUser(loaderUser);
		}

		// retrieve all complex map ref set members for mapProject
		javax.persistence.Query query = manager
				.createQuery("select r from ComplexMapRefSetMemberJpa r "
						+ "where r.refSetId = :refSetId order by r.concept.id, "
						+ "r.mapBlock, r.mapGroup, r.mapPriority");
		query.setParameter("refSetId", mapProject.getRefSetId());
		List<ComplexMapRefSetMember> complexMapRefSetMembers = new ArrayList<>();
		for (Object member : query.getResultList()) {
			ComplexMapRefSetMember refSetMember = (ComplexMapRefSetMember) member;
			complexMapRefSetMembers.add(refSetMember);
		}

		Logger.getLogger(MappingServiceJpa.class).warn(
				"  " + complexMapRefSetMembers.size()
						+ " complex map refset members found (some skipped)");
		createMapRecordsForMapProject(mapProjectId, loaderUser, complexMapRefSetMembers,
				workflowStatus);
	}

	// ONLY FOR TESTING PURPOSES
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#
	 * removeMapRecordsForMapProjectId (java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Long removeMapRecordsForMapProject(Long mapProjectId) {

		tx = manager.getTransaction();

		int nRecords = 0;
		tx.begin();
		List<MapRecord> records = manager
				.createQuery(
						"select m from MapRecordJpa m where m.mapProjectId = :mapProjectId")
				.setParameter("mapProjectId", mapProjectId).getResultList();

		for (MapRecord record : records) {

			// delete notes
			for (MapNote note : record.getMapNotes()) {
				if (manager.contains(note)) {
					manager.remove(note);
				} else {
					manager.remove(manager.merge(note));
				}
			}
			record.setMapNotes(null);

			// delete entries
			for (MapEntry entry : record.getMapEntries()) {

				// remove advices
				entry.setMapAdvices(null);

				// merge entry to remove principle/advice associations
				manager.merge(entry);

				// delete entry
				manager.remove(entry);

				nRecords++;
			}

			// remove principles
			record.setMapPrinciples(null);

			// merge record to remove principle associations
			manager.merge(record);

			// delete record
			manager.remove(record);
		}

		tx.commit();

		Logger.getLogger(this.getClass()).debug(
				Integer.toString(nRecords)
						+ " records deleted for map project id = "
						+ mapProjectId);

		return new Long(nRecords);

	}

	/**
	 * Helper function to call main routine with null value for sampling rate
	 */
	@Override
	public void createMapRecordsForMapProject(Long mapProjectId,
			MapUser mapUser,
			List<ComplexMapRefSetMember> complexMapRefSetMembers,
			WorkflowStatus workflowStatus) throws Exception {
		createMapRecordsForMapProject(mapProjectId, mapUser,
				complexMapRefSetMembers, workflowStatus, -1.0f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#createMapRecordsForMapProject
	 * (java.lang.Long, java.util.List)
	 */
	@Override
	public void createMapRecordsForMapProject(Long mapProjectId,
			MapUser mapUser,
			List<ComplexMapRefSetMember> complexMapRefSetMembers,
			WorkflowStatus workflowStatus, float samplingRate) throws Exception {
		MapProject mapProject = getMapProject(mapProjectId);
		Logger.getLogger(MappingServiceJpa.class).debug(
				"  Creating map records for map project - "
						+ mapProject.getName() + ", assigning workflow status "
						+ WorkflowStatus.PUBLISHED);

		// Verify application is letting the service manage transactions
		if (!getTransactionPerOperation()) {
			throw new IllegalStateException(
					"The application must let the service manage transactions for this method");
		}

		// Setup content service
		ContentService contentService = new ContentServiceJpa();

		// Get map relation id->name mapping
		MetadataService metadataService = new MetadataServiceJpa();

		Map<String, String> relationIdNameMap = metadataService
				.getMapRelations(mapProject.getSourceTerminology(),
						mapProject.getSourceTerminologyVersion());
		Logger.getLogger(MappingServiceJpa.class).debug(
				"    relationIdNameMap = " + relationIdNameMap);

		// use the map relation id->name mapping to construct a hash set of
		// MapRelations
		Map<String, MapRelation> mapRelationIdMap = new HashMap<>();
		for (MapRelation mapRelation : getMapRelations().getIterable()) {
			mapRelationIdMap.put(mapRelation.getTerminologyId(), mapRelation);
		}

		Map<String, String> hierarchicalRelationshipTypeMap = metadataService
				.getHierarchicalRelationshipTypes(
						mapProject.getSourceTerminology(),
						mapProject.getSourceTerminologyVersion());
		if (hierarchicalRelationshipTypeMap.keySet().size() > 1) {
			throw new IllegalStateException(
					"Map project source terminology has too many hierarchical relationship types - "
							+ mapProject.getSourceTerminology());
		}
		if (hierarchicalRelationshipTypeMap.keySet().size() < 1) {
			throw new IllegalStateException(
					"Map project source terminology has too few hierarchical relationship types - "
							+ mapProject.getSourceTerminology());
		}

		boolean prevTransactionPerOperationSetting = getTransactionPerOperation();
		setTransactionPerOperation(false);
		beginTransaction();
		MapAdviceList mapAdvices = getMapAdvices();
		int mapPriorityCt = 0;
		int prevMapGroup = 0;
		
		// sampling tracker variables
		int samplingRecordsCreated = 0;
		int samplingRecordsPublished = 0;
		try {
			// instantiate other local variables
			String prevConceptId = null;
			MapRecord mapRecord = null;
			int ct = 0;
			MapUser loaderUser = getMapUser("loader");
			Random random = new Random();

			if (loaderUser == null) {
				throw new Exception("Loader user could not be found");
			}

			for (ComplexMapRefSetMember refSetMember : complexMapRefSetMembers) {

				// Skip inactive cases
				if (!refSetMember.isActive()) {
					Logger.getLogger(MappingServiceJpa.class).debug(
							"Skipping refset member "
									+ refSetMember.getTerminologyId());
					continue;
				}

				// Skip concept exclusion rules in all cases
				if (refSetMember.getMapRule().matches(
						"IFA\\s\\d*\\s\\|.*\\s\\|")
						&& !(refSetMember.getMapAdvice()
								.contains("MAP IS CONTEXT DEPENDENT FOR GENDER"))
						&& !(refSetMember.getMapRule()
								.matches("IFA\\s\\d*\\s\\|\\s.*\\s\\|\\s[<>]"))) {
					Logger.getLogger(MappingServiceJpa.class).debug(
							"    Skipping refset member exclusion rule "
									+ refSetMember.getTerminologyId());
					continue;
				}

				// retrieve the concept
				Logger.getLogger(MappingServiceJpa.class).debug(
						"    Get refset member concept");
				Concept concept = refSetMember.getConcept();

				// if no concept for this ref set member, skip
				if (concept == null) {
					continue;
					/*
					 * throw new NoResultException(
					 * "    Concept is unexpectedly missing for " +
					 * refSetMember.getTerminologyId());
					 */
				}

				// if different concept than previous ref set member, create new
				// mapRecord
				if (!concept.getTerminologyId().equals(prevConceptId)) {
					Logger.getLogger(MappingServiceJpa.class).debug(
							"    Creating map record for "
									+ concept.getTerminologyId());

					mapPriorityCt = 0;
					prevMapGroup = 0;
					mapRecord = new MapRecordJpa();
					mapRecord.setConceptId(concept.getTerminologyId());
					mapRecord.setConceptName(concept.getDefaultPreferredName());
					mapRecord.setMapProjectId(mapProject.getId());

					// get the number of descendants - Need to optimize this
					// Need a tool to compute and save this for LLCs (e.g.
					// having < 11
					// descendants)
					PfsParameter pfsParameter = new PfsParameterJpa();
					pfsParameter.setMaxResults(100);

					TreePositionList treePositionList = contentService
							.getTreePositionsWithDescendants(
									concept.getTerminologyId(),
									concept.getTerminology(),
									concept.getTerminologyVersion());
					long descCt = 0;
					if (treePositionList.getCount() > 0) {
						descCt = treePositionList.getTreePositions().get(0)
								.getDescendantCount();
					}
					mapRecord.setCountDescendantConcepts(descCt);
					Logger.getLogger(MappingServiceJpa.class).debug(
							"      Computing descendant ct = "
									+ mapRecord.getCountDescendantConcepts());

					// set the previous concept to this concept
					prevConceptId = refSetMember.getConcept()
							.getTerminologyId();

					// set the owner and lastModifiedBy user fields to
					// loaderUser
					mapRecord.setOwner(loaderUser);
					mapRecord.setLastModifiedBy(loaderUser);

					// random determine workflow state
					// based on sampling percentage
					// NOTE: Explicit equality check for -1.0f put in to avoid
					// any possible errors
					// in multiplication/division/comparison
					if (samplingRate != -1.0f
							&& random.nextInt(100 + 1) / 100.0 <= samplingRate) {
						samplingRecordsCreated++;
						mapRecord.setWorkflowStatus(workflowStatus);
					} else {
						samplingRecordsPublished++;
						mapRecord.setWorkflowStatus(WorkflowStatus.PUBLISHED);
					}

					// persist the record
					addMapRecord(mapRecord);

					if (++ct % 500 == 0) {
						Logger.getLogger(MappingServiceJpa.class).info(
								"    " + ct + " records created");
						commit();
						beginTransaction();
						// For memory management, avoid keeping cache of tree
						// positions
						contentService.close();
						contentService = new ContentServiceJpa();
					}
				}

				// check if target is in desired terminology; if so, create
				// entry
				String targetName = null;

				if (!refSetMember.getMapTarget().equals("")) {
					Concept c = contentService.getConcept(
							refSetMember.getMapTarget(),
							mapProject.getDestinationTerminology(),
							mapProject.getDestinationTerminologyVersion());
					if (c == null) {
						targetName = "Target name could not be determined";
					} else {
						targetName = c.getDefaultPreferredName();
					}
					Logger.getLogger(this.getClass()).debug(
							"      Setting target name " + targetName);
				}

				// Set map relation id as well from the cache
				String relationName = null;
				if (refSetMember.getMapRelationId() != null) {
					relationName = relationIdNameMap.get(refSetMember
							.getMapRelationId());
					Logger.getLogger(this.getClass()).debug(
							"      Look up relation name = " + relationName);
				}

				Logger.getLogger(this.getClass()).debug(
						"      Create map entry");
				MapEntry mapEntry = new MapEntryJpa();
				mapEntry.setTargetId(refSetMember.getMapTarget());
				mapEntry.setTargetName(targetName);
				mapEntry.setMapRecord(mapRecord);
				mapEntry.setMapRelation(mapRelationIdMap.get(refSetMember
						.getMapRelationId().toString()));
				String rule = refSetMember.getMapRule();
				if (rule.equals("OTHERWISE TRUE"))
					rule = "TRUE";
				mapEntry.setRule(rule);
				mapEntry.setMapBlock(refSetMember.getMapBlock());
				mapEntry.setMapGroup(refSetMember.getMapGroup());
				if (prevMapGroup != refSetMember.getMapGroup()) {
					mapPriorityCt = 0;
					prevMapGroup = refSetMember.getMapGroup();
				}
				// Increment map priority as we go through records
				mapEntry.setMapPriority(++mapPriorityCt);

				mapRecord.addMapEntry(mapEntry);

				// Add support for advices - and there can be multiple map
				// advice values
				// Only add advice if it is an allowable value and doesn't match
				// relation name
				// This should automatically exclude IFA/ALWAYS advice
				Logger.getLogger(this.getClass()).debug(
						"      Setting map advice");
				if (refSetMember.getMapAdvice() != null
						&& !refSetMember.getMapAdvice().equals("")) {
					for (MapAdvice ma : mapAdvices.getIterable()) {
						if (refSetMember.getMapAdvice().indexOf(ma.getName()) != -1
								&& !ma.getName().equals(relationName)) {
							mapEntry.addMapAdvice(ma);
							Logger.getLogger(this.getClass()).debug(
									"    " + ma.getName());
						}
					}
				}
			}
			
			Logger.getLogger(MappingServiceJpa.class).info(
					"    " + ct + " records created");
			if (samplingRate != -1.0f) {
				Logger.getLogger(MappingServiceJpa.class).info(
						"    " + samplingRecordsCreated + " records set to " + workflowStatus);
				Logger.getLogger(MappingServiceJpa.class).info(
						"    " + samplingRecordsPublished + " records set to PUBLISHED");
			}

			commit();
			contentService.close();
			metadataService.close();
		} catch (Exception e) {
			setTransactionPerOperation(prevTransactionPerOperationSetting);
			throw e;
		}
		setTransactionPerOperation(prevTransactionPerOperationSetting);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getTransactionPerOperation
	 * ()
	 */
	@Override
	public boolean getTransactionPerOperation() {
		return transactionPerOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#setTransactionPerOperation
	 * (boolean)
	 */
	@Override
	public void setTransactionPerOperation(boolean transactionPerOperation) {
		this.transactionPerOperation = transactionPerOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#beginTransaction()
	 */
	@Override
	public void beginTransaction() {

		if (getTransactionPerOperation())
			throw new IllegalStateException(
					"Error attempting to begin a transaction when using transactions per operation mode.");
		else if (tx != null && tx.isActive())
			throw new IllegalStateException(
					"Error attempting to begin a transaction when there "
							+ "is already an active transaction");
		tx = manager.getTransaction();
		tx.begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#commit()
	 */
	@Override
	public void commit() {

		if (getTransactionPerOperation())
			throw new IllegalStateException(
					"Error attempting to commit a transaction when using transactions per operation mode.");
		else if (tx != null && !tx.isActive())
			throw new IllegalStateException(
					"Error attempting to commit a transaction when there "
							+ "is no active transaction");
		tx.commit();
	}

	// ////////////////////////
	// AGE RANGE FUNCTIONS
	// ////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.otf.mapping.services.MappingService#getMapAgeRanges()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapAgeRangeList getMapAgeRanges() {

		// construct query
		javax.persistence.Query query = manager
				.createQuery("select m from MapAgeRangeJpa m");

		MapAgeRangeList m = new MapAgeRangeListJpa();
		m.setMapAgeRanges(query.getResultList());

		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#addMapAgeRange(org.ihtsdo
	 * .otf.mapping.model.MapAgeRange)
	 */
	@Override
	public MapAgeRange addMapAgeRange(MapAgeRange mapAgeRange) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.persist(mapAgeRange);
			tx.commit();

		} else {
			manager.persist(mapAgeRange);
		}

		return mapAgeRange;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#removeMapAgeRange(java
	 * .lang.Long)
	 */
	@Override
	public void removeMapAgeRange(Long mapAgeRangeId) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			MapAgeRange mar = manager.find(MapAgeRangeJpa.class, mapAgeRangeId);
			if (manager.contains(mar)) {
				manager.remove(mar);
			} else {
				manager.remove(manager.merge(mar));
			}
			tx.commit();
		} else {
			MapAgeRange mar = manager.find(MapAgeRangeJpa.class, mapAgeRangeId);
			if (manager.contains(mar)) {
				manager.remove(mar);
			} else {
				manager.remove(manager.merge(mar));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#updateMapAgeRange(org.
	 * ihtsdo .otf.mapping.model.MapAgeRange)
	 */
	@Override
	public void updateMapAgeRange(MapAgeRange mapAgeRange) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.merge(mapAgeRange);
			tx.commit();
		} else {
			manager.merge(mapAgeRange);
		}
	}

	// /////////////////////////////////////
	// MAP USER PREFERENCES FUNCTIONS
	// /////////////////////////////////////

	@Override
	public MapUserPreferences getMapUserPreferences(String userName)
			throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"Finding user " + userName);
		MapUser mapUser = getMapUser(userName);
		javax.persistence.Query query = manager
				.createQuery(
						"select m from MapUserPreferencesJpa m where mapUser_id = :mapUser_id")
				.setParameter("mapUser_id", mapUser.getId());

		MapUserPreferences m;
		try {
			m = (MapUserPreferences) query.getSingleResult();
		}

		// catch no result exception and create default user preferences
		catch (NoResultException e) {

			// create object
			m = new MapUserPreferencesJpa();
			m.setMapUser(mapUser); // set the map user
			MapProjectList mapProjects = getMapProjects();
			m.setLastMapProjectId(mapProjects.getIterable().iterator().next()
					.getId()); // set a default project to 1st project found

			// add object
			addMapUserPreferences(m);
		}

		// return preferences
		return m;
	}

	/**
	 * Retrieve all map user preferences
	 * 
	 * @return a List of MapUserPreferencess
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MapUserPreferencesList getMapUserPreferences() {

		List<MapUserPreferences> m = null;

		// construct query
		javax.persistence.Query query = manager
				.createQuery("select m from MapUserPreferencesJpa m");

		// Try query

		m = query.getResultList();
		MapUserPreferencesListJpa mapUserPreferencesList = new MapUserPreferencesListJpa();
		mapUserPreferencesList.setMapUserPreferences(m);
		mapUserPreferencesList.setTotalCount(m.size());
		return mapUserPreferencesList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#addMapUserPreferences(
	 * org.ihtsdo.otf.mapping.model.MapUserPreferences)
	 */
	@Override
	public MapUserPreferences addMapUserPreferences(
			MapUserPreferences mapUserPreferences) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.persist(mapUserPreferences);
			tx.commit();

		} else {
			manager.persist(mapUserPreferences);
		}

		return mapUserPreferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#removeMapUserPreferences
	 * (java.lang.Long)
	 */
	@Override
	public void removeMapUserPreferences(Long mapUserPreferencesId) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();
			tx.begin();
			MapUserPreferences mar = manager.find(MapUserPreferencesJpa.class,
					mapUserPreferencesId);
			if (manager.contains(mar)) {
				manager.remove(mar);
			} else {
				manager.remove(manager.merge(mar));
			}
			tx.commit();
		} else {
			MapUserPreferences mar = manager.find(MapUserPreferencesJpa.class,
					mapUserPreferencesId);
			if (manager.contains(mar)) {
				manager.remove(mar);
			} else {
				manager.remove(manager.merge(mar));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#updateMapUserPreferences
	 * (org.ihtsdo .otf.mapping.model.MapUserPreferences)
	 */
	@Override
	public void updateMapUserPreferences(MapUserPreferences mapUserPreferences) {
		if (getTransactionPerOperation()) {
			tx = manager.getTransaction();

			tx.begin();
			manager.merge(mapUserPreferences);
			tx.commit();
		} else {
			manager.merge(mapUserPreferences);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#getMapUserRole(java.lang
	 * .String, java.lang.Long)
	 */
	@Override
	public MapUserRole getMapUserRoleForMapProject(String userName,
			Long mapProjectId) throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"Finding user's role " + userName + " " + mapProjectId);

		// get the user and map project for parameters
		MapUser mapUser = getMapUser(userName);
		MapProject mapProject = getMapProject(mapProjectId);

		// check which collection this user belongs to for this project
		if (mapProject.getMapAdministrators().contains(mapUser)) {
			return MapUserRole.ADMINISTRATOR;
		} else if (mapProject.getMapLeads().contains(mapUser)) {
			return MapUserRole.LEAD;
		} else if (mapProject.getMapSpecialists().contains(mapUser)) {
			return MapUserRole.SPECIALIST;
		}

		// default role is Viewer
		return MapUserRole.VIEWER;
	}

	@Override
	@XmlTransient
	public ProjectSpecificAlgorithmHandler getProjectSpecificAlgorithmHandler(
			MapProject mapProject) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		ProjectSpecificAlgorithmHandler algorithmHandler = (ProjectSpecificAlgorithmHandler) Class
				.forName(mapProject.getProjectSpecificAlgorithmHandlerClass())
				.newInstance();

		algorithmHandler.setMapProject(mapProject);
		return algorithmHandler;

	}

	/**
	 * Sets the valid field for tree positions, given a map project id.
	 * 
	 * @param treePositions
	 *            the tree positions
	 * @param mapProjectId
	 *            the map project id
	 * @return the revised list of tree positions
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public TreePositionList setTreePositionValidCodes(
			List<TreePosition> treePositions, Long mapProjectId)
			throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"Setting tree position valid codes");

		// get the map project and its algorithm handler
		MapProject mapProject = getMapProject(mapProjectId);
		ProjectSpecificAlgorithmHandler algorithmHandler = getProjectSpecificAlgorithmHandler(mapProject);

		setTreePositionValidCodesHelper(treePositions, algorithmHandler);

		TreePositionListJpa treePositionList = new TreePositionListJpa();
		treePositionList.setTreePositions(treePositions);
		treePositionList.setTotalCount(treePositions.size());
		return treePositionList;
	}

	/**
	 * Helper function to recursively cycle over nodes and their children.
	 * Instantiated to prevent necessity for retrieving algorithm handler at
	 * each level. Note: Not necessary to return objects, tree positions are
	 * persisted objects
	 * 
	 * @param treePositions
	 *            the tree positions
	 * @param algorithmHandler
	 *            the algorithm handler
	 * @throws Exception
	 *             the exception
	 */
	public void setTreePositionValidCodesHelper(
			List<TreePosition> treePositions,
			ProjectSpecificAlgorithmHandler algorithmHandler) throws Exception {

		// cycle over all tree positions and check target code, recursively
		// cycle over children
		for (TreePosition tp : treePositions) {

			tp.setValid(algorithmHandler.isTargetCodeValid(tp
					.getTerminologyId()));

			setTreePositionValidCodesHelper(tp.getChildren(), algorithmHandler);
		}
	}

	@Override
	public TreePositionList setTreePositionTerminologyNotes(
			List<TreePosition> treePositions, Long mapProjectId)
			throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"Setting tree position terminology notes");

		// get the map project and its algorithm handler
		MapProject mapProject = getMapProject(mapProjectId);
		ProjectSpecificAlgorithmHandler algorithmHandler = getProjectSpecificAlgorithmHandler(mapProject);

		// construct the tree position list
		TreePositionListJpa treePositionList = new TreePositionListJpa();
		treePositionList.setTreePositions(treePositions);
		treePositionList.setTotalCount(treePositions.size());

		// compute the target terminology notes
		algorithmHandler.computeTargetTerminologyNotes(treePositionList);

		return treePositionList;
	}

	@SuppressWarnings("unused")
	@Override
	public MapRecordList getOriginMapRecordsForConflict(Long mapRecordId)
			throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"getRecordsInConflict with record id = "
						+ mapRecordId.toString());

		MapRecordList conflictRecords = new MapRecordListJpa();

		MapRecord mapRecord = getMapRecord(mapRecordId);
		MapProject mapProject = getMapProject(mapRecord.getMapProjectId());

		if (mapRecord == null)
			throw new Exception(
					"getRecordsInConflict: Could not find map record with id = "
							+ mapRecordId.toString() + "!");

		// if a conflict between two specialists, retrieve the CONFLICT_DETECTED
		// records
		if (mapRecord.getWorkflowStatus().equals(WorkflowStatus.CONFLICT_NEW)
				|| mapRecord.getWorkflowStatus().equals(
						WorkflowStatus.CONFLICT_IN_PROGRESS)
				|| mapRecord.getWorkflowStatus().equals(
						WorkflowStatus.CONFLICT_RESOLVED)) {

			// As with review record below, this try/catch block is a
			// method to handle situations where originId set has more than
			// two elements (i.e. is unordered)
			// and where records retrieved are no longer in the database (e.g.
			// in
			// audit history)
			try {
				for (Long originId : mapRecord.getOriginIds()) {
					MapRecord mr = getMapRecord(originId);
					if (mr.getWorkflowStatus().equals(
							WorkflowStatus.CONFLICT_DETECTED)) {
						conflictRecords.addMapRecord(getMapRecord(originId));
					}
				}

				if (conflictRecords.getCount() == 2) {
					conflictRecords.setTotalCount(conflictRecords.getCount());
					return conflictRecords;
				}
			} catch (Exception e) {
				// do nothing
			}

		} else if ((mapProject.getWorkflowType().equals("CONFLICT_PROJECT") && (mapRecord
				.getWorkflowStatus().equals(WorkflowStatus.REVIEW_NEW)
				|| mapRecord.getWorkflowStatus().equals(
						WorkflowStatus.REVIEW_IN_PROGRESS) || mapRecord
				.getWorkflowStatus().equals(WorkflowStatus.REVIEW_RESOLVED)))

				||

				(mapProject.getWorkflowType().equals("REVIEW_PROJECT") && mapRecord
						.getOriginIds().size() > 2)) {

			boolean foundReviewRecord = false; // the specialist's completed
												// work
			boolean foundRevisionRecord = false; // the original published work

			for (Long originId : mapRecord.getOriginIds()) {
				System.out.println("Getting origin id:  " + originId);
				MapRecord mr = getMapRecord(originId);

				// This try/cactch block is here to prevent problems
				// with the origin ids being an unordered set
				// Only records currently in the database are returned
				try {

					if (mr.getWorkflowStatus().equals(
							WorkflowStatus.REVIEW_NEEDED)) {
						conflictRecords.addMapRecord(getMapRecord(originId));
						foundReviewRecord = true;
					} else if (mr.getWorkflowStatus().equals(
							WorkflowStatus.REVISION)) {
						conflictRecords.addMapRecord(getMapRecord(originId));
						foundRevisionRecord = true;
					}

				} catch (Exception e) {
					// do nothing
				}

				// once records are found, stop processing origin ids
				if (foundReviewRecord == true && foundRevisionRecord == true) {
					conflictRecords.setTotalCount(conflictRecords.getCount());
					return conflictRecords;
				}

			}

		} else if (mapProject.getWorkflowType().equals("REVIEW_PROJECT")
				&& mapRecord.getWorkflowStatus().equals(
						WorkflowStatus.REVIEW_NEW)
				|| mapRecord.getWorkflowStatus().equals(
						WorkflowStatus.REVIEW_IN_PROGRESS)
				|| mapRecord.getWorkflowStatus().equals(
						WorkflowStatus.REVIEW_RESOLVED)) {

			System.out.println("Getting origin id for REVIEW_PROJECT record");

			WorkflowService workflowService = new WorkflowServiceJpa();

			TrackingRecord tr = workflowService
					.getTrackingRecordForMapProjectAndConcept(mapProject,
							mapRecord.getConceptId());

			if (tr.getWorkflowPath().equals(WorkflowPath.REVIEW_PROJECT_PATH)) {

				for (Long originId : mapRecord.getOriginIds()) {

					try {
						MapRecord mr = getMapRecord(mapRecord.getOriginIds()
								.iterator().next());

						// check assumption
						if (!mr.getWorkflowStatus().equals(
								WorkflowStatus.REVIEW_NEEDED)) {
							throw new Exception(
									"Single origin record found for review, but was not REVIEW_NEEDED");
						}

						// add and return this record
						conflictRecords.addMapRecord(mr);
						conflictRecords.setTotalCount(conflictRecords
								.getCount());

						return conflictRecords;

					} catch (Exception e) {
						// do nothing
					}

				}

			} else if (tr.getWorkflowPath().equals(WorkflowPath.FIX_ERROR_PATH)) {

				boolean foundReviewRecord = false; // the specialist's completed
				// work
				boolean foundRevisionRecord = false; // the original published
														// work

				for (Long originId : mapRecord.getOriginIds()) {
					System.out.println("Getting origin id:  " + originId);
					MapRecord mr = getMapRecord(originId);

					// As with other try blocks in this section, this
					// try/catch block is implemented to handle situations
					// where the unordered originIds set leads to attempts
					// to retrieve records that no longer exist
					try {

						if (mr.getWorkflowStatus().equals(
								WorkflowStatus.REVIEW_NEEDED)) {
							conflictRecords
									.addMapRecord(getMapRecord(originId));
							foundReviewRecord = true;
						} else if (mr.getWorkflowStatus().equals(
								WorkflowStatus.REVISION)) {
							conflictRecords
									.addMapRecord(getMapRecord(originId));
							foundRevisionRecord = true;
						}

					} catch (Exception e) {
						// do nothing, attempted to find a record that no longer
						// exists
					}

					// once records are found, stop processing origin ids
					if (foundReviewRecord == true
							&& foundRevisionRecord == true) {
						conflictRecords.setTotalCount(conflictRecords
								.getCount());
						return conflictRecords;
					}

				}
			} else {
				throw new Exception(
						"Could not retrieve exactly one origin id for REVIEW_PROJECT path");
			}

		} else {
			throw new Exception(
					"Invalid map record passed to conflict origins routine");
		}

		return conflictRecords;
	}

	/**
	 * Validate that a single user cannot have more than one role on a
	 * particular map project.
	 * 
	 * @param mapProject
	 *            the map project
	 * @throws Exception
	 *             the exception
	 */
	private void validateUserAndRole(MapProject mapProject) throws Exception {
		Map<MapUser, String> userToRoleMap = new HashMap<>();
		for (MapUser user : mapProject.getMapLeads()) {
			// if user is already in map, throw exception
			if (userToRoleMap.containsKey(user))
				throw new IllegalStateException("Error: User " + user.getName()
						+ " has more than one role.");
			else
				userToRoleMap.put(user, "lead");
		}
		for (MapUser user : mapProject.getMapSpecialists()) {
			// if user is already in map, throw exception
			if (userToRoleMap.containsKey(user))
				throw new IllegalStateException("Error: User " + user.getName()
						+ " has more than one role.");
			else
				userToRoleMap.put(user, "specialist");
		}
		for (MapUser user : mapProject.getMapAdministrators()) {
			// if user is already in map, throw exception
			if (userToRoleMap.containsKey(user))
				throw new IllegalStateException("Error: User " + user.getName()
						+ " has more than one role.");
			else
				userToRoleMap.put(user, "administrator");
		}

	}

	/**
	 * Handle map record lazy initialization.
	 * 
	 * @param mapRecord
	 *            the map record
	 */
	private void handleMapRecordLazyInitialization(MapRecord mapRecord) {
		// handle all lazy initializations
		mapRecord.getOwner().getEmail();
		mapRecord.getLastModifiedBy().getEmail();
		mapRecord.getMapNotes().size();
		mapRecord.getMapPrinciples().size();
		mapRecord.getOriginIds().size();
		for (MapEntry mapEntry : mapRecord.getMapEntries()) {
			if (mapEntry.getMapRelation() != null)
				mapEntry.getMapRelation().getName();
			mapEntry.getMapAdvices().size();
		}

	}

	/**
	 * Handle map project lazy initialization.
	 * 
	 * @param mapProject
	 *            the map project
	 */
	private void handleMapProjectLazyInitialization(MapProject mapProject) {
		// handle all lazy initializations
		mapProject.getScopeConcepts().size();
		mapProject.getScopeExcludedConcepts().size();
		mapProject.getMapAdvices().size();
		mapProject.getMapRelations().size();
		mapProject.getMapLeads().size();
		mapProject.getMapSpecialists().size();
		mapProject.getMapPrinciples().size();
		mapProject.getPresetAgeRanges().size();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.otf.mapping.services.MappingService#checkMapGroupsForMapProject
	 * (org.ihtsdo.otf.mapping.model.MapProject)
	 */
	@Override
	public void checkMapGroupsForMapProject(MapProject mapProject,
			boolean updateRecords) throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"Checking map group numbering for project "
						+ mapProject.getName());
		Logger.getLogger(MappingServiceJpa.class).info(
				"  Mode: " + (updateRecords ? "Update" : "Check"));

		MapRecordList mapRecordsInProject = this
				.getMapRecordsForMapProject(mapProject.getId());

		Logger.getLogger(MappingServiceJpa.class).info(
				"Checking " + mapRecordsInProject.getCount() + " map records.");

		// logging variables
		int nRecordsChecked = 0;
		int nRecordsRemapped = 0;
		int nMessageInterval = (int) Math
				.floor(mapRecordsInProject.getCount() / 10);

		// cycle over all records
		for (MapRecord mapRecord : mapRecordsInProject.getIterable()) {

			// create a map representing oldGroup -> newGroup
			List<Integer> mapGroupsFound = new ArrayList<>();

			// map of remappings
			Map<Integer, Integer> mapGroupRemapping = new HashMap<>();

			// find the existing groups
			for (MapEntry mapEntry : mapRecord.getMapEntries()) {

				// if this group not already present, add to list
				if (!mapGroupsFound.contains(mapEntry.getMapGroup()))
					mapGroupsFound.add(mapEntry.getMapGroup());
			}

			// sort the groups found
			Collections.sort(mapGroupsFound);

			// get the total number of groups present
			int nMapGroups = mapGroupsFound.size();

			// if no groups at all, skip this record
			if (nMapGroups > 0) {

				// flag for whether map record needs to be modified
				boolean mapGroupsRemapped = false;

				// shorthand the min/max values
				int minGroup = Collections.min(mapGroupsFound);
				int maxGroup = Collections.max(mapGroupsFound);

				// if the max group is not equal to the number of groups
				// or the min group is not equal to 1
				if (maxGroup != nMapGroups || minGroup != 1) {

					mapGroupsRemapped = true;

					// counter for groups
					int cumMissingGroups = 0;

					// cycle over all group values from 0 to max group
					for (int i = 0; i <= maxGroup; i++) {

						// if this group present,
						// - remove the group from set
						// - subtract current value by the cumulative number of
						// missed groups found
						// - add 1 and subtract the value of the min group
						// - re-add the new remapped group
						// otherwise
						// - increment the missing group counter
						//
						// e.g. (0, 3, 5) goes through the following steps:
						// 0 -> 0 - 0 + 1 = 1 -> map as (0, 1)
						// 1 -> not present, increment offset
						// 2 -> not present, increment offset
						// 3 -> 3 - 2 + 1 = 2 -> map as (3, 2)
						// 4 -> not present, increment offset
						// 5 -> 5 - 3 + 1 = 3 -> map as (5, 3)
						//
						// Note that for this algorithm, zero is considered a
						// "missing group" if not present
						// 0 -> not present, increment offset
						// 1 -> 1 - 1 + 1 = 1 -> map as (1, 1)
						if (mapGroupsFound.contains(i)) {
							mapGroupRemapping.put(i, i - cumMissingGroups + 1);
						} else {
							cumMissingGroups++;
						}
					}

				}

				// if errors detected, log
				if (mapGroupsRemapped == true) {

					nRecordsRemapped++;

					Logger.getLogger(MappingServiceJpa.class).info(
							"Record requires remapping:  " + mapRecord.getId()
									+ ": " + mapRecord.getConceptId() + ", "
									+ mapRecord.getConceptName());

					String mapLogStr = "";
					for (Integer i : mapGroupRemapping.keySet()) {
						mapLogStr += " " + i + "->" + mapGroupRemapping.get(i);
					}

					Logger.getLogger(MappingServiceJpa.class).info(
							"  Groups to remap: " + mapLogStr);
				}

				// if errors detected and update mode specified, update
				if (mapGroupsRemapped == true && updateRecords == true) {

					this.handleMapRecordLazyInitialization(mapRecord);

					for (MapEntry me : mapRecord.getMapEntries()) {
						if (mapGroupRemapping.containsKey(me.getMapGroup())) {
							me.setMapGroup(mapGroupRemapping.get(me
									.getMapGroup()));
						}
					}

					Logger.getLogger(MappingServiceJpa.class).info(
							"  Updating record.");
					this.updateMapRecord(mapRecord);

				}

				// output logging information
				if (++nRecordsChecked % nMessageInterval == 0) {
					Logger.getLogger(MappingServiceJpa.class).info(
							"  " + nRecordsChecked + " records processed ("
									+ (nRecordsChecked / nMessageInterval * 10)
									+ "%), " + nRecordsRemapped
									+ " with group errors");
				}
			}
		}

		Logger.getLogger(MappingServiceJpa.class).info(
				"  " + nRecordsChecked + " total records processed ("
						+ nRecordsRemapped + " with group errors");
	}

	@Override
	public void processRelease(MapProject mapProject, String outputFileName,
			String effectiveTime, String moduleId) throws Exception {

		Logger.getLogger(MappingServiceJpa.class).info(
				"Processing publication release for project "
						+ mapProject.getName());

		// check file directory exists and open writer
		Logger.getLogger(MappingServiceJpa.class).info(
				"  Creating output file: " + outputFileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				outputFileName));

		// retrieve the PUBLICATION and READY_FOR_PUBLICATION map records
		Logger.getLogger(MappingServiceJpa.class).info(
				"  Retrieving publication ready records...");
		List<MapRecord> mapRecords = (List<MapRecord>) this
				.getPublishedAndReadyForPublicationMapRecordsForMapProject(
						mapProject.getId(), null).getIterable();

		Logger.getLogger(MappingServiceJpa.class).info(
				"    " + mapRecords.size() + " records eligible.");

		Collections.sort(mapRecords, new Comparator<MapRecord>() {
			@Override
			public int compare(MapRecord mr1, MapRecord mr2) {
				return mr1.getConceptId().compareTo(mr2.getConceptId());
			}
		});

		// Create a map by concept id for quick retrieval of descendants
		Logger.getLogger(MappingServiceJpa.class).info(
				"  Creating terminology id map");
		Map<String, MapRecord> mapRecordMap = new HashMap<>();

		for (MapRecord mr : mapRecords) {
			mapRecordMap.put(mr.getConceptId(), mr);
		}

		// perform the release
		Logger.getLogger(MappingServiceJpa.class).info(
				"  Processing release...");
		for (MapRecord mapRecord : mapRecords) {

			// first check whether this map record has already been processed
			// via up-propagation
			if (!mapRecordMap.containsKey(mapRecord.getConceptId()))
				continue;

			// second, check whether this record should be up-propagated
			boolean isUpPropagated = false;
			if (mapRecord.getCountDescendantConcepts() < 11) {
				mapRecord = upPropagate(mapRecord, mapProject);
				isUpPropagated = true;
			}

			// extract the entries
			List<MapEntry> mapEntries = mapRecord.getMapEntries();

			// ensure entries are sorted by group and priority
			Collections.sort(mapEntries, new Comparator<MapEntry>() {
				@Override
				public int compare(MapEntry me1, MapEntry me2) {
					if (me1.getMapGroup() == me2.getMapGroup()) {
						return (me1.getMapPriority() < me2.getMapPriority() ? -1
								: 1);
					} else
						return (me1.getMapGroup() < me2.getMapGroup() ? -1 : 1);
				}
			});

			// write each entry
			for (MapEntry mapEntry : mapEntries) {
				this.writeReleaseEntry(writer, mapEntry, mapRecord, mapProject,
						effectiveTime, moduleId, isUpPropagated);

			}
		}
	}

	// create UUID from refset id, concept id, map group, map rule, map target
	// TODO Can't find good documentation on creating UUID from field/hash value
	public String getReleaseUuid(MapEntry mapEntry, MapRecord mapRecord,
			MapProject mapProject) {

		long hashCode = 17;

		hashCode = hashCode * 31 + mapProject.getRefSetId().hashCode();
		hashCode = hashCode * 31 + mapRecord.getConceptId().hashCode();
		hashCode = hashCode * 31 + mapEntry.getMapGroup();
		hashCode = hashCode * 31 + mapEntry.getRule().hashCode();
		hashCode = hashCode * 31 + mapEntry.getTargetId().hashCode();

		return "";
	}

	public MapRecord upPropagate(MapRecord mapRecord, MapProject mapProject)
			throws Exception {

		// extract the existing entries
		List<MapEntry> parentEntries = mapRecord.getMapEntries();

		// clear the existing entries
		mapRecord.setMapEntries(null);

		// get the records matching descendant concepts
		ContentService contentService = new ContentServiceJpa();

		// TODO Change this to a routine (maybe add to ContentService) w
		TreePositionList treePositions = contentService
				.getTreePositionsWithDescendants(mapRecord.getConceptId(),
						mapProject.getSourceTerminology(),
						mapProject.getDestinationTerminologyVersion());

		// get the tree position corresponding to this map record
		TreePosition parentTreePosition = treePositions.getIterable()
				.iterator().next();

		// cycle over parent tree position and extract all tree positions into
		// single list

		// CANNOT ASSUME: All tree positions found have no second-level
		// descendants (e.g. grandchildren of the original concept)
		// construct entries based on children
		int mapPriority = 1;

		for (TreePosition treePosition : parentTreePosition.getChildren()) {

			// get the map record corresponding to this tree position
			MapRecord childRecord = this.getMapRecordForProjectAndConcept(
					mapProject.getId(), treePosition.getTerminologyId());

			// extract the child entries
			List<MapEntry> childEntries = childRecord.getMapEntries();

			// cycle over children entries
			for (MapEntry childEntry : childEntries) {

				MapEntry mapEntry = new MapEntryJpa();
				mapEntry.setMapGroup(1); // should this be set to child's
											// mapGroup?
				mapEntry.setMapPriority(mapPriority++); // if set to child's
														// mapGroup, need to
														// track this
														// differently
				mapEntry.setMapRecord(mapRecord);
				mapEntry.setMapRelation(childEntry.getMapRelation());
				mapEntry.setRule("IFA " + childRecord.getConceptId() + " | "
						+ childRecord.getConceptName() + " |");

				MapAdvice mapAdvice = new MapAdviceJpa();

				// if no target, use relation
				// example: IFA 230597003 | Intercostal post-herpetic neuralgia
				// (disorder) | MAP SOURCE CONCEPT CANNOT BE CLASSIFIED WITH
				// AVAILABLE DATA 447561005 447638001
				if (childEntry.getTargetId() == null) {
					mapAdvice.setDetail(childEntry.getMapRelation().getName());

					// otherwise, construct human readable
					// example: IFA 70171009 | Open wound of alveolar process
					// with complication (disorder) | IF OPEN WOUND OF ALVEOLAR
					// PROCESS WITH COMPLICATION CHOOSE S01.5
				} else {
					String adviceText = "IF "
							+ childRecord.getConceptName().replace(" (*)", "") // Remove
																				// any
																				// (disorder),
																				// (disease),
																				// etc.
							+ " CHOOSE " + childEntry.getTargetId();

					mapAdvice.setDetail(adviceText.toUpperCase());
				}

				// add the advice
				mapEntry.addMapAdvice(mapAdvice);

				// and entry to original record
				mapRecord.addMapEntry(mapEntry);

			}
		}

		// add the original entries
		// Example:
		// 1 1 IFA 230597003 | Intercostal post-herpetic neuralgia (disorder) |
		// IF INTERCOSTAL POST-HERPETIC NEURALGIA CHOOSE G58.0 | MAP OF SOURCE
		// CONCEPT IS CONTEXT DEPENDENT | POSSIBLE REQUIREMENT FOR ADDITIONAL
		// CODE TO FULLY DESCRIBE DISEASE OR CONDITION G58.0 447561005 447639009
		// 1 2 OTHERWISE TRUE ALWAYS B02.2 B02.2 447561005 447637006
		// 2 1 IFA 230597003 | Intercostal post-herpetic neuralgia (disorder) |
		// MAP SOURCE CONCEPT CANNOT BE CLASSIFIED WITH AVAILABLE DATA 447561005
		// 447638001
		// 2 2 OTHERWISE TRUE ALWAYS G53.0 G53.0 447561005 447637006

		for (MapEntry mapEntry : parentEntries) {
			// ??? see example -- how to construct that?
			// also relates to whether entries above are being handled properly
		}

		return mapRecord;
	}

	public void writeReleaseEntry(BufferedWriter writer, MapEntry mapEntry,
			MapRecord mapRecord, MapProject mapProject, String effectiveTime,
			String moduleId, boolean isUpPropagated) throws IOException {
		// create UUID from refset id, concept id, map group, map rule,
		// map target
		String uuid = getReleaseUuid(mapEntry, mapRecord, mapProject);

		// construct human-readable map advice based on rule
		String mapAdviceStr = "";

		// if up-propagated
		if (isUpPropagated) {

			// find the propagated advice and add it first
			MapAdvice propagatedAdvice = null;
			for (MapAdvice mapAdvice : mapEntry.getMapAdvices()) {
				if (mapAdvice.getDetail().matches("IFA * | * |")) {
					mapAdviceStr += mapAdvice.getDetail();
					propagatedAdvice = mapAdvice;
				}
			}

			// remove this advice so it will not be added later
			mapEntry.removeMapAdvice(propagatedAdvice);

		} else {

			// if not propagated, construct map advice based on TRUE/AGE rules
			if (mapEntry.getRule().equals("TRUE"))
				mapAdviceStr += "ALWAYS " + mapEntry.getTargetId();
			else if (mapEntry.getRule().contains("Age at onset")) {
				String[] ruleComponents = mapEntry.getRule().split("|");

				// find if this is a less-than-or-equal to or greater-than
				// rule
				// CHANGE THIS to detect all <, <=, >, >=
				boolean lte1 = ruleComponents[2].contains("<=");

				// extract the value and units
				String valueAndUnits1 = (lte1 ? ruleComponents[2].replace(
						" <= ", "") : ruleComponents[2].replace(" > ", ""));

				// add single-age element
				if (ruleComponents.length == 3) {
					mapAdviceStr += "IF AGE AT ONSET OF CLINICAL FINDING "
							+ (lte1 ? "BEFORE " : "ON OR AFTER ")
							+ valueAndUnits1;
				}

				// add double-age element
				else if (ruleComponents.length == 6) {
					String valueAndUnits2 = ruleComponents[5].replace(" <= ",
							"");

					// EXAMPLE:
					// bb173faf-f53c-5cf0-b576-946bfa90d34e 20140901 1
					// 5991000124107 6011000124106 1771000119109 Arthritis of
					// wrist (disorder) 1 8 IFA 445518008 | Age at onset of
					// clinical finding (observable entity) | >= 1.0 year AND
					// IFA 445518008 | Age at onset of clinical finding
					// (observable entity) | < 18.0 years IF AGE AT ONSET OF
					// CLINICAL FINDING BETWEEN 1.0 YEAR AND 18.0 YEARS CHOOSE
					// M08.939 | CONSIDER ADDITIONAL CODE TO IDENTIFY SPECIFIC
					// CONDITION OR DISEASE | CONSIDER LATERALITY SPECIFICATION
					// | MAP OF SOURCE CONCEPT IS CONTEXT DEPENDENT M08.939
					// Juvenile arthritis, unspecified, unspecified wrist
					// 447639009 Map of source concept is context dependent
					mapAdviceStr += "IF AGE AT ONSET OF CLINICAL FINDING ON OR AFTER "
							+ valueAndUnits1 + " AND BEFORE " + valueAndUnits2;
				}

				// add the CHOOSE TARGET
				mapAdviceStr += " CHOOSE " + mapEntry.getTargetId();
			}
		}

		List<MapAdvice> mapAdvices = new ArrayList<>(mapEntry.getMapAdvices());
		Collections.sort(mapAdvices, new Comparator<MapAdvice>() {
			@Override
			public int compare(MapAdvice ma1, MapAdvice ma2) {
				return ma1.getDetail().compareTo(ma2.getDetail());
			}
		});

		// add the entry's map advices
		for (MapAdvice mapAdvice : mapAdvices) {
			mapAdviceStr += " | " + mapAdvice.getDetail();
		}

		String entryLine = uuid
				+ "\t"
				+ "1"
				+ "\t"
				+ moduleId
				+ "\t"
				+ mapProject.getRefSetId()
				+ "\t"
				+ mapRecord.getConceptId()
				+ "\t"
				+ mapEntry.getMapGroup()
				+ "\t"
				+ mapEntry.getMapPriority()
				+ "\t"
				+ mapEntry.getRule()
				+ "\t"
				+ mapAdviceStr
				+ "\t"
				+ (mapEntry.getTargetId() == null ? "" : mapEntry.getTargetId())
				+ "\t"
				+ effectiveTime
				+ "\t"
				+ "447561005"
				+ "\t"
				+ (mapEntry.getMapRelation() == null ? "THIS SHOULD NOT HAVE HAPPENED!!!"
						: mapEntry.getMapRelation().getId());

		// write the line
		writer.write(entryLine);
		writer.newLine();
	}

}
