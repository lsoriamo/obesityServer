package dad.us.dadVertx;

import dad.us.dadVertx.entities.user.User;

public class ChatServerStrings {

	public static String userLeftGroup(User user) {
		return user.getNickname() + " dejó la conversación";
	}
	
	public static String userJoinGroup(User user) {
		return user.getNickname() + " se unión a la conversación";
	}

	public static String getGroupCreatedBy(String user) {
		return "Grupo creado por " + user;
	}

	public static String getSingleUserGroupDescription(String user, String friend) {
		return "Conversación entre " + user + " y " + friend;
	}
}
