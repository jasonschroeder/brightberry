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

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.UiApplication;

public class BrightBerry extends UiApplication {
	static String useragent = "Brightkite Blackberry v1.0 alpha by challgren";
	static int itembgcolor = Color.WHITE;
	static int itemfontcolor = Color.BLACK;
	static int itemhlcolor = Color.LIGHTBLUE;
	static int buttonfontcolor = Color.BLACK;
	static int buttonbgcolor = Color.LIGHTGREY;
	static int buttonhlcolor = Color.LIGHTBLUE;
	static String version = "0.1.3";
	
	public static void main(String[] args) {
		BrightBerry instance = new BrightBerry();
		instance.enterEventDispatcher();
	}

	public BrightBerry() {
		pushScreen(new BrightBerryMain());
	}
	
	public static String appendConnectionString() {
		ServiceRecord[] ippprecordArray = ServiceBook.getSB().findRecordsByCid("IPPP");
		if (ippprecordArray == null) {
			return ";deviceside=true";
		}

		int numRecords = ippprecordArray.length;
		for (int i = 0; i < numRecords; ++i) {
			ServiceRecord ipppRecord = ippprecordArray[i];
			if ((ipppRecord.isValid()) && (ipppRecord.getName().equals("IPPP for BIBS"))) {
				//return ";deviceside=false;ConnectionUID=" + ipppRecord.getUid();
				return ";deviceside=true";
			}
		}
		return ";deviceside=true";
	}
	
	public static String connectionInfo() {
		ServiceRecord[] ippprecordArray = ServiceBook.getSB().findRecordsByCid("IPPP");
		if (ippprecordArray == null) {
			return "device (No Records)";
		}

		int numRecords = ippprecordArray.length;
		for (int i = 0; i < numRecords; ++i) {
			ServiceRecord ipppRecord = ippprecordArray[i];
			if ((ipppRecord.isValid()) && (ipppRecord.getName().equals("IPPP for BIBS"))) {
				return "BIS-B " + ipppRecord.getUid();
			}
		}
		return "device";
	}
}