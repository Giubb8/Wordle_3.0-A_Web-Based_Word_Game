import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerCallBackInterface extends Remote {
    /* registrazione per la callback */
    public void registerForCallback (ClientCallBackInterface ClientInterface) throws RemoteException;
    /* cancella registrazione per la callback */
    public void unregisterForCallback(ClientCallBackInterface ClientInterface) throws RemoteException;

}
