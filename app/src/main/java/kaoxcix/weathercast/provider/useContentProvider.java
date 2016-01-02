package kaoxcix.weathercast.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class useContentProvider extends ContentProvider {

	private useSQLiteOpenHelper myDb;
	private SQLiteDatabase db;
	private int M = 0;
	
	static UriMatcher matcher;
	{
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI("weatherCastDB", "location", 1);
		matcher.addURI("weatherCastDB", "Weather", 2);
	}
	
	
	@Override
	public boolean onCreate() {
		myDb = new useSQLiteOpenHelper(getContext(),"weatherCastV2DB.db",null,1);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		db = myDb.getReadableDatabase();
		Cursor C = null;
		M = matcher.match(uri);
		if(M == 1){
			C = db.query("location", projection, selection, selectionArgs, null, null, sortOrder);
		}
		else{
			C = db.query("Weather", projection, selection, selectionArgs, null, null, sortOrder);
		}
		return C;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = myDb.getWritableDatabase();
		M = matcher.match(uri);
		int res = 0;
		if(M == 1){
			res = db.delete("location", selection, selectionArgs);
		}
		else{
			res = db.delete("Weather", selection, selectionArgs);
		}
		return res;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		db = myDb.getWritableDatabase();
		M = matcher.match(uri);
		int res = 0;
		if(M == 1){
			res = db.update("location", values, selection, selectionArgs);
		}
		else{
			res = db.update("Weather", values, selection, selectionArgs);
		}
		return res;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri u = null;
		M = matcher.match(uri);
		long id = 0;
		db = myDb.getWritableDatabase();
		if(M == 1){
			id=db.insert("location", null, values);
		}
		else{
			id=db.insert("Weather", null, values);
		}
		u = ContentUris.withAppendedId(uri, id);
		return u;
	}

	
	

}