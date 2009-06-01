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
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.container.DialogFieldManager;

public final class PrivacyDialog extends Dialog {
	RadioButtonGroup rgrp = new RadioButtonGroup();
	
    public PrivacyDialog(String choices[], int values[]) {
        super("Privacy mode", choices, values, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
        
        RadioButtonField publicmode = new RadioButtonField("Public", rgrp, false);
    	RadioButtonField privatemode = new RadioButtonField("Private", rgrp, false);
        if (PrivacyGet.getPrivate()) {
        	publicmode = new RadioButtonField("Public", rgrp, false);
        	privatemode = new RadioButtonField("Private", rgrp, true);
        } else {
        	publicmode = new RadioButtonField("Public", rgrp, true);
        	privatemode = new RadioButtonField("Private", rgrp, false);
        }
        
        Manager delegate = getDelegate();
        if( delegate instanceof DialogFieldManager) {
            DialogFieldManager dfm = (DialogFieldManager)delegate;
            Manager manager = dfm.getCustomManager();
            if( manager != null ) {
				manager.insert(publicmode, 0);
                manager.insert(privatemode, 1);
            }
        }
    }    
}