package org.ihtsdo.otf.mapping.jpa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.mapping.model.MapEntry;
import org.ihtsdo.otf.mapping.model.MapNote;
import org.ihtsdo.otf.mapping.model.MapPrinciple;
import org.ihtsdo.otf.mapping.model.MapRecord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The Map Record Jpa object
 */
@Entity
@Table(name = "map_records", uniqueConstraints=@UniqueConstraint(columnNames={"mapProjectId", "id"}))
@Audited
@Indexed
@XmlRootElement(name="mapRecord")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapRecordJpa implements MapRecord {

	/** The id. */
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable = true)
	private Long mapProjectId;

	@Column(nullable = false)
	private String conceptId;
	
	@Column(nullable = false)
	private String conceptName;
	
	@Column(nullable = false)
	private Long countDescendantConcepts;

	/** The map records */
	@OneToMany(mappedBy = "mapRecord", cascade = CascadeType.ALL,  fetch = FetchType.EAGER, orphanRemoval = true, targetEntity=MapEntryJpa.class)
	@IndexedEmbedded(targetElement=MapEntryJpa.class)
	private List<MapEntry> mapEntries = new ArrayList<MapEntry>();
	
	/** The map notes */	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, targetEntity=MapNoteJpa.class)
	@IndexedEmbedded(targetElement=MapNoteJpa.class)
	private Set<MapNote> mapNotes = new HashSet<MapNote>();
	
	/** The map principles. */
	@ManyToMany(targetEntity=MapPrincipleJpa.class, fetch=FetchType.EAGER)
	@IndexedEmbedded(targetElement=MapPrincipleJpa.class)
	private Set<MapPrinciple> mapPrinciples = new HashSet<MapPrinciple>();
	

	/** Default constructor */
	public MapRecordJpa() {
	}

	/**
	 * Full constructor
	 * @param id the id
	 * @param conceptId the concept id
	 * @param mapEntries the map entries
	 */
	public MapRecordJpa(Long id, String conceptId, List<MapEntry> mapEntries) { // , List<MapNote> mapNotes) {
		super();
		this.id = id;
		this.conceptId = conceptId;
		this.mapEntries = mapEntries;
		//this.mapNotes = mapNotes;
	}

	/**
	 * Return the id
	 * @return the id
	 */
	@Override
	public Long getId() {
		return this.id;
	}
	
	/**
	 * Set the id
	 * @param id the id
	 */
	@Override
	public void setId(Long id) {
		this.id = id;		
	}
	
	/**
	 * Returns the id in string form
	 * @return the id in string form
	 */
	@XmlID
	@Override
	public String getObjectId() {
		return id.toString();
	}
	
	@Override
	public Long getMapProjectId() {
		return mapProjectId;
	}

	@Override
	public void setMapProjectId(Long mapProjectId) {
		this.mapProjectId = mapProjectId;
	}
	
	@Override
	@Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
	public String getConceptId() {
		return conceptId;
	}

	@Override
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	
	@Override
	@Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
	public String getConceptName() {
		return this.conceptName;
	}

	@Override
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;			
	}

	@Override
	public Long getCountDescendantConcepts() {
		return countDescendantConcepts;
	}

	@Override
	public void setCountDescendantConcepts(Long countDescendantConcepts) {
		this.countDescendantConcepts = countDescendantConcepts;
	}

	@Override
	@XmlElement(type=MapNoteJpa.class, name="mapNote")
	public Set<MapNote> getMapNotes() {
		return mapNotes;
	}

	@Override
	public void setMapNotes(Set<MapNote> mapNotes) {
		this.mapNotes = mapNotes;
	}

	@Override
	public void addMapNote(MapNote mapNote) {
		mapNotes.add(mapNote);
	}

	@Override
	public void removeMapNote(MapNote mapNote) {
		mapNotes.remove(mapNote);
	}

	@Override
	@XmlElement(type=MapEntryJpa.class, name="mapEntry")
	public List<MapEntry> getMapEntries() {
		return mapEntries;
	}

	@Override
	public void setMapEntries(List<MapEntry> mapEntries) {
		this.mapEntries = mapEntries;
	}

	@Override
	public void addMapEntry(MapEntry mapEntry) {
		mapEntries.add(mapEntry);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.otf.mapping.model.MapRecord#removeMapEntry(org.ihtsdo.otf.mapping.model.MapEntry)
	 */
	@Override
	public void removeMapEntry(MapEntry mapEntry) {
		mapEntries.remove(mapEntry);
	}
	
	@Override
	@XmlElement(type=MapPrincipleJpa.class, name="mapPrinciple")
	public Set<MapPrinciple> getMapPrinciples() {
		return mapPrinciples;
	}

	@Override
	public void setMapPrinciples(Set<MapPrinciple> mapPrinciples) {
		this.mapPrinciples = mapPrinciples;
	}

	@Override
	public void addMapPrinciple(MapPrinciple mapPrinciple) {
		mapPrinciples.add(mapPrinciple);
	}

	@Override
	public void removeMapPrinciple(MapPrinciple mapPrinciple) {
		mapPrinciples.remove(mapPrinciple);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
		result =
				prime * result + ((mapProjectId == null) ? 0 : mapProjectId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapRecordJpa other = (MapRecordJpa) obj;
		if (conceptId == null) {
			if (other.conceptId != null)
				return false;
		} else if (!conceptId.equals(other.conceptId))
			return false;
		if (mapProjectId == null) {
			if (other.mapProjectId != null)
				return false;
		} else if (!mapProjectId.equals(other.mapProjectId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MapRecordJpa [id=" + id + ", conceptId=" + conceptId
				+ ", mapEntries=" + mapEntries + "]";
	}

}
