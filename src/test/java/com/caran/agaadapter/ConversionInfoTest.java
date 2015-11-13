/*

Copyright (c) 2015, Semcon Sweden AB
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright notice, this list of conditions
    and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,  this list of 
    conditions and the following disclaimer in the documentation and/or other materials provided 
    with the distribution.
 3. Neither the name of the Semcon Sweden AB nor the names of its contributors may be used to 
    endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */
 
package com.caran.agaadapter;

import org.junit.Assert;
import org.junit.Test;

public class ConversionInfoTest {

    @Test
    public void newEmpty() {
        ConversionInfo conv = new ConversionInfo();

        Assert.assertNull(conv.signals);

        String str = conv.toString();
        Assert.assertTrue(str.contains("Signals:"));
        Assert.assertTrue(str.contains("None"));
    }

    @Test
    public void newArrayNull() {
        SignalInfo[] signals = null;

        ConversionInfo conv = new ConversionInfo(signals);

        Assert.assertNull(conv.signals);

        String str = conv.toString();
        Assert.assertTrue(str.contains("Signals:"));
        Assert.assertTrue(str.contains("None"));
    }

    @Test
    public void newArrayValues() {
        SignalInfo[] signals = new SignalInfo[3];
        signals[0] = new SignalInfo(0,"","",1f,"",false,false);
        signals[1] = new SignalInfo(1,"","",1f,"",false,false);
        signals[2] = new SignalInfo(2,"","",1f,"",false,false);

        ConversionInfo conv = new ConversionInfo(signals);

        Assert.assertNotNull(conv.signals);

        String str = conv.toString();
        Assert.assertTrue(str.contains("Signals:"));
        Assert.assertFalse(str.contains("None"));
        Assert.assertTrue(str.contains("AgaID: 0"));
        Assert.assertTrue(str.contains("AgaID: 1"));
        Assert.assertTrue(str.contains("AgaID: 2"));
    }
}
