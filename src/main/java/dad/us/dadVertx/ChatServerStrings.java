package dad.us.dadVertx;

import dad.us.dadVertx.entities.User;

public class ChatServerStrings {

	public static String userLeftGroup(User user) {
		return user.getNickname() + " dej� la conversaci�n";
	}

	public static String getGroupCreatedBy(User user) {
		return "Grupo creado por " + user.getNickname();
	}

	public static String getSingleUserGroupDescription(User user, User friend) {
		return "Conversaci�n entre " + user.getNickname() + " y " + friend.getNickname();
	}
}
