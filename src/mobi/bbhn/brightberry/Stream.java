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

public class Stream {
	private String type;
	private String id;
	private String creator;
	private Bitmap photo;
	private String created_at_as_words;
	private String body;
	private String locationname;
	private float longitude;
	private float latitude;
	private boolean publicpst;
	private boolean about;
	private int comments;
	private String placeid;

	public Stream() {
	}

	public Stream(String type, String creator, String created_at_as_words, String locationname, float latitude, float longitude, String id, String body, boolean publicpst, int comments, boolean about, String placeid) {
		this.type = type;
		this.created_at_as_words = created_at_as_words;
		this.locationname = locationname;
		this.creator = creator;
		this.id = id;
		this.body = body;
		this.publicpst = publicpst;
		this.comments = comments;
		this.about = about;
		this.latitude = latitude;
		this.longitude = longitude;
		this.placeid = placeid;
	}
	
	public Stream(String type, String creator, String created_at_as_words, String locationname, float latitude, float longitude, String id, String body, boolean publicpst, int comments, boolean about, Bitmap photo, String placeid) {
		this.type = type;
		this.created_at_as_words = created_at_as_words;
		this.locationname = locationname;
		this.creator = creator;
		this.id = id;
		this.body = body;
		this.publicpst = publicpst;
		this.comments = comments;
		this.about = about;
		this.photo = photo;
		this.latitude = latitude;
		this.longitude = longitude;
		this.placeid = placeid;
	}
	
	public String getType() {
		return this.type;
	}

	public String getId() {
		return this.id;
	}
	
	public String getCreator() {
		return this.creator;
	}
	
	public Bitmap getAvatar() {
		if (ImageCache.inCache(this.creator)) {
			return ImageCache.getImage(this.creator);
		} else {
			return Bitmap.getBitmapResource("img/default_avatar.gif");
		}
	}
	
	public String getCreatedWords() {
		return this.created_at_as_words;
	}
	
	public String getBody() {
		return this.body;
	}
	
	public String getLocationName() {
		return this.locationname;
	}
	
	public boolean getPublicPost() {
		return this.publicpst;
	}
	
	public int getCommentCount() {
		return this.comments;
	}
	
	public boolean getAbout() {
		return this.about;
	}
	
	public Bitmap getPhoto() {
		return this.photo;
	}
	
	public float getLatitude() {
		return this.latitude;
	}
	
	public float getLongitude() {
		return this.longitude;
	}
	
	public String getPlaceID() {
		return this.placeid;
	}
}