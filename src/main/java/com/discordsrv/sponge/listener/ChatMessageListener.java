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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

/**
 * Chat message listener.
 */
public class ChatMessageListener {

    private final DSRVSponge plugin;

    /**
     * Configured constructor.
     *
     * @param plugin DSRVSponge
     * @param enabled enabled config option
     */
    @Configured
    public ChatMessageListener(final @Val("plugin") DSRVSponge plugin, final @Val("enabled") boolean enabled) {
        this.plugin = plugin;
        if (enabled) {
            plugin.getContext().getGame().getEventManager().registerListeners(plugin, this);
        }
    }

    /**
     * MessageChannelEvent.Chat listener caused by a player.
     *
     * @param event
     *         MessageChannelEvent.Chat
     * @param player
     *         Player
     */
    @Listener(order = Order.POST)
    public void onMessage(MessageChannelEvent.Chat event, @Root Player player) {
        plugin.sendMessage(event, player);
    }
}
