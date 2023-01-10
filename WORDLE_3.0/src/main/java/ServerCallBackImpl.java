import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerCallBackImpl extends RemoteObject implements ServerCallBackInterface{


    private List<ClientCallBackInterface> clients;


    public ServerCallBackImpl()throws RemoteException {
        super( );
        clients = new ArrayList<ClientCallBackInterface>();
    }

    public void registerForCallback(ClientCallBackInterface ClientInterface) throws RemoteException {
        if (!clients.contains(ClientInterface)) {
            clients.add(ClientInterface);
            System.out.println("New client registered." );
        }
    }

    public void unregisterForCallback(ClientCallBackInterface ClientInterface) throws RemoteException {
        if (clients.remove(ClientInterface)) {
            System.out.println("Client unregistered");
        }
        else {
            System.out.println("Unable to unregister client.");
        }
    }

   /* NOTIFICA DI UNA VARIAZIONE DEL VALORE,QUANDO CHIAMATO EFFETTUA CALLBACK
   * @param: value= variazione del valore di cui eseguire l'update, aggiornando i client via callback
   * */
    public void update(String value) throws RemoteException {
        doCallbacks(value);
    }

    /* EFFETTUA LA CALLBACK
     * @param: value= variazione del valore di cui eseguire l'update, aggiornando i client via callback
    * */
    private synchronized void doCallbacks(String value) throws RemoteException {
        System.out.println("Starting callbacks. for"+clients.toString());
        Iterator i = clients.iterator( );
        while (i.hasNext()) {
            ClientCallBackInterface client = (ClientCallBackInterface) i.next();
            client.notifyEvent(value);
        }
        System.out.println("Callbacks complete.");}
}
