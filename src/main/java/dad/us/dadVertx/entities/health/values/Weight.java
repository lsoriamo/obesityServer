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
public class Weight extends HealthValue {
    @JsonProperty("idWeight")
    private Integer idWeight;
    @JsonProperty("value")
    private Float value;
    @SerializedName("fat")
    private Double fat;
    @SerializedName("bmi")
    private Double bmi;

    public Weight() {
        super();
        value= 0f;
        fat = 0d;
        bmi = 0d;
    }

    @JsonCreator
    public Weight(@JsonProperty("lastUpdateTimestamp") Long lastUpdateTimestamp, @JsonProperty("idWeight") Integer idWeight, @JsonProperty("timestamp") Long timestamp, @JsonProperty("status") Integer status, @JsonProperty("datasource") Integer datasource, @JsonProperty("value") Float value, @JsonProperty("bmi") Double bmi, @JsonProperty("fat") Double fat, @JsonProperty("comments") String comments) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.idWeight = idWeight;
        this.timestamp = timestamp;
        this.status = status;
        this.datasource = datasource;
        this.value = value;
        this.comments = comments;
        this.bmi = bmi;
        this.fat = fat;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Integer getIdWeight() {
        return idWeight;
    }

    public void setIdWeight(Integer idWeight) {
        this.idWeight = idWeight;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public Integer getId() {
        return getIdWeight();
    }

    @Override
    public void setId(Integer id) {
        setIdWeight(id);
    }

    @Override
    public String toShareString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", new Locale("es_ES"));
        String message = "Me gustaría compartir contigo que el " + sdf.format(new Date(timestamp)) + " pesaba " + value + " kilos.\n";
        if (bmi > 0 && fat > 0)
            message += "Mi índice de masa corporal era de " + bmi + " y el íncide de grasa " + fat + ".";
        return message;
    }
}
