package pk.training.basit.jpa.audit;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@MappedSuperclass
@Embeddable
@AccessType(AccessType.Type.FIELD)
public class Audit {

	@CreatedBy
	@Column(name = "CreatedBy")
	private Long createdBy;

	@CreatedDate
	@Column(name = "CreatedDate")
	private Instant createdDate;

	@LastModifiedBy
	@Column(name = "UpdatedBy")
	private Long lastModifiedBy;

	@LastModifiedDate
	@Column(name = "UpdatedDate")
	private Instant lastModifiedDate;
	
	public Audit() {
		
	}
	
	public Audit(Long createdBy, Instant createdDate, Long lastModifiedBy, Instant lastModifiedDate) {
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public Audit(Audit audit) {
		this.createdBy = audit.createdBy;
		this.createdDate = audit.createdDate;
		this.lastModifiedBy = audit.lastModifiedBy;
		this.lastModifiedDate = audit.lastModifiedDate;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Long lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Instant getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Instant lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

}
