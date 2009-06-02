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

import net.rim.device.api.ui.component.Dialog;

public class PrivacyGet {
	static Settings settings = Settings.getInstance();
	
	public static boolean getPrivate() { 
		try {
			String url = "http://brightkite.com/me/config.json";
			
			url += NetworkConfig.getConnectionParameters(settings.getConnectionMode());
			HttpConnection httpConnection = ((HttpConnection)Connector.open(url));
			httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			httpConnection.setRequestProperty("Content-Language", "en-US");
			httpConnection.setRequestProperty("Authorization", settings.getAuthHeader());
			InputStream httpInput = httpConnection.openInputStream();

			StringBuffer buffer = new StringBuffer();

			int ch = 0;
			while (ch != -1) {
				ch = httpInput.read();
				buffer.append((char)ch);
			}

			String serverResponse = buffer.toString();
			return parseJSON(serverResponse);

		} catch (IOException e) {
			Dialog.alert("Caught Exception");
		}
		return true;
	}
	
	private static boolean parseJSON(String json) {
		JSONObject me = null;
		try {
			me = new JSONObject(json);
			String prvmode = me.getString("privacy_mode");
			if (prvmode.equals("private")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
}
