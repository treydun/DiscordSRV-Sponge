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

import com.discordsrv.core.api.auth.AuthenticationStore;
import com.discordsrv.core.api.channel.ChatChannelLookup;
import com.discordsrv.core.api.dsrv.platform.Platform;
import com.discordsrv.core.api.role.TeamRoleLookup;
import com.discordsrv.core.api.user.MinecraftPlayer;
import com.discordsrv.core.api.user.PlayerUserLookup;
import com.discordsrv.core.auth.PlayerUserAuthenticator;
import com.discordsrv.core.channel.LocalChatChannelLinker;
import com.discordsrv.core.conf.Configuration;
import com.discordsrv.core.conf.annotation.Configured;
import com.discordsrv.core.conf.annotation.Val;
import com.discordsrv.core.discord.DSRVJDABuilder;
import com.discordsrv.core.role.LocalTeamRoleLinker;
import com.discordsrv.core.user.LocalPlayerUserLinker;
import com.discordsrv.core.user.UplinkedPlayerUserLinker;
import com.discordsrv.sponge.lookup.SpongeChatChannelLookup;
import com.discordsrv.sponge.lookup.SpongePlayerUserLookup;
import com.discordsrv.sponge.lookup.SpongeTeamRoleLookup;
import com.discordsrv.sponge.unit.SpongeConsole;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import javax.naming.ConfigurationException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Plugin(id = "discordsrv", name = "DiscordSRV", description = "empty")
public class DSRVSponge implements Platform<SpongeContext> {

    private final SpongeContext context = new SpongeContext();

    @Configured
    public DSRVSponge(final @Val("bot-token") String botToken, final @Val("prefix") String prefix,
                      final @Val("game-name") String gameName, final @Val("game-type") int gameType,
                      final @Val("store") AuthenticationStore<MinecraftPlayer, User> store,
                      final @Val("executor") ScheduledExecutorService scheduledExecutorService,
                      final @Val("remote-linker") boolean remoteLinker,
                      final @Val("lookup") PlayerUserLookup playerUserLookup,
                      final @Val("users") BidiMap<UUID, String> playerStorage,
                      final @Val("roles") BidiMap<String, String> roleStorage,
                      final @Val("lookup") TeamRoleLookup teamRoleLookup,
                      final @Val("channels") BidiMap<String, String> channelStorage,
                      final @Val("lookup") ChatChannelLookup chatChannelLookup,
                      final @Val("console-channel") String consoleChannelId) {
        try {
            JDA jda = new DSRVJDABuilder(botToken, prefix, gameName, gameType).build(); // TODO
            context.setUserAuthenticator(new PlayerUserAuthenticator(store, scheduledExecutorService));
            context.setPlayerUserLinker(remoteLinker ? new UplinkedPlayerUserLinker(playerUserLookup)
                : new LocalPlayerUserLinker(playerStorage, playerUserLookup));
            context.setPlayerUserLookup(new SpongePlayerUserLookup(context));
            context.setTeamRoleLinker(new LocalTeamRoleLinker(roleStorage, teamRoleLookup));
            context.setTeamRoleLookup(new SpongeTeamRoleLookup(context));
            context.setChatChannelLinker(
                new LocalChatChannelLinker(channelStorage, chatChannelLookup, new SpongeConsole(context),
                    consoleChannelId));
            context.setChatChannelLookup(new SpongeChatChannelLookup(context));
            context.setGame(Sponge.getGame());
            context.setSpongeExecutorService(Sponge.getScheduler().createSyncExecutor(this));
            context.setJda(jda);
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        try {
            Configuration configuration = Configuration.getStandardConfiguration(new Yaml());
            configuration.create(DSRVSponge.class, new DualTreeBidiMap<String, String>());
            context.setConfiguration(configuration);
        } catch (IOException | ConfigurationException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
