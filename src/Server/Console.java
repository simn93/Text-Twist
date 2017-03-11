package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;

/**
 * Created by SIMONE on 05/03/2017.
 */
public class Console implements Runnable {
    private TwistServer server;

    public Console(TwistServer server){
        this.server = server;
    }

    @Override
    public void run() {
        while (true){
            try{
                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                String line;
                switch (line = r.readLine()){
                    case "quit":
                        server.haveToLive.setBool(false);
                        return;
                    case "who":
                        int online = 0;
                        Collection<Client> clients = server.clientManager.getAllClients();
                        System.out.println("Sono connessi: ");

                        for(Client client : clients){
                            if(client.isLogged()){
                                System.out.println(client);
                                online++;
                            }
                        }

                        if(online == 0){
                            System.out.println("Nessun utente connesso.");
                        }
                        break;
                    default:
                        System.out.println("Comando non riconosciuto");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
