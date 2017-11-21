package net.minecraft.command;

import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandAfk extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "afk";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 0;
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.afk.usage";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            if (((EntityPlayer)sender).isAfkForced()) {
                ((EntityPlayer)sender).forcePlayerNotAfk();
            } else {
                ((EntityPlayer)sender).forcePlayerAfk();
            }
        }
    }
}
