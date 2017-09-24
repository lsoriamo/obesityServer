package dad.us.dadVertx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import dad.us.dadVertx.entities.activities.Aim;
import dad.us.dadVertx.entities.appointment.Appointment;
import dad.us.dadVertx.entities.challenges.ChallengeEntity;
import dad.us.dadVertx.entities.chat.ChatGroup;
import dad.us.dadVertx.entities.chat.ChatMessage;
import dad.us.dadVertx.entities.chat.ChatMessageState;
import dad.us.dadVertx.entities.chat.ChatMessageState.MessageState;
import dad.us.dadVertx.entities.consent.Consent;
import dad.us.dadVertx.entities.doctor.Doctor;
import dad.us.dadVertx.entities.doctor.DoctorPatient;
import dad.us.dadVertx.entities.firebase.FirebaseEntity;
import dad.us.dadVertx.entities.firebase.FirebaseUtils;
import dad.us.dadVertx.entities.health.values.BloodGlucose;
import dad.us.dadVertx.entities.health.values.BloodPressure;
import dad.us.dadVertx.entities.health.values.Distance;
import dad.us.dadVertx.entities.health.values.HeartRate;
import dad.us.dadVertx.entities.health.values.HeartRateZone;
import dad.us.dadVertx.entities.health.values.Summary;
import dad.us.dadVertx.entities.health.values.Weight;
import dad.us.dadVertx.entities.medicaltest.MedicalTestEntity;
import dad.us.dadVertx.entities.medicine.Medicine;
import dad.us.dadVertx.entities.medicine.MedicineTaken;
import dad.us.dadVertx.entities.psychology.TestResponse;
import dad.us.dadVertx.entities.user.User;
import dad.us.dadVertx.entities.user.UserData;
import dad.us.dadVertx.security.GenerateRsaKeyPair;
import dad.us.dadVertx.watson.WatsonQuestionAnswer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

@SuppressWarnings("rawtypes")
public class ChatServer extends AbstractVerticle {

	private static AsyncSQLClient mySQLClient;
	private static WatsonQuestionAnswer watsonQuestions;
	private static MailClient mailClient;
	private static String serverAddress = "http://172.29.195.55:8080";

	@Override
	public void start() throws Exception {

		/*
		 * OJO: al hacer el despliegue en un servidor nuevo, leer esto para la
		 * excepción de key size:
		 * https://stackoverflow.com/questions/6481627/java-security-illegal-key
		 * -size-or-default-parameters PAra madeirasic
		 */
		// JsonObject config = new JsonObject().put("host",
		// "localhost").put("username", "root")
		// .put("password", "1d1nf0r!").put("database",
		// "retoobesidad").put("port", 3306).put("maxPoolSize", 100);
		// Para local
		
		JsonObject config = new JsonObject().put("host", "localhost").put("username", "root").put("password", "root")
				.put("database", "retoobesidad").put("port", 3306).put("maxPoolSize", 100);

		mySQLClient = MySQLClient.createNonShared(vertx, config);
		//TODO: hola
		MailConfig configMail = new MailConfig();
		configMail.setPort(25);
		configMail.setStarttls(StartTLSOptions.REQUIRED);
		configMail.setHostname("smtp.preobar.com");
		configMail.setUsername("lsoria@preobar.com");
		configMail.setPassword("Luismisoria_1985");
		

		configMail.setLogin(LoginOption.REQUIRED);
		configMail.setAuthMethods("LOGIN");
		//configMail.setDisableEsmtp(true);
		//configMail.setSsl(true);
		configMail.setTrustAll(true);
		//configMail.setOwnHostname("smtp.preobar.com");
		
		mailClient = MailClient.createShared(vertx, configMail);
		Router router = Router.router(vertx);

		router.route("/api/obesity/*").handler(BodyHandler.create());

		router.get("/api/obesity/security/:userId").handler(this::getPublicKey);

		router.get("/api/obesity/firebase/:userId").handler(this::getUserFirebaseToken);
		router.post("/api/obesity/firebase").handler(this::saveUserFirebaseToken);

		router.get("/api/obesity/medicaltest/:userId/:lastUpdateTime").handler(this::getMedicalTest);
		router.post("/api/obesity/medicaltest").handler(this::saveMedicalTest);
		router.post("/api/obesity/medicaltest/uploadpicture").handler(this::postMedicalTestImage);

		router.get("/api/obesity/consent/medicalgroup/:consentId").handler(this::getConsentMedicalGroup);
		router.post("/api/obesity/consent/upload").handler(this::postUploadSignature);
		router.get("/api/obesity/consent/:userId").handler(this::getPendingConsent);

		router.post("/api/obesity/user/uploadimage").handler(this::postUploadImage);
		router.get("/api/obesity/user/image/:userId").handler(this::getUserImage);
		router.get("/api/obesity/user/login/:userId").handler(this::getUser);
		router.post("/api/obesity/user/login").handler(this::saveUser);

		router.get("/api/obesity/image/:imagePath").handler(this::getImage);

		router.get("/api/obesity/user/data/:userId").handler(this::getUserData);
		router.post("/api/obesity/user/data").handler(this::saveUserData);
		router.delete("/api/obesity/user/data/:userId").handler(this::deleteUserData);

		router.get("/api/obesity/users/:idUser").handler(this::getUserById);

		router.get("/api/obesity/appointment/:userId/:lastUpdateTime").handler(this::getUserAppointment);
		router.post("/api/obesity/appointment").handler(this::saveUserAppointment);

		router.get("/api/obesity/medicine/taken/:userId").handler(this::getMedicineTakenByUser);
		router.post("/api/obesity/medicine/taken").handler(this::saveMedicineTaken);
		router.post("/api/obesity/medicine/drug").handler(this::saveMedicine);
		router.get("/api/obesity/medicine/list").handler(this::getDrugList);
		router.get("/api/obesity/medicine/drug/:userId/:lastUpdateTime").handler(this::getMedicinesByUser);

		router.get("/api/obesity/info/surgery/list/:userId").handler(this::getSurgeryInfoList);

		router.get("/api/obesity/activity/aims/:userId").handler(this::getAimsByUser);

		router.get("/api/obesity/psychology/test/results/:userId").handler(this::getTestResults);
		router.get("/api/obesity/psychology/test/:userId").handler(this::getPsychologyTest);
		router.post("/api/obesity/psychology/test").handler(this::saveTestResult);
		router.post("/api/obesity/psychology/tests").handler(this::saveTestResults);

		router.get("/api/obesity/messages/received/:userId/:messageId").handler(this::markMessageAsReceived);
		router.get("/api/obesity/messages/read/:userId/:messageId").handler(this::markMessageAsRead);
		router.post("/api/obesity/messages/read/:userId").handler(this::markMessagesAsRead);
		router.post("/api/obesity/messages/received/:userId").handler(this::markMessagesAsReceived);
		router.post("/api/obesity/messages/send").handler(this::postMessage);
		router.get("/api/obesity/messages/last/:groupId").handler(this::getGroupLastMessage);
		router.get("/api/obesity/messages/unread/:userId/:groupId").handler(this::getUnreadMessages);

		router.get("/api/obesity/messages/:groupId/:timestamp").handler(this::getGroupMessagesFromDate);
		router.get("/api/obesity/messages/:groupId/:timestamp/reverse").handler(this::getGroupMessagesUntilDate);
		router.get("/api/obesity/messages/:groupId/:fromtimestamp/:totimestamp")
				.handler(this::getGroupMessagesFromToDate);

		router.get("/api/obesity/groups/user/:userId").handler(this::getUserGroups);
		router.get("/api/obesity/groups/relateduser/:userId").handler(this::getRelatedUsers);
		router.get("/api/obesity/groups/members/:groupId").handler(this::getGroupUsers);
		router.post("/api/obesity/groups/multi").handler(this::addMultiUserGroup);
		router.post("/api/obesity/groups/single").handler(this::addSingleUserGroup);
		router.delete("/api/obesity/groups").handler(this::deleteUserFromGroup);

		router.get("/api/obesity/health/weight/:userId/:lastUpdateTime/:startTime/:endTime")
				.handler(this::getHealthWeight);
		router.post("/api/obesity/health/weight").handler(this::saveHealthWeight);
		router.get("/api/obesity/health/pressure/:userId/:lastUpdateTime/:startTime/:endTime")
				.handler(this::getBloodPressure);
		router.post("/api/obesity/health/pressure").handler(this::saveBloodPressure);
		router.get("/api/obesity/health/glucose/:userId/:lastUpdateTime/:startTime/:endTime")
				.handler(this::getBloodGlucose);
		router.post("/api/obesity/health/glucose").handler(this::saveBloodGlucose);
		router.get("/api/obesity/health/heartrate/:userId/:lastUpdateTime/:startTime/:endTime")
				.handler(this::getHeartRate);
		router.post("/api/obesity/health/heartrate").handler(this::saveHeartRate);
		router.get("/api/obesity/health/activitysummary/:userId/:lastUpdateTime/:startTime/:endTime")
				.handler(this::getActivitySummary);
		router.post("/api/obesity/health/activitysummary/:userId").handler(this::saveActivitySummary);

		router.get("/api/obesity/challenge/:userId/:lastUpdateTime").handler(this::getChallenge);
		router.post("/api/obesity/challenge").handler(this::saveChallenge);

		router.get("/api/obesity/doctor/:userId").handler(this::getDoctor);
		router.get("/api/obesity/doctor/request/:idDoctor/:idPatient").handler(this::requestDoctorPatient);
		router.get("/api/obesity/doctor/validate/:idDoctor/:idPatient").handler(this::vaildateDoctorPatient);

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);

		watsonQuestions = new WatsonQuestionAnswer("ObesityFAQ", "sc6891d3ab_a39f_4133_9f8d_ea7b351ec170", "Obesity");

		/*
		 * vertx.setPeriodic(3000, handler -> {
		 * FirebaseUtils.sendMessageToGroup(53, "group_message",
		 * Json.encodePrettily(new ChatMessage("Hola mundo", (int)
		 * Calendar.getInstance().getTimeInMillis(), 53, 3L,
		 * Calendar.getInstance().getTimeInMillis()))); });
		 */

	}

	private void getPublicKey(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		if (GenerateRsaKeyPair.publicKeyString.isEmpty() || GenerateRsaKeyPair.privateKeyString.isEmpty())
			GenerateRsaKeyPair.generateSecurityKeys();

		getUser(userId).setHandler(res -> {
			if (res.succeeded() && res.result() != null) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(GenerateRsaKeyPair.publicKeyString));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void markMessageAsReceived(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Integer messageId = new Integer(routingContext.request().getParam("messageId"));
		addMessageState(messageId, userId, MessageState.Received.name(), Calendar.getInstance().getTimeInMillis())
				.setHandler(res -> {
					if (res.succeeded()) {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
					} else {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
										new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
					}
				});
	}

	private void markMessagesAsRead(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Integer[] messages = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				Integer[].class);

		if (messages.length > 0) {
			addMessageState(userId, Arrays.asList(messages), MessageState.Read.name(),
					Calendar.getInstance().getTimeInMillis()).setHandler(res -> {
						if (res.succeeded()) {
							routingContext.response()
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
						} else {
							routingContext.response()
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
											new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
						}
					});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void markMessagesAsReceived(RoutingContext routingContext) {
		Integer[] messages = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				Integer[].class);
		Long userId = new Long(routingContext.request().getParam("userId"));
		if (messages.length > 0) {
			addMessageState(userId, Arrays.asList(messages), MessageState.Received.name(),
					Calendar.getInstance().getTimeInMillis()).setHandler(res -> {
						if (res.succeeded()) {
							routingContext.response()
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
						} else {
							routingContext.response()
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
											new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
						}
					});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void markMessageAsRead(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Integer messageId = new Integer(routingContext.request().getParam("messageId"));
		addMessageState(messageId, userId, MessageState.Read.name(), Calendar.getInstance().getTimeInMillis())
				.setHandler(res -> {
					if (res.succeeded()) {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
					} else {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
										new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
					}
				});
	}

	private void postMessage(RoutingContext routingContext) {
		ChatMessage[] messages = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				ChatMessage[].class);

		Arrays.asList(messages).stream().forEach(message -> {
			try {
				message.setTimestamp(Calendar.getInstance().getTimeInMillis());
				message.setMessage(StringEscapeUtils.unescapeJava(message.getMessage()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		saveMessagesInDatabase(Arrays.asList(messages)).setHandler(res -> {
			if (res.succeeded() && res.result() != null) {
				new Handler<Integer>() {
					@Override
					public void handle(Integer idx) {
						if (idx < res.result().size()) {
							final Handler<Integer> self = this;
							ChatMessage msg = res.result().get(idx);
							addMessageState(msg.getId_message(), msg.getUser_id(), MessageState.Sent.name(),
									Calendar.getInstance().getTimeInMillis()).setHandler(state -> {
										getGroupUsers(msg.getGroup_id()).setHandler(usersOfInt -> {
											if (usersOfInt.succeeded()) {
												FirebaseUtils.sendMessageToGroup(msg.getGroup_id(), usersOfInt.result(),
														msg.getUser_id(), msg).setHandler(send -> {

															List<User> usersOfInterest = usersOfInt.result().stream()
																	.filter(usr -> usr.getIduser()
																			.equals(StringResources.virtualSurgeryId)
																			|| usr.getIduser()
																					.equals(StringResources.virtualEndocrineId))
																	.collect(Collectors.toList());
															if (!usersOfInterest.isEmpty()) {
																List<String> responses = watsonQuestions
																		.getResponse(msg.getMessage());
																String watsonResponse = StringResources
																		.getRandomWatsonNoResponse();
																if (!responses.isEmpty()) {
																	watsonResponse = responses.get(0);
																}

																ChatMessage response = new ChatMessage(watsonResponse,
																		0, msg.getGroup_id(),
																		usersOfInterest.get(0).getIduser(),
																		Calendar.getInstance().getTimeInMillis());
																saveMessagesInDatabase(Arrays.asList(response))
																		.setHandler(saveResponse -> {
																			FirebaseUtils.sendMessageToGroup(
																					msg.getGroup_id(),
																					usersOfInt.result(),
																					response.getUser_id(), response);
																		});
															}
															self.handle(idx + 1);
														});
											}
										});
									});
						}
					}
				}.handle(0);

				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getGroupMessagesFromDate(RoutingContext routingContext) {
		Long timestamp = new Long(routingContext.request().getParam("timestamp"));
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupMessages(group_id, timestamp, Calendar.getInstance().getTimeInMillis(),
				StringResources.numMaxMessagesQuery).setHandler(res -> {
					if (res.succeeded()) {
						JsonArray array = new JsonArray(res.result());
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
					} else {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
										new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
					}
				});
	}

	private void getGroupMessagesUntilDate(RoutingContext routingContext) {
		Long timestamp = new Long(routingContext.request().getParam("timestamp"));
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupMessages(group_id, 0l, timestamp, StringResources.numMaxMessagesQuery).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getGroupMessagesFromToDate(RoutingContext routingContext) {
		Long fromtimestamp = new Long(routingContext.request().getParam("fromtimestamp"));
		Long totimestamp = new Long(routingContext.request().getParam("totimestamp"));
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupMessages(group_id, fromtimestamp, totimestamp, StringResources.numMaxMessagesQuery).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getGroupLastMessage(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupLastMessage(group_id).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUserGroups(RoutingContext routingContext) {
		Long user_id = new Long(routingContext.request().getParam("userId"));
		getUserGroups(user_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getRelatedUsers(RoutingContext routingContext) {
		Long user_id = new Long(routingContext.request().getParam("userId"));
		getRelatedUsers(user_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getGroupUsers(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupUsers(group_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUnreadMessages(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		Long user_id = new Long(routingContext.request().getParam("userId"));
		getUnreadMessages(user_id, group_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void addMultiUserGroup(RoutingContext routingContext) {
		try {
			JsonObject body = new JsonObject(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()));
			Long user_id = body.getLong("user_id");
			createGroup(user_id, "", ChatServerStrings.getGroupCreatedBy(user_id.toString()),
					"images/chat_group_icon.png").setHandler(res -> {
						if (res.succeeded()) {
							routingContext.response().setStatusCode(201)
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
						} else {
							routingContext.response()
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
											new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
						}
					});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.setStatusCode(500)
					.end(GenerateRsaKeyPair.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
		}
	}

	private void addSingleUserGroup(RoutingContext routingContext) {
		try {
			JsonObject body = new JsonObject(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()));
			Long user_id = body.getLong("user_id");
			Long friend_id = body.getLong("friend_id");
			createGroup(user_id, "",
					ChatServerStrings.getSingleUserGroupDescription(user_id.toString(), friend_id.toString()),
					"images/chat_group_icon.png").setHandler(res -> {
						if (res.succeeded()) {
							addUserToGroup(friend_id, res.result().getIdchat_group(),
									Calendar.getInstance().getTimeInMillis()).setHandler(result -> {
										if (result.succeeded()) {
											routingContext.response().setStatusCode(201)
													.putHeader("content-type",
															StringResources.restResponseHeaderContentType)
													.end(GenerateRsaKeyPair
															.encryptMsg(Json.encodePrettily(res.result())));
										} else {
											routingContext.response()
													.putHeader("content-type",
															StringResources.restResponseHeaderContentType)
													.setStatusCode(
															500)
													.end(GenerateRsaKeyPair.encryptMsg(
															new JsonObject().put("error", result.cause().getMessage())
																	.encodePrettily()));
										}
									});
						} else {
							routingContext.response()
									.putHeader("content-type", StringResources.restResponseHeaderContentType)
									.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
											new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
						}
					});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.setStatusCode(500)
					.end(GenerateRsaKeyPair.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
		}
	}

	private void deleteUserFromGroup(RoutingContext routingContext) {
		JsonObject body = new JsonObject(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()));
		Long user_id = body.getLong("user_id");
		Integer group_id = body.getInteger("group_id");
		deleteUserFromGroup(user_id, group_id).setHandler(res -> {
			if (res.succeeded()) {
				getUserById(user_id).setHandler(res2 -> {
					if (res2.succeeded()) {
						FirebaseUtils.sendMessageToGroup(group_id, "user_left_chat",
								ChatServerStrings.userLeftGroup(res2.result()));
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));

					} else {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
										new JsonObject().put("error", res2.cause().getMessage()).encodePrettily()));
					}
				});
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUserFirebaseToken(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getUserFirebaseToken(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveUserFirebaseToken(RoutingContext routingContext) {
		FirebaseEntity firebaseToken = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				FirebaseEntity.class);
		saveUserFirebaseToken(firebaseToken).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUser(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getUser(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveUser(RoutingContext routingContext) {
		User user = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()), User.class);
		saveUser(user).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUserData(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getUserData(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveUserData(RoutingContext routingContext) {
		UserData userData = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				UserData.class);
		saveUserData(userData).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void deleteUserData(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		deleteUserData(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUserAppointment(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getUserAppointment(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveUserAppointment(RoutingContext routingContext) {
		Appointment[] appointments = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				Appointment[].class);

		if (appointments.length > 0) {
			saveUserAppointment(Arrays.asList(appointments)).setHandler(res -> {
				if (res.succeeded()) {
					res.result().stream()
							.filter(elem -> elem != null && !elem.getCreatedBy().equals(0)
									&& (elem.getUserViewTimestamp() == null || elem.getUserViewTimestamp().equals(0L)))
							.forEach(elem -> {
								FirebaseUtils.sendMessageToUser(elem.getIduser(),
										StringResources.createdExternalAppointment, Json.encode(elem));
							});
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getPendingConsent(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getPendingConsent(userId, true).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getConsentMedicalGroup(RoutingContext routingContext) {
		Integer consent_id = new Integer(routingContext.request().getParam("consentId"));
		getConsentMedicalGroup(consent_id).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void postUploadSignature(RoutingContext routingContext) {
		MultiMap attributes = routingContext.request().formAttributes();
		Set<FileUpload> fileUploadSet = routingContext.fileUploads();
		Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
		try {
			vertx.fileSystem().mkdir(StringResources.consentSignatureFolder, h -> {
				try {
					while (fileUploadIterator.hasNext()) {
						FileUpload fileUpload = fileUploadIterator.next();
						Buffer buffer = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
						Consent consent = Json.decodeValue(attributes.get("info"), Consent.class);
						String name = StringResources.consentSignatureFolder + "/"
								+ StringResources.consentSignaturePrefix + consent.getUser_id().getIduser() + "-"
								+ consent.getSign_timestamp() + "-" + consent.getUdid() + "."
								+ StringResources.consentSignatureExtension;
						vertx.fileSystem().writeFile(name, buffer, res -> {
							if (res.succeeded()) {
								updateFilledConsent(consent).setHandler(event -> {
									if (event.succeeded()) {
										routingContext.response()
												.putHeader("content-type",
														StringResources.restResponseHeaderContentType)
												.end(GenerateRsaKeyPair.encryptMsg("{\"result\":\"OK\"}"));
									} else {
										routingContext.response()
												.putHeader("content-type",
														StringResources.restResponseHeaderContentType)
												.setStatusCode(
														500)
												.end(GenerateRsaKeyPair.encryptMsg(new JsonObject()
														.put("error", event.cause().getMessage()).encodePrettily()));
									}
								});
							} else {
								routingContext.response()
										.putHeader("content-type", StringResources.restResponseHeaderContentType)
										.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(new JsonObject()
												.put("error", res.cause().getMessage()).encodePrettily()));
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair
									.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.setStatusCode(500)
					.end(GenerateRsaKeyPair.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
		}
	}

	private void getImage(RoutingContext routingContext) {
		String imagePath = routingContext.request().getParam("imagePath");
		if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
			BufferedImage image = null;
			try {
				URL url = new URL(imagePath);
				image = ImageIO.read(url);
				String extension = url.toString().substring(url.toString().lastIndexOf(".") + 1);
				File file = File.createTempFile("temp-file-name" + Calendar.getInstance().getTimeInMillis(),
						"." + extension);
				ImageIO.write(image, extension, file);
				routingContext.response().sendFile(GenerateRsaKeyPair.encryptFile(file).getPath());
			} catch (IOException e) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentTypeImage)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
			}
		} else {
			routingContext.response().sendFile(GenerateRsaKeyPair.encryptFile(new File(imagePath)).getPath());
		}
	}

	private void getUserImage(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getUser(userId).setHandler(event -> {

			if (event.succeeded()) {
				User user = event.result();
				if (user.getImage().startsWith("http://") || user.getImage().startsWith("https://")) {
					BufferedImage image = null;
					try {
						URL url = new URL(user.getImage());
						image = ImageIO.read(url);
						String extension = url.toString().substring(url.toString().lastIndexOf(".") + 1);
						File file = File.createTempFile("temp-file-name" + Calendar.getInstance().getTimeInMillis(),
								"." + extension);
						ImageIO.write(image, extension, file);
						routingContext.response().sendFile(GenerateRsaKeyPair.encryptFile(file).getPath());
					} catch (IOException e) {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.setStatusCode(500).end(GenerateRsaKeyPair
										.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
					}
				} else {
					routingContext.response()
							.sendFile(GenerateRsaKeyPair.encryptFile(new File(user.getImage())).getPath());
				}
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
								new JsonObject().put("error", event.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void postUploadImage(RoutingContext routingContext) {
		MultiMap attributes = routingContext.request().formAttributes();
		Set<FileUpload> fileUploadSet = routingContext.fileUploads();
		Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
		try {
			vertx.fileSystem().mkdir(StringResources.consentUserImageFolder, h -> {
				try {
					while (fileUploadIterator.hasNext()) {
						FileUpload fileUpload = fileUploadIterator.next();
						Buffer buffer = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
						JsonObject json = new JsonObject(attributes.get("content"));
						String name = StringResources.consentUserImageFolder + "/" + json.getString("filename");
						Long userId = json.getLong("userId");
						vertx.fileSystem().writeFile(name, buffer, res -> {
							if (res.succeeded()) {
								saveUserImage(userId, name).setHandler(event -> {
									if (event.succeeded() && event.result() != null) {
										getUser(userId).setHandler(handler -> {
											if (handler.succeeded()) {
												routingContext.response()
														.putHeader("content-type",
																StringResources.restResponseHeaderContentType)
														.end(GenerateRsaKeyPair
																.encryptMsg(Json.encodePrettily(handler.result())));
											} else {
												routingContext.response()
														.putHeader("content-type",
																StringResources.restResponseHeaderContentType)
														.setStatusCode(500).end(
																GenerateRsaKeyPair
																		.encryptMsg(
																				new JsonObject()
																						.put("error",
																								handler.cause()
																										.getMessage())
																						.encodePrettily()));
											}
										});
									} else {
										routingContext.response()
												.putHeader("content-type",
														StringResources.restResponseHeaderContentType)
												.setStatusCode(500)
												.end(GenerateRsaKeyPair.encryptMsg(event.cause().getMessage()));
									}
								});

							} else {
								routingContext.response()
										.putHeader("content-type", StringResources.restResponseHeaderContentType)
										.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(new JsonObject()
												.put("error", res.cause().getMessage()).encodePrettily()));
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair
									.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.setStatusCode(500)
					.end(GenerateRsaKeyPair.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
		}
	}

	private void getMedicalTest(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getMedicalTest(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveMedicalTest(RoutingContext routingContext) {
		MedicalTestEntity[] medicalTests = Json.decodeValue(
				GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()), MedicalTestEntity[].class);

		if (medicalTests.length > 0) {
			saveMedicalTest(Arrays.asList(medicalTests)).setHandler(res -> {
				if (res.succeeded()) {
					res.result().stream()
							.filter(elem -> elem != null && !elem.getCreatedBy().equals(0)
									&& (elem.getUserViewTimestamp() == null || elem.getUserViewTimestamp().equals(0L)))
							.forEach(elem -> {
								FirebaseUtils.sendMessageToUser(elem.getIduser(),
										StringResources.createdExternalMedicalTest, Json.encode(elem));
							});
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void postMedicalTestImage(RoutingContext routingContext) {
		MultiMap attributes = routingContext.request().formAttributes();
		Set<FileUpload> fileUploadSet = routingContext.fileUploads();
		Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
		try {
			vertx.fileSystem().mkdir(StringResources.consentMedicalTestFolder, h -> {
				try {
					while (fileUploadIterator.hasNext()) {
						FileUpload fileUpload = fileUploadIterator.next();
						Buffer buffer = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
						MedicalTestEntity medicalTest = Json.decodeValue(attributes.get("test"),
								MedicalTestEntity.class);
						String name = StringResources.consentMedicalTestFolder + "/"
								+ StringResources.consentMedicalTestPrefix + medicalTest.getIdMedicalTest() + "-"
								+ medicalTest.getTimestamp() + "." + StringResources.consentMedicalTestExtension;
						vertx.fileSystem().writeFile(name, buffer, res -> {
							if (res.succeeded()) {
								medicalTest.setPicturePath(name);
								saveMedicalTest(Arrays.asList(medicalTest)).setHandler(event -> {
									if (event.succeeded()) {
										routingContext.response()
												.putHeader("content-type",
														StringResources.restResponseHeaderContentType)
												.end(GenerateRsaKeyPair
														.encryptMsg(Json.encodePrettily(event.result().get(0))));
									} else {
										routingContext.response()
												.putHeader("content-type",
														StringResources.restResponseHeaderContentType)
												.setStatusCode(
														500)
												.end(GenerateRsaKeyPair.encryptMsg(new JsonObject()
														.put("error", event.cause().getMessage()).encodePrettily()));
									}
								});
							} else {
								routingContext.response()
										.putHeader("content-type", StringResources.restResponseHeaderContentType)
										.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(new JsonObject()
												.put("error", res.cause().getMessage()).encodePrettily()));
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair
									.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.setStatusCode(500)
					.end(GenerateRsaKeyPair.encryptMsg(new JsonObject().put("error", e.getMessage()).encodePrettily()));
		}
	}

	private void getMedicinesByUser(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getUserMedicine(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getMedicineTakenByUser(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getUserMedicineTaken(userId).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveMedicineTaken(RoutingContext routingContext) {
		MedicineTaken[] medicineTaken = Json
				.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()), MedicineTaken[].class);

		if (medicineTaken.length > 0) {
			saveUserMedicineTaken(Arrays.asList(medicineTaken)).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void saveMedicine(RoutingContext routingContext) {
		Medicine[] medicine = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				Medicine[].class);

		if (medicine.length > 0) {
			saveUserMedicine(Arrays.asList(medicine)).setHandler(res -> {
				if (res.succeeded()) {
					res.result().stream()
							.filter(elem -> elem != null && !elem.getCreatedBy().equals(0)
									&& (elem.getUserViewTimestamp() == null || elem.getUserViewTimestamp().equals(0L)))
							.forEach(elem -> {
								FirebaseUtils.sendMessageToUser(elem.getIduser(),
										StringResources.createdExternalMedicine, Json.encode(elem));
							});
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getAimsByUser(RoutingContext routingContext) {
		Long user_id = new Long(routingContext.request().getParam("userId"));
		getAimsByUser(user_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getPsychologyTest(RoutingContext routingContext) {
		// Integer userId = new
		// Integer(routingContext.request().getParam("userId"));
		routingContext.response().putHeader("content-type", "file").sendFile("testpsicologico.json");
	}

	private void getDrugList(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "file").sendFile("drug_list.txt");
	}

	private void getSurgeryInfoList(RoutingContext routingContext) {
		// Integer userId = new
		// Integer(routingContext.request().getParam("userId"));
		routingContext.response().putHeader("content-type", "file").sendFile("surgery_info_list.json");
	}

	private void saveTestResult(RoutingContext routingContext) {
		TestResponse testResponse = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				TestResponse.class);
		addTestResponse(testResponse).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveTestResults(RoutingContext routingContext) {
		JsonArray jsonArray = new JsonArray(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()));
		List<TestResponse> responses = new ArrayList<>();
		jsonArray.iterator().forEachRemaining(c -> responses.add(Json.decodeValue(c.toString(), TestResponse.class)));
		addTestResponses(responses).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getTestResults(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getTestResults(userId).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(array.encodePrettily()));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getUserById(RoutingContext routingContext) {
		Long user_id = new Long(routingContext.request().getParam("idUser"));
		getUserById(user_id).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getDoctor(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		getUserDoctors(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void requestDoctorPatient(RoutingContext routingContext) {
		Long idDoctor = new Long(routingContext.request().getParam("idDoctor"));
		Long idPatient = new Long(routingContext.request().getParam("idPatient"));
		getUser(idPatient).setHandler(patient -> {
			getDoctor(idDoctor).setHandler(doctor -> {
				MailMessage message = new MailMessage();
				message.setFrom("lsoria@preobar.com");
				message.setTo(doctor.result().get(0).getEmail());
				message.setCc("lsoria@preobar.com");
				message.setSubject(StringResources.validationNewPatientSubject);
				String validationLink = serverAddress + "/api/obesity/doctor/validate/" + idDoctor + "/" + idPatient;
				message.setHtml(String.format(StringResources.validationNewPatientBody, patient.result().getName(), patient.result().getSurname(), validationLink));
				mailClient.sendMail(message, result -> {
					if (result.succeeded()) {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(result.result())));
					} else {
						routingContext.response()
								.putHeader("content-type", StringResources.restResponseHeaderContentType)
								.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
										new JsonObject().put("error", result.cause().getMessage()).encodePrettily()));
					}
				});
			});
		});

	}

	private void vaildateDoctorPatient(RoutingContext routingContext) {
		Long idDoctor = new Long(routingContext.request().getParam("idDoctor"));
		Long idPatient = new Long(routingContext.request().getParam("idPatient"));
		validateDoctorPatient(idDoctor, idPatient).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void getHealthWeight(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		Long startTime = new Long(routingContext.request().getParam("startTime"));
		Long endTime = new Long(routingContext.request().getParam("endTime"));
		getHealthWeight(userId, lastUpdateTime, startTime, endTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveHealthWeight(RoutingContext routingContext) {
		Weight[] weights = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				Weight[].class);

		if (weights.length > 0) {
			saveHealthWeight(Arrays.asList(weights)).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getBloodPressure(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		Long startTime = new Long(routingContext.request().getParam("startTime"));
		Long endTime = new Long(routingContext.request().getParam("endTime"));
		getBloodPressure(userId, lastUpdateTime, startTime, endTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveBloodPressure(RoutingContext routingContext) {
		BloodPressure[] bloodPressure = Json
				.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()), BloodPressure[].class);

		if (bloodPressure.length > 0) {
			saveBloodPressure(Arrays.asList(bloodPressure)).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getBloodGlucose(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		Long startTime = new Long(routingContext.request().getParam("startTime"));
		Long endTime = new Long(routingContext.request().getParam("endTime"));
		getBloodGlucose(userId, lastUpdateTime, startTime, endTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveBloodGlucose(RoutingContext routingContext) {
		BloodGlucose[] bloodGlucose = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				BloodGlucose[].class);

		if (bloodGlucose.length > 0) {
			saveBloodGlucose(Arrays.asList(bloodGlucose)).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getHeartRate(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		Long startTime = new Long(routingContext.request().getParam("startTime"));
		Long endTime = new Long(routingContext.request().getParam("endTime"));
		getHeartRate(userId, lastUpdateTime, startTime, endTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveHeartRate(RoutingContext routingContext) {
		HeartRate[] heartRate = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				HeartRate[].class);

		if (heartRate.length > 0) {
			saveHeartRate(Arrays.asList(heartRate)).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getChallenge(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getChallenge(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveChallenge(RoutingContext routingContext) {
		ChallengeEntity[] challenges = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				ChallengeEntity[].class);

		if (challenges.length > 0) {
			saveChallenge(Arrays.asList(challenges)).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	private void getActivitySummary(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Long startTime = new Long(routingContext.request().getParam("startTime"));
		Long endTime = new Long(routingContext.request().getParam("endTime"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getActivitySummary(userId, lastUpdateTime, startTime, endTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
			} else {
				routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
						.setStatusCode(500).end(GenerateRsaKeyPair
								.encryptMsg(new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
			}
		});
	}

	private void saveActivitySummary(RoutingContext routingContext) {
		Long userId = new Long(routingContext.request().getParam("userId"));
		Summary[] summaries = Json.decodeValue(GenerateRsaKeyPair.decryptMsg(routingContext.getBodyAsString()),
				Summary[].class);

		if (summaries.length > 0) {
			saveActivitySummary(Arrays.asList(summaries), userId).setHandler(res -> {
				if (res.succeeded()) {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(res.result())));
				} else {
					routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
							.setStatusCode(500).end(GenerateRsaKeyPair.encryptMsg(
									new JsonObject().put("error", res.cause().getMessage()).encodePrettily()));
				}
			});
		} else {
			routingContext.response().putHeader("content-type", StringResources.restResponseHeaderContentType)
					.end(GenerateRsaKeyPair.encryptMsg(Json.encodePrettily(new ArrayList<>())));
		}
	}

	// ------------------------ FUTURES ------------------------

	private Future<Consent> getPendingConsent(Long userId, boolean pending) {
		Future<Consent> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT consent.*, us.* FROM users AS us INNER JOIN retoobesidad.consent "
							+ "WHERE consent.user_id = ? AND us.iduser = consent.user_id";
					if (pending)
						select += " AND sign_timestamp is null";
					select += " ORDER BY timestamp;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res -> {
						conn.result().close();
						if (res.succeeded()) {
							if (res.result().getNumRows() > 0) {
								JsonObject consent_user = res.result().getRows().get(0);
								JsonObject consent_clean = res.result().getRows().get(0);
								consent_clean.remove("user_id");
								consent_clean.remove("medical_team");
								Consent consent = Json.decodeValue(consent_clean.encode(), Consent.class);
								User user = Json.decodeValue(consent_user.encode(), User.class);
								consent.setUser_id(user);
								future.complete(consent);
							} else {
								future.complete(null);
							}
						} else {
							future.fail(res.cause() != null ? res.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<User>> getConsentMedicalGroup(Integer consent_id) {
		Future<List<User>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM users AS us INNER JOIN retoobesidad.consent_team AS team "
							+ "WHERE team.id_consent = ? AND us.iduser = team.id_user;";
					conn.result().queryWithParams(select, new JsonArray().add(consent_id), res -> {
						conn.result().close();
						if (res.succeeded()) {
							future.complete(res.result().getRows().stream()
									.map(user -> Json.decodeValue(user.encode(), User.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res.cause() != null ? res.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<Consent> updateFilledConsent(Consent consent) {
		Future<Consent> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"UPDATE retoobesidad.consent SET sign_timestamp = ?, " + "acciones_oportunas = ?, "
									+ "muestras_biologicas = ?, " + "muestras_investigacion = ?, " + "imagenes = ?, "
									+ "nombre = ?, " + "apellidos = ?, " + "DNI = ?, " + "udid = ? "
									+ "WHERE idconsent = ?;",
							new JsonArray().add(consent.getSign_timestamp()).add(consent.getAcciones_oportunas())
									.add(consent.getMuestras_biologicas()).add(consent.getMuestras_investigacion())
									.add(consent.getImagenes()).add(consent.getNombre()).add(consent.getApellidos())
									.add(consent.getDni()).add(consent.getUdid()).add(consent.getIdconsent()),
							res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(consent);
								} else {
									future.fail(res.cause() != null ? res.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<ChatMessage>> saveMessagesInDatabase(List<ChatMessage> messages) {
		Future<List<ChatMessage>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				List<ChatMessage> results = new ArrayList<>();
				new Handler<Integer>() {
					@Override
					public void handle(Integer idx) {
						if (idx < messages.size()) {
							final Handler<Integer> self = this;
							ChatMessage message = messages.get(idx);
							conn.result().updateWithParams(
									"INSERT INTO retoobesidad.chat_group_messages (message,group_id,user_id,timestamp) VALUES (?,?,?,?)",
									new JsonArray().add(message.getMessage()).add(message.getGroup_id())
											.add(message.getUser_id()).add(message.getTimestamp()),
									res2 -> {
										if (res2.succeeded()) {
											message.setId_message(res2.result().getKeys().getInteger(0));
											results.add(message);
										}
										self.handle(idx + 1);
									});
						} else {
							conn.result().close();
							future.complete(results);
						}
					}
				}.handle(0);
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private static Future<List<ChatMessage>> getUnreadMessages(Long user_id, Integer group_id) {
		Future<List<ChatMessage>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM retoobesidad.chat_group_messages AS group_msg"
							+ " WHERE group_msg.group_id = ? AND group_msg.user_id != ? AND NOT EXISTS ("
							+ "   SELECT state_msg.message_id FROM retoobesidad.chat_message_state AS state_msg "
							+ "	  WHERE group_msg.id_message = state_msg.message_id"
							+ "		AND state_msg.user_id = ?" + "		AND state_msg.state LIKE 'Read'"
							+ " ) LIMIT 200;", new JsonArray().add(group_id).add(user_id).add(user_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(res.result().getRows().stream()
											.map(msg -> Json.decodeValue(msg.encode(), ChatMessage.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<List<ChatMessage>> getGroupMessages(Integer group_id, Long fromtimestamp, final Long totimestamp,
			int limit) {
		Future<List<ChatMessage>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.chat_group_messages WHERE group_id = ? AND timestamp > ? AND timestamp <= ? ORDER BY timestamp DESC";
					if (fromtimestamp == 0l) {
						select += " LIMIT " + limit;
					}
					select += ";";
					conn.result().queryWithParams(select,
							new JsonArray().add(group_id).add(fromtimestamp).add(totimestamp), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(res.result().getRows().stream()
											.map(msg -> Json.decodeValue(msg.encode(), ChatMessage.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<ChatMessage> getGroupLastMessage(Integer group_id) {
		Future<ChatMessage> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"SELECT * FROM retoobesidad.chat_group_messages WHERE group_id = ? ORDER BY timestamp DESC LIMIT 1;",
							new JsonArray().add(group_id), res -> {
								conn.result().close();
								if (res.succeeded() && res.result().getNumRows() > 0) {
									future.complete(Json.decodeValue(res.result().getRows().get(0).encode(),
											ChatMessage.class));
								} else {
									future.fail(res.cause() != null ? res.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<ChatMessageState> addMessageState(Integer message_id, Long user_id, String state, Long timestamp) {
		Future<ChatMessageState> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.chat_message_state (message_id, user_id, state, timestamp) VALUES (?,?,?,?);",
							new JsonArray().add(message_id).add(user_id).add(state).add(timestamp), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(new ChatMessageState(res.result().getKeys().getInteger(0),
											message_id, user_id, state, timestamp));
								} else {
									future.fail(res.cause() != null ? res.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<Integer>> addMessageState(Long userId, List<Integer> messages, String state, Long timestamp) {
		Future<List<Integer>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				List<Integer> responses = new ArrayList<>();
				new Handler<Integer>() {
					@Override
					public void handle(Integer idx) {
						if (idx < messages.size()) {
							final Handler<Integer> self = this;
							Integer message = messages.get(idx);
							conn.result().updateWithParams(
									"INSERT INTO retoobesidad.chat_message_state (message_id, user_id, state, timestamp) VALUES (?,?,?,?);",
									new JsonArray().add(message).add(userId).add(state).add(timestamp), res -> {
										if (res.succeeded()) {
											responses.add(message);
										}
										self.handle(idx + 1);
									});
						} else {
							conn.result().close();
							future.complete(responses);
						}
					}
				}.handle(0);
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<TestResponse> addTestResponse(TestResponse testResponse) {
		Future<TestResponse> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.test_user_response (user_id, test_id, question_number, response, timestamp) VALUES (?,?,?,?,?) "
									+ "ON DUPLICATE KEY UPDATE response = ?, timestamp = ?;",
							new JsonArray().add(testResponse.getUser_id()).add(testResponse.getTest_id())
									.add(testResponse.getQuestion_number()).add(testResponse.getResponse())
									.add(testResponse.getTimestamp()).add(testResponse.getResponse())
									.add(testResponse.getTimestamp()),
							res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(testResponse);
								} else {
									future.fail(res.cause() != null ? res.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<TestResponse>> addTestResponses(List<TestResponse> testResponses) {
		Future<List<TestResponse>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				List<TestResponse> responses = new ArrayList<>();
				new Handler<Integer>() {
					@Override
					public void handle(Integer idx) {
						if (idx < testResponses.size()) {
							final Handler<Integer> self = this;
							TestResponse testResponse = testResponses.get(idx);
							conn.result().updateWithParams(
									"INSERT INTO retoobesidad.test_user_response (user_id, test_id, question_number, response, timestamp) VALUES (?,?,?,?,?) "
											+ "ON DUPLICATE KEY UPDATE response = ?, timestamp = ?;",
									new JsonArray().add(testResponse.getUser_id()).add(testResponse.getTest_id())
											.add(testResponse.getQuestion_number()).add(testResponse.getResponse())
											.add(testResponse.getTimestamp()).add(testResponse.getResponse())
											.add(testResponse.getTimestamp()),
									res2 -> {
										if (res2.succeeded()) {
											responses.add(testResponse);
										}
										self.handle(idx + 1);
									});
						} else {
							conn.result().close();
							future.complete(responses);
						}
					}
				}.handle(0);
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	public static Future<FirebaseEntity> getUserFirebaseToken(Long userId) {
		Future<FirebaseEntity> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.firebase_tokens WHERE iduser = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(
									Json.decodeValue(res2.result().getRows().get(0).encode(), FirebaseEntity.class));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private static Future<FirebaseEntity> saveUserFirebaseToken(FirebaseEntity firebase) {
		Future<FirebaseEntity> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.firebase_tokens (iduser,firebase_token,timestamp) "
									+ "VALUES (?,?,?) " + "ON DUPLICATE KEY UPDATE "
									+ "firebase_token = ?, timestamp = ?",
							new JsonArray().add(firebase.getIduser()).add(firebase.getFirebase_token())
									.add(firebase.getTimestamp()).add(firebase.getFirebase_token())
									.add(firebase.getTimestamp()),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(firebase);
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<User> getUser(Long userId) {
		Future<User> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.users WHERE iduser = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(Json.decodeValue(res2.result().getRows().get(0).encode(), User.class));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<Long> saveUserImage(Long userId, String image) {
		Future<Long> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams("UPDATE retoobesidad.users SET image = ? WHERE iduser = ?;",
							new JsonArray().add(image).add(userId), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(userId);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<User> saveUser(User user) {
		Future<User> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.users (iduser,name,surname,nickname,passhash,email,image) "
									+ "VALUES (?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
									+ "name = ?, surname = ?, nickname = ?, passhash = ?, email = ?, " + "image = ?",
							new JsonArray().add(user.getIduser()).add(user.getName()).add(user.getSurname())
									.add(user.getNickname()).add("").add(user.getEmail()).add(user.getImage())
									.add(user.getName()).add(user.getSurname()).add(user.getNickname()).add("")
									.add(user.getEmail()).add(user.getImage()),
							res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getUpdated() == 1) {
									createGroup(user.getIduser(), StringResources.virtualSurgeryName,
											StringResources.virtualSurgeryChatDescription,
											"https://www.iconexperience.com/_img/v_collection_png/512x512/shadow/surgeon.png")
													.setHandler(new Handler<AsyncResult<ChatGroup>>() {

														@Override
														public void handle(AsyncResult<ChatGroup> event) {
															addUserToGroup(StringResources.virtualSurgeryId,
																	event.result().getIdchat_group(),
																	Calendar.getInstance().getTimeInMillis());
														}
													});
									createGroup(user.getIduser(), StringResources.virtualEndocrineName,
											StringResources.virtualEndocrineChatDescription,
											"http://icons.iconarchive.com/icons/icons-land/vista-people/128/Medical-Nurse-Male-Light-icon.png")
													.setHandler(new Handler<AsyncResult<ChatGroup>>() {

														@Override
														public void handle(AsyncResult<ChatGroup> event) {
															addUserToGroup(StringResources.virtualEndocrineId,
																	event.result().getIdchat_group(),
																	Calendar.getInstance().getTimeInMillis());
														}
													});
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(),
											StringResources.defaultAimActiveMinutes,
											Aim.AimType.activeMinutes.ordinal()));
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(),
											StringResources.defaultAimCaloriesOut, Aim.AimType.caloriesOut.ordinal()));
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(),
											StringResources.defaultAimDistance, Aim.AimType.distance.ordinal()));
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(),
											StringResources.defaultAimSteps, Aim.AimType.steps.ordinal()));
								}

								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									future.complete(user);
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<List<Appointment>> getUserAppointment(Long userId, Long lastUpdateTimestamp) {
		Future<List<Appointment>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.appointments WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result().getRows().stream().map(
											appointment -> Json.decodeValue(appointment.encode(), Appointment.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<Appointment>> saveUserAppointment(List<Appointment> appointments) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < appointments.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<Appointment>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < appointments.size()) {
								Appointment appointment = appointments.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.appointments (idappointment, timestamp,doctor,description,place,things,"
												+ "iduser,type,status,createdBy,userViewTimestamp,lastUpdateTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "timestamp = IF (lastUpdateTimestamp < ?,?,timestamp),"
												+ "doctor = IF (lastUpdateTimestamp < ?,?, doctor),"
												+ "description = IF (lastUpdateTimestamp < ?,?, description),"
												+ "place = IF (lastUpdateTimestamp < ?,?, place),"
												+ "things = IF (lastUpdateTimestamp < ?,?, things),"
												+ "iduser = IF (lastUpdateTimestamp < ?,?, iduser),"
												+ "type = IF (lastUpdateTimestamp < ?,?, type),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "createdBy = IF (lastUpdateTimestamp < ?,?, createdBy),"
												+ "userViewTimestamp = IF (lastUpdateTimestamp < ?,?, userViewTimestamp),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(appointment.getIdAppointment())
												.add(appointment.getTimestamp()).add(appointment.getDoctor())
												.add(appointment.getDescription()).add(appointment.getPlace())
												.add(appointment.getThings()).add(appointment.getIduser())
												.add(appointment.getType()).add(appointment.getStatus())
												.add(appointment.getCreatedBy()).add(appointment.getUserViewTimestamp())
												.add(appointment.getLastUpdateTimestamp())
												.add(appointment.getLastUpdateTimestamp())
												.add(appointment.getTimestamp())
												.add(appointment.getLastUpdateTimestamp()).add(appointment.getDoctor())
												.add(appointment.getLastUpdateTimestamp())
												.add(appointment.getDescription())
												.add(appointment.getLastUpdateTimestamp()).add(appointment.getPlace())
												.add(appointment.getLastUpdateTimestamp()).add(appointment.getThings())
												.add(appointment.getLastUpdateTimestamp()).add(appointment.getIduser())
												.add(appointment.getLastUpdateTimestamp()).add(appointment.getType())
												.add(appointment.getLastUpdateTimestamp()).add(appointment.getStatus())
												.add(appointment.getLastUpdateTimestamp())
												.add(appointment.getCreatedBy())
												.add(appointment.getLastUpdateTimestamp())
												.add(appointment.getUserViewTimestamp())
												.add(appointment.getLastUpdateTimestamp())
												.add(appointment.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0) {
													appointments.get(idx)
															.setIdAppointment(res2.result().getKeys().getInteger(0));
												}
												futures.get(idx).complete();
											} else {
												futures.get(idx).fail(res2.cause());
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(appointments);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<UserData> getUserData(Long userId) {
		Future<UserData> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.user_data WHERE user_id = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(Json.decodeValue(res2.result().getRows().get(0).encode(), UserData.class));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<UserData> saveUserData(UserData userData) {
		Future<UserData> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.user_data (user_id,nacimiento,altura,peso,hipertension,"
									+ "diabetes,apnea,lesion_articular,hiperlipidemia,vesicula,higado,osteoporosis,cardiaca,ejercicio,fecha_intervencion,fecha_primer_uso_app,peso_objetivo) "
									+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
									+ "ON DUPLICATE KEY UPDATE nacimiento = ?, "
									+ "altura = ?, peso = ?, hipertension = ?, diabetes = ?, apnea = ?, "
									+ "lesion_articular = ?, hiperlipidemia = ?, vesicula = ?, higado = ?, osteoporosis = ?, cardiaca = ?, ejercicio = ?, "
									+ "fecha_intervencion = ?, fecha_primer_uso_app = ?, peso_objetivo = ?",
							new JsonArray().add(userData.getUser_id()).add(userData.getNacimiento())
									.add(userData.getAltura()).add(userData.getPeso()).add(userData.getHipertension())
									.add(userData.getDiabetes()).add(userData.getApnea())
									.add(userData.getLesion_articular()).add(userData.getHiperlipidemia())
									.add(userData.getVesicula()).add(userData.getHigado())
									.add(userData.getOsteoporosis()).add(userData.getCardiaca())
									.add(userData.getEjercicio()).add(userData.getFecha_intervencion())
									.add(userData.getFecha_primer_uso_app()).add(userData.getPeso_objetivo())
									.add(userData.getNacimiento()).add(userData.getAltura()).add(userData.getPeso())
									.add(userData.getHipertension()).add(userData.getDiabetes())
									.add(userData.getApnea()).add(userData.getLesion_articular())
									.add(userData.getHiperlipidemia()).add(userData.getVesicula())
									.add(userData.getHigado()).add(userData.getOsteoporosis())
									.add(userData.getCardiaca()).add(userData.getEjercicio())
									.add(userData.getFecha_intervencion()).add(userData.getFecha_primer_uso_app())
									.add(userData.getPeso_objetivo()),
							res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									userData.setIduser_data(res2.result().getKeys().getInteger(0));
									future.complete(userData);
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<Boolean> deleteUserData(Long userId) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams("DELETE FROM retoobesidad.user_data WHERE user_id = ?",
							new JsonArray().add(userId), res -> {
								conn.result().close();
								if (res.succeeded()) {
									if (res.result().getUpdated() > 0)
										future.complete(true);
									else
										future.complete(false);
								} else {
									future.fail(res.cause() != null ? res.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<List<Medicine>> getUserMedicine(Long userId, Long lastUpdateTimestamp) {
		Future<List<Medicine>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicine WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY begin_timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result().getRows().stream()
											.map(medicine -> Json.decodeValue(medicine.encode(), Medicine.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<Medicine>> saveUserMedicine(List<Medicine> medicines) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < medicines.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<Medicine>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < medicines.size()) {
								Medicine medicine = medicines.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.medicine (iduser,medicine,observations,"
												+ "begin_timestamp,end_timestamp,method,dosage,days,idmedicine,lastUpdateTimestamp,status,createdBy,userViewTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "medicine = IF (lastUpdateTimestamp < ?,?, medicine),"
												+ "observations = IF (lastUpdateTimestamp < ?,?, observations),"
												+ "begin_timestamp = IF (lastUpdateTimestamp < ?,?, begin_timestamp),"
												+ "end_timestamp = IF (lastUpdateTimestamp < ?,?, end_timestamp),"
												+ "method = IF (lastUpdateTimestamp < ?,?, method),"
												+ "dosage = IF (lastUpdateTimestamp < ?,?, dosage),"
												+ "days = IF (lastUpdateTimestamp < ?,?, days),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "createdBy = IF (lastUpdateTimestamp < ?,?, createdBy),"
												+ "userViewTimestamp = IF (lastUpdateTimestamp < ?,?, userViewTimestamp),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?, lastUpdateTimestamp);",
										new JsonArray().add(medicine.getIduser()).add(medicine.getMedicine())
												.add(medicine.getObservations()).add(medicine.getBeginTimestamp())
												.add(medicine.getEndTimestamp()).add(medicine.getMethod())
												.add(medicine.getDosage()).add(medicine.getDays())
												.add(medicine.getIdmedicine()).add(medicine.getLastUpdateTimestamp())
												.add(medicine.getStatus()).add(medicine.getCreatedBy())
												.add(medicine.getUserViewTimestamp())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getIduser())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getMedicine())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getObservations())
												.add(medicine.getLastUpdateTimestamp())
												.add(medicine.getBeginTimestamp())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getEndTimestamp())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getMethod())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getDosage())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getDays())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getStatus())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getCreatedBy())
												.add(medicine.getLastUpdateTimestamp())
												.add(medicine.getUserViewTimestamp())
												.add(medicine.getLastUpdateTimestamp())
												.add(medicine.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													medicines.get(idx)
															.setIdmedicine(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx).fail(res2.cause());
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(medicines);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<MedicineTaken>> getUserMedicineTaken(Long userId) {
		Future<List<MedicineTaken>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicine_taken WHERE iduser = ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(res2.result().getRows().stream()
									.map(medicine -> Json.decodeValue(medicine.encode(), MedicineTaken.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<MedicineTaken>> saveUserMedicineTaken(List<MedicineTaken> medicines) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < medicines.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<MedicineTaken>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < medicines.size()) {
								MedicineTaken medicine = medicines.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.medicine_taken (idmedicine_taken,idUser,timestamp,medicineId) "
												+ "VALUES (?,?,?,?) " + "ON DUPLICATE KEY UPDATE " + "idUser = ? ,"
												+ "timestamp = ?," + "medicineId = ?;",
										new JsonArray().add(medicine.getIdmedicine_taken()).add(medicine.getIdUser())
												.add(medicine.getTimestamp()).add(medicine.getMedicineId())
												.add(medicine.getIdUser()).add(medicine.getTimestamp())
												.add(medicine.getMedicineId()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													medicines.get(idx)
															.setIdmedicine_taken(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx).fail(res2.cause());
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(medicines);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<Aim>> getAimsByUser(Long userId) {
		Future<List<Aim>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.user_aim WHERE iduser = ? ORDER BY timestamp;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(res2.result().getRows().stream()
									.map(aim -> Json.decodeValue(aim.encode(), Aim.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<Aim> saveAim(Aim aim) {
		Future<Aim> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO `retoobesidad`.`user_aim`(`iduser`,`timestamp`,`value`,`type`) VALUES(?,?,?,?)",
							new JsonArray().add(aim.getIduser()).add(aim.getTimestamp()).add(aim.getValue())
									.add(aim.getType()),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									aim.setIdAim(res2.result().getKeys().getInteger(0));
									future.complete(aim);
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<List<TestResponse>> getTestResults(Long userId) {
		Future<List<TestResponse>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.test_user_response WHERE user_id = ? ORDER BY timestamp;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(res2.result().getRows().stream()
									.map(response -> Json.decodeValue(response.encode(), TestResponse.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<ChatGroup> createGroup(Long id_user, String name, String description, String image) {
		Future<ChatGroup> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					Long creation_date = Calendar.getInstance().getTimeInMillis();
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.chat_groups (name,creation_date,description,dialogPhoto) VALUES (?,?,?)",
							new JsonArray().add(name).add(creation_date).add(description).add(image), res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									int id_group = res2.result().getKeys().getInteger(0);
									addUserToGroup(id_user, id_group, Calendar.getInstance().getTimeInMillis())
											.setHandler(result -> {
												if (result.succeeded()) {
													ChatGroup group = new ChatGroup(id_group, "", creation_date, "",
															"images/chat_group_icon.png");
													future.complete(group);
												} else {
													future.fail(result.cause());
												}
											});
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<List<ChatGroup>> getUserGroups(Long user_id) {
		Future<List<ChatGroup>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"SELECT groups.* FROM retoobesidad.chat_groups AS groups INNER JOIN retoobesidad.chat_group_members AS members "
									+ "WHERE members.group_id = groups.idchat_group AND members.user_id =  ?;",
							new JsonArray().add(user_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(res.result().getRows().stream()
											.map(group -> Json.decodeValue(group.encode(), ChatGroup.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	public static Future<List<User>> getGroupUsers(Integer group_id) {
		Future<List<User>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"SELECT * FROM users AS us INNER JOIN retoobesidad.chat_group_members AS chat "
									+ "WHERE chat.group_id = ? AND us.iduser = chat.user_id;",
							new JsonArray().add(group_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(res.result().getRows().stream()
											.map(user -> Json.decodeValue(user.encode(), User.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					conn.result().close();
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<List<User>> getRelatedUsers(Long user_id) {
		Future<List<User>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"SELECT * FROM retoobesidad.users AS users " + " WHERE users.iduser IN ("
									+ "   SELECT DISTINCT groups.user_id FROM ("
									+ "     SELECT members2.group_id FROM retoobesidad.chat_group_members AS members2"
									+ "     WHERE members2.user_id = ?) AS user_groups"
									+ " INNER JOIN retoobesidad.chat_group_members AS groups ON user_groups.group_id = groups.group_id)",
							new JsonArray().add(user_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(res.result().getRows().stream()
											.map(user -> Json.decodeValue(user.encode(), User.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<Boolean> addUserToGroup(Long user_id, Integer group_id, Long timestamp) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"INSERT INTO retoobesidad.chat_group_members (user_id,group_id,timestamp) VALUES (?,?,?)",
							new JsonArray().add(user_id).add(group_id).add(timestamp), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(true);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private Future<Boolean> deleteUserFromGroup(Long user_id, Integer group_id) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"DELETE FROM retoobesidad.chat_group_members WHERE user_id = ? AND group_id = ?",
							new JsonArray().add(user_id).add(group_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(res.result().getUpdated() > 0);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});
		return future;
	}

	private static Future<User> getUserById(Long user_id) {
		Future<User> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				conn.result().queryWithParams("SELECT * FROM retoobesidad.users WHERE iduser = ?;",
						new JsonArray().add(user_id), res -> {
							conn.result().close();
							if (res.succeeded()) {
								if (!res.result().getRows().isEmpty()) {
									future.complete(
											Json.decodeValue(Json.encode(res.result().getRows().get(0)), User.class));
								} else {
									future.fail("No users with this id");
								}
							} else {
								future.fail(res.cause() != null ? res.cause().getMessage() : "");
							}
						});
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<Weight>> getHealthWeight(Long userId, Long lastUpdateTimestamp, Long startTime, Long endTime) {
		Future<List<Weight>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.weight WHERE iduser = ? AND lastUpdateTimestamp >= ? AND timestamp > ? AND timestamp <= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select,
							new JsonArray().add(userId).add(lastUpdateTimestamp).add(startTime).add(endTime), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result().getRows().stream()
											.map(weight -> Json.decodeValue(weight.encode(), Weight.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<Weight>> saveHealthWeight(List<Weight> weights) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < weights.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<Weight>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < weights.size()) {
								Weight weight = weights.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.weight (idWeight,iduser,timestamp,"
												+ "status,datasource,value,fat,bmi,comments,lastUpdateTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "timestamp = IF (lastUpdateTimestamp < ?,?, timestamp),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "datasource = IF (lastUpdateTimestamp < ?,?, datasource),"
												+ "value = IF (lastUpdateTimestamp < ?,?, value),"
												+ "fat = IF (lastUpdateTimestamp < ?,?, fat),"
												+ "bmi = IF (lastUpdateTimestamp < ?,?, bmi),"
												+ "comments = IF (lastUpdateTimestamp < ?,?, comments),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(weight.getId()).add(weight.getIduser())
												.add(weight.getTimestamp()).add(weight.getStatus())
												.add(weight.getDatasource()).add(weight.getValue()).add(weight.getFat())
												.add(weight.getBmi()).add(weight.getComments())
												.add(weight.getLastUpdateTimestamp())
												.add(weight.getLastUpdateTimestamp()).add(weight.getIduser())
												.add(weight.getLastUpdateTimestamp()).add(weight.getTimestamp())
												.add(weight.getLastUpdateTimestamp()).add(weight.getStatus())
												.add(weight.getLastUpdateTimestamp()).add(weight.getDatasource())
												.add(weight.getLastUpdateTimestamp()).add(weight.getValue())
												.add(weight.getLastUpdateTimestamp()).add(weight.getFat())
												.add(weight.getLastUpdateTimestamp()).add(weight.getBmi())
												.add(weight.getLastUpdateTimestamp()).add(weight.getComments())
												.add(weight.getLastUpdateTimestamp())
												.add(weight.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													weights.get(idx).setIdWeight(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(weights);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<BloodPressure>> getBloodPressure(Long userId, Long lastUpdateTimestamp, Long startTime,
			Long endTime) {
		Future<List<BloodPressure>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.blood_pressure WHERE iduser = ? AND lastUpdateTimestamp >= ? AND timestamp > ? AND timestamp <= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select,
							new JsonArray().add(userId).add(lastUpdateTimestamp).add(startTime).add(endTime), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result()
											.getRows().stream().map(bloodPressure -> Json
													.decodeValue(bloodPressure.encode(), BloodPressure.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<BloodPressure>> saveBloodPressure(List<BloodPressure> bloodPressures) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < bloodPressures.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<BloodPressure>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < bloodPressures.size()) {
								BloodPressure pressure = bloodPressures.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.blood_pressure (`idBloodPressure`,`iduser`,`timestamp`,"
												+ "`status`,`datasource`,`maxValue`,`minValue`,`comments`,`lastUpdateTimestamp`) "
												+ "VALUES (?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "`iduser` = IF (`lastUpdateTimestamp` < ?,?,`iduser`),"
												+ "`timestamp` = IF (`lastUpdateTimestamp` < ?,?, `timestamp`),"
												+ "`status` = IF (`lastUpdateTimestamp` < ?,?, `status`),"
												+ "`datasource` = IF (`lastUpdateTimestamp` < ?,?, `datasource`),"
												+ "`maxValue` = IF (`lastUpdateTimestamp` < ?,?, `maxValue`),"
												+ "`minValue` = IF (`lastUpdateTimestamp` < ?,?, `minValue`),"
												+ "`comments` = IF (`lastUpdateTimestamp` < ?,?, `comments`),"
												+ "`lastUpdateTimestamp` = IF (`lastUpdateTimestamp` < ?,?,`lastUpdateTimestamp`);",
										new JsonArray().add(pressure.getId()).add(pressure.getIduser())
												.add(pressure.getTimestamp()).add(pressure.getStatus())
												.add(pressure.getDatasource()).add(pressure.getMaxValue())
												.add(pressure.getMinValue()).add(pressure.getComments())
												.add(pressure.getLastUpdateTimestamp())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getIduser())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getTimestamp())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getStatus())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getDatasource())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getMaxValue())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getMinValue())
												.add(pressure.getLastUpdateTimestamp()).add(pressure.getComments())
												.add(pressure.getLastUpdateTimestamp())
												.add(pressure.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													bloodPressures.get(idx)
															.setIdBloodPressure(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(bloodPressures);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<BloodGlucose>> getBloodGlucose(Long userId, Long lastUpdateTimestamp, Long startTime,
			Long endTime) {
		Future<List<BloodGlucose>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.blood_glucose WHERE iduser = ? AND lastUpdateTimestamp >= ? AND timestamp > ? AND timestamp <= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select,
							new JsonArray().add(userId).add(lastUpdateTimestamp).add(startTime).add(endTime), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result().getRows().stream().map(
											bloodGlucose -> Json.decodeValue(bloodGlucose.encode(), BloodGlucose.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<BloodGlucose>> saveBloodGlucose(List<BloodGlucose> bloodGlucoses) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < bloodGlucoses.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<BloodGlucose>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < bloodGlucoses.size()) {
								BloodGlucose glucose = bloodGlucoses.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.blood_glucose (idBloodGlucose,iduser,timestamp,"
												+ "status,datasource,value,comments,lastUpdateTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "timestamp = IF (lastUpdateTimestamp < ?,?, timestamp),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "datasource = IF (lastUpdateTimestamp < ?,?, datasource),"
												+ "value = IF (lastUpdateTimestamp < ?,?, value),"
												+ "comments = IF (lastUpdateTimestamp < ?,?, comments),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(glucose.getId()).add(glucose.getIduser())
												.add(glucose.getTimestamp()).add(glucose.getStatus())
												.add(glucose.getDatasource()).add(glucose.getValue())
												.add(glucose.getComments()).add(glucose.getLastUpdateTimestamp())
												.add(glucose.getLastUpdateTimestamp()).add(glucose.getIduser())
												.add(glucose.getLastUpdateTimestamp()).add(glucose.getTimestamp())
												.add(glucose.getLastUpdateTimestamp()).add(glucose.getStatus())
												.add(glucose.getLastUpdateTimestamp()).add(glucose.getDatasource())
												.add(glucose.getLastUpdateTimestamp()).add(glucose.getValue())
												.add(glucose.getLastUpdateTimestamp()).add(glucose.getComments())
												.add(glucose.getLastUpdateTimestamp())
												.add(glucose.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													bloodGlucoses.get(idx)
															.setIdBloodGlucose(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(bloodGlucoses);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<HeartRate>> getHeartRate(Long userId, Long lastUpdateTimestamp, Long startTime, Long endTime) {
		Future<List<HeartRate>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.heart_rate WHERE iduser = ? AND lastUpdateTimestamp >= ? AND timestamp > ? AND timestamp <= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select,
							new JsonArray().add(userId).add(lastUpdateTimestamp).add(startTime).add(endTime), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<HeartRate> heartRates = new ArrayList<HeartRate>();
									List<Future> futures = new ArrayList<Future>();
									for (int i = 0; i < res2.result().getRows().size(); i++) {
										final int i2 = i;
										futures.add(Future.future());
										HeartRate heartRateRes = Json
												.decodeValue(res2.result().getRows().get(i).encode(), HeartRate.class);
										getHeartRateZone(heartRateRes.getId()).setHandler(handler -> {
											HeartRateZone[] heartRateZones = new HeartRateZone[handler.result().size()];
											heartRateRes.setHeartRateZones(handler.result().toArray(heartRateZones));
											heartRates.add(heartRateRes);
											futures.get(i2).complete();
										});
									}
									CompositeFuture.all(futures).setHandler(handler -> {
										future.complete(heartRates);
									});
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<HeartRateZone>> getHeartRateZone(Integer idHeartRate) {
		Future<List<HeartRateZone>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.heart_rate_zone WHERE idHeartRate = ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(idHeartRate), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(res2.result().getRows().stream()
									.map(heartRateZone -> Json.decodeValue(heartRateZone.encode(), HeartRateZone.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<HeartRate>> saveHeartRate(List<HeartRate> heartRates) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < heartRates.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<HeartRate>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < heartRates.size()) {
								HeartRate heartRate = heartRates.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.heart_rate (idHeartRate,iduser,timestamp,"
												+ "status,datasource,value,comments,lastUpdateTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "timestamp = IF (lastUpdateTimestamp < ?,?, timestamp),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "datasource = IF (lastUpdateTimestamp < ?,?, datasource),"
												+ "value = IF (lastUpdateTimestamp < ?,?, value),"
												+ "comments = IF (lastUpdateTimestamp < ?,?, comments),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(heartRate.getId()).add(heartRate.getIduser())
												.add(heartRate.getTimestamp()).add(heartRate.getStatus())
												.add(heartRate.getDatasource()).add(heartRate.getValue())
												.add(heartRate.getComments()).add(heartRate.getLastUpdateTimestamp())
												.add(heartRate.getLastUpdateTimestamp()).add(heartRate.getIduser())
												.add(heartRate.getLastUpdateTimestamp()).add(heartRate.getTimestamp())
												.add(heartRate.getLastUpdateTimestamp()).add(heartRate.getStatus())
												.add(heartRate.getLastUpdateTimestamp()).add(heartRate.getDatasource())
												.add(heartRate.getLastUpdateTimestamp()).add(heartRate.getValue())
												.add(heartRate.getLastUpdateTimestamp()).add(heartRate.getComments())
												.add(heartRate.getLastUpdateTimestamp())
												.add(heartRate.getLastUpdateTimestamp()),
										res2 -> {

											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													heartRates.get(idx)
															.setIdHeartRate(res2.result().getKeys().getInteger(0));
												saveHeartRateZone(heartRates.get(idx).getIdHeartRate(),
														Arrays.asList(heartRate.getHeartRateZones()), conn.result())
																.setHandler(handler -> {
																	if (handler.succeeded()) {
																		HeartRateZone[] heartRateZones = new HeartRateZone[handler
																				.result().size()];
																		heartRates.get(idx).setHeartRateZones(handler
																				.result().toArray(heartRateZones));
																	}
																	futures.get(idx).complete();
																});
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(heartRates);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<HeartRateZone>> saveHeartRateZone(Integer idHeartRate, List<HeartRateZone> heartRateZones,
			SQLConnection sqlConnection) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < heartRateZones.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<HeartRateZone>> future = Future.future();

		try {
			new Handler<Integer>() {
				@Override
				public void handle(Integer idx) {
					if (idx < heartRateZones.size()) {
						HeartRateZone heartRateZone = heartRateZones.get(idx);
						sqlConnection.updateWithParams(
								"INSERT INTO retoobesidad.heart_rate_zone (idHeartRateZone,idHeartRate,iduser,timestamp,"
										+ "status,datasource,max,min,minutes,caloriesOut,name,comments) "
										+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
										+ "idHeartRate = ?, iduser = ?, timestamp = ?, status = ?, datasource = ?, max = ?, min = ?, minutes = ?, caloriesOut = ?, name = ?, "
										+ "comments = ?",
								new JsonArray().add(heartRateZone.getIdHeartRateZone()).add(idHeartRate)
										.add(heartRateZone.getIduser()).add(heartRateZone.getTimestamp())
										.add(heartRateZone.getStatus()).add(heartRateZone.getDatasource())
										.add(heartRateZone.getMax()).add(heartRateZone.getMin())
										.add(heartRateZone.getMinutes()).add(heartRateZone.getCaloriesOut())
										.add(heartRateZone.getName()).add(heartRateZone.getComments()).add(idHeartRate)
										.add(heartRateZone.getIduser()).add(heartRateZone.getTimestamp())
										.add(heartRateZone.getStatus()).add(heartRateZone.getDatasource())
										.add(heartRateZone.getMax()).add(heartRateZone.getMin())
										.add(heartRateZone.getMinutes()).add(heartRateZone.getCaloriesOut())
										.add(heartRateZone.getName()).add(heartRateZone.getComments()),
								res2 -> {
									if (res2.succeeded() && res2.result().getKeys().size() > 0) {
										if (res2.result().getKeys().getInteger(0) > 0)
											heartRateZones.get(idx)
													.setIdHeartRateZone(res2.result().getKeys().getInteger(0));
										futures.get(idx).complete();
									} else {
										futures.get(idx).fail(res2.cause() != null ? res2.cause().getMessage() : "");
									}
									handle(idx + 1);
								});
					}
				}
			}.handle(0);

			CompositeFuture.all(futures).setHandler(handler -> {
				if (handler.succeeded()) {
					future.complete(heartRateZones);
				} else {
					future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
				}
			});
		} catch (Exception e) {
			future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
		}
		return future;
	}

	private Future<List<Summary>> getActivitySummary(Long userId, Long lastUpdateTimestamp, Long startTime,
			Long endTime) {
		Future<List<Summary>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.fitbit_summary WHERE iduser = ? AND lastUpdateTimestamp >= ? AND timestamp > ? AND timestamp <= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select,
							new JsonArray().add(userId).add(lastUpdateTimestamp).add(startTime).add(endTime), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<Summary> result = res2.result().getRows().stream().map(summary -> {
										String distances = summary.getString("distances");
										summary.remove("distances");
										Summary s = Json.decodeValue(summary.encode(), Summary.class);
										s.setDistances(Arrays.asList(Json.decodeValue(distances, Distance[].class)));
										return s;
									}).collect(Collectors.toList());
									future.complete(result);
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<Summary>> saveActivitySummary(List<Summary> summaries, Long userId) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < summaries.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<Summary>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < summaries.size()) {
								Summary summary = summaries.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.fitbit_summary (idSummary,iduser,timestamp,"
												+ "status,datasource,activityCalories,caloriesBMR, caloriesOut, elevation, fairlyActiveMinutes,"
												+ "floors, lightlyActiveMinutes, marginalCalories, sedentaryMinutes, steps, veryActiveMinutes,"
												+ "activeScore, distances,lastUpdateTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
												+ "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "timestamp = IF (lastUpdateTimestamp < ?,?, timestamp),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "datasource = IF (lastUpdateTimestamp < ?,?, datasource),"
												+ "activityCalories = IF (lastUpdateTimestamp < ?,?, activityCalories),"
												+ "caloriesBMR = IF (lastUpdateTimestamp < ?,?, caloriesBMR),"
												+ "caloriesOut = IF (lastUpdateTimestamp < ?,?, caloriesOut),"
												+ "elevation = IF (lastUpdateTimestamp < ?,?, elevation),"
												+ "fairlyActiveMinutes = IF (lastUpdateTimestamp < ?,?, fairlyActiveMinutes),"
												+ "floors = IF (lastUpdateTimestamp < ?,?, floors),"
												+ "lightlyActiveMinutes = IF (lastUpdateTimestamp < ?,?, lightlyActiveMinutes),"
												+ "marginalCalories = IF (lastUpdateTimestamp < ?,?, marginalCalories),"
												+ "sedentaryMinutes = IF (lastUpdateTimestamp < ?,?, sedentaryMinutes),"
												+ "steps = IF (lastUpdateTimestamp < ?,?, steps),"
												+ "veryActiveMinutes = IF (lastUpdateTimestamp < ?,?, veryActiveMinutes),"
												+ "activeScore = IF (lastUpdateTimestamp < ?,?, activeScore),"
												+ "distances = IF (lastUpdateTimestamp < ?,?, distances),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(summary.getIdSummary()).add(userId)
												.add(summary.getTimestamp()).add(summary.getStatus())
												.add(summary.getDatasource()).add(summary.getActivityCalories())
												.add(summary.getCaloriesBMR()).add(summary.getCaloriesOut())
												.add(summary.getElevation()).add(summary.getFairlyActiveMinutes())
												.add(summary.getFloors()).add(summary.getLightlyActiveMinutes())
												.add(summary.getMarginalCalories()).add(summary.getSedentaryMinutes())
												.add(summary.getSteps()).add(summary.getVeryActiveMinutes())
												.add(summary.getActiveScore())
												.add(new JsonArray(summary.getDistances()).encode())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getLastUpdateTimestamp()).add(userId)
												.add(summary.getLastUpdateTimestamp()).add(summary.getTimestamp())
												.add(summary.getLastUpdateTimestamp()).add(summary.getStatus())
												.add(summary.getLastUpdateTimestamp()).add(summary.getDatasource())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getActivityCalories())
												.add(summary.getLastUpdateTimestamp()).add(summary.getCaloriesBMR())
												.add(summary.getLastUpdateTimestamp()).add(summary.getCaloriesOut())
												.add(summary.getLastUpdateTimestamp()).add(summary.getElevation())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getFairlyActiveMinutes())
												.add(summary.getLastUpdateTimestamp()).add(summary.getFloors())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getLightlyActiveMinutes())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getMarginalCalories())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getSedentaryMinutes())
												.add(summary.getLastUpdateTimestamp()).add(summary.getSteps())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getVeryActiveMinutes())
												.add(summary.getLastUpdateTimestamp()).add(summary.getActiveScore())
												.add(summary.getLastUpdateTimestamp())
												.add(new JsonArray(summary.getDistances()).encode())
												.add(summary.getLastUpdateTimestamp())
												.add(summary.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													summaries.get(idx)
															.setIdSummary(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(summaries);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<MedicalTestEntity>> getMedicalTest(Long userId, Long lastUpdateTimestamp) {
		Future<List<MedicalTestEntity>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicaltest WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result()
											.getRows().stream().map(medicalTest -> Json
													.decodeValue(medicalTest.encode(), MedicalTestEntity.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<MedicalTestEntity>> saveMedicalTest(List<MedicalTestEntity> medicalTest) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < medicalTest.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<MedicalTestEntity>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < medicalTest.size()) {
								MedicalTestEntity test = medicalTest.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.medicaltest (idMedicalTest,iduser,prescriber,"
												+ "prescriberComment,lastUpdateTimestamp,name,description,timestamp,"
												+ "timestampDone,timestampResults,picturePath,timestampCite,placeCite,doctorCite, status, createdBy, userViewTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
												+ "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "prescriber = IF (lastUpdateTimestamp < ?,?, prescriber),"
												+ "prescriberComment = IF (lastUpdateTimestamp < ?,?, prescriberComment),"
												+ "name = IF (lastUpdateTimestamp < ?,?, name),"
												+ "description = IF (lastUpdateTimestamp < ?,?, description),"
												+ "timestamp = IF (lastUpdateTimestamp < ?,?, timestamp),"
												+ "timestampDone = IF (lastUpdateTimestamp < ?,?, timestampDone),"
												+ "timestampResults = IF (lastUpdateTimestamp < ?,?, timestampResults),"
												+ "picturePath = IF (lastUpdateTimestamp < ?,?, picturePath),"
												+ "timestampCite = IF (lastUpdateTimestamp < ?,?, timestampCite),"
												+ "placeCite = IF (lastUpdateTimestamp < ?,?, placeCite),"
												+ "doctorCite = IF (lastUpdateTimestamp < ?,?, doctorCite),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "createdBy = IF (lastUpdateTimestamp < ?,?, createdBy),"
												+ "userViewTimestamp = IF (lastUpdateTimestamp < ?,?, userViewTimestamp),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(test.getIdMedicalTest()).add(test.getIduser())
												.add(test.getPrescriber()).add(test.getPrescriberComment())
												.add(test.getLastUpdateTimestamp()).add(test.getName())
												.add(test.getDescription()).add(test.getTimestamp())
												.add(test.getTimestampDone()).add(test.getTimestampResults())
												.add(test.getPicturePath()).add(test.getTimestampCite())
												.add(test.getPlaceCite()).add(test.getDoctorCite())
												.add(test.getStatus()).add(test.getCreatedBy())
												.add(test.getUserViewTimestamp()).add(test.getLastUpdateTimestamp())
												.add(test.getIduser()).add(test.getLastUpdateTimestamp())
												.add(test.getPrescriber()).add(test.getLastUpdateTimestamp())
												.add(test.getPrescriberComment()).add(test.getLastUpdateTimestamp())
												.add(test.getName()).add(test.getLastUpdateTimestamp())
												.add(test.getDescription()).add(test.getLastUpdateTimestamp())
												.add(test.getTimestamp()).add(test.getLastUpdateTimestamp())
												.add(test.getTimestampDone()).add(test.getLastUpdateTimestamp())
												.add(test.getTimestampResults()).add(test.getLastUpdateTimestamp())
												.add(test.getPicturePath()).add(test.getLastUpdateTimestamp())
												.add(test.getTimestampCite()).add(test.getLastUpdateTimestamp())
												.add(test.getPlaceCite()).add(test.getLastUpdateTimestamp())
												.add(test.getDoctorCite()).add(test.getLastUpdateTimestamp())
												.add(test.getStatus()).add(test.getLastUpdateTimestamp())
												.add(test.getCreatedBy()).add(test.getLastUpdateTimestamp())
												.add(test.getUserViewTimestamp()).add(test.getLastUpdateTimestamp())
												.add(test.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0) {
													medicalTest.get(idx)
															.setIdMedicalTest(res2.result().getKeys().getInteger(0));
												}
												futures.get(idx).complete();
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(medicalTest);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<Doctor>> getUserDoctors(Long userId) {
		Future<List<Doctor>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT doctor.* FROM retoobesidad.doctor AS doctor INNER JOIN retoobesidad.doctorpatient AS rel WHERE rel.idPatient = ? AND doctor.idDoctor=rel.idDoctor ORDER BY doctor.surname ASC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(res2.result().getRows().stream()
									.map(doctor -> Json.decodeValue(doctor.encode(), Doctor.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}
	
	private Future<List<Doctor>> getDoctor(Long doctorId) {
		Future<List<Doctor>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.doctor WHERE idDoctor=?;";
					conn.result().queryWithParams(select, new JsonArray().add(doctorId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							future.complete(res2.result().getRows().stream()
									.map(doctor -> Json.decodeValue(doctor.encode(), Doctor.class))
									.collect(Collectors.toList()));
						} else {
							future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<DoctorPatient>> validateDoctorPatient(Long idDoctor, Long idPatient) {
		Future<List<DoctorPatient>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "UPDATE retoobesidad.doctorpatient SET status = ?, validationTimestamp = ? "
							+ "WHERE idDoctor = ? AND idPatient = ?;";
					conn.result()
							.queryWithParams(select, new JsonArray().add(1)
									.add(Calendar.getInstance().getTimeInMillis()).add(idDoctor).add(idPatient),
									res2 -> {
										conn.result().close();
										if (res2.succeeded()) {
											future.complete(res2.result().getRows()
													.stream().map(doctorPatient -> Json
															.decodeValue(doctorPatient.encode(), DoctorPatient.class))
													.collect(Collectors.toList()));
										} else {
											future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
										}
									});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<ChallengeEntity>> getChallenge(Long userId, Long lastUpdateTimestamp) {
		Future<List<ChallengeEntity>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.challenge WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY startTimestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(res2.result().getRows().stream().map(
											challenge -> Json.decodeValue(challenge.encode(), ChallengeEntity.class))
											.collect(Collectors.toList()));
								} else {
									future.fail(res2.cause() != null ? res2.cause().getMessage() : "");
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
			}
		});

		return future;
	}

	private Future<List<ChallengeEntity>> saveChallenge(List<ChallengeEntity> challenges) {
		List<Future> futures = new ArrayList<>();
		for (int i = 0; i < challenges.size(); i++) {
			futures.add(Future.future());
		}
		Future<List<ChallengeEntity>> future = Future.future();

		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					new Handler<Integer>() {
						@Override
						public void handle(Integer idx) {
							if (idx < challenges.size()) {
								ChallengeEntity challenge = challenges.get(idx);
								conn.result().updateWithParams(
										"INSERT INTO retoobesidad.challenge (`idChallenge`,`iduser`,`challengeTitle`,"
												+ "`challengeDescription`,`lastUpdateTimestamp`,`startTimestamp`,`finishTimestamp`,"
												+ "`status`,`sensations`,`sensationsComments`,`startLocationLatitude`,"
												+ "`startLocationLongitude`,`startLocationDescription`,`endLocationLatitude`,"
												+ "`endLocationLongitude`,`endLocationDescription`,`distance`,`steps`,`time`,"
												+ "`stairs`,`challengeStatus`,`type`)"
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
												+ "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "challengeTitle = IF (lastUpdateTimestamp < ?,?, challengeTitle),"
												+ "challengeDescription = IF (lastUpdateTimestamp < ?,?, challengeDescription),"
												+ "startTimestamp = IF (lastUpdateTimestamp < ?,?, startTimestamp),"
												+ "finishTimestamp = IF (lastUpdateTimestamp < ?,?, finishTimestamp),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "sensations = IF (lastUpdateTimestamp < ?,?, sensations),"
												+ "sensationsComments = IF (lastUpdateTimestamp < ?,?, sensationsComments),"
												+ "startLocationLatitude = IF (lastUpdateTimestamp < ?,?, startLocationLatitude),"
												+ "startLocationLongitude = IF (lastUpdateTimestamp < ?,?, startLocationLongitude),"
												+ "startLocationDescription = IF (lastUpdateTimestamp < ?,?, startLocationDescription),"
												+ "endLocationLatitude = IF (lastUpdateTimestamp < ?,?, endLocationLatitude),"
												+ "endLocationLongitude = IF (lastUpdateTimestamp < ?,?, endLocationLongitude),"
												+ "endLocationDescription = IF (lastUpdateTimestamp < ?,?, endLocationDescription),"
												+ "distance = IF (lastUpdateTimestamp < ?,?, distance),"
												+ "steps = IF (lastUpdateTimestamp < ?,?, steps),"
												+ "time = IF (lastUpdateTimestamp < ?,?, time),"
												+ "stairs = IF (lastUpdateTimestamp < ?,?, stairs),"
												+ "challengeStatus = IF (lastUpdateTimestamp < ?,?, challengeStatus),"
												+ "type = IF (lastUpdateTimestamp < ?,?, type),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(challenge.getIdChallenge()).add(challenge.getIduser())
												.add(challenge.getChallengeTitle())
												.add(challenge.getChallengeDescription())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getStartTimestamp()).add(challenge.getFinishTimestamp())
												.add(challenge.getStatus()).add(challenge.getSensations())
												.add(challenge.getSensationsComments())
												.add(challenge.getStartLocationLatitude())
												.add(challenge.getStartLocationLongitude())
												.add(challenge.getStartLocationDescription())
												.add(challenge.getEndLocationLatitude())
												.add(challenge.getEndLocationLongitude())
												.add(challenge.getEndLocationDescription()).add(challenge.getDistance())
												.add(challenge.getSteps()).add(challenge.getTime())
												.add(challenge.getStairs()).add(challenge.getChallengeStatus())
												.add(challenge.getType()).add(challenge.getLastUpdateTimestamp())
												.add(challenge.getIduser()).add(challenge.getLastUpdateTimestamp())
												.add(challenge.getChallengeTitle())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getChallengeDescription())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getStartTimestamp())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getFinishTimestamp())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getStatus())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getSensations())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getSensationsComments())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getStartLocationLatitude())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getStartLocationLongitude())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getStartLocationDescription())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getEndLocationLatitude())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getEndLocationLongitude())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getEndLocationDescription())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getDistance())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getSteps())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getTime())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getStairs())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getChallengeStatus())
												.add(challenge.getLastUpdateTimestamp()).add(challenge.getType())
												.add(challenge.getLastUpdateTimestamp())
												.add(challenge.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													challenges.get(idx)
															.setIdChallenge(res2.result().getKeys().getInteger(0));
												futures.get(idx).complete();
											} else {
												futures.get(idx)
														.fail(res2.cause() != null ? res2.cause().getMessage() : "");
											}
											handle(idx + 1);
										});
							}
						}
					}.handle(0);

					CompositeFuture.all(futures).setHandler(handler -> {
						conn.result().close();
						if (handler.succeeded()) {
							future.complete(challenges);
						} else {
							future.fail(handler.cause() != null ? handler.cause().getMessage() : "");
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause() != null ? e.getCause().getMessage() : "");
					conn.result().close();
				}
			} else {
				future.fail(conn.cause() != null ? conn.cause().getMessage() : "");
				conn.result().close();
			}
		});
		return future;
	}
}