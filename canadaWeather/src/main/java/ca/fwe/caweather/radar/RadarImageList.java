package ca.fwe.caweather.radar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import android.util.Log;

public class RadarImageList {

	private ArrayList<RadarImage> list ;
	
	public RadarImageList() {
		list = new ArrayList<RadarImage>() ;
	}
	
	public int size() {
		return list.size() ;
	}

	public void add(RadarImage link) {
		list.add(link) ;
	}

	public void sort() {
		Collections.sort(list);
	}
	
	public static RadarImageList getMostRecent(RadarLocation l, RadarImageType type, int animationLength) {
		//FIXME there's a problem with timezones here. (UTC shouldn't matter beause the date is 
		//passed to the radarImage as a Date instance.
		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC")) ;		
		//typical radar image is about 8 minutes behind the last 10-minute interval
		date.set(Calendar.SECOND, 0) ;
		date.set(Calendar.MILLISECOND, 0) ;
		date.add(Calendar.MINUTE, -8);
		
		//set to nearest ten minute interval before date
		int minutes = date.get(Calendar.MINUTE) ;
		int previousTen = minutes / 10 * 10 ;
		date.set(Calendar.MINUTE, previousTen) ;
		
		Log.d("RadarImageList", "creating image list starting with date" + date.getTime()) ;
		RadarImageList out = new RadarImageList() ;
		for(int i=0; i<animationLength; i++) {
			out.add(new RadarImage(l, type, date.getTime())) ;			
			date.add(Calendar.MINUTE, -10) ;
		}
		out.sort();
		return out ;
	}

	public RadarImage get(int i) {
		return list.get(i) ;
	}

}
