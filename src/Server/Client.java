package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import Client.*;

/**
 * Created by SIMONE on 2017/03/01.
 */
public class Client implements Serializable, Comparable{
    private static final long serialVersionUID = 1L;

    // Identificatore unico dell'utente
    private int ID;

    // username
    private String name;

    // password
    private String password;

    // punteggio
    private int score;

    // flag di log
    private Boolean isLogged;

    // stub per rmi callback
    private GameCallManager stub;

    //Costruttore
    public Client(String name, String password, int ID){
        this.ID = ID;
        this.name = name;
        this.password = password;
        this.isLogged = false;
        this.stub = null;
    }

    // Getter and Setter
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public int getID() { return ID; }

    public Boolean isLogged() {
        return isLogged;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GameCallManager getStub() {
        return stub;
    }

    public void setStub(GameCallManager stub) {
        this.stub = stub;
    }

    // Funzione di compatibilità. Restitusce un oggetto con la coppia <nome,punteggio> per la generazione della classifica
    public ClientScore getClientScore(){
        return new ClientScore(this.name,this.score);
    }

    // Gestione del login e del logout con un boolean
    public void login() {
        isLogged = true;
    }

    public void logout() {
        isLogged = false;
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Client)) return false;

        Client other = (Client) o;
        return this.ID == other.ID;
    }

    @Override
    public int compareTo(Object o) {
        Client other = (Client) o;
        return this.score - other.score;
    }

    @Override
    public String toString(){
        return "ID: " + this.ID + ". Nome: " + this.name + ". Punti: " + this.score + ".";
    }
}
