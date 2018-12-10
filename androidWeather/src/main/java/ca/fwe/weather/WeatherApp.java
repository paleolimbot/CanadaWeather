package ca.fwe.weather;

import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import ca.fwe.weather.backend.LocationDatabase;

public abstract class WeatherApp extends Application {
	
	private static final String PREF_KEY_LANG = "pref_lang" ;
	private static final String PREF_KEY_VERSION = "installed_version" ;
	protected static final String PREF_KEY_THEME = "xml_theme" ;
	public static final String PREF_KEY_UNITS = "pref_units" ;
	
	private static final int LANG_AUTO = 0 ;
	public static final int LANG_EN = 1 ;
	public static final int LANG_FR = 2 ;
	public static final int LANG_ES = 3 ;
	
	private static int lang = -1 ;
	private static Locale locale = Locale.CANADA;

	private LocalBroadcastManager lbm;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.setLocale();
		lbm = null;
	}
	
	public static SharedPreferences prefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context) ;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig) ;
		this.setLocale() ;
	}
		
	public static Locale getLocale() {
		return locale ;
	}
	
	public static int getLanguage(Context context) {
		if(lang == -1) {
			int langPref = getLangPref(context) ;
			
			if(langPref == LANG_AUTO) {
				Locale deviceDefault = Locale.getDefault() ;
				if(isFrench(deviceDefault)) {
					lang = LANG_FR ;
				} else {
					lang = LANG_EN ;
				}
			} else {
				lang = langPref ;
			}
		}
		
		return lang ;
	}
	
	private static boolean isFrench(Locale locale) {
		return locale.getLanguage().equals("fr");
	}
	
	private static int getLangPref(Context context) {
		String langPref = prefs(context).getString(PREF_KEY_LANG, Integer.valueOf(LANG_AUTO).toString()) ;
		try {
			return Integer.valueOf(langPref) ;
		} catch(NumberFormatException e) {
			return LANG_AUTO ;
		}
	}
	
	public void setLocale() {
		int langPref = getLangPref(this) ;
		
		if(langPref == LANG_AUTO) {
			Locale deviceDefault = Locale.getDefault() ;
			if(isFrench(deviceDefault)) {
				lang = LANG_FR ;
			} else {
				lang = LANG_EN ;
			}
		} else {
			lang = langPref ;
		}

		if(lang == LANG_FR)
			locale = Locale.CANADA_FRENCH ;
		else
			locale = Locale.CANADA ;
		
		Configuration newConfig = getBaseContext().getResources().getConfiguration() ;
		newConfig.locale = locale;
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
	}
	
	public boolean upgrade() {
		int installedVersion = prefs(this).getInt(PREF_KEY_VERSION, 0) ;
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			int thisVersion = pInfo.versionCode ;

			if(installedVersion != thisVersion) {
				this.onUpgrade(installedVersion, thisVersion) ;

				SharedPreferences.Editor e = prefs(this).edit() ;
				e.putInt(PREF_KEY_VERSION, thisVersion) ;
				e.apply() ;
				return true ;
			} else {
				//upgrading has already happened.
				return false ;
			}

		} catch (NameNotFoundException e) {
			return false ;
		}
	}
	
	public static int getThemeId(Context context) {
		SharedPreferences prefs = prefs(context) ;
		String theme = prefs.getString(PREF_KEY_THEME, "LIGHT") ;
		switch (theme) {
			case "DARK":
				return R.style.WeatherTheme;
			case "LIGHT":
				return R.style.WeatherTheme_Light;
			default:
				return R.style.WeatherTheme_Light;
		}
	}

	public LocalBroadcastManager broadcastManager(Context context) {
		if(lbm == null) {
			lbm = LocalBroadcastManager.getInstance(context);
			this.registerReceivers(lbm, context);
		}
		return lbm;
	}

	public abstract void onUpgrade(int version1, int version2) ;
	public abstract LocationDatabase getLocationDatabase(Context context) ;
	public abstract void registerReceivers(LocalBroadcastManager lbm, Context context);
}
