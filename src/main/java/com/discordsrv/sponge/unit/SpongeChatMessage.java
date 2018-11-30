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

package com.discordsrv.sponge.unit;

import com.discordsrv.core.api.channel.ChatMessage;
import com.discordsrv.core.api.common.unit.Named;
import lombok.Value;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Value
public class SpongeChatMessage implements ChatMessage {

    private Object source;
    private Text message;

    @Override
    public Named getSender() {
        return callback -> callback.accept(source.getClass().getName());
    }

    @Override
    public String getMessage() {
        return message.toPlain();
    }

    @Override
    public void getUniqueIdentifier(@Nullable Consumer callback) {
        //TODO
    }
}
