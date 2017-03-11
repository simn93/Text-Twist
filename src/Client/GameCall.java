package Client;

import java.io.Serializable;

/**
 * Created by SIMONE on 03/03/2017.
 */
public class GameCall implements Serializable, Comparable, Cloneable{
    private int CallerId;
    private int GameId;
    private int ID;
    private int PORT;

    public GameCall(int callerId, int gameId, int ID, int PORT) {
        this.CallerId = callerId;
        this.GameId = gameId;
        this.ID = ID;
        this.PORT = PORT;
    }

    public int getCallerId() {
        return CallerId;
    }

    public int getGameId() {
        return GameId;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getPORT() {
        return PORT;
    }

    @Override
    public int compareTo(Object o) {
        GameCall other = (GameCall) o;
        return this.ID - other.ID;
    }

    @Override
    public String toString(){
        return "ID: " + this.ID + ". MasterID: " + this.CallerId + ".";
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof GameCall)) return false;

        GameCall other = (GameCall) o;
        return this.ID == other.ID;
    }
}
