package ca.fwe.weather.util;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.fwe.weather.WeatherApp;
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

	public ForecastDownloader(Forecast forecast, OnForecastDownloadListener listener, Modes downloadMode, boolean broadcastOnLoad) {
		this.forecast = forecast ;
		this.listener = listener ;
		fmanage = new FilesManager(forecast.getContext()) ;
		this.downloadMode = downloadMode ;
		this.broadcastOnLoad = broadcastOnLoad;
		task = new DlTask(this) ;
	}

	public Forecast getForecast() {
		return forecast ;
	}

	private Modes getDownloadMode() {
		return downloadMode ;
	}

	public void download() {
		task.execute(forecast) ;
	}

	public void cancel() {
	    if(task != null) {
            task.cancel(false);
        }

        task = null;
	}

	private static class DlTask extends AsyncTask<Forecast, Integer, ReturnTypes> {

		private File downloadedFile = null ;
		private ForecastDownloader downloader ;

		DlTask(ForecastDownloader downloader) {
		    this.downloader = downloader;
        }

		@Override
		protected ReturnTypes doInBackground(Forecast... params) {
			Forecast forecast = params[0] ;
			ForecastLocation  l = forecast.getLocation() ;
			ReturnTypes result;
			try {
				File parseFile = downloader.fmanage.cachefile(l, forecast.getLang()) ;
				boolean validCacheFile = downloader.fmanage.cachefileValid(parseFile) ;
				boolean attemptDownload = downloader.getDownloadMode().equals(Modes.FORCE_DOWNLOAD) ||
						(downloader.getDownloadMode().equals(Modes.LOAD_RECENT_CACHE_OR_DOWNLOAD) && !validCacheFile) ;

				if(attemptDownload) {
					downloader.log("expired or nonexistent cache file, or force refresh") ;
					downloadedFile = downloader.fmanage.getTempFile() ;
					String xmlurl = l.getXmlUrl(forecast.getLang())+ "?token=" + RandomString.generate(16);
					downloader.log("downloading from " + xmlurl) ;
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
					downloader.log("download complete") ;
					parseFile = downloadedFile ;
				}

				if(!parseFile.exists() && downloader.getDownloadMode().equals(Modes.LOAD_CACHED)) {
					result = ReturnTypes.NO_CACHED_FORECAST_ERROR ;
				} else {

					if(downloader.listener != null) {
						FilesManager.registerFileLock(parseFile) ;
						ForecastXMLParser parser = l.getXMLParser(forecast, parseFile) ;
						parser.parse() ;
						FilesManager.unregisterFileLock(parseFile) ;
                        downloader.log("done parsing") ;
					} else {
                        downloader.log("no listener registered, skipping parsing") ;
					}

					if(downloadedFile != null) {
                        downloader.fmanage.copyToCache(downloadedFile, forecast.getLocation(), forecast.getLang()) ;
						if(!downloadedFile.delete()) Log.i("ForecastDownloader", "downloaded file not deleted: " + downloadedFile);
						result = ReturnTypes.DOWNLOADED ;
					} else {
						result = ReturnTypes.CACHED ;
					}
				}

                if(downloadedFile != null || downloader.broadcastOnLoad) {
                    //send broadcast to anything that wants to register that a new forecast has been downloaded
                    downloader.log("Sending ACTION_FORECAST_DOWNLOADED intent from downloader");
                    Intent i = new Intent(ACTION_FORECAST_DOWNLOADED) ;
                    i.setData(downloader.forecast.getLocation().getUri()) ;
                    WeatherApp app = (WeatherApp)downloader.forecast.getContext().getApplicationContext();
                    app.broadcastManager(downloader.forecast.getContext()).sendBroadcast(i);
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
			if(downloader.listener != null) {
                downloader.listener.onForecastDownload(downloader.forecast, downloader.getDownloadMode(), result);
			}
		}
	}

	public interface OnForecastDownloadListener {
		void onForecastDownload(Forecast forecast, Modes downloadMode, ReturnTypes returnType) ;
	}

	private void log(String message) {
		Log.v("ForecastDownloader", message) ;
	}

}
