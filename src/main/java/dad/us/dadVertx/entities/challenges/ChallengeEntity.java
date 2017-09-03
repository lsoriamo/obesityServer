package dad.us.dadVertx.entities.challenges;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Calendar;

/**
 * Created by lsori on 29/08/2017.
 */

public class ChallengeEntity {

    @JsonProperty("idChallenge")
    private Integer idChallenge;

    @JsonProperty("iduser")
    private Long iduser;

    @JsonProperty("challengeTitle")
    private String challengeTitle;

    @JsonProperty("challengeDescription")
    private String challengeDescription;

    @JsonProperty("lastUpdateTimestamp")
    private Long lastUpdateTimestamp;

    @JsonProperty("startTimestamp")
    private Long startTimestamp;

    @JsonProperty("finishTimestamp")
    private Long finishTimestamp;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("challengeStatus")
    private Integer challengeStatus;

    @JsonProperty("sensations")
    private Integer sensations;

    @JsonProperty("sensationsComments")
    private String sensationsComments;

    @JsonProperty("startLocationLatitude")
    private Float startLocationLatitude;

    @JsonProperty("startLocationLongitude")
    private Float startLocationLongitude;

    @JsonProperty("startLocationDescription")
    private String startLocationDescription;

    @JsonProperty("endLocationLatitude")
    private Float endLocationLatitude;

    @JsonProperty("endLocationLongitude")
    private Float endLocationLongitude;

    @JsonProperty("endLocationDescription")
    private String endLocationDescription;

    @JsonProperty("distance")
    private Float distance;

    @JsonProperty("steps")
    private Integer steps;

    @JsonProperty("stairs")
    private Integer stairs;

    @JsonProperty("time")
    private Integer time;

    @JsonProperty("type")
    private Integer type;

    public ChallengeEntity(@JsonProperty("idChallenge") Integer idChallenge, @JsonProperty("iduser") Long iduser, @JsonProperty("challengeTitle") String challengeTitle,
                           @JsonProperty("challengeDescription") String challengeDescription, @JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp,
                           @JsonProperty("startTimestamp") Long startTimestamp, @JsonProperty("finishTimestamp") Long finishTimestamp, @JsonProperty("status") Integer status,
                           @JsonProperty("challengeStatus") Integer challengeStatus,
                           @JsonProperty("sensations") Integer sensations, @JsonProperty("sensationsComments") String sensationsComments, @JsonProperty("startLocationLatitude") Float startLocationLatitude,
                           @JsonProperty("startLocationLongitude") Float startLocationLongitude, @JsonProperty("startLocationDescription") String startLocationDescription,
                           @JsonProperty("endLocationLatitude") Float endLocationLatitude, @JsonProperty("endLocationLongitude") Float endLocationLongitude,
                           @JsonProperty("endLocationDescription") String endLocationDescription, @JsonProperty("distance") Float distance, @JsonProperty("steps") Integer steps,
                           @JsonProperty("stairs") Integer stairs, @JsonProperty("time") Integer time, @JsonProperty("type") Integer type) {
        this.idChallenge = idChallenge;
        this.challengeStatus = challengeStatus;
        this.iduser = iduser;
        this.challengeTitle = challengeTitle;
        this.challengeDescription = challengeDescription;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
        this.status = status;
        this.sensations = sensations;
        this.sensationsComments = sensationsComments;
        this.startLocationLatitude = startLocationLatitude;
        this.startLocationLongitude = startLocationLongitude;
        this.startLocationDescription = startLocationDescription;
        this.endLocationLatitude = endLocationLatitude;
        this.endLocationLongitude = endLocationLongitude;
        this.endLocationDescription = endLocationDescription;
        this.distance = distance;
        this.steps = steps;
        this.stairs = stairs;
        this.time = time;
        this.type = type;
    }

    public ChallengeEntity() {
        this.idChallenge = 0;
        this.iduser = 0l;
        this.challengeTitle = "";
        this.challengeDescription = "";
        this.lastUpdateTimestamp = Calendar.getInstance().getTimeInMillis();
        this.startTimestamp = 0l;
        this.finishTimestamp = 0l;
        this.status = 1;
        this.sensations = 0;
        this.sensationsComments = "";
        this.startLocationLatitude = 0f;
        this.startLocationLongitude = 0f;
        this.startLocationDescription = "";
        this.endLocationLatitude = 0f;
        this.endLocationLongitude = 0f;
        this.endLocationDescription = "";
        this.distance = 0f;
        this.steps = 0;
        this.stairs = 0;
        this.time = 0;
        this.challengeStatus = ChallengeStatus.Proposed.ordinal();
        this.type = ChallengeType.StepsPoi.ordinal();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getChallengeStatus() {
        return challengeStatus;
    }

    public void setChallengeStatus(Integer challengeStatus) {
        this.challengeStatus = challengeStatus;
    }

    public Integer getIdChallenge() {
        return idChallenge;
    }

    public void setIdChallenge(Integer idChallenge) {
        this.idChallenge = idChallenge;
    }

    public Long getIduser() {
        return iduser;
    }

    public void setIduser(Long iduser) {
        this.iduser = iduser;
    }

    public String getChallengeTitle() {
        return challengeTitle;
    }

    public void setChallengeTitle(String challengeTitle) {
        this.challengeTitle = challengeTitle;
    }

    public String getChallengeDescription() {
        return challengeDescription;
    }

    public void setChallengeDescription(String challengeDescription) {
        this.challengeDescription = challengeDescription;
    }

    public Long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getFinishTimestamp() {
        return finishTimestamp;
    }

    public void setFinishTimestamp(Long finishTimestamp) {
        this.finishTimestamp = finishTimestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSensations() {
        return sensations;
    }

    public void setSensations(Integer sensations) {
        this.sensations = sensations;
    }

    public String getSensationsComments() {
        return sensationsComments;
    }

    public void setSensationsComments(String sensationsComments) {
        this.sensationsComments = sensationsComments;
    }

    public Float getStartLocationLatitude() {
        return startLocationLatitude;
    }

    public void setStartLocationLatitude(Float startLocationLatitude) {
        this.startLocationLatitude = startLocationLatitude;
    }

    public Float getStartLocationLongitude() {
        return startLocationLongitude;
    }

    public void setStartLocationLongitude(Float startLocationLongitude) {
        this.startLocationLongitude = startLocationLongitude;
    }

    public String getStartLocationDescription() {
        return startLocationDescription;
    }

    public void setStartLocationDescription(String startLocationDescription) {
        this.startLocationDescription = startLocationDescription;
    }

    public Float getEndLocationLatitude() {
        return endLocationLatitude;
    }

    public void setEndLocationLatitude(Float endLocationLatitude) {
        this.endLocationLatitude = endLocationLatitude;
    }

    public Float getEndLocationLongitude() {
        return endLocationLongitude;
    }

    public void setEndLocationLongitude(Float endLocationLongitude) {
        this.endLocationLongitude = endLocationLongitude;
    }

    public String getEndLocationDescription() {
        return endLocationDescription;
    }

    public void setEndLocationDescription(String endLocationDescription) {
        this.endLocationDescription = endLocationDescription;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Integer getStairs() {
        return stairs;
    }

    public void setStairs(Integer stairs) {
        this.stairs = stairs;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChallengeEntity that = (ChallengeEntity) o;

        if (idChallenge != null ? !idChallenge.equals(that.idChallenge) : that.idChallenge != null)
            return false;
        if (iduser != null ? !iduser.equals(that.iduser) : that.iduser != null) return false;
        if (challengeTitle != null ? !challengeTitle.equals(that.challengeTitle) : that.challengeTitle != null)
            return false;
        if (challengeDescription != null ? !challengeDescription.equals(that.challengeDescription) : that.challengeDescription != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp != null)
            return false;
        if (startTimestamp != null ? !startTimestamp.equals(that.startTimestamp) : that.startTimestamp != null)
            return false;
        if (finishTimestamp != null ? !finishTimestamp.equals(that.finishTimestamp) : that.finishTimestamp != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (sensations != null ? !sensations.equals(that.sensations) : that.sensations != null)
            return false;
        if (sensationsComments != null ? !sensationsComments.equals(that.sensationsComments) : that.sensationsComments != null)
            return false;
        if (startLocationLatitude != null ? !startLocationLatitude.equals(that.startLocationLatitude) : that.startLocationLatitude != null)
            return false;
        if (startLocationLongitude != null ? !startLocationLongitude.equals(that.startLocationLongitude) : that.startLocationLongitude != null)
            return false;
        if (startLocationDescription != null ? !startLocationDescription.equals(that.startLocationDescription) : that.startLocationDescription != null)
            return false;
        if (endLocationLatitude != null ? !endLocationLatitude.equals(that.endLocationLatitude) : that.endLocationLatitude != null)
            return false;
        if (endLocationLongitude != null ? !endLocationLongitude.equals(that.endLocationLongitude) : that.endLocationLongitude != null)
            return false;
        if (endLocationDescription != null ? !endLocationDescription.equals(that.endLocationDescription) : that.endLocationDescription != null)
            return false;
        if (distance != null ? !distance.equals(that.distance) : that.distance != null)
            return false;
        if (steps != null ? !steps.equals(that.steps) : that.steps != null) return false;
        if (stairs != null ? !stairs.equals(that.stairs) : that.stairs != null) return false;
        return time != null ? time.equals(that.time) : that.time == null;

    }

    @Override
    public int hashCode() {
        int result = idChallenge != null ? idChallenge.hashCode() : 0;
        result = 31 * result + (iduser != null ? iduser.hashCode() : 0);
        result = 31 * result + (challengeTitle != null ? challengeTitle.hashCode() : 0);
        result = 31 * result + (challengeDescription != null ? challengeDescription.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (startTimestamp != null ? startTimestamp.hashCode() : 0);
        result = 31 * result + (finishTimestamp != null ? finishTimestamp.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (sensations != null ? sensations.hashCode() : 0);
        result = 31 * result + (sensationsComments != null ? sensationsComments.hashCode() : 0);
        result = 31 * result + (startLocationLatitude != null ? startLocationLatitude.hashCode() : 0);
        result = 31 * result + (startLocationLongitude != null ? startLocationLongitude.hashCode() : 0);
        result = 31 * result + (startLocationDescription != null ? startLocationDescription.hashCode() : 0);
        result = 31 * result + (endLocationLatitude != null ? endLocationLatitude.hashCode() : 0);
        result = 31 * result + (endLocationLongitude != null ? endLocationLongitude.hashCode() : 0);
        result = 31 * result + (endLocationDescription != null ? endLocationDescription.hashCode() : 0);
        result = 31 * result + (distance != null ? distance.hashCode() : 0);
        result = 31 * result + (steps != null ? steps.hashCode() : 0);
        result = 31 * result + (stairs != null ? stairs.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChallengeEntity{" +
                "idChallenge=" + idChallenge +
                ", iduser=" + iduser +
                ", challengeTitle='" + challengeTitle + '\'' +
                ", challengeDescription='" + challengeDescription + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", startTimestamp=" + startTimestamp +
                ", finishTimestamp=" + finishTimestamp +
                ", status=" + status +
                ", sensations=" + sensations +
                ", sensationsComments='" + sensationsComments + '\'' +
                ", startLocationLatitude=" + startLocationLatitude +
                ", startLocationLongitude=" + startLocationLongitude +
                ", startLocationDescription='" + startLocationDescription + '\'' +
                ", endLocationLatitude=" + endLocationLatitude +
                ", endLocationLongitude=" + endLocationLongitude +
                ", endLocationDescription='" + endLocationDescription + '\'' +
                ", distance=" + distance +
                ", steps=" + steps +
                ", stairs=" + stairs +
                ", time=" + time +
                '}';
    }

    public enum ChallengeSensations {
        VeryBad,
        Bad,
        Regular,
        Good,
        VeryGood
    }

    public enum ChallengeStatus {
        Started,
        Proposed,
        Finished,
        Discarded
    }

    public enum ChallengeType {
        StepsPoi,
        Distance,
        Bike,
        StairsClimbed,
        WalkingTime,
        RunningTime,
        GymTime
    }
}
