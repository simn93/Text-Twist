package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by SIMONE on 06/03/2017.
 *
 * Classe per la gestione dell'interfaccia grafica
 * per la visualizzazione delle classifiche
 * sia quella generale
 * che quella relativa ad ogni partita
 */
public class HighScore extends JPanel{
    private JButton OKButton;
    private JTextPane Classifica;
    private JPanel panel;

    public HighScore(){
        this.setSize(500,600);
    }

    @Override
    public Dimension getSize(){
        return new Dimension(500,600);
    }

    public void setClose(ActionListener action) {
        this.OKButton.addActionListener(action);
    }

    public void setClassifica(String classifica) {
        Classifica.setText(classifica);
        this.revalidate();
        this.repaint();
    }

    public JPanel getPanel() {
        return panel;
    }
}
