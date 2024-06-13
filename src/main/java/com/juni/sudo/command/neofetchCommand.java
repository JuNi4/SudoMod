package com.juni.sudo.command;

import com.juni.sudo.util.prompt;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import static com.juni.sudo.command.sudoCommand.shout;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class neofetchCommand
{

    private static boolean added_to_apt = false;

    public static String formatBits(double size)
    {
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        int unit = 0;

        while ( size > 1024 )
        {
            size /= 1024;
            unit++;
        }

        return ((int)(size * 1000) / 1000.f ) + " " + units[unit];
    }

    public static String[] createMSG(CommandContext<ServerCommandSource> context)
    {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();

        return new String[]{
            "§2████████§r   "           + prompt.createUser(prompt.getUser(context)),
            "§2█§0██§2██§0██§2█§r   "   + "-".repeat( prompt.createUser(prompt.getUser(context)).length() ),
            "§2█§0██§2██§0██§2█§r   "   + "§2OS§r: " + MinecraftClient.getInstance().getVersionType() + "-" + MinecraftVersion.create().getId(),
            "§2███§0██§2███§r   "       + "§2FPS§r: " + MinecraftClient.getInstance().getCurrentFps(),
            "§2██§0████§2██§r   "       + "§2Packages§r: " + aptCommand.installed_packages.size(),
            "§2██§0█§2██§0█§2██§r   "   + "§2Shell§r: mcfunction",
            "§2██§0█§2██§0█§2██§r   "   + "§2Terminal§r: mc chat",
            "§2████████§r   "           + "§2CPU§r: " + hardware.getProcessor().toString().split("\n")[0],
            "                     "   + "§2GPU§r: " + hardware.getGraphicsCards().get(0).getName(),
            "                     "   + "§2Memory§r: " + formatBits( hardware.getMemory().getTotal() - hardware.getMemory().getAvailable() ) + " / " + formatBits( hardware.getMemory().getTotal() ),
            "                     "   + "§0█§4█§2█§6█§1█§5█§3█§7█§r",
            "                     "   + "§8█§c█§a█§e█§9█§d█§b█§f█§r",
        };
    };

    public static LiteralArgumentBuilder<ServerCommandSource> register()
    {
        // add to apt
        if (!added_to_apt)
        {
            aptCommand.installed_packages.add("neofetch");
            added_to_apt = true;
        }

        return literal("neofetch")
                .executes(neofetchCommand::run);
    }

    public static int run(CommandContext<ServerCommandSource> context)
    {
        shout(context, prompt.createCommandString(context));

        for (String line : createMSG(context))
        {
            shout(context, line);
        }

        return 1;
    }

}
