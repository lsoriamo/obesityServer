package dad.us.dadVertx.entities.health.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lsori on 12/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartRate extends HealthValue {
    @JsonProperty("idHeartRate")
    private Integer idHeartRate;

    @JsonProperty("value")
    private Float value;

    @JsonProperty("heartRateZones")
    private HeartRateZone[] heartRateZones;

    public HeartRate() {
        super();
        value = 0f;
        heartRateZones = new HeartRateZone[0];
    }

    @JsonCreator
    public HeartRate(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp, @JsonProperty("idHeartRate") Integer idHeartRate, @JsonProperty("timestamp") Long timestamp,
                     @JsonProperty("status") Integer status, @JsonProperty("datasource") Integer datasource,
                     @JsonProperty("value") Float value, @JsonProperty("comments") String comments,
                     @JsonProperty("heartRateZones") HeartRateZone[] heartRateZones) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.idHeartRate = idHeartRate;
        this.timestamp = timestamp;
        this.status = status;
        this.datasource = datasource;
        this.value = value;
        this.comments = comments;
        this.heartRateZones = heartRateZones;
    }

    public HeartRateZone[] getHeartRateZones() {
        return heartRateZones;
    }

    public void setHeartRateZones(HeartRateZone[] heartRateZones) {
        this.heartRateZones = heartRateZones;
    }

    public Integer getIdHeartRate() {
        return idHeartRate;
    }

    public void setIdHeartRate(Integer idHeartRate) {
        this.idHeartRate = idHeartRate;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public Integer getId() {
        return getIdHeartRate();
    }

    @Override
    public void setId(Integer id) {
        setIdHeartRate(id);
    }

    @Override
    public String toShareString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es_ES"));
        String message = "Me gustaría compartir contigo que el " + sdf.format(new Date(timestamp)) + " tenía las pulsaciones a " + value + " ppm";
        return message;
    }
}
