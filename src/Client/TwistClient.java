package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Server.*;

/**
 * Created by SIMONE on 02/03/2017.
 *
 * main class
 */

public class TwistClient {
    // Player data
    private String user;
    private String password;
    private int ID;

    // Form
    private MainForm form;
    private Init loginForm;
    private Game gameForm;
    private HighScore scoreForm;
    private Match matchForm;

    // registry
    private Registry registry;

    // Stub per login/logout/register
    private ClientManager manager;

    // Gestione delle richieste di partita
    private GameCallManagerImpl invites;
    private GameCallManager invitesStub;

    // Timer per l'update delle notifiche
    private Timer requestUpdate;

    // Thread runner per l'attesa del multicast
    private ExecutorService multiCastReceiver;

    // Thread per l'attesa del multicast
    private MultiCastReceiver thread;

    // Costruttore
    public TwistClient(){
        form = new MainForm();
        loginForm = new Init();

        scoreForm = new HighScore();
        matchForm = new Match();
        invites = new GameCallManagerImpl();
        requestUpdate = new Timer();
        multiCastReceiver = Executors.newSingleThreadExecutor();
        thread = new MultiCastReceiver();

        gameForm = new Game(invites,this);
    }

    // Avvio il client
    public void start(){
        // Security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        System.setProperty("java.security.policy","file:./src/Client/permission.policy");

        // Ogni 2 secondi controllo se ci sono nuove richieste, e aggiorno l'interfaccia
        requestUpdate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ArrayList<GameCall> vector = invites.getCall();
                gameForm.updateInvite(vector);
            }
        },2000,2000);

        try {
            // mi connetto alla registry e ottengo lo stub
            registry = LocateRegistry.getRegistry(Constant.SERVER_HOST, Constant.REGISTRY_PORT);
            manager = (ClientManager) registry.lookup(ClientManager.REMOTE_OBJECT_NAME);

            // preparo lo stub per le GameCall
            invitesStub = (GameCallManager) UnicastRemoteObject.exportObject(invites,0);

            // Setto l'interfaccia
            setUI();

            // Avvio il pannello per il login
            form.setPanel(loginForm.getPanel());
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

    }

    // Imposta l'interfaccia
    public void setUI(){

        // login con rmi
        loginForm.setLogin(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int ret = manager.login(loginForm.getUser(),loginForm.getPassword(),invitesStub);
                    switch (ret){
                        case -1:
                            loginForm.setError("User o password errati.");
                            break;
                        case -2:
                            loginForm.setError("Utente già loggato. Sessione terminata. Riprova.");
                            break;
                        default:
                            ID = ret;
                            user = loginForm.getUser();
                            password = loginForm.getPassword();

                            gameForm.setUsername_label(user);
                            gameForm.setID_label(ID+"");

                            loginForm.resetError();
                            form.setPanel(gameForm.getPanel());
                            break;
                    }
                } catch (RemoteException e1) {
                    loginForm.setError("Errore remoto. Riprova.");
                }
            }
        });

        // registrazione nuovo utente con rmi
        loginForm.setRegister(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(loginForm.getUser().length() == 0 || loginForm.getPassword().length() == 0){
                        loginForm.setError("Nessun nome utente o password inseriti.");
                    } else {
                        ID = manager.register(loginForm.getUser(), loginForm.getPassword());
                        if(ID == -1){
                            loginForm.setError("Nome utente già registrato. Riprova.");
                        } else {
                            loginForm.setError("Utente Registrato! Ora puoi effettuare il login.");
                        }
                    }
                } catch (RemoteException e1) {
                    loginForm.setError("Errore Remoto. Riprova");
                }
            }
        });

        // logout con rmi
        gameForm.setLogout(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int ret;
                    if((ret = manager.logout(user,password)) == -1){
                        gameForm.setLog("User o password errati.");
                    } else {
                        ID = ret;

                        //reset log
                        loginForm.resetError();
                        gameForm.setLog(" ");
                        gameForm.setInviteLog(" ");

                        form.setPanel(loginForm.getPanel());
                    }
                } catch (RemoteException e1) {
                    gameForm.setLog("Errore Remoto. Riprova");
                }
            }
        });

        // creazione nuova partita con tcp
        gameForm.setRegister(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = Connection.tcpCreateMatch(gameForm.getFriends(),ID);
                switch (response){
                    case 0:
                        gameForm.setLog("Partita creata!");
                        return;
                    case 1:
                        gameForm.setLog("Impossibile creare la partita, riprova più tardi");
                        return;
                    default:
                        gameForm.setLog("Errore di connessione, riprova più tardi");
                        break;
                }
            }
        });

        // Classifica generale ottenuta con tcp
        gameForm.setHighScore(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreForm.setClassifica(Connection.tcpGetHighScore());
                form.setPanel(scoreForm.getPanel());
                return;
            }
        });

        // chiusura pannello classifica
        scoreForm.setClose(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                form.setPanel(gameForm.getPanel());
            }
        });
    }

    // Avvia una nuova partita
    public void createNewMatch(int gameID, String letters, int PORT){

        // creo un nuovo pannello di avviso
        GameStart gameStart = new GameStart();

        // bottone per annullare la partita
        gameStart.setButtonCancel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                form.setPanel(gameForm.getPanel());
            }
        });

        // bottone per iniziare la partita
        // Imposto anche il pannello della partita
        gameStart.setButtonOK(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                matchForm.setGameID(gameID);
                matchForm.setLetters(letters);
                matchForm.setPlayerID(ID);
                matchForm.setInvia(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        form.setPanel(scoreForm.getPanel());
                        gameForm.setInviteLog("Partita conclusa");
                    }
                });
                matchForm.start();
                form.setPanel(matchForm.getPanel());
            }
        });

        form.setPanel(gameStart.getPanel());

        //avvio la ricezione multicast
        scoreForm.setClassifica("In attesa dei risultati...");
        thread.set(PORT,scoreForm);
        multiCastReceiver.submit(thread);
    }
}

