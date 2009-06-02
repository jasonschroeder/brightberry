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

public class NetworkConfig {
	public static final int  MODE_AUTODETECT  = 0;
	public static final int  MODE_BIS    = 1;
	public static final int  MODE_BES    = 2;
	public static final int  MODE_TCP    = 3;
  
	private static String  connectionParameters;
	private static int    connectionMode;
	private static String  mdsConnection  = ";deviceside=false";
	private static String  tcpConnection  = ";deviceside=true";
	private static String  bisConnection  = ";deviceside=false;ConnectionType=mds-public";
  
	public static void init() {
		int m = Settings.getInstance().getConnectionMode();
		if (m > MODE_AUTODETECT) {
			setMode(m);
		} else {
			setMode(autodetect());
		}
	}

	private static int autodetect() {
		// if you have BES service book, use it
		try {
			ServiceBook sb = ServiceBook.getSB();
			ServiceRecord[] records = sb.findRecordsByCid("IPPP");
			if (records != null) {
				for (int i = records.length - 1; i >= 0; i--) {
					ServiceRecord rec = records[i];
					if (rec.isValid() && !rec.isDisabled()) {
						if (rec.getEncryptionMode() == ServiceRecord.ENCRYPT_RIM) {
							return MODE_BES;
						} else {
							return MODE_BIS;
						}
					}
				}
			}
			return MODE_TCP;
		} catch (Exception e) {
			// no permissions to explore service book. fall to TCP, I guess
			return MODE_TCP;
		}
	}

	private static void setMode(int mode) {
		switch (mode) {
			case MODE_BES:
				connectionParameters = mdsConnection;
				break;
			case MODE_TCP:
				connectionParameters = tcpConnection;
				break;
			case MODE_BIS:
				connectionParameters = bisConnection;
				break;
		}
		connectionMode = mode;
	}

	public static String getConnectionParameters() {
		if (connectionParameters == null) {
			init();
		}
		return connectionParameters;
	}

	public static String getConnectionParameters(int mode) {
		switch (mode) {
			case MODE_BES:
				return mdsConnection;
			case MODE_TCP:
				return tcpConnection;
			case MODE_BIS:
				return bisConnection;
			case MODE_AUTODETECT:
				return getConnectionParameters(autodetect());
			default:
				return null;
		}
	}

	public static int getConnectionMode() {
		return connectionMode;
	}
}