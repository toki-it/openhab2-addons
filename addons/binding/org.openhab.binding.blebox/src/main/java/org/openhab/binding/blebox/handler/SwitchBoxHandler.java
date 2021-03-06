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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.internal.devices.SwitchBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class SwitchBoxHandler extends BaseHandler {
    private Logger logger = LoggerFactory.getLogger(SwitchBoxHandler.class);
    private SwitchBox switchBox;

    public SwitchBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(BleboxBindingConstants.CHANNEL_SWITCH0)) {
            if (command instanceof OnOffType) {
                switchBox.setSwitchState((OnOffType) command);
            }
        }
    }

    @Override
    void initializeDevice(String ipAddress) {
        switchBox = new SwitchBox(ipAddress);
    }

    @Override
    void updateDeviceStatus() {
        if (switchBox != null) {
            OnOffType switchState = switchBox.getSwitchState(0);

            if (switchState != null) {
                updateState(BleboxBindingConstants.CHANNEL_SWITCH0, switchState);

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }
}
