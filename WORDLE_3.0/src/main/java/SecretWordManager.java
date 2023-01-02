import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class SecretWordManager extends  Thread{
    private final StringBuilder secretword;
    private final String path_to_wordsfile="src/main/resources/words.txt";
    private final int time;

    public SecretWordManager(StringBuilder secretword,int time){
        this.secretword=secretword;
        this.time=time;
    }

    public void run() {
        ArrayList<String> words_list=txt_to_list(path_to_wordsfile);
       // System.out.println(words_list);
        while(true){//TODO FARE UNA CONDIZIONE DI TERMINAZIONE
            int random= new Random().nextInt(words_list.size());
            secretword.append(words_list.get(random));
            //System.out.println("THE SECRET WORD NOW IS: "+secretword);
            try {Thread.sleep(time);} catch (InterruptedException e) {throw new RuntimeException(e);}
            secretword.setLength(0);
        }

    }

    /* FUNZIONE CHE TRASFORMA IL FILE WORDS IN UN ARRAYLIST (PER MOTIVI DI EFFICENZA NELL'ACCESSO ) */
    public ArrayList<String> txt_to_list(String path){
        Scanner s;
        try {
            s = new Scanner(new File(path));
        } catch (FileNotFoundException e) {throw new RuntimeException(e);}
        ArrayList<String> words_list = new ArrayList<String>();
        while (s.hasNext()){
            words_list.add(s.next());
        }
        s.close();
        return words_list;
    }
}

