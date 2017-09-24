package dad.us.dadVertx.entities.appointment;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lsori on 01/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Appointment {

	@JsonProperty("idAppointment")
	protected Integer idAppointment;
	@JsonProperty("timestamp")
	protected Long timestamp;
	@JsonProperty("doctor")
	protected String doctor;
	@JsonProperty("description")
	protected String description;
	@JsonProperty("place")
	protected String place;
	@JsonProperty("things")
	protected String things;
	@JsonProperty("iduser")
	protected Long iduser;
	@JsonProperty("type")
	protected Integer type;
	@JsonProperty("status")
	protected Integer status;
	@JsonProperty("lastUpdateTimestamp")
	protected Long lastUpdateTimestamp;
	@JsonProperty("createdBy")
	private Integer createdBy;
	@JsonProperty("userViewTimestamp")
	private Long userViewTimestamp;

	@JsonCreator
	public Appointment(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp,
			@JsonProperty("idappointment") Integer idAppointment, @JsonProperty("timestamp") Long timestamp,
			@JsonProperty("doctor") String doctor, @JsonProperty("description") String description,
			@JsonProperty("place") String place, @JsonProperty("things") String things,
			@JsonProperty("iduser") Long iduser, @JsonProperty("type") Integer type,
			@JsonProperty("status") Integer status, @JsonProperty("createdBy") Integer createdBy,
			@JsonProperty("userViewTimestamp") Long userViewTimestamp) {
		super();
		this.lastUpdateTimestamp = lastUpdateTimestamp;
		this.idAppointment = idAppointment;
		this.timestamp = timestamp;
		this.doctor = doctor;
		this.description = description;
		this.place = place;
		this.things = things;
		this.iduser = iduser;
		this.type = type;
		this.status = status;
		this.createdBy = createdBy;
		this.userViewTimestamp = userViewTimestamp;
	}

	public Appointment() {
		super();
		this.lastUpdateTimestamp = Calendar.getInstance().getTimeInMillis();
		this.timestamp = 0l;
		this.doctor = "";
		this.description = "";
		this.place = "";
		this.things = "";
		this.iduser = 0l;
		this.type = 0;
		this.status = 1;
		this.createdBy = 0;
		this.userViewTimestamp = 0L;
	}

	public Integer getIdAppointment() {
		return idAppointment;
	}

	public void setIdAppointment(Integer idAppointment) {
		this.idAppointment = idAppointment;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Long getUserViewTimestamp() {
		return userViewTimestamp;
	}

	public void setUserViewTimestamp(Long userViewTimestamp) {
		this.userViewTimestamp = userViewTimestamp;
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

	public Long getIduser() {
		return iduser;
	}

	public void setIduser(Long iduser) {
		this.iduser = iduser;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	@Override
	public String toString() {
		return "Appointment{" + "idAppointment=" + idAppointment + ", timestamp=" + timestamp + ", doctor='" + doctor
				+ '\'' + ", description='" + description + '\'' + ", place='" + place + '\'' + ", things='" + things
				+ '\'' + ", iduser=" + iduser + ", type=" + type + ", status=" + status + ", lastUpdateTimestamp="
				+ lastUpdateTimestamp + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Appointment that = (Appointment) o;

		if (idAppointment != null ? !idAppointment.equals(that.idAppointment) : that.idAppointment != null)
			return false;
		if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
			return false;
		if (doctor != null ? !doctor.equals(that.doctor) : that.doctor != null)
			return false;
		if (description != null ? !description.equals(that.description) : that.description != null)
			return false;
		if (place != null ? !place.equals(that.place) : that.place != null)
			return false;
		if (things != null ? !things.equals(that.things) : that.things != null)
			return false;
		if (iduser != null ? !iduser.equals(that.iduser) : that.iduser != null)
			return false;
		if (type != null ? !type.equals(that.type) : that.type != null)
			return false;
		if (status != null ? !status.equals(that.status) : that.status != null)
			return false;
		return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(that.lastUpdateTimestamp)
				: that.lastUpdateTimestamp == null;

	}

	@Override
	public int hashCode() {
		int result = idAppointment != null ? idAppointment.hashCode() : 0;
		result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
		result = 31 * result + (doctor != null ? doctor.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (place != null ? place.hashCode() : 0);
		result = 31 * result + (things != null ? things.hashCode() : 0);
		result = 31 * result + (iduser != null ? iduser.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (status != null ? status.hashCode() : 0);
		result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
		return result;
	}

}
