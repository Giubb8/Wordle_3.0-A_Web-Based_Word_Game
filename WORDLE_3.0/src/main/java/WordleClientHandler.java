import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import  java.net.Socket;
import java.util.Hashtable;

/* CLASSE PER LA GESTIONE DELLA CONNESSIONE CON I CLIENT */
public class WordleClientHandler implements  Runnable{
    /* UTILITIES PER LA GESTIONE DELLA CONNESSIONE */
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private WordleService wordleService;

    public WordleClientHandler(Socket clientSocket, RegisterServiceImpl registerService) throws IOException {
        this.client=clientSocket;
        in=new BufferedReader(new InputStreamReader(client.getInputStream()));
        out=new PrintWriter(client.getOutputStream(),true);
        wordleService=new WordleService(registerService);
    }

    /* METODO PER LA LETTURA SAFE DEGLI INPUT DA PARTE DEL CLIENT */
    public String readInput() throws IOException {
        String read=in.readLine();
        if(read==null){
            System.out.println("Readed Null from user");
        }
        return read;
    }

    /* METODO ESEGUITO DALLA THREADPOOL */
    public void run() {
        int is_over=0;
        /* ACCETTO INPUT FINO A QUANDO L'UTENTE NON DECIDE DI DISCONNETTERSI E GESTISCO LE SINGOLE RICHIESTE */
        while(is_over==0){
            System.out.println("ciclo while handler");
            try {
                String input=readInput();
                if(input==null)
                    break;
                switch (input) { //enhanced switch di java 12 dare un occhio in teoria non c'e' bisogno di break
                    case "login" -> is_over = wordleService.login(in, out);
                    case "logout" -> is_over = wordleService.logout();
                    default -> {
                        is_over = 1;
                        System.out.println("Comunicazione Terminata");
                    }
                }
                /* IL CLIENT HA CHIUSO LA CONNESSIONE */
                System.out.println("uscito dallo switch case");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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
