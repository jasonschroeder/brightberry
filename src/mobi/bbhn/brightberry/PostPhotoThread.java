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
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class PostPhotoThread extends Thread {
	String url = "http://brightkite.com/places/";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	DataOutputStream httpOutput = null;
	String serverResponse = "";
	PostPhotoScreen screen;
	private String note;
	Settings settings = Settings.getInstance();

	public PostPhotoThread(String id, String note, PostPhotoScreen screen) {
		PostPhotoThread tmp56_55 = this;
		tmp56_55.url = tmp56_55.url + id + "/photos.json";
		this.note = note;
		this.screen = screen;
	}
	
	private final String CrLf = "\r\n";
	
	public void run() {
		HttpConnection conn = null;
		OutputStream os = null;
		InputStream is = null;

		String url = this.url; //"http://www.bbhn.mobi/test.php";
		url += NetworkConfig.getConnectionParameters(this.settings.getConnectionMode());

		try {
			System.out.println("url:" + url);
			conn = (HttpConnection) Connector.open(url);
			conn.setRequestProperty("User-Agent", BrightBerry.useragent);
			conn.setRequestProperty("Content-Language", "en-US");
			conn.setRequestProperty("Authorization", this.settings.getAuthHeader());
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------4664151417711");

			InputStream imgIs = getClass().getResourceAsStream("/img/BKIcon.jpg");
			byte[] imgData = new byte[imgIs.available()];
			imgIs.read(imgData);
			
			String message0 = "";
			message0 += "-----------------------------4664151417711" + CrLf;
			message0 += "Content-Disposition: form-data; name=\"photo[body]\";" + CrLf;
			message0 += CrLf;
			message0 += this.note + CrLf;
			
			String message1 = "";
			message1 += "-----------------------------4664151417711" + CrLf;
			message1 += "Content-Disposition: form-data; name=\"photo[photo]\"; filename=\"image.jpg\"" + CrLf;
			message1 += "Content-Type: image/jpeg" + CrLf;
			message1 += CrLf;

			String message2 = "";
			message2 += CrLf + "-----------------------------4664151417711--" + CrLf;

			System.out.println("open os");
			os = conn.openOutputStream();
			
			System.out.println(message0);
			os.write(message0.getBytes());

			System.out.println(message1);
			os.write(message1.getBytes());

			// SEND THE IMAGE
			int index = 0;
			int size = 1024;
			do {
				System.out.println("write:" + index);
				if ((index + size) > imgData.length) {
					size = imgData.length - index;
				}
				os.write(imgData, index, size);
				index += size;
			} while (index < imgData.length);
			System.out.println("written:" + index);

			System.out.println(message2);
			os.write(message2.getBytes());
			
			os.flush();

			System.out.println("open is");
			is = conn.openInputStream();

			char buff = 512;
			int len;
			byte[] data = new byte[buff];
			do {
				System.out.println("READ");
				len = is.read(data);

				if (len > 0) {
					System.out.println(new String(data, 0, len));
				}
			} while (len > 0);

			System.out.println("DONE");
			int rc = conn.getResponseCode();
			if (rc == 201) {
				this.screen.callPosted(true);
			} else {
				this.screen.callPosted(false);
			}
			System.out.println("Response code: " + rc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Close connection");
			try {
				os.close();
			} catch (Exception e) {
			}
			try {
				is.close();
			} catch (Exception e) {
			}
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}
}