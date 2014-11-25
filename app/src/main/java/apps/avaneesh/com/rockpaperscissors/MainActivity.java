package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity implements View.OnClickListener{
    private String mConnectedDeviceName = null;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT      = 2;
    protected static final int REQUEST_OK = 3;

    // Message types sent from the BluetoothGameService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String OPPONENT_NAME = "opponent_name";
    public static final String TOAST = "toast";
    public static String username;

    //Protocol Messages
    private static final String MESSAGE_END_GAME= "ENDGAME";
    private static final String MESSAGE_PLAY_AGAIN = "PLAYAGAIN";
    private static final String MESSAGE_USERNAME = "USERNAME:";


    public String opponentMove = null;
    public String opponentName = null;
    public String yourMove = null;

    boolean opponentPlayAgain = false;
    boolean isMultiPlayer = false;

    //Common Global Objects
    GameEngine ge;
    Button connectBtn;
    Button rock;
    Button paper;
    Button scissor;
    TextView total_games;
    TextView total_wins ;
    TextView oppo_wins;
    TextView txtSpeech;
    TextView txtOpp;
    ProgressDialog pDialog;

    //Bluetooth connection
    BluetoothConnection mGameService ;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get View Elements
        connectBtn = (Button)findViewById(R.id.btnConnect);
        txtSpeech = (TextView)findViewById(R.id.txtSpeak);
        txtOpp = (TextView)findViewById(R.id.txtBot);

        rock = (Button)findViewById(R.id.btnRock);
        paper = (Button)findViewById(R.id.btnPaper);
        scissor = (Button)findViewById(R.id.btnScissors);

        total_games = (TextView)findViewById(R.id.total_games);
        total_wins = (TextView)findViewById(R.id.wins_score);
        oppo_wins = (TextView)findViewById(R.id.oppo_wins_score);



        final Intent intent = getIntent();
        //Set Username
        username = intent.getExtras().get("username").toString();
        TextView hello = (TextView)findViewById(R.id.txtHello);
        hello.setText("Hello "+ username +"!!");


        Button btnLeaderboard = (Button)findViewById(R.id.btnLeaderboard);
        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LeaderboardActivity.class);
                i.putExtra("username", username);
                startActivity(i);
            }
        });

        //Check game mode
        if(intent.getExtras().get("isMultiPlayer").equals("true")) {
            isMultiPlayer = true;
        }
        else{
            isMultiPlayer = false;
        }

        if(isMultiPlayer) { //Multi Player setup
            findViewById(R.id.btnConnect).setVisibility(View.VISIBLE);
            connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(i, REQUEST_CONNECT_DEVICE);
                }
            });
            disableButtons();
        }else{  //Single Player setup
            opponentName = "COMPUTER";
            ((TextView)findViewById(R.id.txtOpponent)).setText(opponentName);
            findViewById(R.id.btnConnect).setVisibility(View.INVISIBLE);
        }
        pDialog = new ProgressDialog(this);
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

        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // When DeviceListActivity returns with a device to connect
                    String address = data.getExtras().getString("deviceaddr");
                    // Get the BluetoothDevice object
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                    if (mGameService == null) setupGame();
                    mGameService.stop();
                    mGameService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if(bluetoothAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this,"Status: Enabled",Toast.LENGTH_SHORT).show();

                    Intent btActStart = new Intent(MainActivity.this,DeviceListActivity.class);
                    startActivityForResult(btActStart, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(MainActivity.this,"Status: Disabled",Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_OK: //Speech Recognization
                ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String[] recordedWords = thingsYouSaid.get(0).split(" ");
                if(recordedWords[0].equals("Caesars") || recordedWords[0].equals("Seether")
                        || recordedWords[0].equals("Jesus")){
                    recordedWords[0] = "scissors";
                }
                if(recordedWords[0].equals("rock") || recordedWords[0].equals("paper")
                        || recordedWords[0].equals("scissors")){
                    txtSpeech.setText(recordedWords[0].toUpperCase());
                    if(!isMultiPlayer) {
                        showResult(recordedWords[0].toUpperCase(), null);
                    }
                    else{
                        sendMessage(recordedWords[0].toUpperCase());
                        yourMove = recordedWords[0].toUpperCase();
                        disableButtons();
                    }
                }
                else {
                    txtSpeech.setText("Try again!");
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
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if(isMultiPlayer && mGameService.getState() != 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Game is ongoing, Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            sendMessage(MESSAGE_END_GAME);
                            opponentName = "";
                            mGameService.stop();
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }else{
            MainActivity.super.onBackPressed();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (isMultiPlayer && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if (mGameService == null) setupGame();
        }
    }
    @Override
    public synchronized void onResume(){

        super.onResume();
        if (isMultiPlayer && mGameService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mGameService.getState() == BluetoothConnection.STATE_NONE) {
                // Start the Bluetooth chat services
                mGameService.start();
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

            if(isMultiPlayer){
                String buttonText = ((Button) view).getText().toString().toUpperCase();
                yourMove = buttonText;
                txtSpeech.setText(buttonText);
                sendMessage(buttonText);
                disableButtons();
            }
            else {
                String buttonText = ((Button) view).getText().toString().toUpperCase();
                txtSpeech.setText(buttonText);
                showResult(buttonText, null);
            }
        }
    };

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mGameService.getState() != BluetoothConnection.STATE_CONNECTED) {
            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mGameService.write(send);
        }
    }

    public void sendUsername(){
        if(username != null)
            sendMessage(MESSAGE_USERNAME + username);
    }

    public void disableButtons(){
        rock.setEnabled(false);
        paper.setEnabled(false);
        scissor.setEnabled(false);
        ImageButton btnSpeak = (ImageButton)findViewById(R.id.btnSpeak);
        btnSpeak.setEnabled(false);
    }

    public void enableButtons(){
        rock.setEnabled(true);
        paper.setEnabled(true);
        scissor.setEnabled(true);
        ImageButton btnSpeak = (ImageButton)findViewById(R.id.btnSpeak);
        btnSpeak.setEnabled(true);
    }
    public void updateScoreOnUI(){

        total_games.setText(""+ge.getGames());
        total_wins.setText(""+ge.getWins());
        oppo_wins.setText(""+ge.getOppo_wins());
    }
    public void resetGame(){
        opponentName ="";
        TextView total_games = (TextView)findViewById(R.id.total_games);
        TextView total_wins = (TextView)findViewById(R.id.wins_score);
        TextView oppo_wins = (TextView)findViewById(R.id.oppo_wins_score);
        total_games.setText("0");
        total_wins.setText("0");
        oppo_wins.setText("0");
    }


    private void showResult(String userChoice, String opponentChoice){
        String choice[] = {"ROCK", "PAPER", "SCISSORS"};
        String message = "";
        String title = "";
        int result = 0;
        int bot_choice = 0;

        String OKText ="OK";
        String cancelText = "Cancel";
        if(isMultiPlayer){
            opponentPlayAgain = false;
            OKText = "Play Again";
            cancelText = "End Game";
            int you = Arrays.asList(choice).indexOf(userChoice);
            int opp = Arrays.asList(choice).indexOf(opponentChoice);
            result = ge.calc(you, opp, true);
            txtOpp.setText(opponentChoice);
        }
        else {
            if (userChoice.equals("ROCK")) {
                result = ge.calc(0, -1, false);
            } else if (userChoice.equals("PAPER")) {
                result = ge.calc(1, -1, false);
            } else if (userChoice.equals("SCISSORS")) {
                result = ge.calc(2, -1, false);
            }
            bot_choice = ge.getRandom();
            txtOpp.setText(choice[bot_choice]);
        }
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
        b.setPositiveButton(OKText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(isMultiPlayer){
                    sendMessage(MESSAGE_PLAY_AGAIN);
                    if(!opponentPlayAgain) {
                        pDialog.setMessage("Waiting for " + opponentName);
                        pDialog.show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Start Playing", Toast.LENGTH_SHORT);
                    }
                }
                dialog.dismiss();
            }
        });
        b.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                if(isMultiPlayer){
                    sendMessage(MESSAGE_END_GAME);
                    mGameService.stop();
                    onBackPressed();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = b.create();
        TextView total_games = (TextView)findViewById(R.id.total_games);
        TextView total_wins = (TextView)findViewById(R.id.wins_score);
        TextView oppo_wins = (TextView)findViewById(R.id.oppo_wins_score);
        total_games.setText(""+ge.getGames());
        total_wins.setText(""+ge.getWins());
        oppo_wins.setText(""+ge.getOppo_wins());

        dialog.show();
        //Reset values
        yourMove = null;
        opponentMove = null;
        txtOpp.setText("");
        txtSpeech.setText("");
        //enable RPS buttons
        enableButtons();
    }

    private void setupGame() {
        //Start game Engine
        ge = new GameEngine(getApplicationContext(), username, isMultiPlayer);

        findViewById(R.id.btnSpeak).setOnClickListener(this);

        Button rock = (Button)findViewById(R.id.btnRock);
        Button paper = (Button)findViewById(R.id.btnPaper);
        Button scissor = (Button)findViewById(R.id.btnScissors);
        rock.setOnClickListener(CalculateResult);
        paper.setOnClickListener(CalculateResult);
        scissor.setOnClickListener(CalculateResult);
        if(!isMultiPlayer){
            updateScoreOnUI();
        }
        // Initialize the BluetoothChatService to perform bluetooth connections
        mGameService = new BluetoothConnection(this, mHandler);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        boolean flag = true; // flag for sending username
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnection.STATE_CONNECTED:
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            break;
                        case BluetoothConnection.STATE_LISTEN:
                            break;
                        case BluetoothConnection.STATE_NONE:
                            findViewById(R.id.btnConnect).setVisibility(View.VISIBLE);
                            ((TextView)findViewById(R.id.txtOpponent)).setText("Waiting for Opponent...");
                            resetGame();
                            break;
                    }
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if(!writeMessage.contains(MESSAGE_USERNAME) && !writeMessage.contains(MESSAGE_PLAY_AGAIN) && !writeMessage.contains(MESSAGE_END_GAME)) {
                        Toast.makeText(getApplicationContext(), "You played "+ writeMessage, Toast.LENGTH_SHORT).show();
                        if (opponentMove != null && yourMove != null) {
                            showResult(yourMove, opponentMove);
                        }
                    }
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(readMessage.contains(MESSAGE_USERNAME)){
                        opponentName = readMessage.substring(readMessage.indexOf(':')+1);
                        ge.setOpponent(opponentName);
                        ge.updateScore();
                        updateScoreOnUI();
                        ((TextView)findViewById(R.id.txtOpponent)).setText(opponentName.toUpperCase());
                        if(flag) {
                            sendUsername();
                            flag = false;
                        }
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.now_connected), Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }else if(readMessage.contains(MESSAGE_PLAY_AGAIN)){
                          opponentPlayAgain = true;
                          if(pDialog.isShowing()) {
                              Toast.makeText(getApplicationContext(), getResources().getString(R.string.start_playing), Toast.LENGTH_SHORT).show();
                              pDialog.hide();
                          }
                    }else if(readMessage.contains(MESSAGE_END_GAME)){
                          pDialog.hide();
                          mGameService.stop();
                          onBackPressed();
                          Toast.makeText(getApplicationContext(),getResources().getString(R.string.opponent_cancelled),Toast.LENGTH_SHORT).show();
                    }else{
                        opponentMove = readMessage;
                        if (yourMove == null) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.opponent_played), Toast.LENGTH_SHORT).show();
                        }
                        else if (opponentMove != null && yourMove != null) {
                            showResult(yourMove, opponentMove);
                        }

                    }
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    findViewById(R.id.btnConnect).setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    sendUsername(); //Send Username to opponent
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
