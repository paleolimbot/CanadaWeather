package ca.fwe.weather;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter<Type> extends ArrayAdapter<Type>{

	public CustomSpinnerAdapter(Context context) {
		super(context, android.R.layout.simple_spinner_item);
		this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView v = (TextView)super.getDropDownView(position, convertView, parent) ;
		this.modifyTextView(this.getItem(position), v) ;
		return v ;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView v = (TextView)super.getDropDownView(position, convertView, parent) ;
		this.modifyTextView(this.getItem(position), v) ;
		return v ;
	}

	protected void modifyTextView(Type object, TextView v) {
		
	}
	
}
