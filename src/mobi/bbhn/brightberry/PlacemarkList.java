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
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

class PlacemarkCallback implements ListFieldCallback {
	Placemark[] placemark;

	public PlacemarkCallback(Placemark[] placemark) {
		this.placemark = placemark;
	}
	public void drawListRow(ListField list, Graphics g, int index, int y, int w) {
		Font f = g.getFont();
		int fontht = f.getHeight();
		Bitmap drawbmp = Bitmap.getBitmapResource("img/icon_placemarks.gif");
		int newy = (y+(drawbmp.getHeight()/2));
		System.out.println ("New Y; " + newy);
		g.drawBitmap(2, newy, drawbmp.getWidth(), drawbmp.getHeight(), drawbmp, 0, 0);
		g.setFont(f.derive(Font.BOLD));
		g.drawText(placemark[index].getName(), drawbmp.getWidth()+3, y, DrawStyle.ELLIPSIS);
		g.setFont(f);
		g.drawText(placemark[index].getDisplayLocation(), drawbmp.getWidth()+3, y+fontht, DrawStyle.ELLIPSIS);
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