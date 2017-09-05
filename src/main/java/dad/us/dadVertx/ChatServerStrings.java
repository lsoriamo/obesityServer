package dad.us.dadVertx;

import dad.us.dadVertx.entities.user.User;

public class ChatServerStrings {

	public static String userLeftGroup(User user) {
		return user.getNickname() + " dej� la conversaci�n";
	}

	public static String getGroupCreatedBy(String user) {
		return "Grupo creado por " + user;
	}

	public static String getSingleUserGroupDescription(String user, String friend) {
		return "Conversaci�n entre " + user + " y " + friend;
	}
}
