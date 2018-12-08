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
import com.discordsrv.sponge.listener.ChannelMessageListener;
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
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.TextChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.channel.MessageChannel;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.naming.ConfigurationException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Main plugin class for DiscordSRV-Sponge.
 */
@ParametersAreNonnullByDefault
@NoArgsConstructor
@Plugin(id = "discordsrv", name = "DiscordSRV", description = "empty")
public class DSRVSponge implements Platform<SpongeContext> {

    @Getter private final SpongeContext context = new SpongeContext();

    /**
     * Configured constructor for the Sponge implementation of DiscordSRV.
     *
     * @param remoteLinker Whether or not remote linking should be used
     */
    @Configured
    public DSRVSponge(
        final @Val("remote-linker") Boolean remoteLinker,
        final @Val("executor") SpongeExecutorService spongeExecutorService
    ) {
        try {
            SpongeTeamRoleLookup teamRoleLookup = new SpongeTeamRoleLookup(context);
            SpongeChatChannelLookup chatChannelLookup = new SpongeChatChannelLookup(context);
            SpongePlayerUserLookup playerUserLookup = new SpongePlayerUserLookup(context);
            context.setTeamRoleLookup(teamRoleLookup);
            context.setChatChannelLookup(chatChannelLookup);
            context.setPlayerUserLookup(playerUserLookup);
            context.setUserAuthenticator(
                context.getConfiguration().create(PlayerUserAuthenticator.class, spongeExecutorService));
            context.setPlayerUserLinker(remoteLinker ? new UplinkedPlayerUserLinker(context.getPlayerUserLookup())
                : context.getConfiguration().create(LocalPlayerUserLinker.class, playerUserLookup));
            context.setTeamRoleLinker(context.getConfiguration().create(LocalTeamRoleLinker.class, teamRoleLookup));
            context.setChatChannelLinker(context.getConfiguration()
                .create(LocalChatChannelLinker.class, new SpongeConsole(context), chatChannelLookup));
            context.setMessageChannelChatLookup(new MessageChannelChatLookup());
            context.setSpongeExecutorService(spongeExecutorService);
            context.setGame(Sponge.getGame());
            context.setJda(context.getConfiguration().create(DSRVJDABuilder.class).build());
        } catch (ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException | LoginException e) {
            e.printStackTrace();
        }
    }

    /**
     * GamePreInitializationEvent listener.
     *
     * @param event GamePreInitializationEvent
     */
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        try {
            Configuration configuration = Configuration.getStandardConfiguration(new Yaml());
            configuration.create(DSRVSponge.class, Sponge.getScheduler().createSyncExecutor(this));
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

    /**
     * GameInitializationEvent listener.
     *
     * @param event GameInitializationEvent
     */
    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new ChannelMessageListener(this));
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

    /**
     * Sends a message based on the MessageChannelEvent.
     *
     * @param event MessageChannelEvent
     * @param player the Player that sent the message
     */
    public void sendMessage(MessageChannelEvent event, @Nullable Player player) {
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
                sendMessage(result, player);
            }

            @Override
            public void onFailure(final Throwable t) {
            }
        });
    }

    /**
     * TODO send message with formatting & stuff.
     *
     * @param spongeChat SpongeChat that the message came from
     * @param player the Player that send the message
     */
    public void sendMessage(SpongeChat spongeChat, @Nullable Player player) {
        context.getChatChannelLinker().translate(spongeChat, new FutureCallback<TextChannel>() {
            @Override
            public void onSuccess(@Nullable final TextChannel result) {
                if (result == null) {
                    return;
                }
                if (player != null) {
                    result.sendMessage(player.getName()).queue();
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
            }
        });
    }
}
