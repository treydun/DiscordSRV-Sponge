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
package com.discordsrv.sponge;

import com.discordsrv.core.api.dsrv.platform.Platform;
import com.discordsrv.core.auth.PlayerUserAuthenticator;
import com.discordsrv.core.channel.LocalChatChannelLinker;
import com.discordsrv.core.conf.Configuration;
import com.discordsrv.core.conf.annotation.Configured;
import com.discordsrv.core.conf.annotation.Val;
import com.discordsrv.core.discord.DSRVJDABuilder;
import com.discordsrv.core.role.LocalTeamRoleLinker;
import com.discordsrv.core.user.LocalPlayerUserLinker;
import com.discordsrv.core.user.UplinkedPlayerUserLinker;
import com.discordsrv.sponge.listener.ChatMessageListener;
import com.discordsrv.sponge.listener.DeathMessageListener;
import com.discordsrv.sponge.lookup.MessageChannelChatLookup;
import com.discordsrv.sponge.lookup.SpongeChatChannelLookup;
import com.discordsrv.sponge.lookup.SpongePlayerUserLookup;
import com.discordsrv.sponge.lookup.SpongeTeamRoleLookup;
import com.discordsrv.sponge.unit.SpongeConsole;
import com.discordsrv.sponge.unit.chat.SpongeChat;
import com.discordsrv.sponge.unit.chat.SpongeGlobalChat;
import com.google.common.util.concurrent.FutureCallback;
import lombok.Getter;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageChannel;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.naming.ConfigurationException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@ParametersAreNonnullByDefault
@Plugin(id = "discordsrv", name = "DiscordSRV", description = "empty")
public class DSRVSponge implements Platform<SpongeContext> {

    @Getter private final SpongeContext context = new SpongeContext(this);

    @Configured
    public DSRVSponge(final @Val("remote-linker") boolean remoteLinker) {
        try {
            context.setJda(context.getConfiguration().create(DSRVJDABuilder.class).build());
            context.setUserAuthenticator(context.getConfiguration().create(PlayerUserAuthenticator.class));
            context.setPlayerUserLinker(remoteLinker ? context.getConfiguration().create(UplinkedPlayerUserLinker.class)
                : context.getConfiguration().create(LocalPlayerUserLinker.class));
            context.setTeamRoleLinker(context.getConfiguration().create(LocalTeamRoleLinker.class));
            context.setChatChannelLinker(
                context.getConfiguration().create(LocalChatChannelLinker.class, new SpongeConsole(context)));
            context.setTeamRoleLookup(new SpongeTeamRoleLookup(context));
            context.setChatChannelLookup(new SpongeChatChannelLookup(context));
            context.setPlayerUserLookup(new SpongePlayerUserLookup(context));
            context.setMessageChannelChatLookup(new MessageChannelChatLookup());
            context.setSpongeExecutorService(Sponge.getScheduler().createSyncExecutor(this));
            context.setGame(Sponge.getGame());
        } catch (ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException | LoginException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        try {
            Configuration configuration = Configuration.getStandardConfiguration(new Yaml());
            configuration.create(DSRVSponge.class, new DualTreeBidiMap<String, String>());
            context.setConfiguration(configuration);
        } catch (IOException | ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        context.getMessageChannelChatLookup().addTranslator((original, callback) -> {
            if (original.getClass().getName().startsWith("org.spongepowered.api.text.channel.MessageChannel")) {
                callback.onSuccess(new SpongeGlobalChat(original));
            }
        });
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new ChatMessageListener(this));
        Sponge.getEventManager().registerListeners(this, new DeathMessageListener(this));
        try {
            Class.forName("org.spongepowered.api.advancement.Advancement");
            Sponge.getEventManager()
                .registerListeners(this, new com.discordsrv.sponge.listener.AdvancementMessageListener(this));
        } catch (ClassNotFoundException ignored) {
            Sponge.getEventManager()
                .registerListeners(this, new com.discordsrv.sponge.listener.AchievementMessageListener(this));
        }
    }

    public void sendChatMessage(MessageChannelEvent event, Player player) {
        Optional<MessageChannel> messageChannel = event.getChannel();
        if (!messageChannel.isPresent()) {
            return;
        }

        context.getMessageChannelChatLookup().lookup(messageChannel.get(), new FutureCallback<SpongeChat>() {
            @Override
            public void onSuccess(@Nullable final SpongeChat result) {
                if (result == null) {
                    return;
                }

                sendChatMessage(result, player);
            }

            @Override
            public void onFailure(final Throwable t) {
            }
        });
    }

    public void sendChatMessage(SpongeChat spongeChat, Player player) {
        context.getChatChannelLinker().translate(spongeChat, new FutureCallback<TextChannel>() {
            @Override
            public void onSuccess(@Nullable final TextChannel result) {
                if (result == null) {
                    return;
                }

                result.sendMessage(player.getName()).queue();
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
            }
        });
    }
}
