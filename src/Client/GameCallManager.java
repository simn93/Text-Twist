package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by SIMONE on 03/03/2017.
 */
public interface GameCallManager extends Remote {
    public final static String REMOTE_OBJECT_NAME = "CALLS";

    public int call(int caller, int game, int PORT)
            throws RemoteException;

    public boolean isAlive()
        throws RemoteException;
}
