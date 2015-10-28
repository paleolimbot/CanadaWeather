package ca.fwe.weather;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.TimePeriodForecast;

public class ForecastAdapter extends ArrayAdapter<ForecastItem> {

	private Forecast forecast ;

	public ForecastAdapter(Context context) {
		super(context, R.layout.forecast_item, R.id.forecast_title);
	}

	public void setForecast(Forecast forecast) {
		this.clear() ;
		this.addAll(forecast.items);
		this.forecast = forecast ;
	}

	public Forecast getForecast() {
		return forecast ;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		ForecastItem item = this.getItem(position) ;
		TextView desc = (TextView)v.findViewById(R.id.forecast_subtext) ;
		if(item.getDescription() != null) {
			desc.setText(item.getDescription());
			desc.setVisibility(View.VISIBLE);
		} else {
			desc.setVisibility(View.GONE);
		}

		ImageView icView = (ImageView)v.findViewById(R.id.forecast_icon) ;
		if(item.getIcon() != null) {
			icView.setImageDrawable(item.getIcon());
			icView.setVisibility(View.VISIBLE);
		} else {
			v.findViewById(R.id.forecast_icon).setVisibility(View.GONE);
		}
		
		TextView high = (TextView)v.findViewById(R.id.forecast_high) ;
		TextView low = (TextView)v.findViewById(R.id.forecast_low) ;
		TextView pop = (TextView)v.findViewById(R.id.forecast_pop) ;
		
		if(item instanceof TimePeriodForecast) {
			TimePeriodForecast tpf = (TimePeriodForecast)item ;
			if(tpf.getHigh() != null) {
				high.setText(tpf.getHigh());
				high.setVisibility(View.VISIBLE);
			} else {
				high.setVisibility(View.GONE);
			}

			if(tpf.getLow() != null) {
				low.setText(tpf.getLow());
				low.setVisibility(View.VISIBLE);
			} else {
				low.setVisibility(View.GONE);
			}

			if(tpf.getPop() != null) {
				pop.setText(tpf.getPop());
				pop.setVisibility(View.VISIBLE);
			} else {
				pop.setVisibility(View.GONE);
			}
		} else {
			high.setVisibility(View.GONE);
			low.setVisibility(View.GONE) ;
			pop.setVisibility(View.GONE);
		}
		return v ;
	}



}
