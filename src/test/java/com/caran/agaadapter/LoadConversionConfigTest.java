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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.JsonSyntaxException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class LoadConversionConfigTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void okFile() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_ok.json");
    }

    @Test
    public void badFile() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        
        thrown.expect(FileNotFoundException.class);
        loadConfigObj.loadConversionConfig("badFileName");
    }

    @Test
    public void badJson() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        
        thrown.expect(JsonSyntaxException.class);
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_badJson.json");
    }

    @Test
    public void badData() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        
        thrown.expect(JsonSyntaxException.class);
        thrown.expectMessage("Duplicate data found");
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_badDup.json");
    }

    @Test
    public void badData2() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        
        thrown.expect(JsonSyntaxException.class);
        thrown.expectMessage("Duplicate data found");
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_badDup2.json");
    }

    @Test
    public void okFileGetSetApprovedSignals() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_ok.json");
        
        ArrayList<SignalInfo> appr = loadConfigObj.getApprovedSignals();
        loadConfigObj.setApprovedSignals(appr);
    }

    @Test
    public void okFileGetSetSignalsfromtopic() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_ok.json");
        
        HashMap<String, SignalInfo> signalFromTopic = loadConfigObj.getSignalFromTopic();
        loadConfigObj.setSignalFromTopic(signalFromTopic);
    }

    @Test
    public void okFileGetSetSignalsfromagaid() throws JsonSyntaxException, IOException{
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_ok.json");
        
        HashMap<Integer, SignalInfo> signalFromAgaId = loadConfigObj.getSignalFromAgaId();
        loadConfigObj.setSignalFromAgaId(signalFromAgaId);
    }
}
