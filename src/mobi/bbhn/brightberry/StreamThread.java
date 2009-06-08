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
	String friends = "http://brightkite.com/me/friendstream.json?limit=";
	String nearby = "http://brightkite.com/me/nearbystream.json?limit=";
	String universe = "http://brightkite.com/objects.json?limit=";
	String mentions = "http://brightkite.com/me/mentionsstream.json?limit=";
	String me = "http://brightkite.com/me/objects.json?limit=";
	String person = "http://brightkite.com/people/";
	String place = "http://brightkite.com/places/";
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

	public StreamThread(StreamScreen screen, int maxEntries, String type, int start) {
		this.screen = screen;
		this.maxEntries = maxEntries;
		this.type = type;
		this.start = start;
	}
	
	public StreamThread(StreamScreen screen, int maxEntries, String type, String user, int start) {
		this.screen = screen;
		this.maxEntries = maxEntries;
		this.type = type;
		this.user = user;
		this.start = start;
	}

	public void run() {
		try {
			if (this.type.equals("friend")) {
				this.url = this.friends + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("nearby")) {
				this.url = this.nearby + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("universe")) {
				this.url = this.universe + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("mentions")) {
				this.url = this.mentions + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("person")) {
				this.url = this.person + this.user + "/objects.json?limit=" + this.maxEntries + this.offset + this.start;
			} else if (this.type.equals("place")) {
				this.url = this.place + this.user + "/objects.json?limit=" + this.maxEntries + this.offset + this.start;
			} else {
				this.url = this.me + this.maxEntries + this.offset + this.start;
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
			Stream[] places = parseJSON(this.serverResponse);
			this.screen.updateStream(places);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private Stream[] parseJSON(String json) {
		JSONArray jsonArray = null;
		Vector stream = new Vector();
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
				Bitmap avtr;
				if (ImageCache.inCache(creator)) {
					avtr = ImageCache.getImage(creator);
					System.out.println("Cached image used for " + creator);
				} else {
					String avator = jsonCreator.getString("small_avatar_url");
					System.out.println("Getting image " + avator);
					avtr = HTTPPhoto.getAvator(avator);
					if (avtr == null) {
						avtr = Bitmap.getBitmapResource("img/default_avator.gif");
					}
					ImageCache.cacheImage(creator, avtr);
					System.out.println("Caching image for " + creator);
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
					stream.addElement(new Stream("note", creator, avtr, createdwords, locationname, latitude, longitude, id, body, publicpst, comments, about, placeid));
				} else if (type.equals("checkin")) {
					String body = "";
					stream.addElement(new Stream("checkin", creator, avtr, createdwords, locationname, latitude, longitude, id, body, publicpst, comments, about, placeid));
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
					stream.addElement(new Stream("photo", creator, avtr, createdwords, locationname, latitude, longitude, id, body, publicpst, comments, about, photo, placeid));
				}
				System.out.println("Longitude: " + longitude);
				System.out.println("Latitude: " + latitude);
			}
			rv = new Stream[jsonArray.length()];
			stream.copyInto(rv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return rv;
	}
}