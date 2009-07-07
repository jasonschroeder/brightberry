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

import net.rim.device.api.system.Alert;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class SettingsScreen extends MainScreen {
	Settings settings = Settings.getInstance();
	BasicEditField usernameField = new BasicEditField("Username: ", this.settings.getUsername(), 64, BasicEditField.NO_NEWLINE);
	PasswordEditField passwordField = new PasswordEditField("Password: ", this.settings.getPassword());
	NumericChoiceField maxentriesField = new NumericChoiceField("Max Entries: ", 5, 50, 5, this.settings.getMaxEntriesIndex());
	NumericChoiceField maxsearchField = new NumericChoiceField("Max Search Results: ", 5, 50, 5, this.settings.getMaxSearchIndex());
	NumericChoiceField maxmessagesField = new NumericChoiceField("Max Direct Messages: ", 5, 50, 5, this.settings.getMaxMessagesIndex());
	CheckboxField autoupdateField = new CheckboxField("Update friendstream on startup", this.settings.getAutoUpdate());
	CheckboxField autowhereamiField = new CheckboxField("Update Where Am I", this.settings.getAutoWhereAmI());
	String choicestrs[] = {"Auto-Detect", "BIS", "BES", "TCP"};
	ObjectChoiceField connectionField = new ObjectChoiceField("Connection: ", choicestrs, this.settings.getConnectionMode());
	String powerstrs[] = {"Doesn't matter", "Low", "Medium", "High"};
	ObjectChoiceField powerConsumption = new ObjectChoiceField("GPS Power Usage: ", powerstrs, this.settings.getPowerMode());
	CheckboxField costallowField = new CheckboxField("Allow GPS cost", this.settings.getAllowCost());
	NumericChoiceField gpstimeoutField = new NumericChoiceField("GPS Timeout: ", 5, 60, 5, this.settings.getGPSTimeoutIndex());
	NumericChoiceField snapplaceField = new NumericChoiceField("Place Snap Radius: ", 200, 2000, 100, this.settings.getSnapRadiusIndex());
	CheckboxField vibrateField = new CheckboxField("Vibrate after post", this.settings.getVibrateOnPost());
	LabelField authLabel = new LabelField("Authentication Settings", LabelField.FIELD_HCENTER);
	LabelField notifyLabel = new LabelField("Notification Settings", LabelField.FIELD_HCENTER);
	LabelField gpsLabel = new LabelField("GPS Settings", LabelField.FIELD_HCENTER);
	LabelField programLabel = new LabelField("BrightBerry Settings", LabelField.FIELD_HCENTER);
	LabelField cacheLabel = new LabelField("Cache Settings", LabelField.FIELD_HCENTER);
	LabelField imageLabel = new LabelField("Avatars in cache: " + ImageCache.size());
	ButtonField clearButtonField = new ButtonField("Clear Avatar Cache", ButtonField.FIELD_HCENTER|ButtonField.NEVER_DIRTY);
	ButtonField saveButtonField = new ButtonField("Save", ButtonField.FIELD_HCENTER);

	public SettingsScreen() {
		super.setTitle(new LabelField("BrightBerry Settings", 1152921504606846980L));
		System.out.println("Into the options screen");
		Font boldfnt = this.getFont().derive(Font.BOLD);
		this.authLabel.setFont(boldfnt);
		add(this.authLabel);
		add(this.usernameField);
		add(this.passwordField);
		add(this.connectionField);
		add(new SeparatorField());
		if (Alert.isVibrateSupported()) {
			this.notifyLabel.setFont(boldfnt);
			add(this.notifyLabel);
			add(this.vibrateField);
			add(new SeparatorField());
		}
		this.gpsLabel.setFont(boldfnt);
		add(this.gpsLabel);
		add(this.snapplaceField);
		add(this.powerConsumption);
		add(this.costallowField);
		add(this.gpstimeoutField);
		add(new SeparatorField());
		this.programLabel.setFont(boldfnt);
		add(this.programLabel);
		add(this.maxsearchField);
		add(this.maxentriesField);
		add(this.maxmessagesField);
		add(this.autoupdateField);
		add(this.autowhereamiField);
		add(new SeparatorField());
		this.cacheLabel.setFont(boldfnt);
		add(this.cacheLabel);
		add(this.imageLabel);
		
		FieldChangeListener clearlistener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				ImageCache.clearCache();
				Status.show("Image cache cleared");
			}
		};
		this.clearButtonField.setChangeListener(clearlistener);
		add(this.clearButtonField);
		
		FieldChangeListener savelistener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				SettingsScreen.this.save();
				SettingsScreen.this.close();
			}
		};
		this.saveButtonField.setChangeListener(savelistener);
		add(this.saveButtonField);
		this.usernameField.setCursorPosition(this.usernameField.getText().length());
	}

	public void save() {
		CheckUsername checkusername = new CheckUsername(this.usernameField.getText(), this.passwordField.getText(), this.connectionField.getSelectedIndex());
		if (checkusername.run()) {
			Dialog.alert("Settings saved");
			this.settings.setPassword(this.passwordField.getText());
			this.settings.setUsername(this.usernameField.getText());
			this.settings.setMaxEntries(this.maxentriesField.getSelectedValue());
			this.settings.setMaxEntriesIndex(this.maxentriesField.getSelectedIndex());
			this.settings.setMaxSearch(this.maxsearchField.getSelectedValue());
			this.settings.setMaxSearchIndex(this.maxsearchField.getSelectedIndex());
			this.settings.setMaxMessages(this.maxmessagesField.getSelectedValue());
			this.settings.setMaxMessagesIndex(this.maxmessagesField.getSelectedIndex());
			this.settings.setAutoUpdate(this.autoupdateField.getChecked());
			this.settings.setAutoWhereAmI(this.autowhereamiField.getChecked());
			this.settings.setPowerMode(this.powerConsumption.getSelectedIndex());
			this.settings.setAllowCost(this.costallowField.getChecked());
			this.settings.setAuthed(true);
			this.settings.setGPSTimeout(this.gpstimeoutField.getSelectedValue());
			this.settings.setGPSTimeoutIndex(this.gpstimeoutField.getSelectedIndex());
			this.settings.setSnapRadius(this.snapplaceField.getSelectedValue());
			this.settings.setSnapRadiusIndex(this.snapplaceField.getSelectedIndex());
			this.settings.setConnectionMode(this.connectionField.getSelectedIndex());
			if (Alert.isVibrateSupported()) {
				this.settings.setVibrateOnPost(this.vibrateField.getChecked());
			}
			Settings.save(this.settings);
		} else {
			Dialog.alert("Invalid username and/or password. Or unable to connect using selected method");
			this.settings.setAuthed(false);
			Settings.save(this.settings);
		}
	}
	
	public void close() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new BrightBerryMain());
	}
}
