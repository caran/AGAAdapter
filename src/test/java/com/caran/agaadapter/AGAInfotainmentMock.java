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

import java.io.PrintStream;

/**
 * A mock object imitating an AGA infotainment system.
 *
 * <p>It receives and prints out SDP messages. It can also send SDP messages (scriptable).</p>
 *
 * <p>Note that which AGA ID numbers (SDP signals) that will be subscribed to is hard coded. Also the
 * scripting of the SDP sending is hardcoded. After changes, recompile using the maven buildscript.</p>
 *
 */
public class AGAInfotainmentMock implements Runnable {

    ///////////////////// Settings ////////////////////////////////
    
    private final static String SDP_IP_ADDRESS = "localhost";
    private final static int SDP_IP_PORT = 8251;

    // Signals to subscribe to and provide
    private final static int subscribe[] = {260, 262, 264, 282, 290, 294, 315, 320};
    private final static int provide[] = {11};
    
    ///////////////////// End of settings ////////////////////////////////

    private static AGAMockBase mock;
    public PrintStream outputStream;

    /**
     * Run the AGA infotainment mock from command line. 
     * 
     * <p>No command line arguments are accepted.</p>
     *
     * <p>It is in this method you do the SDP signal sending scripting.Recompile after changes.</p>
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n\nStarting InfotainmentMock AGA server ...");
        mock = new AGAMockBase(SDP_IP_ADDRESS, SDP_IP_PORT, true);

        initSignals(subscribe, provide);

        ///////////// Scripting of signal sending ///////////////////////////
        
        mock.printInfo();
        Thread.sleep(15000);
        mock.sendSignalInt(11, 7);
        
        ////////////////// End of scripting //////////////////////////////////
    }

    /**
     * Inform the other SDP party which AGA signals that are subscribed to,
     * and which are provided.
     * @param subscribe AGA signal ID numbers that should be subscribed to.
     * @param provide AGA signal ID numbers that this is providing.
     */
    private static void initSignals(int[] subscribe, int[] provide) {
        // Subscribe to signals
        for (int s : subscribe){
            mock.subscribe(s);
        }

        // Provide signals
        for (int p : provide){
            mock.provide(p);
        }
    }

    /**
     * Method used by the integration tests.
     */
    @Override
    public void run() {
        // This method is called from the integration test. Do not change.
        int it_subscribe[] = {294, 260, 262, 264, 282, 290, 315, 317, 320, 324, 325, 1000, 1001};
        int it_provide[] = {11, 2001, 2002, 2003, 2004};

        outputStream.println("Starting InfotainmentMock AGA server integration testing ...");
        mock = new AGAMockBase(SDP_IP_ADDRESS, SDP_IP_PORT, true, outputStream);
        initSignals(it_subscribe, it_provide);
    }

    /////////////  Public methods for sending signals ///////////////
    /**
     * Send a SDP message with a single precision float.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendFloat(int dataId, Float signalValue) {
        mock.sendSignalFloat(dataId, signalValue);
    }

    /**
     * Send a SDP message with a double precision float.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendDouble(int dataId, double signalValue){
        mock.sendSignalDouble(dataId, signalValue);
    }

    /**
     * Send a SDP message with an 8-bit unsigned integer.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendUint8(int dataId, int signalValue){
        mock.sendSignalUint8(dataId, signalValue);
    }

    /**
     * Send a SDP message with a short integer.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendShort(int dataId, short signalValue){
        mock.sendSignalShort(dataId, signalValue);
    }

    /**
     * Send a SDP message with an integer.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendInt(int dataId, int signalValue){
        mock.sendSignalInt(dataId, signalValue);
    }
}
