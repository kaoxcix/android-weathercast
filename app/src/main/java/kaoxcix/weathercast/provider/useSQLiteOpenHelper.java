package kaoxcix.weathercast.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class useSQLiteOpenHelper extends SQLiteOpenHelper {

	public useSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String SQL_COMMAND = "CREATE TABLE location ("+
					"id INTEGER PRIMARY KEY AUTOINCREMENT,"+
					"area1 TEXT,"+
					"area2 TEXT,"+
					"country TEXT"+
					");";
		db.execSQL(SQL_COMMAND);
		
		SQL_COMMAND = "CREATE TABLE weather ("+
					  "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
					  "area TEXT,"+
					  "date TEXT,"+
					  "temp TEXT,"+
					  "temp_min TEXT,"+
					  "temp_max TEXT,"+
					  "sunrise TEXT,"+
					  "sunset TEXT,"+
					  "weather TEXT,"+
					  "description TEXT,"+
					  "created TEXT,"+
					  "current TEXT DEFAULT 'false'"+
					  ");";
		db.execSQL(SQL_COMMAND);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE location if exists location");
		db.execSQL("DROP TABLE Weather if exists weather");
		onCreate(db);
	}
}
