package dad.us.dadVertx.entities.medicine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicineDosage {

	private Integer idmedicine_dosage;
	private Integer idmedicine;
	private Integer time_hour;
	private Integer time_minutes;
	private Integer dosage;
	private String unit;

	@JsonCreator
	public MedicineDosage(@JsonProperty("idmedicine_dosage") Integer idmedicine_dosage,
			@JsonProperty("idmedicine") Integer idmedicine, @JsonProperty("time_hour") Integer time_hour,
			@JsonProperty("time_minutes") Integer time_minutes, @JsonProperty("dosage") Integer dosage,
			@JsonProperty("unit") String unit) {
		super();
		this.idmedicine_dosage = idmedicine_dosage;
		this.idmedicine = idmedicine;
		this.time_hour = time_hour;
		this.time_minutes = time_minutes;
		this.dosage = dosage;
		this.unit = unit;
	}

	public Integer getIdmedicine_dosage() {
		return idmedicine_dosage;
	}

	public void setIdmedicine_dosage(Integer idmedicine_dosage) {
		this.idmedicine_dosage = idmedicine_dosage;
	}

	public Integer getIdmedicine() {
		return idmedicine;
	}

	public void setIdmedicine(Integer idmedicine) {
		this.idmedicine = idmedicine;
	}

	public Integer getTime_hour() {
		return time_hour;
	}

	public void setTime_hour(Integer time_hour) {
		this.time_hour = time_hour;
	}

	public Integer getTime_minutes() {
		return time_minutes;
	}

	public void setTime_minutes(Integer time_minutes) {
		this.time_minutes = time_minutes;
	}

	public Integer getDosage() {
		return dosage;
	}

	public void setDosage(Integer dosage) {
		this.dosage = dosage;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dosage == null) ? 0 : dosage.hashCode());
		result = prime * result + ((idmedicine == null) ? 0 : idmedicine.hashCode());
		result = prime * result + ((idmedicine_dosage == null) ? 0 : idmedicine_dosage.hashCode());
		result = prime * result + ((time_hour == null) ? 0 : time_hour.hashCode());
		result = prime * result + ((time_minutes == null) ? 0 : time_minutes.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		MedicineDosage other = (MedicineDosage) obj;
		if (dosage == null) {
			if (other.dosage != null)
				return false;
		} else if (!dosage.equals(other.dosage))
			return false;
		if (idmedicine == null) {
			if (other.idmedicine != null)
				return false;
		} else if (!idmedicine.equals(other.idmedicine))
			return false;
		if (idmedicine_dosage == null) {
			if (other.idmedicine_dosage != null)
				return false;
		} else if (!idmedicine_dosage.equals(other.idmedicine_dosage))
			return false;
		if (time_hour == null) {
			if (other.time_hour != null)
				return false;
		} else if (!time_hour.equals(other.time_hour))
			return false;
		if (time_minutes == null) {
			if (other.time_minutes != null)
				return false;
		} else if (!time_minutes.equals(other.time_minutes))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MedicineDosage [idmedicine_dosage=" + idmedicine_dosage + ", idmedicine=" + idmedicine + ", time_hour="
				+ time_hour + ", time_minutes=" + time_minutes + ", dosage=" + dosage + ", unit=" + unit + "]";
	}

}
