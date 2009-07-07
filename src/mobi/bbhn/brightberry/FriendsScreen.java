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

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MapsArguments;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;

public class FriendsScreen extends MainScreen {
	MenuItem updateItem;
	MenuItem whereAmIItem;
	Settings settings = Settings.getInstance();
	String message;
	FriendsScreen screen = this;
	Friends[] friends;
	PendingFriends[] pendingfriends;
	MenuItem checkinItem;
	ListField list = new FriendsListField();
	ListField pendinglist = new PendingFriendsListField();
	private MenuItem placestreamItem;
	private MenuItem mapItem;
	private MenuItem userstream;
	private MenuItem dmuser;
	private int start;
	private MenuItem refreshItem;
	private MenuItem nextItem;
	private MenuItem previousItem;
	private MenuItem approveItem;
	private MenuItem pendinguserstream;
	private MenuItem pendingdmuser;
	private MenuItem pendingblockItem;
	private MenuItem blockItem;
	protected boolean onSavePrompt() {
		return true;
	}

	public FriendsScreen(int start) {
		this.start = start;
		Thread updateThread = new FriendsThread(FriendsScreen.this.screen, 10, this.start);
		updateThread.start();
		if (BrightBerry.getPendingFriends() > 0) {
			Thread pendingThread = new PendingFriendsThread(FriendsScreen.this.screen);
			pendingThread.start();
		}
		
		super.setTitle(new LabelField("BrightBerry Friends", 1152921504606846980L));
		
		this.approveItem = new MenuItem("Approve Friend Request", 1, 10) {
			public void run() {
				if (FriendsScreen.this.pendinglist.getSelectedIndex() > -1) {
					PendingFriends[] friends = FriendsScreen.this.pendingfriends;
					String username = friends[FriendsScreen.this.pendinglist.getSelectedIndex()].getUsername();
					int sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to add " + username + " to your friend list?", Dialog.YES);
					if (Dialog.YES == sure) {
						Thread acceptThread = new FriendAcceptThread(username, FriendsScreen.this);
						acceptThread.start();
					}
				} else {
					Status.show("No Friend Selected");
				}
			}
		};
		
		this.pendingblockItem = new MenuItem("Block User", 2, 10) {
			public void run() {
				if (FriendsScreen.this.pendinglist.getSelectedIndex() > -1) {
					PendingFriends[] friends = FriendsScreen.this.pendingfriends;
					String username = friends[FriendsScreen.this.pendinglist.getSelectedIndex()].getUsername();
					int sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to block " + username + "?");
					if (Dialog.YES == sure) {
						Thread blockThread = new BlockUserThread(username, FriendsScreen.this);
						blockThread.start();
					}
				} else {
					Status.show("No Friend Selected");
				}
			}
		};
		
		this.pendinguserstream = new MenuItem("View User Stream", 3, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					PendingFriends[] friends = FriendsScreen.this.pendingfriends;
					String username = friends[FriendsScreen.this.list.getSelectedIndex()].getUsername();
					UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, username));
				} else {
					Status.show("No Friend selected");
				}
			}
		};
		
		this.pendingdmuser = new MenuItem("Send Direct Message", 4, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					PendingFriends[] friends = FriendsScreen.this.pendingfriends;
					String username = friends[FriendsScreen.this.list.getSelectedIndex()].getUsername();
					UiApplication.getUiApplication().pushScreen(new SendDirectMessageScreen(username));
				} else {
					Status.show("No Friend selected");
				}
			}
		};
		
		this.checkinItem = new MenuItem("Checkin at Friends Location", 1, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					Friends[] friends = FriendsScreen.this.friends;
					String id = friends[FriendsScreen.this.list.getSelectedIndex()].getLastPlaceID();
					Thread checkinThread = new CheckInThread(id, "friends", FriendsScreen.this.screen);
					checkinThread.start();
				} else {
					Status.show("No Friend Selected");
				}
			}
		};
		
		this.placestreamItem = new MenuItem("View Place Stream", 2, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					Friends[] friends = FriendsScreen.this.friends;
					String placeid = friends[FriendsScreen.this.list.getSelectedIndex()].getLastPlaceID();
					String placename = friends[FriendsScreen.this.list.getSelectedIndex()].getLastPlaceName();
					float latitude = friends[FriendsScreen.this.list.getSelectedIndex()].getLastLatitude();
					float longitude = friends[FriendsScreen.this.list.getSelectedIndex()].getLastLongitude();
					UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "place", 0, latitude, longitude, placeid, placename));
				} else {
					Status.show("No Friend selected");
				}
			}
		};
		
		this.userstream = new MenuItem("View User Stream", 3, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					Friends[] friends = FriendsScreen.this.friends;
					String username = friends[FriendsScreen.this.list.getSelectedIndex()].getUsername();
					UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, username));
				} else {
					Status.show("No Friend selected");
				}
			}
		};
		
		this.dmuser = new MenuItem("Send Direct Message", 4, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					Friends[] friends = FriendsScreen.this.friends;
					String username = friends[FriendsScreen.this.list.getSelectedIndex()].getUsername();
					UiApplication.getUiApplication().pushScreen(new SendDirectMessageScreen(username));
				} else {
					Status.show("No Friend selected");
				}
			}
		};
		
		this.mapItem = new MenuItem("View Friend on Blackberry Map", 5, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					Friends[] friends = FriendsScreen.this.friends;
					float latitude = friends[FriendsScreen.this.list.getSelectedIndex()].getLastLatitude();
					float longitude = friends[FriendsScreen.this.list.getSelectedIndex()].getLastLongitude();
					String label = friends[FriendsScreen.this.list.getSelectedIndex()].getUsername();
					String description = friends[FriendsScreen.this.list.getSelectedIndex()].getLastPlaceName();
		            String location = "<lbs>" + "<location lat='" + (int)(latitude*100000) + "' lon='" + (int)(longitude*100000) + "' label='" + label  +"' description='" + description + "'/>" + "</lbs>";
		            System.out.println("Location string: " + location);
		            Invoke.invokeApplication(Invoke.APP_TYPE_MAPS, new MapsArguments(MapsArguments.ARG_LOCATION_DOCUMENT, location));
				} else {
					Status.show("No Friend selected");
				}
			}
		};
		
		this.blockItem = new MenuItem("Block User", 6, 10) {
			public void run() {
				if (FriendsScreen.this.list.getSelectedIndex() > -1) {
					Friends[] friends = FriendsScreen.this.friends;
					String username = friends[FriendsScreen.this.list.getSelectedIndex()].getUsername();
					int sure = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to block " + username + "?");
					if (Dialog.YES == sure) {
						Thread blockThread = new BlockUserThread(username, FriendsScreen.this);
						blockThread.start();
					}
				} else {
					Status.show("No Friend Selected");
				}
			}
		};
		
		this.nextItem = new MenuItem("Next page", 7, 10) {
			public void run() {
				FriendsScreen.this.start = FriendsScreen.this.start + 10;
				FriendsScreen.this.refresh();
			}
		};
		
		this.previousItem = new MenuItem("Previous page", 9, 10) {
			public void run() {
				FriendsScreen.this.start = FriendsScreen.this.start - 10;
				FriendsScreen.this.refresh();
			}
		};
		
		this.refreshItem = new MenuItem("Refresh", 11, 10) {
			public void run() {
				FriendsScreen.this.refresh();
			}
		};
		
		addMenuItem(this.nextItem);
		if (this.start >= 10) {
			addMenuItem(this.previousItem);
		}
		addMenuItem(MenuItem.separator(10));
		addMenuItem(this.refreshItem);
	}

	public void refresh() {
		UiApplication.getUiApplication().popScreen(this);
		UiApplication.getUiApplication().pushScreen(new FriendsScreen(this.start));
	}
	public void updateStatus(String message) {
		this.message = message;
		UiApplication.getUiApplication().invokeLater(new Runnable() {

			public void run() {
				Status.show("You successfully checked in at " + FriendsScreen.this.message);
				if (UiApplication.getUiApplication().getActiveScreen() == FriendsScreen.this) {
					UiApplication.getUiApplication().popScreen(FriendsScreen.this);
				}
			}
		});
	}
	
	public void updatePendingFriends(final PendingFriends[] pendingfriends) {
		this.pendingfriends = pendingfriends;
		UiApplication.getUiApplication().invokeLater(new Runnable() { 
			public void run() { 
				if (FriendsScreen.this.pendingfriends != null) {
					FriendsScreen.this.pendinglist.setEmptyString("Nothing to see here", DrawStyle.LEFT);
					FriendsScreen.this.pendinglist.setSize(FriendsScreen.this.pendingfriends.length);
					FriendsScreen.this.pendinglist.setCallback(new PendingFriendsCallback(FriendsScreen.this.pendingfriends));
					FriendsScreen.this.pendinglist.setRowHeight((int)(ListField.ROW_HEIGHT_FONT*2));
					LabelField pendingLabel = new LabelField("Pending Friends", LabelField.FIELD_HCENTER|LabelField.NON_FOCUSABLE);
	                pendingLabel.setFont(FriendsScreen.this.getFont().derive(Font.BOLD, FriendsScreen.this.getFont().getHeight()+5));
	                add(pendingLabel);
	                add(new SeparatorField());
					add(FriendsScreen.this.pendinglist);
				} else {
					RichTextField noresults = new RichTextField("You have no friends to display", 45035996273704960L);
					add(noresults);
				}
			}
		});
	}

	public void updateFriends(final Friends[] friends) {
		this.friends = friends;
		UiApplication.getUiApplication().invokeLater(new Runnable() { 
			public void run() { 
				if (FriendsScreen.this.friends != null) {
					FriendsScreen.this.list.setEmptyString("Nothing to see here", DrawStyle.LEFT);
					FriendsScreen.this.list.setSize(FriendsScreen.this.friends.length);
					FriendsScreen.this.list.setCallback(new FriendsCallback(FriendsScreen.this.friends));
					FriendsScreen.this.list.setRowHeight((int)(ListField.ROW_HEIGHT_FONT*2));
					if (BrightBerry.getPendingFriends() > 0) {
						LabelField friendLabel = new LabelField("Friends", LabelField.FIELD_HCENTER|LabelField.NON_FOCUSABLE);
		                friendLabel.setFont(FriendsScreen.this.getFont().derive(Font.BOLD, FriendsScreen.this.getFont().getHeight()+5));
		                add(friendLabel);
		                add(new SeparatorField());
					}
					add(FriendsScreen.this.list);
				} else {
					RichTextField noresults = new RichTextField("You have no friends to display", 45035996273704960L);
					add(noresults);
				}
			}
		});
	}
	
	public void callAccepted(final boolean accepted) {
		UiApplication.getUiApplication().invokeLater(
				new Runnable() {
					public void run() {
						if (accepted == true) {
							Status.show("Friend Request Accepted");
							BrightBerry.setPendingFriends(BrightBerry.getPendingFriends()-1);
							if (UiApplication.getUiApplication().getActiveScreen() == FriendsScreen.this) {
								FriendsScreen.this.refresh();
							}
						} else {
							Status.show("Unable to accept friend request");
						}
					}
				}
			);
	}
	
	public void callBlocked(final boolean blocked) {
		UiApplication.getUiApplication().invokeLater(
				new Runnable() {
					public void run() {
						if (blocked == true) {
							Status.show("User blocked");
							if (UiApplication.getUiApplication().getActiveScreen() == FriendsScreen.this) {
								FriendsScreen.this.refresh();
							}
						} else {
							Status.show("Unable to block user");
						}
					}
				}
			);
	}
	
	public class FriendsListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(checkinItem);
			contextMenu.addItem(placestreamItem);
			contextMenu.addItem(userstream);
			contextMenu.addItem(dmuser);
			contextMenu.addItem(mapItem);
			contextMenu.addItem(blockItem);
		}
	}
	
	public class PendingFriendsListField extends ListField {
		protected void makeContextMenu(ContextMenu contextMenu) {
			contextMenu.addItem(approveItem);
			contextMenu.addItem(pendingblockItem);
			contextMenu.addItem(pendinguserstream);
			contextMenu.addItem(pendingdmuser);
		}
	}
}
