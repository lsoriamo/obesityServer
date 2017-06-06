package dad.us.dadVertx.entities.activities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lsori on 01/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Aim {

    Integer idAim;
    Long timestamp;
    Float value;
    Integer type;
    Integer iduser;

    @JsonCreator
    public Aim(@JsonProperty("iduser") Integer iduser, @JsonProperty("idAim") Integer idAim,
               @JsonProperty("timestamp") Long timestamp, @JsonProperty("value") Float value,
               @JsonProperty("type") Integer type) {
        this.idAim = idAim;
        this.timestamp = timestamp;
        this.value = value;
        this.type = type;
        this.iduser = iduser;
    }

    public Integer getIdAim() {
        return idAim;
    }

    public void setIdAim(Integer idAim) {
        this.idAim = idAim;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public Integer getIduser() {
        return iduser;
    }

    public void setIduser(Integer iduser) {
        this.iduser = iduser;
    }

    public Integer getType() {
        return type;
    }

    public AimType getTypeAsStruct() {
        return AimType.values()[type];
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setTypeAsStruct(AimType type) {
        this.type = type.ordinal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Aim aim = (Aim) o;

        if (idAim != aim.idAim)
            return false;
        if (timestamp != aim.timestamp)
            return false;
        if (Double.compare(aim.value, value) != 0)
            return false;
        return type == aim.type;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (idAim ^ (idAim >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "Aim{" + "timestamp=" + timestamp + ", value=" + value + ", type=" + type + '}';
    }

    public enum AimType {
        caloriesOut, steps, distance, activeMinutes
    }
}
