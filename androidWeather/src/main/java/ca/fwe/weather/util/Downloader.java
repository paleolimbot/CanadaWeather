package ca.fwe.weather.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {

	private File tempFile ;
	private String url ;
	boolean cancelled ;
	
	public Downloader(String url, File tmpFile) {
		this.url = url ;
		this.tempFile = tmpFile ;
		cancelled = false ;
	}
	
	
	public File download() throws IOException {
		cancelled = false ;
		URL url = new URL(this.url);
		URLConnection connection = url.openConnection();
		connection.connect();

		// download the file
		InputStream input = new BufferedInputStream(url.openStream(), 8192);
		OutputStream output = new FileOutputStream(tempFile);

		byte data[] = new byte[1024];
		int count;
		while (((count = input.read(data)) != -1) && !cancelled) {
			output.write(data, 0, count);
		}

		output.flush();
		output.close();
		input.close();
		if(cancelled)
			tempFile.delete() ;
		return tempFile ;
	}
	
	public void cancel() {
		cancelled = true ;
	}


	public boolean isCancelled() {
		return cancelled ;
	}
}
