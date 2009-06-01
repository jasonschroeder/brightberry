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

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.MainScreen;

public class OptionsScreen extends MainScreen {
	Settings settings = Settings.getInstance();
	BasicEditField usernameField = new BasicEditField("Username: ", this.settings.getUsername(), 64, BasicEditField.NO_NEWLINE);
	PasswordEditField passwordField = new PasswordEditField("Password: ", this.settings.getPassword());
	NumericChoiceField maxentriesField = new NumericChoiceField("Max Entries: ", 5, 50, 5, this.settings.getMaxEntriesIndex());
	CheckboxField autoupdateField = new CheckboxField("Update friendstream on startup", this.settings.getAutoUpdate());
	CheckboxField postupdateField = new CheckboxField("Update friendstream after post", this.settings.getPostUpdate());
	ButtonField saveButtonField = new ButtonField("Save", 12884967424L);

	public OptionsScreen() {
		super.setTitle(new LabelField("BrightBerry Settings", 1152921504606846980L));
		add(this.usernameField);
		add(this.passwordField);
		add(this.maxentriesField);
		add(this.autoupdateField);
		add(this.postupdateField);
		
		FieldChangeListener listener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				OptionsScreen.this.save();
				OptionsScreen.this.close();
			}
		};
		this.saveButtonField.setChangeListener(listener);
		add(this.saveButtonField);
		this.usernameField.setCursorPosition(this.usernameField.getText().length());
	}

	public void save() {
		CheckUsername checkusername = new CheckUsername(this.usernameField.getText(), this.passwordField.getText());
		if (checkusername.run()) {
			Dialog.alert("Settings saved");
			this.settings.setPassword(this.passwordField.getText());
			this.settings.setUsername(this.usernameField.getText());
			this.settings.setMaxEntries(this.maxentriesField.getSelectedValue());
			this.settings.setMaxEntriesIndex(this.maxentriesField.getSelectedIndex());
			this.settings.setAutoUpdate(this.autoupdateField.getChecked());
			this.settings.setPostUpdate(this.postupdateField.getChecked());
			this.settings.setAuthed(true);
			Settings.save(this.settings);
		} else {
			Dialog.alert("Invalid username and/or password");
			this.settings.setPassword("");
			this.settings.setUsername("");
			this.settings.setAuthed(false);
			Settings.save(this.settings);
		}
	}
	public void close() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new BrightBerryMain());
	}
}
