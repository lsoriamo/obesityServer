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

/**
 * Created by lsori on 12/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class BloodGlucose extends HealthValue {
    @JsonProperty("idBloodGlucose")
    private Integer idBloodGlucose;

    @JsonProperty("value")
    private Float value;


    public BloodGlucose() {
        super();
        value= 0f;
    }

    @JsonCreator
    public BloodGlucose(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp, @JsonProperty("idBloodGlucose") Integer idBloodGlucose, @JsonProperty("timestamp") Long timestamp, @JsonProperty("status") Integer status, @JsonProperty("datasource") Integer datasource, @JsonProperty("value") Float value, @JsonProperty("comments") String comments) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.idBloodGlucose = idBloodGlucose;
        this.timestamp = timestamp;
        this.status = status;
        this.datasource = datasource;
        this.value = value;
        this.comments = comments;
    }

    public Integer getIdBloodGlucose() {
        return idBloodGlucose;
    }

    public void setIdBloodGlucose(Integer idBloodGlucose) {
        this.idBloodGlucose = idBloodGlucose;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public Integer getId() {
        return getIdBloodGlucose();
    }

    @Override
    public void setId(Integer id) {
        setIdBloodGlucose(id);
    }

    @Override
    public String toShareString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es_ES"));
        String message = "Me gustaría compartir contigo que el " + sdf.format(new Date(timestamp)) + " tenía la glucosa a " + value;
        return message;
    }
}
