package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener{
    protected static final int REQUEST_OK = 1;

    private TextView txtSpeech;
    protected SpeechRecognizer sr;
    GameEngine ge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String username =intent.getExtras().get("username").toString();
        TextView hello = (TextView)findViewById(R.id.txtHello);
        hello.setText("Hello "+ username +"!!");

        //Start game Engine
        ge = new GameEngine(getApplicationContext(), username);

        findViewById(R.id.btnSpeak).setOnClickListener(this);

        Button rock = (Button)findViewById(R.id.btnRock);
        Button paper = (Button)findViewById(R.id.btnPaper);
        Button scissor = (Button)findViewById(R.id.btnScissors);
        rock.setOnClickListener(CalculateResult);
        paper.setOnClickListener(CalculateResult);
        scissor.setOnClickListener(CalculateResult);
    }

    public void onClick(View v) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String[] recordedWords = thingsYouSaid.get(0).split(" ");
            if(recordedWords[0].equals("Caesars") || recordedWords[0].equals("Seether")
                    || recordedWords[0].equals("Jesus")){
                recordedWords[0] = "scissors";
            }
            if(recordedWords[0].equals("rock") || recordedWords[0].equals("paper")
                    || recordedWords[0].equals("scissors")){
                ((TextView) findViewById(R.id.txtSpeak)).setText(recordedWords[0].toUpperCase());
                showResult(recordedWords[0].toUpperCase());
            }
            else {
                ((TextView) findViewById(R.id.txtSpeak)).setText("Try again!");
                AlertDialog.Builder a = new AlertDialog.Builder(MainActivity.this);
                a.setMessage("You have to speak only Rock, Paper or Scissors.");
                a.setTitle("Try again!");
                a.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                a.create().show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener CalculateResult = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            String buttonText = ((Button)view).getText().toString().toUpperCase();
            ((TextView) findViewById(R.id.txtSpeak)).setText(buttonText);
            showResult(buttonText);
        }
    };

    private void showResult(String userChoice){
        String choice[] = {"ROCK", "PAPER", "SCISSORS"};
        String message = "";
        String title = "";
        int result = 0;
        int bot_choice = 0;
        if(userChoice.equals("ROCK")){
            result = ge.calc(0);
        }
        else if(userChoice.equals("PAPER")){
            result = ge.calc(1);
        }
        else if(userChoice.equals("SCISSORS")){
            result = ge.calc(2);
        }
        bot_choice = ge.getRandom();
        ((TextView)findViewById(R.id.txtBot)).setText(choice[bot_choice]);

        message = ge.getMessage();

        if(result == 1){
            title = "You Win!!";
        }
        if(result == -1){
            title = "You Lose";
        }
        if(result == 0){
            title = "Its a draw!";
        }
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setMessage(message);
        b.setTitle(title);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = b.create();
        TextView total_games = (TextView)findViewById(R.id.total_games);
        TextView total_wins = (TextView)findViewById(R.id.wins_score);

        total_games.setText(""+ge.getGames());
        total_wins.setText(""+ge.getWins());
        dialog.show();
    }

}
