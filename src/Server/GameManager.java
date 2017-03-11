package Server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

/**
 * Created by SIMONE on 04/03/2017.
 */
public class GameManager {
    private ConcurrentHashMap<Integer,Game> games;
    private AtomicInteger nextID;
    private AtomicInteger nextPORT;
    private ClientManagerImpl clientManager;

    public GameManager(ClientManagerImpl clientManager) {
        this.games = new ConcurrentHashMap<Integer, Game>();
        this.nextID = new AtomicInteger(0);
        this.nextPORT = new AtomicInteger(4000);
        this.clientManager = clientManager;
    }

    public Game getGame(int ID){
        return games.get(ID);
    }

    private int getNextID(){
        return nextID.getAndIncrement();
    }

    private int getNextPORT(){
        return nextPORT.getAndIncrement();
    }

    public int addGame(int Admin){
        int gameID = getNextID();
        Game newGame = new Game(gameID,Admin,getNextPORT(),clientManager);
        games.put(gameID,newGame);
        return gameID;
    }
}
