import java.util.Comparator;
/* COMPARATOR PER PERMETTERE L'ORDINE DEL TREESET CONCORRENTE */
public class PlayerRankingComparator implements Comparator<Player> {


    public int compare(Player firstPlayer, Player secondPlayer) {
        if(firstPlayer.getUsername().equals(secondPlayer.getUsername()))
            return firstPlayer.getScore() > secondPlayer.getScore() ? -1 : firstPlayer.getScore() < secondPlayer.getScore() ? 1 : 0;
        else
            return firstPlayer.getScore() > secondPlayer.getScore() ? -1 : firstPlayer.getScore() < secondPlayer.getScore() ? 1 : -1;
    }


}