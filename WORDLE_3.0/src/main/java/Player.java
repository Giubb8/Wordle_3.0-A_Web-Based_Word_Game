import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    final private String username;
    final private String password;
    private double score;
    private ArrayList<String> playedwords;
    private int winnedgame;
    private double average_score;
    private int playedgame;
    public Player(String username,String password,int score){
        this.password=password;
        this.score=score;
        this.username=username;
        playedwords=new ArrayList<>();
        this.winnedgame=0;
        this.average_score= 0.0;
        this.playedgame=0;
    }

    public double getScore() {
        return score;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void addwinnedgame(int score) {
        this.winnedgame++;
        double sum=this.average_score*this.playedgame;
        this.average_score=((sum+score)/this.playedgame+1);
        this.score=this.winnedgame*this.average_score;
        this.playedgame++;
    }
    public void addplayedgame(int score){
        if(score==0){
            double sum=this.average_score*this.playedgame;
            this.average_score=sum/(this.playedgame+1);
            this.score=this.winnedgame*this.average_score;
        }
        this.playedgame++;

    }
    public ArrayList<String> getPlayedwords(){ return playedwords; }
    /* Override per consentire la stampa nella hashtable */
    public String toString(){
        return "\nPlayer: "+this.username+"\nPassword: "+this.password+"\nScore: "+this.score+"PlayedWord:"+this.playedwords+"\n\n";
    }
}

