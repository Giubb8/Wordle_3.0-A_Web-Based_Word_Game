import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    final private String username;
    final private String password;
    private final int record;

    public Player(String username,String password,int record){
        this.password=password;
        this.record=record;
        this.username=username;
    }

    public int getRecord() {
        return record;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    /* Override per consentire la stampa nella hashtable */
    public String toString(){
        return "\nPlayer: "+this.username+"\nPassword: "+this.password+"\nRecord: "+this.record+"\n\n";
    }
}

