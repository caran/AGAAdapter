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

public class SignalInfoTest {

    @Test
    public void VerifyFieldValue() {
        final int     Id = 123456789;
        final String  Type = "This is the AGA type!";
        final String  Name = "aga/name";
        final Float   Multi = 0f;
        final String  Topic = "the/mqtt/topic";
        final boolean toAga = true;
        final boolean fromAga = false;

        // Create signal
        SignalInfo signal = new SignalInfo(Id, Type, Name, Multi, Topic, toAga, fromAga);

        // Verify field values
        Assert.assertEquals(Id, signal.agaId);
        Assert.assertEquals(Type, signal.agaType);
        Assert.assertEquals(Name, signal.agaName);
        Assert.assertEquals(Multi, signal.agaMultiplier);
        Assert.assertEquals(Topic, signal.mqttTopic);
        Assert.assertEquals(toAga, signal.toAga);
        Assert.assertEquals(fromAga, signal.fromAga);
    }

    @Test
    public void VerifyStringConvert() {
        final int     Id = 987654321;
        final String  Type = "This is the AGA type!";
        final String  Name = "the/aga/name";
        final Float   Multi = 1.01010f;
        final String  Topic = "mqtt/topic";
        final boolean toAga = false;
        final boolean fromAga = false;

        // Create signal
        SignalInfo signal = new SignalInfo(Id, Type, Name, Multi, Topic, toAga, fromAga);

        // Convert to string
        String str = signal.toString();

        // Verify string
        Assert.assertTrue(str.contains(Integer.toHexString(Id)));
        Assert.assertTrue(str.contains(Type));
        Assert.assertTrue(str.contains(Name));
        Assert.assertTrue(str.contains(Multi.toString()));
        Assert.assertTrue(str.contains(Topic));
    }

    @Test
    public void VerifyStringConvertDirection() {
        // Create signals
        SignalInfo signal_ff = new SignalInfo(0, "", "", 0f, "", false, false);
        SignalInfo signal_tf = new SignalInfo(0, "", "", 0f, "", true, false);
        SignalInfo signal_ft = new SignalInfo(0, "", "", 0f, "", false, true);
        SignalInfo signal_tt = new SignalInfo(0, "", "", 0f, "", true, true);

        // Convert to strings
        String str_ff = signal_ff.toString();
        String str_tf = signal_tf.toString();
        String str_ft = signal_ft.toString();
        String str_tt = signal_tt.toString();

        // Verify strings
        Assert.assertFalse(str_ff.contains("to"));
        Assert.assertFalse(str_ff.contains("from"));

        Assert.assertTrue(str_tf.contains("to"));
        Assert.assertFalse(str_tf.contains("from"));

        Assert.assertFalse(str_ft.contains("to"));
        Assert.assertTrue(str_ft.contains("from"));

        Assert.assertTrue(str_tt.contains("to"));
        Assert.assertTrue(str_tt.contains("from"));
    }
}
