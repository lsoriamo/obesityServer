package dad.us.dadVertx.entities.chat;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    private String message;
    private Integer id_message;
    private Integer group_id;
    private Long user_id;
    private Long timestamp;


    @JsonCreator
    public ChatMessage(@JsonProperty("message") String message, @JsonProperty("id_message") Integer id_message,
                       @JsonProperty("group_id") Integer group_id, @JsonProperty("user_id") Long user_id,
                       @JsonProperty("timestamp") Long timestamp) {
        this.message = message;
        this.id_message = id_message;
        this.group_id = group_id;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public ChatMessage(ChatMessage chatMessage) {
        this(chatMessage.getMessage(), chatMessage.getId_message(), chatMessage.getGroup_id(), chatMessage.getUser_id(), chatMessage.getTimestamp());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getId_message() {
        return id_message;
    }

    public void setId_message(Integer id_message) {
        this.id_message = id_message;
    }

    public Integer getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Integer group_id) {
        this.group_id = group_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
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

    @Override
    public String toString() {
        return "ChatMessage [message=" + message + ", id_message=" + id_message + ", group_id=" + group_id
                + ", user_id=" + user_id + ", timestamp=" + timestamp + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group_id == null) ? 0 : group_id.hashCode());
        result = prime * result + ((id_message == null) ? 0 : id_message.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
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
        ChatMessage other = (ChatMessage) obj;
        if (group_id == null) {
            if (other.group_id != null)
                return false;
        } else if (!group_id.equals(other.group_id))
            return false;
        if (id_message == null) {
            if (other.id_message != null)
                return false;
        } else if (!id_message.equals(other.id_message))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
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
}
