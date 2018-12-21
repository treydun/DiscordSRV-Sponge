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
import com.discordsrv.core.channel.LocalChatChannelLinker;
import com.discordsrv.core.conf.Configuration;
import com.discordsrv.sponge.listener.ChannelMessageListener;
import com.discordsrv.sponge.listener.ChatMessageListener;
import com.discordsrv.sponge.listener.DeathMessageListener;
import com.discordsrv.sponge.listener.JoinLeaveMessageListener;
import com.discordsrv.sponge.unit.chat.SpongeChat;
import com.discordsrv.sponge.unit.chat.SpongeGlobalChat;
import com.google.common.util.concurrent.FutureCallback;
import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.core.entities.TextChannel;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.MessageChannel;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.naming.ConfigurationException;
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
@Plugin(id = "discordsrv", name = "DiscordSRV", description = "empty")
public class DSRVSponge implements Platform<SpongeContext> {

    @Getter private SpongeContext context;
    @Inject @ConfigDir(sharedRoot = false) private File configDirectory;
    @Inject private PluginContainer pluginContainer;
    @Inject private Game game;

    /**
     * GamePreInitializationEvent listener.
     *
     * @param event
     *         GamePreInitializationEvent
     */
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        try {
            // config
            if (!configDirectory.exists()) {
                configDirectory.mkdir();
            }
            URL defaultConfigUrl = pluginContainer.getAsset("defaultConfig.yml")
                .orElseThrow(() -> new RuntimeException("Default config missing from the jar")).getUrl();
            URL protectedConfigUrl = pluginContainer.getAsset("protectedConfig.yml")
                .orElseThrow(() -> new RuntimeException("Protected config missing from the jar")).getUrl();
            URL configUrl = pluginContainer.getAsset("config.yml")
                .orElseThrow(() -> new RuntimeException("Config missing from the jar")).getUrl();
            File userConfig = new File(configDirectory, "config.yml");
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
            Configuration configuration = Configuration
                .getStandardConfiguration(new Yaml(), protectedConfigUrl, userConfig.toURI().toURL(), configUrl);
            // config mappings
            Map<String, String> mappings = new HashMap<>();
            mappings.put("plugin", SpongeContext.class.getName());
            mappings.put("channels", LocalChatChannelLinker.class.getName());
            mappings.put("generic-message-listener", ChannelMessageListener.class.getName());
            configuration.applyRemapping(mappings);
            // context
            context = configuration
                .create(SpongeContext.class, configuration, game.getScheduler().createSyncExecutor(this),
                    game.getScheduler().createAsyncExecutor(this), game);
            // global channel translator
            context.getMessageChannelChatLookup().addTranslator((original, callback) -> {
                if (original.getClass().getName().startsWith(MessageChannel.class.getName())) {
                    callback.onSuccess(new SpongeGlobalChat(original));
                }
            });
            // listeners
            game.getEventManager().registerListeners(this, new ChatMessageListener(this));
            game.getEventManager().registerListeners(this, new JoinLeaveMessageListener(this));
            game.getEventManager().registerListeners(this, new DeathMessageListener(this));
            try {
                Class.forName("org.spongepowered.api.advancement.Advancement");
                game.getEventManager()
                    .registerListeners(this, new com.discordsrv.sponge.listener.AdvancementMessageListener(this));
            } catch (ClassNotFoundException ignored) {
                game.getEventManager()
                    .registerListeners(this, new com.discordsrv.sponge.listener.AchievementMessageListener(this));
            }
            game.getEventManager()
                .registerListeners(this, context.getConfiguration().create(ChannelMessageListener.class, this));
        } catch (IOException | ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            exception.printStackTrace();
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
    public void sendMessage(final MessageChannelEvent event, final @Nullable Player player) {
        Optional<MessageChannel> messageChannel = event.getChannel();
        if (!messageChannel.isPresent() || event.getFormatter().toText().isEmpty() || event.isMessageCancelled()) {
            return;
        }
        context.getMessageChannelChatLookup().lookup(messageChannel.get(), new FutureCallback<SpongeChat>() {
            @Override
            public void onSuccess(@Nullable final SpongeChat result) {
                if (result == null) {
                    return;
                }
                sendMessage(result, event.getFormatter().toText().toPlain(), player);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                throwable.printStackTrace();
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
    public void sendMessage(final SpongeChat spongeChat, final String message, final @Nullable Player player) {
        context.getChatChannelLinker().translate(spongeChat, new FutureCallback<TextChannel>() {
            @Override
            public void onSuccess(@Nullable final TextChannel result) {
                if (result == null) {
                    return;
                }
                if (player != null) {
                    result.sendMessage(message + " (" + player.getName() + ")").queue();
                } else {
                    result.sendMessage(message).queue();
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
