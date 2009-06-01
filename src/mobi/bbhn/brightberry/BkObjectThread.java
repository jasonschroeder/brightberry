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

public class BkObjectThread extends Thread {
	String url = "http://brightkite.com/objects/";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	private String body = "";
	private String creator;
	private BkObjectScreen screen;
	private String location;
	private String created_at_as_words;
	private boolean about;
	private String type;
	private String photo;
	private int commentscount;

	public BkObjectThread(BkObjectScreen screen, String objectID, String type) {
		this.screen = screen;
		BkObjectThread tmp56_55 = this;
		tmp56_55.url = tmp56_55.url + objectID + ".json";
		this.type = type;
	}

	public void run() {
		try {
			this.url += BrightBerry.appendConnectionString();
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Authorization", Settings.getInstance().getAuthHeader());
			this.httpInput = this.httpConnection.openInputStream();

			StringBuffer buffer = new StringBuffer();

			int ch = 0;
			while (ch != -1) {
				ch = this.httpInput.read();
				buffer.append((char)ch);
			}

			this.serverResponse = buffer.toString();
			parseJSON(this.serverResponse);
			this.screen.updateObject(body, creator, location, created_at_as_words, about, photo, commentscount);
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}

	private void parseJSON(String json) {
		JSONObject objectStream = null;
		try {
			objectStream = new JSONObject(json);
			JSONObject placeStream = objectStream.getJSONObject("place");
			JSONObject creatorStream = objectStream.getJSONObject("creator");
			if (placeStream.optString("display_location").length() > 0 && placeStream.optString("name").length() < 1) {
				this.location = placeStream.getString("display_location");
			} else if (placeStream.optString("display_location").length() > 0) {
				this.location = placeStream.getString("name") + " (" + placeStream.getString("display_location") + ")";
			} else {
				this.location = placeStream.getString("name"); 
			}
			if (this.type.toLowerCase().equals("checkin") == false) {
				this.body = objectStream.optString("body");
				this.about = objectStream.getBoolean("about");
			} else {
				this.about = false;
				this.body = "";
			}
			if (this.type.toLowerCase().equals("photo")) {
				this.photo = objectStream.getString("photo");
			}
			this.creator = creatorStream.getString("login");
			this.created_at_as_words = objectStream.getString("created_at_as_words");
			this.commentscount = objectStream.getInt("comments_count");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}