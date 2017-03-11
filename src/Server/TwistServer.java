package Server;

import org.json.simple.ItemList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;

import static java.lang.Boolean.TRUE;

/**
 * Created by SIMONE on 2017/03/01.
 */
public class TwistServer {
    // Oggetto per la gestione dei clienti
    public ClientManagerImpl clientManager;

    // Oggetto per la gestione del dizionario
    private DictionaryManagerImpl dictionaryManager;

    // Oggetto per la gestione delle partite
    private GameManager gameManager;

    // ThreadPool
    private ExecutorService pool;

    // Variabile di condizione per la gestione del loop principale
    public MyBoolean haveToLive;

    // Costruttore
    public TwistServer(){
        clientManager = new ClientManagerImpl();
        dictionaryManager = new DictionaryManagerImpl(Constant.dict_path);
        gameManager = new GameManager(clientManager);
        pool = Executors.newCachedThreadPool();
        haveToLive = new MyBoolean(true);
    }

    // Avvio il server
    public void start(){
        try(ServerSocketChannel server = ServerSocketChannel.open()){
            //CONSOLE
            pool.submit(new Console(this));

            //REGISTRY
            Registry registry = LocateRegistry.createRegistry(Constant.REGISTRY_PORT);
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "10000");

            ClientManager clientManagerStub = (ClientManager) UnicastRemoteObject.exportObject(clientManager,0);
            registry.rebind(ClientManager.REMOTE_OBJECT_NAME, clientManagerStub);

            DictionaryManager dictionaryManagerStub = (DictionaryManager) UnicastRemoteObject.exportObject(dictionaryManager,0);
            registry.rebind(DictionaryManager.REMOTE_OBJECT_NAME, dictionaryManagerStub);

            //RIPRISTINO DATI
            loadSetting();

            //TCP
            server.bind(new InetSocketAddress(InetAddress.getLocalHost(),Constant.SERVER_PORT));
            Selector selector = Selector.open();
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);

            //UDP
            pool.submit(new ServerUdpManager(haveToLive,gameManager,pool));

            while (haveToLive.getBool()){
                //TCP
                selector.selectedKeys().clear();
                selector.select(100);

                for(SelectionKey key : selector.selectedKeys()){
                    if(key.isAcceptable()){
                        SocketChannel client;
                        client = ((ServerSocketChannel) key.channel()).accept();
                        client.configureBlocking(false);

                        ByteBuffer[] message = new ByteBuffer[3];
                        message[0] = ByteBuffer.allocate(Integer.BYTES);
                        message[1] = ByteBuffer.allocate(Integer.BYTES);
                        message[2] = ByteBuffer.allocate(Constant.MAX_GAME_SIZE);

                        client.register(selector,SelectionKey.OP_READ,message);
                        System.out.println("Nuova richiesta TCP in arrivo.");
                    }
                    if(key.isReadable()){
                        key.interestOps(0);
                        pool.submit(new ClientTcpReader(key,selector,clientManager,gameManager));
                    }
                    if(key.isWritable()){
                        key.interestOps(0);
                        pool.submit(new ClientTcpWriter(key,selector,clientManager));
                    }
                }
            }

            saveSetting();
            System.out.println("Chiusura Server :(");
            System.exit(0);

        } catch (IOException e) {
            saveSetting();
            System.out.println("Chiusura Server :(");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void loadSetting(){
        JSONObject jsonObject;
        try(FileReader r = new FileReader("./src/Server/Setting.json");){
            System.out.println("Caricamento dati...");

            JSONParser parser = new JSONParser();
            jsonObject = (JSONObject) parser.parse(r);

            this.clientManager.setNewId(((Long) jsonObject.get("nextID")).intValue());

            JSONArray array = (JSONArray) jsonObject.get("users");
            for(Object o: array){
                JSONObject jo = (JSONObject) o;

                int ID = ((Long) jo.get("ID")).intValue();
                String name = (String) jo.get("name");
                String password = (String) jo.get("password");
                int score = ((Long) jo.get("score")).intValue();

                Client client = new Client(name,password,ID);
                client.setScore(score);

                this.clientManager.addUser(client);
                System.out.println("Aggiunto: " + client);
            }
            System.out.println("Caricamento terminato!");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void saveSetting(){
        JSONObject jsonObject = new JSONObject();
        try(FileWriter w = new FileWriter("./src/Server/Setting.json");) {
            System.out.println("Salvataggio dati...");

            jsonObject.put("nextID", clientManager.getId());

            JSONArray array = new JSONArray();
            for(Client c : clientManager.getAllClients()){
                JSONObject element = new JSONObject();
                element.put("ID",c.getID());
                element.put("name",c.getName());
                element.put("password",c.getPassword());
                element.put("score",c.getScore());

                array.add(element);
            }
            jsonObject.put("users",array);

            jsonObject.writeJSONString(w);
            System.out.println("Salvataggio Terminato!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
