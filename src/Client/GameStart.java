package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by SIMONE on 08/03/2017.
 *
 * Classe per la gestione dell'interfaccia grafica
 * per la visualizzazione della conferma
 * necessaria prima dell'inzio di un match
 */
public class GameStart extends JPanel {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextPane TextPane;

    public GameStart() {
        this.setSize(300,300);
    }

    @Override
    public Dimension getSize(){
        return new Dimension(300,300);
    }

    public JPanel getPanel(){
        return this.contentPane;
    }

    public void setButtonOK(ActionListener action) {
        buttonOK.addActionListener(action);
    }

    public void setButtonCancel(ActionListener action) {
        buttonCancel.addActionListener(action);
    }
}
