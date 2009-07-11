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

class FriendsThread extends Thread {
	String url = "http://brightkite.com/me/friends.json?limit=";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	FriendsScreen screen;
	Settings settings = Settings.getInstance();
	String offset = "&offset=";

	public FriendsThread(FriendsScreen screen, int maxFriends, int start) {
		this.screen = screen;
		this.url = this.url + maxFriends + this.offset + start;
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
			int rc = this.httpConnection.getResponseCode();
			if (rc == 503) {
				BrightBerry.displayAlert("Error", "BrightKite is too busy at the moment try again later");
			} else if (rc == 401 || rc == 403) {
				BrightBerry.errorUnauthorized();
			} else {
				StringBuffer buffer = new StringBuffer();
				int ch = 0;
				while (ch != -1) {
					ch = this.httpInput.read();
					buffer.append((char)ch);
				}
				this.screen.updateFriends(parseJSON(buffer.toString()));
			}
			if (this.httpInput != null) {
				this.httpInput.close();
			}
			if (this.httpConnection != null) {
				this.httpConnection.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private Friends[] parseJSON(String json) {
		JSONArray jsonArray = null;
		Vector fl = new Vector();
		Vector imagescached = new Vector();

		Friends[] rv = null;
		try {
			jsonArray = new JSONArray(json);

			for (int x = 0; x < jsonArray.length(); ++x) {
				JSONObject jsonFriends = jsonArray.getJSONObject(x);
				JSONObject jsonPlace = jsonFriends.getJSONObject("place");
				String username = jsonFriends.getString("login");
				String fullname = jsonFriends.getString("fullname");
				String last_active = "";
				String last_checkin = jsonFriends.getString("last_checked_in_as_words");
				String last_placename = "";
				if (jsonPlace.optString("display_location").length() > 0 && jsonPlace.optString("name").length() < 1) {
					last_placename = jsonPlace.getString("display_location");
				} else if (jsonPlace.optString("display_location").length() > 0) {
					last_placename = jsonPlace.getString("name") + " (" + jsonPlace.getString("display_location") + ")";
				} else {
					last_placename = jsonPlace.getString("name"); 
				}
				String last_placeid = jsonPlace.getString("id");
				float last_longitude = (float) jsonPlace.getDouble("longitude");
				float last_latitude = (float) jsonPlace.getDouble("latitude");
				if (ImageCache.inCache(username) == false && imagescached.contains(username) == false) {
					imagescached.addElement(username);
					String avatar = jsonFriends.getString("small_avatar_url");
					AvatarThread getavtr = new AvatarThread(username, avatar);
					getavtr.start();
				}
				fl.addElement(new Friends(username, fullname, last_active, last_checkin, last_placeid, last_placename, last_latitude, last_longitude));
			}
			rv = new Friends[jsonArray.length()];
			fl.copyInto(rv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return rv;
	}
}