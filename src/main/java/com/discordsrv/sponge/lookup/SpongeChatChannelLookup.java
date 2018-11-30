/*
 * DiscordSRV-Sponge: Sponge platform support plugin or the DiscordSRV project
 * Copyright (C) 2018 DiscordSRV
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.discordsrv.sponge.lookup;

import com.discordsrv.core.api.channel.Chat;
import com.discordsrv.core.channel.MalleableChatChannelLookup;
import com.discordsrv.sponge.SpongeContext;
import com.google.common.util.concurrent.FutureCallback;
import net.dv8tion.jda.core.entities.Channel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class SpongeChatChannelLookup extends MalleableChatChannelLookup<SpongeContext> {

    /**
     * Main constructor for the MalleableChatChannelLookup type.
     *
     * @param context
     *         The context in which this lookup is performing.
     */
    public SpongeChatChannelLookup(SpongeContext context) {
        super(context);
    }

    @Override
    public void getKnownChats(FutureCallback<Stream<Chat>> callback) {
    }

    @Override
    public void getKnownChannels(FutureCallback<Stream<Channel>> callback) {
    }
}
