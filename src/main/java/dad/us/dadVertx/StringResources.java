package dad.us.dadVertx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.util.Random;


public class StringResources {

	private static List<String> watsonNoResponse = Arrays.asList("Lo siento, no sé responderte a esa pregunta :-(", "No entiendo la pregunta", "Ahora mismo no tengo ninguna respuesta para esa cuestión", "Lo siento, no sé la respuesta", "Vaya, pues no sé responderte a esa pregunta", "No te he entendido, ¿puedes hacerme la pregunta de otro modo?");
	
	public static String getRandomWatsonNoResponse() {
		return watsonNoResponse.get(new Random().nextInt(watsonNoResponse.size()));
	}
	
}
