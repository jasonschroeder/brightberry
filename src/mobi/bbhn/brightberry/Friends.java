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

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.util.Comparator;

public class Friends implements Comparator {
	private String username;
	private String fullname;
	private String last_active;
	private String last_checkin;
	private String last_location;
	private String last_placeid;
	private String last_placename;
	private float last_latitude;
	private float last_longitude;
	private Bitmap avator;

	public Friends() {
	}

	public Friends(String username, String fullname, String last_active, String last_checkin, String last_location, String last_placeid, String last_placename, float last_latitude, float last_longitude, Bitmap avator) {
		this.username = username;
		this.fullname = fullname;
		this.last_active = last_active;
		this.last_checkin = last_checkin;
		this.last_location = last_location;
		this.last_placeid = last_placeid;
		this.last_placename = last_placename;
		this.last_latitude = last_latitude;
		this.last_longitude = last_longitude;
		this.avator = avator;
	}

	public String getUsername() {
		return this.username;
	}
	
	public String getFullname() {
		return this.fullname;
	}
	
	public String getLastActive() {
		return this.last_active;
	}
	
	public String getLastCheckin() {
		return this.last_checkin;
	}
	
	public String getLastLocation() {
		return this.last_location;
	}
	
	public String getLastPlaceID() {
		return this.last_placeid;
	}
	
	public String getLastPlaceName() {
		return this.last_placename;
	}
	
	public float getLastLatitude() {
		return this.last_latitude;
	}
	
	public float getLastLongitude() {
		return this.last_longitude;
	}
	
	public Bitmap getAvator() {
		return this.avator;
	}
	
	public int compare(Object Object1, Object Object2) {
		String name1 = ((Friends) Object1).getUsername().toLowerCase();
		String name2 = ((Friends) Object2).getUsername().toLowerCase();
		return name1.compareTo(name2);
	}
}
