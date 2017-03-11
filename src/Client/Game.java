package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by SIMONE on 03/03/2017.
 *
 * Classe per la gestione dell'interfaccia grafica
 * del pannello principale
 */
public class Game extends JPanel{
    // BOTTONI
    // Logout
    private JButton logoutButton;

    // Crea una nuova partita
    private JButton registerButton;

    // Visualizza la classifica generale
    private JButton highscore;

    // ETICHETTE
    // Visualizzazione dell'username
    private JLabel username_label;

    // Visualizzazione dell' ID del giocatore
    private JLabel ID_label;

    // Visualizzazione dell'esito della creazione di una partita
    private JLabel registerLog;

    // Visualizzazione dell'esito di un invito o di una partita
    private JLabel inviteLog;

    // PANNELLI
    // Pannello degli inviti
    private JPanel invitePanel;

    // Pannello per creazione di nuover partite
    private JPanel gamePanel;

    // Campi per l'inserimento degli id dei giocatori con i quali iniziare una nuova partita
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;

    // Link al gestore delle notifiche
    private GameCallManagerImpl callManager;

    // Vettore per la history delle notifiche
    private ArrayList<Notification> notifications;

    // Link al twistClient
    private TwistClient twistClient;

    // Costruttore
    public Game(GameCallManagerImpl callManager, TwistClient twistClient){
        this.callManager = callManager;
        this.notifications = new ArrayList<Notification>();
        this.twistClient = twistClient;

        invitePanel.setLayout(new BoxLayout(invitePanel, BoxLayout.PAGE_AXIS));
        this.setSize(800,1200);
    }

    @Override
    public Dimension getSize(){
        return new Dimension(800,1200);
    }

    // Getter and Setter
    public JPanel getPanel(){
        return gamePanel;
    }

    public void setLogout(ActionListener action){
        logoutButton.addActionListener(action);
    }

    public void setRegister(ActionListener action){
        registerButton.addActionListener(action);
    }

    public void setUsername_label(String username_label) {
        this.username_label.setText(username_label);
    }

    public void setID_label(String ID_label) {
        this.ID_label.setText(ID_label);
    }

    public void setHighScore(ActionListener action) {
        highscore.addActionListener(action);
    }

    public void setInviteLog(String text){
        inviteLog.setText(text);
    }

    public void setLog(String log){
        registerLog.setText(log);
    }

    // Test per l'input ( Creazione di una partita )
    private static boolean isInt(String str){
        try {
            int i = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Getter per l'input ( Creazione di una partita )
    public ArrayList<Integer> getFriends() {
        ArrayList<Integer> array = new ArrayList<>();

        if(textField1.getText().length() > 0 && isInt(textField1.getText())) array.add(Integer.parseInt(textField1.getText()));
        if(textField2.getText().length() > 0 && isInt(textField2.getText())) array.add(Integer.parseInt(textField2.getText()));
        if(textField3.getText().length() > 0 && isInt(textField3.getText())) array.add(Integer.parseInt(textField3.getText()));
        if(textField4.getText().length() > 0 && isInt(textField4.getText())) array.add(Integer.parseInt(textField4.getText()));
        if(textField5.getText().length() > 0 && isInt(textField5.getText())) array.add(Integer.parseInt(textField5.getText()));
        if(textField6.getText().length() > 0 && isInt(textField6.getText())) array.add(Integer.parseInt(textField6.getText()));
        if(textField7.getText().length() > 0 && isInt(textField7.getText())) array.add(Integer.parseInt(textField7.getText()));

        return array;
    }

    // Gestione della visualizzazione delle notifiche
    // Le notifiche arrivano dal server,
    // Un timer ogni 2 secondi raccoglie le nuove notifiche e chiama il metodo
    // Io creo un interfaccia per accettare o rifiutare tali notifiche
    public void updateInvite(ArrayList<GameCall> invites) {
        if(invites != null){
            for(GameCall call : invites){

                // Etichetta con l'id del chiamante
                JLabel label = new JLabel(call.getCallerId()+"");
                label.setVisible(true);

                // Bottone per accettare
                JButton accept = new JButton("Accetta");
                accept.setVisible(true);
                accept.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Rimuovo le altre richieste
                        callManager.removeAll();
                        invitePanel.removeAll();
                        invitePanel.revalidate();
                        invitePanel.repaint();
                        System.out.println("Vecchie richieste rimosse");

                        // Notifico il server
                        MyString letters = new MyString();
                        switch (Connection.tcpAcceptMatch(call.getGameId(),Integer.parseInt(ID_label.getText()),letters)){
                            case 0:
                                inviteLog.setText("Partita creata!");
                                twistClient.createNewMatch(call.getGameId(),letters.getString(), call.getPORT());
                                break;
                            case 1:
                                inviteLog.setText("Partita non creata, problema con gli altri utenti.");
                                break;
                            default:
                                inviteLog.setText("Errore nell'avvio della partita.");
                                break;
                        }
                    }
                });

                JButton reject = new JButton("Rifiuta");
                reject.setVisible(true);
                reject.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        invitePanel.remove(label);
                        invitePanel.remove(accept);
                        invitePanel.remove(reject);
                    }
                });

                Notification notification = new Notification(accept,reject,label,call.getID());
                notification.setVisible(true);

                if(! notifications.contains(notification)){
                    notifications.add(notification);
                    invitePanel.add(label);
                    invitePanel.add(accept);
                    invitePanel.add(reject);
                }
            }
        }
        invitePanel.revalidate();
        invitePanel.repaint();
    }
}
