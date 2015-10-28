package ca.fwe.weather.backend;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import ca.fwe.weather.core.ForecastLocation;

public class UserLocationsList {
	
	private static final String TAG = "UserLocationsList" ;
	
	public static final String DB_NAME = "androidweather_userlocs" ;
	private static final int DB_VERSION = 1 ;
	
	private List<ForecastLocation> list ;
	private UserLocationDatabase db ;
	private LocationDatabase globalDb ;
	
	public UserLocationsList(LocationDatabase locDb) {
		list = new ArrayList<ForecastLocation>() ;
		globalDb = locDb ;
		db = new UserLocationDatabase(locDb.getContext()) ;
		db.populateList() ;
	}
	
	public ForecastLocation addLocation(Uri locationUri) {
		ForecastLocation l = globalDb.getLocation(locationUri) ;
		if(l != null) {
			long inserted = db.addLocation(locationUri.toString()) ;
			if(inserted > 0) {
				list.add(l) ;
			} else if(inserted == 0) {
				//wasn't inserted, but uri exists in database. do nothing and return l
			} else {
				//error inserting
				return null ;
			}
		}
		return l ;
	}
	
	public void removeLocations(ForecastLocation... forecastLocations) {
		for(ForecastLocation l: forecastLocations) {
			db.removeLocation(l);
		}
		db.populateList();
	}
	
	public List<ForecastLocation> getList() {
		return list ;
	}
	
	public void close() {
		db.close() ;
	}
	
	private class UserLocationDatabase extends SQLiteOpenHelper {

		private int SORT_GAP = 10 ;
		
		public UserLocationDatabase(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		public void populateList() {
			list.clear();
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor result = db.query("userlocs", new String [] {"loc_uri", "alias"}, null, new String[] {}, null, null, "sortorder") ;
			for(int i=0; i<result.getCount(); i++) {
				result.moveToPosition(i) ;
				String uri = result.getString(0) ;
				String alias = result.getString(1) ;
				ForecastLocation loc = globalDb.getLocation(Uri.parse(uri));
				if(loc != null) {
					if(alias != null && alias.trim().length() > 0)
						loc.setAlias(alias);
					list.add(loc) ;
				} else {
					Log.e(TAG, "could not find location in location database with uri " + uri) ;
					//ignore quietly
				}
			}
			
		}
		
		public long addLocation(String uriString) {
			ContentValues values = new ContentValues() ;
			values.put("loc_uri", uriString) ;
			values.put("alias", "");
			SQLiteDatabase db = this.getWritableDatabase() ;
			long inserted = -1 ;
			if(!this.hasUri(db, uriString)) {
				int size = this.size(db) ;
				values.put("sortorder", size * SORT_GAP);
				inserted = db.insert("userlocs", null, values) ;
			} else {
				//uri already exists, don't insert but return true anyway
				inserted = 0 ;
			}
			db.close();
			return inserted ;
			
		}
		
		public boolean removeLocation(ForecastLocation location) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			String uri = location.getUri().toString() ;
			int deleted = db.delete("userlocs", "loc_uri=?", new String[]{uri}) ;
			db.close();
			if(deleted > 0) {
				return true ;
			} else {
				return false ;
			}
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE userlocs (sortorder INTEGER, "
					+ "loc_uri TEXT PRIMARY KEY, "
					+ "alias TEXT)" ;
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// no multiple versions yet
			
		}
		
		private boolean hasUri(SQLiteDatabase db, String uriString) {
			Cursor c = db.query("userlocs", null, "loc_uri=?", new String[] {uriString}, null, null, null) ;
			boolean toReturn = false ;
			if(c != null) {
				if(c.getCount() > 0) {
					toReturn = true ;
				}
			}
			return toReturn ;
		}
		
		private int size(SQLiteDatabase db) {
			Cursor result = db.query("userlocs", null, "1", new String[]{}, null, null, null) ;
			int out = 0 ;
			if(result != null) {
				out = result.getCount() ;
				result.close() ;
			}
			return out ;
	 	}
		
	}
	
}
