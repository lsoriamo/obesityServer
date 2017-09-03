package dad.us.dadVertx.entities.health.values;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lsori on 12/06/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class HealthValue {
    @JsonProperty("iduser")
    protected Long iduser;

    @JsonProperty("timestamp")
    protected Long timestamp;

    @JsonProperty("status")
    protected Integer status;

    @JsonProperty("datasource")
    protected Integer datasource;

    @JsonProperty("comments")
    protected String comments;

    @JsonProperty("lastUpdateTimestamp")
    protected Long lastUpdateTimestamp;

    public HealthValue() {
        this.iduser = 0l;
        this.timestamp = 0l;
        this.status = 1;
        this.datasource = Summary.SummaryDataSource.user.ordinal();
        this.comments = "";
        this.lastUpdateTimestamp = Calendar.getInstance().getTimeInMillis();
    }

    public Long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public Long getIduser() {
        return iduser;
    }

    public void setIduser(Long iduser) {
        this.iduser = iduser;
    }

    public abstract Integer getId();

    public abstract void setId(Integer id);

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

    public Integer getDatasource() {
        return datasource;
    }

    public void setDatasource(Integer datasource) {
        this.datasource = datasource;
    }

    public Summary.SummaryDataSource getDatasourceEnum() {
        return Summary.SummaryDataSource.values()[datasource];
    }

    public void setDatasourceEnum(Summary.SummaryDataSource datasource) {
        this.datasource = datasource.ordinal();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }


    @Override
    public String toString() {
        return "HealthValue{" +
                "iduser=" + iduser +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", datasource=" + datasource +
                ", comments='" + comments + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HealthValue that = (HealthValue) o;

        if (iduser != null ? !iduser.equals(that.iduser) : that.iduser != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (datasource != null ? !datasource.equals(that.datasource) : that.datasource != null)
            return false;
        if (comments != null ? !comments.equals(that.comments) : that.comments != null)
            return false;
        return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp == null;

    }

    @Override
    public int hashCode() {
        int result = iduser != null ? iduser.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (datasource != null ? datasource.hashCode() : 0);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    public abstract String toShareString();
}
