import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import  java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;

/* CLASSE PER LA GESTIONE DELLA CONNESSIONE CON I CLIENT */
public class WordleClientHandler implements  Runnable{

    /* UTILITIES PER LA GESTIONE DELLA CONNESSIONE */
    private final Socket client; //socket per la comunicazione con il client
    private final BufferedReader in; // stream in ingresso per la comunicazione con il client
    private final PrintWriter out; //stream in uscita per la comunicazione con il client
    private final WordleService wordleService; //oggetto per implementare le funzionalita del gioco Wordle
    private final StringBuffer secretword; //parola segreta da indovinare, cambia in maniera dinamica
    private Hashtable<String, ArrayList<String>> playedwords; // hashtable contenente le parole giocate dai singoli utenti
    private List<String> words; //lista delle parole contenute nel vocabolario
    private DatagramSocket ms; //Socket per la comunicazione Multicast
    private ServerCallBackImpl callback; //Implementazione dell'interfaccia per la comunicazione tramite RMI Callback
    private List<String> logged_user; // Lista degli utenti loggati

    /* COSTRUTTORE, SETTO LE UTILITIES E CREO IL WORDLESERVICE */
    public WordleClientHandler(Socket clientSocket, RegisterServiceImpl registerService, StringBuffer secretword, Hashtable<String, ArrayList<String>> playedwords, List<String> words, DatagramSocket ms, SortedSet<Player> ranking, ServerCallBackImpl callback, List<String> logged_user) throws IOException {
        this.client=clientSocket;
        this.secretword=secretword;
        in=new BufferedReader(new InputStreamReader(client.getInputStream()));
        out=new PrintWriter(client.getOutputStream(),true);
        this.playedwords=playedwords;
        this.words=words;
        this.ms=ms;
        this.callback=callback;
        this.logged_user=logged_user;
        wordleService=new WordleService(registerService,secretword,playedwords,words,ms,ranking,callback,logged_user);
    }

    /*
    * METODO ESEGUITO DALLA THREADPOOL,GESTISCE LA CONNESSIONE CON IL CLIENT
    * ANALIZZA IL COMANDO E DELEGA ALL'OGGETTO WORDLE SERVICE
    * @throws: IOException - Errore in lettura dagli stream
    * @throws: ExecutionException - @see WordleService.playwordle()
    * */
    public void run() {
        int is_over=0;
        String username="";

        /* ACCETTO INPUT FINO A QUANDO L'UTENTE NON DECIDE DI DISCONNETTERSI E GESTISCO LE SINGOLE RICHIESTE */
        while(is_over==0){
            try {
                String input=in.readLine();// da controllare largomento
                System.out.println(input);
                if(input==null)
                    break;
                //enhanced switch di java 12 dare un occhio in teoria non c'e' bisogno di break
                switch (input) {
                    case "login":{
                        username = wordleService.login(in, out);
                        break;}
                    case "logout":{
                        is_over = wordleService.logout();
                        break;}
                    case "playwordle":{
                        is_over = wordleService.playwordle(secretword, in, out);
                        break;}
                    case "share":{
                        is_over = wordleService.share(out);
                        break;}
                    case "statistics":{
                        is_over = wordleService.sendstats(in, out);
                        break;}
                    case "showrank":{
                        is_over = wordleService.showranking(in, out);
                        break;}
                    default:{
                        is_over = 1;
                        System.out.println("Comunicazione Terminata");
                        break;}
                }
            } catch (IOException | InterruptedException e) {
                System.out.println();
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        /* IL CLIENT HA TERMINATO LA CONNESSIONE */

        /* RIMUOVO L'UTENTE DALLA LISTA DEGLI UTENTI LOGGATI */
        if(logged_user.contains(username))
            logged_user.remove(username);

        /* UNA VOLTA CHE HO INTERROTTO LA CONNESIONE CON IL CLIENT POSSO CHIUDERE GLI STREAM E LA CONNESSIONE */
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
