package dad.us.dadVertx.entities.medicine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicineTaken {

	@JsonProperty("idmedicine_taken")
	private Integer idmedicine_taken;
	@JsonProperty("medicineId")
	private Integer medicineId;
	@JsonProperty("idUser")
	private Integer idUser;
	@JsonProperty("timestamp")
	private Long timestamp;

	@JsonCreator
	public MedicineTaken(@JsonProperty("idmedicine_taken") Integer idmedicine_taken,
			@JsonProperty("idUser") Integer idUser, @JsonProperty("medicineId") Integer medicineId,
			@JsonProperty("timestamp") Long timestamp) {
		super();
		this.idUser = idUser;
		this.idmedicine_taken = idmedicine_taken;
		this.medicineId = medicineId;
		this.timestamp = timestamp;
	}

	public MedicineTaken() {
		this.idUser = 0;
		this.medicineId = 0;
		this.timestamp = 0L;
	}

	
	
	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public Integer getIdmedicine_taken() {
		return idmedicine_taken;
	}

	public void setIdmedicine_taken(Integer idmedicine_taken) {
		this.idmedicine_taken = idmedicine_taken;
	}

	public Integer getMedicineId() {
		return medicineId;
	}

	public void setMedicineId(Integer medicineId) {
		this.medicineId = medicineId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idmedicine_taken == null) ? 0 : idmedicine_taken.hashCode());
		result = prime * result + ((medicineId == null) ? 0 : medicineId.hashCode());
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
		MedicineTaken other = (MedicineTaken) obj;
		if (idmedicine_taken == null) {
			if (other.idmedicine_taken != null)
				return false;
		} else if (!idmedicine_taken.equals(other.idmedicine_taken))
			return false;
		if (medicineId == null) {
			if (other.medicineId != null)
				return false;
		} else if (!medicineId.equals(other.medicineId))
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
		return "MedicineTaken [idmedicine_taken=" + idmedicine_taken + ", medicineId=" + medicineId + ", timestamp="
				+ timestamp + "]";
	}

}
