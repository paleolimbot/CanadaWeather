package ca.fwe.weather.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.util.SparseArray;
import ca.fwe.weather.WeatherApp;
import ca.fwe.weather.core.ForecastLocation;
import ca.fwe.weather.util.RandomString;

public class FilesManager {

	private static SparseArray<String> langExts = new SparseArray<String>() ;

	static {
		langExts.put(WeatherApp.LANG_EN, "_en.xml") ;
		langExts.put(WeatherApp.LANG_FR, "_fr.xml") ;
		langExts.put(WeatherApp.LANG_ES, "_es.xml") ;
	}

	public static final int DEFAULT_CACHEFILE_EXPIRES = 1020000 ; //17 minutes
	public static final int DEFAULT_CACHEFILE_DELETE = 259200000 ; //three days


	public static final String PREF_CACHEFILE_EXPIRES = "cachefile_expires" ;
	public static final String PREF_CACHEFILE_DELETE = "cachefile_delete" ;

	private static List<File> fileLocks = new ArrayList<File>() ;

	private Context context ;
	private SharedPreferences prefs ;

	public FilesManager(Context context) {
		this.context = context ;
		prefs = WeatherApp.prefs(context) ;

		Editor edit = prefs.edit() ;
		if(!prefs.contains(PREF_CACHEFILE_EXPIRES))
			edit.putInt(PREF_CACHEFILE_EXPIRES, DEFAULT_CACHEFILE_EXPIRES) ;
		if(!prefs.contains(PREF_CACHEFILE_DELETE))
			edit.putInt(PREF_CACHEFILE_DELETE, DEFAULT_CACHEFILE_DELETE) ;
		edit.commit() ;
	}

	public File getCacheDirectory() {
		return context.getCacheDir() ;
	}

	public void deleteOldCacheFiles() {
		File[] cacheFiles = getCacheDirectory().listFiles() ;
		long now = System.currentTimeMillis() ;
		long delAge = this.getForecastDeleteAge() ;
		log("Deleting old cache files...") ;
		int deleted = 0 ;
		if(cacheFiles != null) {
			for(File f: cacheFiles) {
				if(now - f.lastModified() > delAge) {
					if(f.delete())
						deleted++ ;
				}
			}
		}
		log("Deleted " + deleted + " files") ;
	}

	public void deleteAllCacheFiles() {
		File[] cacheFiles = getCacheDirectory().listFiles() ;
		log("Deleting all cache files...") ;
		int deleted = 0 ;
		if(cacheFiles != null) {
			for(File f: cacheFiles) {
				if(f.delete())
					deleted++ ;
			}
		}
		log("Deleted " + deleted + "of" + cacheFiles.length + " files") ;
	}

	public long getForecastValidAge() {
		return prefs.getInt(PREF_CACHEFILE_EXPIRES, DEFAULT_CACHEFILE_EXPIRES) ;
	}

	public long getForecastDeleteAge() {
		return prefs.getInt(PREF_CACHEFILE_DELETE, DEFAULT_CACHEFILE_DELETE) ;
	}

	public File getTempFile() {
		File dir = context.getCacheDir() ;
		String base = "temp_" ;
		File tmpfile = null ;
		do {
			tmpfile = new File(dir, base + RandomString.generate(5)) ;
		} while(tmpfile.exists()) ;
		return tmpfile ;
	}

	public boolean cachefileValid(ForecastLocation location, int lang) {
		return cachefileValid(cachefile(location, lang)) ;
	}

	public File cachefile(ForecastLocation location, int lang) {
		return new File(this.getCacheDirectory(), location.getCacheFileName(lang)) ;
	}

	public boolean cachefileValid(File file) {
		long validage = this.getForecastValidAge() ;
		return System.currentTimeMillis() - file.lastModified() < validage ;
	}

	public static void registerFileLock(File file) {
		log("locking file" + file) ;
		fileLocks.add(file) ;
	}

	public static void unregisterFileLock(File file) {
		log("unlocking file" + file) ;
		fileLocks.remove(file) ;
	}

	public boolean copyToCache(File xmlFile, ForecastLocation location, int lang) {
		File from = xmlFile ;
		File to = cachefile(location, lang) ;
		log("Copying file " + from + " to " + to) ;
		registerFileLock(to) ;
		try {
			InputStream is = new FileInputStream(from) ;
			OutputStream os = new FileOutputStream(to) ;
			byte[] buffer = new byte[1024] ;
			int length ;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
			is.close(); 
			os.close();
			unregisterFileLock(to) ;
			return true ;
		} catch(IOException e) {
			unregisterFileLock(to) ;
			Log.e("FilesManager", "error copying file!", e) ;
			return false ;
		}
	}

	private static void log(String message) {
		Log.v("FilesManager", message) ;
	}

}
