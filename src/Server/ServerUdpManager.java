package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;

/**
 * Created by SIMONE on 09/03/2017.
 */
public class ServerUdpManager implements Runnable {
    private MyBoolean haveToLive;
    private GameManager gameManager;
    private ExecutorService pool;

    private byte[] udpMessage;
    private DatagramPacket request;

    public ServerUdpManager(MyBoolean haveToLive, GameManager gameManager, ExecutorService pool){
        this.haveToLive = haveToLive;
        this.gameManager = gameManager;
        this.pool = pool;

        udpMessage = new byte[Constant.UDP_MAX_LENGTH];
        request = new DatagramPacket(udpMessage,Constant.UDP_MAX_LENGTH);
    }

    @Override
    public void run() {
        try(DatagramSocket udpServer = new DatagramSocket(Constant.UDP_PORT);){
            while (haveToLive.getBool()) {
                request.setLength(Constant.UDP_MAX_LENGTH);
                udpServer.receive(request);

                System.out.println("Gestione richiesta upd...");
                pool.submit(new ClientUdpReader(gameManager, request.getData()));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
