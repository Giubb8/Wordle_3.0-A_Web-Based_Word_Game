import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/* CLASSE CLIENT DI WORDLE */
public class WordleClientMain {
    /*CODICE DI ERRORE E COSTANTI PER LA CONNESSIONE */
    public static int PORT_NUMBER;
    public static int REGISTRY_PORT;
    public static String SERVER_NAME;
    final private static String USERNAMENOTEXIST="ERR_UNAME_NOT_EXIST";
    final private static String PASSWORDNOMATCH="ERR_PASSWORD_NOT_MATCHING";

    public static void main(String[] args) throws IOException {
        /* CONFIGURAZIONE INIZIALE DEL CLIENT */
        String configfile_path=args[0];
        int initial_choice;
        int is_over=0;
        configclient(configfile_path);

        /*CREAZIONE DELLA SOCKET E DEGLI STREAM PER EFFETTUARE LA CONNESSIONE */
        BufferedReader input;
        PrintWriter output;
        Socket socket = new Socket("127.0.0.1", PORT_NUMBER);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);

        /* FASE DI BENVENUTO E SELEZIONE OPZIONE  */
        System.out.println("Benvenuto in Wordle 3.0\n"+"1) Registrazione\n"+"2) Login\n"+"3) Spiegazione Gioco");
        Scanner stdin = new Scanner(System.in);
        initial_choice=stdin.nextInt();

        /* SCELTA TRA LOGIN,REGISTRAZIONE E SPIEGAZIONE  */
        //ovviamente da cambiare
        switch (initial_choice){
            case 1 -> register();
            case 2 -> {
                String login="login";
                output.println(login);//QUI
                login(input,output);
                System.out.println("uscito dal login client");
            }
            default -> System.out.println("SPIEGAZIONE");
        }

        /* MAIN LOOP DEL CLIENT FINO A QUANDO NON EFFETTUO IL LOGOUT O INTERROMPO PRENDO COMANDI */
        while(is_over==0) {
            System.out.println("dentro loop isover client");
            String command=stdin.next();
            System.out.println("ricevuto "+command);
            output.println(command);
            if (command.equals("logout")) {
                logout();
            } else {
                is_over = 1;
                System.out.println("Ho finito lato client");
            }
        }


    }

    /* METODO PER EFFETTUARE IL LOGOUT */
    private static void logout() {
        System.out.println("Ho finito");
    }

    /* METODO PER LA REGISTRAZIONE DI UN NUOVO GIOCATORE */
    static public void register() throws RemoteException {
        /* SUPPORTO PER DATI E CONNESSIONE */
        Scanner stdin = new Scanner(System.in);
        RegisterService serverObject;
        Remote RemoteObject;
        /* PRENDO I DATI PER LA REGISTRAZIONE */
        System.out.println("Fase di registrazione:\n"+"Username:");
        String username=stdin.next();
        System.out.println("Password:");
        String password=stdin.next();
        /* CREO UN NUOVO GIOCATORE */
        Player player=new Player(username,password,0);

        /* TROVO L'OGGETTO REMOTO CON IL REGISTRY ED ESEGUO IL SUO METODO PER LA REGISTRAZIONE */
        try {
            Registry r = LocateRegistry.getRegistry(REGISTRY_PORT);
            RemoteObject = r.lookup(SERVER_NAME);
            serverObject = (RegisterService) RemoteObject;
            Player testplayer=serverObject.register_user(player);
            System.out.println("PLAYER RITORNATO:"+testplayer.toString());
        }
        catch (Exception e) {
            System.out.println("Error in invoking object method " + e + e.getMessage());e.printStackTrace();
        }
    }

    /*METODO PER IL LOGIN DEL CLIENT */
    static public void login(BufferedReader input,PrintWriter output) throws IOException {
        Scanner stdin = new Scanner(System.in);

        /* PRENDO I DATI PER EFFETTUARE IL LOGIN */
        System.out.println("Fase di Login:\n"+"Username:");
        String username=stdin.next();
        System.out.println("Password:");
        String password=stdin.next();

        /* INVIO I DATI AL SERVER PER I LOGIN */
        output.println(username);
        output.println(password);

        /* EFFETTUO IL CONTROLLO SULLO USERNAME*/
        String result=input.readLine();
        if(result.equals(USERNAMENOTEXIST)){
            System.out.println("Username non registrato");
            login(input, output);
        }
        System.out.println("Username "+username+"trovato");

        /*EFFETTUO IL CONTROLLO SULLA PASSWORD */
        String result2=input.readLine();
        if(result2.equals(PASSWORDNOMATCH)){
            System.out.println("Password non corrisponde");
            login(input, output);
        }


    }

    /* METODO PER LA CONFIGURAZIONE DEL CLIENT DAL FILE PASSATO COME ARGOMENTO */
    static public void configclient(String configfile_path){
        try (InputStream input = new FileInputStream(configfile_path)){
            Properties prop = new Properties(); //classe apposita per settare le proprieta degli oggetti
            prop.load(input); // faccio il loading del file.properties
            PORT_NUMBER=Integer.parseInt(prop.getProperty("port_number"));
            REGISTRY_PORT=Integer.parseInt(prop.getProperty("registry_port"));
            SERVER_NAME=prop.getProperty("server_name");
            System.out.println("PORT_NUMBER:"+PORT_NUMBER+"\nREGISTRY_PORT:"+REGISTRY_PORT+"\nSERVER_NAME:"+SERVER_NAME); // stampo il valore delle proprieta
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
