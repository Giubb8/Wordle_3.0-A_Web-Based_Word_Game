import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

/* CLASSE CHE IMPLEMENTA L'INTERFACCIA PER LA REGISTRAZIONE DEGLI UTENTI VIA RMI */
public class RegisterServiceImpl implements RegisterService{
    /* OGGETTI  E COSTANTI DI SUPPORTO  */
    final private int PORT_NUMBER;
    final private int REGISTRY_PORT;
    final private String SERVER_NAME;

    private Hashtable<String,Player> players_table;
    private Player testplayer=new Player("FrancoMicalizzi","Stridio",0);
    private Gson gson=new GsonBuilder().setPrettyPrinting().create();
    private String playersdata_path="src/main/resources/PlayersData.json";

    public RegisterServiceImpl(int PORT_NUMBER,int REGISTRY_PORT,String SERVER_NAME) throws RemoteException, FileNotFoundException {
        /* Inizializzo la HashTable per riprendere lo stato precedente dal file .json*/
        JsonReader reader = new JsonReader(new FileReader(playersdata_path));
        //problemi con parsing del tipo a runtime quindi ho dovuto mettere player
        this.players_table= new Gson().fromJson(reader, new TypeToken<Hashtable<String, Player>>() {}.getType());

        /* Inizializzo gli altri valori */
        this.PORT_NUMBER=PORT_NUMBER;
        this.REGISTRY_PORT=REGISTRY_PORT;
        this.SERVER_NAME=SERVER_NAME;
        System.out.println("APPENA INIZIALIZZATO: "+players_table);
    }

  /*  public void register_user(String username, String password) throws RemoteException {
        Player newplayer=new Player(username,password,0);
        players_table.put(username,newplayer);
        //inserire nel file json
        System.out.println(players_table);
    }*/

    /* Funzione utilizzata per registrare un utente */
    public Player register_user(Player newplayer) throws RemoteException {
        //Player newplayer=new Player(username,password,0);
        //inserire nel file json

        /* Inserisco il nuovo giocatore nella tabella */
        players_table.put(newplayer.getUsername(), newplayer);
        System.out.println(players_table);
        /* Aggiorno il file .json con il nuovo stato della Hashtable */
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
        // TODO poi levare il fatto che returno il giocatore,per ora lascio per verifica
        return newplayer;
    }

    public Hashtable<String, Player> getPlayers_table() {
        return players_table;
    }
}
