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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.json.me.JSONObject;

public class PlCheckInThread extends Thread {
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	DataOutputStream httpOutput = null;
	String url = "http://brightkite.com/places/";
	String host = "brightkite.com";
	String serverResponse = "";
	PlacemarkScreen screen = null;
	String message = "";
	Settings settings = Settings.getInstance();

	public PlCheckInThread(String id, PlacemarkScreen screen) {
		PlCheckInThread tmp56_55 = this; tmp56_55.url = tmp56_55.url + id + "/checkins.json";
		this.screen = screen;
	}

	public void run() {
		try {
			this.url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			this.httpConnection.setRequestMethod("POST");
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Authorization", this.settings.getAuthHeader());
			this.httpOutput = this.httpConnection.openDataOutputStream();
			this.httpOutput.write(this.message.getBytes());
			this.httpInput = this.httpConnection.openInputStream();

			StringBuffer buffer = new StringBuffer();

			int ch = 0;
			while (ch != -1) {
				ch = this.httpInput.read();
				buffer.append((char)ch);
			}

			this.serverResponse = buffer.toString();
			this.screen.updateStatus(parseJSON(this.serverResponse));
		} catch (IOException ex) {
			ex.printStackTrace();
			this.screen.updateStatus(ex.toString());
		}
	}
	
	private String parseJSON(String json) {
		JSONObject me = null;
		try {
			me = new JSONObject(json);
			JSONObject place = me.getJSONObject("place");
			return(place.getString("name"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "";
	}
}