package ca.fwe.caweather.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import ca.fwe.caweather.core.CityPageLocation;
import ca.fwe.caweather.core.Province;
import ca.fwe.locations.geometry.LatLon;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.backend.LocationDatabase;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.core.ForecastRegion;

public class CityPageLocationDatabase extends LocationDatabase {

	private static final String TAG = "CityPageLocationDatabas" ;
	
	private static final String DB_NAME = "citypage_locations" ;
	private static final int DB_VERSION = 1 ;
	
	private LocDbHelper db ;
	private int lang ;
	
	public CityPageLocationDatabase(Context context) {
		super(context) ;
		db = new LocDbHelper(context) ;
		lang = WeatherApp.getLanguage(context) ;
	}
	
	@Override
	public List<? extends ForecastRegion> getRegions() {
		return db.getProvinces() ;
	}

	@Override
	public List<? extends ForecastLocation> getLocations(String regionCode) {
		return db.getByProvinceCode(regionCode) ;
	}

	@Override
	public List<? extends ForecastLocation> filterLocations(String filterText) {
		return db.getByFilterText(filterText, 20) ;
	}

	@Override
	public List<? extends ForecastLocation> locationsNear(LatLon location) {
		return db.getByLocation(location, 10) ;
	}


	@Override
	public ForecastLocation getLocation(Uri uri) {
		String sitecode = uri.getLastPathSegment() ;
		if(sitecode != null) {
			return db.getBySitecode(sitecode) ;
		} else {
			return null ;
		}
	}

	public CityPageLocation getBySitecode(String sitecode) {
		return db.getBySitecode(sitecode) ;
	}
	
	private class LocDbHelper extends SQLiteOpenHelper {

		Context context ;
		
		public LocDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.context = context ;
		}

		public CityPageLocation getBySitecode(String sitecode) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			CityPageLocation out = null ;
			Cursor c = db.query("locations", null, "sitecode=?", new String[]{sitecode}, null, null, null) ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = locationFromCursor(c) ;
				}
				c.close();
			}
			db.close();
			return out ;
		}
		
		public List<CityPageLocation> getByLocation(LatLon l, int numResults) {
			List<CityPageLocation> out = new ArrayList<>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("locations", null, "1", new String[]{}, null, null, 
					String.format("(%s-`lat`)*(%s-`lat`)+(%s-`lon`)*(%s-`lon`)", l.getLat(), l.getLat(), l.getLon(), l.getLon() ), 
					Integer.toString(numResults)) ;
			if(c!= null) {
				if(c.getCount() > 0) {
					for(int i=0; i<c.getCount(); i++) {
						c.moveToPosition(i) ;
						out.add(locationFromCursor(c)) ;
					}
				}
				c.close();
			}
			db.close();
			
			return out ;
		}
		
		public List<CityPageLocation> getByFilterText(String filter, int numResults) {
			List<CityPageLocation> out = new ArrayList<>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			
			String likeClause = "%" + filter.trim() + "%" ;
			Cursor c = db.query("locations", null, 
					"(name_en LIKE ?) OR (name_fr LIKE ?)", new String[]{likeClause, likeClause}, 
					null, null, getOrderBy(lang), 
					Integer.toString(numResults)) ;
			if(c!= null) {
				if(c.getCount() > 0) {
					for(int i=0; i<c.getCount(); i++) {
						c.moveToPosition(i) ;
						out.add(locationFromCursor(c)) ;
					}
				}
				c.close();
			}
			db.close();
			
			return out ;
		}
		
		public List<CityPageLocation> getByProvinceCode(String code) {
			List<CityPageLocation> out = new ArrayList<>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			
			Cursor c = db.query("locations", null, 
					"province_code=?", new String[]{code}, 
					null, null, getOrderBy(lang), null) ;
			if(c!= null) {
				if(c.getCount() > 0) {
					for(int i=0; i<c.getCount(); i++) {
						c.moveToPosition(i) ;
						out.add(locationFromCursor(c)) ;
					}
				}
				c.close();
			}
			db.close();
			
			return out ;
		}
		
		public List<Province> getProvinces() {
			List<Province> out = new ArrayList<>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			
			Cursor c = db.query("provinces", null, 
					"1", new String[]{}, 
					null, null, getOrderBy(lang), null) ;
			if(c!= null) {
				if(c.getCount() > 0) {
					for(int i=0; i<c.getCount(); i++) {
						c.moveToPosition(i) ;
						out.add(provinceFromCursor(c)) ;
					}
				}
				c.close();
			}
			db.close();
			
			return out ;
		}
		
		@Override
		public void onCreate(final SQLiteDatabase db) {
			//Create tables
			db.execSQL("CREATE TABLE provinces (province_code TEXT PRIMARY KEY, "
					+ "name_en TEXT, "
					+ "name_fr TEXT)");
			db.execSQL("CREATE TABLE locations (sitecode TEXT PRIMARY KEY, "
					+ "province_code TEXT, "
					+ "name_en TEXT, "
					+ "name_fr TEXT, "
					+ "lat NUMERIC, "
					+ "lon NUMERIC, web_id TEXT)");	
			//Populate tables with locations from the sitelist-base-v1.xml asset.
			try {
				InputStream s = context.getAssets().open("sitelist-base-v1.xml") ;
				mergeInputStream(s, db) ;
				s.close() ;
			} catch (IOException e) {
				//TODO need to edit the sitelist-base-v1.xml asset to possibly include new locations
				Log.e(TAG, "unable to open asset sitelist-base-v1.xml while creating database", e) ;
			} catch (SAXException e) {
				Log.e(TAG, "parse error while parsing sitelist-base-v1.xml from assets while creating database", e) ;
			}
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//only one version so far
		}
		
	}
	
	private static void mergeInputStream(InputStream s, final SQLiteDatabase db) throws IOException, SAXException {
		LocationListParser p = new LocationListParser(s) ;
		p.parse(new LocationListParser.OnNewLocationListener() {
			
			@Override
			public boolean provinceFound(Province province) {
				ContentValues values = makeProvinceContentValues(province) ;
				//if no record exists (i.e. db.update returns 0), insert it into the database
				if (db.update("provinces", values, "province_code=?",
						new String[] {province.getRegionCode()}) == 0)
					db.insert("provinces", null, values) ;
				return true ;
			}
			
			@Override
			public boolean locationFound(CityPageLocation location) {
				ContentValues values = makeContentValues(location) ;
				if(db.update("locations", values, "sitecode=?", 
						new String[] {location.getSitecode()}) == 0)
					db.insert("locations", null, values) ;
				return true ;
			}
		}) ;
	}
	
	private static String getOrderBy(int lang) {
		switch(lang) {
		case WeatherApp.LANG_FR:
			return "name_fr" ;
		default:
			return "name_en" ;
		}
	}
	
	private static CityPageLocation locationFromCursor(Cursor c) {
		String sitecode = c.getString(0) ;
		String provinceCode = c.getString(1) ;
		String nameEn = c.getString(2) ;
		String nameFr = c.getString(3) ;
		String webId = c.getString(6) ;
		LatLon latlon = null ;
		try {
			double lat = c.getDouble(4) ;
			double lon = c.getDouble(5) ;
			latlon = new LatLon(lat, lon) ;
		} catch(NumberFormatException e) {
			//do nothing, there is no lat/lon info
		}
		
		return new CityPageLocation(sitecode, provinceCode, nameEn, nameFr, webId, latlon) ;
		
	}
	
	private static Province provinceFromCursor(Cursor c) {
		String provinceCode = c.getString(0) ;
		String nameEn = c.getString(1) ;
		String nameFr = c.getString(2) ;
		return new Province(provinceCode, nameEn, nameFr) ;
	}
	
	private static ContentValues makeProvinceContentValues(Province p) {
		ContentValues values = new ContentValues() ;
		values.put("province_code", p.getRegionCode());
		values.put("name_en", p.getName(WeatherApp.LANG_EN)) ;
		values.put("name_fr", p.getName(WeatherApp.LANG_FR)) ;
		return values ;
	}
	
	private static ContentValues makeContentValues(CityPageLocation l) {
		ContentValues v  = new ContentValues() ;
		if(l.getSitecode() != null && l.getProvinceCode() != null) {
			v.put("sitecode", l.getSitecode()) ;
			v.put("province_code", l.getProvinceCode());
			if(l.getName(WeatherApp.LANG_EN) != null)
				v.put("name_en", l.getName(WeatherApp.LANG_EN));
			if(l.getName(WeatherApp.LANG_FR) != null)
				v.put("name_fr", l.getName(WeatherApp.LANG_FR));
			if(l.getLatLon() != null) {
				v.put("lat", l.getLatLon().getLat());
				v.put("lon", l.getLatLon().getLon());
			}
			if(l.getWebId() != null) {
				v.put("web_id", l.getWebId());
			}
		} else {
			return null ;
		}
		return v ;
	}
	
}
