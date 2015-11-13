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
import java.util.Arrays;

import android.swedspot.scs.data.SCSBoolean;
import android.swedspot.scs.data.SCSDouble;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSInteger;
import android.swedspot.scs.data.SCSLong;
import android.swedspot.scs.data.SCSShort;
import android.swedspot.scs.data.Uint16;
import android.swedspot.scs.data.Uint32;
import android.swedspot.scs.data.Uint8;
import android.swedspot.sdp.ConnectionStatus;
import android.swedspot.sdp.SDPFactory;
import android.swedspot.sdp.SubscriptionStatus;
import android.swedspot.sdp.observer.SDPConnectionListener;
import android.swedspot.sdp.observer.SDPDataListener;
import android.swedspot.sdp.observer.SDPGatewayNode;
import android.swedspot.sdp.observer.SDPNode;
import android.swedspot.sdp.observer.SDPStatusListener;
import android.swedspot.sdp.routing.SDPNodeEthAddress;

/**
 * Base class for SDP communication mock objects.
 */
public class AGAMockBase {

    private SDPNode node;
    private PrintStream outputStream;

    /**
     * Constructor for creating a base SDP communication mock object. 
     *
     * It can act as a server (listening to incoming IP connections) or
     * as a client (connects to a server).
     *
     * @param hostname IP number or host name of the SDP node
     * @param portnumber IP port number of the SDP node
     * @param isSDPServer Whether it should act as a SDP server. Otherwise as a SDP client.
     * @param out Which output stream should be used for program output.
     */
    public AGAMockBase(String hostname, int portnumber, Boolean isSDPServer, PrintStream out){
        this.outputStream = out;
        initialize(hostname, portnumber, isSDPServer);
    }

    /**
     * Constructor for creating a base SDP communication mock object. 
     *
     * It can act as a server (listening to incoming IP connections) or
     * as a client (connects to a server).
     *
     * @param hostname IP number or host name of the SDP node
     * @param portnumber IP port number of the SDP node
     * @param isSDPServer Whether it should act as a SDP server. Otherwise as a SDP client.
     */
    public AGAMockBase(String hostname, int portnumber, Boolean isSDPServer){
        this.outputStream = System.out;
        initialize(hostname, portnumber, isSDPServer);
    }
    
    /**
     * Initialize the SDP communication. Set up handlers for incoming SDP data
     * and for changes in connection and subscription status.
     */
    private void initialize(String hostname, int portnumber, Boolean isSDPServer) {
        // Set up a SDP node
        node = SDPFactory.createNodeInstance();
        node.addDataListener(new SDPDataListener() {

            // Show incoming SDP data
            @Override
            public void receive(int dataId, byte[] data) {
                outputStream.println("Received signal ID: " + dataId + ", Data: " + Arrays.toString(data)
                    + " (" + data.length + " bytes)");
                if (data.length == 1) {
                    outputStream.println("    Boolean: " + new SCSBoolean(data).getBooleanValue());
                    outputStream.println("    UInt8: " + new Uint8(data).getIntValue());
                 } else if (data.length == 2) {
                    outputStream.println("    Short: " + new SCSShort(data).getShortValue());
                    outputStream.println("    UInt16: " + new Uint16(data).getIntValue());
                } else if (data.length == 4) {
                    outputStream.println("    Integer: " + new SCSInteger(data).getIntValue());
                    outputStream.println("    UInt32: " + new Uint32(data).getIntValue());
                    outputStream.println("    Float: " + new SCSFloat(data).getFloatValue());
                } else if (data.length == 8) {
                    outputStream.println("    Long: " + new SCSLong(data).getLongValue());
                    outputStream.println("    Double: " + new SCSDouble(data).getDoubleValue());
                }
            }

            @Override
            public byte[] request(int dataId) {
                return null;
            }
        });
        node.addStatusListener(new SDPStatusListener() {

            // Handle subscription status changes
            @Override
            public void statusChanged(int dataId, SubscriptionStatus status) {
                outputStream.println("    ---> Subscription status changed for signal ID: " + dataId +
                                   ". Now: " + status);
            }
        });

        // Use a server or client (within the SDP node) that talks TCP/IP.
        // There are other communication protocols available, for example CAN.
        SDPGatewayNode tcp = null;
        if (isSDPServer == true) {
            tcp = SDPFactory.createGatewayServerInstance();
            outputStream.println("Setting up a SDP server. Listening on port: " + portnumber +
                               " (on host " + hostname + ")");
        } else {
            tcp = SDPFactory.createGatewayClientInstance();
            outputStream.println("Connecting to SDP server: " + hostname + " on port: " + portnumber);
        }
        tcp.setConnectionListener(new SDPConnectionListener() {

            // Handle connection changes
            @Override
            public void connectionStatusChanged(ConnectionStatus connectionStatus) {
                outputStream.println("  ===> Connection status changed: " + connectionStatus);
            }
        });
        tcp.init(new SDPNodeEthAddress(hostname, portnumber), node);
        tcp.start();
    }

    ///////////////  Public methods for 'subscribe' and 'provide' information ///////////////////    
    /**
     * Inform the other SDP party that an AGA should be subscribed to.
     * @param dataId One AGA signal ID number that this would like to subscribe to.
     */
    public void subscribe(int dataId){
        outputStream.println("Subscribing to signal ID: " + dataId);
        node.subscribe(dataId);
    }

    /**
     * Inform the other SDP party that an AGA signal is provided.
     * @param dataId One AGA signal ID number that this is providing.
     */
    public void provide(int dataId){
        outputStream.println("Announcing 'provide' information for signal ID: " + dataId);
        node.provide(dataId);
    }

    /**
     * Print out which AGA signals that are subscribed to,
     * and which are provided.
     */
    public void printInfo(){
        outputStream.println("  Provides these signals: " + Arrays.toString(node.provides()));
        outputStream.println("  Subscribes to these signals: " + Arrays.toString(node.subscribes()));
    }

    
    ///////////////////////  Public methods for sending signals ////////////////////////////////
    /**
     * Send a SDP message with a single precision float.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendSignalFloat(int dataId, float signalValue){
        outputStream.println("Sending float signal. ID: " + dataId + " Value: " + signalValue);
        node.send(dataId, (new SCSFloat(signalValue)).getData());
    }

    /**
     * Send a SDP message with a double precision float.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendSignalDouble(int dataId, double signalValue){
        outputStream.println("Sending double signal. ID: " + dataId + " Value: " + signalValue);
        node.send(dataId, (new SCSDouble(signalValue)).getData());
    }

    /**
     * Send a SDP message with an 8-bit unsigned integer.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendSignalUint8(int dataId, int signalValue){
        outputStream.println("Sending uint8 signal. ID: " + dataId + " Value: " + signalValue);
        node.send(dataId, (new Uint8(signalValue)).getData());
    }

    /**
     * Send a SDP message with a short integer.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendSignalShort(int dataId, short signalValue){
        outputStream.println("Sending short integer signal. ID: " + dataId + " Value: " + signalValue);
        node.send(dataId, (new SCSShort(signalValue)).getData());
    }

    /**
     * Send a SDP message with an integer.
     * @param dataId AGA signal ID number
     * @param signalValue value
     */
    public void sendSignalInt(int dataId, int signalValue){
        outputStream.println("Sending integer signal. ID: " + dataId + " Value: " + signalValue);
        node.send(dataId, (new SCSInteger(signalValue)).getData());
    }
}
