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

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.Base64OutputStream;

public class CheckUsername {
	Settings settings = Settings.getInstance();
	String url = "http://brightkite.com/me/config.json";
	HttpConnection httpConnection = null;
	String serverResponse = "";
	String username;
	String password;
	int ConnectionMode;

	public CheckUsername(String username, String password, int connectionmode) {
		this.username = username;
		this.password = password;
		this.ConnectionMode = connectionmode;
	}

	public boolean run() {
		try {
			this.url += NetworkConfig.getConnectionParameters(ConnectionMode);
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			System.out.println("Connection: " + this.url);
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
			String combo = this.username + ":" + this.password;
			this.httpConnection.setRequestProperty("Authorization", "Basic " + Base64OutputStream.encodeAsString(combo.getBytes(), 0, combo.getBytes().length, false, false));
			int rc = httpConnection.getResponseCode();
			System.out.println("Response code; " + rc);
			if (this.httpConnection != null) {
				this.httpConnection.close();
			}
			if (rc == HttpConnection.HTTP_OK) {
				return true;
			} else {
				return false;
			}
			
	    } catch (IOException ex) {
	    	System.out.println("Caught exception");
	    	ex.printStackTrace();
	    }
	    System.out.println("No more exceptions to catch");
		return false;
	}
}