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

import com.discordsrv.core.api.role.Team;
import com.discordsrv.core.api.user.MinecraftPlayer;
import com.discordsrv.core.role.MalleableTeamRoleLookup;
import com.discordsrv.sponge.SpongeContext;
import com.google.common.util.concurrent.FutureCallback;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

/**
 * MalleableTeamRoleLookup implementation, for DiscordSRV-Sponge.
 */
@ParametersAreNonnullByDefault
public class SpongeTeamRoleLookup extends MalleableTeamRoleLookup<SpongeContext> {

    /**
     * Main constructor for the MalleableChatChannelLookup type.
     *
     * @param context
     *         The context in which this lookup is performing.
     */
    public SpongeTeamRoleLookup(SpongeContext context) {
        super(context);
    }

    /**
     * Fetches all the teams which this lookup can find.
     *
     * @param callback
     *         The callback to invoke when collected.
     */
    @Override
    public void getKnownTeams(FutureCallback<Stream<Team<MinecraftPlayer>>> callback) {
        // TODO
    }

    /**
     * Fetches all the roles which this lookup can find.
     *
     * @param callback
     *         The callback to invoke when collected.
     */
    @Override
    public void getKnownRoles(FutureCallback<Stream<Role>> callback) {
        try {
            callback.onSuccess(getContext().getJda().getRoles().stream());
        } catch (Throwable throwable) {
            callback.onFailure(throwable);
        }
    }
}
