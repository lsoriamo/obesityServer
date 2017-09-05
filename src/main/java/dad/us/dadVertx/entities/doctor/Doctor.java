package dad.us.dadVertx.entities.doctor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Doctor {

	@JsonProperty("idDoctor")
	private Long idDoctor;

	@JsonProperty("name")
	private String name;

	@JsonProperty("surname")
	private String surname;

	@JsonProperty("specialty")
	private Integer specialty;

	@JsonCreator
	public Doctor(@JsonProperty("idDoctor") Long idDoctor, @JsonProperty("name") String name,
			@JsonProperty("surname") String surname, @JsonProperty("specialty") Integer specialty) {
		this.idDoctor = idDoctor;
		this.name = name;
		this.surname = surname;
		this.specialty = specialty;
	}

	public Long getIdDoctor() {
		return idDoctor;
	}

	public void setIdDoctor(Long idDoctor) {
		this.idDoctor = idDoctor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Integer getSpecialty() {
		return specialty;
	}

	public void setSpecialty(Integer specialty) {
		this.specialty = specialty;
	}

	@Override
	public String toString() {
		return "Doctor [idDoctor=" + idDoctor + ", name=" + name + ", surname=" + surname + ", specialty=" + specialty
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idDoctor == null) ? 0 : idDoctor.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((specialty == null) ? 0 : specialty.hashCode());
		result = prime * result + ((surname == null) ? 0 : surname.hashCode());
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
		Doctor other = (Doctor) obj;
		if (idDoctor == null) {
			if (other.idDoctor != null)
				return false;
		} else if (!idDoctor.equals(other.idDoctor))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (specialty == null) {
			if (other.specialty != null)
				return false;
		} else if (!specialty.equals(other.specialty))
			return false;
		if (surname == null) {
			if (other.surname != null)
				return false;
		} else if (!surname.equals(other.surname))
			return false;
		return true;
	}
	
	

}
