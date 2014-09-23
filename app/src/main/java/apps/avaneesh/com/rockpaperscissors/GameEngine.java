package apps.avaneesh.com.rockpaperscissors;

/**
 * Created by Lenovo on 9/17/2014.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GameEngine
{
    private int bot_random;
    private int user_wins = 0;
    private int user_games= 0;
    private int user_loss = 0;
    private String uname;
    final private int ROCK = 0;
    final private int PAPER = 1;
    final private int SCISSORS = 2;
    private String message =  null;
    SQLiteDatabase database;
    RPSDatabase db;

    GameEngine(Context context, String username){
        this.uname = username;
        db = new RPSDatabase(context);
        database = db.getWritableDatabase();
        Cursor c = database.rawQuery("SELECT username, wins, total_games from users WHERE username=?", new String[]{username});
        if(c.moveToFirst()){
            if(c.getString(c.getColumnIndex("username")).equals(username)) {
                System.out.println(c.getString((c.getColumnIndex("wins"))));
                System.out.println(c.getString((c.getColumnIndex("total_games"))));
               if(c.getString(c.getColumnIndex("wins"))!= null){
                   this.user_wins = Integer.parseInt(c.getString(c.getColumnIndex("wins")));
               }
               if(c.getString(c.getColumnIndex("total_games"))!=null){
                   this.user_games = Integer.parseInt(c.getString(c.getColumnIndex("total_games")));
               }
            }
         }
    }
    public int getRandom(){
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

    public void setRandom(){
        this.bot_random = (int)(Math.random()*3);
    }

    public void setMessage(String msg){
        this.message = msg;
    }
    public String getMessage(){
        return this.message;
    }

    public int calc(int y){
        int x;
        String choice;

        int result = 0;
        this.setRandom();
        this.setMessage("");
        x = this.getRandom();


        setGames();
        if (y==ROCK && x==PAPER )
        {
            setLoss();
            result = -1;
            this.setMessage("Paper Covers Rock");
        }
//        else if (y==ROCK && x==ROCK)
//        {
//            setGames();
//        }
        else if (y==ROCK && x==SCISSORS)
        {
            setWins();
            result = 1;
            this.setMessage("Rock Crushes Scissors");
        }
        else if (y==PAPER && x==ROCK)
        {
            setWins();
            result = 1;
            this.setMessage("Paper Covers Rock");
        }
//        else if (y==PAPER && x==PAPER)
//        {
//            setGames();
//        }
        else if (y==PAPER && x==SCISSORS)
        {
            setLoss();
            result = -1;
            this.setMessage("Scissor cuts Paper");
        }
        else if (y==SCISSORS && x==ROCK)
        {
            setLoss();
            result = -1;
            this.setMessage("Rock Crushes Scissors");
        }
        else if (y==SCISSORS && x==PAPER)
        {
            setWins();
            result = 1;
            this.setMessage("Scissor cuts Paper");
        }
//        else if (y==SCISSORS && x==SCISSORS)
//        {
//            setGames();
//        }
        this.saveData();
        return result;
    }

    public void saveData(){
        database = db.getWritableDatabase();
        database.beginTransaction();
        try {
            database.execSQL("UPDATE users SET total_games ="+ this.getGames()+ ", wins ="+ this.getWins()+" WHERE username ='"+ uname +"'");
            database.setTransactionSuccessful();
            Log.d("database", uname);
        }
        catch(Exception e){}
        finally{
            database.endTransaction();
        }
    }

}

