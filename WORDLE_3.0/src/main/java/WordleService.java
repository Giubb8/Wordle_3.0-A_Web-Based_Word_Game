import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/* CLASSE CHE IMPLEMENTA IL SERVIZIO DI WORDLE */
public class WordleService {

    /* COSTANTI E CODICI DI ERRORE */
    final private static String USERNAMENOTEXIST="ERR_UNAME_NOT_EXIST";
    final private static String PASSWORDNOMATCH="ERR_PASSWORD_NOT_MATCHING";
    final private static String ALREADYLOGGED="ALREADYLOGGED";
    final private static String SUCCESS="SUCCESS";
    final private static String WORD_NOT_EXISTS="WORD_NOT_EXISTS";
    private static final int WRONG_WORD = 15001;
    final private static int ERROR=1;
    final private int ERROR_CODE=13001;
    final private int OK_CODE=12002;
    final private static int OK=0;

    /* STRUTTURE DI SUPPORTO */
    private InetAddress multicast_group=null;
    private int multicastport;
    private DatagramSocket ms;
    private final Hashtable<String,Player> players_table;
    private final StringBuffer secretword;
    private ArrayList<String> played_words;
    private String session_username;
    private Hashtable<String, ArrayList<String>> playedwords;
    private List<String> words;
    private RegisterServiceImpl registerService;

    /* UTILITIES PER GLI INDIZI */
    private static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_END = "\u001B[0m";
    private static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    private static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";

    private String s_username;
    private String s_secretword;
    private int s_score;
    private SortedSet<Player> ranking;
    private ServerCallBackImpl callback;
    private List<String> logged_user;

    /*COSTRUTTORE DEL SERVIZIO WORDLE */
    public WordleService(RegisterServiceImpl registerService, StringBuffer secretword, Hashtable<String, ArrayList<String>> playedwords, List<String> words, DatagramSocket ms, SortedSet<Player> ranking, ServerCallBackImpl callback, List<String> logged_user) throws UnknownHostException, SocketException {
        this.players_table=registerService.getPlayers_table();
        this.registerService=registerService;
        this.secretword=secretword;
        played_words=new ArrayList<String>();
        this.playedwords=playedwords;
        this.words=words;
        this.multicast_group=InetAddress.getByName("228.5.6.7");
        this.multicastport=4400;
        this.ms=ms;
        this.ranking=ranking;
        this.callback=callback;
        this.logged_user=logged_user;
    }

    /* METODO PER EFFETTUARE IL LOGIN DA PARTE DELL'UTENTE
    *  EFFETTUA I CONTROLLI SUI DATI E RESTITUISCE AL CLIENT
    *  I CODICI DI ERRORE ADATTI AL PROBLEMA SORTO IN  FASE DI LOGIN
    * @param: in= Stream per la comunicazione in ingresso dal client
    * @param out= Stream per la comunicazione in uscita verso il client
    * @returns: username= username dell'utente che si è loggato
    * @throws: IOException - lettura dati dagli stream
    * */
    public String login(BufferedReader in, PrintWriter out) throws IOException {
        /* VARIABILI DI SUPPORTO E STREAM */
        String username=in.readLine();
        String password=in.readLine();
        System.out.println("Username: "+username+" Password: "+password);


        /* CONTROLLO USERNAME */
        if(!players_table.containsKey(username)){
            System.out.println("Dentro il not");
            out.println(USERNAMENOTEXIST);
        }
        else if(logged_user.contains(username)){
            System.out.println("utente gia loggato");
            out.println(ALREADYLOGGED);
        }
        else{
            out.println(SUCCESS);
        }
        /* CONTROLLO PASSWORD CORRISPONDA */
        if(!players_table.containsKey(username)||!players_table.get(username).getPassword().equals(password)){// se la password NON coincide
            System.out.println("Dentro il not pass");
            out.println(PASSWORDNOMATCH);
            login(in, out);
        }
        else{
            out.println(SUCCESS);
            session_username=username;
            System.out.println(registerService.getPlayers_table().get(username));
        }

        /* AGGIORNO LA LISTA DEGLI UTENTI LOGGATI */
        logged_user.add(username);
        /* RETURNO USERNAME PER POTERLO RIMUOVERE QUANDO LOGOUT */
        return username;
    }

    /* FUNZIONE PER IL LOGOUT DA PARTE DELL'UTENTE
    * @return: ret= intero per uscire dal while di WordleClientHandler
    * */
    public int logout(){
        int ret=1;
        System.out.println("logout");
        return ret;
    }

    /* FUNZIONE PER GENERARE GLI INDIZI DA RESTITUIRE ALL'UTENTE
    *  SFRUTTA I CODICI ANSI PER LA COLORAZIONE DEL BACKGROUND
    *  E DEL FONT DEL TERMINALE
    * @param: word= Stringa contenente il tentativo dell'utente
    * @param: s_word= Stringa contenente la parola da indovinare (secretword)
    * @returns: hints= String contenente l'indizio da fornire al client
    * */
    public static  StringBuilder getWordHint(String word,String s_word) {
        StringBuilder hints = new StringBuilder();
        for (int i=0; i < s_word.length(); i++) {
            char curr_character = word.charAt(i);
            String newChar;
            if (s_word.charAt(i) == curr_character) { // lettera nella posizione giusta
                newChar = ANSI_GREEN_BACKGROUND +ANSI_BLACK+ curr_character + ANSI_END;
            }
            else if (s_word.indexOf(curr_character) != -1) { // lettera c'è ma non nella posizione i
                newChar = ANSI_YELLOW_BACKGROUND + ANSI_BLACK+ curr_character + ANSI_END;
            }
            else  {
                newChar = ANSI_WHITE_BACKGROUND+String.valueOf(curr_character);
            }
            hints.append(newChar);
        }
        return hints;
    }


    /*
    *  FUNZIONE PER AVVIARE IL GIOCO, EFFETTUA I CONTROLLI SUI DATI,
    *  IMPLEMENTA LE FUNZIONALITA' DEL GIOCO, LANCIA IL THREAD PER LA TRADUZIONE DELLA PAROLA
    *  AGGIORNA LA CLASSIFICA E GESTISCE LA COMUNICAZIONE CON IL CLIENT
    *
    * @param: secret_word= StringBuffer contenente la parola segreta per la corrente sessione di gioco
    * @param: in= Stream per la comunicazione in ingresso dal client
    * @param out= Stream per la comunicazione in uscita verso il client
    * @returns: OK|ERROR = codice per segnalare la buona o meno esecuzione del metodo
    * @throws: IOException - errore nella comunicazione negli stream in/out
    * @throws: ExecutionException || InterruptedException - errore nella gestione della futureTask
    *
    * */
    public int playwordle(StringBuffer secret_word, BufferedReader in, PrintWriter out) throws IOException, ExecutionException, InterruptedException {
        /* SALVO LA PAROLA SEGRETA CORRENTE IN UNA STRINGA QUINDI IMMUTABLE */
        String current_secretword=secret_word.toString();
        TranslatorTask t_task=new TranslatorTask(current_secretword);
        FutureTask<String> futuretask=new FutureTask<String>(t_task);
        Thread tr_thread=new Thread(futuretask);
        tr_thread.start();
        /* EFFETTUO CONTROLLO SULLE PAROLE GIOCATE DALL'UTENTE */
        boolean condition2=(!registerService.getPlayers_table().get(session_username).getPlayedwords().isEmpty() && registerService.getPlayers_table().get(session_username).getPlayedwords().contains(current_secretword));
        if (condition2){
            System.out.println("Controllo con la hashtable");
            System.out.println("No more chances for this word");
            System.out.println("Funzione playwordle: Lista parole giocate "+played_words);
            out.println(ERROR_CODE);
            return ERROR;
        }

        /* INVIO RESPONSE E LA SECRET WORD PER LA GESTIONE DELLA STESSA LATO CLIENT */
        out.println(OK_CODE);
        out.println(current_secretword);
        int client_finished=0;
        int client_response =-1;

        /* WHILE PRINCIPALE,SESSIONE DI GIOCO DELL'UTENTE */
        while(client_finished==0){
            String client_guess=in.readLine();
            System.out.println("parola del client:"+client_guess+" "+words.contains(client_guess));

            /* CONTROLLO SE LA PAROLA ESISTE NEL VOCABOLARIO */
            if(words.contains(client_guess)!=true){
                /* SE NON ESISTE */
                System.out.println("La parola non esiste nel vocabolario");
                out.println(WORD_NOT_EXISTS);
            }
            else{
                /* SE ESISTE RESTITUISCO L'INDIZIO */
                out.println(getWordHint(client_guess,current_secretword));
            }
            client_response=Integer.parseInt(in.readLine());
            if(client_response==ERROR_CODE || client_response!=WRONG_WORD)
                client_finished=1;
        }
        /* SESSIONE DI GIOCO CONCLUSA */

        /* CONTROLLO LA RISPOSTA DEL CLIENT E CALCOLO IL PUNTEGGIO DELLA SESSIONE DI GIOCO */
        int score=0;
        if(client_response!=ERROR_CODE && client_response!=WRONG_WORD)
             score=(client_response*-1)+4;
        System.out.println("score: "+score);

        /* ESEGUO L'UPDATE DELLA CLASSIFICA */
        updateranking(session_username,current_secretword,ranking,score,callback);

        /* TRADUCO LA PAROLA E LA RESTITUISCO AL CLIENT */
        /*String traduzione=translateWords(current_secretword);
        System.out.println("traduzione: "+traduzione);*/
        String traduzionefuture=futuretask.get();
        System.out.println("TRADUZIONE FUTURE "+traduzionefuture);
        out.println(traduzionefuture);

        /* AGGIORNO L'ULTIMA CONFIGURAZIONE DELLA SESSIONE DI GIOCO PIU RECENTE */
        s_username=session_username;
        s_secretword=current_secretword;
        s_score=score;


        return OK;

    }

    /*
    * FUNZIONE PER AGGIORNARE LA CLASSIFICA AGGIORNA IL FILE JSON ASSOCIATO
    * CONTROLLA SE LA CLASSIFICA SI AGGIORNA NELLE PRIME 3 POSIZIONI
    * E COMUNICA AL CLIENT TRAMITE CALLBACK
    *
    * @param: session_username= username del quale bisogna aggiornare la classifica
    * @param: current_secretword= secretword giocata dall'utente al momento nel quale l'update viene effettuato
    * @param: ranking= SortedSet che implementa la classifica
    * @param: score= punteggio effettuato dall'utente
    * @param: callback= istanza per gestire la comunicazione tramite RMI Callback
    * */
    private void updateranking(String session_username, String current_secretword, SortedSet<Player> ranking, int score, ServerCallBackImpl callback) throws RemoteException {
        Player prev;
        prev=players_table.get(session_username);
        String ranking_path="../resources/Ranking.json";

        /* CONTROLLO SULLO STATO DELLA CLASSIFICA */
        if(ranking.contains(prev)){
            ranking.remove(prev);
        }

        /* UPDATE EFFETTIVO DELLA CLASSIFICA  */
        registerService.updateplayer(session_username,current_secretword,score);
        ranking.add(players_table.get(session_username));

        /* AGGIORNO IL FILE */
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        try {
            FileOutputStream fos=new FileOutputStream(ranking_path);
            OutputStreamWriter ow=new OutputStreamWriter(fos);
            String rankingString=gson.toJson(ranking);
            ow.write(rankingString);
            ow.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* FACCIO IL CONTROLLO SULLA POSIZIONE OTTENUTA,NEL CASO FOSSE TRA LE PRIME 3 -> CALLBACK UPDATE */
        Iterator iterator=ranking.iterator();
        int i=1;
        while(iterator.hasNext() && i<=3){
            Player player=players_table.get(session_username);
            if( (iterator.next().equals(player)) )
                callback.update(session_username+" è in "+i+"a posizione con "+player.getScore()+"punti");
            i++;
        }
    }

    /* FUNZIONE PER LA CONDIVISIONE DEI RISULTATI CON UDP MULTICASTING
    * @param: out= stream per la comunicazione verso l'utente
    * @throws: IOException - errori nella comunicazione tramite stream
    * */
    public  int share(PrintWriter out) throws IOException {
        byte[] data;
        String tosend;
        if(s_username==null ||s_secretword==null||s_username.equals("") || s_secretword.equals("") )
             tosend=(WORD_NOT_EXISTS);
        else
            tosend=""+s_username+" for "+s_secretword+" scored:"+s_score;
        data=tosend.getBytes();

        /* INVIO IL PACCHETTO */
        DatagramPacket dp=new DatagramPacket(data,data.length,multicast_group,multicastport);
        ms.send(dp);
        out.println(OK_CODE);

        /* RESET DELLE VARIABILI */
        s_score=0;
        s_username=null;
        s_secretword=null;
        return 0;
    }

    /* FUNZIONE PER LA TRADUZIONE DELLE PAROLE CON MYMEMORY */ //TODO FORSE CANCELLARE E LASCIARE AL NUOVO THREAD IL COMPITO
    public static String translateWords(String word) throws IOException {
        URL url= new URL("https://api.mymemory.translated.net/get?q=" + word + "&langpair=en|it");
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

    /* FUNZIONE PER MANDARE LE STATISTICHE DELL'UTENTE
    *
    * @param: in= Stream per la comunicazione in ingresso dal client
    * @param out= Stream per la comunicazione in uscita verso il client
    * */
    public int sendstats(BufferedReader in, PrintWriter out) {
        int ret=0;
        String stat=this.players_table.get(session_username).toString();
        System.out.println("STATISTICHE\n"+stat);
        out.println(this.players_table.get(session_username));
        return ret;
    }

    /* FUNZIONE PER MANDARE LA CLASSIFICA ALL'UTENTE
    *
    * @param: in= Stream per la comunicazione in ingresso dal client
    * @param out= Stream per la comunicazione in uscita verso il client
    * */
    public int showranking(BufferedReader in, PrintWriter out) {
        int ret=0;
        out.println(ranking.toString());
        out.println("stop");
        return ret;
    }
}
