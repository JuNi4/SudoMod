package com.juni.sudo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class sudoCommand
{
    //// Constants ////
    // base command prefix
    public static String BASE_COMMAND_STRING = "$";

    public static LiteralArgumentBuilder<ServerCommandSource> BASE_COMMAND = literal(BASE_COMMAND_STRING);

    // root commands
    public static String[] ROOT_COMMANDS = {"sudo", "doas", "pkexec", "cowsay"};

    // register commands

    public static LiteralArgumentBuilder<ServerCommandSource> createCommandBranch(LiteralArgumentBuilder<ServerCommandSource> base)
    {
        return base
            // apt
            .then(aptCommand.register())
            .then(aptCommand.register_apt_get())
            .then(aptCommand.register_repository())
            // tree
            .then(treeCommand.register())
            // neofetch command
            .then(neofetchCommand.register());
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

        // without sudo / doas, etc
        BASE_COMMAND = createCommandBranch(BASE_COMMAND);

        // create all sudo (+ alternatives) commands
        for (String cmd : ROOT_COMMANDS) {
            BASE_COMMAND = BASE_COMMAND.then( createCommandBranch( literal(cmd) ) );
        }

        // register commands
        dispatcher.register( BASE_COMMAND );
    }

    // TODO: make this send the message to all players
    public static void shout(CommandContext<ServerCommandSource> context, String text) {
        // send a message to the player executing the command
        context.getSource().sendMessage(Text.literal(text));
    }
}
