import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* CLASSE PER GESTIRE LE NOTIFICHE DA PARTE DEL SERVER */
public class MulticastNotificationService implements Runnable {
    final private static String WORD_NOT_EXISTS="WORD_NOT_EXISTS";
    private int port;
    byte[] buffer;
    List<String> notification= (List<String>) Collections.synchronizedList(new ArrayList<String>()); //LISTA PER CONTENERE LE NOTIFICHE

    public MulticastNotificationService(int port,List<String> notification){
        this.port=port;
        buffer=new byte[100];
        this.notification=notification;
    }

    /* OVERRIDE DEL METODO RUN
    * CREA LA SOCKET PER LA COMUNICAZIONE MULTICAST E RICEVE IL PACCHETTO UDP
    * @throws: UnknownHostException - Errore nella connessione all'host indicato
    * @throws: IOException - Errore nella comunicazione con gli stream UDP/socket multicast
    * */
    public void run() {
        /* MI UNISCO AL GRUPPO PER IL MULTICAST SU PORT E GROUP */
        InetAddress group= null ;
        try {
            group = InetAddress.getByName("228.5.6.7");
        } catch (UnknownHostException e) {
            System.out.println("Errore connessione ad Host MulticastNotificationService");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        MulticastSocket ms=null;
        try {
            ms=new MulticastSocket(port);
            ms.setReuseAddress(true);
            ms.joinGroup(group);
            DatagramPacket dp=new DatagramPacket(buffer, buffer.length);
            while(true){
                ms.receive(dp);
                String s=new String(dp.getData());
                if(true!=s.equals(WORD_NOT_EXISTS))
                    notification.add(s);
            }

        } catch (IOException e) {
            System.out.println("Errore comunicazione UDP MulticastNotificationService");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            if(ms!=null){
                try {
                    ms.leaveGroup(group);
                } catch (IOException e) {
                    System.out.println("errore socket multicast");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                ms.close();
            }
        }

    }
}
