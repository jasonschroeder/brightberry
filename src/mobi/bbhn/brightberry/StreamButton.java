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

import javax.microedition.lcdui.Font;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class StreamButton extends Field {
	private String _text;
	private int backgroundColour = BrightBerry.buttonbgcolor;
	private int highlightColour = BrightBerry.buttonhlcolor;
	
	StreamButton (String intext, long style) {
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
		invalidate();
	}
	
	protected void onUnfocus() {   
		backgroundColour = BrightBerry.buttonbgcolor;
		invalidate();
	} 
	
	public int getPreferredHeight(){
    	return Font.getDefaultFont().getHeight()+10;
    }
	
	public int getPreferredWidth(){
    	return (Display.getWidth()/2);
    }
	
	protected void paint(Graphics graphics) {
        graphics.setColor(Color.BLACK);   
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(backgroundColour);
        graphics.fillRect(1, 1, getWidth()-2, getHeight()-2);
        graphics.setColor(BrightBerry.buttonfontcolor);   
        graphics.drawText(_text, 2, 5, DrawStyle.HCENTER, getWidth());
	}
	
	protected void layout(int width, int height) {
		setExtent(Math.min(width, getPreferredWidth()), Math.min(height, getPreferredHeight()));
	}
	
	protected boolean navigationClick(int status, int time){
		fieldChangeNotify(1);
		return true;
	}
}