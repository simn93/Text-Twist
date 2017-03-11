package Client;

import Server.Constant;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SIMONE on 08/03/2017.
 *
 * Classe per la gestione dell'interfaccia grafica della partita
 *
 * Consente di visualizzare quali sono le lettere della partita,
 * di inserire le parole trovate
 * e di inviare la soluzione al server
 */
public class Match extends JPanel{
    // Pannello dove vengono inserite le parole del gioco
    private JTextArea inputPanel;

    // Bottone per inviare al server le parole
    private JButton sendButton;

    // 7 etichette, una per ogni lettera
    private JLabel l1;
    private JLabel l2;
    private JLabel l3;
    private JLabel l4;
    private JLabel l5;
    private JLabel l6;
    private JLabel l7;

    // Etichetta per il tempo rimasto
    private JLabel timerLabel;

    // Pannello principale
    private JPanel matchPanel;

    // Caratteri rimasti
    private JLabel charLeft;

    // Stringa rappresentante le lettere possibili
    private String letters;

    // Timer
    private Timer timer;

    // Tempo rimasto
    private int time;

    // Informazioni del giocatore
    private int gameID;
    private int playerID;

    // Costruttore
    public Match(){
        super();
        this.setSize(500,500);

        // Operazioni standard alla pressione del tasto invia
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.cancel();
                sendWord();
            }
        });

        // Visualizza a schermo i numeri di caratteri rimasti da inserire
        inputPanel.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void warn(){
                charLeft.setText(((Constant.UDP_MAX_LENGTH - 3*Integer.BYTES - Constant.GAME_LETTERS) - inputPanel.getText().length())+"");
            }
        });
    }

    // Getter and Setter
    public void setGameID(int gameID) { this.gameID = gameID;  }

    public JPanel getPanel() {
        return matchPanel;
    }

    public void setInvia(ActionListener action){
        sendButton.addActionListener(action);
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    @Override
    public Dimension getSize(){
        return new Dimension(500,500);
    }

    // Divide la stringa sulle 7 etichette
    public void setLetters(String letters){
        if(letters.length() >= Constant.GAME_LETTERS){
            char[] letters_c = letters.toCharArray();
            l1.setText(String.valueOf(letters_c[0]));
            l2.setText(String.valueOf(letters_c[1]));
            l3.setText(String.valueOf(letters_c[2]));
            l4.setText(String.valueOf(letters_c[3]));
            l5.setText(String.valueOf(letters_c[4]));
            l6.setText(String.valueOf(letters_c[5]));
            l7.setText(String.valueOf(letters_c[6]));

            this.letters = letters;
        }
    }

    // Avvia la comunicazione udp col server
    private void sendWord(){
        String words = inputPanel.getText();
        Connection.udpSendWords(words,gameID,playerID,letters);
        System.out.println("Punteggio inviato");

        //reset
        inputPanel.setText(" ");
    }

    // Avvia la partita
    // Viene avviato il timer per il conteggio del tempo rimasto
    // Viene avviato il timer per l'invio finale delle parole
    public void start(){
        time = 120;

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendButton.doClick();
            }
        }, 2 * 60 * 1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerLabel.setText(time+"");
                time--;
            }
        }, 0, 1000);
    }
}
