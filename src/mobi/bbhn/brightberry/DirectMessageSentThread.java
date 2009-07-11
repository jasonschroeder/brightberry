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

class DirectMessageSentThread extends Thread {
	String url = "http://brightkite.com/me/sent_messages.json?limit=";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	String serverResponse = "";
	DirectMessageSentScreen screen;
	Settings settings = Settings.getInstance();
	String offset = "&offset=";

	public DirectMessageSentThread(DirectMessageSentScreen directMessageSentScreen, int maxMessages, int start) {
		this.screen = directMessageSentScreen;
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
				this.screen.updateMessages(parseJSON(buffer.toString()));
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

	private DirectMessageSent[] parseJSON(String json) {
		JSONArray jsonArray = null;
		Vector msgsent = new Vector();
		Vector imagescached = new Vector();
		DirectMessageSent[] rv = null;
		
		try {
			jsonArray = new JSONArray(json);

			for (int x = 0; x < jsonArray.length(); ++x) {
				JSONObject jsonStream = jsonArray.getJSONObject(x);
				String createdwords = jsonStream.getString("created_at_as_words");
				JSONObject jsonCreator = jsonStream.getJSONObject("recipient");
				String creator = jsonCreator.getString("login");
				String avatar = jsonCreator.getString("small_avatar_url");
				if (ImageCache.inCache(creator) == false && imagescached.contains(creator) == false) {
					imagescached.addElement(creator);
					AvatarThread getavtr = new AvatarThread(creator, avatar);
					getavtr.start();
				}
				String body = jsonStream.getString("body");
				int id = jsonStream.getInt("id");
				msgsent.addElement(new DirectMessageSent(creator, body, createdwords, id));
			}
			rv = new DirectMessageSent[jsonArray.length()];
			msgsent.copyInto(rv);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return rv;
	}
}