package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import apps.avaneesh.com.rockpaperscissors.RPSDatabase;

public class MyActivity extends Activity {

    RPSDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //On Click cancel
        Button cancelBtn = (Button)findViewById(R.id.btnCancel);
        cancelBtn.setOnClickListener(ExitApp);


        //On Click Login
        Button LoginBtn = (Button)findViewById(R.id.btnLogin);
        LoginBtn.setOnClickListener(loginUser);

        //create DB
        db = new RPSDatabase(getApplicationContext());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
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





    private View.OnClickListener ExitApp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.exit(0);
        }
    };

    private View.OnClickListener loginUser = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
            String age = ((EditText) findViewById(R.id.txtAge)).getText().toString();
            String gender = "";
            int radioButtonId = ((RadioGroup)findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
            System.out.println(radioButtonId);
            if(radioButtonId > 0) {
                gender = ((RadioButton) findViewById(radioButtonId)).getText().toString();
            }
            System.out.println(username + "-" + age + "-" + gender);

            if(username.equals("") || age.equals("") || gender.equals("")){
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MyActivity.this);
                dlgAlert.setMessage("Username , age or gender is invalid!");
                dlgAlert.setTitle("Invalid");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();

                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                            }
                        });
            }
            else {

                SQLiteDatabase database = db.getWritableDatabase();
                database.beginTransaction();
                try {
                    Cursor c = database.rawQuery("SELECT username from users WHERE username=?", new String[]{username});
                    if (c.moveToFirst()) {
                        if (c.getString(c.getColumnIndex("username")).equals(username)) {
                            AlertDialog.Builder b = new AlertDialog.Builder(MyActivity.this);
                            b.setMessage("Do you want to continue with your previous session?");
                            b.setTitle("Username Exists");
                            b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    showGameModeDialog(username);
                                }
                            });
                            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = b.create();
                            dialog.show();
                        }

                    } else {
                        ContentValues values = new ContentValues();
                        values.put("username", username);
                        values.put("age", age);
                        values.put("gender", gender);
                        values.put("total_games", 0);
                        values.put("wins", 0);
                        database.insert("users", null, values);
                        database.setTransactionSuccessful();
                        showGameModeDialog(username);

                    }
                } finally {
                    database.endTransaction();
                }

            }
        }
    };

    public void showGameModeDialog(String username){

        final Intent i = new Intent(MyActivity.this, MainActivity.class);
        i.putExtra("username", username);


        final Intent listIntent = new Intent(MyActivity.this, DeviceListActivity.class);
        i.putExtra("username", username);

        AlertDialog.Builder gameModeDialog = new AlertDialog.Builder(MyActivity.this);
        gameModeDialog.setTitle("Game Mode");
        gameModeDialog.setMessage("Single Player OR Multi Player");

        gameModeDialog.setPositiveButton("Single Player", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(i);
            }
        });

        gameModeDialog.setNegativeButton("Multi Player", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(listIntent);

            }
        } );

        gameModeDialog.show();
    }


}
