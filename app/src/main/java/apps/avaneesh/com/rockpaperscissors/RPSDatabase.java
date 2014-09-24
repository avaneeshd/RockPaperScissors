package apps.avaneesh.com.rockpaperscissors;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RPSDatabase extends SQLiteOpenHelper  {
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_UNAME = "username";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_GENDER = "gender";
    public static final String TOTAL_GAMES = "total_games";
    public static final String WINS = "wins";

    private static final String DATABASE_NAME = "rockpaperscissors.db";
    private static final int DATABASE_VERSION = 1;
    public RPSDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    private static final String DATABASE_CREATE = "create table "
            + TABLE_USERS + "(" + COLUMN_UNAME
            + " text primary key not null, "+ COLUMN_AGE
            + " integer null, " + COLUMN_GENDER
            + " text null, " + TOTAL_GAMES
            + " integer null, " + WINS
            + " integer null );";

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(RPSDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

}
