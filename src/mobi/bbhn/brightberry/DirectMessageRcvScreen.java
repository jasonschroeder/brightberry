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

public class DirectMessageRcvScreen extends MainScreen {
	RichTextField statusField = new RichTextField("", 45035996273704960L);
	Settings settings = Settings.getInstance();
	DirectMessageRcvScreen screen = this;
	ListField msglist = new ListField(0, ListField.MULTI_SELECT) {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(fulltextItem);
			String name = directMessage[msglist.getSelectedIndex()].getSender();
			if (name.equals(settings.getUsername()) == false) {
				contextMenu.addItem(replyItem);
			}
			contextMenu.addItem(ViewStreamItem);
			contextMenu.addItem(MenuItem.separator(4));
			if (msglist.getSelection().length == 1) {
				contextMenu.addItem(dlt1menuItem);
			} else {
				contextMenu.addItem(dltmanymenuItem);
			}
			contextMenu.addItem(blockItem);
		}
	};
	DirectMessageRcv[] directMessage;
	private boolean deleted;
	int[] deletedIndexs;
	
	MenuItem fulltextItem = new MenuItem("View Full Text", 1, 10) {
		public void run() {
			String body = directMessage[msglist.getSelectedIndex()].getBody();
			Dialog.alert(body);
		}
	};
	
	MenuItem replyItem = new MenuItem("Reply", 2, 10) {
		public void run() {
			String name = directMessage[msglist.getSelectedIndex()].getSender();
			UiApplication.getUiApplication().pushScreen(new SendDirectMessageScreen(name));
		}
	};
	
	MenuItem ViewStreamItem = new MenuItem("View User Stream", 3, 10) {
		public void run() {
			String user = directMessage[msglist.getSelectedIndex()].getSender();
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, user));
		}
	};
	
	MenuItem dlt1menuItem = new MenuItem("Delete Message", 5, 10) {
		public void run() {
			DirectMessageRcvScreen.this.delfunc();
		}
	};
	
	MenuItem dltmanymenuItem = new MenuItem("Delete Messages", 5, 10) {
		public void run() {
			DirectMessageRcvScreen.this.delfunc();
		}
	};
	
	MenuItem blockItem = new MenuItem("Block User", 6, 10) {
		public void run() {
			if (DirectMessageRcvScreen.this.msglist.getSelectedIndex() > -1) {
				String username = directMessage[msglist.getSelectedIndex()].getSender();
				int sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to block " + username + "?");
				if (Dialog.YES == sure) {
					Thread blockThread = new BlockUserThread(username, DirectMessageRcvScreen.this);
					blockThread.start();
				}
			} else {
				Status.show("No user Selected");
			}
		}
	};
	
	MenuItem sentItem = new MenuItem("Sent Messages", 8, 10) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new DirectMessageSentScreen(0));
		}
	};
	
	MenuItem nextItem = new MenuItem("Next " + settings.getMaxMessages() + " Messages", 10, 10) {
		public void run() {
			DirectMessageRcvScreen.this.start = DirectMessageRcvScreen.this.start + settings.getMaxMessages();
			DirectMessageRcvScreen.this.refresh();
		}
	};
	
	MenuItem previousItem = new MenuItem("Previous " + settings.getMaxMessages() + " Messages", 11, 10) {
		public void run() {
			DirectMessageRcvScreen.this.start = DirectMessageRcvScreen.this.start - settings.getMaxMessages();
			DirectMessageRcvScreen.this.refresh();
		}
	};
	
	MenuItem refreshmenuItem = new MenuItem("Refresh", 13, 10) {
		public void run() {
			DirectMessageRcvScreen.this.refresh();
		}
	};
	
	private int start;
	
	protected boolean onSavePrompt() {
		return true;
	}

	public DirectMessageRcvScreen(int start) {
		this.start = start;
		Thread msgThread = new DirectMessageRcvThread(this.screen, settings.getMaxMessages(), this.start);
	    msgThread.start();
		super.setTitle(new LabelField("BrightBerry Direct Messages Received", 1152921504606846980L));
		addMenuItem(sentItem);
		addMenuItem(MenuItem.separator(8));
		addMenuItem(nextItem);
		if (this.start >= settings.getMaxMessages()) {
			addMenuItem(previousItem);
		}
		addMenuItem(MenuItem.separator(11));
		addMenuItem(refreshmenuItem);
	}

	public void updateMessages(final DirectMessageRcv[] directMessage) {
		this.directMessage = directMessage;
		UiApplication.getUiApplication().invokeLater(new Runnable() { 
			public void run() {
				if (directMessage.length > 0) {
					msglist.setEmptyString("No messages to display", DrawStyle.LEFT);
					msglist.setSize(directMessage.length);
					msglist.setCallback(new DirectMessageRcvCallback(DirectMessageRcvScreen.this.directMessage));
					msglist.setRowHeight(ListField.ROW_HEIGHT_FONT*3);
					add(msglist);
				} else {
					removeMenuItem(nextItem);
					if (DirectMessageRcvScreen.this.start == 0) {
						add(new LabelField("You haven't received any messages yet", LabelField.FIELD_HCENTER));
					} else {
						add(new LabelField("No messages to display", LabelField.FIELD_HCENTER));
					}
				}

			}
		});
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
			Thread deletetrd = new DirectMessageRcvDeleteThread(sel, DirectMessageRcvScreen.this.screen);
			deletetrd.start();
			if (sel.length == 1) {
				Status.show("Deleting message");
			} else {
				Status.show("Deleting messages");
			}
		}
	}
	public void refresh() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new DirectMessageRcvScreen(this.start));
	}
	
	public void callDelete(boolean deleted, int[] delindexs) {
		this.deleted = deleted;
		this.deletedIndexs = delindexs;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (DirectMessageRcvScreen.this.deleted) {
					if (DirectMessageRcvScreen.this.deletedIndexs.length == 1) {
						Status.show("Message deleted");
					} else {
						Status.show("Messages deleted");
					}
					for (int i = DirectMessageRcvScreen.this.deletedIndexs.length-1; i >= 0; i--) {
						Arrays.removeAt(DirectMessageRcvScreen.this.directMessage, DirectMessageRcvScreen.this.deletedIndexs[i]);
						System.out.println("Deleted msg: " + DirectMessageRcvScreen.this.deletedIndexs[i]);
					}
					if (directMessage.length == 0) {
						DirectMessageRcvScreen.this.refresh();
					} else {
						DirectMessageRcvScreen.this.msglist.setSize(directMessage.length);
						UiApplication.getUiApplication().repaint();
						DirectMessageRcvScreen.this.msglist.setSelectedIndex(0);
					}
				} else {
					Status.show("Unable to delete message");
				}
			}
		});
	}
	
	public void callBlocked(final boolean blocked) {
		UiApplication.getUiApplication().invokeLater(
				new Runnable() {
					public void run() {
						if (blocked == true) {
							Status.show("User blocked");
							if (UiApplication.getUiApplication().getActiveScreen() == DirectMessageRcvScreen.this) {
								DirectMessageRcvScreen.this.refresh();
							}
						} else {
							Status.show("Unable to block user");
						}
					}
				}
			);
	}
}