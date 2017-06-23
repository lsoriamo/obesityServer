package dad.us.dadVertx.entities.appointment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lsori on 01/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Appointment {

	Integer idAppointment;
	Long timestamp;
	String doctor;
	String description;
	String place;
	String things;
	Integer iduser;
	Integer type;

	@JsonCreator
	public Appointment(@JsonProperty("idappointment") Integer idAppointment, @JsonProperty("timestamp") Long timestamp,
			@JsonProperty("doctor") String doctor, @JsonProperty("description") String description,
			@JsonProperty("place") String place, @JsonProperty("things") String things,
			@JsonProperty("iduser") Integer iduser, @JsonProperty("type") Integer type) {
		super();
		this.idAppointment = idAppointment;
		this.timestamp = timestamp;
		this.doctor = doctor;
		this.description = description;
		this.place = place;
		this.things = things;
		this.iduser = iduser;
		this.type = type;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getIdAppointment() {
		return idAppointment;
	}

	public void setIdAppointment(Integer idAppointment) {
		this.idAppointment = idAppointment;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getThings() {
		return things;
	}

	public void setThings(String things) {
		this.things = things;
	}

	public Integer getIduser() {
		return iduser;
	}

	public void setIduser(Integer iduser) {
		this.iduser = iduser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((doctor == null) ? 0 : doctor.hashCode());
		result = prime * result + ((idAppointment == null) ? 0 : idAppointment.hashCode());
		result = prime * result + ((iduser == null) ? 0 : iduser.hashCode());
		result = prime * result + ((place == null) ? 0 : place.hashCode());
		result = prime * result + ((things == null) ? 0 : things.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		Appointment other = (Appointment) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (doctor == null) {
			if (other.doctor != null)
				return false;
		} else if (!doctor.equals(other.doctor))
			return false;
		if (idAppointment == null) {
			if (other.idAppointment != null)
				return false;
		} else if (!idAppointment.equals(other.idAppointment))
			return false;
		if (iduser == null) {
			if (other.iduser != null)
				return false;
		} else if (!iduser.equals(other.iduser))
			return false;
		if (place == null) {
			if (other.place != null)
				return false;
		} else if (!place.equals(other.place))
			return false;
		if (things == null) {
			if (other.things != null)
				return false;
		} else if (!things.equals(other.things))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Appointment [idAppointment=" + idAppointment + ", timestamp=" + timestamp + ", doctor=" + doctor
				+ ", description=" + description + ", place=" + place + ", things=" + things + ", iduser=" + iduser
				+ "]";
	}

}
