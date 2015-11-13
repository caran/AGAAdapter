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

import static org.hamcrest.CoreMatchers.containsString;     

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Integration test for the AGA adapter.
 */
public class AGAAdapterIT {
    private static class AGAAdapterWrapper implements Runnable {
        public void run() {
            String[] arguments = new String[] {"./src/test/resources/conversion_ok.json",
                                               "./src/test/resources/integrationtest.properties"};
            AGAAdapter.main(arguments);
        }
    }

    /**
     * Send MQTT messages, and verify that converted SDP messages appear. Similarly, verify the 
     * SDP-to-MQTT conversion.
     */
    @Test
    public void injectMqtt() throws InterruptedException, IOException {
        // Publish MQTT message to make sure the MQTT broker is up
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("mosquitto_pub -t /test/mqtt -m 123");
        Assert.assertEquals("Failed to run mosquitto_pub. IS MOSQUITTO BROKER RUNNING?", 0, pr.waitFor());

        // Start InfotainmentMock in own thread with output redirected
        ByteArrayOutputStream infoBuffer = new ByteArrayOutputStream(8192);
        PrintStream infoOutStream = new PrintStream(infoBuffer);
        AGAInfotainmentMock infoMock = new AGAInfotainmentMock();
        infoMock.outputStream = infoOutStream;
        Thread infoMockThread = new Thread(infoMock);
        infoMockThread.start();

        // Start AGAAdapter in own thread
        Thread AGAAdapterThread = new Thread(new AGAAdapterWrapper());
        AGAAdapterThread.start();

        // Listen to all MQTT messages
        Process mosquitto_subscribe_process = rt.exec("mosquitto_sub -t command/canadapter/# -v ");


        ///////////////// Test MQTT to SDP conversion ////////////////////////////
        //////////////////////////////////////////////////////////////////////////

        // Send MQTT messages
        System.out.println("Waiting 5 second for components to connect");
        Thread.sleep(5000);

        System.out.println("Sending MQTT messages");

        // Float
        pr = rt.exec("mosquitto_pub -t data/canadapter/enginespeed -m 8514");
        Assert.assertEquals(0, pr.waitFor());

        pr = rt.exec("mosquitto_pub -t data/canadapter/vehiclespeed -m 10.01");
        Assert.assertEquals(0, pr.waitFor());

        // Uint8
        pr = rt.exec("mosquitto_pub -t data/canadapter/direction -m 157");
        Assert.assertEquals(0, pr.waitFor());

        // Short
        pr = rt.exec("mosquitto_pub -t data/canadapter/enginetemperature -m 210");
        Assert.assertEquals(0, pr.waitFor());

        // Integer
        pr = rt.exec("mosquitto_pub -t data/canadapter/integertopic -m 123");
        Assert.assertEquals(0, pr.waitFor());

        // Double
        pr = rt.exec("mosquitto_pub -t data/canadapter/doubletopic -m 654987");
        Assert.assertEquals(0, pr.waitFor());

        // Send bad messages
        pr = rt.exec("mosquitto_pub -t data/canadapter/enginetemperature -m tfg13123h");
        Assert.assertEquals(0, pr.waitFor());

        // Unknown topic
        pr = rt.exec("mosquitto_pub -t unknown/topic -m 123");
        Assert.assertEquals(0, pr.waitFor());

        // Print output from infotainmentMock
        String infotainmentOutput = infoBuffer.toString();
        System.out.println("<---------- Output from infotainmentMock ---------->");
        System.out.println(infotainmentOutput);
        System.out.println("<----------             END              ---------->");

        // Verify response in infotainmentMock
        Assert.assertThat(infotainmentOutput, containsString("Received signal ID: 262"));
        Assert.assertThat(infotainmentOutput, containsString("Float: 8514.0"));
        Assert.assertThat(infotainmentOutput, containsString("Received signal ID: 320"));
        Assert.assertThat(infotainmentOutput, containsString("Float: 36.0"));
        Assert.assertThat(infotainmentOutput, containsString("Received signal ID: 290"));
        Assert.assertThat(infotainmentOutput, containsString("UInt8: 157"));
        Assert.assertThat(infotainmentOutput, containsString("Received signal ID: 315"));
        Assert.assertThat(infotainmentOutput, containsString("Short: 210"));
        Assert.assertThat(infotainmentOutput, containsString("Received signal ID: 1000"));
        Assert.assertThat(infotainmentOutput, containsString("Integer: 123"));
        Assert.assertThat(infotainmentOutput, containsString("Received signal ID: 1001"));
        Assert.assertThat(infotainmentOutput, containsString("Double: 654987.0"));


        ///////////////// Test SDP to MQTT conversion ////////////////////////////
        //////////////////////////////////////////////////////////////////////////

        // ----- SDP Signals -----
        infoMock.sendInt(11, 1000);
        infoMock.sendFloat(2001, (float) 652.25);
        infoMock.sendUint8(2002, 205);
        infoMock.sendShort(2003, (short) 147);
        infoMock.sendDouble(2004, 6543.1);

        // Define the expected output
        String correctOutput[] = new String[6];
        correctOutput[0] = "command/canadapter/testsignal1 12000";
        correctOutput[1] = "command/canadapter/testsignal2 652.25";
        correctOutput[2] = "command/canadapter/testsignal3 205";
        correctOutput[3] = "command/canadapter/testsignal4 147";
        correctOutput[4] = "command/canadapter/testsignal5 6543.1";
        correctOutput[5] = null;

        // Read stdOut from mosquitto_sub
        System.out.println("");
        System.out.println("Waiting for MQTT messages (will hang if there are too few messages):");
        System.out.println("<----------  Output from mosquitto_sub   ---------->");
        BufferedReader mosquittoSubstdInput =
                new BufferedReader(new InputStreamReader(mosquitto_subscribe_process.getInputStream()));
        String s = null;
        int i = 0;
        while ((s = mosquittoSubstdInput.readLine()) != null) {
            Assert.assertTrue(i < correctOutput.length);
            Assert.assertEquals(correctOutput[i++], s);
            System.out.println(s);
            if (i>=5) {
                break;
            }
        }
        mosquitto_subscribe_process.destroy();
        
        /*
        // Read stdErr from mosquitto_sub
        BufferedReader mosquittoSubstdError =
                new BufferedReader(new InputStreamReader(mosquitto_subscribe_process.getErrorStream()));
        int errors = 0;

        while ((s = mosquittoSubstdError.readLine()) != null) {
            System.out.println("[ERROR] " + s);
            errors++;
        }
        Assert.assertTrue(errors == 0);
        */
        System.out.println("<----------             END              ---------->");
    }

    // TODO: Disconnect MQTT

    // TODO: Disconnect SDP

}
