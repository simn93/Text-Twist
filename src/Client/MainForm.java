package Client;

import javax.swing.*;

/**
 * Created by SIMONE on 03/03/2017.
 *
 * Classe Contenitore per la visualizzazione di JPanel
 */
public class MainForm extends JFrame{
    private JPanel mainForm;

    public MainForm(){
        super("TextTwist");
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(800,800);
    }

    public void setPanel(JPanel panel){
        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
    }
}
