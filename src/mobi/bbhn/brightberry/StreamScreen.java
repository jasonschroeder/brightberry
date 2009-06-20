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

import java.util.Date;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MapsArguments;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;

public class StreamScreen extends MainScreen {
	RichTextField statusField = new RichTextField("Status field", RichTextField.NON_FOCUSABLE);
    Bitmap loading = Bitmap.getBitmapResource("BKIcon.jpg");
    BitmapField photoField = new BitmapField(loading, BitmapField.FIELD_HCENTER|BitmapField.FOCUSABLE|BitmapField.EDITABLE|HIGHLIGHT_FOCUS) {
    	protected void makeContextMenu(ContextMenu contextMenu) {
    		contextMenu.addItem(new PostNoteMenuItem());
    		contextMenu.addItem(new PostPhotoMenuItem());
			contextMenu.addItem(new ViewMapMenuItem());
			contextMenu.addItem(MenuItem.separator(8));
			contextMenu.addItem(new CheckinMenuItem());
			contextMenu.addItem(new CreatePlacemark());
		}
    };
    BasicEditField debugpost = new BasicEditField("Go to post: ", "") {
		protected void makeContextMenu(ContextMenu contextMenu) {
    		contextMenu.addItem(new DebugMenuItem());
		}
	};
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
	private float longitude;
	private float latitude;
	private boolean refreshMe;
	private String placeid;
	private String placename;
	private String message;
	private boolean deleted;
	private String deletedtype;
	private int debugger;
	private boolean plcreated;
	private int dlindex;
	static boolean BKauth;
	
	// Regular constructor
	public StreamScreen(boolean refreshMe, String streamtoview, int start) {
		this.refreshMe = refreshMe;
		this.streamtoview = streamtoview;
		this.start = start;
		this.runme();
	}
	
	// User constructor
	public StreamScreen(boolean refreshMe, String streamtoview, int start, String user) {
		this.refreshMe = refreshMe;
		this.streamtoview = streamtoview;
		this.start = start;
		this.user = user;
		this.runme();
	}
	
	// Place constructor
	public StreamScreen(boolean refreshMe, String streamtoview, int start, float latitude, float longitude, String placeid, String placename) {
		this.refreshMe = refreshMe;
		this.streamtoview = streamtoview;
		this.start = start;
		this.placeid = placeid;
		this.placename = placename;
		this.longitude = longitude;
		this.latitude = latitude;
		this.runme();
	}
	
	public void runme () {
		
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
		
		HorizontalFieldManager hfm = new HorizontalFieldManager(USE_ALL_WIDTH|NO_HORIZONTAL_SCROLL);
		
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
		} else if (this.streamtoview.equals("place")) {
			this.title = "Place ";
		} else if (this.streamtoview.equals("person")) {
			if (this.user.equals("brightberry")) {
				this.title = "latest news";
			} else {
				this.title = this.user + "'s ";
			}
		} else {
			this.title = "My ";
		}
		
		if (this.title.equals("latest news")) {
			super.setTitle(new LabelField("BrightBerry " + this.title, 1152921504606846980L));
		} else {
			super.setTitle(new LabelField("BrightBerry " + this.title + "stream", 1152921504606846980L));
		}
		
		if (this.streamtoview.equals("place")) {
			Bitmap bmPhoto = HTTPPhoto.getMapPhoto(longitude, latitude, 15);
			photoField.setBitmap(bmPhoto);
			add(photoField);
		}
		
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
			if (this.title.equals("latest news")) {
				this.statusField.setText("Loading BrightBerry " + this.title);
			} else {
				this.statusField.setText("Loading " + this.title + "stream");
			}
			Thread posts;
			Date start = new Date();
			System.out.println("Start stream: " + start.toString());
			if (this.streamtoview.equals("person")) {
				posts = new StreamThread(this.screen, this.settings.getMaxEntries(), this.streamtoview, this.user, this.start);
			} else if (this.streamtoview.equals("place")) {
				posts = new StreamThread(this.screen, this.settings.getMaxEntries(), this.streamtoview, this.start, this.placeid);
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
					if (StreamScreen.this.stream.length == 0) {
						statusField.setText("No posts or checkins to show");
					} else {
						delete(statusField);
						list.setEmptyString("No posts to display empty list", DrawStyle.LEFT);
						list.setSize(StreamScreen.this.stream.length);
						list.setCallback(new StreamCallback(StreamScreen.this.stream, StreamScreen.this.screen));
						list.setRowHeight(ListField.ROW_HEIGHT_FONT*4);
						add(list);
					}
				} else {
					removeMenuItem(nextItem);
					statusField.setText("No posts to display");
				}
				Date end = new Date();
				System.out.println("End stream: " + end.toString());
			}
		});
	}
	
	public void noPosts() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				System.out.println("No posts");
				delete(statusField);
				add(new LabelField("There are no posts to show at this place yet. Be the first!", LabelField.FOCUSABLE|LabelField.FIELD_HCENTER));
			}
		});
	}
	
	public void refresh() {
		UiApplication.getUiApplication().popScreen(this);
		if (this.streamtoview.equals("person")) {
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", this.start, this.user));
		} else if (this.streamtoview.equals("place")) {
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "place", this.start, this.longitude, this.latitude, this.placeid, this.placename));
		} else {
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, this.streamtoview, this.start));
		}
	}
	
	public void friendstream() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "friend", 0));
	}
	
	public void nearbystream() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "nearby", 0));
	}
	
	public void universestream() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "universe", 0));
	}
	
	public final class ViewMoreMenuItem extends MenuItem {
		public ViewMoreMenuItem() {
			super("View Details", 1, 1);
		}
		
		public void run() {
			String objectID = stream[list.getSelectedIndex()].getId();
			UiApplication.getUiApplication().pushScreen(new BkObjectScreen(objectID));
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
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, creator));
		}
	}
	
	public final class ViewPlaceItem extends MenuItem {
		public ViewPlaceItem() {
			super("View Place Stream", 6, 1);
		}
		
		public void run() {
			String placeid = stream[list.getSelectedIndex()].getPlaceID();
			String placename = stream[list.getSelectedIndex()].getLocationName();
			float latitude = stream[list.getSelectedIndex()].getLatitude();
			float longitude = stream[list.getSelectedIndex()].getLongitude();
			UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "place", 0, latitude, longitude, placeid, placename));
		}
	}
	
	public final class ViewMapMenuItem extends MenuItem {
		public ViewMapMenuItem() {
			super("View On Blackberry Map", 7, 1);
		}
		
		public void run() {
			float longitude = stream[list.getSelectedIndex()].getLongitude();
			float latitude = stream[list.getSelectedIndex()].getLatitude();
			String location = null;
			if (StreamScreen.this.streamtoview.equals("place") == false) {
				String creator = stream[list.getSelectedIndex()].getCreator();
				String description = stream[list.getSelectedIndex()].getBody();
				description = BrightBerry.replaceAll(description, "'", "");
				location = "<lbs>" + "<location lat='" + (int)(latitude*100000) + "' lon='" + (int)(longitude*100000) + "' label='" + creator  +"' description='" + description + "'/>" + "</lbs>";
			} else {
				location = "<lbs>" + "<location lat='" + (int)(latitude*100000) + "' lon='" + (int)(longitude*100000) + "'/>" + "</lbs>";
			}
            System.out.println("Location string: " + location);
            if (location != null) {
            	Invoke.invokeApplication(Invoke.APP_TYPE_MAPS, new MapsArguments(MapsArguments.ARG_LOCATION_DOCUMENT, location));
            }
		}
	}
	
	public final class PostNoteMenuItem extends MenuItem {
		public PostNoteMenuItem() {
			super("Post Note About", 1, 1);
		}
		
		public void run() {
			String placeid = StreamScreen.this.placeid;
			String name = StreamScreen.this.placename;
			UiApplication.getUiApplication().pushScreen(new PostNoteScreen(placeid, name));
		}
	}
	
	public final class PostPhotoMenuItem extends MenuItem {
		public PostPhotoMenuItem() {
			super("Post Photo About", 2, 1);
		}
		
		public void run() {
			String placeid = StreamScreen.this.placeid;
			String name = StreamScreen.this.placename;
			UiApplication.getUiApplication().pushScreen(new PostPhotoScreen(placeid, name));
		}
	}
	
	public final class CheckinMenuItem extends MenuItem {
		public CheckinMenuItem() {
			super("Checkin Here", 8, 1);
		}
		
		public void run() {
			String placeid = StreamScreen.this.placeid;
			Thread checkinThread = new CheckInThread(placeid, "stream", StreamScreen.this.screen);
			checkinThread.start();
		}
	}
	
	public final class DeleteObjectMenuItem extends MenuItem {
		public DeleteObjectMenuItem() {
			super("Delete " +  StreamScreen.this.stream[StreamScreen.this.list.getSelectedIndex()].getType().toUpperCase().substring(0, 1) + StreamScreen.this.stream[StreamScreen.this.list.getSelectedIndex()].getType().substring(1), 8, 1);
		}
		
		public void run() {
			String type = StreamScreen.this.stream[StreamScreen.this.list.getSelectedIndex()].getType().toUpperCase().substring(0, 1) + StreamScreen.this.stream[StreamScreen.this.list.getSelectedIndex()].getType().substring(1);
			int sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to delete this " + type + "?");
			if (Dialog.YES == sure) {
				String objectid = stream[list.getSelectedIndex()].getId();
				DeleteObjectThread delthread = new DeleteObjectThread(objectid, StreamScreen.this.screen, type, list.getSelectedIndex());
				delthread.start();
			}
		}
	}
	
	public final class CreatePlacemark extends MenuItem {
		public CreatePlacemark() {
			super("Add To Placemarks", 9, 1);
		}
		
		public void run() {
			String placeid = StreamScreen.this.placeid;
			String plname = StreamScreen.this.placename;
			if (plname.length() > 20) {
				plname = plname.substring(0, 19);
			}
			Dialog pldialog = new Dialog(Dialog.D_OK_CANCEL, "Placemark this place:", 0, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
			BasicEditField placename = new BasicEditField("", plname, 20, BasicEditField.NO_NEWLINE);
			pldialog.add(placename);
			pldialog.add(new SeparatorField());
			pldialog.add(new LabelField("Give your placemark a name. For example, \"home\", \"work\", \"hockey rink\""));
			int answer = pldialog.doModal();
			System.out.println("PD: " + answer);
			System.out.println("Length: " + placename.getText().length());
			if (answer == Dialog.OK) {
				if (placename.getText().length() < 2) {
					Dialog.alert("Placemark name must be greater than 2 characters");
				} else {
					PlacemarkCreateThread plcreate = new PlacemarkCreateThread(placeid, placename.getText(), StreamScreen.this);
					plcreate.start();
				}
				System.out.println("Text: " + placename.getText());
			}
		}
	}
	
	public void updateStatus(String message) {
		this.message = message;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Status.show("You successfully checked in at " + StreamScreen.this.message);
				if (UiApplication.getUiApplication().getActiveScreen() == StreamScreen.this) {
					UiApplication.getUiApplication().popScreen(StreamScreen.this);
				}
			}
		});
	}
	
	public class StreamListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			String creator = StreamScreen.this.stream[StreamScreen.this.list.getSelectedIndex()].getCreator();
			if (StreamScreen.this.settings.getUsername().equals(creator) == false) {
				contextMenu.addItem(new SendDirectMessageMenuItem());
			} else {
				contextMenu.addItem(MenuItem.separator(8));
				contextMenu.addItem(new DeleteObjectMenuItem());
			}
			if (StreamScreen.this.streamtoview.equals("place") == false) {
				contextMenu.addItem(new ViewMapMenuItem());
				contextMenu.addItem(new ViewPlaceItem());
			}
			contextMenu.addItem(new ViewMoreMenuItem());
			contextMenu.addItem(new FullTextMenuItem());
			contextMenu.addItem(new PostCommentMenuItem());
			if (StreamScreen.this.streamtoview.equals("person") == false) {
				contextMenu.addItem(new ViewStreamItem());
			}
		}
	}
	
	public void callDelete(boolean deleted, String type, int listindex) {
		this.deleted = deleted;
		this.deletedtype = type;
		this.dlindex = listindex;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (StreamScreen.this.deleted) {
					Status.show(StreamScreen.this.deletedtype + " deleted");
					Arrays.removeAt(StreamScreen.this.stream, StreamScreen.this.dlindex);
					StreamScreen.this.list.delete(StreamScreen.this.dlindex);
					StreamScreen.this.list.setSize(StreamScreen.this.stream.length);
					UiApplication.getUiApplication().repaint();
				} else {
					Status.show("Unable to delete " + StreamScreen.this.deletedtype.toLowerCase());
				}
			}
		});
	}
	
	public boolean keyDown(int keycode, int time) {
		char test = Keypad.map(keycode);
		String mytest = "" + test;
		if (mytest.equals("d") && this.debugger == 0) {
			this.debugger = 1;
		} else if (mytest.equals("e") && this.debugger == 1) {
			this.debugger = 2;
		} else if (mytest.equals("b") && this.debugger == 2) {
			this.debugger = 3;
		} else if (mytest.equals("u") && this.debugger == 3) {
			this.debugger = 4;
		} else if (mytest.equals("g") && this.debugger == 4) {
			add(new SeparatorField());
			add(debugpost);
		} else {
			this.debugger = 0;
		}
		return false;
	}
	
	public final class DebugMenuItem extends MenuItem {
		public DebugMenuItem() {
			super("Go to Debug Post", 1, 1);
		}
		
		public void run() {
			UiApplication.getUiApplication().pushScreen(new BkObjectScreen(debugpost.getText()));
		}
	}
	
	public void plCreated(boolean success) {
		this.plcreated = success;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if (StreamScreen.this.plcreated) {
					Dialog.alert("Placemark Created");
				} else {
					Dialog.alert("Unable to create placemark");
				}
			}
		});
	}
	
	protected boolean onSavePrompt() {
		return true;
	}
}
