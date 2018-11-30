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

package com.discordsrv.sponge.unit;

import com.discordsrv.core.api.user.MinecraftPlayer;
import com.google.common.util.concurrent.FutureCallback;
import lombok.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@Value
public class SpongeMinecraftPlayer implements MinecraftPlayer {

    public final Player player;

    @Override
    public void sendMessage(String message, FutureCallback<Void> resultCallback) {
        try {
            player.sendMessage(Text.of(message));
            resultCallback.onSuccess(null);
        } catch (Throwable throwable) {
            resultCallback.onFailure(throwable);
        }
    }

    @Override
    public void getName(Consumer<CharSequence> callback) {
        callback.accept(player.getName());
    }

    @Override
    public void getUniqueIdentifier(Consumer<UUID> callback) {
        callback.accept(player.getUniqueId());
    }
}
