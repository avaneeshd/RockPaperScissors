package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener{
    private String mConnectedDeviceName = null;
    private static final int handRock     = 0;
    private static final int handPaper    = 1;
    private static final int handScissors = 2;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT      = 2;

    // Message types sent from the BluetoothGameService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    protected static final int REQUEST_OK = 3;

    private TextView txtSpeech;
    protected SpeechRecognizer sr;
    String username;
    String deviceAddress;
    boolean isMultiPlayer = false;
    GameEngine ge;
    Button connectBtn;
    BluetoothConnection mGameService ;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectBtn = (Button)findViewById(R.id.btnConnect);
        final Intent intent = getIntent();
        username = intent.getExtras().get("username").toString();
        if(intent.getExtras().get("isMultiPlayer").equals("true")) {
            System.out.println("isMultiplayer");
            isMultiPlayer = true;
        }
        else{
            isMultiPlayer = false;
        }

        if(isMultiPlayer) {
            findViewById(R.id.btnConnect).setVisibility(View.VISIBLE);
            connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivity(i);
                }
            });
        }


        else{
            findViewById(R.id.btnConnect).setVisibility(View.INVISIBLE);
        }
        TextView hello = (TextView)findViewById(R.id.txtHello);
        hello.setText("Hello "+ username +"!!");

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
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                    if (mGameService == null) setupGame();

                    mGameService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if(bluetoothAdapter.isEnabled()) {
                    Toast.makeText(MainActivity.this,"Status: Enabled",Toast.LENGTH_LONG).show();

                    Intent btActStart = new Intent(MainActivity.this,DeviceListActivity.class);
                    startActivityForResult(btActStart, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(MainActivity.this,"Status: Disabled",Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_OK:
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
                break;

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("onStart");
        if (!bluetoothAdapter.isEnabled()) {
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
        System.out.println("onResumeee");
        if (mGameService != null) {
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
                sendMessage(buttonText);
            }
            else {
                String buttonText = ((Button) view).getText().toString().toUpperCase();
                ((TextView) findViewById(R.id.txtSpeak)).setText(buttonText);
                showResult(buttonText);
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

    private void setupGame() {

        //Start game Engine
        ge = new GameEngine(getApplicationContext(), username);

        findViewById(R.id.btnSpeak).setOnClickListener(this);

        Button rock = (Button)findViewById(R.id.btnRock);
        Button paper = (Button)findViewById(R.id.btnPaper);
        Button scissor = (Button)findViewById(R.id.btnScissors);
        rock.setOnClickListener(CalculateResult);
        paper.setOnClickListener(CalculateResult);
        scissor.setOnClickListener(CalculateResult);
        // Initialize the BluetoothChatService to perform bluetooth connections
        mGameService = new BluetoothConnection(this, mHandler);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
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
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(getApplicationContext(), "You played "
                            + writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), "Opponent played "
                            + readMessage, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
