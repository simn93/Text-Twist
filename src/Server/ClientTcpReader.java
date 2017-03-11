package Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by SIMONE on 04/03/2017.
 */
public class ClientTcpReader implements Runnable {
    SelectionKey key;
    Selector selector;
    ClientManagerImpl clientManager;
    GameManager gameManager;

    public ClientTcpReader(SelectionKey key, Selector selector, ClientManagerImpl clientManager, GameManager gameManager){
        this.key = key;
        this.selector = selector;
        this.clientManager = clientManager;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {

        try{
            SocketChannel client=(SocketChannel)key.channel();
            ByteBuffer[] buffers= (ByteBuffer[]) key.attachment();

            client.read(buffers);
            if(! buffers[0].hasRemaining()){
                buffers[0].flip();

                switch (buffers[0].getInt()){
                    case 0:
                        // richiesta nuova partita
                        System.out.println("Gestione nuova partita...");

                        if(! buffers[1].hasRemaining()){
                            buffers[1].flip();
                            int n_users = buffers[1].getInt();
                            System.out.println("Partita tra " + n_users + " utenti");

                            // Vettore degli ID degli utenti coinvolti nella partita
                            // L'admin è quello in posizione 0
                            buffers[2].limit(n_users * Integer.BYTES);
                            if(! buffers[2].hasRemaining()){
                                buffers[2].flip();
                                int[] users = new int[n_users];

                                for (int i = 0; i < n_users ; i++){
                                    users[i] = buffers[2].getInt();
                                }

                                ByteBuffer[] answer = new ByteBuffer[2];
                                answer[0] = ByteBuffer.allocate(Integer.BYTES);
                                answer[1] = ByteBuffer.allocate(Integer.BYTES);

                                answer[0].putInt(0);
                                answer[0].flip();

                                //Dati pronti
                                for (int i = 0; i <n_users ; i++){
                                    if(!clientManager.isClientOnline(users[i])){
                                        System.out.println("Client with ID: " + users[i] + " not Exist or is offline.");
                                        System.out.println("Game abort.");

                                        answer[1].putInt(1);;
                                        answer[1].flip();

                                        client.register(this.selector,SelectionKey.OP_WRITE,answer);
                                        this.selector.wakeup();
                                        return;
                                    }
                                }

                                //Utenti tutti online
                                //Creo il gioco
                                int gameID = gameManager.addGame(users[0]);

                                //Invito gli inviti
                                //E setto i giocatori della partita
                                for(int id : users){
                                    clientManager.sendInvite(id,users[0],gameID,gameManager.getGame(gameID).getPORT());
                                    gameManager.getGame(gameID).appendPlayer(id);
                                    System.out.println("Invio invito a " + id + " per la partita " + gameID + ".");
                                }

                                answer[1].putInt(0);
                                answer[1].flip();

                                client.register(this.selector,SelectionKey.OP_WRITE,answer);
                                this.selector.wakeup();
                                System.out.println("...Partita creata!");
                            } else {
                                key.interestOps(SelectionKey.OP_READ);
                                this.selector.wakeup();
                            }
                        } else {
                            key.interestOps(SelectionKey.OP_READ);
                            this.selector.wakeup();
                        }
                            break;
                    case 1:
                        // gestione della partita
                        System.out.println("Avvio nuova partita...");
                        if(! buffers[1].hasRemaining()){
                            buffers[1].flip();
                            int gameID = buffers[1].getInt();

                            if(buffers[2].position() == Integer.BYTES){
                                buffers[2].flip();
                                int myID = buffers[2].getInt();

                                //Dati pronti
                                //confermo il giocatore
                                gameManager.getGame(gameID).confirmPlayer(myID);
                                System.out.println(myID + " confermato.");

                                //preparo il buffer
                                ByteBuffer[] answer = new ByteBuffer[3];
                                answer[0] = ByteBuffer.allocate(Integer.BYTES);
                                answer[1] = ByteBuffer.allocate(Integer.BYTES);
                                answer[2] = ByteBuffer.allocate(Constant.GAME_LETTERS);

                                answer[0].putInt(1);
                                answer[0].flip();

                                //Resto in attesa della risposta
                                if(gameManager.getGame(gameID).isStartAble()){
                                    //tutti i partecipanti hanno confermato, invio le lettere
                                    answer[1].putInt(0);
                                    answer[1].flip();

                                    answer[2].put(gameManager.getGame(gameID).getLetters().getBytes());
                                    answer[2].flip();

                                    gameManager.getGame(gameID).matchStart();//Avvio timer udp e multicast
                                    System.out.println("...Partita avviata!");
                                } else {
                                    //time up, cancello la partita
                                    answer[1].putInt(1);
                                    answer[1].flip();
                                    System.out.println("...Partita non avviata. Gioco abortito.");
                                }

                                client.register(this.selector,SelectionKey.OP_WRITE,answer);
                                this.selector.wakeup();
                            } else {
                                key.interestOps(SelectionKey.OP_READ);
                                this.selector.wakeup();
                            }
                        } else {
                            key.interestOps(SelectionKey.OP_READ);
                            this.selector.wakeup();
                        }
                        break;
                    case 2:
                        // return Classifica
                        System.out.println("Invio classifica generale...");

                        Collection<Client> clients = clientManager.getHighScore();


                        ByteBuffer[] answer = new ByteBuffer[3];
                        answer[0] = ByteBuffer.allocate(Integer.BYTES);
                        answer[1] = ByteBuffer.allocate(Integer.BYTES);
                        answer[2] = ByteBuffer.allocate(clients.size() * (Constant.MAX_NAME_SIZE + Integer.BYTES));

                        answer[0].putInt(2);
                        answer[0].flip();

                        answer[1].putInt(Math.min(clients.size(),Constant.MAX_HIGHSCORE_SIZE));
                        answer[1].flip();

                        for(Client c : clients){
                            ClientScore clientScore = c.getClientScore();
                            String name = clientScore.getName();

                            //Scrivo la stringa come una sequenza di caratter
                            //Aggiungo " " alla fine per riempire
                            byte[] nameByte = name.getBytes();
                            int nameByteLength = nameByte.length;

                            answer[2].put(nameByte);

                            byte[] space = (new String(" ")).getBytes();
                            while (nameByteLength < Constant.MAX_NAME_SIZE){
                                answer[2].put(space);
                                nameByteLength ++;
                            }

                            //Aggiungo il punteggio
                            answer[2].putInt(clientScore.getScore());
                        }
                        answer[2].flip();

                        //Dati pronti
                        System.out.println("...dati pronti per l'invio...");
                        client.register(this.selector,SelectionKey.OP_WRITE,answer);
                        this.selector.wakeup();

                        break;
                    default:
                        throw new IOException("ClientTcpOperation: Operation not supported");
                }
            } else {
                key.interestOps(SelectionKey.OP_READ);
                this.selector.wakeup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
