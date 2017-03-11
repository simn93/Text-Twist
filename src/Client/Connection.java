package Client;

import Server.Constant;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Created by SIMONE on 11/03/2017.
 *
 * Classe per la gestione delle connessioni
 */
public abstract class Connection {
    /**
     * Protocollo:
     * Invio: <gameID, playerID, game_letters, words_length, words>
     * Ricevo: <>
     */
    public static void udpSendWords(String words, int gameID, int playerID, String letters){
        try(DatagramSocket client = new DatagramSocket();) {
            ByteBuffer buffer = ByteBuffer.allocate(3 * Integer.BYTES + Constant.GAME_LETTERS + words.length());
            buffer.putInt(gameID);
            buffer.putInt(playerID);
            buffer.put(letters.getBytes());
            buffer.putInt(words.length());
            buffer.put(words.getBytes());

            buffer.flip();

            InetSocketAddress server = new InetSocketAddress(Constant.SERVER_HOST, Constant.UDP_PORT);
            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.limit(), server);
            client.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Protocollo:
     * Invio: <>
     * Ricevo: <Classifica>
     */
    public static String udpMultiCastReceive(int PORT){
        try(MulticastSocket client = new MulticastSocket(PORT); ) {
            InetAddress multiCastGroup = InetAddress.getByName(Constant.SERVER_MULTICAST_IP);
            client.joinGroup(multiCastGroup);
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            client.receive(packet);
            return new String(packet.getData());
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Errore nella ricezione dei risultati";
    }

    /**
     * Protocollo:
     * Invio: <codice_accetta, gameID, playerID>
     * Ricevo: <codice_risultato>
     *     0: ok
     *     1: no ok
     */
    public static int tcpAcceptMatch(int gameID, int playerID, MyString letters){
        //Notifico il server
        try( SocketChannel client = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), Constant.SERVER_PORT)); ){
            client.configureBlocking(false);

            ByteBuffer[] request = new ByteBuffer[3];
            request[0] = ByteBuffer.allocate(Integer.BYTES);
            request[1] = ByteBuffer.allocate(Integer.BYTES);
            request[2] = ByteBuffer.allocate(Integer.BYTES);

            request[0].putInt(Constant.ANSWER_GAME);
            request[1].putInt(gameID);
            request[2].putInt(playerID);

            request[0].flip();
            request[1].flip();
            request[2].flip();

            client.write(request);

            ByteBuffer[] answer = new ByteBuffer[2];
            answer[0] = ByteBuffer.allocate(Integer.BYTES);
            answer[1] = ByteBuffer.allocate(Constant.GAME_LETTERS);

            System.out.println("Lettura risposta server");
            while (client.read(answer) != -1){
                if(! answer[0].hasRemaining()){
                    answer[0].flip();
                    int response = answer[0].getInt();

                    if(response == 0){
                        // Partita creata
                        if(! answer[1].hasRemaining()){
                            answer[1].flip();
                            char[] letters_c = new char[Constant.GAME_LETTERS];
                            int i = 0;

                            while (answer[1].hasRemaining()){
                                letters_c[i] = (char) answer[1].get();
                                i++;
                            }
                            letters.setString(new String(letters_c));
                            client.close();

                            return 0;
                        }
                    } else {
                        //partita non creata
                        client.close();
                        return 1;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Protocollo:
     * Invio: <codice_nuova_partita, friendList_size, friendList>
     * Ricevo: <codice_risultato>
     *     0: ok
     *     1: no ok
     */
    public static int tcpCreateMatch(ArrayList<Integer> friendList, int playerID){
        try( SocketChannel client = SocketChannel.open(new InetSocketAddress(Constant.SERVER_HOST,Constant.SERVER_PORT));){
            client.configureBlocking(false);

            ByteBuffer[] buffer = new ByteBuffer[3];
            buffer[0] = ByteBuffer.allocate(Integer.BYTES);
            buffer[1] = ByteBuffer.allocate(Integer.BYTES);
            buffer[2] = ByteBuffer.allocate(Constant.MAX_GAME_SIZE);

            ByteBuffer answer = ByteBuffer.allocate(Integer.BYTES);

            if(friendList.size() == 0) return 1;

            System.out.println("Avvio partita con " + friendList.size() + " amici.");

            buffer[0].putInt(Constant.REGISTER_GAME);
            buffer[1].putInt(friendList.size() + 1);
            buffer[2].putInt(playerID);
            for (Integer friend : friendList){
                buffer[2].putInt(friend);
            }

            buffer[0].flip();
            buffer[1].flip();
            buffer[2].flip();

            client.write(buffer);

            while(client.read(answer) != -1){
                if(! answer.hasRemaining()){
                    answer.flip();
                    return answer.getInt();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return 2;
    }

    /**
     * Protocollo:
     * Invio: <codice_classifica>
     * Ricevo: <classifica_size, classifica>
     */
    public static String tcpGetHighScore(){
        try( SocketChannel client = SocketChannel.open(new InetSocketAddress(Constant.SERVER_HOST,Constant.SERVER_PORT));){
            client.configureBlocking(false);

            ByteBuffer request = ByteBuffer.allocate(Integer.BYTES);
            request.putInt(Constant.GET_HIGHSCORE);
            request.flip();
            client.write(request);

            ByteBuffer[] answer = new ByteBuffer[2];
            answer[0] = ByteBuffer.allocate(Integer.BYTES);
            answer[1] = ByteBuffer.allocate(Constant.MAX_HIGHSCORE_SIZE * (Constant.MAX_NAME_SIZE + Integer.BYTES));

            StringBuilder highScore = new StringBuilder();
            while (client.read(answer) != -1){
                if(! answer[0].hasRemaining()){
                    answer[0].flip();
                    int size = answer[0].getInt();

                    if(answer[1].position() == size*(Constant.MAX_NAME_SIZE + Integer.BYTES)){
                        answer[1].flip();

                        for(int j = 0; j < size; j++) {
                            char[] name_c = new char[Constant.MAX_NAME_SIZE];
                            for (int i = 0; i < Constant.MAX_NAME_SIZE; i++) {
                                name_c[i] = (char) answer[1].get();
                            }
                            String name = new String(name_c);

                            int point = answer[1].getInt();

                            highScore.append(name);
                            highScore.append(" ");
                            highScore.append(point);
                            highScore.append("\n");
                        }

                        return highScore.toString();
                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "Errore nella ricezione della classifica";
    }
}
