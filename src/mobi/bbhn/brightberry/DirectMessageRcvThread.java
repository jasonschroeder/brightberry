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

class DirectMessageRcvThread extends Thread {
	String url = "http://brightkite.com/me/received_messages.json?limit=";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	DirectMessageRcvScreen screen;
	Settings settings = Settings.getInstance();
	String offset = "&offset=";

	public DirectMessageRcvThread(DirectMessageRcvScreen directMessageRcvScreen, int maxMessages, int start) {
		this.screen = directMessageRcvScreen;
		this.url = this.url + maxMessages + this.offset + start;
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
			this.screen.updateMessages(parseJSON(this.serverResponse));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private DirectMessageRcv[] parseJSON(String json) {
		JSONArray jsonArray = null;
		Vector msgrcv = new Vector();
		Vector imagescached = new Vector();

		DirectMessageRcv[] rv = null;
		try {
			jsonArray = new JSONArray(json);

			for (int x = 0; x < jsonArray.length(); ++x) {
				JSONObject jsonStream = jsonArray.getJSONObject(x);
				String createdwords = jsonStream.getString("created_at_as_words");
				JSONObject jsonCreator = jsonStream.getJSONObject("sender");
				String creator = jsonCreator.getString("login");
				String avatar = jsonCreator.getString("small_avatar_url");
				if (ImageCache.inCache(creator) == false && imagescached.contains(creator) == false) {
					imagescached.addElement(creator);
					AvatarThread getavtr = new AvatarThread(creator, avatar);
					getavtr.start();
				}
				String body = jsonStream.getString("body");
				int id = jsonStream.getInt("id");
				msgrcv.addElement(new DirectMessageRcv(creator, body, createdwords, id));
			}
			rv = new DirectMessageRcv[jsonArray.length()];
			msgrcv.copyInto(rv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return rv;
	}
}