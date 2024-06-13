package com.juni.sudo.command;

import com.juni.sudo.util.prompt;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.juni.sudo.command.sudoCommand.shout;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static java.lang.Math.random;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class aptCommand
{
    // variable for storing all installed packages
    public static String[] pre_installed = {
            MinecraftClient.getInstance().getVersionType() + "-" + MinecraftVersion.create().getId(),
    };

    public static ArrayList<String> installed_packages = new ArrayList<>(Arrays.stream(pre_installed).toList());
    // variable for storing the selected package repository
    public static int selected_repo = 0;
    public static int pre_selected_repo = 0;
    // variable for stopping commands from being run simultaneously
    public static boolean in_use = false;

    public static String PACKAGE_NOT_FOUND = "§cE§r: Unable to locate package ";

    public static boolean setupPackages = false;

    private static boolean added_to_apt = false;

    public static void createPackages()
    {
        // add all loaded mods as packages
        for (int i = 0; i < FabricLoader.getInstance().getAllMods().size(); i++)
        {
            ModContainer mod = FabricLoader.getInstance().getAllMods().stream().toList().get(i);
            installed_packages.add( "mod-" + mod.getMetadata().getId() );
        }

        // add all root commands
        for (String cmd : sudoCommand.ROOT_COMMANDS)
        {
            installed_packages.add(cmd);
        }

        // make sure the packages aren't added more than once
        setupPackages = true;
    }

    protected static LiteralArgumentBuilder<ServerCommandSource> createCommands(LiteralArgumentBuilder<ServerCommandSource> base)
    {
        return base
                // install <package>
                .then(literal("install").then(argument("package", word()).executes(aptCommand::apt_install)))
                // remove <package>
                .then(literal("remove").then(argument("package", word()).executes(aptCommand::apt_remove)))
                // list all packages
                .then(literal("list").executes(aptCommand::apt_listAll))
                // update repository index
                .then( literal( "update" ).executes( aptCommand::apt_update ) );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> register()
    {
        // make sure all apt packages are there
        if (!setupPackages)
            aptCommand.createPackages();

        // add to apt
        if (!added_to_apt)
        {
            aptCommand.installed_packages.add("apt");
            added_to_apt = true;
        }

        // apt command
        return createCommands(literal("apt"));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> register_apt_get()
    {
        // make sure all apt packages are there
        if (!setupPackages)
            aptCommand.createPackages();

        // apt command
        return createCommands(literal("apt-get"));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> register_repository()
    {
        // repository shenanigans
        return literal("add-apt-repository")
            // repo options
            .then(literal("none").executes(aptCommand::apt_selectRepoNone))
            .then(literal("dev").executes(aptCommand::apt_selectRepoDev))
            .then(literal("default").executes(aptCommand::apt_selectRepoDefault));
    }

    private static boolean getLock(CommandContext<ServerCommandSource> context)
    {
        /*
        E: Could not open lock file /var/lib/dpkg/lock-frontend - open (13: Permission denied)
        E: Unable to acquire the dpkg frontend lock (/var/lib/dpkg/lock-frontend), are you root?
         */

        // check for root
        if (!prompt.isRoot(context))
        {
            shout(context, "§cE§r: Could not open lock file /var/lib/dpkg/lock-frontend - open (13: Permission denied)\n" +
                    "§cE§r: Unable to acquire the dpkg frontend lock (/var/lib/dpkg/lock-frontend), are you root?");

            return false;
        }

        // check if another instance is already running
        if ( in_use ) {
            shout(context,"Unable to get exclusive lock\nThis usually means that another package management application (like apt-get or aptitude) is already running. Please close that application first.");
            return false;
        } else {
            in_use = true;
        }

        return true;
    }

    // install a package
    public static int apt_install(CommandContext<ServerCommandSource> context)
    {
        // say initial command thing
        //shout(context, createPrompt("root") + " sudo apt install " + pkg );
        shout(context, prompt.createCommandString(context));

        if (!getLock(context))
            return 1;

        // get the package argument
        String pkg = context.getArgument("package",String.class);

        new Thread(() -> {
            // wait a few things
            try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
            // say things
            shout(context,"Reading package lists... Done");
            // wait a few things
            try { Thread.sleep(250); } catch (InterruptedException e) { throw new RuntimeException(e); }
            shout(context,"Building dependency tree");
            // wait a few things
            try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
            shout(context,"Reading state information... Done");
            // wait a few things
            try { Thread.sleep(750); } catch (InterruptedException e) { throw new RuntimeException(e); }

            // if package is already installed
            if ( installed_packages.contains(pkg) ) {
                // say package already installed
                shout(context,pkg+" is already the newest version.\n0 upgraded, 0 newly installed, 0 to remove and 0 not upgraded.");
                // if package is not installed but will be
            } else if ( selected_repo == 1 || ( random() >= 0.5 && selected_repo == 2 ) ) {
                // install the package
                shout(context,"The following NEW packages will be installed:\n  "+pkg);
                shout(context,"0 upgraded, 1 newly installed, 0 to remove and 0 not upgraded.\nAfter this operation, "+ ( (float)(int)(random()*10000) ) /100.f +" MB of additional disk space will be used.");
                try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
                shout(context,"Get:1 http://archive.ubuntu.com/ubuntu focal/main amd64 "+pkg+" amd64 ubuntu ["+ ( (float)(int)(random()*10000000) ) /100.f +" kB]");
                shout(context,"(Reading database ... "+ (int) ( random()*10000 ) +" files and directories currently installed.");
                try { Thread.sleep(750); } catch (InterruptedException e) { throw new RuntimeException(e); }
                shout(context,"Selecting previously unselected package "+pkg+".");
                shout(context,"Preparing to unpack .../"+pkg+".deb ...");
                shout(context,"Unpacking "+pkg+" (ubuntu) ...");
                try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
                shout(context,"Setting up "+pkg+" ...");
                shout(context,"Processing triggers for man-db (2.9.1-1) ...");
                // add the package to the installed list
                installed_packages.add(pkg);
                // if package was not found
            } else {
                // say package not found
                shout(context, PACKAGE_NOT_FOUND + pkg);
            }

            // reset in_use var
            in_use = false;
        }).start();

        return 1;
    }

    // remove a package
    public static int apt_remove(CommandContext<ServerCommandSource> context)
    {
        // say initial command thing
        //shout(context, createPrompt("root") + " sudo apt remove " + pkg );
        shout(context, prompt.createCommandString(context));

        // check if another instance is already running
        if (!getLock(context))
            return 1;

        // get the package argument
        String pkg = context.getArgument("package",String.class);

        new Thread(() -> {
            // wait a few things
            try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
            // say things
            shout(context,"Reading package lists... Done");
            // wait a few things
            try { Thread.sleep(250); } catch (InterruptedException e) { throw new RuntimeException(e); }
            shout(context,"Building dependency tree");
            // wait a few things
            try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
            shout(context,"Reading state information... Done");
            // wait a few things
            try { Thread.sleep(750); } catch (InterruptedException e) { throw new RuntimeException(e); }

            // check if package is in the installed package list
            if ( installed_packages.contains(pkg) )
            {
                // remove the package
                shout(context,"The following packages will be REMOVED:");
                shout(context,"  "+pkg);
                shout(context,"0 upgraded, 0 newly installed, 1 to remove and 0 not upgraded.");
                shout(context,"After this operation, "+ (float)(int) (random()*10000) / 100 +" kB disk space will be freed.");
                // wait 250 ms
                try { Thread.sleep(250); } catch (InterruptedException e) { throw new RuntimeException(e); }
                // do the removal
                shout(context,"Removing "+pkg+" ...");
                shout(context,"Processing triggers for man-db (2.9.1-1) ...");
                installed_packages.remove(pkg);
            } else
            {
                // say package not found
                shout(context, PACKAGE_NOT_FOUND + pkg);
            }

            // reset in_use var
            in_use = false;
        }).start();

        return 1;
    }

    // function for listing all installed packages
    public static int apt_listAll (CommandContext<ServerCommandSource> context)
    {
        // say command
        shout(context, prompt.createCommandString(context));

        // list all packages
        shout(context, "Listing... Done");

        Collections.sort(installed_packages);

        for (String pkg : installed_packages) {
            // print package
            shout(context, "§2" + pkg + "§r");
        }

        return 1;
    }

    // repository selection function
    public static int apt_selectRepoNone (CommandContext<ServerCommandSource> context) {
        // say command
        shout(context, prompt.createCommandString(context));

        // check if another instance is already running
        if (!getLock(context))
            return 1;

        // set the repo
        shout(context,"Added repo 'None'");
        pre_selected_repo = 0;

        // reset in_use var
        in_use = false;

        return 1;
    }
    public static int apt_selectRepoDefault (CommandContext<ServerCommandSource> context) {
        // say command
        shout(context, prompt.createCommandString(context));

        // check if another instance is already running
        if (!getLock(context))
            return 1;

        // set the repo
        shout(context,"Added repo 'Default'");
        pre_selected_repo = 1;

        // reset in_use var
        in_use = false;

        return 1;
    }
    public static int apt_selectRepoDev (CommandContext<ServerCommandSource> context)
    {
        // say command
        shout(context, prompt.createCommandString(context));

        // check if another instance is already running
        if (!getLock(context))
            return 1;

        // set the repo
        shout(context,"Added repo 'dev'");
        pre_selected_repo = 2;

        // reset in_use var
        in_use = false;

        return 1;
    }

    public static int apt_update (CommandContext<ServerCommandSource> context)
    {
        // say command
        shout(context, prompt.createCommandString(context));

        // check if another instance is already running
        if (!getLock(context))
            return 1;

        // size of the package data fetch
        double size = (int)(random() * 1000) /100.d;
        // speed of the request
        double speed = (int)(random() * 1000);
        // time it will take to fetch the results
        int time = (int) (random() * 10);

        new Thread(() -> {
            // print the get request
            shout(context,"Get:1 http://archive.ubuntu.com/ubuntu");
            // wait a few things
            try { Thread.sleep(time* 1000L); } catch (InterruptedException e) { throw new RuntimeException(e); }
            // say time it took to download data
            shout(context,"Fetched "+size+" MB in "+time+"s ("+speed+" kB/s)");
            // random things
            shout(context, "Reading package lists... Done\nBuilding dependency tree\nReading state information... Done");
            // say amount of packages
            shout(context,(int)( random()*installed_packages.size() )+" packages can be upgraded. Run 'apt list --upgradable' to see them.");

            // reset in_use var
            in_use = false;
        }).start();

        // update the package list
        selected_repo = pre_selected_repo;

        return 1;
    }

}
