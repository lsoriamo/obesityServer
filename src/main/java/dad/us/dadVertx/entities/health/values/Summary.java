package dad.us.dadVertx.entities.health.values;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Summary {

    @SerializedName("idSummary")
    @Expose
    private Integer idSummary;
    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("datasource")
    @Expose
    private Integer datasource;
    @SerializedName("activeScore")
    @Expose
    private Integer activeScore;
    @SerializedName("activityCalories")
    @Expose
    private Integer activityCalories;
    @SerializedName("caloriesBMR")
    @Expose
    private Integer caloriesBMR;
    @SerializedName("caloriesOut")
    @Expose
    private Integer caloriesOut;
    @SerializedName("distances")
    @Expose
    private List<Distance> distances = new ArrayList<Distance>();
    @SerializedName("elevation")
    @Expose
    private Double elevation;
    @SerializedName("fairlyActiveMinutes")
    @Expose
    private Integer fairlyActiveMinutes;
    @SerializedName("floors")
    @Expose
    private Integer floors;
    @SerializedName("lightlyActiveMinutes")
    @Expose
    private Integer lightlyActiveMinutes;
    @SerializedName("marginalCalories")
    @Expose
    private Integer marginalCalories;
    @SerializedName("sedentaryMinutes")
    @Expose
    private Integer sedentaryMinutes;
    @SerializedName("steps")
    @Expose
    private Integer steps;
    @SerializedName("veryActiveMinutes")
    @Expose
    private Integer veryActiveMinutes;
    @SerializedName("lastUpdateTimestamp")
    @Expose
    protected Long lastUpdateTimestamp;

    public enum SummaryDataSource {
        user,
        fitbit,
        googleFit,
        grouped
    }

    public Long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Integer getDatasource() {
        return datasource;
    }

    public void setDatasource(Integer datasource) {
        this.datasource = datasource;
    }

    public SummaryDataSource getDatasourceEnum() {
        return SummaryDataSource.values()[datasource];
    }

    public void setDatasourceEnum(SummaryDataSource datasource) {
        this.datasource = datasource.ordinal();
    }

    public Integer getIdSummary() {
        return idSummary;
    }

    public void setIdSummary(Integer idSummary) {
        this.idSummary = idSummary;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return The activeScore
     */
    public Integer getActiveScore() {
        return activeScore;
    }

    /**
     * @param activeScore The activeScore
     */
    public void setActiveScore(Integer activeScore) {
        this.activeScore = activeScore;
    }

    /**
     * @return The activityCalories
     */
    public Integer getActivityCalories() {
        return activityCalories;
    }

    /**
     * @param activityCalories The activityCalories
     */
    public void setActivityCalories(Integer activityCalories) {
        this.activityCalories = activityCalories;
    }

    /**
     * @return The caloriesBMR
     */
    public Integer getCaloriesBMR() {
        return caloriesBMR;
    }

    /**
     * @param caloriesBMR The caloriesBMR
     */
    public void setCaloriesBMR(Integer caloriesBMR) {
        this.caloriesBMR = caloriesBMR;
    }

    /**
     * @return The caloriesOut
     */
    public Integer getCaloriesOut() {
        return caloriesOut;
    }

    /**
     * @param caloriesOut The caloriesOut
     */
    public void setCaloriesOut(Integer caloriesOut) {
        this.caloriesOut = caloriesOut;
    }

    /**
     * @return The distances
     */
    public List<Distance> getDistances() {
        return distances;
    }

    /**
     * @param distances The distances
     */
    public void setDistances(List<Distance> distances) {
        this.distances = distances;
    }

    /**
     * @return The elevation
     */
    public Double getElevation() {
        return elevation;
    }

    /**
     * @param elevation The elevation
     */
    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    /**
     * @return The fairlyActiveMinutes
     */
    public Integer getFairlyActiveMinutes() {
        return fairlyActiveMinutes;
    }

    /**
     * @param fairlyActiveMinutes The fairlyActiveMinutes
     */
    public void setFairlyActiveMinutes(Integer fairlyActiveMinutes) {
        this.fairlyActiveMinutes = fairlyActiveMinutes;
    }

    public Integer getTotalActiveMinutes() {
        return fairlyActiveMinutes + lightlyActiveMinutes + veryActiveMinutes;
    }

    /**
     * @return The floors
     */
    public Integer getFloors() {
        return floors;
    }

    /**
     * @param floors The floors
     */
    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    /**
     * @return The lightlyActiveMinutes
     */
    public Integer getLightlyActiveMinutes() {
        return lightlyActiveMinutes;
    }

    /**
     * @param lightlyActiveMinutes The lightlyActiveMinutes
     */
    public void setLightlyActiveMinutes(Integer lightlyActiveMinutes) {
        this.lightlyActiveMinutes = lightlyActiveMinutes;
    }

    /**
     * @return The marginalCalories
     */
    public Integer getMarginalCalories() {
        return marginalCalories;
    }

    /**
     * @param marginalCalories The marginalCalories
     */
    public void setMarginalCalories(Integer marginalCalories) {
        this.marginalCalories = marginalCalories;
    }

    /**
     * @return The sedentaryMinutes
     */
    public Integer getSedentaryMinutes() {
        return sedentaryMinutes;
    }

    /**
     * @param sedentaryMinutes The sedentaryMinutes
     */
    public void setSedentaryMinutes(Integer sedentaryMinutes) {
        this.sedentaryMinutes = sedentaryMinutes;
    }

    /**
     * @return The steps
     */
    public Integer getSteps() {
        return steps;
    }

    /**
     * @param steps The steps
     */
    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    /**
     * @return The veryActiveMinutes
     */
    public Integer getVeryActiveMinutes() {
        return veryActiveMinutes;
    }

    /**
     * @param veryActiveMinutes The veryActiveMinutes
     */
    public void setVeryActiveMinutes(Integer veryActiveMinutes) {
        this.veryActiveMinutes = veryActiveMinutes;
    }

    public Double getTotalDistance() {
        Double distance = 0d;
        if (distances != null && !distances.isEmpty()) {
            for (Distance d : distances) {
                distance += d.getDistance();
            }
        }
        if (datasource == SummaryDataSource.fitbit.ordinal())
            distance = distance / 3;
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Summary summary = (Summary) o;

        if (timestamp != null ? !timestamp.equals(summary.timestamp) : summary.timestamp != null)
            return false;
        return datasource != null ? datasource.equals(summary.datasource) : summary.datasource == null;

    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (datasource != null ? datasource.hashCode() : 0);
        return result;
    }

    public String toShareString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es_ES"));
        String message = "Me gustaría compartir contigo el diario de actividades físicas del día " + sdf.format(new Date(timestamp)) + ": \n"
                + "Pasos: " + steps + "\n"
                + "Calorías consumidas: " + activityCalories + "\n"
                + "Plantas subidas: " + floors + "\n"
                + "Distancia recorrida: " + getTotalDistance() + "\n"
                + "Elevación alcanzada: " + elevation + "\n"
                + "Minutos en periodos activos: " + fairlyActiveMinutes + "\n"
                + "Minutos en periodos poco activos: " + lightlyActiveMinutes + "\n"
                + "Minutos sedentarios: " + sedentaryMinutes + "\n"
                + "Minutos en periodos muy activos: " + veryActiveMinutes + "\n"
                + "Caloría basales: " + caloriesBMR + "\n"
                + "Puntuación: " + activeScore + "\n";
        return message;
    }
}
