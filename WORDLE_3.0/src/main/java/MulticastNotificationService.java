import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/* CLASSE PER GESTIRE LE NOTIFICHE DA PARTE DEL SERVER */
public class MulticastNotificationService implements Runnable {
    private int port;
    byte[] buffer;
    ArrayList<String> notification;

    public MulticastNotificationService(int port,ArrayList<String> notification){
        this.port=port;
        buffer=new byte[100];
        this.notification=notification;
    }

    public void run() {
        /* MI UNISCO AL GRUPPO PER IL MULTICAST SU PORT E GROUP */
        InetAddress group= null ;
        try {
            group = InetAddress.getByName("228.5.6.7");
        } catch (UnknownHostException e) {
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
                //System.out.println("WE SONO MULTICAST RICEVUTO "+s);
                notification.add(s);

                //System.out.println("size: "+notification.size()+"\n"+notification );
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(ms!=null){
                try {
                    ms.leaveGroup(group);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ms.close();
            }
        }

    }
}
