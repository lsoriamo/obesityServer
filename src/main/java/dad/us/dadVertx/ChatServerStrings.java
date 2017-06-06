package dad.us.dadVertx;

import dad.us.dadVertx.entities.User;

public class ChatServerStrings {

	public static String userLeftGroup(User user) {
		return user.getNickname() + " dejó la conversación";
	}

	public static String getGroupCreatedBy(User user) {
		return "Grupo creado por " + user.getNickname();
	}

	public static String getSingleUserGroupDescription(User user, User friend) {
		return "Conversación entre " + user.getNickname() + " y " + friend.getNickname();
	}
}
