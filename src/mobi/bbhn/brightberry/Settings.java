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
	private boolean PostUpdate;
	private Placemark[] placemarks;
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
		this.PostUpdate = true;
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

	public void setPostUpdate(boolean postupdate) {
		this.PostUpdate = postupdate;
	}

	public boolean getPostUpdate() {
		return this.PostUpdate;
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

	public Placemark[] getPlacemarks() {
		return this.placemarks;
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
