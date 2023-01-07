import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallBackInterface extends Remote {
    public void notifyEvent(String value) throws RemoteException;

}
