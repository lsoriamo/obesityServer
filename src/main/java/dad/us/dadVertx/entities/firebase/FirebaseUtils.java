package dad.us.dadVertx.entities.firebase;

import java.util.List;
import java.util.stream.Collectors;

import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import dad.us.dadVertx.ChatServer;
import dad.us.dadVertx.entities.chat.ChatMessage;
import dad.us.dadVertx.entities.user.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;

public class FirebaseUtils {

	private static final String serverKey = "AAAAkMiL6XU:APA91bGxxACZhbUPLexRpIzFwsHa-s8PVdqoKFSUVO0OYb3lZHFtRzNhKKcju27C9y-_UGvSWPRRBSYAXrZJkA4uJnAfEELozHMVi7pKhQ1rtC9A12LcEisovf8C_V5at72OgY2SkV7R";

	public FirebaseUtils() {
		super();
	}

	public static Future<Result> sendMessage(String clientFirebaseToken, String messageTitle, String messageBody) {
		Future<Result> res = Future.future();
		Thread t = new Thread() {
			public void run() {
				try {
					Sender sender = new FCMSender(serverKey);
					com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
							.collapseKey("message").timeToLive(3).delayWhileIdle(true)
							.notification(new Notification.Builder("").body(messageBody).title(messageTitle).build())
							.build();
					Result result = sender.send(message, clientFirebaseToken, 1);
					res.complete(result);
				} catch (Exception e) {
					res.fail(e.getCause());
				}
			};
		};
		t.start();
		return res;
	}

	public static void sendMessageToGroup(Integer groupId, Integer userException, ChatMessage message) {
		ChatServer.getGroupUsers(groupId).setHandler(new Handler<AsyncResult<List<User>>>() {

			@Override
			public void handle(AsyncResult<List<User>> event) {
				List<User> users = event.result();
				if (userException != null)
					users = users.stream().filter(user -> !user.getIduser().equals(userException))
							.collect(Collectors.toList());
				users.stream().forEach(user -> {
					ChatServer.getUserFirebaseToken(user.getIduser()).setHandler(handler -> {
						if (handler.succeeded() && handler.result() != null)
							FirebaseUtils.sendMessage(handler.result().getFirebase_token(), "group_message",
									Json.encodePrettily(message));
					});
				});
			}
		});
	}

}
