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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import android.swedspot.scs.data.DataType;

/**
 * Loader for object defining the conversion from a MQTT signal to a SDP signal,
 * or the opposite.
 *
 */
public class LoadConversionConfig {

    /**
     * Logger.
     */
    private Logger logger = null;

    /**
     * Signal conversion information.
     */
    public ArrayList<SignalInfo> approvedSignals = null;

    /**
     * Signal conversion information. The AGA ID is the key.
     */
    public HashMap<Integer, SignalInfo> signalFromAgaId = null;

    /**
     * Signal conversion information. The MQTT topic is the key.
     */
    public HashMap<String, SignalInfo> signalFromTopic = null;


    /**
     * Load a .JSON file describing the conversion from MQTT to SDP
     * (and opposite).
     * @param fileName name of the .JSON file
     * @throws JsonSyntaxException TODO
     * @throws IOException TODO
     */
    public final void loadConversionConfig(final String fileName) throws
                      JsonSyntaxException, IOException {
        ConversionInfo conversionInfo = null;
        Gson jsonParser = new Gson();

        final Float agaMultiplierDefault = 1.0f;
        final Float agaMultiplierMin = 0.0000001f; // Avoid divide by zero
        final int agaIdMax = 0xFFFF;
        final String[] settingsAgaTypesAllowed = new String[] {
                DataType.UINT8.name(),
                DataType.SHORT.name(),
                DataType.INTEGER.name(),
                DataType.FLOAT.name(),
                DataType.DOUBLE.name() };

        // Read configuration file
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        conversionInfo = jsonParser.fromJson(reader, ConversionInfo.class);

        // Initialize array to store approved signals
        approvedSignals = new ArrayList<SignalInfo>();

        for (SignalInfo signal : conversionInfo.signals) {
            // Verify AGA id
            if (signal.agaId <= 0 || signal.agaId > agaIdMax) {
                //logger.warn("Wrong agaId for signal " + signal);
                continue;
            }
            // Verify AGA Type
            if (!(Arrays.asList(settingsAgaTypesAllowed).
                    contains(signal.agaType))) {
                //logger.warn("Wrong agaType for signal " + signal +
                //". Not implemented.");
                continue;
            }
            // Give AGA multiplier a value if none was given in config file
            if (signal.agaMultiplier == null) {
                signal.agaMultiplier = agaMultiplierDefault;
            }
            // Verify that AGA multiplier is larger than MIN_
            if (signal.agaMultiplier < agaMultiplierMin) {
            //  logger.warn("Wrong agaMultiplier for signal " + signal);
                continue;
            }
            // Verify that MQTT topic is given
            if (signal.mqttTopic == null) {
            //  logger.warn("Wrong mqttTopic for signal " + signal);
                continue;
            }

            // Signal is approved
            approvedSignals.add(signal);

        }

        // Create hash tables to ease signal lookup
        signalFromAgaId = new HashMap<Integer, SignalInfo>();
        signalFromTopic = new HashMap<String, SignalInfo>();
        for (SignalInfo signal : approvedSignals) {
            signalFromAgaId.put(signal.agaId, signal);
            signalFromTopic.put(signal.mqttTopic, signal);
        }

        // Check if topic or agaId has been duplicated
        if (approvedSignals.size() != signalFromAgaId.size()
            || approvedSignals.size() != signalFromTopic.size()) {
        //  logger.error("mqttTopic and/or AGAid duplication found in "
        //+ "conversion file");
            JsonSyntaxException e =
                new JsonSyntaxException("Duplicate data found");
            throw e;
        }
    }

    /**
     * @return an object holding the approved signals.
     */
    public final ArrayList<SignalInfo> getApprovedSignals() {
        return approvedSignals;
    }

    /**
     * Set the approved signals.
     * @param approvedSignals An object holding signal conversion information,
     *         where the AGA ID is the key.
     */
    public final void setApprovedSignals(
                      final ArrayList<SignalInfo> approvedSignals) {
        this.approvedSignals = approvedSignals;
    }

    /**
     * @return an object holding signal conversion information,
     *         where the AGA ID is the key.
     */
    public final HashMap<Integer, SignalInfo> getSignalFromAgaId() {
        return signalFromAgaId;
    }

    /**
     * Set the signal conversion info.
     * @param signalFromAgaId An object holding signal conversion information,
     *         where the AGA ID is the key.
     */
    public final void setSignalFromAgaId(
                final HashMap<Integer, SignalInfo> signalFromAgaId) {
        this.signalFromAgaId = signalFromAgaId;
    }

    /**
     * @return an object holding signal conversion information,
     *         where the MQTT topic is the key.
     */
    public final HashMap<String, SignalInfo> getSignalFromTopic() {
        return signalFromTopic;
    }

    /**
     * Set the signal conversion info.
     * @param signalFromTopic An object holding signal conversion information,
     *         where the MQTT topic is the key.
     */
    public final void setSignalFromTopic(
                final HashMap<String, SignalInfo> signalFromTopic) {
        this.signalFromTopic = signalFromTopic;
    }

}
