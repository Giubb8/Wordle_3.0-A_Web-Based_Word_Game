import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerCallBackImpl extends RemoteObject implements ServerCallBackInterface{


    private List<ClientCallBackInterface> clients;
    /* crea un nuovo servente */
    public ServerCallBackImpl()throws RemoteException {
        super( );
        clients = new ArrayList<ClientCallBackInterface>();
    }

    public void registerForCallback(ClientCallBackInterface ClientInterface) throws RemoteException {
        System.out.println("Dentroregistercallback");
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

    /* notifica di una variazione di valore dell'azione
   /* quando viene richiamato, fa il callback a tutti i client
   registrati */
    public void update(String value) throws RemoteException {
        doCallbacks(value);
    }

    private synchronized void doCallbacks(String value) throws RemoteException {
        System.out.println("Starting callbacks. for"+clients.toString());
        Iterator i = clients.iterator( );
        //int numeroClienti = clients.size( );
        while (i.hasNext()) {
            ClientCallBackInterface client = (ClientCallBackInterface) i.next();
            client.notifyEvent(value);
        }
        System.out.println("Callbacks complete.");}
}
