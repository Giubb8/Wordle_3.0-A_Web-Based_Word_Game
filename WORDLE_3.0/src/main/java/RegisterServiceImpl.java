import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;

/* CLASSE CHE IMPLEMENTA L'INTERFACCIA PER LA REGISTRAZIONE DEGLI UTENTI VIA RMI */
public class RegisterServiceImpl implements RegisterService{
    /* OGGETTI  E COSTANTI DI SUPPORTO  */
    final private int PORT_NUMBER;
    final private int REGISTRY_PORT;
    final private String SERVER_NAME;
    final private int ERROR_CODE=13001;
    final private int OK_CODE=12002;

    private final Hashtable<String,Player> players_table;
    private final Player testplayer=new Player("FrancoMicalizzi","Stridio",0);
    private final Gson gson=new GsonBuilder().setPrettyPrinting().create();
    private final String playersdata_path="src/main/resources/PlayersData.json";
    private Hashtable<String, ArrayList<String>> playedwords;

    public RegisterServiceImpl(int PORT_NUMBER,int REGISTRY_PORT,String SERVER_NAME,Hashtable<String,ArrayList<String>> playedwords) throws RemoteException, FileNotFoundException {
        /* INIZIALIZZO LA HASHTABLE PER RIPRENDERE LO STATO PRECEDENTE DAL FILE .JSON */
        JsonReader reader = new JsonReader(new FileReader(playersdata_path));
        //problemi con parsing del tipo a runtime quindi ho dovuto mettere player
        this.players_table= new Gson().fromJson(reader, new TypeToken<Hashtable<String, Player>>() {}.getType());

        /* INIZIALIZZO GLI ALTRI VALORI  */
        this.PORT_NUMBER=PORT_NUMBER;
        this.REGISTRY_PORT=REGISTRY_PORT;
        this.SERVER_NAME=SERVER_NAME;
        //System.out.println("APPENA INIZIALIZZATO: "+players_table);
        this.playedwords=playedwords;
    }

    /* FUNZIONE USATA PER LA REGISTRAZIONE DEGLI UTENTI  */
    public int register_user(Player newplayer) throws RemoteException {

        System.out.println();
        /* INSERISCO IL NUOVO GIOCATORE NELLA TABELLA  */
        /** CONTROLLO CHE NON SI SIA GIA REGISTRATO UN UTENTE CON LO STESSO USERNAME */
        if( (players_table.get(newplayer.getUsername()))==null){
            players_table.put(newplayer.getUsername(), newplayer);
            playedwords.put(newplayer.getUsername(),new ArrayList<String>());
            System.out.println(playedwords);
            //System.out.println(players_table);

            /* AGGIORNO IL FILE .JSON CON IL NUOVO STATO DELLA HASHTABLE */
            Gson gson=new GsonBuilder().setPrettyPrinting().create();
            try {
                FileOutputStream fos=new FileOutputStream(playersdata_path);
                OutputStreamWriter ow=new OutputStreamWriter(fos);
                String playertableString=gson.toJson(players_table);
                ow.write(playertableString);
                ow.flush();
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return OK_CODE;
        }
        else
            return ERROR_CODE;
    }

    /* GETTER HASHTABLE */
    public Hashtable<String, Player> getPlayers_table() {
        return players_table;
    }

    /* FUNZIONE PER L'UPDATE DELLE VARIABILI DEL PLAYER */
    public void updateplayer(String player,String word,int score){

        players_table.get(player).getPlayedwords().add(word);

        /* CONTROLLO DEL RISULTATO */
        if(score!=0)
            players_table.get(player).addwinnedgame(score);
        else
            players_table.get(player).addplayedgame(score);

        /* SCRITTURA SUL FILE */
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        try {
            FileOutputStream fos=new FileOutputStream(playersdata_path);
            OutputStreamWriter ow=new OutputStreamWriter(fos);
            String playertableString=gson.toJson(players_table);
            ow.write(playertableString);
            ow.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
