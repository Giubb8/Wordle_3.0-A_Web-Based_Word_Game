import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/* CLASSE PER LA GESTIONE DELLA PAROLA SEGRETA, OGNI TIME SECONDI CAMBIA LA PAROLA */
public class SecretWordManager extends  Thread{
    private final StringBuffer secretword;
    private final String path_to_wordsfile="../resources/words.txt";
    private final int time;

    public SecretWordManager(StringBuffer secretword,int time){
        this.secretword=secretword;
        this.time=time;
    }

    /* OVVERIDE DEL METODO RUN
    *  OGNI TIME SECONDI GENERA UNA NUOVA PAROLA SEGRETA
    *  @throws: InterruptedException - nel caso in cui il thread venga testato mentre dormiente per la sleep
    * */
    public void run() {
        ArrayList<String> words_list=txt_to_list(path_to_wordsfile);
        while(true){
            int random= new Random().nextInt(words_list.size());
            secretword.append(words_list.get(random));
            try {Thread.sleep(time);} catch (InterruptedException e) {throw new RuntimeException(e);}
            secretword.setLength(0);
        }
    }

    /* FUNZIONE CHE TRASFORMA IL FILE WORDS IN UN ARRAYLIST (PER MOTIVI DI EFFICENZA NELL'ACCESSO )
    * @param: path= path al file da trasformare in una lista
    * @return: words_list= ArrayList contenente la lista di parole
    * @throws: FileNotFoundException - file specificato non trovato
    * */
    public ArrayList<String> txt_to_list(String path){
        Scanner s;
        try {
            s = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.out.println("File da convertire in ArrayList non trovato");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        ArrayList<String> words_list = new ArrayList<String>();
        while (s.hasNext()){
            words_list.add(s.next());
        }
        s.close();
        return words_list;
    }
}

