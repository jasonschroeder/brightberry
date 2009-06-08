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

public class PrivacyThread extends Thread {
	String url = "http://brightkite.com/me/config.json";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	DataOutputStream httpOutput = null;
	String serverResponse = "";
	BrightBerryMain screen;
	String postmode;
	Settings settings = Settings.getInstance();
	
	public PrivacyThread(int privacyint, BrightBerryMain BrightBerryMain) {
		if (privacyint == 3) {
			this.postmode = "person[privacy_mode]=private";
		} else if (privacyint == 2) {
			this.postmode = "person[privacy_mode]=public";
		} else {
			this.postmode = "";
		}
		this.screen = BrightBerryMain;
	}

	public void run() {
		try {
			if (this.postmode.length() > 21) {
				this.url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());
				this.httpConnection = ((HttpConnection)Connector.open(this.url));
				this.httpConnection.setRequestMethod("PUT");
				this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
				this.httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				this.httpConnection.setRequestProperty("Authorization", this.settings.getAuthHeader());
				this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
				this.httpOutput = this.httpConnection.openDataOutputStream();
				this.httpOutput.write(this.postmode.getBytes());
				this.httpInput = this.httpConnection.openInputStream();
				int rc = httpConnection.getResponseCode();
				if (rc == 200) {
					this.screen.callPrivacy(true);
				} else {
					this.screen.callPrivacy(false);
				}
			} else {
				this.screen.callPrivacy(false);
			}
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
}