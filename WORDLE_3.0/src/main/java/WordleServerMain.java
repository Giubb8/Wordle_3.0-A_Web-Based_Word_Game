import java.io.*;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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

    public static void main(String[] args) throws IOException {

        String configfile_path=args[0];
        ThreadPoolExecutor threadpool=(ThreadPoolExecutor) newCachedThreadPool();
        RegisterServiceImpl registerService=new RegisterServiceImpl(PORT_NUMBER,REGISTRY_PORT,SERVER_NAME);//Oggetto per la registrazione degli utenti tramite RMI
        ServerSocket server=new ServerSocket(PORT_NUMBER);

        /* CONFIGURO IL SERVER E FACCIO PARTIRE RMI PER LA REGISTRAZIONE  */
        configserver(configfile_path);
        RMI_register_start(registerService);

        /* STA IN ATTESA DELLE CONNESSIONI CON I CLIENT */
        while(true){
            threadpool.execute(new WordleClientHandler(server.accept(),registerService));
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
            System.out.println("Communication error " + e.toString());
        }
    }
}
