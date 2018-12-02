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

import com.discordsrv.core.api.channel.Chat;
import com.discordsrv.core.api.channel.ChatMessage;
import com.google.common.util.concurrent.FutureCallback;
import lombok.AllArgsConstructor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@AllArgsConstructor
public abstract class SpongeChat implements Chat {

    private final MessageChannel messageChannel;

    @Override
    public void sendMessage(ChatMessage<Long> message, FutureCallback<Void> resultCallback) {
        try {
            messageChannel.send(message.getSender(), Text.of(message.getMessage()));
            resultCallback.onSuccess(null);
        } catch (Throwable throwable) {
            resultCallback.onFailure(throwable);
        }
    }
}
