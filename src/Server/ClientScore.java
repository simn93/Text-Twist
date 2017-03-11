package Server;

/**
 * Created by SIMONE on 04/03/2017.
 */
public class ClientScore implements Comparable{
    private String name;
    private int score;

    public ClientScore(String name, int score){
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(Object o) {
        ClientScore other = (ClientScore) o;
        return this.score - other.score;
    }
}
