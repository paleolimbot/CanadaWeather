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

public class UpdatesManager {

	//TODO evaluate battery usage for polling (use inexact repeating alarm?)

	private static final String DB_NAME = "androidweather_updates" ;
	private static final int DB_VERSION = 1 ;

	private UpdatesDb db ;

	public UpdatesManager(Context context) {
		db = new UpdatesDb(context) ;
	}

	public void addNotification(Uri locationUri) {
		log("adding notification for uri " + locationUri) ;
		db.addNotifications(locationUri.toString());
	}

	public void addWidget(int widgetId, Uri locationUri) {
		log("adding widget for uri " + locationUri + " and widget id " + widgetId) ;
		db.addOrUpdateWidget(widgetId, locationUri.toString()) ;
	}

	public Uri getForecastLocation(int widgetId) {
		String uri = db.getUriByWidgetId(widgetId) ;
		if(uri != null)
			return Uri.parse(uri) ;
		else
			return null ;
	}

	public void removeNotification(Uri locationUri) {
		log("removing notification for uri " + locationUri) ;
		db.removeNotifications(locationUri.toString());
	}

	public void removeWidget(int widgetId) {
		log("removing widget with widgetId " + widgetId) ;
		db.addOrUpdateWidget(widgetId, null) ;
	}

	public int[] getWidgetIds(Uri locationUri) {
		return db.getWidgetIds(locationUri.toString()) ;
	}

	public boolean notificationsEnabled(Uri locationUri) {
		int result = db.notificationsEnabled(locationUri.toString()) ;
		return result > 0 ;
	}

	public List<Uri> getWidgetUpdateUris() {
		List<Uri> list = new ArrayList<Uri>() ;
		db.getWidgetUris(list);
		return list ;
	}
	
	public List<Uri> getNotificationUpdateUris() {
		List<Uri> list = new ArrayList<Uri>() ;
		db.getNotificationUris(list);
		return list ;
	}
	
	public List<Uri> getAllUpdateUris() {
		List<Uri> list = new ArrayList<Uri>() ;
		db.getWidgetUris(list);
		db.getNotificationUris(list);
		return list ;
	}

	private static class UpdatesDb extends SQLiteOpenHelper {
		
		public static final String WIDGET_ID = "widget_id" ;
		public static final String LOC_URI = "loc_uri" ;
		public static final String ITEM_KEY = "item_key" ;
		public static final String ITEM_VALUE = "item_value" ;

		public UpdatesDb(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String widgets = "CREATE TABLE widgets (widget_id INTEGER PRIMARY KEY, "
					+ "item_key TEXT, item_value TEXT)" ;
			db.execSQL(widgets);
			String notifications = "CREATE TABLE notifications (loc_uri TEXT PRIMARY KEY)" ;
			db.execSQL(notifications);
		}

		public int addNotifications(String uri) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			if(this.notificationsEnabled(db, uri) <= 0) {
				ContentValues cv = new ContentValues() ;
				cv.put(LOC_URI, uri);
				long id = db.insert("notifications", null, cv) ;
				db.close();
				if(id != -1) {
					return 0 ;
				} else {
					//error
					return -1 ;
				}
			} else {
				//already added
				return 1 ;
			}

		}

		public int notificationsEnabled(String uri) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			int result = this.notificationsEnabled(db, uri) ;
			db.close();
			return result ;
		}
		
		private int notificationsEnabled(SQLiteDatabase db, String uri) {
			Cursor c = db.query("notifications", null, "loc_uri=?", new String[] {uri}, null, null, null) ;
			int out = -1 ;
			if(c != null) {
				if(c.getCount() > 0) {
					out = 1 ;
				} else {
					out = 0 ;
				}
				c.close() ;
			} else {
				out = 0 ;
			}
			return out ;
		}

		public int removeNotifications(String uri) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int affected = db.delete("notifications", "loc_uri=?", new String[]{uri}) ;
			db.close();
			return affected ;
		}

		public int addOrUpdateWidget(int widgetId, String uri) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int out = -1 ;
			if(uri != null) {
				String existingUri = this.getUriByWidgetId(db, widgetId) ;
				ContentValues cv = new ContentValues() ;
				cv.put(WIDGET_ID, widgetId);
				cv.put(ITEM_KEY, LOC_URI);
				cv.put(ITEM_VALUE, uri) ;
				if(existingUri == null) {
					//insert
					long newId = db.insert("widgets", null, cv) ;
					if(newId != -1) {
						out = 0 ;
					} else {
						out = -1 ;
					}
				} else {
					//update
					out = db.update("widgets", cv, "widget_id=?", new String[] {Integer.valueOf(widgetId).toString()}) ;
				}
			} else {
				//delete
				int affected = db.delete("widgets", "widget_id=?", new String[] {Integer.valueOf(widgetId).toString()}) ;
				if(affected > 0) {
					out = -3 ;
				} else {
					out = -2 ;
				}
			}
			db.close();
			return out ;
		}

		public String getUriByWidgetId(int widgetId) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			String out = this.getUriByWidgetId(db, widgetId) ;
			db.close();
			return out ;
		}

		private String getUriByWidgetId(SQLiteDatabase db, int widgetId) {
			Cursor c = db.query("widgets", new String[] {ITEM_VALUE}, "widget_id=? AND item_key='loc_uri'", new String[] {Integer.valueOf(widgetId).toString()}, null, null, null) ;
			if(c != null) {
				String out = null ;
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = c.getString(0) ;
				}
				c.close();
				return out ;
			} else {
				return null ;
			}
		}

		private void getWidgetUris(List<Uri> list) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("widgets", new String[] {ITEM_VALUE}, "1", new String[] {}, null, null, null) ;
			if(c != null) {
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					Uri val = Uri.parse(c.getString(0)) ;
					if(!list.contains(val))
						list.add(val) ;
				}
				c.close() ;
			} else {
				log("error getting widget URIs from widget table!") ;
			}
			db.close();
		}

		private void getNotificationUris(List<Uri> list) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("notifications", new String[] {LOC_URI}, "1", new String[] {}, null, null, null) ;
			if(c != null) {
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					Uri val = Uri.parse(c.getString(0)) ;
					if(!list.contains(val))
						list.add(val) ;
				}
				c.close();
			} else {
				log("error getting notification URIs") ;
			}
			db.close() ;
		}

		private int[] getWidgetIds(String locUri) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			int[] out = null ;
			Cursor c = db.query("widgets", new String[] {WIDGET_ID}, "item_key='loc_uri' AND item_value=?", new String[] {locUri}, null, null, null) ;
			if(c != null) {
				out = new int[c.getCount()] ;
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					out[i] = c.getInt(0) ;
				}
			} else {
				//error, keep out as null
			}

			db.close() ;
			return out ;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//only one version of database
		}

	}

	private static void log(String message) {
		Log.i("UpdatesManager", message) ;
	}
}
