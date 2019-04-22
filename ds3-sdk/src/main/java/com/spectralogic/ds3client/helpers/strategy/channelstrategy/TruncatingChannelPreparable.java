/*
 * ******************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3client.helpers.strategy.channelstrategy;

import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * When initiating data movement in a put or get, the source or destination channel my need some sort of
 * preparation.  For instance, when initiating a get, the destination channel may need to be truncated
 * prior to moving data.
 */
public class TruncatingChannelPreparable implements ChannelPreparable {
    private final Set<String> channelNames = new HashSet<>();

    /**
     * Prepare a channel prior to moving data. This will truncate a chennal prior to opening it the
     * first time.
     * @param channelName The channel's identifier.
     * @param channelBuilder The instance of {@link com.spectralogic.ds3client.helpers.Ds3ClientHelpers.ObjectChannelBuilder}
     *                       used to create a channel.
     * @throws IOException
     */
    @Override
    public void prepareChannel(final String channelName, final Ds3ClientHelpers.ObjectChannelBuilder channelBuilder) throws IOException {
        if (channelNames.add(channelName)) {
            channelBuilder.buildChannel(channelName).truncate(0).close();
        }
    }
}
