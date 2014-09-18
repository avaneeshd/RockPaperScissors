package apps.avaneesh.com.rockpaperscissors;

/**
 * Created by Lenovo on 9/17/2014.
 */
public class GameEngine
{
    private int bot_random;
    private int user_wins;
    private int user_games;
    private int user_loss;
    final private int ROCK = 0;
    final private int PAPER = 1;
    final private int SCISSORS = 2;

    public int getRandom(){
        this.bot_random = (int)(Math.random()*3);
        return this.bot_random;
    }

    public int getWins(){
        return this.user_wins;
    }

    public int getGames(){
        return this.user_games;
    }

    public void setWins(){
        this.user_wins++;
    }
    public void setGames(){
        this.user_games++;
    }

    public void setLoss()
    {
        this.user_loss++;
    }


    public void calc(int y)
    {
        int x;
        String choice;

        x = this.getRandom();


        if (y==ROCK && x==PAPER )
        {
            setGames();
            setLoss();
        }
        else if (y==ROCK && x==ROCK)
        {
            setGames();
        }
        else if (y==ROCK && x==SCISSORS)
        {
            setGames();
            setWins();
        }
        else if (y==PAPER && x==ROCK)
        {
            setGames();
            setWins();
        }
        else if (y==PAPER && x==PAPER)
        {
            setGames();
        }
        else if (y==PAPER && x==SCISSORS)
        {
            setGames();
            setLoss();
        }
        else if (y==SCISSORS && x==ROCK)
        {
            setGames();
            setLoss();
        }
        else if (y==SCISSORS && x==PAPER)
        {
            setGames();
            setWins();
        }
        else if (y==SCISSORS && x==SCISSORS)
        {
            setGames();
        }
    }

}

