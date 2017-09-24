package dad.us.dadVertx.entities.doctor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorPatient {

	@JsonProperty("idDoctor")
	private Long idDoctor;
	
	@JsonProperty("idPatient")
	private Long idPatient;

	@JsonProperty("status")
	private Integer status;

	@JsonProperty("validationTimestamp")
	private Long validationTimestamp;

	@JsonCreator
	public DoctorPatient(@JsonProperty("idDoctor") Long idDoctor, @JsonProperty("idPatient") Long idPatient,
			@JsonProperty("status") Integer status, @JsonProperty("validationTimestamp") Long validationTimestamp) {
		this.idDoctor = idDoctor;
		this.idPatient = idPatient;
		this.status = status;
		this.validationTimestamp = validationTimestamp;
	}

	public Long getIdDoctor() {
		return idDoctor;
	}

	public void setIdDoctor(Long idDoctor) {
		this.idDoctor = idDoctor;
	}

	public Long getIdPatient() {
		return idPatient;
	}

	public void setIdPatient(Long idPatient) {
		this.idPatient = idPatient;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getValidationTimestamp() {
		return validationTimestamp;
	}

	public void setValidationTimestamp(Long validationTimestamp) {
		this.validationTimestamp = validationTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idDoctor == null) ? 0 : idDoctor.hashCode());
		result = prime * result + ((idPatient == null) ? 0 : idPatient.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((validationTimestamp == null) ? 0 : validationTimestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DoctorPatient other = (DoctorPatient) obj;
		if (idDoctor == null) {
			if (other.idDoctor != null)
				return false;
		} else if (!idDoctor.equals(other.idDoctor))
			return false;
		if (idPatient == null) {
			if (other.idPatient != null)
				return false;
		} else if (!idPatient.equals(other.idPatient))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (validationTimestamp == null) {
			if (other.validationTimestamp != null)
				return false;
		} else if (!validationTimestamp.equals(other.validationTimestamp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DoctorPatient [idDoctor=" + idDoctor + ", idPatient=" + idPatient + ", status=" + status
				+ ", validationTimestamp=" + validationTimestamp + "]";
	}

	

}
