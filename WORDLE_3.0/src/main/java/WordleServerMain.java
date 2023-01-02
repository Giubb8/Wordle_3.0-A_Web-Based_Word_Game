import java.io.*;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import static java.util.concurrent.Executors.newCachedThreadPool;


/* SERVER DI WORDLE 3.0*/
public class WordleServerMain {
    /* COSTANTI PER LA DESCRIZIONE DEL SERVER */
    public static int PORT_NUMBER;
    public static int NUM_THREAD;
    public static int WORD_DURATION;
    public static int REGISTRY_PORT;
    public static String SERVER_NAME;
    public static final String path_to_wordsfile="src/main/resources/words.txt";

    public static void main(String[] args) throws IOException {

        String configfile_path=args[0];
        StringBuilder secretword=new StringBuilder();
        ThreadPoolExecutor threadpool=(ThreadPoolExecutor) newCachedThreadPool();
        //TODO FORSE POSSO TRASFORMARE QUESTA HASHTABLE IN UNA HASHTABLE CONTENTENTE TIPO LE STATISTICHE DI OGNI GIOCATORE METTENDO UN OGGETTO AL POSTO DI ARRAYLIST
        Hashtable<String,ArrayList<String>> playedwords=new Hashtable<>();
        RegisterServiceImpl registerService=new RegisterServiceImpl(PORT_NUMBER,REGISTRY_PORT,SERVER_NAME,playedwords);//Oggetto per la registrazione degli utenti tramite RMI

        /* CONFIGURO IL SERVER E FACCIO PARTIRE RMI PER LA REGISTRAZIONE  */
        configserver(configfile_path);
        RMI_register_start(registerService);
        try (ServerSocket server = new ServerSocket(PORT_NUMBER)) {

            /* CREO E AVVIO IL MANAGER PER LA PAROLA SEGRETA */
            SecretWordManager sw_manager = new SecretWordManager(secretword, WORD_DURATION);
            sw_manager.start();
            ArrayList<String> words = sw_manager.txt_to_list(path_to_wordsfile);
            /* STA IN ATTESA DELLE CONNESSIONI CON I CLIENT */
            while (true) {
                threadpool.execute(new WordleClientHandler(server.accept(),registerService,secretword,playedwords,words));
            }
        }


    }

    /* FUNZIONE PER LA CONFIGURAZIONE INIZIALE DEL SERVER */
    static public void configserver(String configfile_path){
        try (InputStream input = new FileInputStream(configfile_path)){
            Properties prop = new Properties();
            prop.load(input);// faccio il loading del file.properties
            PORT_NUMBER=Integer.parseInt(prop.getProperty("port_number"));
            NUM_THREAD=Integer.parseInt(prop.getProperty("num_thread"));
            WORD_DURATION=Integer.parseInt(prop.getProperty("word_duration"));
            REGISTRY_PORT=Integer.parseInt(prop.getProperty("registry_port"));
            SERVER_NAME=prop.getProperty("server_name");
            System.out.println("PORT_NUMBER:"+PORT_NUMBER+"\nNUM_THREAD:"+NUM_THREAD+"\nWORD_DURATION:"+WORD_DURATION+"\nREGISTRY_PORT:"+REGISTRY_PORT+"\nSERVER_NAME:"+SERVER_NAME); // stampo il valore delle proprieta
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /* FUNZIONE PER GESTIRE LA REGISTRAZIONE DEGLI UTENTI TRAMITE RMI*/
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
