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

import net.rim.device.api.util.Arrays;

public class DirectMessageRcvDeleteThread extends Thread {
	String url = "http://brightkite.com/me/received_messages/";
	HttpConnection httpConnection = null;
	String serverResponse = "";
	DirectMessageRcvScreen screen;
	Settings settings = Settings.getInstance();
	private int[] sel;
	
	public DirectMessageRcvDeleteThread(int[] sel, DirectMessageRcvScreen screen) {
		this.screen = screen;
		this.sel = sel;
	}

	public void run() {
		try {
			int[] deleted = {};
			for (int i = 0; i < this.sel.length; i++) {
				this.url = this.url + this.screen.directMessage[i].getID();
				this.url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());
				this.httpConnection = ((HttpConnection)Connector.open(this.url));
				this.httpConnection.setRequestMethod("DELETE");
				this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
				this.httpConnection.setRequestProperty("Authorization", this.settings.getAuthHeader());
				this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
				int rc = httpConnection.getResponseCode();
				if (rc == 302) {
					Arrays.add(deleted, this.sel[i]);
				}
				this.httpConnection.close();
				this.url = "http://brightkite.com/me/received_messages/";
			}
			for (int i = 0; i < deleted.length; i++) {
				System.out.println("Deleted: " + deleted[i]);
			}
			if (deleted.length > 0) {
				this.screen.callDelete(true, deleted);
			} else {
				this.screen.callDelete(false, this.sel);
			}
			if (this.httpConnection != null) {
				this.httpConnection.close();
			}
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
}