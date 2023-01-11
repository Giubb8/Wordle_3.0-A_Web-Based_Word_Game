import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerCallBackInterface extends Remote {
    /* REGISTRAZIONE PER LE CALLBACK  */
    public void registerForCallback (ClientCallBackInterface ClientInterface) throws RemoteException;
    /* CANCELLA REGISTRAZIONE PER LE CALLBACK */
    public void unregisterForCallback(ClientCallBackInterface ClientInterface) throws RemoteException;

}
