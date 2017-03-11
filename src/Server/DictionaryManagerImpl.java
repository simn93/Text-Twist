package Server;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SIMONE on 02/03/2017.
 */
public class DictionaryManagerImpl extends RemoteObject implements DictionaryManager {
    private Set<String> dictionary;
    private ExecutorService pool;

    public DictionaryManagerImpl(String path){
        dictionary = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        //pool = Executors.newCachedThreadPool();
        loadFromFile(path);
    }

    private void loadFromFile(String path){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))){
            String line;
            while((line = reader.readLine()) != null){
                dictionary.add(line);
            }
            System.out.println("Caricamento dizionario completato.");
            System.out.println(dictionary.size() + " parole aggiunte!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid(String world) throws RemoteException {
        return dictionary.contains(world);
    }
}
