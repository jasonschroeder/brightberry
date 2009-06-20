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


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Keypad;

import net.rim.device.api.ui.Graphics;

public class MainButton extends Field {
	private String _text;
	private int backgroundColour = BrightBerry.itembgcolor;
	private boolean bold = false;
	Font boldfnt = this.getFont().derive(Font.BOLD);
	private int highlightColour = BrightBerry.itemhlcolor;
	
	MainButton (String intext, long style){
    	super(style);
    	_text = intext;
    }
	
	protected void fieldChangeNotify(int context) {   
		try {
			this.getChangeListener().fieldChanged(this, context);
		} catch (Exception e){ 
			
		}   
	}
	
	protected void onFocus(int direction) {   
		backgroundColour = highlightColour;
		bold = true;
		invalidate();
	}
	
	protected void onUnfocus() {   
		backgroundColour = BrightBerry.itembgcolor;
		bold = false;
		invalidate();
	} 
	
	public int getPreferredHeight(){
    	return this.getFont().getHeight()+10;
    }
	
	public int getPreferredWidth(){
    	return Display.getWidth();
    }
	
	protected void paint(Graphics graphics) {
        graphics.setColor(backgroundColour);   
        graphics.fillRect(0, 0, getWidth(), getHeight());      
        graphics.setColor(BrightBerry.itemfontcolor);
        Bitmap drawbmp = null;
        if (_text.equals("Search & Check in")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_checkin.gif");
        } else if (_text.equals("Post Note")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_note.gif");
        } else if (_text.equals("Post Photo")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_photo.gif");
        } else if (_text.equals("What's Happening?")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_happening.gif");
        } else if (_text.equals("People Near Me")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_nearby.gif");
        } else if (_text.equals("Messages")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_messages.gif");
        } else if (_text.equals("Placemarks")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_placemarks.gif");
        } else if (_text.equals("Mentions")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_mentions.gif");
        } else if (_text.endsWith("Settings")) {
        	drawbmp = Bitmap.getBitmapResource("img/icon_settings.gif");
        }
        int fntnewy = (getHeight()/2)-(this.getFont().getHeight()/2);
        if (drawbmp != null) {
        	int bmpnewy = (getHeight()/2)-(drawbmp.getHeight()/2);
        	graphics.drawBitmap(2, bmpnewy, drawbmp.getWidth(), drawbmp.getHeight(), drawbmp, 0, 0);
        	if (bold) {
        		graphics.setFont(boldfnt);
        	}
        	graphics.drawText(_text, drawbmp.getWidth()+4, fntnewy);
        } else {
        	if (bold) {
        		graphics.setFont(boldfnt);
        	}
        	graphics.drawText(_text, 2, fntnewy);
        }
        Bitmap arrow = Bitmap.getBitmapResource("img/listArrow.png");
        int newarrowy = (getHeight()/2)-(arrow.getHeight()/2);
        graphics.drawBitmap(getPreferredWidth()-arrow.getWidth(), newarrowy, arrow.getWidth(), arrow.getHeight(), arrow, 0, 0);
	}
	
	protected void layout(int width, int height) {
		setExtent(Math.min(width, getPreferredWidth()), Math.min(height, getPreferredHeight()));
	}
	
	protected boolean keyChar(char character, int status, int time) {
		if (character == Keypad.KEY_ENTER) {
			fieldChangeNotify(0);
			return true;
		}
		return super.keyChar(character, status, time);   
	}
	
	protected boolean navigationClick(int status, int time){
		fieldChangeNotify(1);
		return true;
	}
}