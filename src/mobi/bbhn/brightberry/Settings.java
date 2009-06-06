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

import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;

public class Settings implements Persistable {
	private boolean Authed;
	private String Username;
	private String Password;
	private int MaxEntries;
	private int MaxEntriesIndex;
	private boolean AutoUpdate;
	private Placemark[] placemarks;
	private int ConnectionMode;
	private int powerMode;
	private boolean allowCost;
	private boolean vibrateOnPost;
	private int gpsTimeout;
	private int gpsTimeoutIndex;
	private int snapRadius;
	private int snapRadiusIndex;
	private int maxSearch;
	private int maxSearchIndex;
	private boolean AutoWhereAmI;
	private static Settings settings;
	private static final long STOREHASH = 0x6cdd063e456b4742L;
	private static PersistentObject persist = PersistentStore.getPersistentObject(STOREHASH);

	public Settings() {
		this.Authed = false;
		this.Username = "";
		this.Password = "";
		this.MaxEntries = 10;
		this.MaxEntriesIndex = 1;
		this.AutoUpdate = true;
		this.AutoWhereAmI = true;
		this.ConnectionMode = 0;
		this.powerMode = 0;
		this.allowCost = true;
		this.gpsTimeout = 30;
		this.gpsTimeoutIndex = 5;
		this.snapRadius = 200;
		this.snapRadiusIndex = 0;
		this.maxSearch = 10;
		this.maxSearchIndex = 1;
		if (Alert.isVibrateSupported()) {
			this.vibrateOnPost = true;
		} else {
			this.vibrateOnPost = false;
		}
	}

	public static Settings getInstance() {
		return settings;
	}

	public static void save(Settings settings) {
		persist = PersistentStore.getPersistentObject(STOREHASH);
		persist.setContents(settings);
		persist.commit();
		settings = (Settings) persist.getContents();
	}

	public void setAuthed(boolean authed) {
		this.Authed = authed;
	}

	public boolean getAuthed() {
		return this.Authed;
	}

	public void setUsername(String username) {
		this.Username = username;
	}

	public String getUsername() {
		return this.Username;
	}

	public void setPassword(String password) {
		this.Password = password;
	}

	public String getPassword() {
		return this.Password;
	}

	public void setMaxEntries(int maxentries) {
		this.MaxEntries = maxentries;
	}

	public int getMaxEntries() {
		return this.MaxEntries;
	}

	public void setMaxEntriesIndex(int maxentriesindex) {
		this.MaxEntriesIndex = maxentriesindex;
	}

	public int getMaxEntriesIndex() {
		return this.MaxEntriesIndex;
	}

	public void setAutoUpdate(boolean autoupdate) {
		this.AutoUpdate = autoupdate;
	}

	public boolean getAutoUpdate() {
		return this.AutoUpdate;
	}

	public void setPlacemarks(Placemark[] placemarks) {
		this.placemarks = placemarks;
	}
	
	public void setPowerMode(int powerMode) {
		this.powerMode = powerMode;
	}
	
	public int getPowerMode() {
		return this.powerMode;
	}
	
	public void setAllowCost(boolean allowCost) {
		this.allowCost = allowCost;
	}
	
	public boolean getAllowCost() {
		return this.allowCost;
	}
	
	public void setGPSTimeout(int gpsTimeout) {
		this.gpsTimeout = gpsTimeout;
	}
	
	public int getGPSTimeout() {
		return this.gpsTimeout;
	}
	
	public void setGPSTimeoutIndex(int gpsTimeoutIndex) {
		this.gpsTimeoutIndex = gpsTimeoutIndex;
	}
	
	public int getGPSTimeoutIndex() {
		return this.gpsTimeoutIndex;
	}

	public Placemark[] getPlacemarks() {
		return this.placemarks;
	}
	
	public void setConnectionMode(int mode) {
		this.ConnectionMode = mode;
	}
	
	public int getConnectionMode() {
		return this.ConnectionMode;
	}
	
	public void setVibrateOnPost(boolean vibrateOnPost) {
		this.vibrateOnPost = vibrateOnPost;
	}
	
	public boolean getVibrateOnPost() {
		return this.vibrateOnPost;
	}
	
	public void setSnapRadius(int snapRadius) {
		this.snapRadius = snapRadius;
	}
	
	public int getSnapRadius() {
		return this.snapRadius;
	}
	
	public void setSnapRadiusIndex(int snapRadiusIndex) {
		this.snapRadiusIndex = snapRadiusIndex;
	}
	
	public int getSnapRadiusIndex() {
		return this.snapRadiusIndex;
	}
	
	public void setMaxSearch(int maxSearch) {
		this.maxSearch = maxSearch;
	}
	
	public int getMaxSearch() {
		return this.maxSearch;
	}
	
	public void setMaxSearchIndex(int maxSearchIndex) {
		this.maxSearchIndex = maxSearchIndex;
	}
	
	public int getMaxSearchIndex() {
		return this.maxSearchIndex;
	}
	
	public void setAutoWhereAmI(boolean autoWhereAmI) {
		this.AutoWhereAmI = autoWhereAmI;
	}
	
	public boolean getAutoWhereAmI() {
		return this.AutoWhereAmI;
	}

	public String getAuthHeader() {
		try {
			String combo = this.Username + ":" + this.Password;
			return "Basic " + Base64OutputStream.encodeAsString(combo.getBytes(), 0, combo.getBytes().length, false, false);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "";
	} 
	
	static {
		synchronized (persist) {
			if (persist.getContents() == null) {
				persist.setContents(new Settings());
				persist.commit();
			}
		}
		settings = (Settings) persist.getContents();
	}
}
