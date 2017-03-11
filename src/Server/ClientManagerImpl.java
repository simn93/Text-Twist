package Server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import Client.*;

/**
 * Created by SIMONE on 02/03/2017.
 */
public class ClientManagerImpl extends RemoteObject implements ClientManager {
    private static final long serialVersionUID = 154L;

    // Lista di utenti
    private ConcurrentHashMap<Integer,Client> users;

    // Classifica utenti
    private ConcurrentSkipListSet<Client> scores;
    private AtomicLong lastUpdate;
    private final static long refreshRate = 30000L;

    // Gestione degli ID
    private AtomicInteger newIdUsers;

    // Costruttore
    public ClientManagerImpl(){
        users = new ConcurrentHashMap<Integer, Client>();
        scores = new ConcurrentSkipListSet<Client>();
        newIdUsers = new AtomicInteger(0);
        lastUpdate = new AtomicLong(0L);
    }

    // Getter and Setter
    public void setNewId(int id){
        this.newIdUsers.set(id);
    }

    public int getId(){
        return this.newIdUsers.get();
    }

    // Evito ogni modifica indiretta ai clienti nella collezione
    // Consento solo di accedere alla collezione per visionarne i dati
    public Collection<Client> getAllClients(){
        return Collections.unmodifiableCollection(users.values());
    }

    // Funzioni per il salvataggio e il ripristino dei dati
    public void addUser(Client c){
        users.put(c.getID(),c);
    }

    public void setClientScore(int ID, int score){
        Client user = users.get(ID);
        if( user != null){
            user.setScore(score);
        }
    }

    // Ottengo un ID nuovo per registrare un nuovo utente
    private int getNewId(){
        return newIdUsers.getAndIncrement();
    }

    // Aumento il punteggio del giocatore in seguito al risultato di una partita
    public void incClientScore(int ID, int score){
        Client user;

        user = users.get(ID);
        if( user != null){
            user.setScore(user.getScore() + score);
        }
    }

    // Funzione di compatibilità. Cerca l'ID a partire da username e password
    private int getIdFromName(String user, String password){
        Enumeration<Client> set = users.elements();
        while (set.hasMoreElements()){
            Client current = set.nextElement();
            if(current.getName().equals(user) && current.getPassword().equals(password)){
                return current.getID();
            }
        }
        return -1;
    }

    // Per una migliore esperienza utente
    public String getNameFromId(int id){
        Client client = users.get(id);
        if(client != null){
            return client.getName();
        } else {
            return "undefined";
        }
    }

    // Inoltro l'invito per una partita usando rmi
    public int sendInvite(int ID, int caller, int gameID, int PORT){
        if(! users.containsKey(ID)) return -1;

        Client client = users.get(ID);
        GameCallManager stub = client.getStub();

        int issue = -1;
        try {
            issue = stub.call(caller,gameID,PORT);
        } catch (RemoteException e) {
            System.out.println("Eccezione remota: disconnetto l'utente " + ID);
            users.get(ID).logout();
        }
        return issue;
    }

    // Testo se l'utente è online provando a fare una chiamata rmi
    // In seguito controllo il suo stato
    public boolean isClientOnline(int ID){
        if(! users.containsKey(ID)) return false;
        Client client = users.get(ID);

        try{
            GameCallManager stub = client.getStub();
            if(stub == null) return false;
            stub.isAlive();
        } catch (RemoteException e) {
            System.out.println("Eccezione remota: disconnetto l'utente " + ID);
            users.get(ID).logout();
            return false;
        }

        return client.isLogged();
    }

    // Aggiorna la classifica se necessario
    // e la restituisce
    public Collection<Client> getHighScore(){
        long now = System.currentTimeMillis();
        if(lastUpdate.get() < now - refreshRate){
            lastUpdate.set(now);

            scores.clear();
            for(Client c : users.values()){
                scores.add(c);
            }
        }
        return scores.descendingSet();
    }

    @Override
    public int register(String user, String password) throws RemoteException {
        boolean can = true;
        for(Client c : users.values()){
            if(c.getName().equals(user)){
                can = false;
            }
        }

        if(can) {
            int ID = getNewId();
            Client newClient = new Client(user, password, ID);
            users.put(ID, newClient);
            System.out.println("Nuovo utente: " + newClient);
            return ID;
        } else {
            return -1;
        }
    }

    @Override
    public int login(String user, String password, GameCallManager stub) throws RemoteException {
        int id = getIdFromName(user,password);
        if(id != -1) {
            Client logged = users.get(id);

            if(logged.isLogged()){
                System.out.println(logged + " già loggato. Sessione terminata.");
                logged.logout();
                return -2;
            }

            logged.login();
            logged.setStub(stub);
            System.out.println(logged + " ha loggato.");
            return id;
        }
        return -1;
    }

    @Override
    public int logout(String user, String password) throws RemoteException {
        int id = getIdFromName(user,password);
        if(id != -1){
            Client slogged = users.get(id);
            slogged.logout();
            slogged.setStub(null);
            System.out.println(slogged + " ha sloggato.");
            return id;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o){
        return o==this;
    }

    @Override
    public int hashCode(){
        return users.hashCode();
    }

    @Override
    public String toString(){
        return users.toString() + newIdUsers.get();
    }
}
