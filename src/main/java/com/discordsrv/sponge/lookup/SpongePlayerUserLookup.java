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

import com.discordsrv.core.api.user.MinecraftPlayer;
import com.discordsrv.core.user.MalleablePlayerUserLookup;
import com.discordsrv.sponge.SpongeContext;
import com.discordsrv.sponge.unit.SpongeMinecraftPlayer;
import com.google.common.util.concurrent.FutureCallback;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class SpongePlayerUserLookup extends MalleablePlayerUserLookup<SpongeContext> {

    /**
     * Main constructor for the MalleablePlayerUserLookup type.
     *
     * @param context
     *         The context in which this lookup is performing.
     */
    public SpongePlayerUserLookup(SpongeContext context) {
        super(context);
    }

    @Override
    public void getOnlinePlayers(FutureCallback<Stream<MinecraftPlayer>> callback) {
        try {
            callback.onSuccess(
                getContext().getGame().getServer().getOnlinePlayers().stream().map(SpongeMinecraftPlayer::new));
        } catch (Throwable throwable) {
            callback.onFailure(throwable);
        }
    }

    @Override
    public void getOnlineUsers(FutureCallback<Stream<User>> callback) {
        try {
            callback.onSuccess(getContext().getJda().getUsers().stream());
        } catch (Throwable throwable) {
            callback.onFailure(throwable);
        }
    }
}
