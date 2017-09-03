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
public class BloodPressure extends HealthValue {
    @JsonProperty("idBloodPressure")
    private Integer idBloodPressure;

    @JsonProperty("maxValue")
    private Float maxValue;

    @JsonProperty("minValue")
    private Float minValue;

    public BloodPressure() {
        super();
        maxValue = 0f;
        minValue = 0f;
    }

    @JsonCreator
    public BloodPressure(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp, @JsonProperty("idBloodPressure") Integer idBloodPressure, @JsonProperty("timestamp") Long timestamp, @JsonProperty("status") Integer status, @JsonProperty("datasource") Integer datasource, @JsonProperty("maxValue") Float maxValue, @JsonProperty("minValue") Float minValue, @JsonProperty("comments") String comments) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.idBloodPressure = idBloodPressure;
        this.timestamp = timestamp;
        this.status = status;
        this.datasource = datasource;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.comments = comments;
    }

    public Integer getIdBloodPressure() {
        return idBloodPressure;
    }

    public void setIdBloodPressure(Integer idBloodPressure) {
        this.idBloodPressure = idBloodPressure;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public void setMinValue(Float minValue) {
        this.minValue = minValue;
    }

    @Override
    public Integer getId() {
        return getIdBloodPressure();
    }

    @Override
    public void setId(Integer id) {
        setIdBloodPressure(id);
    }

    @Override
    public String toShareString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es_ES"));
        String message = "Me gustaría compartir contigo este valor de tensión que me he tomado el " + sdf.format(new Date(timestamp)) + ": \n"
                + "Mínima: " + minValue + "\n"
                + "Máxima: " + maxValue;
        return message;
    }
}
