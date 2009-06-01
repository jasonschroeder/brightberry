package mobi.bbhn.brightberry;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.json.me.JSONObject;

public class SearchPlaceCheckInThread extends Thread {
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	DataOutputStream httpOutput = null;
	String url = "http://brightkite.com/places/";
	String host = "brightkite.com";
	String serverResponse = "";
	SearchPlaceScreen screen = null;
	String message = "";

	public SearchPlaceCheckInThread(String id, SearchPlaceScreen screen) {
		SearchPlaceCheckInThread tmp56_55 = this; tmp56_55.url = tmp56_55.url + id + "/checkins.json";
		this.screen = screen;
	}

	public void run() {
		try {
			this.url += BrightBerry.appendConnectionString();
			this.httpConnection = ((HttpConnection)Connector.open(this.url));
			this.httpConnection.setRequestMethod("POST");
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Authorization", Settings.getInstance().getAuthHeader());
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