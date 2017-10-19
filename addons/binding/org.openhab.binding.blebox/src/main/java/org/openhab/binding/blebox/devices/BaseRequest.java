/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.devices;

/**
 * The {@link BaseRequest} class defines a base class for Blebox devices Http request
 *
 * @author Szymon Tokarski - Initial contribution
 */
public abstract class BaseRequest {
    public abstract String getRootElement();
}
