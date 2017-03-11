package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import Client.*;

/**
 * Created by SIMONE on 04/03/2017.
 */
public class ClientTcpWriter implements Runnable {
    SelectionKey key;
    Selector selector;
    ClientManagerImpl clientManager;


    public ClientTcpWriter(SelectionKey key, Selector selector, ClientManagerImpl clientManager){
        this.key = key;
        this.selector = selector;
        this.clientManager = clientManager;
    }

    @Override
    public void run() {
        try{
            SocketChannel client = (SocketChannel) this.key.channel();
            ByteBuffer[] buffer = (ByteBuffer[]) this.key.attachment();

            switch (buffer[0].getInt()){
                case 0:
                    //Richiesta di nuova partita: Invio dell'esito
                    System.out.println("Inoltro esito gestione partita...");
                    client.write(buffer[1]);
                    if(! buffer[1].hasRemaining()){
                        client.close();
                        System.out.println("...Esito gestione partita inoltrato.");
                    } else {
                        this.key.interestOps(SelectionKey.OP_WRITE);
                        this.selector.wakeup();
                    }
                    break;
                case 1:
                    System.out.println("Inoltro esito avvio partita...");
                    client.write(buffer[1]);
                    if(! buffer[1].hasRemaining()){
                        buffer[1].flip();

                        if(buffer[1].getInt() == 0){
                            //partita avviata correttamente, inoltro le parole
                            client.write(buffer[2]);
                            if(! buffer[2].hasRemaining()){
                                System.out.println("...lettere inoltrate...");
                            } else {
                                this.key.interestOps(SelectionKey.OP_WRITE);
                                this.selector.wakeup();
                            }
                        }
                        System.out.println("...esito avvio partita inoltrato.");
                    } else {
                        this.key.interestOps(SelectionKey.OP_WRITE);
                        this.selector.wakeup();
                    }
                    break;
                case 2:
                    System.out.println("Inoltro classifica...");
                    client.write(buffer[1]);
                    if(! buffer[1].hasRemaining()){
                        client.write(buffer[2]);
                        if(! buffer[2].hasRemaining()){
                            client.close();
                            System.out.println("...Classifica inoltrata.");
                        } else {
                            this.key.interestOps(SelectionKey.OP_WRITE);
                            this.selector.wakeup();
                        }
                    } else {
                        this.key.interestOps(SelectionKey.OP_WRITE);
                        this.selector.wakeup();
                    }
                    break;
                default:
                    throw new IOException("ServerTcpOperation: Operation not supported");
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.key.cancel();
        }
    }
}
