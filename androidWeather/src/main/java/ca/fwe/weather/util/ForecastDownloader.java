package ca.fwe.weather.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import ca.fwe.weather.backend.FilesManager;
import ca.fwe.weather.backend.ForecastXMLParser;
import ca.fwe.weather.core.Forecast;
import ca.fwe.weather.core.ForecastLocation;

public class ForecastDownloader {

	public enum Modes {LOAD_CACHED, LOAD_RECENT_CACHE_OR_DOWNLOAD, FORCE_DOWNLOAD}

	public enum ReturnTypes {CACHED, DOWNLOADED, XML_ERROR, IO_ERROR, 
		NO_CACHED_FORECAST_ERROR, UNKNOWN_ERROR}

	public static final String ACTION_FORECAST_DOWNLOADED = "ca.fwe.weather.FORECAST_DOWNLOADED" ;

	private Forecast forecast ;
	private OnForecastDownloadListener listener ;
	private FilesManager fmanage ;
	private DlTask task ;
	private Modes downloadMode ;
	private boolean broadcastOnLoad ;

	public ForecastDownloader(Forecast forecast, OnForecastDownloadListener listener, Modes downloadMode) {
		this.forecast = forecast ;
		this.listener = listener ;
		fmanage = new FilesManager(forecast.getContext()) ;
		this.downloadMode = downloadMode ;
		this.setBroadcastOnLoad(false) ;
		task = new DlTask() ;
	}

	public Forecast getForecast() {
		return forecast ;
	}

	public Modes getDownloadMode() {
		return downloadMode ;
	}

	public void download() {
		task.execute(forecast) ;
	}

	public void cancel() {
		task.cancel(false) ;
	}

	private class DlTask extends AsyncTask<Forecast, Integer, ReturnTypes> {

		private File downloadedFile = null ;

		@Override
		protected ReturnTypes doInBackground(Forecast... params) {
			Forecast forecast = params[0] ;
			ForecastLocation  l = forecast.getLocation() ;
			ReturnTypes result;
			try {
				File parseFile = fmanage.cachefile(l, forecast.getLang()) ;
				boolean validCacheFile = fmanage.cachefileValid(parseFile) ;
				boolean attemptDownload = getDownloadMode().equals(Modes.FORCE_DOWNLOAD) || 
						(getDownloadMode().equals(Modes.LOAD_RECENT_CACHE_OR_DOWNLOAD) && !validCacheFile) ;

				if(attemptDownload) {
					log("expired or nonexistent cache file, or force refresh") ;
					downloadedFile = fmanage.getTempFile() ;
					String xmlurl = l.getXmlUrl(forecast.getLang())+ "?token=" + RandomString.generate(16);
					log("downloading from " + xmlurl) ;
					URL url = new URL(xmlurl);
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setUseCaches(false);
                    connection.connect();
					// download the file
					InputStream input = connection.getInputStream();
                    if(connection.getURL() != url)
                        throw new IOException("Redirected; throwing IOException");
					OutputStream output = new FileOutputStream(downloadedFile);

					byte data[] = new byte[1024];
					int count;
					while (((count = input.read(data)) != -1) && !this.isCancelled()) {
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
                    connection.disconnect();
					log("download complete") ;
					parseFile = downloadedFile ;
				}

				if(!parseFile.exists() && getDownloadMode().equals(Modes.LOAD_CACHED)) {
					result = ReturnTypes.NO_CACHED_FORECAST_ERROR ;
				} else {

					if(listener != null) {
						FilesManager.registerFileLock(parseFile) ;
						ForecastXMLParser parser = l.getXMLParser(forecast, parseFile) ;
						parser.parse() ;
						FilesManager.unregisterFileLock(parseFile) ;
						log("done parsing") ;
					} else {
						log("no listener registered, not parsing") ;
					}

					if(downloadedFile != null) {
						fmanage.copyToCache(downloadedFile, forecast.getLocation(), forecast.getLang()) ;
						if(!downloadedFile.delete()) Log.i("ForecastDownloader", "downloaded file not deleted: " + downloadedFile);
						result = ReturnTypes.DOWNLOADED ;
					} else {
						result = ReturnTypes.CACHED ;
					}
				}

				if(downloadedFile != null || broadcastOnLoad) {
					//send broadcast to anything that wants to register that a new forecast has been downloaded
					Intent i = new Intent(ACTION_FORECAST_DOWNLOADED) ;
					i.setData(forecast.getLocation().getUri()) ;
					forecast.getContext().sendBroadcast(i);
				}
			} catch(IOException e) {
				result = ReturnTypes.IO_ERROR ;
			} catch (XmlPullParserException e) {
				result = ReturnTypes.XML_ERROR ;
			}
			return result ;
		}

		@Override
		protected void onPostExecute(ReturnTypes result) {
			if(listener != null) {
				listener.onForecastDownload(forecast, getDownloadMode(), result);
			}
		}
	}

	public interface OnForecastDownloadListener {
		void onForecastDownload(Forecast forecast, Modes downloadMode, ReturnTypes returnType) ;
	}

	private void log(String message) {
		Log.v("ForecastDownloader", message) ;
	}

	public void setBroadcastOnLoad(boolean broadcastOnLoad) {
		this.broadcastOnLoad = broadcastOnLoad;
	}

}
