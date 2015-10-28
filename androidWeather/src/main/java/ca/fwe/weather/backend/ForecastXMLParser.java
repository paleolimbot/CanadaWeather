package ca.fwe.weather.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import ca.fwe.weather.core.Forecast;

public abstract class ForecastXMLParser {

	private Forecast forecast ;
	private File file ;
	
	public ForecastXMLParser(Forecast forecast, File file) {
		this.forecast = forecast ;
		this.file = file ;
	}
	
	public Forecast getForecast() {
		return forecast ;
	}
	
	public File getFile() {
		return file ;
	}
	
	public void parse() throws IOException, XmlPullParserException {
		log("beginning parse") ;
		XmlPullParser parser = Xml.newPullParser() ;
		InputStream is = new FileInputStream(this.getFile()) ;
		parser.setInput(is, this.getEncoding());
		this.startParsing(parser);
		is.close();
		log("done parsing") ;
	}
	
	protected abstract String getEncoding() ;
	
	protected abstract void startParsing(XmlPullParser parser) throws IOException, XmlPullParserException ;
	
	protected String readTag(XmlPullParser parser) throws IOException, XmlPullParserException {
		String text = readText(parser);
		parser.next() ;
		return text;
	}

	protected String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = null ;
		int code = parser.next() ;
		if (code == XmlPullParser.TEXT || code == XmlPullParser.CDSECT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
	
	private void log(String message) {
		Log.v("ForecastXMLParser", message) ;
	}
	
}
