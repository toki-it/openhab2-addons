/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blebox.BleboxBindingConstants;
import org.openhab.binding.blebox.internal.BleboxDeviceConfiguration;
import org.openhab.binding.blebox.internal.devices.GateBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GateBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class GateBoxHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(GateBoxHandler.class);
    private GateBox gateBox;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (gateBox != null) {
                GateBox.StateResponse state = gateBox.getStatus();

                if (state != null) {
                    updateState(BleboxBindingConstants.CHANNEL_POSITION, state.getPosition());

                    if (getThing().getStatus() == ThingStatus.OFFLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }
    };
    private ScheduledFuture<?> pollingJob;

    public GateBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case BleboxBindingConstants.CHANNEL_POSITION:

                if (command instanceof UpDownType) {
                    UpDownType upDownCommand = (UpDownType) command;

                    if (upDownCommand == UpDownType.UP) {
                        // blebox uses inverted values - 100% = wide open
                        gateBox.setPosition(PercentType.HUNDRED);
                    } else if (upDownCommand == UpDownType.DOWN) {
                        gateBox.setPosition(PercentType.ZERO);
                    }
                }
                break;
        }
    }

    @Override
    public void initialize() {
        BleboxDeviceConfiguration config = getConfigAs(BleboxDeviceConfiguration.class);

        gateBox = new GateBox(config.ip);
        updateStatus(ThingStatus.ONLINE);

        int pollingInterval = (config.pollingInterval != null) ? config.pollingInterval.intValue()
                : BleboxDeviceConfiguration.DEFAULT_POLL_INTERVAL;

        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, pollingInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }
}
