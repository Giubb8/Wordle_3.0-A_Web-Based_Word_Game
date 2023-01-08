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
/*TODO LOGIN MI DEVE RESTITUIRE LO USERNAME CHE POI UTLIIZZO PER SLOGGARE LUTENTE LEVANDOLO DALLA LISTA DOPO CHE ESCO DAL WHILE */
/* CLASSE PER LA GESTIONE DELLA CONNESSIONE CON I CLIENT */
public class WordleClientHandler implements  Runnable{

    /* UTILITIES PER LA GESTIONE DELLA CONNESSIONE */
    private final Socket client;
    private final BufferedReader in;
    private final PrintWriter out;
    private final WordleService wordleService;
    private final StringBuilder secretword;
    private Hashtable<String, ArrayList<String>> playedwords;
    private List<String> words;
    private DatagramSocket ms;
    private ServerCallBackImpl callback;
    private List<String> logged_user;

    /* COSTRUTTORE, SETTO LE UTILITIES E CREO IL WORDLESERVICE */
    public WordleClientHandler(Socket clientSocket, RegisterServiceImpl registerService, StringBuilder secretword, Hashtable<String, ArrayList<String>> playedwords, List<String> words, DatagramSocket ms, SortedSet<Player> ranking, ServerCallBackImpl callback, List<String> logged_user) throws IOException {
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

    /* METODO ESEGUITO DALLA THREADPOOL */
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
                switch (input) { //enhanced switch di java 12 dare un occhio in teoria non c'e' bisogno di break
                    case "login" -> username = wordleService.login(in, out);
                    case "logout" -> is_over = wordleService.logout();
                    case "playwordle" ->is_over=wordleService.playwordle(secretword,in,out);
                    case "share" -> is_over=wordleService.share(out);
                    case "statistics" -> is_over=wordleService.sendstats(in,out);
                    case "showrank"->is_over=wordleService.showranking(in,out);
                    default -> {
                        is_over = 1;
                        System.out.println("Comunicazione Terminata");
                    }
                }
            } catch (IOException | InterruptedException e) {
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
