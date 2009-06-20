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
import net.rim.device.api.util.Arrays;

public class DirectMessageSentScreen extends MainScreen {
	RichTextField statusField = new RichTextField("", 45035996273704960L);
	Settings settings = Settings.getInstance();
	DirectMessageSentScreen screen = this;
	ListField msglist = new ListField(0, ListField.MULTI_SELECT) {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(fulltextItem);
			String name = directMessage[msglist.getSelectedIndex()].getTo();
			if (name.equals(settings.getUsername()) == false) {
				contextMenu.addItem(sendItem);
			}
			contextMenu.addItem(ViewStreamItem);
			contextMenu.addItem(MenuItem.separator(4));
			if (msglist.getSelection().length == 1) {
				contextMenu.addItem(dlt1MenuItem);
			} else {
				contextMenu.addItem(dltmanyMenuItem);
			}
		}
	};
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
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, user));
		}
	};
	
	MenuItem dlt1MenuItem = new MenuItem("Delete Message", 5, 10) {
		public void run() {
			DirectMessageSentScreen.this.delfunc();
		}
	};
	
	MenuItem dltmanyMenuItem = new MenuItem("Delete Messages", 5, 10) {
		public void run() {
			DirectMessageSentScreen.this.delfunc();
		}
	};
	
	MenuItem nextItem = new MenuItem("Next " + settings.getMaxMessages() + " Messages", 7, 10) {
		public void run() {
			DirectMessageSentScreen.this.start = DirectMessageSentScreen.this.start + settings.getMaxMessages();
			DirectMessageSentScreen.this.refresh();
		}
	};
	
	MenuItem previousItem = new MenuItem("Previous " + settings.getMaxMessages() + " Messages", 8, 10) {
		public void run() {
			DirectMessageSentScreen.this.start = DirectMessageSentScreen.this.start - settings.getMaxMessages();
			DirectMessageSentScreen.this.refresh();
		}
	};
	
	MenuItem refreshMenuItem = new MenuItem("Refresh", 10, 10) {
		public void run() {
			DirectMessageSentScreen.this.refresh();
		}
	};
	private int[] deletedIndexs;
	private int start;
	
	protected boolean onSavePrompt() {
		return true;
	}

	public DirectMessageSentScreen(int start) {
		this.start = start;
		Thread msgThread = new DirectMessageSentThread(this.screen, settings.getMaxMessages(), this.start);
	    msgThread.start();
		super.setTitle(new LabelField("BrightBerry Direct Messages Sent", 1152921504606846980L));
		addMenuItem(MenuItem.separator(6));
		addMenuItem(nextItem);
		if (this.start >= settings.getMaxMessages()) {
			addMenuItem(previousItem);
		}
		addMenuItem(MenuItem.separator(9));
		addMenuItem(refreshMenuItem);
	}
	
	public void delfunc() {
		int[] sel = msglist.getSelection();
		int sure;
		if (sel.length == 1) {
			sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to delete this message?");
		} else { 
			sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to delete these " + sel.length + " messages?");
		}
		if (Dialog.YES == sure) {
			Thread deletetrd = new DirectMessageSentDeleteThread(sel, DirectMessageSentScreen.this.screen);
			deletetrd.start();
			if (sel.length == 1) {
				Status.show("Deleting message");
			} else {
				Status.show("Deleting messages");
			}
		}
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
					removeMenuItem(nextItem);
					DirectMessageSentScreen.this.add(new LabelField("No messages to display", LabelField.FIELD_HCENTER));
				}
			}
		});
	}
	
	public void refresh() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new DirectMessageSentScreen(this.start));
	}
	
	public void callDelete(boolean deleted, int[] delindexs) {
		this.deleted = deleted;
		this.deletedIndexs = delindexs;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (DirectMessageSentScreen.this.deleted) {
					if (DirectMessageSentScreen.this.deletedIndexs.length == 1) {
						Status.show("Message deleted");
					} else {
						Status.show("Messages deleted");
					}
					for (int i = DirectMessageSentScreen.this.deletedIndexs.length-1; i >= 0; i--) {
						Arrays.removeAt(DirectMessageSentScreen.this.directMessage, DirectMessageSentScreen.this.deletedIndexs[i]);
						System.out.println("Deleted msg: " + DirectMessageSentScreen.this.deletedIndexs[i]);
					}
					if (directMessage.length == 0) {
						DirectMessageSentScreen.this.refresh();
					} else {
						DirectMessageSentScreen.this.msglist.setSize(directMessage.length);
						UiApplication.getUiApplication().repaint();
						DirectMessageSentScreen.this.msglist.setSelectedIndex(0);
					}
				} else {
					Status.show("Unable to delete message");
				}
			}
		});
	}
}