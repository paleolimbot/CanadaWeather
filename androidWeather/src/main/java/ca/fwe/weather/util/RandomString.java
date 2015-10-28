package ca.fwe.weather.util;

public class RandomString {

	private static final String chars = "abcdefghijklmnopABCDEFGHIJKLMNOP" ;
	
	private static int randint(int low, int high) {
		int delta = high - low ;
		return (int)(low + Math.round(Math.random() * delta)) ;
	}
	
	private static char randomchar() {
		return chars.charAt(randint(0, chars.length()-1)) ;
	}
	
	public static String generate(int length) {
		char[] out = new char[length] ;
		for(int i=0; i<length; i++) {
			out[i] = randomchar() ;
		}
		return new String(out) ;
	}
	
}
