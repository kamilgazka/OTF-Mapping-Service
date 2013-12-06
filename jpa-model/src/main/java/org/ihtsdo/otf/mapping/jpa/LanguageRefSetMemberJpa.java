package org.ihtsdo.otf.mapping.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.mapping.model.LanguageRefSetMember;

/**
 * Concrete implementation of {@link LanguageRefSetMember}.
 */
@Entity
@Table (name = "language_refset_members")
@Audited
public class LanguageRefSetMemberJpa extends AbstractDescriptionRefSetMember
		implements LanguageRefSetMember {
	
	/** the acceptability id */
	@Column ( nullable = false )
	private Long acceptabilityId;

	/** returns the acceptability id
	 * @return the acceptability id
	 */
	@Override
	public Long getAcceptabilityId() {
		return this.acceptabilityId;
	}

	/** sets the acceptability id
	 * @param acceptabilityId the acceptability id
	 */
	@Override
	public void setAcceptabilityId(Long acceptabilityId) {
		this.acceptabilityId = acceptabilityId;

	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		 return this.getId() + "," +
				 this.getTerminology() + "," +
				 this.getTerminologyId() + "," +
				 this.getTerminologyVersion() + "," +
				 this.getEffectiveTime() + "," +
				 this.isActive() + "," +
				 this.getModuleId() + "," + // end of basic component fields
				 
				 (this.getDescription() == null ? null : getDescription().getTerminologyId()) + "," +
				 this.getAcceptabilityId();
	}
}