package dad.us.dadVertx.entities.firebase;

import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import io.vertx.core.Future;

public class FirebaseUtils {

	private static final String serverKey = "AAAAkMiL6XU:APA91bGxxACZhbUPLexRpIzFwsHa-s8PVdqoKFSUVO0OYb3lZHFtRzNhKKcju27C9y-_UGvSWPRRBSYAXrZJkA4uJnAfEELozHMVi7pKhQ1rtC9A12LcEisovf8C_V5at72OgY2SkV7R";
	
	public FirebaseUtils() {
		super();
	}
	
	public static Future<Result> sendMessage(String clientFirebaseToken, String messageBody, String messageTitle){
		Future<Result> res = Future.future();
		Thread t = new Thread() {
			public void run() {
				try {
					Sender sender = new FCMSender(serverKey);
					com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
							.collapseKey("message").timeToLive(3).delayWhileIdle(true)
							.notification(new Notification.Builder("").body(messageBody).title(messageTitle).build())
							.build();
					Result result = sender.send(message,
							clientFirebaseToken,
							1);
					res.complete(result);
				} catch (Exception e) {
					res.fail(e.getCause());
				}
			};
		};
		t.start();
		return res;
	}

	
	
}
