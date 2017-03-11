package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by SIMONE on 04/03/2017.
 */
public class Game {
    // Codice identificativo unico della partita
    private int ID;

    // ID dell'utente che ha creato la partita
    private int admin;

    // Porta multicast sulla quale connettersi per ricevere i risultati della partita
    private int PORT;

    // Collegamento al clientManager
    private ClientManagerImpl clientManager;

    // Lista di giocatori registrati alla partita
    // E' composta di due campi: Il primo integer è l'ID del giocatore
    // Il secondo booleano indica se il giocatore ha confermato o meno la partecipazione alla partita
    private ConcurrentHashMap<Integer,Boolean> players;

    // Lista di giocatori che hanno inviato il loro punteggio
    private ConcurrentHashMap<Integer,Integer> scores;

    // Variabili per il controllo della partecipazione dei giocatori
    // E del tempo limite per consegnare la partecipazione
    private Lock lock;
    private Condition condition;
    private Timer timer;
    private boolean time_up;
    private int confirmed;

    // Lettere della partita
    private String letters;

    // Variabili per la gestione della consegna dei punteggi dei giocatori
    // E del tempo limite per la consegna
    private Lock udpLock;
    private Condition udpCondition;
    private Timer udpTimer;
    private boolean udp_time_up;
    private AtomicBoolean justOneTime;

    // Thread per la consegna dei risultati con multicast
    private ExecutorService udpMultiCastWaiter;



    // Costruttore
    public Game(int id, int admin, int port, ClientManagerImpl clientManager){
        this.ID = id;
        this.admin = admin;
        this.PORT = port;
        this.clientManager = clientManager;

        players = new ConcurrentHashMap<>();
        scores = new ConcurrentHashMap<>();

        lock = new ReentrantLock();
        condition = lock.newCondition();

        udpLock = new ReentrantLock();
        udpCondition = udpLock.newCondition();

        timer = new Timer();
        time_up = false;
        confirmed = 0;

        udpTimer = new Timer();
        udp_time_up = false;
        justOneTime = new AtomicBoolean(false);

        letters = generateLetters();

        udpMultiCastWaiter = Executors.newSingleThreadExecutor();

        // Avvio il primo timer
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lock.lock();
                time_up = true;
                condition.signalAll();
                lock.unlock();
                System.out.println("Timer scaduto!");
            }
        },7 * 60 * 1000);
    }

    // Getter and Setter
    public int getID() {
        return ID;
    }

    public int getAdmin() {
        return admin;
    }

    public int getPORT() {
        return PORT;
    }

    public String getLetters() {
        return letters;
    }

    // Inserisco un giocatore al quale è stato consegnato l'invito
    public void appendPlayer(int ID){ players.put(ID,false); }

    // Confermo un giocatore che ha accettato l'invito
    public void confirmPlayer(int ID){
        if(! players.put(ID,true)){
            lock.lock();
            confirmed++;
            condition.signalAll();
            lock.unlock();
        };
    }

    // Memorizzo il punteggio di un giocatore che ha terminato la partita
    public void setScore(int ID, int score){
        scores.put(ID,score);
        udpLock.lock();
        udpCondition.signal();
        udpLock.unlock();
    }

    /**
     * La connessione rimane bloccata in attesa della risoluzione di due casi:
     * * O il timer scade
     * * O tutti gli utendi partecipano alla partita
     *
     * In ogni caso, azzero il timer, e rispondo.
     */
    public boolean isStartAble(){
        //richieste arrivate in ritardo
        if(time_up) return false;

        lock.lock();
        while (! (time_up || confirmed == players.size())){
            try {
                System.out.println("time_up: "+time_up+" confirmed: "+confirmed+" size: "+players.size());
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        condition.signalAll();
        lock.unlock();
        timer.cancel();
        return !time_up;
    }

    // Genero le lettere iniziali
    // Prendendo 7 lettere casuali
    // Ma rispettando la frequenza di tali lettere nella lingua inglese
    // Fonte: Wikipedia
    private String generateLetters(){
        StringBuilder string = new StringBuilder();

        char[] alphabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        double[] frequency = {8.17, 1.49, 2.78, 4.25, 12.70, 2.23, 2.02, 6.09, 6.97, 0.15, 0.77, 4.03, 2.41, 6.75, 7.51, 1.93, 0.10, 5.99, 6.33, 9.06, 2.76, 0.98, 2.36, 0.15, 1.97, 0.07};

        RandomCollection<Character> collection = new RandomCollection<>();
        for(int i = 0; i < 26; i++ ){ collection.add(frequency[i],alphabet[i]); }

        int size = 0;
        while(size < Constant.GAME_LETTERS){
            char element = collection.next();
            if(! string.toString().contains(String.valueOf(element))){
                string.append(element);
                size++;
            }
        }

        return string.toString();
    }

    // La partita può iniziare
    // Chiamo questa funzione da tutti i membri che hanno effettuato la conferma di partecipazione
    // Ma effettivamente viene eseguito solo una volta
    // Avvio il timer per il tempo limite della ricezione dei punteggi
    // Avvio un thread in background per la consegna dei risultati con multicast
    public void matchStart(){
        if(justOneTime.compareAndSet(false,true)) {
            udpTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    udp_time_up = true;
                }
            }, 5 * 60 * 1000);

            udpMultiCastWaiter.submit(new Runnable() {
                @Override
                public void run() {
                    udpLock.lock();
                    while (!(udp_time_up || confirmed == scores.size())){
                        try {
                            System.out.println(confirmed + " " + scores.size() + " " +udp_time_up);
                            udpCondition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    udpLock.unlock();
                    udpTimer.cancel();

                    //in ogni caso, invio i punteggi in multicast
                    StringBuilder string = new StringBuilder();

                    Enumeration<Integer> player_s = players.keys();
                    while (player_s.hasMoreElements()) {
                        Integer current = player_s.nextElement();

                        string.append(clientManager.getNameFromId(current)).append(" ");
                        Integer score;
                        if((score = scores.get(current)) != null) {
                            string.append(score).append("\n");
                            clientManager.incClientScore(current,score);
                        } else {
                            string.append(0).append("\n");
                            clientManager.incClientScore(current,0);
                        }
                    }

                    try(MulticastSocket server = new MulticastSocket(PORT);){
                        server.setTimeToLive(16);
                        server.setLoopbackMode(true); //true to disable the LoopbackMode
                        server.setReuseAddress(true);

                        InetAddress multiCastGroup = InetAddress.getByName(Constant.SERVER_MULTICAST_IP);

                        byte[] data = string.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(data, data.length, multiCastGroup, PORT);

                        server.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
