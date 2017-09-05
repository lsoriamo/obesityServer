package dad.us.dadVertx.entities.user;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData {

    private Integer iduser_data;
    private Long user_id;
    private Long nacimiento;
    private Integer altura;
    private Integer peso;
    private Boolean hipertension;
    private Boolean diabetes;
    private Boolean apnea;
    private Boolean lesion_articular;
    private Boolean hiperlipidemia;
    private Boolean vesicula;
    private Boolean higado;
    private Boolean osteoporosis;
    private Boolean cardiaca;
    private Integer ejercicio;
    private Long fecha_intervencion;
    private Long fecha_primer_uso_app;
    private Integer peso_objetivo;

    public UserData() {
        super();
        this.iduser_data = -1;
        this.user_id = -1L;
        this.nacimiento = 0l;
        this.altura = 0;
        this.peso = 0;
        this.hipertension = false;
        this.diabetes = false;
        this.apnea = false;
        this.lesion_articular = false;
        this.hiperlipidemia = false;
        this.vesicula = false;
        this.higado = false;
        this.osteoporosis = false;
        this.cardiaca = false;
        this.higado = false;
        this.fecha_intervencion = 0l;
        this.fecha_primer_uso_app = Calendar.getInstance().getTimeInMillis();
        this.ejercicio = 0;
        this.peso_objetivo= 0;
    }

    public UserData(@JsonProperty("iduser_data") Integer iduser_data, @JsonProperty("user_id") Long user_id,
                    @JsonProperty("nacimiento") Long nacimiento, @JsonProperty("altura") Integer altura,
                    @JsonProperty("peso") Integer peso, @JsonProperty("hipertension") Boolean hipertension,
                    @JsonProperty("diabetes") Boolean diabetes, @JsonProperty("apnea") Boolean apnea, @JsonProperty("lesion_articular") Boolean lesion_articular,
                    @JsonProperty("hiperlipidemia") Boolean hiperlipidemia, @JsonProperty("vesicula") Boolean vesicula,
                    @JsonProperty("higado") Boolean higado, @JsonProperty("osteoporosis") Boolean osteoporosis, @JsonProperty("cardiaca") Boolean cardiaca,
                    @JsonProperty("ejercicio") Integer ejercicio, @JsonProperty("fecha_primer_uso_app") Long fecha_primer_uso_app,
                    @JsonProperty("fecha_intervencion") Long fecha_intervencion, @JsonProperty("peso_objetivo") Integer peso_objetivo) {
        super();
        this.iduser_data = iduser_data;
        this.user_id = user_id;
        this.nacimiento = nacimiento;
        this.altura = altura;
        this.peso = peso;
        this.hipertension = hipertension;
        this.diabetes = diabetes;
        this.apnea = apnea;
        this.lesion_articular = lesion_articular;
        this.hiperlipidemia = hiperlipidemia;
        this.vesicula = vesicula;
        this.higado = higado;
        this.osteoporosis = osteoporosis;
        this.cardiaca = cardiaca;
        this.ejercicio = ejercicio;
        this.fecha_intervencion = fecha_intervencion;
        this.fecha_primer_uso_app = fecha_primer_uso_app;
        this.peso_objetivo = peso_objetivo;
    }
    
    

    public Integer getPeso_objetivo() {
		return peso_objetivo;
	}

	public void setPeso_objetivo(Integer peso_objetivo) {
		this.peso_objetivo = peso_objetivo;
	}

	public Integer getIduser_data() {
        return iduser_data;
    }

    public void setIduser_data(Integer iduser_data) {
        this.iduser_data = iduser_data;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Long getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(Long nacimiento) {
        this.nacimiento = nacimiento;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public Boolean getHipertension() {
        return hipertension;
    }

    public void setHipertension(Boolean hipertension) {
        this.hipertension = hipertension;
    }

    public Boolean getDiabetes() {
        return diabetes;
    }

    public void setDiabetes(Boolean diabetes) {
        this.diabetes = diabetes;
    }

    public Boolean getApnea() {
        return apnea;
    }

    public void setApnea(Boolean apnea) {
        this.apnea = apnea;
    }

    public Boolean getLesion_articular() {
        return lesion_articular;
    }

    public void setLesion_articular(Boolean lesion_articular) {
        this.lesion_articular = lesion_articular;
    }

    public Integer getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Integer ejercicio) {
        this.ejercicio = ejercicio;
    }

    public Boolean getHiperlipidemia() {
        return hiperlipidemia;
    }

    public void setHiperlipidemia(Boolean hiperlipidemia) {
        this.hiperlipidemia = hiperlipidemia;
    }

    public Boolean getVesicula() {
        return vesicula;
    }

    public void setVesicula(Boolean vesicula) {
        this.vesicula = vesicula;
    }

    public Boolean getHigado() {
        return higado;
    }

    public void setHigado(Boolean higado) {
        this.higado = higado;
    }

    public Boolean getOsteoporosis() {
        return osteoporosis;
    }

    public void setOsteoporosis(Boolean osteoporosis) {
        this.osteoporosis = osteoporosis;
    }

    public Boolean getCardiaca() {
        return cardiaca;
    }

    public void setCardiaca(Boolean cardiaca) {
        this.cardiaca = cardiaca;
    }

    public Long getFecha_intervencion() {
        return fecha_intervencion;
    }

    public void setFecha_intervencion(Long fecha_intervencion) {
        this.fecha_intervencion = fecha_intervencion;
    }

    public Long getFecha_primer_uso_app() {
        return fecha_primer_uso_app;
    }

    public void setFecha_primer_uso_app(Long fecha_primer_uso_app) {
        this.fecha_primer_uso_app = fecha_primer_uso_app;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserData userData = (UserData) o;

        if (iduser_data != null ? !iduser_data.equals(userData.iduser_data) : userData.iduser_data != null)
            return false;
        if (user_id != null ? !user_id.equals(userData.user_id) : userData.user_id != null)
            return false;
        if (nacimiento != null ? !nacimiento.equals(userData.nacimiento) : userData.nacimiento != null)
            return false;
        if (altura != null ? !altura.equals(userData.altura) : userData.altura != null)
            return false;
        if (peso != null ? !peso.equals(userData.peso) : userData.peso != null) return false;
        if (hipertension != null ? !hipertension.equals(userData.hipertension) : userData.hipertension != null)
            return false;
        if (diabetes != null ? !diabetes.equals(userData.diabetes) : userData.diabetes != null)
            return false;
        if (apnea != null ? !apnea.equals(userData.apnea) : userData.apnea != null) return false;
        if (lesion_articular != null ? !lesion_articular.equals(userData.lesion_articular) : userData.lesion_articular != null)
            return false;
        if (hiperlipidemia != null ? !hiperlipidemia.equals(userData.hiperlipidemia) : userData.hiperlipidemia != null)
            return false;
        if (vesicula != null ? !vesicula.equals(userData.vesicula) : userData.vesicula != null)
            return false;
        if (higado != null ? !higado.equals(userData.higado) : userData.higado != null)
            return false;
        if (osteoporosis != null ? !osteoporosis.equals(userData.osteoporosis) : userData.osteoporosis != null)
            return false;
        if (cardiaca != null ? !cardiaca.equals(userData.cardiaca) : userData.cardiaca != null)
            return false;
        return fecha_intervencion != null ? fecha_intervencion.equals(userData.fecha_intervencion) : userData.fecha_intervencion == null;

    }

    @Override
    public int hashCode() {
        int result = iduser_data != null ? iduser_data.hashCode() : 0;
        result = 31 * result + (user_id != null ? user_id.hashCode() : 0);
        result = 31 * result + (nacimiento != null ? nacimiento.hashCode() : 0);
        result = 31 * result + (altura != null ? altura.hashCode() : 0);
        result = 31 * result + (peso != null ? peso.hashCode() : 0);
        result = 31 * result + (hipertension != null ? hipertension.hashCode() : 0);
        result = 31 * result + (diabetes != null ? diabetes.hashCode() : 0);
        result = 31 * result + (apnea != null ? apnea.hashCode() : 0);
        result = 31 * result + (lesion_articular != null ? lesion_articular.hashCode() : 0);
        result = 31 * result + (hiperlipidemia != null ? hiperlipidemia.hashCode() : 0);
        result = 31 * result + (vesicula != null ? vesicula.hashCode() : 0);
        result = 31 * result + (higado != null ? higado.hashCode() : 0);
        result = 31 * result + (osteoporosis != null ? osteoporosis.hashCode() : 0);
        result = 31 * result + (cardiaca != null ? cardiaca.hashCode() : 0);
        result = 31 * result + (fecha_intervencion != null ? fecha_intervencion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "iduser_data=" + iduser_data +
                ", user_id=" + user_id +
                ", nacimiento=" + nacimiento +
                ", altura=" + altura +
                ", peso=" + peso +
                ", hipertension=" + hipertension +
                ", diabetes=" + diabetes +
                ", apnea=" + apnea +
                ", lesion_articular=" + lesion_articular +
                ", hiperlipidemia=" + hiperlipidemia +
                ", vesicula=" + vesicula +
                ", higado=" + higado +
                ", osteoporosis=" + osteoporosis +
                ", cardiaca=" + cardiaca +
                ", fecha_intervencion=" + fecha_intervencion +
                '}';
    }
}
