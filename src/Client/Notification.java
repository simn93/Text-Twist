package Client;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SIMONE on 06/03/2017.
 */
public class Notification extends JPanel{
    public JButton accept;
    public JButton reject;
    public JLabel ID;
    public int notificationID;

    public Notification(JButton accept, JButton reject, JLabel ID, int notificationID){
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.accept = accept;
        this.reject = reject;
        this.ID = ID;
        this.notificationID = notificationID;
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Notification)) return false;

        Notification other = (Notification) o;
        return this.notificationID == other.notificationID;
    }
}
