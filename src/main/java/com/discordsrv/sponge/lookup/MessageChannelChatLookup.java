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

import com.discordsrv.core.api.common.callback.MultiCallbackWrapper;
import com.discordsrv.core.api.common.functional.Translator;
import com.discordsrv.sponge.unit.chat.SpongeChat;
import com.google.common.util.concurrent.FutureCallback;
import org.spongepowered.api.text.channel.MessageChannel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * MessageChannelChatLookup type, for making dynamic lookups.
 *
 * Sponge specific.
 */
@ParametersAreNonnullByDefault
public class MessageChannelChatLookup {

    private final Set<Translator<MessageChannel, SpongeChat>> chatTranslators = new CopyOnWriteArraySet<>();

    /**
     * Looks up the {@link SpongeChat} from the {@link MessageChannel}.
     *
     * @param messageChannel The message channel used for the lookup
     * @param callback Lookup result callback
     */
    public void lookup(final MessageChannel messageChannel, final FutureCallback<SpongeChat> callback) {
        try {
            new MultiCallbackWrapper<>(chatTranslators.stream().map(
                translator -> (Consumer<FutureCallback<SpongeChat>>) internal -> translator
                    .translate(messageChannel, internal)).collect(Collectors.toList()), callback).run();
        } catch (Throwable throwable) {
            callback.onFailure(throwable);
        }
    }

    /**
     * Adds a translator to this lookup.
     *
     * @param translator
     *         The translator to invoke for getting a SpongeChat object.
     */
    public void addTranslator(Translator<MessageChannel, SpongeChat> translator) {
        this.chatTranslators.add(translator);
    }
}
