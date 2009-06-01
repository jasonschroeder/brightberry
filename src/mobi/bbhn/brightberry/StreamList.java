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

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

class StreamCallback implements ListFieldCallback {
	Stream[] friendstream;
	StreamScreen BrightBerryMain;

	public StreamCallback(Stream[] friendstream, StreamScreen BrightBerryMain) {
		this.friendstream = friendstream;
		this.BrightBerryMain = BrightBerryMain;
	}
	public void drawListRow(ListField list, Graphics g, int index, int y, int w) {
		Font f = g.getFont();
		int fontht = f.getHeight();
		EncodedImage c_Image = EncodedImage.getEncodedImageResource("img/icon_comments.gif");
		int coldheight = c_Image.getHeight();			
		int cnumerator = Fixed32.toFP(coldheight);
		int cdenominator = Fixed32.toFP(fontht);
		int cheightScale = Fixed32.div(cnumerator, cdenominator);
					
		EncodedImage cnewEi = c_Image.scaleImage32(cheightScale, cheightScale);
		Bitmap cImage = cnewEi.getBitmap();
	
		String type = friendstream[index].getType();
		String body = friendstream[index].getBody();
		Bitmap avator = friendstream[index].getAvator();
		String location = friendstream[index].getLocationName();
		
		g.drawBitmap(0, y, avator.getWidth(), avator.getHeight(), avator, 0, 0);
		g.setFont(f.derive(Font.BOLD));
		if (type.equals("photo")) {
			Bitmap p_Image = friendstream[index].getPhoto();
			g.drawBitmap(Display.getWidth()-p_Image.getWidth(), y, p_Image.getWidth(), p_Image.getHeight(), p_Image, 0, 0);
			w = Display.getWidth()-p_Image.getWidth();
		}
		g.drawText(friendstream[index].getCreator(), avator.getWidth()+1, y, DrawStyle.LEFT, w);
		int cc = friendstream[index].getCommentCount();
		String comment = "(" + cc + ")";
		g.setFont(f);
		g.drawText(comment, 0, y, DrawStyle.RIGHT, w);
		g.drawBitmap(w-cImage.getWidth()-f.getAdvance(comment)-1, y, cImage.getWidth(), cImage.getWidth(), cImage, 0, 0);
		
		EncodedImage t_Image;
		if (type.equals("checkin")) {
			t_Image = EncodedImage.getEncodedImageResource("img/icon_checkin.gif");
		} else if (type.equals("note")) {
			t_Image = EncodedImage.getEncodedImageResource("img/icon_note.gif");
		} else {
			t_Image = EncodedImage.getEncodedImageResource("img/icon_photo.gif");
		}
		int toldheight = t_Image.getHeight();			
		int tnumerator = Fixed32.toFP(toldheight);
		int tdenominator = Fixed32.toFP(fontht);
		int theightScale = Fixed32.div(tnumerator, tdenominator);
					
		EncodedImage tnewEi = t_Image.scaleImage32(theightScale, theightScale);
		Bitmap tImage = tnewEi.getBitmap();
		g.drawBitmap(avator.getWidth()+1, y+fontht, tImage.getWidth(), fontht, tImage, 0, 0);
		
		if (type.equals("photo")) {
			w = w-avator.getWidth()-t_Image.getWidth()-2;
		}
		
		if (type.equals("checkin") == false) {
			if (body.equals("null") == false) {
				g.drawText(body, avator.getWidth()+tImage.getWidth()+1, y+fontht, DrawStyle.ELLIPSIS, w);
			}
		} else {
			g.drawText("Checked in", avator.getWidth()+tImage.getWidth()+1, y+fontht, DrawStyle.ELLIPSIS, w);
		}
		
		if (type.equals("photo")) {
			w = Display.getWidth();
		}
		if (friendstream[index].getAbout()) {
			location = "about " + location;
		}
		if (location.startsWith("near") == false && location.startsWith("about") == false) {
			location = "@" + location;
		}
		g.drawText(location, 0, y+(fontht*2), DrawStyle.ELLIPSIS, w);
		
		EncodedImage m_Image;
		if (friendstream[index].getPublicPost()) {
			m_Image = EncodedImage.getEncodedImageResource("img/icon_public.gif");
		} else {
			m_Image = EncodedImage.getEncodedImageResource("img/icon_private.gif");
		}
		int oldheight = m_Image.getHeight();			
		int numerator = Fixed32.toFP(oldheight);
		int denominator = Fixed32.toFP(fontht);
		int heightScale = Fixed32.div(numerator, denominator);
					
		EncodedImage newEi = m_Image.scaleImage32(heightScale, heightScale);
		Bitmap bgImage = newEi.getBitmap();
		
		g.drawBitmap(0, y+(fontht*3), bgImage.getWidth(), fontht, bgImage, 0, 0);
		g.drawText(friendstream[index].getCreatedWords() + " ago", bgImage.getWidth()+1, y+(fontht*3), DrawStyle.ELLIPSIS, w);
    }
	
	public Object get(ListField listField, int index) {
        return null;
    }
	
    public int indexOfList(ListField listField, String prefix, int start) {
        return listField.indexOfList(prefix, start);
    }
    
	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}
}