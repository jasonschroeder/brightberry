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


import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.EventInjector;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class PostPhotoScreen extends MainScreen implements FieldChangeListener {
	Settings settings = Settings.getInstance();
	RichTextField statusField = new RichTextField("Loading...", RichTextField.NON_FOCUSABLE);
	PostPhotoScreen screen = this;
	AutoTextEditField note = new AutoTextEditField("Caption: ", "", 140, AutoTextEditField.SPELLCHECKABLE|AutoTextEditField.NO_NEWLINE);
	EditField fileName = new EditField("File: ", "", 255, EditField.NON_FOCUSABLE);
	RichTextField leftField = new RichTextField("Characters Left: 140/140", RichTextField.NON_FOCUSABLE);
	ButtonField postBtn;
	MenuItem updateItem = new MenuItem("Post Photo", 1, 10) {
		public void run() {
			if (filetoupload.length() > 0) {
				Status.show("Posting...");
				PostPhotoThread thread = new PostPhotoThread(locationID, note.getText(), screen, filetoupload);
				thread.start();
				if (UiApplication.getUiApplication().getActiveScreen() == PostPhotoScreen.this) {
					UiApplication.getUiApplication().popScreen(PostPhotoScreen.this);
				}
			} else {
				Status.show("Please attach a photo");
			}
		}
	};
	boolean Posted;
	private static String locationName;
	private static String locationID;
	private BrightBerryJournalListener _fileListener;
	private BrightBerry _uiApp;
	private String filetoupload;
	private boolean fromExplorer = false;
	
	protected boolean onSavePrompt() {
		return true;
	}

	FieldChangeListener inputListener = new FieldChangeListener() {
		public void fieldChanged(Field field, int context) {
			int charleft = 140-((BasicEditField) field).getTextLength();
			leftField.setText("Characters Left: " + charleft + "/140");
		}
	};
	
	// Post photo at the current checked in location from the app
	public PostPhotoScreen() {
		_uiApp = (BrightBerry)UiApplication.getUiApplication();
    	_fileListener = new BrightBerryJournalListener(this);        
    	_uiApp.addFileSystemJournalListener(_fileListener);
        	
		Thread whereThread = new WhereAmIThread(this.screen);
		whereThread.start();
		setTitle(new LabelField("BrightBerry Post Photo", Field.FIELD_HCENTER));
		postBtn = new ButtonField("Post Photo", ButtonField.CONSUME_CLICK);
		postBtn.setChangeListener(this);
	}
	
	// Post a photo at the current checked in location from file explorer
	public PostPhotoScreen(String filename) {
		filetoupload = filename;
	    int lastSlash = filename.lastIndexOf('/') + 1;
        String newfilename = filename.substring(lastSlash, filename.length());
		this.fileName.setText(newfilename);
		this.fromExplorer = true;
		
		Thread whereThread = new WhereAmIThread(this.screen);
		whereThread.start();
		setTitle(new LabelField("BrightBerry Post Photo", Field.FIELD_HCENTER));
		postBtn = new ButtonField("Post Photo", ButtonField.CONSUME_CLICK);
		postBtn.setChangeListener(this);
		
	}
	
	// Post photo about a location
	public PostPhotoScreen(String placeid, String name) {
		locationID = placeid;
		
		_uiApp = (BrightBerry)UiApplication.getUiApplication();
        _fileListener = new BrightBerryJournalListener(this);        
        _uiApp.addFileSystemJournalListener(_fileListener);

		setTitle(new LabelField("BrightBerry Post Photo", Field.FIELD_HCENTER));
		postBtn = new ButtonField("Post Photo", ButtonField.CONSUME_CLICK);
		postBtn.setChangeListener(this);
		
		VerticalFieldManager status = new VerticalFieldManager();
		status.add(leftField);
		status.add(new SeparatorField());
		status.add(statusField);
		setStatus(status);
		statusField.setText("About: " + name);
		note.setChangeListener(inputListener);
		note.setCursorPosition(0);
		add(note);
		add(fileName);
		add(postBtn);
		addMenuItem(updateItem);
		Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
	}
	
	public void fieldChanged(Field field, int context) {
		if (filetoupload.length() > 0) {
			Status.show("Posting...");
			PostPhotoThread thread = new PostPhotoThread(locationID, note.getText(), screen, filetoupload);
			thread.start();
			if (UiApplication.getUiApplication().getActiveScreen() == PostPhotoScreen.this) {
				UiApplication.getUiApplication().popScreen(PostPhotoScreen.this);
			}
		} else {
			Status.show("Please attach a photo");
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
	
	public void updateLocation(String locName, String locID){
		locationName = locName;
		locationID = locID;
		UiApplication.getUiApplication().invokeLater(
			new Runnable() {
				public void run() {
					VerticalFieldManager status = new VerticalFieldManager();
					status.add(leftField);
					status.add(new SeparatorField());
					status.add(statusField);
					PostPhotoScreen.super.setStatus(status);
					PostPhotoScreen.this.statusField.setText("You're checked in @ " + PostPhotoScreen.locationName);
					PostPhotoScreen.this.note.setChangeListener(PostPhotoScreen.this.inputListener);
					PostPhotoScreen.this.note.setCursorPosition(0);
					PostPhotoScreen.this.add(note);
					PostPhotoScreen.this.add(fileName);
					PostPhotoScreen.this.add(postBtn);
					PostPhotoScreen.this.addMenuItem(updateItem);
					if (PostPhotoScreen.this.fromExplorer == false) {
						Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
					} else {
						PostPhotoScreen.this.drawPreview(PostPhotoScreen.this.filetoupload);
					}
				}
			});
	}
	
	public void updateFileName (String filename) {
		filetoupload = filename;
		EventInjector.KeyEvent inject = new EventInjector.KeyEvent(EventInjector.KeyEvent.KEY_DOWN, Characters.ESCAPE, 0);
	    inject.post();
	    inject.post();
	    int lastSlash = filename.lastIndexOf('/') + 1;
        String newfilename = filename.substring(lastSlash, filename.length());
		this.fileName.setText(newfilename);
		drawPreview(filename);
		_uiApp.removeFileSystemJournalListener(_fileListener);
	}
	
	public void drawPreview(String filename) {
		try {
			FileConnection fconn = (FileConnection)Connector.open("file://" + filename);
			// If no exception is thrown, then the URI is valid, but the file may or may not exist.
			if (fconn.exists()) {
				InputStream input = fconn.openInputStream();
				int available = (int) fconn.fileSize();
				System.out.println("Available: " + available);
				byte[] data = new byte[available];
				input.read(data, 0, available);
				EncodedImage image = EncodedImage.createEncodedImage(data, 0, data.length);
				int oldHeight = image.getHeight();
				int Height = 150;
				
				int numerator = Fixed32.toFP(oldHeight);
				int denominator = Fixed32.toFP(Height);
				int widthScale = Fixed32.div(numerator, denominator);
				
				EncodedImage newEi = image.scaleImage32(widthScale, widthScale);
				Bitmap b = newEi.getBitmap();
				BitmapField picture = new BitmapField(b, BitmapField.FOCUSABLE|BitmapField.FIELD_HCENTER);
				add(picture);
			}
			fconn.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}
