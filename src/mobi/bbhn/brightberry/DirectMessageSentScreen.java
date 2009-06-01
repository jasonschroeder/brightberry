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
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class DirectMessageSentScreen extends MainScreen {
	RichTextField statusField = new RichTextField("", 45035996273704960L);
	Settings settings = Settings.getInstance();
	DirectMessageSentScreen screen = this;
	ListField msglist = new DirectMessageSentListField();
	DirectMessageSent[] directMessage;
	private boolean deleted;
	
	MenuItem fulltextItem = new MenuItem("View Full Text", 1, 10) {
		public void run() {
			String body = directMessage[msglist.getSelectedIndex()].getBody();
			Dialog.alert(body);
		}
	};
	
	MenuItem sendItem = new MenuItem("Send Message", 2, 10) {
		public void run() {
			String name = directMessage[msglist.getSelectedIndex()].getTo();
			UiApplication.getUiApplication().pushScreen(new SendDirectMessageScreen(name));
		}
	};
	
	MenuItem ViewStreamItem = new MenuItem("View User Stream", 3, 10) {
		public void run() {
			String user = directMessage[msglist.getSelectedIndex()].getTo();
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", user, 0));
		}
	};
	
	MenuItem dltmenuItem = new MenuItem("Delete Message", 5, 10) {
		public void run() {
			int id = directMessage[msglist.getSelectedIndex()].getID();
			Thread deletetrd = new DirectMessageSentDeleteThread(id, DirectMessageSentScreen.this.screen);
			deletetrd.start();
			Status.show("Deleting message");
		}
	};
	
	protected boolean onSavePrompt() {
		return true;
	}

	public DirectMessageSentScreen() {
		Thread msgThread = new DirectMessageSentThread(this.screen);
	    msgThread.start();
		super.setTitle(new LabelField("BrightBerry Direct Messages Sent", 1152921504606846980L));
	}

	public void updateMessages(final DirectMessageSent[] directMessage) {
		this.directMessage = directMessage;
		UiApplication.getUiApplication().invokeLater(new Runnable() { 
			public void run() {
				if (DirectMessageSentScreen.this.directMessage.length > 0) {
					DirectMessageSentScreen.this.msglist.setEmptyString("Nothing to see here", DrawStyle.LEFT);
					DirectMessageSentScreen.this.msglist.setSize(DirectMessageSentScreen.this.directMessage.length);
					DirectMessageSentScreen.this.msglist.setCallback(new DirectMessageSentCallback(DirectMessageSentScreen.this.directMessage));
					DirectMessageSentScreen.this.msglist.setRowHeight(ListField.ROW_HEIGHT_FONT*3);
					DirectMessageSentScreen.this.add(msglist);
				} else {
					DirectMessageSentScreen.this.add(new LabelField("No message to display", LabelField.FIELD_HCENTER));
				}
			}
		});
	}
	
	public void callDelete(boolean deleted) {
		this.deleted = deleted;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (DirectMessageSentScreen.this.deleted) {
					Status.show("Message deleted");
				} else {
					Status.show("Unable to delete message");
				}
			}
		});
	}
	
	public class DirectMessageSentListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(fulltextItem);
			contextMenu.addItem(sendItem);
			contextMenu.addItem(ViewStreamItem);
			contextMenu.addItem(MenuItem.separator(4));
			contextMenu.addItem(dltmenuItem);
		}
	}
}