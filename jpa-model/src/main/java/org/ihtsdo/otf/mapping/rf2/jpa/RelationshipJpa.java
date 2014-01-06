package org.ihtsdo.otf.mapping.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ContainedIn;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.rf2.Relationship;

/**
 * Concrete implementation of {@link Relationship} for use with JPA.
 */
@Entity
@Table(name = "relationships", uniqueConstraints = @UniqueConstraint(columnNames = {
		"terminologyId", "terminology", "terminologyVersion"
}))
@XmlRootElement(name="relationship")
@Audited
public class RelationshipJpa extends AbstractComponent implements Relationship {

	/** The source concept. */
	@ManyToOne(targetEntity = ConceptJpa.class, optional=false)
	@XmlTransient
	@ContainedIn
	private Concept sourceConcept;

	/** The destination concept. */
	@ManyToOne(targetEntity = ConceptJpa.class, optional=false)
	@XmlTransient
	@ContainedIn
	private Concept destinationConcept;

	/** The type id. */
	@Column(nullable = false)
	private Long typeId;

	/** The characteristic type id. */
	@Column(nullable = false)
	private Long characteristicTypeId;

	/** The modifier id. */
	@Column(nullable = false)
	private Long modifierId;

	/** The relationship group. */
	@Column(nullable = true)
	private Integer relationshipGroup;
	
	/** For serialization */
	@XmlElement
	private String getSourceId() {
		return sourceConcept.getTerminologyId();
	}
	
	/** For serialization */
	@XmlElement	
	private String getDestinationId() {
		return destinationConcept.getTerminologyId();
	}
	
	/**
     * Returns the destination concept preferred name. Used for XML/JSON serialization.
	 * @return the destination concept preferred name
	 */
	@XmlElement
	public String getDestinationConceptPreferredName() {
	    return destinationConcept != null ? destinationConcept.getDefaultPreferredName() : null;
	}
	

	/**
	 * Returns the type id.
	 * 
	 * @return the type id
	 */
	@Override
	public Long getTypeId() {
		return typeId;
	}

	/**
	 * Sets the type id.
	 * 
	 * @param typeId the type id
	 */
	@Override
	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}

	/**
	 * Returns the characteristic type id.
	 * 
	 * @return the characteristic type id
	 */
	@Override
	public Long getCharacteristicTypeId() {
		return characteristicTypeId;
	}

	/**
	 * Sets the characteristic type id.
	 * 
	 * @param characteristicTypeId the characteristic type id
	 */
	@Override
	public void setCharacteristicTypeId(Long characteristicTypeId) {
		this.characteristicTypeId = characteristicTypeId;
	}

	/**
	 * Returns the modifier id.
	 * 
	 * @return the modifier id
	 */
	@Override
	public Long getModifierId() {
		return modifierId;
	}

	/**
	 * Sets the modifier id.
	 * 
	 * @param modifierId the modifier id
	 */
	@Override
	public void setModifierId(Long modifierId) {
		this.modifierId = modifierId;
	}

	/**
	 * Returns the source concept.
	 * 
	 * @return the source concept
	 */
	@Override
	@XmlTransient
	public Concept getSourceConcept() {
		return sourceConcept;
	}

	/**
	 * Sets the source concept.
	 * 
	 * @param sourceConcept the source concept
	 */
	@Override
	public void setSourceConcept(Concept sourceConcept) {
		this.sourceConcept = sourceConcept;
	}

	/**
	 * Returns the destination concept.
	 * 
	 * @return the destination concept
	 */
	@Override
	@XmlTransient
	public Concept getDestinationConcept() {
		return this.destinationConcept;
	}

	/**
	 * Sets the destination concept.
	 * 
	 * @param destinationConcept the destination concept
	 */
	@Override
	public void setDestinationConcept(Concept destinationConcept) {
		this.destinationConcept = destinationConcept;
	}

	/**
	 * Returns the relationship group.
	 * 
	 * @return the relationship group
	 */
	@Override
	public Integer getRelationshipGroup() {
		return relationshipGroup;
	}

	/**
	 * Sets the relationship group.
	 * 
	 * @param relationshipGroup the relationship group
	 */
	@Override
	public void setRelationshipGroup(Integer relationshipGroup) {
		this.relationshipGroup = relationshipGroup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.getId()
				+ ","
				+ this.getTerminology()
				+ ","
				+ this.getTerminologyId()
				+ ","
				+ this.getTerminologyVersion()
				+ ","
				+ this.getEffectiveTime()
				+ ","
				+ this.isActive()
				+ ","
				+ this.getModuleId()
				+ ","
				+ // end of basic component fields

				(this.getSourceConcept() == null ? null : this.getSourceConcept()
						.getId())
				+ ","
				+ (this.getDestinationConcept() == null ? null : this
						.getDestinationConcept().getId()) + ","
				+ this.getRelationshipGroup() + "," + this.getTypeId() + ","
				+ this.getCharacteristicTypeId() + "," + this.getModifierId(); // end of
																																				// relationship
																																				// fields

	}

}
