import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterService extends Remote {
    //void register_user(String username,String password) throws RemoteException ;
    int register_user(Player newplayer) throws RemoteException ;

}
