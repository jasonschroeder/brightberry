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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.component.Dialog;

public class getPhoto {
	public static Bitmap getphoto(String url) { 
		try {
			int end = url.length() - 4;
			String urlend = url.substring(end);
			String urlbeg = url.substring(0, end);
			String newurl = "";
			if (urlend.endsWith(".jpg") || urlend.endsWith(".png") || urlend.endsWith(".gif")) {
				newurl = urlbeg + "-feed" + urlend;
			} else {
				newurl = url;
			}
			newurl += BrightBerry.appendConnectionString();
			HttpConnection httpConnection = ((HttpConnection)Connector.open(newurl));
			httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			httpConnection.setRequestProperty("Content-Language", "en-US");
			httpConnection.setRequestProperty("Accept", "*/*");
			httpConnection.setRequestProperty("Connection", "Keep-Alive");
			httpConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			DataInputStream iStrm = httpConnection.openDataInputStream();
			
			byte imageData[];
			int length = (int) httpConnection.getLength();
			if (length != -1) {
				imageData = new byte[length];
				iStrm.readFully(imageData);
			} else {
				ByteArrayOutputStream bStrm = new ByteArrayOutputStream();

				int ch;
				while ((ch = iStrm.read()) != -1) {
					bStrm.write(ch);
				}
	
				imageData = bStrm.toByteArray();
				bStrm.close();
			}
			EncodedImage m_Image = EncodedImage.createEncodedImage(imageData, 0, imageData.length);
			int oldWidth = m_Image.getHeight();
			int screenWidth = Display.getWidth();
			
			int numerator = Fixed32.toFP(oldWidth);
			int denominator = Fixed32.toFP(screenWidth);
			int widthScale = Fixed32.div(numerator, denominator);
			
			EncodedImage newEi = m_Image.scaleImage32(widthScale, widthScale);
			Bitmap img = newEi.getBitmap();
			return img;

		} catch (IOException e) {
			Dialog.alert("Caught Exception");
		}
		return null;
	}
}
