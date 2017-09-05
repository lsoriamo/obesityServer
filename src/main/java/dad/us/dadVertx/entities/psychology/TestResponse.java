package dad.us.dadVertx.entities.psychology;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestResponse {

	private Long user_id;
	private Integer test_id;
	private Integer question_number;
	private String response;
	private Long timestamp;

	@JsonCreator
	public TestResponse(@JsonProperty("user_id") Long user_id, @JsonProperty("test_id") Integer test_id,
			@JsonProperty("question_number") Integer question_number, @JsonProperty("response") String response,
			@JsonProperty("timestamp") Long timestamp) {
		this.user_id = user_id;
		this.test_id = test_id;
		this.question_number = question_number;
		this.timestamp = timestamp;
		this.response = response;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public Integer getTest_id() {
		return test_id;
	}

	public void setTest_id(Integer test_id) {
		this.test_id = test_id;
	}

	public Integer getQuestion_number() {
		return question_number;
	}

	public void setQuestion_number(Integer question_number) {
		this.question_number = question_number;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
