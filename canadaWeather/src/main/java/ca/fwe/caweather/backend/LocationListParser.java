package ca.fwe.caweather.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.fwe.caweather.core.CityPageLocation;
import ca.fwe.caweather.core.Province;
import ca.fwe.locations.geometry.LatLon;

public class LocationListParser {
	
	private OnNewLocationListener listener ;
	private File listXmlFile ;
	private InputStream inputStream = null ;
	
	public LocationListParser(File listXmlFile) {
		inputStream = null ;
		this.listXmlFile = listXmlFile ;
	}
	
	public LocationListParser(InputStream stream) {
		this.listXmlFile = null ;
		inputStream = stream ;
	}
	
	public boolean parse(OnNewLocationListener listener) throws SAXException, IOException {
		this.listener =  listener ;
		try {
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser() ;
			if(this.listXmlFile != null && inputStream == null)
				inputStream = new FileInputStream(listXmlFile) ;
			InputSource is = new InputSource(inputStream);
			is.setEncoding("ISO-8859-1"); //always this encoding in environment canada stuff
			sp.parse(is, new SiteListHandler()); 
			if(!(this.listXmlFile != null && inputStream == null))
				inputStream.close() ;
			return true ;
		} catch(ParserConfigurationException e) {
			return false ;
		} catch(StopParsingException e) {
			if(!(this.listXmlFile != null && inputStream == null))
				inputStream.close() ;
			return true ;
		}
		
	}
	
	
	
	public interface OnNewLocationListener {
		public boolean locationFound(CityPageLocation location) ;
		public boolean provinceFound(Province province) ;
	}
	
	private class StopParsingException extends SAXException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	private class SiteListHandler extends DefaultHandler {
		
		String currentString = null;
		
		String code = null ;
		String nameEn = null ;
		String nameFr = null ;
		String provinceCode = null ;
		String lat = null ;
		String lon = null ;
		String webId = null ;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(qName.equals("fw:province") || qName.equals("site")) {
				code = attributes.getValue("code") ;
				nameEn = null ;
				nameFr = null ;
				provinceCode = null ;
				lat = null ;
				lon = null ;
				webId = null ;
			}
			currentString = "" ;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			currentString += new String(ch, start, length) ;
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if(qName.equals("nameEn")) {
				nameEn = currentString ;
			} else if(qName.equals("nameFr")) {
				nameFr = currentString ;
			} else if(qName.equals("provinceCode")) {
				provinceCode = currentString ;
			} else if(qName.equals("fw:lat")) {
				lat = currentString ;
			} else if(qName.equals("fw:lon")) {
				lon = currentString ;
			} else if(qName.equals("fw:webId")) {
				webId = currentString ;
			} else if(qName.equals("fw:province")) {
				if(code != null) {
					if(!listener.provinceFound(new Province(code, nameEn, nameFr)))
						throw new StopParsingException() ;
				}
			} else if(qName.equals("site")) {
				if(code != null) {
					LatLon latLon = null ;
					if(lat != null && lon != null) {
						try {
							latLon = new LatLon(Double.valueOf(lat), Double.valueOf(lon)) ;
						} catch(NumberFormatException e) {
							//do nothing
						}
					}
					if(!listener.locationFound(new CityPageLocation(code, provinceCode, nameEn, nameFr, webId, latLon)))
						throw new StopParsingException() ;
				}
			}
		}
		
		
		
	}
	
}
