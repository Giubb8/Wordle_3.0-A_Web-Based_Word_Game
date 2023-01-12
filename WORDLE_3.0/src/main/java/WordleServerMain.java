import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import static java.util.concurrent.Executors.newCachedThreadPool;

/*
* @author: Andrea Carollo
* https://github.com/Giubb8
*/



/* SERVER DI WORDLE 3.0*/
public class WordleServerMain {
    /* COSTANTI PER LA DESCRIZIONE DEL SERVER */
    private static int PORT_NUMBER;
    private static int NUM_THREAD;
    private static int WORD_DURATION;
    private static int REGISTRY_PORT;
    private static String SERVER_NAME;
    private static int UDP_PORT;
    private static final String path_to_wordsfile="../resources/words.txt";


    public static void main(String[] args) throws IOException, AlreadyBoundException {
        /* UTILITIES */
        List<String> logged_user= (List<String>) Collections.synchronizedList(new ArrayList<String>()); //LISTA PER CONTENERE GLI UTENTI LOGGATI
        String configfile_path=args[0];
        StringBuffer secretword=new StringBuffer();
        ThreadPoolExecutor threadpool=(ThreadPoolExecutor) newCachedThreadPool();
        Hashtable<String,ArrayList<String>> playedwords=new Hashtable<>();//TODO FORSE POSSO ELIMINARE
        RegisterServiceImpl registerService=new RegisterServiceImpl(PORT_NUMBER,REGISTRY_PORT,SERVER_NAME,playedwords);//Oggetto per la registrazione degli utenti tramite RMI

        /* CREO E RIEMPIO LA CLASSIFICA */
        String ranking_path="../resources/Ranking.json";
        JsonReader reader = new JsonReader(new FileReader(ranking_path));
        PlayerRankingComparator comparator=new PlayerRankingComparator();
        TreeSet<Player> treeSet=new Gson().fromJson(reader, new TypeToken<TreeSet<Player>>() {}.getType());
        SortedSet<Player> ranking = Collections.synchronizedSortedSet(new TreeSet<Player>(comparator));
        ranking.addAll(treeSet);

        /* CONFIGURO IL DATAGRAMSOCKET PER IL MULTICAST */
        DatagramSocket ms=new DatagramSocket(UDP_PORT);
        ms.setReuseAddress(true);

        /* CONFIGURO IL SERVER E FACCIO PARTIRE RMI PER LA REGISTRAZIONE  */
        configserver(configfile_path);
        RMI_register_start(registerService);
        ServerCallBackImpl callback=handlecallback();

        /* CREO IL SOCKET PER LA COMUNICAZIONE */
        try (ServerSocket server = new ServerSocket(PORT_NUMBER)) {
            /* CREO E AVVIO IL MANAGER PER LA PAROLA SEGRETA */
            SecretWordManager sw_manager = new SecretWordManager(secretword, WORD_DURATION);
            sw_manager.start();
            List<String> words= (List<String>) Collections.synchronizedList(new ArrayList<String>());
            words.addAll(sw_manager.txt_to_list(path_to_wordsfile));
            /* STA IN ATTESA DELLE CONNESSIONI CON I CLIENT */
            while (true) {
                threadpool.execute(new WordleClientHandler(server.accept(),registerService,secretword,playedwords,words,ms,ranking,callback,logged_user));
            }
        }
        /* FINE DEL MAIN */
    }

    /* FUNZIONE PER GESTIRE LE CALLBACK CON I CLIENT
    * @return: server= Implementazione dell'interfaccia "ServerCallBackInterface", per gestione servizio RMI CallBack
    * @throws: RemoteException - Esportazione dello stub fallimentare
     * @throws: AlreadyBoundException - Nome pre esistente nel registry
    * */
    private static ServerCallBackImpl handlecallback() throws RemoteException, AlreadyBoundException {
        String name = "Server";
        ServerCallBackImpl server = new ServerCallBackImpl( );
        ServerCallBackInterface stub=(ServerCallBackInterface) UnicastRemoteObject.exportObject (server,39000);
        LocateRegistry.createRegistry(5000);
        Registry registry=LocateRegistry.getRegistry(5000);
        registry.bind(name, stub);
        return server;
    }

    /*
    * FUNZIONE PER LA CONFIGURAZIONE INIZIALE DEL SERVER
    * @param: configfile_path= String contenente il path del file di configurazione del Server
    * @throws: FileNotFoundException - file di configurazione non trovato
    * @throws: IoException - errore nella lettura dell'input stream
    * */
    static public void configserver(String configfile_path){
        try (InputStream input = new FileInputStream(configfile_path)){
            Properties prop = new Properties();
            prop.load(input);// faccio il loading del file.properties
            PORT_NUMBER=Integer.parseInt(prop.getProperty("port_number"));
            NUM_THREAD=Integer.parseInt(prop.getProperty("num_thread"));
            WORD_DURATION=Integer.parseInt(prop.getProperty("word_duration"));
            REGISTRY_PORT=Integer.parseInt(prop.getProperty("registry_port"));
            UDP_PORT=Integer.parseInt(prop.getProperty("udp_port"));
            SERVER_NAME=prop.getProperty("server_name");
            System.out.println("PORT_NUMBER:"+PORT_NUMBER+"\nNUM_THREAD:"+NUM_THREAD+"\nWORD_DURATION:"+WORD_DURATION+"\nREGISTRY_PORT:"+REGISTRY_PORT+"\nSERVER_NAME:"+SERVER_NAME); // stampo il valore delle proprieta
        } catch (IOException ex) {
            System.out.println("IOException sul file di configurazione:");
            ex.printStackTrace();
        }
    }


     /*
     * FUNZIONE PER GESTIRE LA REGISTRAZIONE DEGLI UTENTI TRAMITE RMI
     * @param: registerService= Implementazione dell'interfaccia RegisterService
     * @throws: RemoteException - Esportazione dello stub fallimentare
     * */
    static public void RMI_register_start(RegisterServiceImpl registerService){
        try {
            //creo una istanza dell'oggetto remoto
            //esporto l'oggetto
            RegisterService stub=(RegisterService) UnicastRemoteObject.exportObject(registerService,0);
            //creo un registry nuovo sulla porta adatta
            LocateRegistry.createRegistry(REGISTRY_PORT);
            Registry registry=LocateRegistry.getRegistry(REGISTRY_PORT);
            registry.rebind(SERVER_NAME,stub);
        }
        catch (RemoteException e){
            System.out.println("Communication error " + e);
        }
    }
}
