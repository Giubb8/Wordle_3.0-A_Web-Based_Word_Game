import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TranslatorTask implements Callable<String> {
    String str_to_translate;

    public TranslatorTask(String str_to_translate){
        this.str_to_translate=str_to_translate;
    }

    public String call() throws Exception {
        URL url= new URL("https://api.mymemory.translated.net/get?q=" + str_to_translate + "&langpair=en|it");
        String traduzione=new String();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder inputLine = new StringBuilder();
            String reader;

            while ((reader = in.readLine()) != null) {
                inputLine.append(reader);
            }

            /* CREAZIONE DEGLI OGGETTI JSON */
            JSONObject jsonObject;
            JSONParser parser = new JSONParser();

            try {
                jsonObject = (JSONObject) parser.parse(inputLine.toString());
                JSONArray array = (JSONArray) jsonObject.get("matches");
                ArrayList<String> tmpArray = new ArrayList<String>(array.size());
                //Prendo il JSON ricevuto e per ogni elemento prendo solamente la traduzione e la inserisco nella ArrayList
                for (Object o : array) {/* TODO CONTROLLARE SE POSSO LEVARE QUESTO FOR E SEMPLICEMENTE FARE TRADUZIONE=*/
                    JSONObject obj = (JSONObject) o;
                    traduzione = (String) obj.get("translation");
                    System.out.println(traduzione+""+obj.toString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace(System.err);
        }
        return traduzione;
    }


}
