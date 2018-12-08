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

import com.discordsrv.sponge.DSRVSponge;
import lombok.AllArgsConstructor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.advancement.AdvancementEvent;

/**
 * Advancement message listener.
 */
@AllArgsConstructor
public class AdvancementMessageListener {

    private DSRVSponge plugin;

    /**
     * AdvancementEvent.Grant listener.
     *
     * @param event
     *         AdvancementEvent.Grant
     */
    @Listener(order = Order.POST)
    public void onAdvancement(AdvancementEvent.Grant event) {
        plugin.sendMessage(event, event.getTargetEntity());
    }
}
