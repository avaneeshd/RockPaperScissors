package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class LeaderboardActivity extends Activity {
    SQLiteDatabase database;
    RPSDatabase db;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        Intent i = getIntent();
        username = i.getExtras().get("username").toString();


        System.out.println("uname"+username);
        db = new RPSDatabase(getApplicationContext());
        database = db.getWritableDatabase();
        ListView list = (ListView)findViewById(R.id.listLeader);

        Cursor c = database.rawQuery("SELECT username, opponent, your_wins, oppo_wins, total_games from users WHERE username=?", new String[]{this.username});
        String[] scores = new String[c.getCount()];
        int k =0;
        if(c.getCount() > 0 && c.moveToFirst()) {
            System.out.println(c.getCount());
           do {
               int user_wins=0;
               String oppo_name="";
               int oppo_wins=0;
               if (c.getString(c.getColumnIndex("username")).equals(this.username)) {
                   if (c.getString(c.getColumnIndex("your_wins")) != null) {
                       user_wins = Integer.parseInt(c.getString(c.getColumnIndex("your_wins")));
                   }
                   if (c.getString(c.getColumnIndex("opponent")) != null) {
                       oppo_name = c.getString(c.getColumnIndex("opponent"));
                   }
                   if (c.getString(c.getColumnIndex("oppo_wins")) != null) {
                       oppo_wins = Integer.parseInt(c.getString(c.getColumnIndex("oppo_wins")));
                   }
               }
                 String text = "You :   "+ user_wins +"   V/S   " +oppo_name+" :   "+oppo_wins;
                 scores[k] = text;
                 k++;
           }while(c.moveToNext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, scores);
           list.setAdapter(adapter);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_leaderboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
