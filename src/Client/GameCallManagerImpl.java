package Client;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SIMONE on 03/03/2017.
 */
public class GameCallManagerImpl extends RemoteObject implements GameCallManager {
    private static final long serialVersionUID = 344L;

    private AtomicInteger inviteID;
    private ConcurrentHashMap<Integer,GameCall> list;

    public GameCallManagerImpl(){
        inviteID = new AtomicInteger(0);
        list = new ConcurrentHashMap<Integer,GameCall>();
    }

    private int getNextId(){
        return inviteID.getAndIncrement();
    }

    @Override
    public int call(int caller, int game, int PORT) throws RemoteException {
        int id = getNextId();
        GameCall gameCall = new GameCall(caller,game,id,PORT);

        list.put(id,gameCall);
        return 0;
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    public ArrayList<GameCall> getCall(){
        ArrayList<GameCall> ret = new ArrayList<GameCall>(list.values());
        return ret;
    }

    public void removeAll(){
        list.clear();
    }
}
