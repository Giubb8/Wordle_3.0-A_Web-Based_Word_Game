import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
/* CLASSE CHE IMPLEMENTA IL SERVIZIO DI WORDLE */
public class WordleService {
    /* COSTANTI E STRUTTURE DI SUPPORTO */
    private final Hashtable<String,Player> players_table;
    final private static String USERNAMENOTEXIST="ERR_UNAME_NOT_EXIST";
    final private static String PASSWORDNOMATCH="ERR_PASSWORD_NOT_MATCHING";
    final private static String SUCCESS="SUCCESS";
    final private static int ERROR=1;
    private final StringBuilder secretword;

    public WordleService(RegisterServiceImpl registerService,StringBuilder secretword){
        this.players_table=registerService.getPlayers_table();
        this.secretword=secretword;
    }
    /* METODO PER EFFETTUARE IL LOGIN DA PARTE DELL'UTENTE */
    public int login(BufferedReader in, PrintWriter out) throws IOException, InterruptedException {
        /* VARIABILI DI SUPPORTO E STREAM */
        int ret=0;
        String username=in.readLine();
        String password=in.readLine();
        System.out.println("Username: "+username+" Password: "+password);

        /* CONTROLLO SUI DATI DELL'UTENTE*/
            /* CONTROLLO USERNAME */ //TODO NON PERMETTERE L'USO DI USERNAME UGUALI IN FASE DI REGISTRAZIONE
        if(!players_table.containsKey(username)){
            System.out.println("Dentro il not");
            out.println(USERNAMENOTEXIST);
            return ERROR;
        }
        else{
            out.println(SUCCESS);
        }
            /* CONTROLLO PASSWORD CORRISPONDA */ //TODO MAGARI POSSO METTERE DEI VINCOLI SULLE PASSWORD IN FASE DI REGISTRAZIONE
        if(!players_table.get(username).getPassword().equals(password)){// se la password NON coincide
            System.out.println("Dentro il not pass");
            out.println(PASSWORDNOMATCH);
            login(in, out);
        }
        else{ //altrimenti se la password coincide
            out.println(SUCCESS);
        }
        /* TEST */
        /*for (int i = 0; i <10000000 ; i++) {
            System.out.println("Thread:"+Thread.currentThread().getName()+" SecretWord: "+secretword);
            Thread.sleep(2000);
        }*/

        return ret;
    }

    /* FUNZIONE PER IL LOGOUT DA PARTE DELL'UTENTE */ //TODO ANCORA DA IMPLEMENTARE
    public int logout(){
        int ret=122;
        System.out.println("logout");
        return ret;
    }

    /* FUNZIONE PER AVVIARE IL GIOCO */
    public int playwordle(String secret_word){
        return 0;
    }


}
