/*

Copyright (c) 2015, Semcon Sweden AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 3. Neither the name of the Semcon Sweden AB nor the names of its contributors
    may be used to endorse or promote products derived from this software
    without specific prior written permission.

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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import android.swedspot.scs.data.DataType;
import android.swedspot.scs.data.SCSDouble;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSInteger;
import android.swedspot.scs.data.SCSShort;
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
 * AGA adapter - A MQTT to SDP adapter for the Automotive Grade Android
 * (AGA) framework.
 */
public class AGAAdapter implements MqttCallback {

    //////////////////// Settings ////////////////////
    /**
     * MQTT quality of service level setting.
     */
    private static final int MQTT_QOS = 1;

    /**
     * Setting for the MQTT client name.
     */
    private static final String MQTT_CLIENT_ID = "AGAAdapter";

    /////////////// End of settings ///////////////////


    /**
     * MQTT client.
     */
    private MqttClient mqttc = null;

    /**
     * SDP client.
     */
    private SDPNode sdpNode = null;

    /**
     * Logger.
     */
    private Logger logger = null;

    /**
     * Object holding the .properties file info (like IP addresses etc).
     */
    private LoadProperties loadPropObj;

    /**
     * Object holding the signal conversion info.
     */
    private LoadConversionConfig loadConfigObj;

    /**
     * Constructor for creating an AGA adapter object.
     * @param loglevel Use "DEBUG", "INFO", "ERROR" etc.
     *                 See slf4j documentation.
     */
    public AGAAdapter(final String loglevel) {
        // Set up logger
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY,
                           loglevel);
        logger = LoggerFactory.getLogger(AGAAdapter.class);
        logger.debug("Running in verbose mode");
    }

    /**
     * Run the AGAAdapter from command line.
     *
     * <p>Command line arguments:</p>
     * <ul>
     *   <li> conversionsetup_filepath.json </li>
     *   <li> propertiesfilepath.properties Optional </li>
     *   <li> -v Verbose command line output </li>
     * </ul>
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final  String defaultPropertiesFilenameAndPath =
                     "src/main/resources/agaadapter.properties";
        final String commandVerbose = "-v";
        final int maxArgumentLength = 3;

        // Parse command line arguments
        if (args.length == 0) {
            System.out.println("\nUsage: ");
            System.out.println("AGAAdapter conversions.json "
                               + "[conf.properties] [-v]");
            System.out.println("If the properties filename not is given, "
                               + "it will use the default file "
                               + "'agaadapter.properties'");
            System.out.println("  -v    Run in verbose mode.");
            System.exit(1);
        }
        String conversionFilename = args[0];
        String propertiesFilename = defaultPropertiesFilenameAndPath;
        String loglevel = "INFO";

        if (args.length >= 2) {
            if (args[1].equals(commandVerbose)) {
                loglevel = "DEBUG";
            } else {
                propertiesFilename = args[1];
            }
        }
        if (args.length >= maxArgumentLength) {
            if (args[2].equals(commandVerbose)) {
                loglevel = "DEBUG";
            }
        }

        AGAAdapter adapter = new AGAAdapter(loglevel);
        adapter.logger.info("\n\nStarting AGA adaptor ...");
        adapter.logger.info("Using conversion setup file: "
                            + conversionFilename);
        adapter.logger.info("Using properties file: "
                            + propertiesFilename);

        // Read conversion settings JSON file
        try {
            LoadConversionConfig loadConfigObj = new LoadConversionConfig();
            loadConfigObj.loadConversionConfig(conversionFilename);
            adapter.setLoadConfigObj(loadConfigObj);
            adapter.logger.info("Read " + loadConfigObj.approvedSignals.size()
                                + " approved signals from file.");
        } catch (JsonSyntaxException e) {
            adapter.logger.error("\nThe JSON conversion file is invalid.");
            adapter.logger.error(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            adapter.logger.error("\nFailed to read JSON conversion file");
            adapter.logger.error(e.getMessage());
            System.exit(1);
        }

        // Read properties settings file
        try {
            LoadProperties loadPropObj = new LoadProperties();
            loadPropObj.loadProperties(propertiesFilename);
            adapter.setLoadPropObj(loadPropObj);
        } catch (IOException e) {
            adapter.logger.error("\nThe properties file in invalid.");
            adapter.logger.error(e.getMessage());
            System.exit(1);
        }

        // Set up MQTT client
        adapter.logger.info("Connecting to MQTT broker on host: "
                            + adapter.loadPropObj.mqttHostname + ", port: "
                            + adapter.loadPropObj.mqttPortnumber);
        try {
            adapter.connectMqtt();
        } catch (MqttException e) {
            adapter.logger.error("\nCould not connect to MQTT broker!");
            adapter.logger.error(e.getMessage());
            System.exit(1);
        }

        // Set up a SDP node, with a corresponding TCP/IP client
        adapter.logger.info("Connecting to AGA server on host: "
                            + adapter.loadPropObj.agaHostname
                            + ", port: " + adapter.loadPropObj.agaPortnumber);
        adapter.connectSdp();

        adapter.logger.debug("  Provides these AGA signals: "
                             + Arrays.toString(adapter.sdpNode.provides()));
        adapter.logger.debug("  Subscribes to these AGA signals: "
                             + Arrays.toString(adapter.sdpNode.subscribes()));
    }


    /**
     * Connect to the MQTT broker and subscribe to topics.
     * @throws MqttException TODO
     */
    public final void connectMqtt() throws MqttException {
        String mqttServerUri = String.format("tcp://%s:%d",
                                             loadPropObj.mqttHostname,
                                             loadPropObj.mqttPortnumber);

        mqttc = new MqttClient(mqttServerUri, MQTT_CLIENT_ID);

        MqttConnectOptions mqttConnectionOptions = new MqttConnectOptions();
        mqttConnectionOptions.setCleanSession(true);
        mqttc.setCallback(this);
        mqttc.connect();

        // Subscribe to MQTT signals
        for (SignalInfo signal: loadConfigObj.approvedSignals) {
            if (signal.toAga) {
                mqttc.subscribe(signal.mqttTopic);
                logger.debug("  Subscribing to MQTT topic: "
                             + signal.mqttTopic);
            }
        }
    }

    /**
     * Connect to the AGA infotainment system via SDP protocol.
     *
     * <p>Creates a SDP node. Sets up a listener that converts incoming SDP
     * messages to MQTT. Other listeners handle changes in connection status
     * or subscription status. Subscribes to SDP signals, and informs about
     * which SDP signals it is providing.</p>
     */
    public final void connectSdp() {
        sdpNode = SDPFactory.createNodeInstance();
        sdpNode.addDataListener(new SDPDataListener() {

            // Handle incoming SDP data
            @Override
            public void receive(final int dataId, final byte[] agaBytes) {
                logger.debug("Received AGA id " + dataId + " bytes: "
                             + Arrays.toString(agaBytes));

                // TODO Warning for not found signaldefinition
                SignalInfo signal = loadConfigObj.signalFromAgaId.get(dataId);
                String mqttPayload;
                try {
                    if (signal.agaType.equals(DataType.UINT8.name())) {
                        mqttPayload = Integer.toString(
                                    Math.round(signal.agaMultiplier
                                    * new Uint8(agaBytes).getIntValue()));

                    } else if (signal.agaType.equals(DataType.SHORT.name()))  {
                        mqttPayload = Integer.toString(
                            Math.round(signal.agaMultiplier
                            * (int) new SCSShort(agaBytes).getShortValue()));

                    } else if (signal.agaType.equals(DataType.INTEGER.name())) {
                        mqttPayload = Integer.toString(
                            Math.round(signal.agaMultiplier
                            * new SCSInteger(agaBytes).getIntValue()));

                    } else if (signal.agaType.equals(DataType.FLOAT.name())) {
                        mqttPayload = Float.toString(signal.agaMultiplier
                            * new SCSFloat(agaBytes).getFloatValue());

                    } else if (signal.agaType.equals(DataType.DOUBLE.name())) {
                        mqttPayload = Float.toString(signal.agaMultiplier
                            * (float) new SCSDouble(agaBytes).getDoubleValue());

                    } else {
                        logger.warn("Wrong agaType: " + signal.agaType
                                    + " for agaId" + dataId);
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("\n\nCould not parse data from AGA");
                    logger.warn(e.getMessage());
                    return;
                }
                logger.debug("  Sending MQTT message on topic: "
                             + signal.mqttTopic + " Payload: " + mqttPayload);
                try {
                    MqttMessage msg = new MqttMessage(mqttPayload.getBytes());
                    msg.setQos(MQTT_QOS);
                    mqttc.publish(signal.mqttTopic, msg);
                } catch (MqttPersistenceException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public final byte[] request(final int dataId) {
                return null;
            }
        });

        // Handle SDP subscription status changes
        sdpNode.addStatusListener(new SDPStatusListener() {
            @Override
            public void statusChanged(final int dataId,
                                      final SubscriptionStatus status) {
                logger.info("SDP subscription status for agaId: " + dataId
                            + ". Now: " + status);
            }
        });

        // Use a client (within the SDP node) that talks TCP/IP.
        // There are other types of clients that can talk CAN etc.
        SDPGatewayNode client = SDPFactory.createGatewayClientInstance();
        client.setConnectionListener(new SDPConnectionListener() {
            // Handle SDP connection changes
            @Override
            public void connectionStatusChanged(final ConnectionStatus
                                                connectionStatus) {
                logger.info("SDP connection status changed: "
                            + connectionStatus);
            }
        });
        client.init(new SDPNodeEthAddress(loadPropObj.agaHostname,
                                          loadPropObj.agaPortnumber),
                    sdpNode);
        client.start();

        // Register (provide/subscribe) AGA signals
        for (SignalInfo signal: loadConfigObj.approvedSignals) {
            if (signal.toAga) {
                sdpNode.provide(signal.agaId);
            }
            if (signal.fromAga) {
                sdpNode.subscribe(signal.agaId);
            }
        }
    }

    /////////////////////////  MQTT callback methods  //////////////////////////

    /**
     * MQTT callback for lost connections. Not implemented.
     */
    @Override
    public void connectionLost(final Throwable arg0) {
    }

    /**
     * MQTT callback for message delivered. Not implemented.
     */
    @Override
    public void deliveryComplete(final IMqttDeliveryToken arg0) {
    }

    /**
     * MQTT callback for incoming data. Sends out a SDP message.
     * @param topic MQTT topic
     * @param mqttMsg object describing a MQTT message
     * @throws Exception TODO
     */
    @Override
    public final void messageArrived(final String topic,
                                     final MqttMessage mqttMsg)
                            throws Exception {
        String payload = new String(mqttMsg.getPayload());
        logger.debug("Received MQTT message on topic: " + topic
                     + " Payload: " + payload);
        SignalInfo signal = loadConfigObj.signalFromTopic.get(topic);

        Float mqttFloatValue;
        Float agaFloatValue;
        byte[] agaBytes;
        try {
            mqttFloatValue = Float.parseFloat(payload);
        } catch  (Exception e) {
                logger.warn("Could not parse payload: " + payload
                            + " Topic: " + topic);
                return;
        }
        agaFloatValue = mqttFloatValue / signal.agaMultiplier;
        if (signal.agaType.equals(DataType.UINT8.name())) {
            agaBytes = new Uint8(Math.round(agaFloatValue)).getData();

        } else if (signal.agaType.equals(DataType.SHORT.name())) {
            agaBytes = new SCSShort(
                              (short) Math.round(agaFloatValue)).getData();

        } else if (signal.agaType.equals(DataType.INTEGER.name())) {
            agaBytes = new SCSInteger(Math.round(agaFloatValue)).getData();

        } else if (signal.agaType.equals(DataType.FLOAT.name())) {
            agaBytes = new SCSFloat(agaFloatValue).getData();

        } else if (signal.agaType.equals(DataType.DOUBLE.name())) {
            agaBytes = new SCSDouble((double) agaFloatValue).getData();

        } else {
            logger.warn("Wrong agaType: " + signal.agaType
                        + " for topic " + topic);
            return;
        }

        sdpNode.send(signal.agaId, agaBytes);
        logger.debug("  Sent signal ID " + signal.agaId
                     + " to AGA. Value: " + agaFloatValue
                     + " Type: " + signal.agaType
                     + " Bytes: " + Arrays.toString(agaBytes));
    }


    ///////////////////////////////  Getters and setters  /////////////////////

    /**
     * @return an object holding the IP addresses and port numbers.
     */
    public final LoadProperties getLoadPropObj() {
        return loadPropObj;
    }

    /**
     * Set the IP addresses and port numbers.
     * @param loadPropObj An object holding the IP addresses and port numbers.
     */
    public final void setLoadPropObj(final LoadProperties loadPropObj) {
        this.loadPropObj = loadPropObj;
    }

    /**
     * @return an object holding the signal conversion info.
     */
    public final LoadConversionConfig getLoadConfigObj() {
        return loadConfigObj;
    }

    /**
     * Set the signal conversion info.
     * @param loadConfigObj An object holding the signal conversion info
     */
    public final void setLoadConfigObj(
                     final LoadConversionConfig loadConfigObj) {
        this.loadConfigObj = loadConfigObj;
    }
}
