package com.gos.veleta;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class WeatherAsynctask extends AsyncTask<String, Void, WindInfo> {

	// private static final String HTTP_ICANHAZIP_COM = "http://icanhazip.com/";
	private static final String TAG_WINDSPEED_KMPH = "/data/current_condition/windspeedKmph";
	private static final String TAG_WINDSPEED_MILES = "/data/current_condition/windspeedMiles";

	private static final String TAG_WINDDIR_DEGREE = "/data/current_condition/winddirDegree";

	private static final String TAG_AREA_NAME = "/data/nearest_area/areaName";
	private static final String TAG_COUNTRY = "/data/nearest_area/country";
	private static final String TAG_REGION = "/data/nearest_area/region";
	private static final String TAG_PROVIDER = "/data/provider";
	private static final String TAG_ERROR = "/veleta/error/message";

	Context ctx;

	public WeatherAsynctask(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * The system calls this to perform work in a worker thread and delivers it
	 * the parameters given to AsyncTask.execute()
	 */
	protected WindInfo doInBackground(String... urls) {

		WindInfo windInfo = new WindInfo();
		try {
			checkInetConnection();

			// String ip = getIp();
			String weatherHttpUrl = urls[0]; // + ip;
			String xml = doCallWeatehrApi(weatherHttpUrl);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			Document document = null;
			try {
				db = dbf.newDocumentBuilder();
				document = db.parse(new ByteArrayInputStream(xml.getBytes()));
			} catch (ParserConfigurationException | SAXException | IOException e) {

				throw new ErrorInfo(R.string.ERR_CANT_GET_HTTP_INFO);
			}

			String error = getFirstTag(TAG_ERROR, document, xPath);
			if (error != null && error.length() > 0) {
				throw new ErrorInfo(error);
			}

			String winddirDegree = getFirstTag(TAG_WINDDIR_DEGREE, document,
					xPath);
			String windSpeedKm = getFirstTag(TAG_WINDSPEED_KMPH, document,
					xPath);

			String provider = getFirstTag(TAG_PROVIDER, document, xPath);

			windInfo.setProvider(provider);

			String windSpeedMiles = getFirstTag(TAG_WINDSPEED_MILES, document,
					xPath);

			String areaName = getFirstTag(TAG_AREA_NAME, document, xPath);
			windInfo.setAreaName(areaName);

			String country = getFirstTag(TAG_COUNTRY, document, xPath);
			windInfo.setCountry(country);

			String region = getFirstTag(TAG_REGION, document, xPath);
			windInfo.setRegion(region);

			int degrees = getNumber(winddirDegree);
			if (degrees < 0) {
				throw new ErrorInfo(R.string.ERR_CANT_GET_HTTP_INFO);
			}
			windInfo.setDegrees(degrees);
			int speedKm = getNumber(windSpeedKm);
			windInfo.setSpeedKm(speedKm);
			int speedMiles = getNumber(windSpeedMiles);
			windInfo.setSpeedMiles(speedMiles);

		} catch (ErrorInfo e) {
			windInfo.setError(e);
		}
		return windInfo;

	}

	private void checkInetConnection() throws ErrorInfo {

		ConnectivityManager connectivityManager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();

		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			throw new ErrorInfo(R.string.ERR_NO_INET);
		}

	}

	private int getNumber(String str) {
		if (str == null || str.trim().length() == 0) {
			return -1;
		} else {

			return Integer.parseInt(str);
		}
	}

	private String getFirstTag(String xpathExpr, Document document, XPath xPath) {
		String result;

		try {
			return xPath.evaluate(xpathExpr, document);
		} catch (XPathExpressionException e) {
			result = null;
		}

		return result;
	}

	private String doCallWeatehrApi(String urlStr) throws ErrorInfo {

		RestClient rc = new RestClient(urlStr);
		rc.executeRequest();
		return rc.getResponse();
	}

	// public static String getIp() throws ErrorInfo {
	//
	// RestClient rc = new RestClient(HTTP_ICANHAZIP_COM);
	// rc.executeRequest();
	// return rc.getResponse().replace("\n", "");
	// }

}