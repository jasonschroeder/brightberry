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

import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class PlacemarkScreen extends MainScreen {
	RichTextField statusField = new RichTextField("", 45035996273704960L);
	MenuItem updateItem;
	MenuItem whereAmIItem;
	Settings settings = Settings.getInstance();
	String message;
	PlacemarkScreen screen = this;
	Placemark[] placemarks;
	MenuItem checkinItem;
	ListField list = new PlacemarkListField();
	protected boolean onSavePrompt() {
		return true;
	}

	public PlacemarkScreen() {
		Thread whereThread = new PlWhereAmIThread(this.screen);
	    whereThread.start();
	    
		super.setTitle(new LabelField("BrightBerry Placemarks", 1152921504606846980L));
		statusField.setText("Finding your current location");
		
		this.checkinItem = new MenuItem("Checkin Here", 1, 10) {
			public void run() {
				if (PlacemarkScreen.this.list.getSelectedIndex() > -1) {
					Placemark[] places = settings.getPlacemarks();
					String id = places[PlacemarkScreen.this.list.getSelectedIndex()].getId();
					Thread checkinThread = new PlCheckInThread(id, PlacemarkScreen.this.screen);
					checkinThread.start();
				} else {
					Status.show("No Placemark selected");
				}
			}
		};
		
		this.updateItem = new MenuItem("Update Placemarks", 3, 10) {
			public void run() {
				Status.show("Updating placemarks");
				Thread updateThread = new PlacemarksUpdateThread(PlacemarkScreen.this.screen);
				updateThread.start();
			}
		};
		
		this.whereAmIItem = new MenuItem("Where Am I", 4, 10) {
			public void run() {
				statusField.setText("Finding your current location");
				Thread whereThread = new PlWhereAmIThread(PlacemarkScreen.this.screen);
				whereThread.start();
			}
		};
		
		addMenuItem(this.updateItem);
		addMenuItem(this.whereAmIItem);
		addMenuItem(MenuItem.separator(5));
		
		add(this.statusField);
		add(new SeparatorField());
		if (settings.getPlacemarks() != null) {
			this.list.setEmptyString("Nothing to see here", DrawStyle.LEFT);
			this.list.setSize(settings.getPlacemarks().length);
			this.list.setCallback(new PlacemarkCallback(settings.getPlacemarks()));
			this.list.setRowHeight((int)(ListField.ROW_HEIGHT_FONT*2));
			add(this.list);
		} else {
			RichTextField noresults = new RichTextField("You have no placemarks set", 45035996273704960L);
			add(noresults);
		}
	}

	public void updateStatus(String message) {
		this.message = message;
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {
				Status.show("You successfully checked in at " + PlacemarkScreen.this.message);
				if (UiApplication.getUiApplication().getActiveScreen() == PlacemarkScreen.this) {
					UiApplication.getUiApplication().popScreen(PlacemarkScreen.this);
				}
			}
		});
	}

	public void updateWhereAmI(String message) {
		this.message = message;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() { 
				PlacemarkScreen.this.statusField.setText("You are currently checked in at " + PlacemarkScreen.this.message);
			}
		});
	}

	public void updatePlacemarks(final Placemark[] placemarks) {
		this.placemarks = placemarks;
		UiApplication.getUiApplication().invokeLater(new Runnable() { 
			public void run() { 
				PlacemarkScreen.this.settings.setPlacemarks(PlacemarkScreen.this.placemarks);
				Settings.save(PlacemarkScreen.this.settings);
				UiApplication.getUiApplication().popScreen(PlacemarkScreen.this.screen);
				UiApplication.getUiApplication().pushScreen(new PlacemarkScreen());
			}
		});
	}
	
	public class PlacemarkListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(checkinItem);
		}
	}
}