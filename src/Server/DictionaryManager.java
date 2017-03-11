package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by SIMONE on 02/03/2017.
 */
public interface DictionaryManager extends Remote {
    public final static String REMOTE_OBJECT_NAME = "DICTIONARY";

    public boolean isValid(String world)
            throws RemoteException;
}
