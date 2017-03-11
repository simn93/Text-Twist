package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import Client.*;

/**
 * Created by SIMONE on 02/03/2017.
 */
public interface ClientManager extends Remote {
    public final static String REMOTE_OBJECT_NAME = "CLIENTS";

    /**
     * @param user
     * @param password
     * @return ID assegnato al giocatore registrato
     * @return -1 in caso di errore
     * @throws RemoteException
     */
    public int register(String user, String password)
            throws RemoteException;

    /**
     * @param user
     * @param password
     * @param stub
     * @return ID del giocatore
     * @return -1 se il giocatore non è registrato
     * @return -2 se il giocatore era già loggato
     * @throws RemoteException
     */
    public int login(String user, String password, GameCallManager stub)
            throws RemoteException;

    /**
     * @param user
     * @param password
     * @return ID del giocatore
     * @return -1 se il giocatore non è registrato
     * @throws RemoteException
     */
    public int logout(String user, String password)
            throws RemoteException;
}
