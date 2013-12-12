package org.ihtsdo.otf.mapping.services;

import java.util.List;

import org.ihtsdo.otf.mapping.model.MapLead;
import org.ihtsdo.otf.mapping.model.MapProject;
import org.ihtsdo.otf.mapping.model.MapSpecialist;

/**
 * Interface for services to retrieve (get) map objects
 * @author Patrick
 */
public interface MappingService {
	

	//////////////////////////////
	// Basic retrieval services //
	//////////////////////////////
	
	/**
	 * Return map project for auto-generated id
	 * @param id the auto-generated id
	 * @return the MapProject
	 */
	public MapProject getMapProject(Long id);
	
	/**
	 * Return map project by project name
	 * @param name the project name
	 * @return the MapProject
	 */
	public MapProject getMapProject(String name);
	
	/**
	 * Retrieve all map projects
	 * @return a List of MapProjects
	 */
	public List<MapProject> getMapProjects();
	
	/**
	 * Retrieve all map specialists
	 * @return a List of MapSpecialists
	 */
	public List<MapSpecialist> getMapSpecialists();
	
	/**
	 * Retrieve all map leads
	 * @return a List of MapLeads
	 */
	public List<MapLead> getMapLeads();
	
	// TODO: Update this once MapAdvice is implemented
	/**
	 * Retrieve all map advice
	 * @return a List of MapAdvices
	 */
	public List<String> getMapAdvice();
	
	/**
	 * Retrieve all map projects assigned to a particular map specialist
	 * @param mapSpecialist the map specialist
	 * @return a List of MapProjects
	 */
	public List<MapProject> getMapProjectsForMapSpecialist(MapSpecialist mapSpecialist);
	
	/**
	 * Retrieve all map projects assigned to a particular map lead
	 * @param mapLead the map lead
	 * @return a List of MapProjects
	 */
	public List<MapProject> getMapProjectsForMapLead(MapLead mapLead);
	
	////////////////////
	// Query services //
	////////////////////
	
	/**
	 * Query for MapProjects
	 * @param query the query
	 * @return the list of MapProject
	 */
	public List<MapProject> findMapProjects(String query);
	
	/** 
	 * Query for MapSpecialists
	 * @param query the query
	 * @return the List of MapProjects
	 */
	public List<MapSpecialist> findMapSpecialists(String query);
	
	/**
	 * Query for MapLeads
	 * @param query the query
	 * @return the List of MapProjects
	 */
	public List<MapLead> findMapLeads(String query);
}