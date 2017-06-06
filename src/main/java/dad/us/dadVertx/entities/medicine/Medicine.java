package dad.us.dadVertx.entities.medicine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Medicine {

	private Integer idmedicine;
	private Integer iduser;
	private String medicine;
	private String observations;
	private Long begin_timestamp;
	private Long end_timestamp;
	private Integer each_days;
	private Boolean mon;
	private Boolean tue;
	private Boolean wed;
	private Boolean thu;
	private Boolean fri;
	private Boolean sat;
	private Boolean sun;

	@JsonCreator
	public Medicine(@JsonProperty("idmedicine") Integer idmedicine, @JsonProperty("iduser") Integer iduser,
			@JsonProperty("medicine") String medicine, @JsonProperty("observations") String observations,
			@JsonProperty("begin_timestamp") Long begin_timestamp, @JsonProperty("end_timestamp") Long end_timestamp,
			@JsonProperty("each_days") Integer each_days, @JsonProperty("mon") Boolean mon,
			@JsonProperty("tue") Boolean tue, @JsonProperty("wed") Boolean wed, @JsonProperty("thu") Boolean thu,
			@JsonProperty("fri") Boolean fri, @JsonProperty("sat") Boolean sat, @JsonProperty("sun") Boolean sun) {
		super();
		this.idmedicine = idmedicine;
		this.iduser = iduser;
		this.medicine = medicine;
		this.observations = observations;
		this.begin_timestamp = begin_timestamp;
		this.end_timestamp = end_timestamp;
		this.each_days = each_days;
		this.mon = mon;
		this.tue = tue;
		this.wed = wed;
		this.thu = thu;
		this.fri = fri;
		this.sat = sat;
		this.sun = sun;
	}

	public Integer getIdmedicine() {
		return idmedicine;
	}

	public void setIdmedicine(Integer idmedicine) {
		this.idmedicine = idmedicine;
	}

	public Integer getIduser() {
		return iduser;
	}

	public void setIduser(Integer iduser) {
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

	public Long getBegin_timestamp() {
		return begin_timestamp;
	}

	public void setBegin_timestamp(Long begin_timestamp) {
		this.begin_timestamp = begin_timestamp;
	}

	public Long getEnd_timestamp() {
		return end_timestamp;
	}

	public void setEnd_timestamp(Long end_timestamp) {
		this.end_timestamp = end_timestamp;
	}

	public Integer getEach_days() {
		return each_days;
	}

	public void setEach_days(Integer each_days) {
		this.each_days = each_days;
	}

	public Boolean getMon() {
		return mon;
	}

	public void setMon(Boolean mon) {
		this.mon = mon;
	}

	public Boolean getTue() {
		return tue;
	}

	public void setTue(Boolean tue) {
		this.tue = tue;
	}

	public Boolean getWed() {
		return wed;
	}

	public void setWed(Boolean wed) {
		this.wed = wed;
	}

	public Boolean getThu() {
		return thu;
	}

	public void setThu(Boolean thu) {
		this.thu = thu;
	}

	public Boolean getFri() {
		return fri;
	}

	public void setFri(Boolean fri) {
		this.fri = fri;
	}

	public Boolean getSat() {
		return sat;
	}

	public void setSat(Boolean sat) {
		this.sat = sat;
	}

	public Boolean getSun() {
		return sun;
	}

	public void setSun(Boolean sun) {
		this.sun = sun;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((begin_timestamp == null) ? 0 : begin_timestamp.hashCode());
		result = prime * result + ((each_days == null) ? 0 : each_days.hashCode());
		result = prime * result + ((end_timestamp == null) ? 0 : end_timestamp.hashCode());
		result = prime * result + ((fri == null) ? 0 : fri.hashCode());
		result = prime * result + ((idmedicine == null) ? 0 : idmedicine.hashCode());
		result = prime * result + ((iduser == null) ? 0 : iduser.hashCode());
		result = prime * result + ((medicine == null) ? 0 : medicine.hashCode());
		result = prime * result + ((mon == null) ? 0 : mon.hashCode());
		result = prime * result + ((observations == null) ? 0 : observations.hashCode());
		result = prime * result + ((sat == null) ? 0 : sat.hashCode());
		result = prime * result + ((sun == null) ? 0 : sun.hashCode());
		result = prime * result + ((thu == null) ? 0 : thu.hashCode());
		result = prime * result + ((tue == null) ? 0 : tue.hashCode());
		result = prime * result + ((wed == null) ? 0 : wed.hashCode());
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
		Medicine other = (Medicine) obj;
		if (begin_timestamp == null) {
			if (other.begin_timestamp != null)
				return false;
		} else if (!begin_timestamp.equals(other.begin_timestamp))
			return false;
		if (each_days == null) {
			if (other.each_days != null)
				return false;
		} else if (!each_days.equals(other.each_days))
			return false;
		if (end_timestamp == null) {
			if (other.end_timestamp != null)
				return false;
		} else if (!end_timestamp.equals(other.end_timestamp))
			return false;
		if (fri == null) {
			if (other.fri != null)
				return false;
		} else if (!fri.equals(other.fri))
			return false;
		if (idmedicine == null) {
			if (other.idmedicine != null)
				return false;
		} else if (!idmedicine.equals(other.idmedicine))
			return false;
		if (iduser == null) {
			if (other.iduser != null)
				return false;
		} else if (!iduser.equals(other.iduser))
			return false;
		if (medicine == null) {
			if (other.medicine != null)
				return false;
		} else if (!medicine.equals(other.medicine))
			return false;
		if (mon == null) {
			if (other.mon != null)
				return false;
		} else if (!mon.equals(other.mon))
			return false;
		if (observations == null) {
			if (other.observations != null)
				return false;
		} else if (!observations.equals(other.observations))
			return false;
		if (sat == null) {
			if (other.sat != null)
				return false;
		} else if (!sat.equals(other.sat))
			return false;
		if (sun == null) {
			if (other.sun != null)
				return false;
		} else if (!sun.equals(other.sun))
			return false;
		if (thu == null) {
			if (other.thu != null)
				return false;
		} else if (!thu.equals(other.thu))
			return false;
		if (tue == null) {
			if (other.tue != null)
				return false;
		} else if (!tue.equals(other.tue))
			return false;
		if (wed == null) {
			if (other.wed != null)
				return false;
		} else if (!wed.equals(other.wed))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Medicine [idmedicine=" + idmedicine + ", iduser=" + iduser + ", medicine=" + medicine
				+ ", observations=" + observations + ", begin_timestamp=" + begin_timestamp + ", end_timestamp="
				+ end_timestamp + ", each_days=" + each_days + ", mon=" + mon + ", tue=" + tue + ", wed=" + wed
				+ ", thu=" + thu + ", fri=" + fri + ", sat=" + sat + ", sun=" + sun + "]";
	}

}
