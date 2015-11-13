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
 * Object defining the conversion from MQTT signals to SDP signals,
 * or the opposite.
 *
 * <p>Contains a list of {@link SignalInfo}, describing the individual signal
 * conversions.</p>
 *
 * <p>The name of the list is the one used in JSON configuration files.</p>
 *
 */
public class ConversionInfo {

    /**
     * The list of {@link SignalInfo} objects describing the individual
     * signal conversions.
     *
     */
    public SignalInfo[] signals;

    /**
     * Constructor.
     */
    public ConversionInfo() {
    }

    /**
     * Constructor.
     * @param signals List of {@link SignalInfo} describing the individual
                      signal conversions.
     */
    public ConversionInfo(final SignalInfo[] signals) {
        this.signals = signals;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Signals:\n");

        // Check if there are any signals to process
        if (signals != null) {
            for (SignalInfo signal: signals) {
                builder.append("  " + signal + "\n");
            }
        } else {
            builder.append("  None\n");
        }
        return builder.toString();
    }
}
