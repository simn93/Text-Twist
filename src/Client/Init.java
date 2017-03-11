package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by SIMONE on 03/03/2017.
 *
 * Classe per la gestione dell'interfaccia grafica
 * per effettuare login e logout nell'account
 */
public class Init extends JPanel {
    private JPanel loginPanel;
    private JTextField username;
    private JPasswordField password;
    private JButton registerButton;
    private JButton loginButton;
    private JLabel loginErrorLabel;

    public Init(){
        this.setSize(400,200);
    }

    @Override
    public Dimension getSize(){
        return new Dimension(400,200);
    }

    public void setLogin(ActionListener action){
        loginButton.addActionListener(action);
    }

    public void setRegister(ActionListener action){
        registerButton.addActionListener(action);
    }

    public String getUser(){
        return this.username.getText();
    }

    public String getPassword(){
        return String.valueOf(this.password.getPassword());
    }

    public void setError(String error){
        this.loginErrorLabel.setText(error);
    }

    public void resetError(){
        this.loginErrorLabel.setText("");
    }

    public JPanel getPanel(){
        return loginPanel;
    }
}
