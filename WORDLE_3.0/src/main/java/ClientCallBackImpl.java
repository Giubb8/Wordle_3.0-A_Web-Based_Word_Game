import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientCallBackImpl implements ClientCallBackInterface{
    private ArrayList<String> top_position;

    public ClientCallBackImpl(){
        super();
        top_position=new ArrayList<>();
    }
    public void notifyEvent(String value) throws RemoteException {
            String returnMessage = "UPDATE RICEVUTO: " + value; //TODO CAMBIARE MESSAGGIO
            top_position.add(returnMessage);
            System.out.println("Update Ricevuti:\n"+top_position);

    }
}
