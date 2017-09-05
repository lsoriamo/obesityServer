package dad.us.dadVertx.entities.medicaltest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalTestEntity {
	@JsonProperty("idMedicalTest")
	private Integer idMedicalTest;

	@JsonProperty("iduser")
	private Long iduser;

	@JsonProperty("prescriber")
	private Integer prescriber;

	@JsonProperty("prescriberComment")
	private String prescriberComment;

	@JsonProperty("lastUpdateTimestamp")
	private Long lastUpdateTimestamp;

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("timestamp")
	private Long timestamp;

	@JsonProperty("timestampDone")
	private Long timestampDone;

	@JsonProperty("timestampResults")
	private Long timestampResults;

	@JsonProperty("picturePath")
	private String picturePath;

	@JsonProperty("timestampCite")
	private Long timestampCite;

	@JsonProperty("placeCite")
	private String placeCite;

	@JsonProperty("doctorCite")
	private String doctorCite;

	@JsonProperty("status")
	private Integer status;

	@JsonProperty("createdBy")
	private Integer createdBy;

	@JsonCreator
	public MedicalTestEntity(@JsonProperty("idMedicalTest") Integer idMedicalTest,
			@JsonProperty("iduser") Long iduser, @JsonProperty("prescriber") Integer prescriber,
			@JsonProperty("prescriberComment") String prescriberComment,
			@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp, @JsonProperty("name") String name,
			@JsonProperty("description") String description, @JsonProperty("timestamp") Long timestamp,
			@JsonProperty("timestampDone") Long timestampDone, @JsonProperty("timestampResults") Long timestampResults,
			@JsonProperty("picturePath") String picturePath, @JsonProperty("timestampCite") Long timestampCite,
			@JsonProperty("placeCite") String placeCite, @JsonProperty("doctorCite") String doctorCite,
			@JsonProperty("status") Integer status, @JsonProperty("createdBy") Integer createdBy) {
		super();
		this.idMedicalTest = idMedicalTest;
		this.iduser = iduser;
		this.prescriber = prescriber;
		this.prescriberComment = prescriberComment;
		this.lastUpdateTimestamp = lastUpdateTimestamp;
		this.name = name;
		this.description = description;
		this.timestamp = timestamp;
		this.timestampDone = timestampDone;
		this.timestampResults = timestampResults;
		this.picturePath = picturePath;
		this.timestampCite = timestampCite;
		this.placeCite = placeCite;
		this.doctorCite = doctorCite;
		this.status = status;
		this.createdBy = createdBy;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getIdMedicalTest() {
		return idMedicalTest;
	}

	public void setIdMedicalTest(Integer idMedicalTest) {
		this.idMedicalTest = idMedicalTest;
	}

	public Long getIduser() {
		return iduser;
	}

	public void setIduser(Long iduser) {
		this.iduser = iduser;
	}

	public Integer getPrescriber() {
		return prescriber;
	}

	public void setPrescriber(Integer prescriber) {
		this.prescriber = prescriber;
	}

	public String getPrescriberComment() {
		return prescriberComment;
	}

	public void setPrescriberComment(String prescriberComment) {
		this.prescriberComment = prescriberComment;
	}

	public Long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTimestampDone() {
		return timestampDone;
	}

	public void setTimestampDone(Long timestampDone) {
		this.timestampDone = timestampDone;
	}

	public Long getTimestampResults() {
		return timestampResults;
	}

	public void setTimestampResults(Long timestampResults) {
		this.timestampResults = timestampResults;
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}

	public Long getTimestampCite() {
		return timestampCite;
	}

	public void setTimestampCite(Long timestampCite) {
		this.timestampCite = timestampCite;
	}

	public String getPlaceCite() {
		return placeCite;
	}

	public void setPlaceCite(String placeCite) {
		this.placeCite = placeCite;
	}

	public String getDoctorCite() {
		return doctorCite;
	}

	public void setDoctorCite(String doctorCite) {
		this.doctorCite = doctorCite;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((doctorCite == null) ? 0 : doctorCite.hashCode());
		result = prime * result + ((idMedicalTest == null) ? 0 : idMedicalTest.hashCode());
		result = prime * result + ((iduser == null) ? 0 : iduser.hashCode());
		result = prime * result + ((lastUpdateTimestamp == null) ? 0 : lastUpdateTimestamp.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((picturePath == null) ? 0 : picturePath.hashCode());
		result = prime * result + ((placeCite == null) ? 0 : placeCite.hashCode());
		result = prime * result + ((prescriber == null) ? 0 : prescriber.hashCode());
		result = prime * result + ((prescriberComment == null) ? 0 : prescriberComment.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((timestampCite == null) ? 0 : timestampCite.hashCode());
		result = prime * result + ((timestampDone == null) ? 0 : timestampDone.hashCode());
		result = prime * result + ((timestampResults == null) ? 0 : timestampResults.hashCode());
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
		MedicalTestEntity other = (MedicalTestEntity) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (doctorCite == null) {
			if (other.doctorCite != null)
				return false;
		} else if (!doctorCite.equals(other.doctorCite))
			return false;
		if (idMedicalTest == null) {
			if (other.idMedicalTest != null)
				return false;
		} else if (!idMedicalTest.equals(other.idMedicalTest))
			return false;
		if (iduser == null) {
			if (other.iduser != null)
				return false;
		} else if (!iduser.equals(other.iduser))
			return false;
		if (lastUpdateTimestamp == null) {
			if (other.lastUpdateTimestamp != null)
				return false;
		} else if (!lastUpdateTimestamp.equals(other.lastUpdateTimestamp))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (picturePath == null) {
			if (other.picturePath != null)
				return false;
		} else if (!picturePath.equals(other.picturePath))
			return false;
		if (placeCite == null) {
			if (other.placeCite != null)
				return false;
		} else if (!placeCite.equals(other.placeCite))
			return false;
		if (prescriber == null) {
			if (other.prescriber != null)
				return false;
		} else if (!prescriber.equals(other.prescriber))
			return false;
		if (prescriberComment == null) {
			if (other.prescriberComment != null)
				return false;
		} else if (!prescriberComment.equals(other.prescriberComment))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (timestampCite == null) {
			if (other.timestampCite != null)
				return false;
		} else if (!timestampCite.equals(other.timestampCite))
			return false;
		if (timestampDone == null) {
			if (other.timestampDone != null)
				return false;
		} else if (!timestampDone.equals(other.timestampDone))
			return false;
		if (timestampResults == null) {
			if (other.timestampResults != null)
				return false;
		} else if (!timestampResults.equals(other.timestampResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MedicalTestEntity [idMedicalTest=" + idMedicalTest + ", iduser=" + iduser + ", prescriber=" + prescriber
				+ ", prescriberComment=" + prescriberComment + ", lastUpdateTimestamp=" + lastUpdateTimestamp
				+ ", name=" + name + ", description=" + description + ", timestamp=" + timestamp + ", timestampDone="
				+ timestampDone + ", timestampResults=" + timestampResults + ", picturePath=" + picturePath
				+ ", timestampCite=" + timestampCite + ", placeCite=" + placeCite + ", doctorCite=" + doctorCite + "]";
	}

}
