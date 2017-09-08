package dad.us.dadVertx.entities.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatGroup {

	private Integer idchat_group;
	private String name;
	private Long creation_date;
	private String description;
	private String dialogPhoto;

	@JsonCreator
    public ChatGroup(@JsonProperty("idchat_group") Integer idchat_group, @JsonProperty("name") String name,
                     @JsonProperty("creation_date") Long creation_date, @JsonProperty("description") String description, @JsonProperty("dialogPhoto") String dialogPhoto) {
        this.idchat_group = idchat_group;
        this.name = name;
        this.creation_date = creation_date;
        this.description = description;
        this.dialogPhoto = dialogPhoto;
	}

	public void setCreationDateFromDate(Date date) {
		this.creation_date = date.getTime();
	}

	public String getFormattedCreationDate() {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String dateString = formatter.format(new Date(creation_date));
		return dateString;
	}
	
	public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }

    public String getDialogPhoto() {
        return this.dialogPhoto;
    }

	public Integer getIdchat_group() {
		return idchat_group;
	}

	public void setIdchat_group(Integer idchat_group) {
		this.idchat_group = idchat_group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCreation_date() {
		return creation_date;
	}

	public void setCreation_date(Long creation_date) {
		this.creation_date = creation_date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "ChatGroup [idchat_group=" + idchat_group + ", name=" + name + ", creation_date=" + creation_date
				+ ", description=" + description + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creation_date == null) ? 0 : creation_date.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((idchat_group == null) ? 0 : idchat_group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ChatGroup other = (ChatGroup) obj;
		if (creation_date == null) {
			if (other.creation_date != null)
				return false;
		} else if (!creation_date.equals(other.creation_date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (idchat_group == null) {
			if (other.idchat_group != null)
				return false;
		} else if (!idchat_group.equals(other.idchat_group))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}