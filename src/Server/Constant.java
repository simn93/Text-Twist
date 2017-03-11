package Server;

import java.net.InetAddress;

/**
 * Created by SIMONE on 03/03/2017.
 */
public abstract interface Constant {
    int REGISTRY_PORT = 2000;
    int SERVER_PORT = 3000;
    int UDP_PORT = 3200;

    int UDP_MAX_LENGTH = 512;
    int MAX_GAME_SIZE = 8*Integer.BYTES;
    int MAX_NAME_SIZE = 15;

    int GAME_LETTERS = 7;
    int MAX_HIGHSCORE_SIZE = 150;

    String dict_path = "./src/Server/dictionary.txt";
    String SERVER_MULTICAST_IP = "239.255.1.1";

    int REGISTER_GAME = 0;
    int ANSWER_GAME = 1;
    int GET_HIGHSCORE = 2;

    String SERVER_HOST = "192.168.1.10";
}
