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
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class StreamScreen extends MainScreen {
	RichTextField statusField = new RichTextField("field", RichTextField.NON_FOCUSABLE);
	Settings settings = Settings.getInstance();
	MenuItem refreshItem;
	StreamScreen screen = this;
	String debugmsg;
	ListField list = new StreamListField();
	Stream[] stream;
	ObjectChoiceField streamChoiceField;
	private String streamtoview;
	private String title;
	private int start;
	private MenuItem nextItem;
	private MenuItem previousItem;
	private String user;
	static boolean BKauth;
	
	public StreamScreen(boolean refreshMe, String streamtoview, String user, int start) {
		this.streamtoview = streamtoview;
		this.start = start;
		this.user = user;
		
		StreamButton aroundmeButton = new StreamButton("Around Me", ButtonField.FOCUSABLE|ButtonField.CONSUME_CLICK);
		FieldChangeListener AroundMeListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				StreamScreen.this.nearbystream();
			}
		};
		aroundmeButton.setChangeListener(AroundMeListener);
		
		StreamButton universeButton = new StreamButton("Universe", ButtonField.FOCUSABLE|ButtonField.CONSUME_CLICK);
		FieldChangeListener UniverseListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				StreamScreen.this.universestream();
			}
		};
		universeButton.setChangeListener(UniverseListener);
		
		StreamButton friendsButton = new StreamButton("Me & Friends",ButtonField.FOCUSABLE|ButtonField.CONSUME_CLICK);
		FieldChangeListener FriendsListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				StreamScreen.this.friendstream();
			}
		};
		friendsButton.setChangeListener(FriendsListener);
		
		HorizontalFieldManager hfm = new HorizontalFieldManager(Manager.USE_ALL_WIDTH|NO_HORIZONTAL_SCROLL);
		
		if (this.streamtoview.equals("friend")) {
			this.title = "Friend";
			hfm.add(aroundmeButton);
		    hfm.add(universeButton);
		    add(hfm);
		    add(new SeparatorField());
		} else if (this.streamtoview.equals("nearby")) {
			this.title = "Nearby ";
			hfm.add(friendsButton);
		    hfm.add(universeButton);
		    add(hfm);
		    add(new SeparatorField());
		} else if (this.streamtoview.equals("universe")) {
			this.title = "Universe ";
			hfm.add(friendsButton);
		    hfm.add(aroundmeButton);
		    add(hfm);
		    add(new SeparatorField());
		} else if (this.streamtoview.equals("mentions")) {
			this.title = "Mentions ";
		} else if (this.streamtoview.equals("person")) {
			this.title = this.user + "'s ";
		} else {
			this.title = "My ";
		}
		
		super.setTitle(new LabelField("BrightBerry " + this.title + "stream", 1152921504606846980L));
		
		this.refreshItem = new MenuItem("Refresh", 13, 10) {
			public void run() {
				StreamScreen.this.refresh();
			}
		};
		
		this.nextItem = new MenuItem("Next " + settings.getMaxEntries() + " Posts", 10, 10) {
			public void run() {
				StreamScreen.this.start = StreamScreen.this.start + settings.getMaxEntries();
				StreamScreen.this.refresh();
			}
		};
		
		this.previousItem = new MenuItem("Previous " + settings.getMaxEntries() + " Posts", 11, 10) {
			public void run() {
				StreamScreen.this.start = StreamScreen.this.start - settings.getMaxEntries();
				StreamScreen.this.refresh();
			}
		};
		
		addMenuItem(this.nextItem);
		if (this.start >= settings.getMaxEntries()) {
			addMenuItem(this.previousItem);
		}
		addMenuItem(MenuItem.separator(12));
		addMenuItem(this.refreshItem);
		
		if (this.settings.getAutoUpdate() || refreshMe) {
			this.statusField.setText("Loading " + this.title + "stream");
			Thread posts;
			if (this.streamtoview.equals("person")) {
				posts = new StreamThread(this.screen, this.settings.getMaxEntries(), this.streamtoview, this.user, this.start);
			} else {
				posts = new StreamThread(this.screen, this.settings.getMaxEntries(), this.streamtoview, this.start);
			}
			if (posts != null) {
				posts.start();
			}
			super.add(this.statusField);
		}
	}
	
	public void updateStream(Stream[] stream) {
		this.stream = stream;
		UiApplication.getUiApplication().invokeLater(new Runnable() { 
			public void run() {
				if (StreamScreen.this.stream != null) {
					delete(statusField);
					list.setEmptyString("Nothing to see here", DrawStyle.LEFT);
					list.setSize(StreamScreen.this.stream.length);
					list.setCallback(new StreamCallback(StreamScreen.this.stream, StreamScreen.this.screen));
					list.setRowHeight(ListField.ROW_HEIGHT_FONT*4);
					add(list);
				} else {
					statusField.setText("No posts to display");
				}
			}
		});
	}
	
	public void refresh() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, this.streamtoview, null, this.start));
	}
	
	public void friendstream() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "friend", null, 0));
	}
	
	public void nearbystream() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "nearby", null, 0));
	}
	
	public void universestream() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "universe", null, 0));
	}
	
	public final class ViewMoreMenuItem extends MenuItem {
		public ViewMoreMenuItem() {
			super("View Details", 1, 1);
		}
		
		public void run() {
			String objectID = stream[list.getSelectedIndex()].getId();
			String type = stream[list.getSelectedIndex()].getType();
			UiApplication.getUiApplication().pushScreen(new BkObjectScreen(objectID, type));
		}
	}
	
	public final class FullTextMenuItem extends MenuItem {
		public FullTextMenuItem() {
			super("View Full Text", 2, 1);
		}

		public void run() {
			String body = stream[list.getSelectedIndex()].getBody();
			Dialog.alert(body);
		}
	}
	
	public final class PostCommentMenuItem extends MenuItem {
		public PostCommentMenuItem() {
			super("Post Comment", 3, 1);
		}
		
		public void run() {
			String objectID = stream[list.getSelectedIndex()].getId();
			UiApplication.getUiApplication().pushScreen(new PostCommentScreen(objectID));
		}
	}
	
	public final class SendDirectMessageMenuItem extends MenuItem {
		public SendDirectMessageMenuItem() {
			super("Send Direct Message", 4, 1);
		}
		
		public void run() {
			String creator = stream[list.getSelectedIndex()].getCreator();
			UiApplication.getUiApplication().pushScreen(new SendDirectMessageScreen(creator));
		}
	}
	
	public final class ViewStreamItem extends MenuItem {
		public ViewStreamItem() {
			super("View User Stream", 5, 1);
		}
		
		public void run() {
			String creator = stream[list.getSelectedIndex()].getCreator();
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", creator, 0));
		}
	}
	
	public class StreamListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(new ViewMoreMenuItem());
			contextMenu.addItem(new FullTextMenuItem());
			contextMenu.addItem(new PostCommentMenuItem());
			contextMenu.addItem(new SendDirectMessageMenuItem());
			if (StreamScreen.this.user == null) {
				contextMenu.addItem(new ViewStreamItem());
			}
		}
	}
}
