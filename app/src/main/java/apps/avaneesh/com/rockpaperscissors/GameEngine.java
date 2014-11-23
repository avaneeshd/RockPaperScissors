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
    private boolean isMultiPlayer;
    private int bot_random;
    private int user_wins = 0;
    private int user_games= 0;
    private int oppo_wins = 0;
    private String uname;
    private String oppo;
    final private int ROCK = 0;
    final private int PAPER = 1;
    final private int SCISSORS = 2;
    private String message =  null;
    SQLiteDatabase database;
    RPSDatabase db;

    GameEngine(Context context, String username, boolean isMP){
        this.uname = username;
        this.isMultiPlayer = isMP;

        db = new RPSDatabase(context);
        database = db.getWritableDatabase();

        if(!isMultiPlayer) {
            this.oppo= "_COMPUTER";
            Cursor c = database.rawQuery("SELECT username, your_wins, oppo_wins, total_games from users WHERE username=? AND opponent=?", new String[]{username, this.oppo});
            if (c.moveToFirst()) {
                if (c.getString(c.getColumnIndex("username")).equals(username)) {
                    System.out.println(c.getString((c.getColumnIndex("your_wins"))));
                    System.out.println(c.getString((c.getColumnIndex("total_games"))));
                    if (c.getString(c.getColumnIndex("your_wins")) != null) {
                        this.user_wins = Integer.parseInt(c.getString(c.getColumnIndex("your_wins")));
                    }
                    if (c.getString(c.getColumnIndex("total_games")) != null) {
                        this.user_games = Integer.parseInt(c.getString(c.getColumnIndex("total_games")));
                    }
                    if (c.getString(c.getColumnIndex("oppo_wins")) != null) {
                        this.oppo_wins = Integer.parseInt(c.getString(c.getColumnIndex("oppo_wins")));
                    }
                }
            }
        }
    }

    public void updateScore() {
        if (isMultiPlayer) {
            Cursor c = database.rawQuery("SELECT username, opponent, your_wins, oppo_wins, total_games from users WHERE username=? AND opponent=?", new String[]{this.uname, this.oppo});
            if (c.getCount() > 0 && c.moveToFirst()) {
                if (c.getString(c.getColumnIndex("username")).equals(this.uname) && c.getString(c.getColumnIndex("opponent")).equals(this.oppo)) {
                    if (c.getString(c.getColumnIndex("your_wins")) != null) {
                        this.user_wins = Integer.parseInt(c.getString(c.getColumnIndex("your_wins")));
                    }
                    if (c.getString(c.getColumnIndex("total_games")) != null) {
                        this.user_games = Integer.parseInt(c.getString(c.getColumnIndex("total_games")));
                    }
                    if (c.getString(c.getColumnIndex("oppo_wins")) != null) {
                        this.oppo_wins = Integer.parseInt(c.getString(c.getColumnIndex("oppo_wins")));
                    }
                }
            }
            else {
                // database.beginTransaction();
                ContentValues values = new ContentValues();
                values.put("username", this.uname);
                values.put("opponent", this.oppo);
                values.put("age", "10");
                values.put("gender", "Male");
                values.put("total_games", 0);
                values.put("your_wins", 0);
                values.put("oppo_wins", 0);
                database.insert("users", null, values);
                // database.endTransaction();
            }
        }
    }

    public int getRandom(){
        return this.bot_random;
    }

    public int getWins(){
        return this.user_wins;
    }

    public int getOppo_wins(){
        return this.oppo_wins;
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

    public void setOppoWin()  { this.oppo_wins++; }

    public void setRandom(){
        this.bot_random = (int)(Math.random()*3);
    }

    public void setOpponent(String opponent){ this.oppo = opponent; }

    public void setMessage(String msg){
        this.message = msg;
    }
    public String getMessage(){
        return this.message;
    }

    public int calc(int y, int z, boolean isMultiPlayer){
        int x;
        String choice;
        this.setMessage("");
        int result = 0;
        if(isMultiPlayer){
            x = z;
        }
        else {
            this.setRandom();
            x = this.getRandom();
        }

        setGames();
        if (y==ROCK && x==PAPER )
        {
            setOppoWin();
            result = -1;
            this.setMessage("Paper Covers Rock");
        }

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
        else if (y==PAPER && x==SCISSORS)
        {
            setOppoWin();
            result = -1;
            this.setMessage("Scissor cuts Paper");
        }
        else if (y==SCISSORS && x==ROCK)
        {
            setOppoWin();
            result = -1;
            this.setMessage("Rock Crushes Scissors");
        }
        else if (y==SCISSORS && x==PAPER)
        {
            setWins();
            result = 1;
            this.setMessage("Scissor cuts Paper");
        }

        this.saveData();
        return result;
    }

    public void saveData(){
        database = db.getWritableDatabase();
        database.beginTransaction();
        try {
            database.execSQL("UPDATE users SET total_games ="+ this.getGames()+ ", your_wins ="+ this.getWins()+", oppo_wins="+ this.getOppo_wins() +" WHERE username ='"+ uname +"' AND opponent ='"+ oppo +"'");
            database.setTransactionSuccessful();
            Log.d("database", uname);
        }
        catch(Exception e){}
        finally{
            database.endTransaction();
        }
    }

}

