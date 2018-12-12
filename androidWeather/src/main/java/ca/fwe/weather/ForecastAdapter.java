package ca.fwe.weather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastItem;
import ca.fwe.weather.core.MarkerForecastItem;
import ca.fwe.weather.core.NullForecastItem;
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

	@NonNull
    @Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		ForecastItem item = this.getItem(position) ;
		if(item == null) {
			item = new NullForecastItem(forecast);
		}

		TextView desc = v.findViewById(R.id.forecast_subtext) ;
		if(item.getDescription() != null) {
			desc.setText(item.getDescription());
			desc.setVisibility(View.VISIBLE);
		} else {
			desc.setVisibility(View.GONE);
		}

		ImageView icView = v.findViewById(R.id.forecast_icon) ;
		if(item.getIcon() != null) {
			icView.setImageDrawable(item.getIcon());
			icView.setVisibility(View.VISIBLE);
		} else {
			v.findViewById(R.id.forecast_icon).setVisibility(View.GONE);
		}
		
		TextView high = v.findViewById(R.id.forecast_high) ;
		TextView low = v.findViewById(R.id.forecast_low) ;
		TextView pop = v.findViewById(R.id.forecast_pop) ;

        if(item.getHigh() != null) {
            high.setText(item.getHigh());
            high.setVisibility(View.VISIBLE);
        } else {
            high.setVisibility(View.GONE);
        }

        if(item.getLow() != null) {
            low.setText(item.getLow());
            low.setVisibility(View.VISIBLE);
        } else {
            low.setVisibility(View.GONE);
        }

        if(item.getPop() != null) {
            pop.setText(item.getPop());
            pop.setVisibility(View.VISIBLE);
        } else {
            pop.setVisibility(View.GONE);
        }

		return v ;
	}



}
