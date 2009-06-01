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

public class PostCommentScreen extends MainScreen implements FieldChangeListener {
	Settings settings = Settings.getInstance();
	PostCommentScreen screen = this;
	AutoTextEditField comment = new AutoTextEditField("Comment: ", "", 1000, AutoTextEditField.SPELLCHECKABLE);
	ButtonField postBtn;
	MenuItem updateItem;
	private String objectID;
	private static boolean Posted;
	protected boolean onSavePrompt() {
		return true;
	}

	
	public PostCommentScreen(String objectID) {
		this.objectID = objectID;
		super.setTitle(new LabelField("BrightBerry Post Comment", Field.FIELD_HCENTER));
		this.updateItem = new MenuItem("Post Comment", 1, 10) {
			public void run() {
				if (comment.getTextLength() > 0) {
					Status.show("Posting...");
					Thread postThread = new PostCommentThread(PostCommentScreen.this.objectID, comment.getText(), PostCommentScreen.this.screen);
					postThread.start();
				} else {
					Status.show("Please enter a post");
				}
			}
		};
		
		postBtn = new ButtonField("Post Comment", ButtonField.CONSUME_CLICK);
		postBtn.setChangeListener(this);
		add(comment);
		comment.setCursorPosition(0);
		add(postBtn);
		addMenuItem(updateItem);
	}
	
	public void fieldChanged(Field field, int context) {
		if (comment.getTextLength() > 0) {
			Status.show("Posting..");
			Thread postThread = new PostCommentThread(objectID, comment.getText(), this.screen);
			postThread.start();
		} else {
			Status.show("Please enter a post");
		}
	}
	
	public void callPosted(boolean posted) {
		Posted = posted;
		UiApplication.getUiApplication().invokeLater(
				new Runnable() {
					public void run() {
						if (Posted == true) {
							Status.show("Posted Successfully");
							if (UiApplication.getUiApplication().getActiveScreen() == PostCommentScreen.this) {
								UiApplication.getUiApplication().popScreen(PostCommentScreen.this);
							}
						} else {
							Status.show("Post was unsuccessful");
						}
					}
				}
			);
	}
}