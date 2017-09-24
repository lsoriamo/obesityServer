package dad.us.dadVertx.entities.medicine;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Medicine {

	@JsonProperty("idmedicine")
	private Integer idmedicine;
	@JsonProperty("iduser")
	private Long iduser;
	@JsonProperty("medicine")
	private String medicine;
	@JsonProperty("observations")
	private String observations;
	@JsonProperty("begin_timestamp")
	private Long begin_timestamp;
	@JsonProperty("end_timestamp")
	private String end_timestamp;
	@JsonProperty("method")
	private String method;
	@JsonProperty("days")
	private String days;
	@JsonProperty("dosage")
	private String dosage;
	@JsonProperty("status")
	private Integer status;
	@JsonProperty("lastUpdateTimestamp")
	protected Long lastUpdateTimestamp;
	@JsonProperty("createdBy")
	private Integer createdBy;
	@JsonProperty("userViewTimestamp")
	private Long userViewTimestamp;

	@JsonCreator
	public Medicine(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp,
			@JsonProperty("idmedicine") Integer idmedicine, @JsonProperty("iduser") Long iduser,
			@JsonProperty("medicine") String medicine, @JsonProperty("observations") String observations,
			@JsonProperty("begin_timestamp") Long begin_timestamp, @JsonProperty("end_timestamp") String end_timestamp,
			@JsonProperty("method") String method, @JsonProperty("days") String days,
			@JsonProperty("dosage") String dosage, @JsonProperty("status") Integer status,
			@JsonProperty("createdBy") Integer createdBy, @JsonProperty("userViewTimestamp") Long userViewTimestamp) {
		super();
		this.idmedicine = idmedicine;
		this.iduser = iduser;
		this.medicine = medicine;
		this.observations = observations;
		this.begin_timestamp = begin_timestamp;
		this.end_timestamp = end_timestamp;
		this.method = method;
		this.dosage = dosage;
		this.days = days;
		this.status = status;
		this.lastUpdateTimestamp = lastUpdateTimestamp;
		this.createdBy = createdBy;
		this.userViewTimestamp = userViewTimestamp;
	}

	public Medicine() {
		this.iduser = 0L;
		this.medicine = "";
		this.observations = "";
		this.begin_timestamp = 0L;
		this.end_timestamp = "";
		this.method = "";
		this.dosage = "";
		this.days = "";
		this.status = 1;
		this.createdBy = 0;
		this.userViewTimestamp = 0L;
		this.lastUpdateTimestamp = Calendar.getInstance().getTimeInMillis();
	}

	public Integer getIdmedicine() {
		return idmedicine;
	}

	public void setIdmedicine(Integer idmedicine) {
		this.idmedicine = idmedicine;
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

	public Long getIduser() {
		return iduser;
	}

	public void setIduser(Long iduser) {
		this.iduser = iduser;
	}

	public String getMedicine() {
		return medicine;
	}

	public void setMedicine(String medicine) {
		this.medicine = medicine;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Long getBeginTimestamp() {
		return begin_timestamp;
	}

	public void setBeginTimestamp(Long begin_timestamp) {
		this.begin_timestamp = begin_timestamp;
	}

	public String getEndTimestamp() {
		return end_timestamp;
	}

	public void setEndTimestamp(String end_timestamp) {
		this.end_timestamp = end_timestamp;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public String getDosage() {
		return dosage;
	}

	public Long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public void setDosage(String dosage) {
		this.dosage = dosage;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Medicine{" + "idmedicine=" + idmedicine + ", iduser=" + iduser + ", medicine='" + medicine + '\''
				+ ", observations='" + observations + '\'' + ", begin_timestamp=" + begin_timestamp
				+ ", end_timestamp='" + end_timestamp + '\'' + ", method='" + method + '\'' + ", days='" + days + '\''
				+ ", dosage='" + dosage + '\'' + ", status=" + status + ", lastUpdateTimestamp=" + lastUpdateTimestamp
				+ '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Medicine medicine1 = (Medicine) o;

		if (idmedicine != null ? !idmedicine.equals(medicine1.idmedicine) : medicine1.idmedicine != null)
			return false;
		if (iduser != null ? !iduser.equals(medicine1.iduser) : medicine1.iduser != null)
			return false;
		if (medicine != null ? !medicine.equals(medicine1.medicine) : medicine1.medicine != null)
			return false;
		if (observations != null ? !observations.equals(medicine1.observations) : medicine1.observations != null)
			return false;
		if (begin_timestamp != null ? !begin_timestamp.equals(medicine1.begin_timestamp)
				: medicine1.begin_timestamp != null)
			return false;
		if (end_timestamp != null ? !end_timestamp.equals(medicine1.end_timestamp) : medicine1.end_timestamp != null)
			return false;
		if (method != null ? !method.equals(medicine1.method) : medicine1.method != null)
			return false;
		if (days != null ? !days.equals(medicine1.days) : medicine1.days != null)
			return false;
		if (dosage != null ? !dosage.equals(medicine1.dosage) : medicine1.dosage != null)
			return false;
		if (status != null ? !status.equals(medicine1.status) : medicine1.status != null)
			return false;
		return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(medicine1.lastUpdateTimestamp)
				: medicine1.lastUpdateTimestamp == null;

	}

	@Override
	public int hashCode() {
		int result = idmedicine != null ? idmedicine.hashCode() : 0;
		result = 31 * result + (iduser != null ? iduser.hashCode() : 0);
		result = 31 * result + (medicine != null ? medicine.hashCode() : 0);
		result = 31 * result + (observations != null ? observations.hashCode() : 0);
		result = 31 * result + (begin_timestamp != null ? begin_timestamp.hashCode() : 0);
		result = 31 * result + (end_timestamp != null ? end_timestamp.hashCode() : 0);
		result = 31 * result + (method != null ? method.hashCode() : 0);
		result = 31 * result + (days != null ? days.hashCode() : 0);
		result = 31 * result + (dosage != null ? dosage.hashCode() : 0);
		result = 31 * result + (status != null ? status.hashCode() : 0);
		result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
		return result;
	}
}
