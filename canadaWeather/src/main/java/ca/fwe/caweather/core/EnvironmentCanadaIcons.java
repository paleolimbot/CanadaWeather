package ca.fwe.caweather.core;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import ca.fwe.caweather.R;

public class EnvironmentCanadaIcons {

	public enum IconSet {ENV_CANADA_NEW, ENV_CANADA_OLD, AVMAN}

	public enum Icons {
		STORM, WIND_RAIN, SNOW_RAIN,
		ICY_SNOW_RAIN, ICY_LIGHT_RAIN, LIGHT_RAIN,
		ICY_HEAVY_RAIN, RAIN, HEAVY_RAIN, LIGHT_SNOW,
		SNOW, CLEAR_HEAVY_SNOW, HEAVY_SNOW, VERY_HEAVY_SNOW,
		DUST, FOG, HAZE, SMOKE, WINDY, WINDY_SNOW,
		COLD, CLOUDY, NIGHT_CLOUDY, MOSTLY_CLOUDY, NIGHT_MOSTLY_CLOUDY,
		NIGHT_PARTIALLY_CLOUDY, HEAVY_LIGHTNING,
		PARTIALLY_CLOUDY, NIGHT_CLEAR, SUNNY, MAINLY_SUNNY, NIGHT_LIGHT_CLOUDS,
		LIGHT_CLOUDS, HOT, LIGHTNING, WINDY_HEAVY_SNOW, NIGHT_RAIN,
		NIGHT_SNOW, NIGHT_STORM, NA, SUNSHOWER, SUN_STORM,
		SUN_RAIN_SNOW, ICE_CRYSTALS, NIGHT_SNOW_RAIN,
		CLEARING, NIGHT_CLEARING, DISTANT_RAIN, FUNNEL, TORNADO,
		HAIL, INCREASING_CLOUD, NIGHT_INCREASING_CLOUD, FREEZING_RAIN,		
	}
	
    private Context context ;

	private static Map<Icons, Integer> avmanMap ;
	private static SparseArray<Icons> intMap ;
	static {
		avmanMap = new HashMap<>() ;

		avmanMap.put(Icons.STORM, R.drawable.iconsets_avman_tstorms) ;
		avmanMap.put(Icons.WIND_RAIN, R.drawable.iconsets_avman_rain) ;
		avmanMap.put(Icons.SNOW_RAIN, R.drawable.iconsets_avman_sleet) ;
		avmanMap.put(Icons.ICY_SNOW_RAIN, R.drawable.iconsets_avman_sleet) ;
		avmanMap.put(Icons.ICY_LIGHT_RAIN, R.drawable.iconsets_avman_chancesleet) ;
		avmanMap.put(Icons.LIGHT_RAIN, R.drawable.iconsets_avman_chancerain) ;
		avmanMap.put(Icons.ICY_HEAVY_RAIN, R.drawable.iconsets_avman_sleet) ;
		avmanMap.put(Icons.RAIN, R.drawable.iconsets_avman_rain) ;
		avmanMap.put(Icons.HEAVY_RAIN, R.drawable.iconsets_avman_rain) ;
		avmanMap.put(Icons.LIGHT_SNOW, R.drawable.iconsets_avman_chancesnow) ;
		avmanMap.put(Icons.SNOW, R.drawable.iconsets_avman_snow) ;
		avmanMap.put(Icons.CLEAR_HEAVY_SNOW, R.drawable.iconsets_avman_snow) ;
		avmanMap.put(Icons.HEAVY_SNOW, R.drawable.iconsets_avman_snow) ;
		avmanMap.put(Icons.VERY_HEAVY_SNOW, R.drawable.iconsets_avman_snow) ;
		//avmanMap.put(Icons.DUST, R.drawable.iconsets_envcanada_icon45) ; //no AVMAN icon
		avmanMap.put(Icons.FOG, R.drawable.iconsets_avman_fog) ;
		avmanMap.put(Icons.HAZE, R.drawable.iconsets_avman_hazy) ;
		avmanMap.put(Icons.SMOKE, R.drawable.iconsets_avman_smoke) ;
		//avmanMap.put(Icons.WINDY, R.drawable.iconsets_envcanada_icon43) ; //no AVMAN icon
		avmanMap.put(Icons.WINDY_SNOW, R.drawable.iconsets_avman_snow) ;
		//avmanMap.put(Icons.COLD, R.drawable.iconsets_avman_) ; //no AVMAN icon for this
		avmanMap.put(Icons.CLOUDY, R.drawable.iconsets_avman_cloudy) ;
		avmanMap.put(Icons.NIGHT_CLOUDY, R.drawable.iconsets_avman_cloudy) ;
		avmanMap.put(Icons.MOSTLY_CLOUDY, R.drawable.iconsets_avman_mostlycloudy) ;
		avmanMap.put(Icons.NIGHT_MOSTLY_CLOUDY, R.drawable.iconsets_avman_mostlycloudypm) ;
		avmanMap.put(Icons.NIGHT_PARTIALLY_CLOUDY, R.drawable.iconsets_avman_partlycloudypm) ;
		avmanMap.put(Icons.HEAVY_LIGHTNING, R.drawable.iconsets_avman_tstorms) ;
		avmanMap.put(Icons.PARTIALLY_CLOUDY, R.drawable.iconsets_avman_partlycloudy) ;
		avmanMap.put(Icons.NIGHT_CLEAR, R.drawable.iconsets_avman_clearpm) ;
		avmanMap.put(Icons.SUNNY, R.drawable.iconsets_avman_sunny) ;
		avmanMap.put(Icons.MAINLY_SUNNY, R.drawable.iconsets_avman_mostlysunny) ;
		avmanMap.put(Icons.NIGHT_LIGHT_CLOUDS, R.drawable.iconsets_avman_partlycloudypm) ;
		avmanMap.put(Icons.LIGHT_CLOUDS, R.drawable.iconsets_avman_partlycloudy) ;
		avmanMap.put(Icons.HOT, R.drawable.iconsets_avman_sunny) ;
		avmanMap.put(Icons.LIGHTNING, R.drawable.iconsets_avman_tstorms) ;
		avmanMap.put(Icons.WINDY_HEAVY_SNOW, R.drawable.iconsets_avman_snow) ;
		avmanMap.put(Icons.NIGHT_RAIN, R.drawable.iconsets_avman_rain) ;
		avmanMap.put(Icons.NIGHT_SNOW, R.drawable.iconsets_avman_snow) ;
		avmanMap.put(Icons.NIGHT_STORM, R.drawable.iconsets_avman_tstorms) ;
		//avmanMap.put(Icons.NA, R.drawable.iconsets_avman_unknown) ;
		avmanMap.put(Icons.SUNSHOWER, R.drawable.iconsets_avman_chancerain) ;
		avmanMap.put(Icons.SUN_STORM, R.drawable.iconsets_avman_tstorms) ;
		avmanMap.put(Icons.SUN_RAIN_SNOW, R.drawable.iconsets_avman_chancesleet) ;
		avmanMap.put(Icons.ICE_CRYSTALS, R.drawable.iconsets_avman_ice) ;
		avmanMap.put(Icons.NIGHT_SNOW_RAIN, R.drawable.iconsets_avman_chancesleet) ;
		avmanMap.put(Icons.CLEARING, R.drawable.iconsets_avman_mostlysunny) ;
		avmanMap.put(Icons.NIGHT_CLEARING, R.drawable.iconsets_avman_clearpm) ;
		avmanMap.put(Icons.DISTANT_RAIN, R.drawable.iconsets_avman_chancerain) ;
		//avmanMap.put(Icons.FUNNEL, R.drawable.iconsets_avman_unknown) ;
		//avmanMap.put(Icons.TORNADO, R.drawable.iconsets_avman_unknown) ;
		avmanMap.put(Icons.HAIL, R.drawable.iconsets_avman_sleet) ;
		avmanMap.put(Icons.INCREASING_CLOUD, R.drawable.iconsets_avman_partlycloudy) ;
		avmanMap.put(Icons.NIGHT_INCREASING_CLOUD, R.drawable.iconsets_avman_partlycloudypm) ;
		avmanMap.put(Icons.FREEZING_RAIN, R.drawable.iconsets_avman_ice) ;

		intMap = new SparseArray<>();
		intMap.put(0, Icons.SUNNY);
		intMap.put(1, Icons.MAINLY_SUNNY);
		intMap.put(2, Icons.PARTIALLY_CLOUDY);
		intMap.put(3, Icons.MOSTLY_CLOUDY);
		intMap.put(4, Icons.INCREASING_CLOUD);
		intMap.put(5, Icons.CLEARING);
		intMap.put(6, Icons.LIGHT_RAIN);
		intMap.put(7, Icons.SNOW_RAIN);
		intMap.put(8, Icons.LIGHT_SNOW);
		intMap.put(9, Icons.SUN_STORM);
		intMap.put(10, Icons.CLOUDY);
		intMap.put(11, Icons.DISTANT_RAIN);
		intMap.put(12, Icons.LIGHT_RAIN);
		intMap.put(13, Icons.HEAVY_RAIN);
		intMap.put(14, Icons.FREEZING_RAIN);
		intMap.put(15, Icons.SNOW_RAIN);
		intMap.put(16, Icons.LIGHT_SNOW);
		intMap.put(17, Icons.SNOW);
		intMap.put(18, Icons.HEAVY_SNOW);
		intMap.put(19, Icons.STORM);
		intMap.put(20, Icons.CLOUDY);
		intMap.put(21, Icons.CLOUDY);
		intMap.put(22, Icons.CLOUDY);
		intMap.put(23, Icons.HAZE);
		intMap.put(24, Icons.FOG);
		intMap.put(25, Icons.WINDY_SNOW);
		intMap.put(26, Icons.ICE_CRYSTALS);
		intMap.put(27, Icons.HAIL);
		intMap.put(28, Icons.LIGHT_RAIN);
		intMap.put(29, Icons.NA);
		intMap.put(30, Icons.NIGHT_CLEAR);
		intMap.put(31, Icons.NIGHT_PARTIALLY_CLOUDY);
		intMap.put(32, Icons.NIGHT_PARTIALLY_CLOUDY);
		intMap.put(33, Icons.NIGHT_MOSTLY_CLOUDY);
		intMap.put(34, Icons.NIGHT_INCREASING_CLOUD);
		intMap.put(35, Icons.NIGHT_CLEARING);
		intMap.put(36, Icons.NIGHT_RAIN);
		intMap.put(37, Icons.NIGHT_SNOW_RAIN);
		intMap.put(38, Icons.NIGHT_SNOW);
		intMap.put(39, Icons.NIGHT_STORM);
		intMap.put(40, Icons.WINDY_HEAVY_SNOW);
		intMap.put(41, Icons.FUNNEL);
		intMap.put(42, Icons.TORNADO);
		intMap.put(43, Icons.WINDY);
		intMap.put(44, Icons.SMOKE);
		intMap.put(45, Icons.DUST);
	}


	public EnvironmentCanadaIcons(Context context) {
		this.context = context ;
	}

	@SuppressWarnings("deprecation")
	public Drawable getIcon(IconSet set, String iconCode) {
		try {
			int codeInt = Integer.valueOf(iconCode) ;
			return context.getResources().getDrawable(getIconId(set, codeInt)) ;
		} catch(NumberFormatException e) {
			return context.getResources().getDrawable(getIconId(set, 29)) ;
		}
	}

	public static int getIconId(IconSet set, String iconCode) {
		try {
			int codeInt = Integer.valueOf(iconCode) ;
			return getIconId(set, codeInt) ;
		} catch(NumberFormatException e) {
			return getIconId(set, 29) ;
		}
	}
	
	public static int getIconId(IconSet set, int iconCode) {
		switch(set) {
		case AVMAN:
			Icons icon = intMap.get(iconCode) ;
			if(icon != null) {
				if(avmanMap.containsKey(icon)) {
					return avmanMap.get(icon) ;
				} else {
					return R.drawable.icon_na ;
				}
			} else {
				return R.drawable.icon_na ;
			}
		case ENV_CANADA_OLD:
			switch(iconCode) {
			case 0: return R.drawable.iconsets_envcanada_icon00 ;
			case 1: return R.drawable.iconsets_envcanada_icon01 ;
			case 2: return R.drawable.iconsets_envcanada_icon02 ;
			case 3: return R.drawable.iconsets_envcanada_icon03 ;
			case 4: return R.drawable.iconsets_envcanada_icon04 ;
			case 5: return R.drawable.iconsets_envcanada_icon05 ;
			case 6: return R.drawable.iconsets_envcanada_icon06 ;
			case 7: return R.drawable.iconsets_envcanada_icon07 ;
			case 8: return R.drawable.iconsets_envcanada_icon08 ;
			case 9: return R.drawable.iconsets_envcanada_icon09 ;
			case 10: return R.drawable.iconsets_envcanada_icon10 ;
			case 11: return R.drawable.iconsets_envcanada_icon11 ;
			case 12: return R.drawable.iconsets_envcanada_icon12 ;
			case 13: return R.drawable.iconsets_envcanada_icon13 ;
			case 14: return R.drawable.iconsets_envcanada_icon14 ;
			case 15: return R.drawable.iconsets_envcanada_icon15 ;
			case 16: return R.drawable.iconsets_envcanada_icon16 ;
			case 17: return R.drawable.iconsets_envcanada_icon17 ;
			case 18: return R.drawable.iconsets_envcanada_icon18 ;
			case 19: return R.drawable.iconsets_envcanada_icon19 ;
			case 20: return R.drawable.iconsets_envcanada_icon20 ;
			case 21: return R.drawable.iconsets_envcanada_icon21 ;
			case 22: return R.drawable.iconsets_envcanada_icon22 ;
			case 23: return R.drawable.iconsets_envcanada_icon23 ;
			case 24: return R.drawable.iconsets_envcanada_icon24 ;
			case 25: return R.drawable.iconsets_envcanada_icon25 ;
			case 26: return R.drawable.iconsets_envcanada_icon26 ;
			case 27: return R.drawable.iconsets_envcanada_icon27 ;
			case 28: return R.drawable.iconsets_envcanada_icon28 ;
			//case 29: return R.drawable.iconsets_envcanada_icon29 ; the NA Icon
			case 30: return R.drawable.iconsets_envcanada_icon30 ;
			case 31: return R.drawable.iconsets_envcanada_icon31 ;
			case 32: return R.drawable.iconsets_envcanada_icon32 ;
			case 33: return R.drawable.iconsets_envcanada_icon33 ;
			case 34: return R.drawable.iconsets_envcanada_icon34 ;
			case 35: return R.drawable.iconsets_envcanada_icon35 ;
			case 36: return R.drawable.iconsets_envcanada_icon36 ;
			case 37: return R.drawable.iconsets_envcanada_icon37 ;
			case 38: return R.drawable.iconsets_envcanada_icon38 ;
			case 39: return R.drawable.iconsets_envcanada_icon39 ;
			case 40: return R.drawable.iconsets_envcanada_icon40 ;
			case 41: return R.drawable.iconsets_envcanada_icon41 ;
			case 42: return R.drawable.iconsets_envcanada_icon42 ;
			case 43: return R.drawable.iconsets_envcanada_icon43 ;
			case 44: return R.drawable.iconsets_envcanada_icon44 ;
			case 45: return R.drawable.iconsets_envcanada_icon45 ;
			case 46: return R.drawable.iconsets_envcanada_icon46 ;
			case 47: return R.drawable.iconsets_envcanada_icon47 ;
			default: return R.drawable.icon_na ;
			}
			case ENV_CANADA_NEW:
			switch(iconCode) {
			case 0: return R.drawable.iconsets_envcanadanew_icon00 ;
			case 1: return R.drawable.iconsets_envcanadanew_icon01 ;
			case 2: return R.drawable.iconsets_envcanadanew_icon02 ;
			case 3: return R.drawable.iconsets_envcanadanew_icon03 ;
			case 4: return R.drawable.iconsets_envcanadanew_icon04 ;
			case 5: return R.drawable.iconsets_envcanadanew_icon05 ;
			case 6: return R.drawable.iconsets_envcanadanew_icon06 ;
			case 7: return R.drawable.iconsets_envcanadanew_icon07 ;
			case 8: return R.drawable.iconsets_envcanadanew_icon08 ;
			case 9: return R.drawable.iconsets_envcanadanew_icon09 ;
			case 10: return R.drawable.iconsets_envcanadanew_icon10 ;
			case 11: return R.drawable.iconsets_envcanadanew_icon11 ;
			case 12: return R.drawable.iconsets_envcanadanew_icon12 ;
			case 13: return R.drawable.iconsets_envcanadanew_icon13 ;
			case 14: return R.drawable.iconsets_envcanadanew_icon14 ;
			case 15: return R.drawable.iconsets_envcanadanew_icon15 ;
			case 16: return R.drawable.iconsets_envcanadanew_icon16 ;
			case 17: return R.drawable.iconsets_envcanadanew_icon17 ;
			case 18: return R.drawable.iconsets_envcanadanew_icon18 ;
			case 19: return R.drawable.iconsets_envcanadanew_icon19 ;
			case 20: return R.drawable.iconsets_envcanadanew_icon20 ;
			case 21: return R.drawable.iconsets_envcanadanew_icon21 ;
			case 22: return R.drawable.iconsets_envcanadanew_icon22 ;
			case 23: return R.drawable.iconsets_envcanadanew_icon23 ;
			case 24: return R.drawable.iconsets_envcanadanew_icon24 ;
			case 25: return R.drawable.iconsets_envcanadanew_icon25 ;
			case 26: return R.drawable.iconsets_envcanadanew_icon26 ;
			case 27: return R.drawable.iconsets_envcanadanew_icon27 ;
			case 28: return R.drawable.iconsets_envcanadanew_icon28 ;
			//case 29: return R.drawable.iconsets_envcanadanew_icon29 ; the NA Icon
			case 30: return R.drawable.iconsets_envcanadanew_icon30 ;
			case 31: return R.drawable.iconsets_envcanadanew_icon31 ;
			case 32: return R.drawable.iconsets_envcanadanew_icon32 ;
			case 33: return R.drawable.iconsets_envcanadanew_icon33 ;
			case 34: return R.drawable.iconsets_envcanadanew_icon34 ;
			case 35: return R.drawable.iconsets_envcanadanew_icon35 ;
			case 36: return R.drawable.iconsets_envcanadanew_icon36 ;
			case 37: return R.drawable.iconsets_envcanadanew_icon37 ;
			case 38: return R.drawable.iconsets_envcanadanew_icon38 ;
			case 39: return R.drawable.iconsets_envcanadanew_icon39 ;
			case 40: return R.drawable.iconsets_envcanadanew_icon40 ;
			case 41: return R.drawable.iconsets_envcanadanew_icon41 ;
			case 42: return R.drawable.iconsets_envcanadanew_icon42 ;
			case 43: return R.drawable.iconsets_envcanadanew_icon43 ;
			case 44: return R.drawable.iconsets_envcanadanew_icon44 ;
			case 45: return R.drawable.iconsets_envcanadanew_icon45 ;
			default: return R.drawable.icon_na ;
			}
                default: return getIconId(IconSet.ENV_CANADA_NEW, iconCode);
		}
	}
}
