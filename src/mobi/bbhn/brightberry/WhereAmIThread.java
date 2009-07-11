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

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.json.me.JSONObject;

public class WhereAmIThread extends Thread {
	String url = "http://brightkite.com/me.json";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	Settings settings = Settings.getInstance();
	private BrightBerryMain screen;
	
	// Main Screen Constructor
	public WhereAmIThread(BrightBerryMain screen) {
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
				parseJSON(buffer.toString());
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

	private void parseJSON(String json) {
		JSONObject me = null;
		System.out.println("/me.json Response: " + json);
		try {
			me = new JSONObject(json);
			JSONObject place = me.getJSONObject("place");
			BrightBerry.setCurrentPlace(place.getString("name"));
			BrightBerry.setCurrentPlaceID(place.getString("id"));
			BrightBerry.setUnreadMessages(me.getInt("unread_messages"));
			BrightBerry.setPendingFriends(me.getInt("pending_friends"));
			this.screen.updateCurrentPlace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
