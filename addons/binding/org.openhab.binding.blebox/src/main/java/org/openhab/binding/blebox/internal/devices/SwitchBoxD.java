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
 * The {@link SwitchBoxD} class defines a logic for SwitchBoxD device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class SwitchBoxD extends SwitchBox {
    public static final String RESPONSE_ROOT = "relays";
    private Logger logger = LoggerFactory.getLogger(SwitchBoxD.class);

    public SwitchBoxD(String ipAddress) {
        super(BleboxBindingConstants.SWITCHBOXD, ipAddress);
    }

    public void setSwitchState(int switchIndex, OnOffType onOff) {
        String url = SET_URL + switchIndex + "/" + (onOff.equals(OnOffType.ON) ? "1" : "0");
        try {
            getJson(url, StateResponse.class, RESPONSE_ROOT);
        } catch (Exception e) {
            logger.warn("setSwitchState(): Error: {}", e);
        }
    }

    public OnOffType[] getSwitchesState() {
        Relay[] response = null;

        try {
            response = getJsonArray(STATE_URL, Relay[].class, RESPONSE_ROOT);
        } catch (Exception e) {
            logger.warn("getSwitchesState(): Error: {}", e);
        }

        OnOffType[] result = new OnOffType[2];

        if (response != null && response.length == 2) {
            result[0] = response[0].state > 0 ? OnOffType.ON : OnOffType.OFF;
            result[1] = response[1].state > 0 ? OnOffType.ON : OnOffType.OFF;

            return result;
        }

        return null;
    }

}
