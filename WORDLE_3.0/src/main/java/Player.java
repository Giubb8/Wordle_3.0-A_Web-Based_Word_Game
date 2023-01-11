import java.io.Serializable;
import java.util.ArrayList;

/* CLASSE PER RAPPRESENTARE IL GIOCATORE */
public class Player implements Serializable,Comparable<Player> {
    /* VARIABILI PER RAPPRESENTARE IL GIOCATORE */
    private static final long serialVersionUID = 1L;
    final private String username;
    final private String password;
    private int score;
    private ArrayList<String> playedwords;
    private int winnedgame;
    private int average_score;
    private int playedgame;

    public Player(String username,String password,int score){
        this.password=password;
        this.score=score;
        this.username=username;
        playedwords=new ArrayList<>();
        this.winnedgame=0;
        this.average_score= 0;
        this.playedgame=0;
    }

    public int getScore() {
        return score;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    /* FUNZIONE PER AGGIUNGERE UNA PARTITA VINTA AL GIOCATORE,AGGIORNANDO LE STATISTICHE
    * @param: score= punteggio effettuato dal giocatore
    * */
    public void addwinnedgame(int score) {
        this.winnedgame++;
        int sum=this.average_score*this.playedgame;
        this.average_score= Math.round ((sum+score)/(this.playedgame+1));
        this.score=this.winnedgame*this.average_score;
        this.playedgame++;
    }

    /* FUNZIONE PER AGGIUNGERE UNA PARTITA GIOCATA AL GIOCATORE
     * @param: score= punteggio effettuato dal giocatore
     * */
    public void addplayedgame(int score){
        if(score==0){
            int sum=this.average_score*this.playedgame;
            this.average_score=Math.round (sum/(this.playedgame+1));
            this.score=this.winnedgame*this.average_score;
        }
        this.playedgame++;

    }

    /* FUNZIONE PER RESTITUIRE LE PAROLE GIOCATE DAL GIOCATORE */
    public ArrayList<String> getPlayedwords(){ return playedwords; }

    /* OVERRIDE PER CONSENTIRE LA STAMPA NELLE HASHTABLE */
    public String toString(){
        return "\nPlayer: "+this.username+"\nPassword: "+this.password+"\nScore: "+this.score+"\nWinnedGame: "+this.winnedgame+"\nPlayedGame: "+this.playedgame+"\nAVGscore: "+this.average_score+"\nPlayedWord:"+this.playedwords+"\n";
    }


    public int compareTo(Player player) {
        if(this.equals(player))
            return 0;
        else return -1;
    }

}

