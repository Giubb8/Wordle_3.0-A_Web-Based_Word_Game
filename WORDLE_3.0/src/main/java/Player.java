import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable,Comparable<Player> {
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

    public void addwinnedgame(int score) {
        this.winnedgame++;
        int sum=this.average_score*this.playedgame;
        this.average_score= Math.round ((sum+score)/(this.playedgame+1));
        this.score=this.winnedgame*this.average_score;
        this.playedgame++;
    }
    public void addplayedgame(int score){
        if(score==0){
            int sum=this.average_score*this.playedgame;
            this.average_score=Math.round (sum/(this.playedgame+1));
            this.score=this.winnedgame*this.average_score;
        }
        this.playedgame++;

    }
    public ArrayList<String> getPlayedwords(){ return playedwords; }
    /* Override per consentire la stampa nella hashtable */
    public String toString(){
        return "\nPlayer: "+this.username+"\nPassword: "+this.password+"\nScore: "+this.score+"\nWinnedGame: "+this.winnedgame+"\nPlayedGame: "+this.playedgame+"\nAVGscore: "+this.average_score+"PlayedWord:"+this.playedwords+"\n";
    }


    public int compareTo(Player player) {
        return this.score > player.getScore() ? -1 : this.score < player.getScore() ? 1 : 0;
    }
}

