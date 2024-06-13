package com.juni.sudo.util;

import com.juni.sudo.command.sudoCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;

public class prompt
{

    // prompt
    public static String USER_TEMPLATE = "§a%s@%s§r";
    public static String PROMPT = ":§9~§r";

    // prompts

    public static String createUser(String user)
    {
        return USER_TEMPLATE.formatted(user, "server");
    }

    public static String createPrompt(String user)
    {
        return createUser(user) + PROMPT;
    }

    public static String createCommandString(CommandContext<ServerCommandSource> context)
    {
        return createPrompt(getUser(context)) + context.getInput();
    }

    public static String getUser(CommandContext<ServerCommandSource> context)
    {
        /*
        for (String cmd : ROOT_COMMANDS)
        {
            if (context.getInput().contains(cmd))
                return "root";
        }
         */

        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public static boolean isRoot(CommandContext<ServerCommandSource> context)
    {
        for (String cmd : sudoCommand.ROOT_COMMANDS)
        {
            if (context.getInput().contains(cmd))
                return true;
        }

        return false;
    }

}
