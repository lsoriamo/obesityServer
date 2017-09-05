package dad.us.dadVertx.entities.health.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Distance {

    @SerializedName("activity")
    @Expose
    private String activity;
    @SerializedName("distance")
    @Expose
    private Double distance;

    @JsonCreator
	public Distance(@JsonProperty("activity") String activity, @JsonProperty("distance") Double distance) {
    	if (activity != null)
    		this.activity = activity;
    	else
    		 this.activity = "";
		this.distance = distance;
	}
    
    /**
     * @return The activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @param activity The activity
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * @return The distance
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * @param distance The distance
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

}
