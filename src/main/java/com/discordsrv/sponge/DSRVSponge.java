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
import com.discordsrv.core.api.user.PlayerUserLinker;
import com.discordsrv.core.auth.PlayerUserAuthenticator;
import com.discordsrv.core.channel.LocalChatChannelLinker;
import com.discordsrv.core.conf.Configuration;
import com.discordsrv.core.conf.annotation.Configured;
import com.discordsrv.core.conf.annotation.Val;
import com.discordsrv.core.debug.Debugger;
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
import com.google.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.TextChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Main plugin class for DiscordSRV-Sponge.
 */
@ParametersAreNonnullByDefault
@NoArgsConstructor
@Plugin(id = "discordsrv", name = "DiscordSRV", description = "empty")
public class DSRVSponge implements Platform<SpongeContext> {

    @Getter private SpongeContext context = new SpongeContext();
    @Inject @ConfigDir(sharedRoot = false) private File configDirectory;

    /**
     * Configured constructor for the Sponge implementation of DiscordSRV.
     *
     * @param remoteLinker
     *         Whether or not remote linking should be used
     */
    @Configured
    public DSRVSponge(final @Val("configuration") Configuration configuration,
                      final @Val("use_remote_linking") boolean remoteLinker,
                      final @Val("executor") SpongeExecutorService spongeExecutorService) {
        try {
            SpongePlayerUserLookup playerUserLookup = new SpongePlayerUserLookup(context);
            PlayerUserLinker playerUserLinker =
                remoteLinker ? configuration.create(UplinkedPlayerUserLinker.class, playerUserLookup)
                    : configuration.create(LocalPlayerUserLinker.class, playerUserLookup);
            SpongeTeamRoleLookup teamRoleLookup = new SpongeTeamRoleLookup(context);
            SpongeChatChannelLookup chatChannelLookup = new SpongeChatChannelLookup(context);
            context.setConfiguration(configuration);
            context.setUserAuthenticator(
                configuration.create(PlayerUserAuthenticator.class, playerUserLinker, spongeExecutorService));
            context.setPlayerUserLinker(playerUserLinker);
            context.setPlayerUserLookup(playerUserLookup);
            context.setTeamRoleLinker(configuration.create(LocalTeamRoleLinker.class, teamRoleLookup));
            context.setTeamRoleLookup(teamRoleLookup);
            context.setChatChannelLinker(
                configuration.create(LocalChatChannelLinker.class, new SpongeConsole(context), chatChannelLookup));
            context.setChatChannelLookup(chatChannelLookup);
            context.setMessageChannelChatLookup(new MessageChannelChatLookup());
            context.setSpongeExecutorService(spongeExecutorService);
            context.setGame(Sponge.getGame());
            context.setJda(configuration.create(DSRVJDABuilder.class).build());
        } catch (ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException | LoginException e) {
            e.printStackTrace();
        }
    }

    /**
     * GamePreInitializationEvent listener.
     *
     * @param event
     *         GamePreInitializationEvent
     */
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        try {
            if (!configDirectory.exists()) {
                configDirectory.mkdir();
            }
            File userConfig = new File(configDirectory, "config.yml");
            URL defaultConfigUrl = Sponge.getAssetManager().getAsset(this, "defaultConfig.yml")
                .orElseThrow(() -> new RuntimeException("Default config missing from the jar")).getUrl();
            if (!userConfig.exists()) {
                userConfig.createNewFile();
                InputStream inputStream = defaultConfigUrl.openStream();
                FileOutputStream outputStream = new FileOutputStream(userConfig);
                int bit;
                while ((bit = inputStream.read()) != -1) {
                    outputStream.write(bit);
                }
                inputStream.close();
                outputStream.close();
            }
            URL configUrl = Sponge.getAssetManager().getAsset(this, "config.yml")
                .orElseThrow(() -> new RuntimeException("Config missing from the jar")).getUrl();
            Configuration configuration =
                Configuration.getStandardConfiguration(new Yaml(), configUrl, userConfig.toURI().toURL());
            Map<String, String> mappings = new HashMap<>();
            mappings.put("plugin", DSRVSponge.class.getName());
            mappings.put("channels", LocalChatChannelLinker.class.getName());
            configuration.applyRemapping(mappings);
            context = configuration.create(DSRVSponge.class, configuration,
                Sponge.getScheduler().createSyncExecutor(this)).getContext();
            context.getMessageChannelChatLookup().addTranslator((original, callback) -> {
                if (original.getClass().getName().startsWith("org.spongepowered.api.text.channel.MessageChannel")) {
                    callback.onSuccess(new SpongeGlobalChat(original));
                }
            });
        } catch (IOException | ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * GameInitializationEvent listener.
     *
     * @param event
     *         GameInitializationEvent
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
     * @param event
     *         MessageChannelEvent
     * @param player
     *         the Player that sent the message
     */
    public void sendMessage(MessageChannelEvent event, @Nullable Player player) {
        Optional<MessageChannel> messageChannel = event.getChannel();
        if (!messageChannel.isPresent()) {
            return;
        }
        context
            .getMessageChannelChatLookup()
            .lookup(
                messageChannel.get(),
                new FutureCallback<SpongeChat>() {
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
     * @param spongeChat
     *         SpongeChat that the message came from
     * @param player
     *         the Player that send the message
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
