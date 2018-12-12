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

package com.discordsrv.sponge.listener;

import com.discordsrv.core.conf.annotation.Configured;
import com.discordsrv.core.conf.annotation.Val;
import com.discordsrv.sponge.DSRVSponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.List;

/**
 * Generic channel message listener.
 */
public class ChannelMessageListener {

    private final DSRVSponge plugin;
    private final boolean enabled, blacklist;
    private final List<String> events;

    /**
     * Configured constructor.
     *
     * @param plugin
     *         DSRVSponge plugin
     * @param enabled
     *         enabled config option
     * @param blacklist
     *         blacklist config option
     * @param events
     *         events config option
     */
    @Configured
    public ChannelMessageListener(@Val("plugin") DSRVSponge plugin, @Val("enabled") boolean enabled,
                                  @Val("blacklist") boolean blacklist, @Val("events") List<String> events) {
        this.plugin = plugin;
        this.enabled = enabled;
        this.blacklist = blacklist;
        this.events = events;
    }

    /**
     * MessageChannelEvent listener.
     *
     * @param event
     *         MessageChannelEvent
     */
    @Listener(order = Order.POST)
    public void onMessage(MessageChannelEvent event) {
        if (!enabled
            || events.stream().anyMatch(eventClass -> event.getClass().getName().startsWith(eventClass)) == blacklist) {
            return;
        }
        plugin.sendMessage(event, null);
    }
}
