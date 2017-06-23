package dad.us.dadVertx;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.request.CollectionAdminRequest.CreateAlias;

import dad.us.dadVertx.entities.activities.Aim;
import dad.us.dadVertx.entities.appointment.Appointment;
import dad.us.dadVertx.entities.chat.ChatGroup;
import dad.us.dadVertx.entities.chat.ChatMessage;
import dad.us.dadVertx.entities.chat.ChatMessageState;
import dad.us.dadVertx.entities.chat.ChatMessageState.MessageState;
import dad.us.dadVertx.entities.consent.Consent;
import dad.us.dadVertx.entities.medicine.Medicine;
import dad.us.dadVertx.entities.medicine.MedicineDosage;
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
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ChatServer extends AbstractVerticle {

	private static AsyncSQLClient mySQLClient;

	@Override
	public void start() throws Exception {

		JsonObject config = new JsonObject().put("host", "localhost").put("username", "root").put("password", "root")
				.put("database", "retoobesidad").put("port", 3306).put("maxPoolSize", 100);
		mySQLClient = MySQLClient.createShared(vertx, config);

		Router router = Router.router(vertx);

		router.route("/api/obesity/*").handler(BodyHandler.create());

		router.post("/api/obesity/consent/upload").handler(this::postUploadSignature);

		router.get("/api/obesity/user/login/:userId").handler(this::getUser);
		router.post("/api/obesity/user/login").handler(this::saveUser);

		router.get("/api/obesity/user/data/:userId").handler(this::getUserData);
		router.post("/api/obesity/user/data").handler(this::saveUserData);
		router.delete("/api/obesity/user/data/:userId").handler(this::deleteUserData);

		router.get("/api/obesity/appointment/:userId").handler(this::getUserAppointment);
		router.post("/api/obesity/appointment").handler(this::saveUserAppointment);
		router.delete("/api/obesity/appointment/:appointmentId").handler(this::deleteUserAppointment);

		router.post("/api/obesity/medicine/drug").handler(this::saveMedicine);
		router.get("/api/obesity/medicine/drug/:userId").handler(this::getMedicinesByUser);
		router.delete("/api/obesity/medicine/drug/:idMedicine").handler(this::deleteMedicine);

		router.post("/api/obesity/medicine/dosage").handler(this::saveDosage);
		router.get("/api/obesity/medicine/dosage/:idMedicine").handler(this::getDosageByMedicine);
		router.delete("/api/obesity/medicine/dosage/:dosageId").handler(this::deleteDosage);

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
		 * new JsonObject( Json.encode(new ChatMessage("Mensaje a las " +
		 * Calendar.getInstance().getTime().toString(), 0, 32, 2,
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
		getUserAppointment(userId).setHandler(res -> {
			if (res.succeeded()) {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(res.result()));
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveUserAppointment(RoutingContext routingContext) {
		Appointment appointment = Json.decodeValue(routingContext.getBodyAsString(), Appointment.class);
		saveUserAppointment(appointment).setHandler(new Handler<AsyncResult<Appointment>>() {
			@Override
			public void handle(AsyncResult<Appointment> event) {
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

	private void deleteUserAppointment(RoutingContext routingContext) {
		Integer appointmentId = new Integer(routingContext.request().getParam("appointmentId"));
		deleteUserAppointment(appointmentId).setHandler(new Handler<AsyncResult<Boolean>>() {
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
														.end("{\"result\":\"fail\"}");
											}
										}
									});
								} else {
									routingContext.response()
											.putHeader("content-type", "application/json; charset=utf-8")
											.end("{\"result\":\"fail\"}");
								}
							}
						});
					}
				} catch (Exception e) {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.end("{\"result\":\"fail\"}");
				}
			});
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.end("{\"result\":\"fail\"}");
		}
	}

	private void getMedicinesByUser(RoutingContext routingContext) {
		Integer user_id = new Integer(routingContext.request().getParam("userId"));
		getMedicinesByUser(user_id).setHandler(res -> {
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
		Medicine medicine = Json.decodeValue(routingContext.getBodyAsString(), Medicine.class);
		saveMedicine(medicine).setHandler(new Handler<AsyncResult<Medicine>>() {
			@Override
			public void handle(AsyncResult<Medicine> event) {
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

	private void getDosageByMedicine(RoutingContext routingContext) {
		Integer idMedicine = new Integer(routingContext.request().getParam("idMedicine"));
		getDosageByMedicine(idMedicine).setHandler(res -> {
			if (res.succeeded()) {
				JsonArray array = new JsonArray(res.result());
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
						.end(array.encodePrettily());
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	}

	private void saveDosage(RoutingContext routingContext) {
		MedicineDosage dosage = Json.decodeValue(routingContext.getBodyAsString(), MedicineDosage.class);
		saveDosage(dosage).setHandler(new Handler<AsyncResult<MedicineDosage>>() {
			@Override
			public void handle(AsyncResult<MedicineDosage> event) {
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

	private void deleteMedicine(RoutingContext routingContext) {
		Integer idMedicine = new Integer(routingContext.request().getParam("idMedicine"));
		deleteMedicine(idMedicine).setHandler(new Handler<AsyncResult<Boolean>>() {
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

	private void deleteDosage(RoutingContext routingContext) {
		Integer dosageId = new Integer(routingContext.request().getParam("dosageId"));
		deleteDosage(dosageId).setHandler(new Handler<AsyncResult<Boolean>>() {
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
		Integer userId = new Integer(routingContext.request().getParam("userId"));
		// TODO: es posible enviar un test distinto en función del usuario
		routingContext.response().putHeader("content-type", "application/json; charset=ascii")
				.sendFile("testpsicologico.json");
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

	private Future<List<Appointment>> getUserAppointment(Integer userId) {
		Future<List<Appointment>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.appointments WHERE iduser = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							List<Appointment> appointments = new ArrayList<Appointment>();
							for (JsonObject appointment : res2.result().getRows()) {
								appointments.add(Json.decodeValue(appointment.encode(), Appointment.class));
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

	private Future<Appointment> saveUserAppointment(Appointment appointment) {
		Future<Appointment> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.appointments (idappointment, timestamp,doctor,description,place,things,"
									+ "iduser,type) " + "VALUES (?,?,?,?,?,?,?,?) "
									+ "ON DUPLICATE KEY UPDATE idappointment = ?, timestamp = ?, "
									+ "doctor = ?, description = ?, place = ?, things = ?, iduser = ?, " + "type = ?",
							new JsonArray().add(appointment.getIdAppointment()).add(appointment.getTimestamp())
									.add(appointment.getDoctor()).add(appointment.getDescription())
									.add(appointment.getPlace()).add(appointment.getThings())
									.add(appointment.getIduser()).add(appointment.getType())
									.add(appointment.getIdAppointment()).add(appointment.getTimestamp())
									.add(appointment.getDoctor()).add(appointment.getDescription())
									.add(appointment.getPlace()).add(appointment.getThings())
									.add(appointment.getIduser()).add(appointment.getType()),
							res2 -> {
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									conn.result().close();
									appointment.setIduser(res2.result().getKeys().getInteger(0));
									future.complete(appointment);
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

	private Future<Boolean> deleteUserAppointment(Integer appointmentId) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("DELETE FROM retoobesidad.appointments WHERE idappointment = ?",
							new JsonArray().add(appointmentId), res -> {
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
					System.out.println(userData.toString());
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
									.add(userData.getFecha_primer_uso_app()).add(userData.getPeso_objetivo()).add(userData.getNacimiento())
									.add(userData.getAltura()).add(userData.getPeso()).add(userData.getHipertension())
									.add(userData.getDiabetes()).add(userData.getApnea())
									.add(userData.getLesion_articular()).add(userData.getHiperlipidemia())
									.add(userData.getVesicula()).add(userData.getHigado())
									.add(userData.getOsteoporosis()).add(userData.getCardiaca())
									.add(userData.getEjercicio()).add(userData.getFecha_intervencion())
									.add(userData.getFecha_primer_uso_app()).add(userData.getPeso_objetivo()),
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

	private Future<List<Medicine>> getMedicinesByUser(Integer userId) {
		Future<List<Medicine>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicine WHERE iduser = ? ORDER BY end_timestamp DESC;";
					conn.result().queryWithParams(select, new JsonArray().add(userId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							List<Medicine> medicines = new ArrayList<Medicine>();
							for (JsonObject medicine : res2.result().getRows()) {
								medicines.add(Json.decodeValue(medicine.encode(), Medicine.class));
							}
							future.complete(medicines);
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

	private Future<List<MedicineDosage>> getDosageByMedicine(Integer medicineId) {
		Future<List<MedicineDosage>> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					String select = "SELECT * FROM retoobesidad.medicine_dosage WHERE idmedicine = ?;";
					conn.result().queryWithParams(select, new JsonArray().add(medicineId), res2 -> {
						conn.result().close();
						if (res2.succeeded()) {
							List<MedicineDosage> dosages = new ArrayList<MedicineDosage>();
							for (JsonObject dosage : res2.result().getRows()) {
								dosages.add(Json.decodeValue(dosage.encode(), MedicineDosage.class));
							}
							future.complete(dosages);
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

	private Future<Medicine> saveMedicine(Medicine medicine) {
		Future<Medicine> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.medicine (iduser,medicine,observations,"
									+ "begin_timestamp,end_timestamp,each_days,mon,tue,wed,thu,fri,sat,sun) "
									+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new JsonArray().add(medicine.getIduser()).add(medicine.getMedicine())
									.add(medicine.getObservations()).add(medicine.getBegin_timestamp())
									.add(medicine.getEnd_timestamp()).add(medicine.getEach_days())
									.add(medicine.getMon()).add(medicine.getTue()).add(medicine.getWed())
									.add(medicine.getThu()).add(medicine.getFri()).add(medicine.getSat())
									.add(medicine.getSun()),
							res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									medicine.setIdmedicine(res2.result().getKeys().getInteger(0));
									future.complete(medicine);
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

	private Future<MedicineDosage> saveDosage(MedicineDosage dosage) {
		Future<MedicineDosage> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().updateWithParams(
							"INSERT INTO retoobesidad.medicine_dosage (idmedicine,time_hour,time_minutes,"
									+ "dosage,unit) " + "VALUES (?,?,?,?,?)",
							new JsonArray().add(dosage.getIdmedicine()).add(dosage.getTime_hour())
									.add(dosage.getTime_minutes()).add(dosage.getDosage()).add(dosage.getUnit()),
							res2 -> {
								conn.result().close();
								if (res2.succeeded() && res2.result().getKeys().size() > 0) {
									dosage.setIdmedicine(res2.result().getKeys().getInteger(0));
									future.complete(dosage);
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

	private Future<Boolean> deleteMedicine(Integer medicineId) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("DELETE FROM retoobesidad.medicine WHERE idmedicine = ?",
							new JsonArray().add(medicineId), res -> {
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

	private Future<Boolean> deleteDosage(Integer dosageId) {
		Future<Boolean> future = Future.future();
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams(
							"DELETE FROM retoobesidad.medicine_dosage WHERE idmedicine_dosage = ?",
							new JsonArray().add(dosageId), res -> {
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
}