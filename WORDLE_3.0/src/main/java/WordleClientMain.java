import java.io.*;
import java.net.Socket;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
    final private static String ALREADYLOGGED="ALREADYLOGGED";
    final private static String WORD_NOT_EXISTS="WORD_NOT_EXISTS";
    private static int MULTICASTPORT=0;
    final private static int ERROR_CODE=13001;
    final private static int WRONG_WORD=15001;
    public static final String ANSI_RESET = "\u001B[0m";
    final private static int OK_CODE=12002;
    private static int gameplayed=0;


    public static void main(String[] args) throws IOException, NotBoundException {

        /* CONFIGURAZIONE INIZIALE DEL CLIENT */
        String configfile_path=args[0];
        int initial_choice;
        int is_over=0;
        configclient(configfile_path);
        ArrayList<String> notification=new ArrayList<>(); //Lista contenente le notifiche da segnalare al client
        ArrayList<String> rank_update=new ArrayList<>();

        /*CREAZIONE DELLA SOCKET E DEGLI STREAM PER EFFETTUARE LA CONNESSIONE */
        BufferedReader input;
        PrintWriter output;
        Socket socket = new Socket("127.0.0.1", PORT_NUMBER);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);

        /* MI REGISTRO PER LE CALLBACK */
        handlecallback(rank_update);
        /* FASE DI BENVENUTO E SELEZIONE OPZIONE  */
        System.out.println("Benvenuto in Wordle 3.0\n"+"1) Registrazione\n"+"2) Login\n"+"3) Spiegazione Gioco");
        Scanner stdin = new Scanner(System.in);
        initial_choice=stdin.nextInt();

        /* SCELTA TRA LOGIN,REGISTRAZIONE E SPIEGAZIONE  */
        switch (initial_choice){
            case 1 -> {
                register(output);
                login(input,output,notification);
            }
            case 2 -> {
                String login="login";
                output.println(login);//QUI
                login(input,output,notification);
                System.out.println("uscito dal login client");
            }
            default -> System.out.println("SPIEGAZIONE");
        }

        /* MAIN LOOP DEL CLIENT FINO A QUANDO NON EFFETTUO IL LOGOUT O INTERROMPO PRENDO COMANDI */
        while(is_over==0) {
            System.out.println("OPZIONI DISPONIBILI:\n"+"1)PlayWordle\n"+"2)Logout\n"+"3)Mostra Notifiche\n"+"4)Condividi Risultato\n"+"5)Mostra Statistiche\n"+"6)Mostra Classifica");
            int command=stdin.nextInt();
            switch (command){
                case 1 :{
                    output.println("playwordle");//segnalo al server che voglio giocare
                    playwordle(input,output);
                    break;
                }
                case 2 : {
                    output.println("logout");
                    logout();
                    is_over=1;
                    break;
                }
                case 3:{
                    showMeSharing(notification);
                    break;
                }
                case 4:{
                    share(input,output);
                    break;
                }
                case 5:{
                    showstats(input,output);
                    break;
                }
                case 6:{
                    showmeranking(input,output);
                    System.out.println("uscito da showranking");
                    break;
                }
                default : {
                    System.out.println("Opzione non riconosciuta");
                }
            }
        }
        main(args);


    }

    /* FUNZIONE PER RICEVERE LA CLASSIFICA */
    private static void showmeranking(BufferedReader input, PrintWriter output) throws IOException {
        output.println("showrank");
        String line;
        while((line=input.readLine())!=null){
            if(line.equals("stop"))
                break;
            System.out.println(line);

        }
    }

    /* FUNZIONE PER LA GESTIONE DELLE CALLBACK */
    private static void handlecallback(ArrayList<String> rank_update) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(5000);
        String name = "Server";
        ServerCallBackInterface server = (ServerCallBackInterface) registry.lookup(name);
        ClientCallBackInterface callbackObj = new ClientCallBackImpl();
        ClientCallBackInterface stub = (ClientCallBackInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
        server.registerForCallback(stub);
    }

    /* FUNZIONE PER MOSTRARE LE STATISTICHE DELL'UTENTE */
    private static void showstats(BufferedReader input, PrintWriter output) throws IOException {
        output.println("statistics");
        String stats=input.readLine();
        while(stats !=null){
            System.out.println("Statistiche Ricevute:\n"+stats);
            stats=input.readLine();
            if(stats.equals(""))
                break;
        }
    }


    /* METODO PER LA CONDIVISIONE DELL'ULTIMA PARTITA */
    private static void share(BufferedReader input, PrintWriter output) throws IOException {
        if(gameplayed>0){// controllo se l'utente abbia gia' giocato o meno
            output.println("share");
            int ret=Integer.parseInt(input.readLine());
            if(ret!=OK_CODE){
                System.out.println("Impossibile Condividere il risultato");
            }
            else
                System.out.println("Risultato Condiviso");
        }
        else{
            System.out.println("Ancora niente da condividere");
        }
    }


    /* METODO PER LA VISUALIZZAZIONE DELLE NOTIFICHE RICEVUTE */
    private static void showMeSharing(ArrayList<String> notification) {
        System.out.println("Ricevute le seguenti notifiche:");
        for(String string: notification){
            System.out.println(string);
        }
    }


    /* METODO PER GIOCARE A WORDLE */
    private static void playwordle(BufferedReader input, PrintWriter output) throws IOException {
        String secretword;
        Scanner stdin=new Scanner(System.in);
        int tries=0;
        output.flush();

        /* CONTROLLO CHE L'UTENTE NON ABBIA GIA GIOCATO QUESTA PAROLA E GESTISCO L'ERRORE */
        int response=Integer.parseInt( input.readLine());
        System.out.println("respone ricevuto: "+response);
        if(response==ERROR_CODE){
            /*gestione dell'errore*/
            System.out.println("Hai gia effettuato il tuo tentativo per questa parola");
            return;
        }

        /* SE UTENTE NON HA GIOCATO LA PAROLA RICEVO LA PAROLA SEGRETA DAL SERVER */
        secretword= input.readLine();
        System.out.println("parola segreta ricevuta "+secretword);
        System.out.println("Effettua il tentativo per indovinare :");

        /* LOOP PER EFFETTUARE I TENTATIVI  */
        while(tries<12){

            /* UTENTE SCRIVE NUOVA PAROLA */
            String guess=stdin.nextLine();

            /* INVIO IL GUESS AL SERVER ATTRAVERSO LA FUNZIONE SENDWORD() */
            StringBuilder retstring=new StringBuilder(sendWord(guess,input,output));
            System.out.println(retstring);

            /* SE LA PAROLA INVIATA NON ESISTE */
            if(retstring.toString().equals(WORD_NOT_EXISTS)){
                //TODO QUA DEVO DARE L OPPORTUNITA DI RIPROVARE QUINDI DEVO FAR FARE UN ALTRO CICLO
                System.out.println("Parola non Esiste");
                output.println(WRONG_WORD);
            }
            else { /* ALTRIMENTI SE LA PAROLA ESISTE */
                /* SE LA PAROLA E' STATA INDOVINATA */
                if(guess.equals(secretword)){
                    int score=tries+1;
                    System.out.println("Hai indovinato la parola in "+score+" tentativi");
                    output.println(score);
                    break;
                }
                else {
                    /* ALTRIMENTI SE LA PAROLA NON E STATA INDOVINATA */
                    tries++;
                    if(tries==3){/* SE HO FINITO I TENTATIVI */
                        output.println(ERROR_CODE);
                    }
                    else { /* SE LA PAROLA E' SBAGLIATA */
                        output.println(WRONG_WORD);
                    }
                }
            }
        }
        System.out.println(ANSI_RESET+"Gioco Concluso");

        /* RICEVO LA TRADUZIONE DELLA PAROLA DA PARTE DEL SERVER */
        String traduzione;
        traduzione=input.readLine();
        System.out.println("Parola segreta: "+secretword+" Traduzione: "+traduzione);
        gameplayed++;
    }


    /* METODO PER INVIARE IL GUESS AL SERVER */
    static public StringBuilder sendWord(String guess,BufferedReader input, PrintWriter output) throws IOException {
        output.println(guess);
        StringBuilder retstring= new StringBuilder(input.readLine());
        return retstring;
    }

    /* METODO PER EFFETTUARE IL LOGOUT */
    private static void logout() {
        System.out.println("Logout effettuato");
    }

    /* METODO PER LA REGISTRAZIONE DI UN NUOVO GIOCATORE */
    static public void register(PrintWriter output) throws RemoteException {
        String login="login";
        output.println(login);

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
            Registry r = LocateRegistry.getRegistry(REGISTRY_PORT);//trovo il registry
            RemoteObject = r.lookup(SERVER_NAME);
            serverObject = (RegisterService) RemoteObject;//ottengo lo stub
            int returnedvalue=serverObject.register_user(player);// effettuo la registrazione dell'utente con il metodo remoto
            if(returnedvalue==ERROR_CODE)
                register(output);
            // System.out.println("PLAYER RITORNATO:"+testplayer.toString());
        }
        catch (Exception e) {
            System.out.println("Error in invoking object method " + e + e.getMessage());e.printStackTrace();
        }
    }

    /* METODO PER IL LOGIN DEL CLIENT */
    static public void login(BufferedReader input,PrintWriter output,ArrayList<String> notification) throws IOException {
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
        if(result.equals(USERNAMENOTEXIST)||result.equals(ALREADYLOGGED)){
            System.out.println("Username non registrato");
            login(input, output,notification);
        }

        /*EFFETTUO IL CONTROLLO SULLA PASSWORD */
        String result2=input.readLine();
        if(result2.equals(PASSWORDNOMATCH)){
            System.out.println("Password non corrisponde");
            login(input, output,notification);
        }

        /* FACCIO PARTIRE IL THREAD PER LA RICEZIONE DELLE NOTIFICHE */
        MulticastNotificationService notificationService=new MulticastNotificationService(MULTICASTPORT,notification);
        Thread notificationthread=new Thread(notificationService);
        notificationthread.start();

    }

    /* METODO PER LA CONFIGURAZIONE DEL CLIENT DAL FILE PASSATO COME ARGOMENTO */
    static public void configclient(String configfile_path){
        try (InputStream input = new FileInputStream(configfile_path)){
            Properties prop = new Properties(); //classe apposita per settare le proprieta degli oggetti
            prop.load(input); // faccio il loading del file.properties
            PORT_NUMBER=Integer.parseInt(prop.getProperty("port_number"));
            REGISTRY_PORT=Integer.parseInt(prop.getProperty("registry_port"));
            SERVER_NAME=prop.getProperty("server_name");
            MULTICASTPORT= Integer.parseInt(prop.getProperty("multicastport"));
            System.out.println("PORT_NUMBER:"+PORT_NUMBER+"\nREGISTRY_PORT:"+REGISTRY_PORT+"\nSERVER_NAME:"+SERVER_NAME); // stampo il valore delle proprieta
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
