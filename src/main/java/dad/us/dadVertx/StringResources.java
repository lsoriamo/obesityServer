package dad.us.dadVertx;

import java.util.Arrays;
import java.util.List;

import scala.util.Random;


public class StringResources {

	private static List<String> watsonNoResponse = Arrays.asList("Lo siento, no s� responderte a esa pregunta :-(", "No entiendo la pregunta", "Ahora mismo no tengo ninguna respuesta para esa cuesti�n", "Lo siento, no s� la respuesta", "Vaya, pues no s� responderte a esa pregunta", "No te he entendido, �puedes hacerme la pregunta de otro modo?");
	
	public static String getRandomWatsonNoResponse() {
		return watsonNoResponse.get(new Random().nextInt(watsonNoResponse.size()));
	}
	
	public static String virtualSurgeryName = "Cirujano bari�trico";
	public static String virtualSurgeryChatDescription = "Conversaci�n con el cirujano bari�trico";
	public static Long virtualSurgeryId = 3L;
	public static String virtualEndocrineName = "Endocrino";
	public static String virtualEndocrineChatDescription = "Conversaci�n con el endocrino";
	public static Long virtualEndocrineId = 4L;
	public static Integer numMaxMessagesQuery = 50;
	public static String consentMedicalTestExtension = "jpg";
	public static String consentMedicalTestFolder = "medical";
	public static String consentMedicalTestPrefix = "test-";
	public static String consentUserImageFolder = "images";
	public static String consentSignatureFolder = "consents";
	public static String consentSignaturePrefix = "consent-";
	public static String consentSignatureExtension = ".svg";
	public static String restResponseHeaderContentType = "application/json; charset=utf-8";
	public static String restResponseHeaderContentTypeImage = "image";
	
	public static Float defaultAimActiveMinutes = 50f;
	public static Float defaultAimCaloriesOut = 1250f;
	public static Float defaultAimDistance = 0.5f;
	public static Float defaultAimSteps = 3000f;
	
}
