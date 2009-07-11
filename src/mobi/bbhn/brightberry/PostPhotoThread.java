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

import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.Alert;

public class PostPhotoThread extends Thread {
	String url = "http://brightkite.com/places/";
	HttpConnection httpConnection = null;
	InputStream httpInput = null;
	OutputStream os = null;
	String serverResponse = "";
	PostPhotoScreen screen;
	static Settings settings = Settings.getInstance();
	String CrLf = "\r\n";
	private String note;
	private String filename;

	public PostPhotoThread(String id, String note, PostPhotoScreen screen, String filename) {
		url = url + id + "/photos.json";
		this.note = note;
		this.screen = screen;
		this.filename = filename;
	}
	
	public void run() {
		url += NetworkConfig.getConnectionParameters(settings.getConnectionMode());

		try {
			this.httpConnection = (HttpConnection) Connector.open(url);
			this.httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			this.httpConnection.setRequestProperty("Content-Language", "en-US");
			this.httpConnection.setRequestProperty("Authorization", settings.getAuthHeader());
			this.httpConnection.setRequestProperty("x-rim-transcode-content", "none");
			this.httpConnection.setRequestMethod(HttpConnection.POST);
			this.httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------4664151417711");

			System.out.println("File Name: file://" + filename);
			FileConnection fcon = (FileConnection)Connector.open("file://" + filename);
			InputStream imgIs = fcon.openInputStream();
			System.out.println("File Size: " + fcon.fileSize());

			byte[] imgData = new byte[(int) fcon.fileSize()];
			imgIs.read(imgData);
			
			String message0 = "";
			message0 += "-----------------------------4664151417711" + CrLf;
			message0 += "Content-Disposition: form-data; name=\"photo[body]\";" + CrLf;
			message0 += CrLf;
			message0 += note + CrLf;
			
			String message1 = "";
			message1 += "-----------------------------4664151417711" + CrLf;
			if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
				message1 += "Content-Disposition: form-data; name=\"photo[photo]\"; filename=\"BrightBerry-uploadedimage.jpg\"" + CrLf;
				message1 += "Content-Type: image/jpeg" + CrLf;
			} else if (filename.endsWith(".png")) {
				message1 += "Content-Disposition: form-data; name=\"photo[photo]\"; filename=\"BrightBerry-uploadedimage.png\"" + CrLf;
				message1 += "Content-Type: image/x-png" + CrLf;
			} else if (filename.endsWith(".gif")) {
				message1 += "Content-Disposition: form-data; name=\"photo[photo]\"; filename=\"BrightBerry-uploadedimage.gif\"" + CrLf;
				message1 += "Content-Type: image/gif" + CrLf;
			} else if (filename.endsWith(".bmp")) {
				message1 += "Content-Disposition: form-data; name=\"photo[photo]\"; filename=\"BrightBerry-uploadedimage.bmp\"" + CrLf;
				message1 += "Content-Type: image/x-ms-bmp" + CrLf;
			}
			message1 += CrLf;

			String message2 = "";
			message2 += CrLf + "-----------------------------4664151417711--" + CrLf;

			System.out.println("open os");
			os = this.httpConnection.openOutputStream();
			
			System.out.println(message0);
			os.write(message0.getBytes());

			System.out.println(message1);
			os.write(message1.getBytes());

			// SEND THE IMAGE
			int index = 0;
			int size = 1024;
			do {
				System.out.println("write:" + index);
				int percent = (index * 100) / imgData.length;
				System.out.println("Data percent: " + percent);
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
			this.httpInput = this.httpConnection.openInputStream();

			char buff = 512;
			int len;
			byte[] data = new byte[buff];
			do {
				System.out.println("READ");
				len = this.httpInput.read(data);

				if (len > 0) {
					System.out.println(new String(data, 0, len));
				}
			} while (len > 0);

			System.out.println("DONE");
			int rc = this.httpConnection.getResponseCode();
			if (rc == 201) {
				if (Alert.isVibrateSupported() && settings.getVibrateOnPost()) {
					Alert.startVibrate(2000);
				}
				screen.callPosted(true);
			} else {
				if (rc == 503) {
					BrightBerry.displayAlert("Error", "BrightKite is too busy at the moment try again later");
				} else if (rc == 401 || rc == 403) {
					BrightBerry.errorUnauthorized();
				}
				screen.callPosted(false);
			}
			System.out.println("Response code: " + rc);
			if (this.os != null) {
				this.os.close();
			}
			if (this.httpInput != null) {
				this.httpInput.close();
			}
			if (this.httpConnection != null) {
				this.httpConnection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
