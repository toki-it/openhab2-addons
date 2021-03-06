/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SwitchBox} class defines a logic for SwitchBox device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class SwitchBox extends BaseDevice {
    public static final String SET_URL = "/s/";
    public static final String STATE_URL = "/api/relay/state";
    private Logger logger = LoggerFactory.getLogger(SwitchBox.class);

    public SwitchBox(String ipAddress) {
        super(BleboxBindingConstants.SWITCHBOX, ipAddress);
    }

    public SwitchBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse implements BaseResponse {

        public Relay[] relays;

        @Override
        public String getRootElement() {
            return null;
        }

    }

    public class Relay implements BaseResponse {
        public int relay;
        public int state;
        public int stateAfterRestart;

        @Override
        public String getRootElement() {
            return null;
        }
    }

    public void setSwitchState(OnOffType onOff) {
        String url = SET_URL + (onOff.equals(OnOffType.ON) ? "1" : "0");
        try {
            getJson(url, StateResponse.class, null);
        } catch (Exception e) {
            logger.warn("setSwitchState(): Error: {}", e);
        }
    }

    public OnOffType getSwitchState(int switchIndex) {
        Relay[] response = null;

        try {
            response = getJsonArray(STATE_URL, Relay[].class, null);
        } catch (Exception e) {
            logger.warn("getSwitchState(): Error: {}", e);
        }

        if (response != null && response.length > switchIndex) {
            return response[switchIndex].state > 0 ? OnOffType.ON : OnOffType.OFF;
        }

        return null;
    }
}
