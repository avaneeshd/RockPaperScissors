package apps.avaneesh.com.rockpaperscissors;

import android.app.Activity;
import android.app.AlertDialog;
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
import apps.avaneesh.com.rockpaperscissors.RPSDatabase;

public class MyActivity extends Activity {

    RPSDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        //Clear Username Hint
        EditText txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtUsername.setOnFocusChangeListener(textboxFocusListener);
        //Clear Age Hint
        EditText txtAge = (EditText)findViewById(R.id.txtAge);
        txtAge.setOnFocusChangeListener(textboxFocusListener);

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

    private OnFocusChangeListener textboxFocusListener =  new OnFocusChangeListener() {
        public void onFocusChange(View view, boolean gainFocus) {
            //onFocus
            if (gainFocus) {
                //EditText e = (EditText)findViewById(R.id.txtUsername);
                String text = ((EditText)view).getText().toString();
                if(text.equals("Enter Username"))
                    ((EditText) view).setText("");
                else if(text.equals("Enter Age"))
                    ((EditText) view).setText("");
            }
            //onBlur
            else {
                //set the text
                String text = ((EditText)view).getText().toString();
                if(text.equals("")) {
                    if (view.getId() == R.id.txtUsername)
                        ((EditText) view).setText(R.string.unameHint);
                    else if (view.getId() == R.id.txtAge)
                        ((EditText) view).setText(R.string.ageHint);
                }

            }
        };
    };

    private View.OnClickListener ExitApp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.exit(0);
        }
    };

    private View.OnClickListener loginUser = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SQLiteDatabase database = db.getWritableDatabase();
            database.beginTransaction();
            String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
            String age = ((EditText) findViewById(R.id.txtAge)).getText().toString();
            String gender = "Male";

            final Intent i = new Intent(MyActivity.this, MainActivity.class);
            i.putExtra("username", username);

            try {
                Cursor c = database.rawQuery("SELECT username from users WHERE username=?", new String[]{username});
                if(c.moveToFirst()){
                    if(c.getString(c.getColumnIndex("username")).equals(username)){
                        AlertDialog.Builder b = new AlertDialog.Builder(MyActivity.this);
                        b.setMessage("Do you want to continue with your previous session?");
                        b.setTitle("Username Exists");
                        b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Go To Main Activity
                                startActivity(i);
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

                }
                else {
                    ContentValues values = new ContentValues();
                    values.put("username", username);
                    values.put("age", age);
                    values.put("gender", gender);
                    values.put("total_games", 0);
                    values.put("wins", 0);
                    database.insert("users", null, values);
                    database.setTransactionSuccessful();
                    //Go To Main Activity
                    startActivity(i);
                }
            }
            finally {
                database.endTransaction();
            }

        }
    };
}
