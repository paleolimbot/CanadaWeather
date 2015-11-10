package ca.fwe.caweather.radar;

import java.io.File;

import android.content.Context;
import android.util.Log;

public class RadarCacheManager {

	private static final String RADAR_CACHE_SUBDIR = "radar" ;
	private static final long RADAR_VALID_AGE = 10800000 ; //three hours

	private Context context ;

	public RadarCacheManager(Context context) {
		this.context = context ;
		if(!this.getRadarCacheDir().mkdirs())
		    Log.i("RadarCacheManager", "RadarCacheManager: mkdirs returned false");
	}

	private File getRadarCacheDir() {
		return new File(context.getCacheDir(), RADAR_CACHE_SUBDIR) ;
	}

	public File getRadarCacheFile(String filename) {
		return new File(getRadarCacheDir(), filename) ;
	}

	public void clearCache() {
		File dir = this.getRadarCacheDir() ;
		File[] list = dir.listFiles() ;
		int deleted = 0 ;
		if(list != null) {
			for(File f: list) {
				if(f.delete())
					deleted++ ;
			}
		}
		Log.i("RadarCacheManager", "deleted " + deleted + " files of " + dir.length()) ;
	}

	public void cleanCache() {
		File dir = this.getRadarCacheDir() ;
		File[] list = dir.listFiles() ;
		int deleted = 0 ;
		long now = System.currentTimeMillis() ;
		if(list != null) {
			for(File f: list) {
				if((now - f.lastModified()) > RADAR_VALID_AGE) {
					if(f.delete())
						deleted++ ;
				}
			}
		}
		Log.i("RadarCacheManager", "deleted " + deleted + " old radar images") ;
	}

}
