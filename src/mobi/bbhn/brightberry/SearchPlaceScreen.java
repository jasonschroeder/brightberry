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

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class SearchPlaceScreen extends MainScreen {
	private Criteria _criteria;
	private LocationProvider _provider;
	Location _location;
	
	AutoTextEditField searchField = new AutoTextEditField("Location: ", "", 255, AutoTextEditField.NO_NEWLINE|AutoTextEditField.SPELLCHECKABLE);
	ButtonField searchButtonField = new ButtonField("Search", 12884967424L);
	SearchPlaceScreen screen = this;
	SearchPlace[] searchPlaceResults;
	ObjectChoiceField searchChoiceField;

	MenuItem GPSItem = new MenuItem("Get GPS Location", 1, 10) {
		public void run() {
			Status.show("Getting GPS Location");
			SearchPlaceScreen.this.getGPS();
		}
	};
	private String message;
	MenuItem checkinItem;
	ListField list = new SearchPlaceListField();
	private String lasterror;
	private MenuItem placestreamItem;
	protected boolean onSavePrompt() {
		return true;
	}

	public SearchPlaceScreen() {
	    
		super.setTitle(new LabelField("BrightBerry Search Place", 1152921504606846980L));
		
		add(this.searchField);
		FieldChangeListener listener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (SearchPlaceScreen.this.searchField.getTextLength() == 0) {
					Status.show("Please enter a search");
				} else {
					Thread searchThread = new SearchPlaceThread(SearchPlaceScreen.this.screen, searchField.getText());
					searchThread.start();
				}
			}
		};
		
		checkinItem = new MenuItem("Checkin Here", 1, 10) {
			public void run() {
				if (list.getSelectedIndex() > -1) {
					int place = list.getSelectedIndex();
					Thread checkinThread = new SearchPlaceCheckInThread(searchPlaceResults[place].getId(), SearchPlaceScreen.this.screen);
					checkinThread.start();
				} else {
					Status.show("No place selected");
				}
			}
		};
		
		placestreamItem = new MenuItem("View Placestream", 2, 10) {
			public void run() {
				if (list.getSelectedIndex() > -1) {
					String placeid = searchPlaceResults[list.getSelectedIndex()].getId();
					float latitude = searchPlaceResults[list.getSelectedIndex()].getLatitude();
					float longitude = searchPlaceResults[list.getSelectedIndex()].getLongitude();
					UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "place", placeid, 0, latitude, longitude));
				} else {
					Status.show("No place selected");
				}
			}
		};
		
		this.searchButtonField.setChangeListener(listener);
		add(this.searchButtonField);
		addMenuItem(GPSItem);
	}

	public void updateSearch(SearchPlace[] results) {
		this.searchPlaceResults = results;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (SearchPlaceScreen.this.searchPlaceResults == null || SearchPlaceScreen.this.searchPlaceResults.length == 0) {
					Status.show("No locations found");
				} else {
					SearchPlaceScreen.this.list.setEmptyString("Nothing to see here", DrawStyle.LEFT);
					SearchPlaceScreen.this.list.setSize(SearchPlaceScreen.this.searchPlaceResults.length);
					SearchPlaceScreen.this.list.setCallback(new SearchPlaceCallback(SearchPlaceScreen.this.searchPlaceResults));
					SearchPlaceScreen.this.list.setRowHeight((int)(ListField.ROW_HEIGHT_FONT*2));
					SearchPlaceScreen.this.add(list);
					SearchPlaceScreen.this.delete(searchField);
					SearchPlaceScreen.this.removeMenuItem(GPSItem);
					SearchPlaceScreen.this.delete(searchButtonField);
				}
			}
		});
	}
	
	public void updateStatus(String message) {
		this.message = message;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Status.show("You successfully checked in at " + SearchPlaceScreen.this.message);
				if (UiApplication.getUiApplication().getActiveScreen() == SearchPlaceScreen.this) {
					UiApplication.getUiApplication().popScreen(SearchPlaceScreen.this);
				}
			}
		});
	}
	
	public void GPSerror(String message) {
		this.lasterror = message;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(SearchPlaceScreen.this.lasterror);
			}
		});
	}
	
	public void getGPS() {
		new Thread() {
			public void run() {
				resetProvider();
				setupCriteria();
				createLocationProvider();
				
				try {
					_location = _provider.getLocation(Settings.getInstance().getGPSTimeout());
				} catch (LocationException e) {
					SearchPlaceScreen.this.GPSerror(e.getMessage());
				} catch (InterruptedException e) {
					SearchPlaceScreen.this.GPSerror(e.getMessage());
				}
				
				if (_location != null && _location.isValid()) {
					QualifiedCoordinates coordinates = _location.getQualifiedCoordinates();
					if (coordinates.getLatitude() != 0.0 && coordinates.getLongitude() != 0.0) {
						Thread searchThread = new SearchPlaceThread(SearchPlaceScreen.this.screen, coordinates.getLatitude(), coordinates.getLongitude(), coordinates.getHorizontalAccuracy(), 0);
						searchThread.start();
					} else {
						SearchPlaceScreen.this.GPSerror("Can't location you");
					}
				}
			}
		}.start();
	}
	
	private void resetProvider() {
		if (_provider != null) {
			_provider.setLocationListener(null, 0, 0, 0);
			_provider.reset();
			_provider = null;
		}
	}
	
	private void setupCriteria() {
		_criteria = new Criteria();
		_criteria.setCostAllowed(Settings.getInstance().getAllowCost());
		_criteria.setPreferredPowerConsumption(Settings.getInstance().getPowerMode());
	}
	
	private void createLocationProvider() {
		try {
			_provider = LocationProvider.getInstance(_criteria);
		} catch (LocationException e) {			
			GPSerror(e.getMessage());
		}
	}
	
	public class SearchPlaceListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(checkinItem);
			contextMenu.addItem(placestreamItem);
		}
	}
}