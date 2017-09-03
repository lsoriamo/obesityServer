package dad.us.dadVertx.entities.health.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lsori on 12/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartRateZone extends HealthValue {
    @JsonProperty("idHeartRateZone")
    private Integer idHeartRateZone;
    @JsonProperty("idHeartRate")
    private Integer idHeartRate;
    @SerializedName("max")
    private Integer max;
    @SerializedName("min")
    private Integer min;
    @SerializedName("caloriesOut")
    private Float caloriesOut;
    @SerializedName("minutes")
    private Integer minutes;
    @SerializedName("name")
    private String name;

    public HeartRateZone() {
        super();
        name = "";
        minutes = 0;
        caloriesOut = 0f;
        max = 0;
        min = 0;
        idHeartRate = 0;
    }

    @JsonCreator
    public HeartRateZone(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp, @JsonProperty("idHeartRateZone") Integer idHeartRateZone, @JsonProperty("idHeartRate") Integer idHeartRate, @JsonProperty("timestamp") Long timestamp, @JsonProperty("status") Integer status, @JsonProperty("datasource") Integer datasource,  @JsonProperty("max") Integer max,  @JsonProperty("min") Integer min,  @JsonProperty("minutes") Integer minutes, @JsonProperty("caloriesOut") Float caloriesOut,  @JsonProperty("name") String name, @JsonProperty("comments") String comments) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.idHeartRateZone = idHeartRateZone;
        this.idHeartRate = idHeartRate;
        this.timestamp = timestamp;
        this.status = status;
        this.datasource = datasource;
        this.max = max;
        this.min = min;
        this.minutes = minutes;
        this.caloriesOut = caloriesOut;
        this.name = name;
        this.comments = comments;
    }

    public Integer getIdHeartRate() {
        return idHeartRate;
    }

    public void setIdHeartRate(Integer idHeartRate) {
        this.idHeartRate = idHeartRate;
    }

    public Integer getIdHeartRateZone() {
        return idHeartRateZone;
    }

    public void setIdHeartRateZone(Integer idHeartRateZone) {
        this.idHeartRateZone = idHeartRateZone;
    }

    public Float getCaloriesOut() {
        return caloriesOut;
    }

    public void setCaloriesOut(Float caloriesOut) {
        this.caloriesOut = caloriesOut;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getId() {
        return getIdHeartRateZone();
    }

    @Override
    public void setId(Integer id) {
        setIdHeartRateZone(id);
    }

    @Override
    public String toShareString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es_ES"));
        String message = "Me gustaría compartir contigo que el " + sdf.format(new Date(timestamp)) + " hice ejercicio y la zona era:\n";
        message += "Zona: " + name + "\n";
        message += "Minutos: " + minutes + "\n";
        message += "Mínimo de la zona: " + min + "\n";
        message += "Máximo de la zona: " + max + "\n";
        message += "Calorías quemadas en la zona: " + caloriesOut + "\n";
        return message;
    }
}
