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

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.system.Alert;

public class FriendAcceptThread extends Thread {
	String url = "http://brightkite.com/me/friendship.json";
	HttpConnection httpConnection = null;
	DataOutputStream httpOutput = null;
	String serverResponse = "";
	FriendsScreen screen;
	Settings settings = Settings.getInstance();
	private String poststring;
	
	public FriendAcceptThread(String username, FriendsScreen screen) {
		URLEncodedPostData urlenc = new URLEncodedPostData(URLEncodedPostData.DEFAULT_CHARSET, true);
		urlenc.append("person_id", username);
		this.poststring = urlenc.toString();
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
			this.httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
			this.httpOutput = this.httpConnection.openDataOutputStream();
			this.httpOutput.write(this.poststring.getBytes());
			int rc = httpConnection.getResponseCode();
			if (rc == 201) {
				if (Alert.isVibrateSupported() && settings.getVibrateOnPost()) {
					Alert.startVibrate(2000);
				}
				this.screen.callAccepted(true);
			} else {
				if (rc == 503) {
					BrightBerry.displayAlert("Error", "BrightKite is too busy at the moment try again later");
				} else if (rc == 401 || rc == 403) {
					BrightBerry.errorUnauthorized();
				}
				this.screen.callAccepted(false);
			}
			if (this.httpOutput != null) {
				this.httpOutput.close();
			}
			if (this.httpConnection != null) {
				this.httpConnection.close();
			}
			System.out.println("Post was: " + this.poststring);
			System.out.println("Auth was: "+ Settings.getInstance().getAuthHeader());
			System.out.println("Response code was: " + rc);
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
}
