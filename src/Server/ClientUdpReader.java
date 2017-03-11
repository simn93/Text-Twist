package Server;

import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by SIMONE on 08/03/2017.
 */
public class ClientUdpReader implements Runnable {
    GameManager gameManager;
    ByteBuffer buffer;

    public ClientUdpReader(GameManager gameManager, byte[] data){
        this.gameManager = gameManager;
        this.buffer = ByteBuffer.allocate(Constant.UDP_MAX_LENGTH);
        this.buffer.put(data);
    }

    @Override
    public void run() {
        System.out.println("...analisi richiesta upd...");
        buffer.flip();

        // Get game ID
        int gameID = buffer.getInt();

        // Get player ID
        int playerID = buffer.getInt();

        // Get game Letters
        byte[] letters_b = new byte[Constant.GAME_LETTERS];
        buffer.get(letters_b);
        String letters = new String(letters_b);

        // Get game words lenght
        int size = buffer.getInt();

        // Get game words
        byte[] chars = new byte[size];
        buffer.get(chars);
        String longWord = new String(chars);

        // Split words
        String[] words = longWord.split("[ \n]");

        // Check words
        int points = 0;
        try {
            // Dictionary connection
            Registry registry = LocateRegistry.getRegistry(Constant.SERVER_HOST, Constant.REGISTRY_PORT);
            DictionaryManager dictionary = (DictionaryManager) registry.lookup(DictionaryManager.REMOTE_OBJECT_NAME);

            // Per Ogni parola inserita
            // Se:
            // 1: E' composta solo di lettere fornite dal gioco.
            // 2: Non è già stata inserita prima.
            // 3: E' presente nel dizionario
            // Allora:
            // Incremento il punteggio di un valore proporzionale alla sua lunghezza
            int i = 0;
            for(String s : words){
                if(s.matches("["+letters+"]*") && !contains(words,0,i-1,s) && dictionary.isValid(s)){
                    points += s.length();
                }
                i++;
            }

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        Game game = gameManager.getGame(gameID);
        game.setScore(playerID,points);
        System.out.println("Punteggio ricevuto dal player " + playerID + " : " + points);
    }

    private boolean contains(String[] source, int from, int to, String target){
        if(from > to || from < 0 || to > source.length) return false;
        for(int i = from; i <= to; i++){
            if(source[i].equals(target)) return true;
        }
        return false;
    }
}
