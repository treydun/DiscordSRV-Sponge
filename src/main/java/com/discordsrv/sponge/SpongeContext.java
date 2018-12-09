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

import com.discordsrv.core.api.channel.ChatChannelLinker;
import com.discordsrv.core.api.dsrv.Context;
import com.discordsrv.core.api.role.TeamRoleLinker;
import com.discordsrv.core.api.user.PlayerUserLinker;
import com.discordsrv.core.auth.PlayerUserAuthenticator;
import com.discordsrv.core.channel.LocalChatChannelLinker;
import com.discordsrv.core.conf.Configuration;
import com.discordsrv.core.conf.annotation.Configured;
import com.discordsrv.core.conf.annotation.Val;
import com.discordsrv.core.discord.DSRVJDABuilder;
import com.discordsrv.core.role.LocalTeamRoleLinker;
import com.discordsrv.core.user.LocalPlayerUserLinker;
import com.discordsrv.core.user.UplinkedPlayerUserLinker;
import com.discordsrv.sponge.lookup.MessageChannelChatLookup;
import com.discordsrv.sponge.lookup.SpongeChatChannelLookup;
import com.discordsrv.sponge.lookup.SpongePlayerUserLookup;
import com.discordsrv.sponge.lookup.SpongeTeamRoleLookup;
import com.discordsrv.sponge.unit.SpongeConsole;
import lombok.Getter;
import net.dv8tion.jda.core.JDA;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.spongepowered.api.Game;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import javax.naming.ConfigurationException;
import javax.security.auth.login.LoginException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * SpongeContext type, to provide the context for extensions.
 */
@Getter
public class SpongeContext implements Context {

    private final Configuration configuration;
    private final PlayerUserAuthenticator userAuthenticator;
    private final PlayerUserLinker playerUserLinker;
    private final SpongePlayerUserLookup playerUserLookup;
    private final TeamRoleLinker teamRoleLinker;
    private final SpongeTeamRoleLookup teamRoleLookup;
    private final ChatChannelLinker chatChannelLinker;
    private final SpongeChatChannelLookup chatChannelLookup;
    private final MessageChannelChatLookup messageChannelChatLookup;
    private final SpongeExecutorService spongeExecutorService;
    private final Game game;
    private final JDA jda;

    /**
     * Main constructor for SpongeContext.
     *
     * @param configuration Configuration used to initiate objects
     * @param spongeExecutorService synchronous executor service
     * @param game Sponge game object
     * @param remoteLinker user_remote_linking config option
     *
     * @throws ConfigurationException
     *         If there are no constructors with the {@link Configured} annotation.
     * @throws IllegalAccessException
     *         If the constructor attempted to be used is not accessible.
     * @throws InvocationTargetException
     *         Shouldn't happen, but inherited from {@link Constructor#newInstance(Object...)}.
     * @throws InstantiationException
     *         If instantiation of the type fails.
     */
    @Configured
    public SpongeContext(final @Val("configuration") Configuration configuration,
                         final @Val("executor") SpongeExecutorService spongeExecutorService,
                         final @Val("game") Game game, final @Val("use_remote_linking") boolean remoteLinker)
        throws ConfigurationException, IllegalAccessException, InvocationTargetException, InstantiationException,
               LoginException {
        this.playerUserLookup = new SpongePlayerUserLookup(this);
        this.playerUserLinker = remoteLinker ? configuration.create(UplinkedPlayerUserLinker.class, playerUserLookup)
            : configuration.create(LocalPlayerUserLinker.class, playerUserLookup);
        this.teamRoleLookup = new SpongeTeamRoleLookup(this);
        this.chatChannelLookup = new SpongeChatChannelLookup(this);
        this.configuration = configuration;
        this.userAuthenticator =
            configuration.create(PlayerUserAuthenticator.class, playerUserLinker, spongeExecutorService);
        this.teamRoleLinker = configuration.create(LocalTeamRoleLinker.class, teamRoleLookup);
        chatChannelLinker = configuration.create(LocalChatChannelLinker.class, new DualLinkedHashBidiMap<>(),
            getChatChannelLookup(), new SpongeConsole(this));
        this.messageChannelChatLookup = new MessageChannelChatLookup();
        this.spongeExecutorService = spongeExecutorService;
        this.game = game;
        this.jda = configuration.create(DSRVJDABuilder.class).build();
    }

    @Override
    public Consumer<Runnable> getEventHandler() {
        return spongeExecutorService::execute;
    }
}
