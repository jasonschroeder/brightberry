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

import net.rim.blackberry.api.browser.URLEncodedPostData;

class SearchPlaceThread extends Thread {
	String url = "";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	SearchPlaceScreen screen;
	Settings settings = Settings.getInstance();

	public SearchPlaceThread(SearchPlaceScreen screen, String query) {
		URLEncodedPostData urlenc = new URLEncodedPostData(URLEncodedPostData.DEFAULT_CHARSET, true);
		urlenc.append("q", query);
		this.url = "http://brightkite.com/places/search.json?" + urlenc.toString();
		this.screen = screen;
	}

	public void run() {
		try {
			this.url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Authorization", this.settings.getAuthHeader());

			this.httpInput = this.httpConnection.openInputStream();

			StringBuffer buffer = new StringBuffer();

			int ch = 0;
			while (ch != -1) {
				ch = this.httpInput.read();
				buffer.append((char)ch);
			}

			this.serverResponse = buffer.toString();

			this.screen.updateSearch(parseJSON(this.serverResponse));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private SearchPlace[] parseJSON(String json) {
		JSONArray jsonArray = null;

		Vector searchplace = new Vector();

		SearchPlace[] rv = null;
		if (json.startsWith("[")) {
			try {
				jsonArray = new JSONArray(json);
				for (int x = 0; x < jsonArray.length(); ++x) {
					JSONObject jsonPlace = jsonArray.getJSONObject(x);
					String name = jsonPlace.getString("name");
					String display_location = jsonPlace.getString("display_location");
					String id = jsonPlace.getString("id");
					searchplace.addElement(new SearchPlace(name, display_location, id));
				}
	      
				rv = new SearchPlace[jsonArray.length()];
				searchplace.copyInto(rv);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else {
			try {
				JSONObject jsonPlace = new JSONObject(json);
				String name = jsonPlace.getString("name");
				String id = jsonPlace.getString("id");
				String display_location = jsonPlace.optString("display_location");
				if (display_location.length() > 0) {
					searchplace.addElement(new SearchPlace(name, display_location, id));
				} else {
					searchplace.addElement(new SearchPlace(name, "", id));
				}
				rv= new SearchPlace[1];
				searchplace.copyInto(rv);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return rv;
	}
}