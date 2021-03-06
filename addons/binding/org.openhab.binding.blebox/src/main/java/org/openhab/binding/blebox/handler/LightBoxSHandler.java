/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.internal.devices.LightBoxS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightBoxSHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class LightBoxSHandler extends BaseHandler {
    private Logger logger = LoggerFactory.getLogger(LightBoxSHandler.class);
    private LightBoxS lightBoxS;

    public LightBoxSHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case BleboxBindingConstants.CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    lightBoxS.setWhiteBrightness((PercentType) command);
                } else if (command instanceof OnOffType) {
                    lightBoxS.setWhiteBrightness((OnOffType) command);
                }
                break;
        }
    }

    @Override
    void initializeDevice(String ipAddress) {
        lightBoxS = new LightBoxS(ipAddress);
    }

    @Override
    void updateDeviceStatus() {
        if (lightBoxS != null) {
            LightBoxS.StateResponse state = lightBoxS.getStatus();

            if (state != null) {
                updateState(BleboxBindingConstants.CHANNEL_BRIGHTNESS, state.getWhiteBrightness());

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }
}
