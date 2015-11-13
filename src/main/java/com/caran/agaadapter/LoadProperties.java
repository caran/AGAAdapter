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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

 /**
 * Loader for object defining host names and ports for
 * MQTT broker and SDP server.
 *
 */
public class LoadProperties {

    /**
     * Host name for AGA infotaiment system.
     */
    public String agaHostname = null;

    /**
     * Port number for AGA infotaiment system.
     */
    public int agaPortnumber = 0;

    /**
     * Host name for MQTT broker.
     */
    public String mqttHostname = null;

    /**
     * Port number for MQTT broker.
     */
    public int mqttPortnumber = 0;

    /**
     * Load a .properties file describing host names and ports for
     * MQTT broker and SDP server.
     * @param fileName name of the .properties file
     * @throws NumberFormatException TODO
     * @throws IOException TODO
     */
    public final void loadProperties(final String fileName)
                      throws NumberFormatException, IOException {
        final String settingsKeyAgaHost = "AGA_host";
        final String settingsKeyAgaPort = "AGA_port";
        final String settingsKeyMqttHost = "MQTT_host";
        final String settingsKeyMqttPort = "MQTT_port";

        Properties settings = new Properties();
        InputStream propertyStream = null;
        propertyStream = new FileInputStream(fileName);
        settings.load(propertyStream);

        try {
            agaHostname = settings.getProperty(settingsKeyAgaHost);
            agaPortnumber = Integer.parseInt(
                                settings.getProperty(settingsKeyAgaPort));
            mqttHostname = settings.getProperty(settingsKeyMqttHost);
            mqttPortnumber = Integer.parseInt(
                                settings.getProperty(settingsKeyMqttPort));
        } finally {
            propertyStream.close();
        }
    }
}
