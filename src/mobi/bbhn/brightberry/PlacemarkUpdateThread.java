package mobi.bbhn.brightberry;

/*
Copyright (c) 2009, Chris Hallgren & Hallgren Networks
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

	* Redistributions of source code must retain the above copyright notice,
	  this list of conditions and the following disclaimer.
	* Redistributions in binary form must reproduce the above copyright notice,
	  this list of conditions and the following disclaimer in the documentation
	  and/or other materials provided with the distribution.
	* Neither the name of Chris Hallgren or Hallgren Networks nor the names of
	  its contributors may be used to endorse or promote products derived from
	  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
*/

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;

class PlacemarksUpdateThread extends Thread {
	String url = "http://brightkite.com/me/placemarks.json";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	PlacemarkScreen screen;
	Settings settings = Settings.getInstance();

	public PlacemarksUpdateThread(PlacemarkScreen screen) {
		this.screen = screen;
	}

	public void run() {
		try {
			this.url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Authorization", this.settings.getAuthHeader());
			this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
			this.httpInput = this.httpConnection.openInputStream();

			StringBuffer buffer = new StringBuffer();

			int ch = 0;
			while (ch != -1) {
				ch = this.httpInput.read();
				buffer.append((char)ch);
			}

			this.serverResponse = buffer.toString();

			this.screen.updatePlacemarks(parseJSON(this.serverResponse));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private Placemark[] parseJSON(String json) {
		JSONArray jsonArray = null;

		Vector placemarks = new Vector();

		Placemark[] rv = null;
		Comparator plcomp = new Placemark();
		try {
			jsonArray = new JSONArray(json);
			
			for (int x = 0; x < jsonArray.length(); ++x) {
				JSONObject jsonPlacemark = jsonArray.getJSONObject(x);
				String placeName = jsonPlacemark.getString("placemark");
				JSONObject jsonPlace = jsonPlacemark.getJSONObject("place");
				String placeid = jsonPlace.getString("id");
				int placemarkid = jsonPlacemark.getInt("id");
				float latitude = (float)jsonPlace.getDouble("latitude");
				float longitude = (float)jsonPlace.getDouble("longitude");
				String name = jsonPlace.optString("name");
				String display_location = jsonPlace.optString("display_location");
				String locationname = "";
				if (display_location.length() > 0 && name.length() < 1) {
					locationname = jsonPlace.getString("display_location");
				} else if (display_location.length() > 0) {
					locationname = jsonPlace.getString("name") + " (" + jsonPlace.getString("display_location") + ")";
				} else {
					locationname = jsonPlace.getString("name"); 
				}
				placemarks.addElement(new Placemark(placemarkid, placeid, placeName, locationname, latitude, longitude));
			}
			rv = new Placemark[jsonArray.length()];
			placemarks.copyInto(rv);
			Arrays.sort(rv, plcomp);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return rv;
	}
}