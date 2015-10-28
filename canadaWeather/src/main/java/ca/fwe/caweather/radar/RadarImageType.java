package ca.fwe.caweather.radar;



public class RadarImageType {

	public static final String PRODUCT_PRECIP = "PRECIPET" ;
	public static final String PRODUCT_24_HR_ACCUM = "24_HR_ACCUM" ;
	public static final String PRODUCT_CAPPI = "CAPPI" ;

	private static final String PRODUCT_24_HR_ACCUM_NOSPACES = "24HRACCUM" ;

	public static final String PRECIP_TYPE_RAIN = "RAIN" ;
	public static final String PRECIP_TYPE_SNOW = "SNOW" ;

	public static final String EXTRA_CAPPI_10 = "1.0" ;
	public static final String EXTRA_CAPPI_15 = "1.5" ;

	public static final String EXTRA_24_HR_ACCUM_MM = "MM" ;

	private String product ;
	private String precipType ;
	private String extra ;

	public RadarImageType(String product, String precipType, String extra) {
		this.product = product;
		this.precipType = precipType;
		this.extra = extra;
	}

	public String getProduct() {
		return product;
	}

	public String getPrecipType() {
		return precipType;
	}

	public String getExtra() {
		return extra;
	}

	public String getFilenameSuffix() {
		String suffix = this.getProduct() ;
		if(this.getPrecipType() != null) {
			suffix += "_" + this.getPrecipType() ;
		}
		if(this.getExtra() != null) {
			suffix += "_" + this.getExtra() ;
		}
		return suffix ;
	}
	
	public String toString() {
		return "Product: " + product + " precipType: " + precipType + " extra: " + extra ;
	}
	
	public boolean equals(Object otherObject) {
		if(otherObject instanceof RadarImageType && otherObject != null) {
			RadarImageType rit = (RadarImageType)otherObject ;
			if(product != null && product.equals(rit.product)) {
				if(precipType != null && extra != null) {
					if(precipType.equals(rit.getPrecipType()) && extra.equals(rit.getExtra())) 
						return true ;
				} else if(precipType != null) {
					if(precipType.equals(rit.getPrecipType()) && rit.getExtra() == null)
						return true ;
				} else if(precipType == null && extra != null) {
					if(rit.getPrecipType() == null &&  extra.equals(rit.getExtra()))
						return true ;
				}
			}
		}
		return false ;
	}


	public static RadarImageType from(String filename) {
		String[] filenameSplit = filename.replace(PRODUCT_24_HR_ACCUM, PRODUCT_24_HR_ACCUM_NOSPACES).replace(".gif", "").split("_") ;
		if(filenameSplit.length >= 4) {
			//Date d = parseDate(filenameSplit[0]) ;
			//RadarLocation l = RadarLocations.get(filenameSplit[1]) ;

			String product = filenameSplit[2] ;
			String precip = filenameSplit[3] ;
			String extra = null ;
			if(filename.contains(PRODUCT_24_HR_ACCUM_NOSPACES)) {
				product = PRODUCT_24_HR_ACCUM ;
				extra = precip ; //filenameSplit[3]
				precip = null ; //not applicable to this product
			} else if(filename.contains(PRODUCT_CAPPI)) {
				if(filenameSplit.length >= 5) {
					extra = filenameSplit[3] ;
					precip = filenameSplit[4] ;
				} else {
					return null ;
				}
			}

			if(product != null) {
				return new RadarImageType(product, precip, extra) ;
			} else {
				return null ;
			}
		} else {
			return null ;
		}		
	}

}
