package mobi.bbhn.brightberry;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.Dialog;

public class AvatorThread extends Thread {
	private String user;
	private String url;
	static Settings settings = Settings.getInstance();

	public AvatorThread(String user, String url) {
		this.user = user;
		this.url = url;
	}
	
	public void run() {
		try {
			Date start = new Date();
			System.out.println("AvatorThread user: " + this.user);
			System.out.println("Avator " + user + " Start: " + start.toString());
			this.url += NetworkConfig.getConnectionParameters(settings.getConnectionMode());
			HttpConnection httpConnection = ((HttpConnection)Connector.open(this.url));
			httpConnection.setRequestProperty("User-Agent", BrightBerry.useragent);
			httpConnection.setRequestProperty("Content-Language", "en-US");
			httpConnection.setRequestProperty("Accept", "*/*");
			httpConnection.setRequestProperty("Connection", "Keep-Alive");
			httpConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			httpConnection.setRequestProperty("x-rim-transcode-content", "none");
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
			int oldHeight = m_Image.getHeight();
			Font f = Font.getDefault();
			int fontHeight = f.getHeight()*2;
			
			int numerator = Fixed32.toFP(oldHeight);
			int denominator = Fixed32.toFP(fontHeight);
			int widthScale = Fixed32.div(numerator, denominator);
			
			EncodedImage newEi = m_Image.scaleImage32(widthScale, widthScale);
			Bitmap img = newEi.getBitmap();
			ImageCache.cacheImage(this.user, img);
			Date end = new Date();
			System.out.println("Avator " + user + " End: " + end.toString());
		} catch (IOException e) {
			Dialog.alert("Caught Exception");
		}
	}
}
