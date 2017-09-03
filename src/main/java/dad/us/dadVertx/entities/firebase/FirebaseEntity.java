package dad.us.dadVertx.entities.firebase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirebaseEntity {
	@JsonProperty("iduser")
	private Integer iduser;

	@JsonProperty("firebase_token")
	private String firebase_token;

	@JsonProperty("timestamp")
	private Long timestamp;

	@JsonCreator
    public FirebaseEntity(@JsonProperty("iduser") Integer iduser,
    		@JsonProperty("firebase_token") String firebase_token,
    		@JsonProperty("timestamp") Long timestamp){
		this.iduser = iduser;
		this.firebase_token = firebase_token;
		this.timestamp = timestamp;
	}
	
	public Integer getIduser() {
		return iduser;
	}

	public void setIduser(Integer iduser) {
		this.iduser = iduser;
	}

	public String getFirebase_token() {
		return firebase_token;
	}

	public void setFirebase_token(String firebase_token) {
		this.firebase_token = firebase_token;
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
		result = prime * result + ((firebase_token == null) ? 0 : firebase_token.hashCode());
		result = prime * result + ((iduser == null) ? 0 : iduser.hashCode());
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
		FirebaseEntity other = (FirebaseEntity) obj;
		if (firebase_token == null) {
			if (other.firebase_token != null)
				return false;
		} else if (!firebase_token.equals(other.firebase_token))
			return false;
		if (iduser == null) {
			if (other.iduser != null)
				return false;
		} else if (!iduser.equals(other.iduser))
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
		return "FirebaseEntity [iduser=" + iduser + ", firebase_token=" + firebase_token + ", timestamp=" + timestamp
				+ "]";
	}

}
