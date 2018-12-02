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
import com.discordsrv.core.conf.Configuration;
import com.discordsrv.sponge.lookup.MessageChannelChatLookup;
import com.discordsrv.sponge.lookup.SpongeChatChannelLookup;
import com.discordsrv.sponge.lookup.SpongePlayerUserLookup;
import com.discordsrv.sponge.lookup.SpongeTeamRoleLookup;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.core.JDA;
import org.spongepowered.api.Game;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.util.function.Consumer;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SpongeContext implements Context {

    private final DSRVSponge plugin;
    private Configuration configuration;
    private PlayerUserAuthenticator userAuthenticator;
    private PlayerUserLinker playerUserLinker;
    private SpongePlayerUserLookup playerUserLookup;
    private TeamRoleLinker teamRoleLinker;
    private SpongeTeamRoleLookup teamRoleLookup;
    private ChatChannelLinker chatChannelLinker;
    private SpongeChatChannelLookup chatChannelLookup;
    private MessageChannelChatLookup messageChannelChatLookup;
    private SpongeExecutorService spongeExecutorService;
    private Game game;
    private JDA jda;

    @Override
    public Consumer<Runnable> getEventHandler() {
        return spongeExecutorService::execute;
    }
}
