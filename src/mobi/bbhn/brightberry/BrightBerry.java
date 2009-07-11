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

import me.regexp.RE;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.NoSuchServiceException;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.event.FolderEvent;
import net.rim.blackberry.api.mail.event.FolderListener;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;

public class BrightBerry extends UiApplication {
	static String version = "0.2.8-ALPHA";
	static String gmkey = "ABQIAAAAyqsOf4y12VmEo_2G0kkmUxRpIJO9csrDHHCYF6wRDNKwcymzzRQrUdTZ3AkMMnIbfqA_JKHMK0MjHw";
	static String useragent = "BrightBerry " + version;
	static int itembgcolor = Color.WHITE;
	static int itemfontcolor = Color.BLACK;
	static int itemhlcolor = Color.LIGHTBLUE;
	static int buttonfontcolor = Color.BLACK;
	static int buttonbgcolor = Color.WHITE;
	static int buttonhlcolor = Color.LIGHTBLUE;
	static int pendingFriends;
	static int unreadMessages;
	static String currentPlace;
	static String currentPlaceID;
	final static long RUNSTORE = 0xb1850218a6a07789L;
	static ApplicationMenuItem imagemenu = new BrightBerryPhotoMenuItem();
	static long locationToAddMenuItem = ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER;
    static ApplicationMenuItemRepository amir = ApplicationMenuItemRepository.getInstance();
    static ApplicationDescriptor app = ApplicationDescriptor.currentApplicationDescriptor();
	private static boolean background = true;
	
	public static void main(String[] args) {
		Store store = Session.getDefaultInstance().getStore();
		store.addFolderListener(new FolderListener() {
			   public void messagesAdded(FolderEvent e) {
				   if (e.getMessage().isInbound() == true) {
						try {
							Address from = e.getMessage().getFrom();
							if (from.getAddr().toString().endsWith("no-reply@brightkite.com")) {
								Alert.startVibrate(3000);
								System.out.println("Brightkite Message!!!");
								RE r = new RE("http://brightkite.com/objects/[A-za-z0-9]$");
								boolean matched = r.match(e.getMessage().getBodyText());
								System.out.println("Object Match" + matched);
							}
						} catch (MessagingException e1) {
						}
						System.out.println("New email is inbound");
						System.out.println("Subject is: " + e.getMessage().getSubject());
				   }
			   }

				public void messagesRemoved(FolderEvent e) {
					System.out.println("Message Deleted");
					System.out.println("Subject is: " + e.getMessage().getSubject());
				}
			});
		
        amir.addMenuItem(locationToAddMenuItem, imagemenu, app);
        
		BrightBerry instance = new BrightBerry();
		instance.checkPermissions();
		instance.checkPermissions();
		instance.enterEventDispatcher();
	}

	public BrightBerry() {
		pushScreen(new BrightBerryMain());
	}
	
	public void checkPermissions() {
		ApplicationPermissionsManager permissionsManager = ApplicationPermissionsManager.getInstance();
		ApplicationPermissions newPermissions = new ApplicationPermissions();
		boolean permissionsRequest = false;
		
		if (DeviceInfo.hasCamera()) {
			int keyInject = permissionsManager.getPermission(ApplicationPermissions.PERMISSION_EVENT_INJECTOR);
	        if (keyInject == ApplicationPermissions.VALUE_DENY){
	            newPermissions.addPermission(ApplicationPermissions.PERMISSION_EVENT_INJECTOR);
	            permissionsRequest = true;
	        }
		}
        
        int locationApi = permissionsManager.getPermission(ApplicationPermissions.PERMISSION_LOCATION_API);
        if (locationApi == ApplicationPermissions.VALUE_DENY) {
        	newPermissions.addPermission(ApplicationPermissions.PERMISSION_LOCATION_API);
        	permissionsRequest = true;
        }
        
        int externalConnections = permissionsManager.getPermission(ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS);
        if (externalConnections == ApplicationPermissions.VALUE_DENY) {
        	newPermissions.addPermission(ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS);
        	permissionsRequest = true;
        }
        
        int wifiConnection = permissionsManager.getPermission(ApplicationPermissions.PERMISSION_WIFI);
        if (wifiConnection == ApplicationPermissions.VALUE_DENY) {
        	newPermissions.addPermission(ApplicationPermissions.PERMISSION_WIFI);
        	permissionsRequest = true;
        }
        
        if (permissionsRequest){
            boolean allowed = permissionsManager.invokePermissionsRequest(newPermissions);
            if (!allowed){
                //We would show error code here
            	System.out.println("Unable to set permissions");
            }
        }
	}
	
	public static String replaceAll(String source, String pattern, String replacement) {
        if (source == null) {
            return "";
        }
       
        StringBuffer sb = new StringBuffer();
        int idx = -1;
        int patIdx = 0;

        while ((idx = source.indexOf(pattern, patIdx)) != -1) {
            sb.append(source.substring(patIdx, idx));
            sb.append(replacement);
            patIdx = idx + pattern.length();
        }
        sb.append(source.substring(patIdx));
        return sb.toString();
    }
	
	public static int getPendingFriends() {
		return BrightBerry.pendingFriends;
	}
	
	public static void setPendingFriends(int count) {
		BrightBerry.pendingFriends = count;
	}
	
	public static int getUnreadMessages() {
		return BrightBerry.unreadMessages;
	}
	
	public static void setUnreadMessages(int count) {
		BrightBerry.unreadMessages = count;
	}
	
	public static void setCurrentPlace(String currentPlace) {
		BrightBerry.currentPlace = currentPlace;
	}
	
	public static String getCurrentPlace() {
		return BrightBerry.currentPlace;
	}
	
	public static void setCurrentPlaceID(String currentPlaceID) {
		BrightBerry.currentPlaceID = currentPlaceID;
	}
	
	public static String getCurrentPlaceID() {
		return BrightBerry.currentPlaceID;
	}
    
	private static class BrightBerryPhotoMenuItem extends ApplicationMenuItem {
		BrightBerryPhotoMenuItem() {
			super(20);
		}
		
		public String toString() {
			return "Send To BrightBerry";
		}
		        
		 public Object run(Object context) {
			 String filename = context.toString().substring(7);
			 if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".gif") || filename.endsWith(".png") || filename.endsWith(".bmp")) {
				 Application.getApplication().requestForeground();
				 UiApplication.getUiApplication().pushScreen(new PostPhotoScreen(filename));
			 } else {
				 UiEngine _uiEngine = Ui.getUiEngine();
				 Screen _screen = new Dialog(Dialog.D_OK, "Warning:", Dialog.D_OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Manager.VERTICAL_SCROLL);		
				 LabelField _text = new LabelField("File type not supported");
				 LabelField _pic = new LabelField("Brightkite supports JPEG, GIF, PNG, and BMP");
				 _screen.add(_text);
				 _screen.add(new SeparatorField());
				 _screen.add(_pic);
				 _uiEngine.pushGlobalScreen(_screen, 1, UiEngine.GLOBAL_QUEUE);
			 }
			 return context;
		 } 
	}

	public static void removeMenus() {
	    boolean remove = amir.removeMenuItem(locationToAddMenuItem, imagemenu);
	    if (remove == false) {
	    	System.out.println("Unable to remove item");
	    }
	}
	
	public static void displayAlert(String title, final String message) {
		final Screen _screen = new Dialog(Dialog.D_OK, title + ":", Dialog.D_OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Manager.VERTICAL_SCROLL);		
		LabelField _text = new LabelField(message);
		_screen.add(_text);
		UiApplication.getUiApplication().invokeLater (new Runnable() {
		    public void run() {
		    	if (message.equals("Your username and/or password do not match!")) {
		    		UiApplication.getUiApplication().pushScreen(new SettingsScreen());
			    	while (UiApplication.getUiApplication().getScreenCount() >= 2) {
			    		UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
			    	}
		    	}
		    	UiApplication.getUiApplication().pushGlobalScreen(_screen, 1, GLOBAL_MODAL);
		    }
		});
	}
	
	public static void errorUnauthorized() {
		Settings settings = Settings.getInstance();
		settings.setAuthed(false);
		Settings.save(settings);
		BrightBerry.displayAlert("Error", "Your username and/or password do not match!");
	}

	public static void toBackground() {
		BrightBerry.background = true;
	}
	
	public static void fromBackground() {
		BrightBerry.background = false;
	}
	
	public static boolean isBackground() {
		return BrightBerry.background;
	}
}