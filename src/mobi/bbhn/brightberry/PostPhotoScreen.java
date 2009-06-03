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

import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class PostPhotoScreen extends MainScreen implements FieldChangeListener {
	Settings settings = Settings.getInstance();
	RichTextField statusField = new RichTextField("Loading...", RichTextField.NON_FOCUSABLE);
	PostPhotoScreen screen = this;
	AutoTextEditField note = new AutoTextEditField("Caption: ", "", 140, AutoTextEditField.SPELLCHECKABLE|AutoTextEditField.NO_NEWLINE);
	EditField fileName = new EditField("File: ", "", 255, EditField.NON_FOCUSABLE);
	ButtonField postBtn;
	MenuItem updateItem;
	boolean Posted;
	private static String locationName;
	private static String locationID;
	protected boolean onSavePrompt() {
		return true;
	}
	private BrightBerryJournalListener _fileListener;
	private BrightBerry _uiApp;

	
	public PostPhotoScreen() {
		_uiApp = (BrightBerry)UiApplication.getUiApplication();
        _fileListener = new BrightBerryJournalListener(this);        
        _uiApp.addFileSystemJournalListener(_fileListener);
        
		Thread whereThread = new WhereAmIThread(this.screen);
		whereThread.start();
		super.setTitle(new LabelField("BrightBerry Post Photo", Field.FIELD_HCENTER));
		this.updateItem = new MenuItem("Post Photo", 1, 10) {
			public void run() {
				if (note.getTextLength() > 0 && fileName.getTextLength() > 0) {
					Status.show("Posting...");
					Thread postThread = new PostPhotoThread(locationID, note.getText(), PostPhotoScreen.this.screen, fileName.getText());
					postThread.start();
				} else {
					Status.show("Please enter a caption and take a photo");
				}
			}
		};
		postBtn = new ButtonField("Post Photo", ButtonField.CONSUME_CLICK);
		postBtn.setChangeListener(this);
		super.add(this.statusField);
	}
	
	public void fieldChanged(Field field, int context) {
		if (note.getTextLength() > 0 && fileName.getTextLength() > 0) {
			Status.show("Posting..");
			Thread postThread = new PostPhotoThread(locationID, note.getText(), this.screen, fileName.getText());
			postThread.start();
		} else {
			Status.show("Please enter a caption and take a photo");
		}
	}
	
	public void callPosted(boolean posted) {
		this.Posted = posted;
		UiApplication.getUiApplication().invokeLater(
				new Runnable() {
					public void run() {
						if (PostPhotoScreen.this.Posted == true) {
							Status.show("Posted Successfully");
							if (UiApplication.getUiApplication().getActiveScreen() == PostPhotoScreen.this) {
								UiApplication.getUiApplication().popScreen(PostPhotoScreen.this);
							}
						} else {
							Status.show("Post was unsuccessful");
						}
					}
				}
			);
	}
	
	public void updatePostPhotoScreen(String locName, String locID){
		locationName = locName;
		locationID = locID;
		UiApplication.getUiApplication().invokeLater(
			new Runnable() {
				public void run() {
					PostPhotoScreen.this.statusField.setText("You are currently checked in at " + PostPhotoScreen.locationName);
					PostPhotoScreen.this.add(note);
					PostPhotoScreen.this.note.setCursorPosition(0);
					PostPhotoScreen.this.add(fileName);
					PostPhotoScreen.this.add(postBtn);
					PostPhotoScreen.this.addMenuItem(updateItem);
					Dialog.alert("Once you take a photo close out the camera app and your last photo will be attached to the post");
					Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
				}
			});
	}
	
	public void updateFileName (String filename) {
		this.fileName.setText(filename);
		_uiApp.removeFileSystemJournalListener(_fileListener);
	}
}
