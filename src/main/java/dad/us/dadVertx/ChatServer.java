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
import dad.us.dadVertx.entities.firebase.FirebaseEntity;
import dad.us.dadVertx.entities.health.values.BloodGlucose;
import dad.us.dadVertx.entities.health.values.BloodPressure;
import dad.us.dadVertx.entities.health.values.Distance;
import dad.us.dadVertx.entities.health.values.HeartRate;
import dad.us.dadVertx.entities.health.values.HeartRateZone;
import dad.us.dadVertx.entities.health.values.Summary;
import dad.us.dadVertx.entities.health.values.Weight;
import dad.us.dadVertx.entities.medicaltest.MedicalTestEntity;
import dad.us.dadVertx.entities.medicine.Medicine;
import dad.us.dadVertx.entities.psychology.TestResponse;
import dad.us.dadVertx.entities.user.User;
import dad.us.dadVertx.entities.user.UserData;
import dad.us.dadVertx.watson.WatsonQuestionAnswer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

@SuppressWarnings("rawtypes")
public class ChatServer extends AbstractVerticle {

	private static AsyncSQLClient mySQLClient;

	@Override
	public void start() throws Exception {
		JsonObject config = new JsonObject().put("host", "localhost").put("username", "root").put("password", "root")
				.put("database", "retoobesidad").put("port", 3306).put("maxPoolSize", 100);
		mySQLClient = MySQLClient.createShared(vertx, config);

		/*
		 * getUserFirebaseToken(-2124119833).setHandler(handler -> { if
		 * (handler.succeeded() && handler.result() != null)
		 * FirebaseUtils.sendMessage(handler.result().getFirebase_token(),
		 * "Hola mundo", "Firebase"); });
		 */

		Router router = Router.router(vertx);

		router.route("/api/obesity/*").handler(BodyHandler.create());

		router.get("/api/obesity/firebase/:userId").handler(this::getUserFirebaseToken);
		router.post("/api/obesity/firebase").handler(this::saveUserFirebaseToken);

		router.get("/api/obesity/medicaltest/:userId/:lastUpdateTime").handler(this::getMedicalTest);
		router.post("/api/obesity/medicaltest").handler(this::saveMedicalTest);
		router.post("/api/obesity/medicaltest/uploadpicture").handler(this::postMedicalTestImage);

		router.post("/api/obesity/consent/upload").handler(this::postUploadSignature);
		router.post("/api/obesity/user/uploadimage").handler(this::postUploadImage);
		router.get("/api/obesity/user/image/:userId").handler(this::getUserImage);

		router.get("/api/obesity/user/login/:userId").handler(this::getUser);
		router.post("/api/obesity/user/login").handler(this::saveUser);

		router.get("/api/obesity/user/data/:userId").handler(this::getUserData);
		router.post("/api/obesity/user/data").handler(this::saveUserData);
		router.delete("/api/obesity/user/data/:userId").handler(this::deleteUserData);

		router.get("/api/obesity/appointment/:userId/:lastUpdateTime").handler(this::getUserAppointment);
		router.post("/api/obesity/appointment").handler(this::saveUserAppointment);

		router.post("/api/obesity/medicine/drug").handler(this::saveMedicine);
		router.get("/api/obesity/medicine/list").handler(this::getDrugList);
		router.get("/api/obesity/medicine/drug/:userId/:lastUpdateTime").handler(this::getMedicinesByUser);

		router.get("/api/obesity/info/surgery/list/:userId").handler(this::getSurgeryIngoList);

		router.get("/api/obesity/activity/aims/:userId").handler(this::getAimsByUser);
		router.get("/api/obesity/consent/:userId").handler(this::getPendingConsent);
		router.get("/api/obesity/psychology/test/results/:userId").handler(this::getTestResults);
		router.get("/api/obesity/psychology/test/:userId").handler(this::getPsychologyTest);
		router.post("/api/obesity/psychology/test").handler(this::saveTestResult);
		router.post("/api/obesity/psychology/tests").handler(this::saveTestResults);

		router.get("/api/obesity/messages/last/:groupId").handler(this::getGroupLastMessage);
		router.get("/api/obesity/messages/:groupId/:timestamp").handler(this::getGroupMessagesFromDate);
		router.get("/api/obesity/messages/:groupId/:timestamp/reverse").handler(this::getGroupMessagesUntilDate);
		router.get("/api/obesity/messages/:groupId/:fromtimestamp/:totimestamp")
				.handler(this::getGroupMessagesFromToDate);

		router.get("/api/obesity/groups/user/:userId").handler(this::getUserGroups);
		router.get("/api/obesity/groups/relateduser/:userId").handler(this::getRelatedUsers);
		router.get("/api/obesity/groups/members/:groupId").handler(this::getGroupUsers);
		router.get("/api/obesity/groups/unread/unpending/:userId/:groupId").handler(this::markAsReadMessages);
		router.get("/api/obesity/groups/unread/:userId/:groupId").handler(this::getUnreadMessages);
		router.post("/api/obesity/groups/multi").handler(this::addMultiUserGroup);
		router.post("/api/obesity/groups/single").handler(this::addSingleUserGroup);
		router.delete("/api/obesity/groups").handler(this::deleteUserFromGroup);
		router.get("/api/obesity/users/:idUser").handler(this::getUserById);

		router.get("/api/obesity/health/weight/:userId/:lastUpdateTime").handler(this::getHealthWeight);
		router.post("/api/obesity/health/weight").handler(this::saveHealthWeight);

		router.get("/api/obesity/health/pressure/:userId/:lastUpdateTime").handler(this::getBloodPressure);
		router.post("/api/obesity/health/pressure").handler(this::saveBloodPressure);

		router.get("/api/obesity/health/glucose/:userId/:lastUpdateTime").handler(this::getBloodGlucose);
		router.post("/api/obesity/health/glucose").handler(this::saveBloodGlucose);

		router.get("/api/obesity/health/heartrate/:userId/:lastUpdateTime").handler(this::getHeartRate);
		router.post("/api/obesity/health/heartrate").handler(this::saveHeartRate);

		router.get("/api/obesity/challenge/:userId/:lastUpdateTime").handler(this::getChallenge);
		router.post("/api/obesity/challenge").handler(this::saveChallenge);

		router.get("/api/obesity/health/activitysummary/:userId/:lastUpdateTime").handler(this::getActivitySummary);
		router.post("/api/obesity/health/activitysummary/:userId").handler(this::saveActivitySummary);

		router.get("/api/obesity/doctor/:userId").handler(this::getDoctor);

		vertx.createHttpServer().requestHandler(router::accept).listen(8081);

		WatsonQuestionAnswer watsonQuestions = new WatsonQuestionAnswer("ObesityFAQ",
				"sc6891d3ab_a39f_4133_9f8d_ea7b351ec170", "Obesity");
		EventBus eb = vertx.eventBus();

		eb.consumer("chat.to.server").handler(message -> {
			ChatMessage jsonMessage = Json.decodeValue(message.body().toString(), ChatMessage.class);
			try {
				jsonMessage.setMessage(StringEscapeUtils.unescapeJava(jsonMessage.getMessage()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			publishMessage(jsonMessage, message, eb);
			getGroupUsers(jsonMessage.getGroup_id()).setHandler(new Handler<AsyncResult<List<User>>>() {

				@Override
				public void handle(AsyncResult<List<User>> event) {
					List<User> usersOfInterest = event.result().stream()
							.filter(usr -> usr.getIduser().equals(3) || usr.getIduser().equals(4))
							.collect(Collectors.toList());
					if (!usersOfInterest.isEmpty()) {
						List<String> responses = watsonQuestions.getResponse(jsonMessage.getMessage());
						String msg = "Lo siento, no sé responderte a esa pregunta :-(";
						if (!responses.isEmpty()) {
							msg = responses.get(0);
						}
						publishMessage(new ChatMessage(msg, 0, jsonMessage.getGroup_id(),
								usersOfInterest.get(0).getIduser(), Calendar.getInstance().getTimeInMillis()), message,
								eb);
					}
				}
			});

		});

		eb.consumer("server.ping").handler(message -> {
			message.reply(new JsonObject().put("pong", "pong"));
			System.out.println("Ping recibido");
		});

		eb.consumer("chat.received").handler(message -> {
			JsonObject jsonReceived = new JsonObject(message.body().toString());
			addMessageState(jsonReceived.getInteger("message_id"), jsonReceived.getInteger("user_id"),
					MessageState.Received.name(), Calendar.getInstance().getTimeInMillis());
		});

		eb.consumer("chat.read").handler(message -> {
			JsonObject jsonRead = new JsonObject(message.body().toString());
			addMessageState(jsonRead.getInteger("message_id"), jsonRead.getInteger("user_id"), MessageState.Read.name(),
					Calendar.getInstance().getTimeInMillis());
		});

		TcpEventBusBridge bridge = TcpEventBusBridge.create(vertx,
				new BridgeOptions().addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
						.addInboundPermitted(new PermittedOptions().setAddress("chat.received"))
						.addInboundPermitted(new PermittedOptions().setAddress("chat.read"))
						.addInboundPermitted(new PermittedOptions().setAddress("server.ping"))
						.addOutboundPermitted(new PermittedOptions().setAddressRegex("info.to.client.*"))
						.addOutboundPermitted(new PermittedOptions().setAddressRegex("chat.to.client.*")));

		bridge.listen(7000, res -> System.out.println("Ready"));

		/*
		 * vertx.setPeriodic(10000, handler -> { eb.publish("chat.to.server",
		 * new JsonObject( Json.encode(new
		 * ChatMessage("Es cierto, puedo responder a estos mensajes", 0, 32, 2,
		 * Calendar.getInstance().getTimeInMillis())))); });
		 */

	}

	private void publishMessage(ChatMessage jsonMessage, Message<Object> message, EventBus eb) {
		saveMessageInDatabase(jsonMessage.getMessage(), jsonMessage.getGroup_id(), jsonMessage.getUser_id(),
				jsonMessage.getTimestamp()).setHandler(res -> {
					if (res.succeeded()) {
						addMessageState(jsonMessage.getId_message(), jsonMessage.getUser_id(), MessageState.Sent.name(),
								Calendar.getInstance().getTimeInMillis());
						ChatMessage savedMessage = res.result();
						try {
							savedMessage.setMessage(StringEscapeUtils.escapeJava(savedMessage.getMessage()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						eb.publish("chat.to.client." + jsonMessage.getGroup_id(),
								new JsonObject(Json.encodePrettily(savedMessage)));
						message.reply(new JsonObject(Json.encodePrettily(savedMessage)));
					} else {
						message.reply(new JsonObject("Error"));
					}
				});

	}

	private void publishGroupInfo(Integer group_id, String info_message) {
		vertx.eventBus().publish("info.to.client." + group_id, new JsonObject().put("info_message", info_message));
	}

	private void getMedicalTest(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getMedicalTest(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveMedicalTest(RoutingContext routingContext) {
		MedicalTestEntity[] medicalTests = Json.decodeValue(routingContext.getBodyAsString(),
				MedicalTestEntity[].class);

		if (medicalTests.length > 0) {
			saveMedicalTest(Arrays.asList(medicalTests))
					.setHandler(new Handler<AsyncResult<List<MedicalTestEntity>>>() {
						@Override
						public void handle(AsyncResult<List<MedicalTestEntity>> event) {
							if (event.succeeded()) {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.end(Json.encode(event.result()));
							} else {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(401).end();
							}
						}
					});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void postMedicalTestImage(RoutingContext routingContext) {
		MultiMap attributes = routingContext.request().formAttributes();
		Set<FileUpload> fileUploadSet = routingContext.fileUploads();
		Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
		try {
			vertx.fileSystem().mkdir("consents", h -> {
				try {
					while (fileUploadIterator.hasNext()) {
						FileUpload fileUpload = fileUploadIterator.next();
						Buffer buffer = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
						MedicalTestEntity medicalTest = Json.decodeValue(attributes.get("test"),
								MedicalTestEntity.class);
						String name = "medical/test-" + medicalTest.getIdMedicalTest() + "-"
								+ medicalTest.getTimestamp() + ".jpg";
						vertx.fileSystem().writeFile(name, buffer, new Handler<AsyncResult<Void>>() {
							@Override
							public void handle(AsyncResult<Void> result) {
								if (result.succeeded()) {
									medicalTest.setPicturePath(name);
									saveMedicalTest(Arrays.asList(medicalTest))
											.setHandler(new Handler<AsyncResult<List<MedicalTestEntity>>>() {

												@Override
												public void handle(AsyncResult<List<MedicalTestEntity>> event) {
													if (event.succeeded()) {
														routingContext.response()
																.putHeader("content-type",
																		"application/json; charset=utf-8")
																.end(Json.encode(event.result().get(0)));
													} else {
														routingContext.response()
																.putHeader("content-type",
																		"application/json; charset=utf-8")
																.setStatusCode(401).end();
													}
												}
											});
								} else {
									routingContext.response()
											.putHeader("content-type", "application/json; charset=utf-8")
											.setStatusCode(401).end();
								}
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(401)
					.end();
		}
	}

	private void getUserFirebaseToken(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getUserFirebaseToken(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveUserFirebaseToken(RoutingContext routingContext) {
		FirebaseEntity firebaseToken = Json.decodeValue(routingContext.getBodyAsString(), FirebaseEntity.class);
		saveUserFirebaseToken(firebaseToken).setHandler(new Handler<AsyncResult<FirebaseEntity>>() {
			@Override
			public void handle(AsyncResult<FirebaseEntity> event) {
				if (event.succeeded()) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encodePrettily(event.result()));
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			}
		});
	}

	private void getUser(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getUser(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveUser(RoutingContext routingContext) {
		User user = Json.decodeValue(routingContext.getBodyAsString(), User.class);
		saveUser(user).setHandler(new Handler<AsyncResult<User>>() {
			@Override
			public void handle(AsyncResult<User> event) {
				if (event.succeeded()) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encode(event.result()));
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			}
		});
	}

	private void getUserData(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getUserData(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveUserData(RoutingContext routingContext) {
		UserData userData = Json.decodeValue(routingContext.getBodyAsString(), UserData.class);
		saveUserData(userData).setHandler(new Handler<AsyncResult<UserData>>() {
			@Override
			public void handle(AsyncResult<UserData> event) {
				if (event.succeeded()) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encode(event.result()));
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			}
		});
	}

	private void deleteUserData(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		deleteUserData(userId).setHandler(new Handler<AsyncResult<Boolean>>() {
			@Override
			public void handle(AsyncResult<Boolean> event) {
				if (event.succeeded()) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encode(event.result()));
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			}
		});
	}

	private void getUserAppointment(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getUserAppointment(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveUserAppointment(RoutingContext routingContext) {
		Appointment[] weights = Json.decodeValue(routingContext.getBodyAsString(), Appointment[].class);

		if (weights.length > 0) {
			saveUserAppointment(Arrays.asList(weights)).setHandler(new Handler<AsyncResult<List<Appointment>>>() {
				@Override
				public void handle(AsyncResult<List<Appointment>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getPendingConsent(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getPendingConsent(userId, true).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void postUploadSignature(RoutingContext routingContext) {
		MultiMap attributes = routingContext.request().formAttributes();
		Set<FileUpload> fileUploadSet = routingContext.fileUploads();
		Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
		try {
			vertx.fileSystem().mkdir("consents", h -> {
				try {
					while (fileUploadIterator.hasNext()) {
						FileUpload fileUpload = fileUploadIterator.next();
						Buffer buffer = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
						Consent consent = Json.decodeValue(attributes.get("consent"), Consent.class);
						String name = "consents/consent-" + consent.getUser_id().getIduser() + "-"
								+ consent.getSign_timestamp() + "-" + consent.getUdid() + ".svg";
						vertx.fileSystem().writeFile(name, buffer, new Handler<AsyncResult<Void>>() {
							@Override
							public void handle(AsyncResult<Void> result) {
								if (result.succeeded()) {
									updateFilledConsent(consent).setHandler(new Handler<AsyncResult<Consent>>() {

										@Override
										public void handle(AsyncResult<Consent> event) {
											if (event.succeeded()) {
												routingContext.response()
														.putHeader("content-type", "application/json; charset=utf-8")
														.end("{\"result\":\"OK\"}");
											} else {
												routingContext.response()
														.putHeader("content-type", "application/json; charset=utf-8")
														.setStatusCode(401).end();
											}
										}
									});
								} else {
									routingContext.response()
											.putHeader("content-type", "application/json; charset=utf-8")
											.setStatusCode(401).end();
								}
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(401)
					.end();
		}
	}

	private void getUserImage(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getUser(userId).setHandler(new Handler<AsyncResult<User>>() {

			@Override
			public void handle(AsyncResult<User> event) {
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
							routingContext.response().sendFile(file.getPath());
						} catch (IOException e) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
									.setStatusCode(401).end();
						}
					} else {
						routingContext.request().response().sendFile(user.getImage());
					}
				}

			}
		});
	}

	private void postUploadImage(RoutingContext routingContext) {
		MultiMap attributes = routingContext.request().formAttributes();
		Set<FileUpload> fileUploadSet = routingContext.fileUploads();
		Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
		try {
			vertx.fileSystem().mkdir("images", h -> {
				try {
					while (fileUploadIterator.hasNext()) {
						FileUpload fileUpload = fileUploadIterator.next();
						Buffer buffer = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
						JsonObject json = new JsonObject(attributes.get("content"));
						String name = "images/" + json.getString("filename");
						Integer userId = json.getInteger("userId");
						vertx.fileSystem().writeFile(name, buffer, new Handler<AsyncResult<Void>>() {
							@Override
							public void handle(AsyncResult<Void> result) {
								if (result.succeeded()) {
									saveUserImage(userId, name).setHandler(event -> {
										if (event.succeeded() && event.result() != null) {
											routingContext.response()
													.putHeader("content-type", "application/json; charset=utf-8")
													.end("{\"filename\":\"" + name + "\"}");
										} else {
											routingContext.response()
													.putHeader("content-type", "application/json; charset=utf-8")
													.setStatusCode(401).end();
										}
									});

								} else {
									routingContext.response()
											.putHeader("content-type", "application/json; charset=utf-8")
											.setStatusCode(401).end();
								}
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(401)
					.end();
		}
	}

	private void getMedicinesByUser(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getUserMedicine(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveMedicine(RoutingContext routingContext) {
		Medicine[] medicine = Json.decodeValue(routingContext.getBodyAsString(), Medicine[].class);

		if (medicine.length > 0) {
			saveUserMedicine(Arrays.asList(medicine)).setHandler(new Handler<AsyncResult<List<Medicine>>>() {
				@Override
				public void handle(AsyncResult<List<Medicine>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getAimsByUser(RoutingContext routingContext) {
		Integer user_id = new Integer(routingContext.request().getParam("userId"));
		getAimsByUser(user_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getPsychologyTest(RoutingContext routingContext) {
		// Integer userId = new
		// Integer(routingContext.request().getParam("userId"));
		routingContext.response().putHeader("content-type", "application/json; charset=ascii")
				.sendFile("testpsicologico.json");
	}

	private void getDrugList(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=ascii")
				.sendFile("drug_list.txt");
	}

	private void getSurgeryIngoList(RoutingContext routingContext) {
		// Integer userId = new
		// Integer(routingContext.request().getParam("userId"));
		routingContext.response().putHeader("content-type", "application/json; charset=ascii")
				.sendFile("surgery_info_list.json");
	}

	private void saveTestResult(RoutingContext routingContext) {
		TestResponse testResponse = Json.decodeValue(routingContext.getBodyAsString(), TestResponse.class);
		addTestResponse(testResponse).setHandler(new Handler<AsyncResult<TestResponse>>() {
			@Override
			public void handle(AsyncResult<TestResponse> event) {
				if (event.succeeded()) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encode(event.result()));
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			}
		});
	}

	private void saveTestResults(RoutingContext routingContext) {
		JsonArray jsonArray = new JsonArray(routingContext.getBodyAsString());
		List<TestResponse> responses = new ArrayList<>();
		jsonArray.iterator().forEachRemaining(c -> responses.add(Json.decodeValue(c.toString(), TestResponse.class)));
		addTestResponses(responses).setHandler(new Handler<AsyncResult<List<TestResponse>>>() {
			@Override
			public void handle(AsyncResult<List<TestResponse>> event) {
				if (event.succeeded()) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encode(event.result()));
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(401).end();
				}
			}
		});
	}

	private void getTestResults(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getTestResults(userId).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getGroupMessagesFromDate(RoutingContext routingContext) {
		Long timestamp = new Long(routingContext.request().getParam("timestamp"));
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupMessages(group_id, timestamp, Calendar.getInstance().getTimeInMillis(), 100).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getGroupMessagesUntilDate(RoutingContext routingContext) {
		Long timestamp = new Long(routingContext.request().getParam("timestamp"));
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupMessages(group_id, 0l, timestamp, 100).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getGroupMessagesFromToDate(RoutingContext routingContext) {
		Long fromtimestamp = new Long(routingContext.request().getParam("fromtimestamp"));
		Long totimestamp = new Long(routingContext.request().getParam("totimestamp"));
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupMessages(group_id, fromtimestamp, totimestamp, 300).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getGroupLastMessage(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupLastMessage(group_id).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getUserGroups(RoutingContext routingContext) {
		Integer user_id = new Integer(routingContext.request().getParam("userId"));
		getUserGroups(user_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getRelatedUsers(RoutingContext routingContext) {
		Integer user_id = new Integer(routingContext.request().getParam("userId"));
		getRelatedUsers(user_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getGroupUsers(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		getGroupUsers(group_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getUnreadMessages(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		Integer user_id = new Integer(routingContext.request().getParam("userId"));
		getUnreadMessages(user_id, group_id).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void markAsReadMessages(RoutingContext routingContext) {
		Integer group_id = new Integer(routingContext.request().getParam("groupId"));
		Integer user_id = new Integer(routingContext.request().getParam("userId"));
		markAsReadMessages(user_id, group_id).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end();
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void addMultiUserGroup(RoutingContext routingContext) {
		try {
			JsonObject body = new JsonObject(routingContext.getBodyAsString());
			Integer user_id = body.getInteger("user_id");
			getUserById(user_id).setHandler(res -> {
				if (res.succeeded()) {
					User user = res.result();
					createGroup(user, "", ChatServerStrings.getGroupCreatedBy(user)).setHandler(res2 -> {
						if (res2.succeeded()) {
							ChatGroup group = res2.result();
							Future<Boolean> f1 = addUserToGroup(user.getIduser(), group.getIdchat_group(),
									Calendar.getInstance().getTimeInMillis());
							f1.setHandler(result -> {
								if (result.succeeded()) {
									routingContext.response().setStatusCode(201)
											.putHeader("content-type", "application/json; charset=utf-8")
											.end(Json.encodePrettily(group));
								} else {
									routingContext.response().setStatusCode(401).end();
								}
							});
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} else {
					routingContext.response().setStatusCode(401).end();
				}
			});
		} catch (Exception e) {
			routingContext.response().setStatusCode(401).end();
		}
	}

	private void addSingleUserGroup(RoutingContext routingContext) {
		try {
			JsonObject body = new JsonObject(routingContext.getBodyAsString());
			Integer user_id = body.getInteger("user_id");
			Integer friend_id = body.getInteger("friend_id");
			getUserById(user_id).setHandler(res -> {
				if (res.succeeded()) {
					User user = res.result();
					getUserById(friend_id).setHandler(res1 -> {
						if (res1.succeeded()) {
							User friend = res1.result();
							createGroup(user, "", ChatServerStrings.getSingleUserGroupDescription(user, friend))
									.setHandler(res2 -> {
										if (res2.succeeded()) {
											ChatGroup group = res2.result();
											Long timestamp = Calendar.getInstance().getTimeInMillis();
											Future<Boolean> f1 = addUserToGroup(user.getIduser(),
													group.getIdchat_group(), timestamp);
											Future<Boolean> f2 = addUserToGroup(friend.getIduser(),
													group.getIdchat_group(), timestamp);
											CompositeFuture.all(f1, f2).setHandler(result -> {
												if (result.succeeded()) {
													routingContext.response().setStatusCode(201)
															.putHeader("content-type",
																	"application/json; charset=utf-8")
															.end(Json.encodePrettily(group));
												} else {
													routingContext.response().setStatusCode(401).end();
												}
											});
										} else {
											routingContext.response().setStatusCode(401).end();
										}
									});
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} else {
					routingContext.response().setStatusCode(401).end();
				}
			});
		} catch (Exception e) {
			routingContext.response().setStatusCode(401).end();
		}
	}

	private void deleteUserFromGroup(RoutingContext routingContext) {
		JsonObject body = new JsonObject(routingContext.getBodyAsString());
		Integer user_id = body.getInteger("user_id");
		Integer group_id = body.getInteger("group_id");
		deleteUserFromGroup(user_id, group_id).setHandler(res -> {
			if (res.succeeded()) {
				Future<User> userFuture = getUserById(user_id);
				userFuture.setHandler(res2 -> {
					if (res2.succeeded()) {
						publishGroupInfo(group_id, ChatServerStrings.userLeftGroup(res2.result()));
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encodePrettily(res.result()));

					}
				});
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getUserById(RoutingContext routingContext) {
		Integer user_id = new Integer(routingContext.request().getParam("idUser"));
		getUserById(user_id).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getDoctor(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		getDoctor(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void getHealthWeight(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getHealthWeight(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveHealthWeight(RoutingContext routingContext) {
		Weight[] weights = Json.decodeValue(routingContext.getBodyAsString(), Weight[].class);

		if (weights.length > 0) {
			saveHealthWeight(Arrays.asList(weights)).setHandler(new Handler<AsyncResult<List<Weight>>>() {
				@Override
				public void handle(AsyncResult<List<Weight>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getBloodPressure(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getBloodPressure(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveBloodPressure(RoutingContext routingContext) {
		BloodPressure[] bloodPressure = Json.decodeValue(routingContext.getBodyAsString(), BloodPressure[].class);

		if (bloodPressure.length > 0) {
			saveBloodPressure(Arrays.asList(bloodPressure)).setHandler(new Handler<AsyncResult<List<BloodPressure>>>() {
				@Override
				public void handle(AsyncResult<List<BloodPressure>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getBloodGlucose(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getBloodGlucose(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveBloodGlucose(RoutingContext routingContext) {
		BloodGlucose[] bloodGlucose = Json.decodeValue(routingContext.getBodyAsString(), BloodGlucose[].class);

		if (bloodGlucose.length > 0) {
			saveBloodGlucose(Arrays.asList(bloodGlucose)).setHandler(new Handler<AsyncResult<List<BloodGlucose>>>() {
				@Override
				public void handle(AsyncResult<List<BloodGlucose>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getHeartRate(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getHeartRate(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveHeartRate(RoutingContext routingContext) {
		HeartRate[] heartRate = Json.decodeValue(routingContext.getBodyAsString(), HeartRate[].class);

		if (heartRate.length > 0) {
			saveHeartRate(Arrays.asList(heartRate)).setHandler(new Handler<AsyncResult<List<HeartRate>>>() {
				@Override
				public void handle(AsyncResult<List<HeartRate>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getChallenge(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getChallenge(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveChallenge(RoutingContext routingContext) {
		ChallengeEntity[] challenges = Json.decodeValue(routingContext.getBodyAsString(), ChallengeEntity[].class);

		if (challenges.length > 0) {
			saveChallenge(Arrays.asList(challenges)).setHandler(new Handler<AsyncResult<List<ChallengeEntity>>>() {
				@Override
				public void handle(AsyncResult<List<ChallengeEntity>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	private void getActivitySummary(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Long lastUpdateTime = new Long(routingContext.request().getParam("lastUpdateTime"));
		getActivitySummary(userId, lastUpdateTime).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveActivitySummary(RoutingContext routingContext) {
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		Summary[] summaries = Json.decodeValue(routingContext.getBodyAsString(), Summary[].class);

		if (summaries.length > 0) {
			saveActivitySummary(Arrays.asList(summaries), userId).setHandler(new Handler<AsyncResult<List<Summary>>>() {
				@Override
				public void handle(AsyncResult<List<Summary>> event) {
					if (event.succeeded()) {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.end(Json.encode(event.result()));
					} else {
						routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(401).end();
					}
				}
			});
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encode(new ArrayList<>()));
		}
	}

	// ------------------------ FUTURES ------------------------

	private Future<Consent> getPendingConsent(Integer userId, boolean pending) {
		Future<Consent> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.consent WHERE user_id = ?";
					if (pending)
						select += " AND sign_timestamp is null ";
					select += "ORDER BY timestamp;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), consent_handler -> {
						conn.result().close();
						if (consent_handler.succeeded()) {
							if (consent_handler.result().getNumRows() > 0) {
								JsonObject consent_json = consent_handler.result().getRows().get(0);
								getUserById(consent_json.getInteger("user_id")).setHandler(user_handler -> {
									if (user_handler.succeeded()) {
										getConsentMedicalGroup(consent_json.getInteger("idconsent"))
												.setHandler(final_handler -> {
													if (final_handler.succeeded()) {
														Consent consent = new Consent(
																consent_json.getInteger("idconsent"),
																user_handler.result(), consent_json.getString("centro"),
																consent_json.getString("servicio"),
																consent_json.getString("informacion_interes"),
																consent_json.getLong("timestamp"),
																consent_json.getLong("sign_timestamp"),
																consent_json.getInteger("acciones_oportunas") == 1,
																consent_json.getInteger("muestras_biologicas") == 1,
																consent_json.getInteger("muestras_investigacion") == 1,
																consent_json.getInteger("imagenes") == 1,
																consent_json.getInteger("tipo_intervencion"),
																final_handler.result(),
																consent_json.getString("nombre"),
																consent_json.getString("apellidos"),
																consent_json.getString("dni"),
																consent_json.getString("udid"));
														future.complete(consent);
													} else {
														future.fail(final_handler.cause());
													}
												});
									} else {
										future.fail(user_handler.cause());
									}
								});
							} else {
								future.complete(null);
							}
						} else {
							future.fail(consent_handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<List<User>> getConsentMedicalGroup(Integer consent_id) {
		Future<List<User>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT id_user FROM retoobesidad.consent_team WHERE id_consent = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(consent_id), res -> {
						conn.result().close();
						if (res.succeeded()) {
							List<User> teamUsers = new ArrayList<>();
							new Handler<Integer>() {
								@Override
								public void handle(Integer idx) {
									if (idx < res.result().getNumRows()) {
										final Handler<Integer> self = this;
										getUserById(res.result().getRows().get(idx).getInteger("id_user"))
												.setHandler(team_user -> {
													if (team_user.succeeded()) {
														teamUsers.add(team_user.result());
														self.handle(idx + 1);
													} else {
														future.fail(team_user.cause());
													}
												});
									} else {
										future.complete(teamUsers);
									}
								}
							}.handle(0);
						} else {
							future.fail(res.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(consent);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<ChatMessage> saveMessageInDatabase(String message, Integer group_id, Integer user_id,
			Long timestamp) {
		Future<ChatMessage> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.chat_group_messages (message,group_id,user_id,timestamp) VALUES (?,?,?,?)",
							new JsonArray().add(message).add(group_id).add(user_id).add(timestamp), res -> {
								conn.result().close();
								if (res.succeeded() && res.result().getKeys().size() > 0) {
									int id_message = res.result().getKeys().getInteger(0);
									ChatMessage msg = new ChatMessage(message, id_message, group_id, user_id,
											timestamp);
									future.complete(msg);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<Boolean> markAsReadMessages(Integer user_id, Integer group_id) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"INSERT INTO retoobesidad.chat_message_state (message_id,user_id,state,timestamp)"
									+ " 	SELECT group_msg.id_message, ?, 'Read', ? "
									+ "			FROM retoobesidad.chat_group_messages AS group_msg"
									+ " 		WHERE group_msg.group_id = ? AND group_msg.user_id != ?"
									+ " 			AND NOT EXISTS ("
									+ "					SELECT state_msg.message_id "
									+ "						FROM retoobesidad.chat_message_state AS state_msg "
									+ " 					WHERE group_msg.id_message = state_msg.message_id"
									+ " 						AND state_msg.user_id = ?"
									+ " 						AND state_msg.state LIKE 'Read');",
							new JsonArray().add(user_id).add(Calendar.getInstance().getTimeInMillis()).add(group_id)
									.add(user_id).add(user_id),
							res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(true);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<ChatMessage>> getUnreadMessages(Integer user_id, Integer group_id) {
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
									List<ChatMessage> messages = new ArrayList<ChatMessage>();
									for (JsonObject msg : res.result().getRows()) {
										messages.add(Json.decodeValue(msg.encode(), ChatMessage.class));
									}
									future.complete(messages);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							new JsonArray().add(group_id).add(fromtimestamp).add(totimestamp), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<ChatMessage> messages = new ArrayList<ChatMessage>();
									for (JsonObject msg : res2.result().getRows()) {
										messages.add(Json.decodeValue(msg.encode(), ChatMessage.class));
									}
									future.complete(messages);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							new JsonArray().add(group_id), res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									ChatMessage message = null;
									if (res2.result().getNumRows() > 0) {
										message = Json.decodeValue(res2.result().getRows().get(0).encode(),
												ChatMessage.class);
									}
									future.complete(message);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<ChatMessageState> addMessageState(Integer message_id, Integer user_id, String state,
			Long timestamp) {
		Future<ChatMessageState> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.chat_message_state (message_id, user_id, state, timestamp) VALUES (?,?,?,?);",
							new JsonArray().add(message_id).add(user_id).add(state).add(timestamp), res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									int message_stat_id = res2.result().getKeys().getInteger(0);
									ChatMessageState msg_state = new ChatMessageState(message_stat_id, message_id,
											user_id, state, timestamp);
									future.complete(msg_state);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									future.complete(testResponse);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							future.complete(responses);
						}
					}
				}.handle(0);
			}
		});

		return future;
	}

	private Future<FirebaseEntity> getUserFirebaseToken(Integer userId) {
		Future<FirebaseEntity> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.firebase_tokens WHERE iduser = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							FirebaseEntity firebase = Json.decodeValue(res2.result().getRows().get(0).encode(),
									FirebaseEntity.class);
							future.complete(firebase);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<FirebaseEntity> saveUserFirebaseToken(FirebaseEntity firebase) {
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
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<User> getUser(Integer userId) {
		Future<User> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.users WHERE iduser = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							User userData = Json.decodeValue(res2.result().getRows().get(0).encode(), User.class);
							future.complete(userData);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<Integer> saveUserImage(Integer userId, String image) {
		Future<Integer> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams("UPDATE retoobesidad.users SET image = ? WHERE iduser = ?;",
							new JsonArray().add(image).add(userId), res -> {
								conn.result().close();
								if (res.succeeded() && res.result().getKeys().size() > 0) {
									future.complete(userId);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
									createGroup(user, "Cirujano bariátrico", "Conversación con el cirujano bariátrico")
											.setHandler(new Handler<AsyncResult<ChatGroup>>() {

												@Override
												public void handle(AsyncResult<ChatGroup> event) {
													addUserToGroup(user.getIduser(), event.result().getIdchat_group(),
															Calendar.getInstance().getTimeInMillis());
													addUserToGroup(3, event.result().getIdchat_group(),
															Calendar.getInstance().getTimeInMillis());
												}
											});
									createGroup(user, "Endocrino", "Conversación con el endocrino")
											.setHandler(new Handler<AsyncResult<ChatGroup>>() {

												@Override
												public void handle(AsyncResult<ChatGroup> event) {
													addUserToGroup(user.getIduser(), event.result().getIdchat_group(),
															Calendar.getInstance().getTimeInMillis());
													addUserToGroup(4, event.result().getIdchat_group(),
															Calendar.getInstance().getTimeInMillis());
												}
											});
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(), 50f,
											Aim.AimType.activeMinutes.ordinal()));
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(),
											1250f, Aim.AimType.caloriesOut.ordinal()));
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(), 0.5f,
											Aim.AimType.distance.ordinal()));
									saveAim(new Aim(user.getIduser(), 0, Calendar.getInstance().getTimeInMillis(),
											3000f, Aim.AimType.steps.ordinal()));
								}

								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									future.complete(user);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<Appointment>> getUserAppointment(Integer userId, Long lastUpdateTimestamp) {
		Future<List<Appointment>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.appointments WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<Appointment> appointments = new ArrayList<Appointment>();
									for (JsonObject bloodGlucose : res2.result().getRows()) {
										appointments.add(Json.decodeValue(bloodGlucose.encode(), Appointment.class));
									}
									future.complete(appointments);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
												+ "iduser,type,status,lastUpdateTimestamp) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "timestamp = IF (lastUpdateTimestamp < ?,?,timestamp),"
												+ "doctor = IF (lastUpdateTimestamp < ?,?, doctor),"
												+ "description = IF (lastUpdateTimestamp < ?,?, description),"
												+ "place = IF (lastUpdateTimestamp < ?,?, place),"
												+ "things = IF (lastUpdateTimestamp < ?,?, things),"
												+ "iduser = IF (lastUpdateTimestamp < ?,?, iduser),"
												+ "type = IF (lastUpdateTimestamp < ?,?, type),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(appointment.getIdAppointment())
												.add(appointment.getTimestamp()).add(appointment.getDoctor())
												.add(appointment.getDescription()).add(appointment.getPlace())
												.add(appointment.getThings()).add(appointment.getIduser())
												.add(appointment.getType()).add(appointment.getStatus())
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
												.add(appointment.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													appointments.get(idx)
															.setIdAppointment(res2.result().getKeys().getInteger(0));
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
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<UserData> getUserData(Integer userId) {
		Future<UserData> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.user_data WHERE user_id = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							UserData userData = Json.decodeValue(res2.result().getRows().get(0).encode(),
									UserData.class);
							future.complete(userData);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<Boolean> deleteUserData(Integer userId) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("DELETE FROM retoobesidad.user_data WHERE user_id = ?",
							new JsonArray().add(userId), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(true);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<Medicine>> getUserMedicine(Integer userId, Long lastUpdateTimestamp) {
		Future<List<Medicine>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicine WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY begin_timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<Medicine> appointments = new ArrayList<Medicine>();
									for (JsonObject medicine : res2.result().getRows()) {
										appointments.add(Json.decodeValue(medicine.encode(), Medicine.class));
									}
									future.complete(appointments);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
												+ "begin_timestamp,end_timestamp,method,dosage,days,idmedicine,lastUpdateTimestamp,status) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE "
												+ "iduser = IF (lastUpdateTimestamp < ?,?,iduser),"
												+ "medicine = IF (lastUpdateTimestamp < ?,?, medicine),"
												+ "observations = IF (lastUpdateTimestamp < ?,?, observations),"
												+ "begin_timestamp = IF (lastUpdateTimestamp < ?,?, begin_timestamp),"
												+ "end_timestamp = IF (lastUpdateTimestamp < ?,?, end_timestamp),"
												+ "method = IF (lastUpdateTimestamp < ?,?, method),"
												+ "dosage = IF (lastUpdateTimestamp < ?,?, dosage),"
												+ "days = IF (lastUpdateTimestamp < ?,?, days),"
												+ "status = IF (lastUpdateTimestamp < ?,?, status),"
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?, lastUpdateTimestamp);",
										new JsonArray().add(medicine.getIduser()).add(medicine.getMedicine())
												.add(medicine.getObservations()).add(medicine.getBeginTimestamp())
												.add(medicine.getEndTimestamp()).add(medicine.getMethod())
												.add(medicine.getDosage()).add(medicine.getDays())
												.add(medicine.getIdmedicine()).add(medicine.getLastUpdateTimestamp())
												.add(medicine.getStatus()).add(medicine.getLastUpdateTimestamp())
												.add(medicine.getIduser()).add(medicine.getLastUpdateTimestamp())
												.add(medicine.getMedicine()).add(medicine.getLastUpdateTimestamp())
												.add(medicine.getObservations()).add(medicine.getLastUpdateTimestamp())
												.add(medicine.getBeginTimestamp())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getEndTimestamp())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getMethod())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getDosage())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getDays())
												.add(medicine.getLastUpdateTimestamp()).add(medicine.getStatus())
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
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<Aim>> getAimsByUser(Integer userId) {
		Future<List<Aim>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.user_aim WHERE iduser = ? ORDER BY timestamp;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							List<Aim> aims = new ArrayList<Aim>();
							for (JsonObject aim : res2.result().getRows()) {
								aims.add(Json.decodeValue(aim.encode(), Aim.class));
							}
							future.complete(aims);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									aim.setIdAim(res2.result().getKeys().getInteger(0));
									future.complete(aim);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<TestResponse>> getTestResults(Integer userId) {
		Future<List<TestResponse>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.test_user_response WHERE user_id = ? ORDER BY timestamp;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							List<TestResponse> messages = new ArrayList<TestResponse>();
							for (JsonObject msg : res2.result().getRows()) {
								messages.add(Json.decodeValue(msg.encode(), TestResponse.class));
							}
							future.complete(messages);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<ChatGroup> createGroup(User user, String name, String description) {
		Future<ChatGroup> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					Long creation_date = Calendar.getInstance().getTimeInMillis();
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.chat_groups (name,creation_date,description) VALUES (?,?,?)",
							new JsonArray().add(name).add(creation_date).add(description), res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									int id_group = res2.result().getKeys().getInteger(0);
									Future<Boolean> f1 = addUserToGroup(user.getIduser(), id_group,
											Calendar.getInstance().getTimeInMillis());
									f1.setHandler(result -> {
										if (result.succeeded()) {
											ChatGroup group = new ChatGroup(id_group, "", creation_date, "");
											future.complete(group);
										} else {
											future.fail(result.cause());
										}
									});
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<ChatGroup>> getUserGroups(Integer user_id) {
		Future<List<ChatGroup>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"SELECT group_id FROM retoobesidad.chat_group_members WHERE user_id = ?;",
							new JsonArray().add(user_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									ResultSet resultSet = res.result();

									List<ChatGroup> groups = new ArrayList<ChatGroup>();
									new Handler<Integer>() {
										@Override
										public void handle(Integer idx) {
											if (idx < resultSet.getNumRows()) {
												final Handler<Integer> self = this;
												getGroupById(resultSet.getRows().get(idx).getInteger("group_id"))
														.setHandler(e -> {
															groups.add((ChatGroup) e.result());
															self.handle(idx + 1);
														});
											} else {
												future.complete(groups);
											}
										}
									}.handle(0);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<User>> getGroupUsers(Integer group_id) {
		Future<List<User>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"SELECT user_id FROM retoobesidad.chat_group_members WHERE group_id = ?;",
							new JsonArray().add(group_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									ResultSet resultSet = res.result();

									List<User> users = new ArrayList<User>();
									new Handler<Integer>() {
										@Override
										public void handle(Integer idx) {
											if (idx < resultSet.getNumRows()) {
												final Handler<Integer> self = this;
												getUserById(resultSet.getRows().get(idx).getInteger("user_id"))
														.setHandler(e -> {
															users.add(e.result());
															self.handle(idx + 1);
														});
											} else {
												future.complete(users);
											}
										}
									}.handle(0);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<ChatGroup> getGroupById(Integer group_id) {
		Future<ChatGroup> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM retoobesidad.chat_groups WHERE idchat_group = ?;",
							new JsonArray().add(group_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									if (res.result().getNumRows() > 0) {
										future.complete(Json.decodeValue(res.result().getRows().get(0).encode(),
												ChatGroup.class));
									} else {
										future.complete();
									}
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<User>> getRelatedUsers(Integer user_id) {
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
									List<User> users = new ArrayList<User>();
									for (JsonObject msg : res.result().getRows()) {
										users.add(Json.decodeValue(msg.encode(), User.class));
									}
									future.complete(users);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<Boolean> addUserToGroup(Integer user_id, Integer group_id, Long timestamp) {
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
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<Boolean> deleteUserFromGroup(Integer user_id, Integer group_id) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"DELETE FROM retoobesidad.chat_group_members WHERE user_id = ? AND group_id = ?",
							new JsonArray().add(user_id).add(group_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									future.complete(true);
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<User> getUserById(Integer user_id) {
		Future<User> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM retoobesidad.users WHERE iduser = ?;",
							new JsonArray().add(user_id), res -> {
								conn.result().close();
								if (res.succeeded()) {
									ResultSet resultSet = res.result();
									future.complete(
											Json.decodeValue(Json.encode(resultSet.getRows().get(0)), User.class));
								} else {
									future.fail(res.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});
		return future;
	}

	private Future<List<Weight>> getHealthWeight(Integer userId, Long lastUpdateTimestamp) {
		Future<List<Weight>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.weight WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<Weight> weights = new ArrayList<Weight>();
									for (JsonObject medicine : res2.result().getRows()) {
										weights.add(Json.decodeValue(medicine.encode(), Weight.class));
									}
									future.complete(weights);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							future.complete(weights);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<BloodPressure>> getBloodPressure(Integer userId, Long lastUpdateTimestamp) {
		Future<List<BloodPressure>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.blood_pressure WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<BloodPressure> bloodPressures = new ArrayList<BloodPressure>();
									for (JsonObject bloodPressure : res2.result().getRows()) {
										bloodPressures
												.add(Json.decodeValue(bloodPressure.encode(), BloodPressure.class));
									}
									future.complete(bloodPressures);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							future.complete(bloodPressures);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<BloodGlucose>> getBloodGlucose(Integer userId, Long lastUpdateTimestamp) {
		Future<List<BloodGlucose>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.blood_glucose WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<BloodGlucose> bloodGlucoses = new ArrayList<BloodGlucose>();
									for (JsonObject bloodGlucose : res2.result().getRows()) {
										bloodGlucoses.add(Json.decodeValue(bloodGlucose.encode(), BloodGlucose.class));
									}
									future.complete(bloodGlucoses);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							future.complete(bloodGlucoses);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<HeartRate>> getHeartRate(Integer userId, Long lastUpdateTimestamp) {
		Future<List<HeartRate>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.heart_rate WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
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
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							List<HeartRateZone> heartRateZones = new ArrayList<HeartRateZone>();
							for (JsonObject heartRateZone : res2.result().getRows()) {
								heartRateZones.add(Json.decodeValue(heartRateZone.encode(), HeartRateZone.class));
							}
							future.complete(heartRateZones);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							future.complete(heartRates);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
										futures.get(idx).fail(res2.cause());
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
					future.fail(handler.cause());
				}
			});
		} catch (Exception e) {
			future.fail(e.getCause());
		}
		return future;
	}

	private Future<List<Summary>> getActivitySummary(Integer userId, Long lastUpdateTimestamp) {
		Future<List<Summary>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.blood_glucose WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<Summary> summaries = new ArrayList<Summary>();
									for (JsonObject summary : res2.result().getRows()) {
										Summary summaryRes = Json.decodeValue(summary.encode(), Summary.class);
										summaryRes.setDistances(Arrays.asList(
												Json.decodeValue(summary.getString("distances"), Distance[].class)));
										summaries.add(summaryRes);
									}
									future.complete(summaries);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<List<Summary>> saveActivitySummary(List<Summary> summaries, Integer userId) {
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
							future.complete(summaries);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<MedicalTestEntity>> getMedicalTest(Integer userId, Long lastUpdateTimestamp) {
		Future<List<MedicalTestEntity>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicaltest WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<MedicalTestEntity> medicalTest = new ArrayList<MedicalTestEntity>();
									for (JsonObject medicine : res2.result().getRows()) {
										medicalTest.add(Json.decodeValue(medicine.encode(), MedicalTestEntity.class));
									}
									future.complete(medicalTest);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
												+ "timestampDone,timestampResults,picturePath,timestampCite,placeCite,doctorCite, status, createdBy) "
												+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
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
												+ "lastUpdateTimestamp = IF (lastUpdateTimestamp < ?,?,lastUpdateTimestamp);",
										new JsonArray().add(test.getIdMedicalTest()).add(test.getIduser())
												.add(test.getPrescriber()).add(test.getPrescriberComment())
												.add(test.getLastUpdateTimestamp()).add(test.getName())
												.add(test.getDescription()).add(test.getTimestamp())
												.add(test.getTimestampDone()).add(test.getTimestampResults())
												.add(test.getPicturePath()).add(test.getTimestampCite())
												.add(test.getPlaceCite()).add(test.getDoctorCite())
												.add(test.getStatus()).add(test.getCreatedBy())
												.add(test.getLastUpdateTimestamp()).add(test.getIduser())
												.add(test.getLastUpdateTimestamp()).add(test.getPrescriber())
												.add(test.getLastUpdateTimestamp()).add(test.getPrescriberComment())
												.add(test.getLastUpdateTimestamp()).add(test.getName())
												.add(test.getLastUpdateTimestamp()).add(test.getDescription())
												.add(test.getLastUpdateTimestamp()).add(test.getTimestamp())
												.add(test.getLastUpdateTimestamp()).add(test.getTimestampDone())
												.add(test.getLastUpdateTimestamp()).add(test.getTimestampResults())
												.add(test.getLastUpdateTimestamp()).add(test.getPicturePath())
												.add(test.getLastUpdateTimestamp()).add(test.getTimestampCite())
												.add(test.getLastUpdateTimestamp()).add(test.getPlaceCite())
												.add(test.getLastUpdateTimestamp()).add(test.getDoctorCite())
												.add(test.getLastUpdateTimestamp()).add(test.getStatus())
												.add(test.getLastUpdateTimestamp()).add(test.getCreatedBy())
												.add(test.getLastUpdateTimestamp()).add(test.getLastUpdateTimestamp()),
										res2 -> {
											if (res2.succeeded() && res2.result().getKeys().size() > 0) {
												if (res2.result().getKeys().getInteger(0) > 0)
													medicalTest.get(idx)
															.setIdMedicalTest(res2.result().getKeys().getInteger(0));
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
							future.complete(medicalTest);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}

	private Future<List<Doctor>> getDoctor(Integer userId) {
		Future<List<Doctor>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.doctor ORDER BY surname DESC;";
					conn.result().queryWithParams(select, new JsonArray(), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							List<Doctor> doctors = new ArrayList<Doctor>();
							for (JsonObject doctor : res2.result().getRows()) {
								doctors.add(Json.decodeValue(doctor.encode(), Doctor.class));
							}
							future.complete(doctors);
						} else {
							future.fail(res2.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
			}
		});

		return future;
	}

	private Future<List<ChallengeEntity>> getChallenge(Integer userId, Long lastUpdateTimestamp) {
		Future<List<ChallengeEntity>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.challenge WHERE iduser = ? AND lastUpdateTimestamp >= ? ORDER BY startTimestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId).add(lastUpdateTimestamp),
							res2 -> {
								conn.result().close();
								if (res2.succeeded()) {
									List<ChallengeEntity> challenges = new ArrayList<ChallengeEntity>();
									for (JsonObject challenge : res2.result().getRows()) {
										challenges.add(Json.decodeValue(challenge.encode(), ChallengeEntity.class));
									}
									future.complete(challenges);
								} else {
									future.fail(res2.cause());
								}
							});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
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
							future.complete(challenges);
						} else {
							future.fail(handler.cause());
						}
					});
				} catch (Exception e) {
					future.fail(e.getCause());
					conn.result().close();
				}
			} else {
				future.fail(conn.cause());
				conn.result().close();
			}
		});
		return future;
	}
}