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

import net.rim.device.api.system.Bitmap;

class StreamThread extends Thread {
	String offset = "&offset=";
	HttpConnection httpConnection = null;
	String type;
	InputStream httpInput = null;
	String serverResponse = "";
	StreamScreen screen;
	int maxEntries;
	String url;
	int start;
	String user;
	String placeid;
	Settings settings = Settings.getInstance();

	// Normal constructor
	public StreamThread(StreamScreen screen, int maxEntries, String type, int start) {
		this.screen = screen;
		this.maxEntries = maxEntries;
		this.type = type;
		this.start = start;
	}
	
	// User constructor
	public StreamThread(StreamScreen screen, int maxEntries, String type, String user, int start) {
		this.screen = screen;
		this.maxEntries = maxEntries;
		this.type = type;
		this.user = user;
		this.start = start;
	}
	
	// Place constructor
	public StreamThread(StreamScreen screen, int maxEntries, String type, int start, String placeid) {
		this.screen = screen;
		this.maxEntries = maxEntries;
		this.type = type;
		this.placeid = placeid;
		this.start = start;
	}

	public void run() {
		try {
			if (this.type.equals("friend")) {
				this.url = "http://brightkite.com/me/friendstream.json?limit=" + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("nearby")) {
				this.url = "http://brightkite.com/me/nearbystream.json?limit=" + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("universe")) {
				this.url = "http://brightkite.com/objects.json?limit=" + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("mentions")) {
				this.url = "http://brightkite.com/me/mentionsstream.json?limit=" + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("person")) {
				this.url = "http://brightkite.com/people/" + this.user + "/objects.json?limit=" + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("place")) {
				this.url = "http://brightkite.com/places/" + this.placeid + "/objects.json?limit=" + this.maxEntries + this.offset + this.start;
			} else {
				this.url = "http://brightkite.com/me/objects.json?limit=" + this.maxEntries + this.offset + this.start;
			}
			this.url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Accept", "*/*");
			this.httpConnection.setRequestProperty("Connection", "Keep-Alive");
			this.httpConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			this.httpConnection.setRequestProperty("Authorization", this.settings.getAuthHeader());
			this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
			this.httpInput = this.httpConnection.openInputStream();
			StringBuffer buffer = new StringBuffer();
			System.out.println("URL: " + this.url);
			System.out.println("Encoding: " + this.httpConnection.getEncoding());

			int ch = 0;
			while (ch != -1) {
				ch = this.httpInput.read();
				buffer.append((char)ch);
			}

			this.serverResponse = buffer.toString();
			System.out.println("Server Response: " + this.serverResponse);
			if (this.serverResponse.startsWith("[]")) {
				this.screen.noPosts();
			} else {
				Stream[] places = parseJSON(this.serverResponse);
				this.screen.updateStream(places);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private Stream[] parseJSON(String json) {
		JSONArray jsonArray = null;
		Vector stream = new Vector();
		Vector imagescached = new Vector();
		Stream[] rv = null;
		
		try {
			jsonArray = new JSONArray(json);

			for (int x = 0; x < jsonArray.length(); ++x) {
				JSONObject jsonStream = jsonArray.getJSONObject(x);
				String type = jsonStream.getString("object_type");
				String id = jsonStream.getString("id");
				String createdwords = jsonStream.getString("created_at_as_words");
				int comments = jsonStream.getInt("comments_count");
				JSONObject jsonCreator = jsonStream.getJSONObject("creator");
				String creator = jsonCreator.getString("login");
				if (ImageCache.inCache(creator) == false && imagescached.contains(creator) == false) {
					String avatar = jsonCreator.getString("small_avatar_url");
					imagescached.addElement(creator);
					AvatarThread getavtr = new AvatarThread(creator, avatar);
					getavtr.start();
				}
				JSONObject jsonPlace = jsonStream.getJSONObject("place");
				String locationname = "";
				if (jsonPlace.optString("display_location").length() > 0 && jsonPlace.optString("name").length() < 1) {
					locationname = jsonPlace.getString("display_location");
				} else if (jsonPlace.optString("display_location").length() > 0) {
					locationname = jsonPlace.getString("name") + " (" + jsonPlace.getString("display_location") + ")";
				} else {
					locationname = jsonPlace.getString("name"); 
				}
				boolean publicpst = jsonStream.optBoolean("public");
				boolean about = jsonStream.optBoolean("about");
				float longitude = (float)jsonPlace.getDouble("longitude");
				float latitude = (float)jsonPlace.getDouble("latitude");
				String placeid = jsonPlace.getString("id");
				if (type.equals("note")) {
					String body = jsonStream.optString("body");
					stream.addElement(new Stream("note", creator, createdwords, locationname, latitude, longitude, id, body, publicpst, comments, about, placeid));
				} else if (type.equals("checkin")) {
					String body = "";
					stream.addElement(new Stream("checkin", creator, createdwords, locationname, latitude, longitude, id, body, publicpst, comments, about, placeid));
				} else if (type.equals("photo")) {
					String body = jsonStream.optString("body");
					Bitmap photo = null;
					if (ImageCache.inCache(id)) {
						photo = ImageCache.getImage(id);
					} else {
						String photourl = jsonStream.getString("photo");
						photo = HTTPPhoto.getFeedPhoto(photourl);
					}
					ImageCache.cacheImage(id, photo);
					stream.addElement(new Stream("photo", creator, createdwords, locationname, latitude, longitude, id, body, publicpst, comments, about, photo, placeid));
				}
			}
			rv = new Stream[jsonArray.length()];
			stream.copyInto(rv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return rv;
	}
}