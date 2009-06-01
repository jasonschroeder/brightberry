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
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class SendDirectMessageScreen extends MainScreen implements FieldChangeListener {
	Settings settings = Settings.getInstance();
	SendDirectMessageScreen screen = this;
	AutoTextEditField note = new AutoTextEditField("Message: ", "", 140, AutoTextEditField.SPELLCHECKABLE|AutoTextEditField.NO_NEWLINE);
	ButtonField postBtn;
	MenuItem updateItem;
	private String user;
	private static boolean Posted;
	protected boolean onSavePrompt() {
		return true;
	}

	
	public SendDirectMessageScreen(String user) {
		this.user = user;
		
		super.setTitle(new LabelField("BrightBerry Send Direct Message", Field.FIELD_HCENTER));
		add(new LabelField("Direct Message: " + user, Field.NON_FOCUSABLE));
		this.updateItem = new MenuItem("Send Message", 1, 10) {
			public void run() {
				if (note.getTextLength() > 0) {
					Status.show("Posting...");
					Thread postThread = new SendDirectMessageThread(SendDirectMessageScreen.this.user, note.getText(), SendDirectMessageScreen.this.screen);
					postThread.start();
				} else {
					Status.show("Please enter a message");
				}
			}
		};
		
		postBtn = new ButtonField("Send Message", ButtonField.CONSUME_CLICK);
		postBtn.setChangeListener(this);
		add(note);
		add(postBtn);

	}
	
	public void fieldChanged(Field field, int context) {
		if (note.getTextLength() > 0) {
			Status.show("Posting..");
			Thread postThread = new SendDirectMessageThread(SendDirectMessageScreen.this.user, note.getText(), SendDirectMessageScreen.this.screen);
			postThread.start();
		} else {
			Status.show("Please enter a message");
		}
	}
	
	public void callPosted(boolean posted) {
		Posted = posted;
		UiApplication.getUiApplication().invokeLater(
				new Runnable() {
					public void run() {
						if (Posted == true) {
							Status.show("Sent Successfully");
							if (UiApplication.getUiApplication().getActiveScreen() == SendDirectMessageScreen.this) {
								UiApplication.getUiApplication().popScreen(SendDirectMessageScreen.this);
							}
						} else {
							Status.show("Message was unsuccessful");
						}
					}
				}
			);
	}
}