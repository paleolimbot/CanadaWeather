package ca.fwe.weather.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class UpdatesManager {

	private static final String DB_NAME = "androidweather_updates" ;
	private static final int DB_VERSION = 1 ;

	private UpdatesDb db ;
    private Context context;

	public UpdatesManager(Context context) {
		this.context = context;
        db = new UpdatesDb(context) ;
	}

	public void addNotification(Uri locationUri) {
		log("adding notification for uri " + locationUri) ;
		db.addNotifications(locationUri.toString());
	}

	public void addWidget(int widgetId, Uri locationUri, String jsonOptions) {
		log("adding widget for uri " + locationUri + " and widget id " + widgetId) ;
		db.addOrUpdateWidget(widgetId, locationUri.toString(), jsonOptions) ;
	}

	public WidgetInfo getWidgetInfo(int widgetId) {
		return db.getInfoByWidgetId(widgetId) ;
	}

	public void removeNotification(Uri locationUri) {
		log("removing notification for uri " + locationUri) ;
		db.removeNotifications(locationUri.toString());
		//need to remove all keys that look like .*?_cancelled
        SharedPreferences prefs = context.getSharedPreferences("prefs_NOTIFICATIONS", Context.MODE_PRIVATE);
        Set<String> names = prefs.getAll().keySet();
        List<String> offendingStrings = new ArrayList<>();
        String notificationId = String.valueOf(NotificationsReceiver.getUniqueNotificationId(locationUri));
        for(String s: names) {
            if(s.matches("^" + notificationId + "_.*?$")) {
                offendingStrings.add(s);
            }
        }
        SharedPreferences.Editor editor = prefs.edit();
        for(String s: offendingStrings) {
            Log.i("UpdatesManager", "removeNotification: removing irrelevant notifications key " + s);
            editor.remove(s);
        }
        editor.apply();
	}

	public void removeWidget(int widgetId) {
		log("removing widget with widgetId " + widgetId) ;
		db.addOrUpdateWidget(widgetId, null, null) ;
	}

	public int[] getWidgetIds(Uri locationUri) {
		return db.getWidgetIds(locationUri.toString()) ;
	}

	boolean notificationsEnabled(Uri locationUri) {
		int result = db.notificationsEnabled(locationUri.toString()) ;
		return result > 0 ;
	}
	
	public List<Uri> getNotificationUpdateUris() {
		List<Uri> list = new ArrayList<>() ;
		db.getNotificationUris(list);
		return list ;
	}
	
	List<Uri> getAllUpdateUris() {
		List<Uri> list = new ArrayList<>() ;
		db.getWidgetUris(list);
		db.getNotificationUris(list);
		return list ;
	}

	private static class UpdatesDb extends SQLiteOpenHelper {
		
		static final String WIDGET_ID = "widget_id" ;
		static final String LOC_URI = "loc_uri" ;
		static final String ITEM_OPTIONS = "item_key" ;
		static final String ITEM_VALUE = "item_value" ;

		UpdatesDb(Context context) {
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

		int addNotifications(String uri) {
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

		int notificationsEnabled(String uri) {
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

		int removeNotifications(String uri) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int affected = db.delete("notifications", "loc_uri=?", new String[]{uri}) ;
			db.close();
			return affected ;
		}

		int addOrUpdateWidget(int widgetId, String uri, String jsonOptions) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int out;
			if(uri != null) {
				WidgetInfo wi = this.getInfoByWidgetId(db, widgetId) ;
                if(jsonOptions == null) {
                    jsonOptions = "{}";
                }
				ContentValues cv = new ContentValues() ;
				cv.put(WIDGET_ID, widgetId);
				cv.put(ITEM_VALUE, uri) ;
                cv.put(ITEM_OPTIONS, jsonOptions) ;
				if(wi == null) {
					//insert
					long newId = db.insert("widgets", null, cv) ;
					if(newId != -1) {
						out = 0 ;
					} else {
						out = -1 ;
                        log("insert widget info into widgets failed");
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

		WidgetInfo getInfoByWidgetId(int widgetId) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			WidgetInfo wi = this.getInfoByWidgetId(db, widgetId) ;
			db.close();
			return wi ;
		}

		private WidgetInfo getInfoByWidgetId(SQLiteDatabase db, int widgetId) {
			Cursor c = db.query("widgets", new String[] {ITEM_OPTIONS, ITEM_VALUE}, "widget_id=?", new String[] {Integer.valueOf(widgetId).toString()}, null, null, null) ;
			if(c != null) {
				WidgetInfo out = null;
				if(c.getCount() > 0) {
					c.moveToFirst() ;
                    String uri = c.getString(1);
                    if(uri != null) {
                        out = new WidgetInfo();
                        out.uri = uri;
                        out.jsonOptions = c.getString(0);
                        if(TextUtils.isEmpty(out.jsonOptions)) {
                            out.jsonOptions = "{}";
                        }
                    } else {
                        log("error fetching URI for widget id " + widgetId);
                    }
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
			Cursor c = db.query("widgets", new String[] {WIDGET_ID}, "item_value=?", new String[] {locUri}, null, null, null) ;
			if(c != null) {
				out = new int[c.getCount()] ;
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					out[i] = c.getInt(0) ;
				}
                c.close();
			} else {
				//error, keep out as null
				Log.i("UpdatesDb", "getWidgetIds: NULL cursor");
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

    public static class WidgetInfo {
        public String uri;
        public String jsonOptions;
    }
}
