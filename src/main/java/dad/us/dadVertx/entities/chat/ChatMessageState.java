package dad.us.dadVertx.entities.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageState {

	private Integer state_id;
	private Integer message_id;
	private Long user_id;
	private String state;
	private Long timestamp;

	@JsonCreator
	public ChatMessageState(@JsonProperty("state_id") Integer state_id, @JsonProperty("message_id") Integer message_id,
			@JsonProperty("user_id") Long user_id, @JsonProperty("state") String state,
			@JsonProperty("timestamp") Long timestamp) {
		this.state_id = state_id;
		this.message_id = message_id;
		this.user_id = user_id;
		this.timestamp = timestamp;
		this.state = state;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public void setTimestampFromDate(Date date) {
		this.timestamp = date.getTime();
	}

	public String getFormattedTimestamp() {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String dateString = formatter.format(new Date(timestamp));
		return dateString;
	}

	public Integer getState_id() {
		return state_id;
	}

	public void setState_id(Integer state_id) {
		this.state_id = state_id;
	}

	public Integer getMessage_id() {
		return message_id;
	}

	public void setMessage_id(Integer message_id) {
		this.message_id = message_id;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "ChatMessageState [state_id=" + state_id + ", message_id=" + message_id + ", user_id=" + user_id
				+ ", state=" + state + ", timestamp=" + timestamp + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message_id == null) ? 0 : message_id.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((state_id == null) ? 0 : state_id.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
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
		ChatMessageState other = (ChatMessageState) obj;
		if (message_id == null) {
			if (other.message_id != null)
				return false;
		} else if (!message_id.equals(other.message_id))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (state_id == null) {
			if (other.state_id != null)
				return false;
		} else if (!state_id.equals(other.state_id))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (user_id == null) {
			if (other.user_id != null)
				return false;
		} else if (!user_id.equals(other.user_id))
			return false;
		return true;
	}

	public enum MessageState {
		Read, Received, Sent
	}

}
