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

/**
 * Object defining the conversion from a MQTT signal to a SDP signal,
 * or the opposite.
 *
 * <p>The field names are the ones used in the JSON configuration
 * files.</p>
 *
 */
public class SignalInfo {

    /**
     * Signal number ID (for the SDP signal) as defined by AGA.
     */
    public int agaId;

    /**
     * Aga signal type. Can for example be "UINT8", "SHORT", "INTEGER",
     * "FLOAT", "DOUBLE".
     */
    public String agaType;

    /**
     * Aga signal name. This is a documentation feature, and is at
     * present not used by this software.
     */
    public String agaName;

    /**
     * A conversion multiplier used for signals originating from AGA
     * (in SDP format).
     * Signals originating from MQTT are instead divided by that field.
     * Should be larger than zero (strictly positive). Defaults to 1.0f.
     */
    public Float agaMultiplier;

    /**
     * Topic for the MQTT message.
     */
    public String mqttTopic;

    /**
     * Wheter MQTT-to-SDP conversion is allowed.
     */
    public boolean toAga;

    /**
     * Wheter SDP-to-MQTT conversion is allowed.
     */
    public boolean fromAga;

    /**
     * Constructor.
     * @param agaId         Signal number ID as defined by AGA.
     * @param agaType       Aga signal type.
     * @param agaName       Aga signal name.
     * @param agaMultiplier a conversion multiplier.
     * @param mqttTopic     topic for the MQTT message.
     * @param toAga         wheter MQTT-to-SDP conversion is allowed.
     * @param fromAga       wheter SDP-to-MQTT conversion is allowed.
     */
    public SignalInfo(final int agaId, final String agaType,
                      final String agaName, final Float agaMultiplier,
                      final String mqttTopic,
                      final boolean toAga, final boolean fromAga) {
        this.agaId = agaId;
        this.agaType = agaType;
        this.agaName = agaName;
        this.agaMultiplier = agaMultiplier;
        this.mqttTopic = mqttTopic;
        this.toAga = toAga;
        this.fromAga = fromAga;
    }

    @Override
    public final String toString() {
        String directionText = "none";
        if (toAga && fromAga) {
            directionText = "to+from AGA";
        } else if (toAga) {
            directionText = "to AGA";
        } else if (fromAga) {
            directionText = "from AGA";
        }
        return ("AgaID: " + agaId + " (0x" + Integer.toHexString(agaId) + ", "
                + agaName + ", " + agaType + ", " + agaMultiplier
                + ") Topic: " + mqttTopic + " Direction: " + directionText);
    }
}
