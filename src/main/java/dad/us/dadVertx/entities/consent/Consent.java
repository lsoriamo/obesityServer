package dad.us.dadVertx.entities.consent;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import dad.us.dadVertx.entities.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Consent {

	private Integer idconsent;
	private User user_id;
	private String centro;
	private String nombre;
	private String apellidos;
	private String dni;
	private String servicio;
	private String informacion_interes;
	private Long timestamp;
	private Long sign_timestamp;
	private Boolean acciones_oportunas;
	private Boolean muestras_biologicas;
	private Boolean muestras_investigacion;
	private Boolean imagenes;
	private Integer tipo_intervencion;
	private String udid;
	private List<User> medical_team;

	@JsonCreator
	public Consent(@JsonProperty("idconsent") Integer idconsent, @JsonProperty("user_id") User user_id,
			@JsonProperty("centro") String centro, @JsonProperty("servicio") String servicio,
			@JsonProperty("informacion_interes") String informacion_interes, @JsonProperty("timestamp") Long timestamp,
			@JsonProperty("sign_timestamp") Long sign_timestamp,
			@JsonProperty("acciones_oportunas") Boolean acciones_oportunas,
			@JsonProperty("muestras_biologicas") Boolean muestras_biologicas,
			@JsonProperty("muestras_investigacion") Boolean muestras_investigacion,
			@JsonProperty("imagenes") Boolean imagenes, @JsonProperty("tipo_intervencion") Integer tipo_intervencion,
			@JsonProperty("medical_team") List<User> medical_team, @JsonProperty("nombre") String nombre,
			@JsonProperty("apellidos") String apellidos, @JsonProperty("dni") String dni,
			@JsonProperty("udid") String udid) {
		super();
		this.idconsent = idconsent;
		this.user_id = user_id;
		this.centro = centro;
		this.servicio = servicio;
		this.informacion_interes = informacion_interes;
		this.timestamp = timestamp;
		this.sign_timestamp = sign_timestamp;
		this.acciones_oportunas = acciones_oportunas;
		this.muestras_biologicas = muestras_biologicas;
		this.muestras_investigacion = muestras_investigacion;
		this.imagenes = imagenes;
		this.tipo_intervencion = tipo_intervencion;
		this.medical_team = medical_team;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.dni = dni;
		this.udid = udid;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public List<User> getMedical_team() {
		return medical_team;
	}

	public void setMedical_team(List<User> medical_team) {
		this.medical_team = medical_team;
	}

	public Integer getIdconsent() {
		return idconsent;
	}

	public void setIdconsent(Integer idconsent) {
		this.idconsent = idconsent;
	}

	public User getUser_id() {
		return user_id;
	}

	public void setUser_id(User user_id) {
		this.user_id = user_id;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public String getServicio() {
		return servicio;
	}

	public void setServicio(String servicio) {
		this.servicio = servicio;
	}

	public String getInformacion_interes() {
		return informacion_interes;
	}

	public void setInformacion_interes(String informacion_interes) {
		this.informacion_interes = informacion_interes;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getSign_timestamp() {
		return sign_timestamp;
	}

	public void setSign_timestamp(Long sign_timestamp) {
		this.sign_timestamp = sign_timestamp;
	}

	public Boolean getAcciones_oportunas() {
		return acciones_oportunas;
	}

	public void setAcciones_oportunas(Boolean acciones_oportunas) {
		this.acciones_oportunas = acciones_oportunas;
	}

	public Boolean getMuestras_biologicas() {
		return muestras_biologicas;
	}

	public void setMuestras_biologicas(Boolean muestras_biologicas) {
		this.muestras_biologicas = muestras_biologicas;
	}

	public Boolean getMuestras_investigacion() {
		return muestras_investigacion;
	}

	public void setMuestras_investigacion(Boolean muestras_investigacion) {
		this.muestras_investigacion = muestras_investigacion;
	}

	public Boolean getImagenes() {
		return imagenes;
	}

	public void setImagenes(Boolean imagenes) {
		this.imagenes = imagenes;
	}

	public Integer getTipo_intervencion() {
		return tipo_intervencion;
	}

	public void setTipo_intervencion(Integer tipo_intervencion) {
		this.tipo_intervencion = tipo_intervencion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idconsent == null) ? 0 : idconsent.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((tipo_intervencion == null) ? 0 : tipo_intervencion.hashCode());
		result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Consent other = (Consent) obj;
		if (idconsent == null) {
			if (other.idconsent != null)
				return false;
		} else if (!idconsent.equals(other.idconsent))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (tipo_intervencion == null) {
			if (other.tipo_intervencion != null)
				return false;
		} else if (!tipo_intervencion.equals(other.tipo_intervencion))
			return false;
		if (user_id == null) {
			if (other.user_id != null)
				return false;
		} else if (!user_id.equals(other.user_id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Consent [idconsent=" + idconsent + ", user_id=" + user_id + ", centro=" + centro + ", servicio="
				+ servicio + ", informacion_interes=" + informacion_interes + ", timestamp=" + timestamp
				+ ", sign_timestamp=" + sign_timestamp + ", acciones_oportunas=" + acciones_oportunas
				+ ", muestras_biologicas=" + muestras_biologicas + ", muestras_investigacion=" + muestras_investigacion
				+ ", imagenes=" + imagenes + ", tipo_intervencion=" + tipo_intervencion + "]";
	}

}
