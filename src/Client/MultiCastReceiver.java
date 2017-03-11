package Client;

import Server.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by SIMONE on 10/03/2017.
 */
public class MultiCastReceiver implements Runnable {
    private int PORT;
    private HighScore scoreForm;

    public MultiCastReceiver(){

    }

    public void set(int PORT, HighScore scoreForm){
        this.PORT = PORT;
        this.scoreForm = scoreForm;
    }

    @Override
    public void run() {
        String result = Connection.udpMultiCastReceive(PORT);
        scoreForm.setClassifica(result);
    }
}
