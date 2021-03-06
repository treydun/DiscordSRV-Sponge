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
package com.discordsrv.sponge.unit.chat;

import org.spongepowered.api.text.channel.MessageChannel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

/**
 * SpongeChat implementation for the "global" channel.
 */
@ParametersAreNonnullByDefault
public class SpongeGlobalChat extends SpongeChat {

    /**
     * The main constructor.
     *
     * @param messageChannel
     *         The message channel representing the "global" channel
     */
    public SpongeGlobalChat(final MessageChannel messageChannel) {
        super(messageChannel);
    }

    /**
     * Fetches the name of this named instance.
     *
     * @param callback
     *         The callback for this getter.
     */
    @Override
    public void getName(Consumer<CharSequence> callback) {
        callback.accept("Global");
    }

    /**
     * Fetches the identifier for this uniquely identifiable type.
     *
     * @param callback
     *         The callback of this comparison.
     */
    @Override
    public void getUniqueIdentifier(Consumer<String> callback) {
        callback.accept("global");
    }
}
