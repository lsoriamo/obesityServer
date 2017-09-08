package dad.us.dadVertx.entities.firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.android.gcm.server.Message.Priority;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

import dad.us.dadVertx.ChatServer;
import dad.us.dadVertx.entities.chat.ChatMessage;
import dad.us.dadVertx.entities.user.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;

public class FirebaseUtils {

	private static final String serverKey = "AAAAkMiL6XU:APA91bGxxACZhbUPLexRpIzFwsHa-s8PVdqoKFSUVO0OYb3lZHFtRzNhKKcju27C9y-_UGvSWPRRBSYAXrZJkA4uJnAfEELozHMVi7pKhQ1rtC9A12LcEisovf8C_V5at72OgY2SkV7R";

	public FirebaseUtils() {
		super();
	}

	public static Future<MulticastResult> sendMessage(List<String> clientFirebaseToken, String messageTitle,
			String messageBody) {
		Future<MulticastResult> res = Future.future();
		Thread t = new Thread() {
			public void run() {
				try {
					Sender sender = new FCMSender(serverKey);
					com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
							.priority(Priority.HIGH).timeToLive(5).delayWhileIdle(true)
							.addData("messageBody", messageBody).addData("messageTitle", messageTitle)
							// .notification(new
							// Notification.Builder("").body(messageBody).title(messageTitle).build())
							.build();
					MulticastResult result = sender.send(message, clientFirebaseToken, 1);
					res.complete(result);
				} catch (Exception e) {
					res.fail(e.getCause());
				}
			};
		};
		t.start();
		return res;
	}

	public static void sendMessageToGroup(Integer groupId, String messageTitle, String messageBody) {
		ChatServer.getGroupUsers(groupId).setHandler(new Handler<AsyncResult<List<User>>>() {

			@Override
			public void handle(AsyncResult<List<User>> event) {
				List<String> tokens = new ArrayList<>();
				new Handler<Integer>() {
					@Override
					public void handle(Integer idx) {
						if (idx < event.result().size()) {
							final Handler<Integer> self = this;
							User user = event.result().get(idx);
							ChatServer.getUserFirebaseToken(user.getIduser()).setHandler(handler -> {
								if (handler.succeeded() && handler.result() != null) {
									tokens.add(handler.result().getFirebase_token());
								}
								self.handle(idx + 1);
							});
						} else {
							FirebaseUtils.sendMessage(tokens, messageTitle, messageBody);
						}
					}
				}.handle(0);
			}
		});
	}

	public static Future<Boolean> sendMessageToGroup(Integer groupId, List<User> users, Long userException,
			ChatMessage message) {
		Future<Boolean> future = Future.future();
		if (userException != null) {
			users = users.stream().filter(user -> !user.getIduser().equals(userException)).collect(Collectors.toList());
		}
		List<User> usersFinal = new ArrayList<>(users);
		List<String> tokens = new ArrayList<>();
		List<Future> futures = new ArrayList<>();
		new Handler<Integer>() {
			@Override
			public void handle(Integer idx) {
				if (idx < usersFinal.size()) {
					final Handler<Integer> self = this;
					User user = usersFinal.get(idx);
					Future<FirebaseEntity> fut = Future.future();
					futures.add(fut);
					ChatServer.getUserFirebaseToken(user.getIduser()).setHandler(handler -> {
						if (handler.succeeded() && handler.result() != null) {
							tokens.add(handler.result().getFirebase_token());
							fut.complete();
						}else{
							fut.fail("");
						}
						self.handle(idx + 1);
					});
				} else {
					CompositeFuture.all(futures).setHandler(handler -> {
						FirebaseUtils.sendMessage(tokens, "group_message", Json.encodePrettily(message));
						future.complete(true);
					});
				}
			}
		}.handle(0);
		return future;
	}

}
