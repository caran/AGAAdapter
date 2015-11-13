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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import com.google.gson.JsonSyntaxException;
import com.caran.agaadapter.LoadProperties;
import com.caran.agaadapter.LoadConversionConfig;

/**
 * Unit tests for the AGA adapter.
 */
public class AGAAdapterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();


    /////////////////////////// Test constructor /////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    @Test
    public void constructor() {
        @SuppressWarnings("unused")
        AGAAdapter adapter = new AGAAdapter("INFO");
        adapter = new AGAAdapter("DEBUG");
    }

    @Test
    public void mainUsage() {
        String[] arguments = new String[] {};
        exit.expectSystemExitWithStatus(1);
        AGAAdapter.main(arguments);
    }

    @Test
    public void mainBadConversionFile() {
        String[] arguments = new String[] {"badfilename.bad"};
        exit.expectSystemExitWithStatus(1);
        AGAAdapter.main(arguments);
    }

    @Test
    public void mainBadConvJson() {
        String[] arguments = new String[] {"./src/test/resources/conversion_badJson.json"};
        exit.expectSystemExitWithStatus(1);
        AGAAdapter.main(arguments);
    }

    @Test
    public void mainBadConvDebug() {
        String[] arguments = new String[] {"badfilename.bad", "-v"};
        exit.expectSystemExitWithStatus(1);
        AGAAdapter.main(arguments);
    }

    @Test
    public void mainBadConvPropDebug() {
        String[] arguments = new String[] {"name.bad", "bad.name", "-v"};
        exit.expectSystemExitWithStatus(1);
        AGAAdapter.main(arguments);
    }

    @Test
    public void mainBadConvPropUnknown() {
        String[] arguments = new String[] {"name.bad", "bad.name", "-a"};
        exit.expectSystemExitWithStatus(1);
        AGAAdapter.main(arguments);
    }

    //////////////// Test loadConversionConfig() /////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    @Test
    public void loadConvOk() throws JsonSyntaxException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        loadConfigObj.loadConversionConfig("./src/test/resources/conversion_ok.json");
        adapter.setLoadConfigObj(loadConfigObj);
        // The file contain 18 approved signals
        Assert.assertEquals(18, adapter.getLoadConfigObj().approvedSignals.size());
    }

    @Test
    public void loadConvBadFile() throws JsonSyntaxException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        adapter.setLoadConfigObj(loadConfigObj);

        thrown.expect(FileNotFoundException.class);
        adapter.getLoadConfigObj().loadConversionConfig("badFileName");
    }

    @Test
    public void loadConvBadJson() throws JsonSyntaxException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        adapter.setLoadConfigObj(loadConfigObj);

        thrown.expect(JsonSyntaxException.class);
        adapter.getLoadConfigObj().loadConversionConfig("./src/test/resources/conversion_badJson.json");
    }

    @Test
    public void loadConvBadData() throws JsonSyntaxException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        adapter.setLoadConfigObj(loadConfigObj);
        
        // This file shall contain 1 approved signal only
        adapter.getLoadConfigObj().loadConversionConfig("./src/test/resources/conversion_badData.json");
   //     adapter.loadConversionConfig("./src/test/resources/conversion_badData.json");
        Assert.assertEquals(1, adapter.getLoadConfigObj().approvedSignals.size());
    }

    @Test
    public void loadConvBadDuplicatedData() throws JsonSyntaxException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        adapter.setLoadConfigObj(loadConfigObj);
        // This file contains duplicate AGA id
        thrown.expect(JsonSyntaxException.class);
        thrown.expectMessage("Duplicate data found");
        adapter.getLoadConfigObj().loadConversionConfig("./src/test/resources/conversion_badDup.json");
      //  adapter.loadConversionConfig("./src/test/resources/conversion_badDup.json");
    }

    @Test
    public void loadConvBadDuplicatedData2() throws JsonSyntaxException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadConversionConfig loadConfigObj = new LoadConversionConfig();
        adapter.setLoadConfigObj(loadConfigObj);
        // This file contains duplicate MQTT topic
        thrown.expect(JsonSyntaxException.class);
        thrown.expectMessage("Duplicate data found");
        adapter.getLoadConfigObj().loadConversionConfig("./src/test/resources/conversion_badDup2.json");
    }

    ///////////////////// Test .loadProperties() /////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    @Test
    public void loadPropertiesOk() throws NumberFormatException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        LoadProperties loadPropObj = new LoadProperties();
        adapter.setLoadPropObj(loadPropObj);
        
        // No value are set by default
        Assert.assertEquals(adapter.getLoadPropObj().agaHostname, null);
        Assert.assertEquals(adapter.getLoadPropObj().agaPortnumber, 0);
        Assert.assertEquals(adapter.getLoadPropObj().mqttHostname, null);
        Assert.assertEquals(adapter.getLoadPropObj().mqttPortnumber, 0);
        
        adapter.getLoadPropObj().loadProperties("./src/test/resources/ok_reading.properties");

        // Make sure the right values have been read from the file
        Assert.assertEquals(adapter.getLoadPropObj().agaHostname, "hostname.aga");
        Assert.assertEquals(adapter.getLoadPropObj().agaPortnumber, 8251);
        Assert.assertEquals(adapter.getLoadPropObj().mqttHostname, "myhost.mqtt");
        Assert.assertEquals(adapter.getLoadPropObj().mqttPortnumber, 1883);
    }

    @Test
    public void loadPropertiesBadFile() throws NumberFormatException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        
        LoadProperties loadPropObj = new LoadProperties();
        adapter.setLoadPropObj(loadPropObj);
        
        thrown.expect(FileNotFoundException.class);
        adapter.getLoadPropObj().loadProperties("badFileName");
    }

    @Test
    public void loadPropertiesBadData() throws NumberFormatException, IOException {
        AGAAdapter adapter = new AGAAdapter("INFO");
        
        LoadProperties loadPropObj = new LoadProperties();
        adapter.setLoadPropObj(loadPropObj);
        thrown.expect(NumberFormatException.class);
        adapter.getLoadPropObj().loadProperties("./src/test/resources/badData.properties");
    }
}
