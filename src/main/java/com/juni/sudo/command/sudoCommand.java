package com.juni.sudo.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static java.lang.Math.random;
import static net.minecraft.server.command.CommandManager.*;

public class sudoCommand {

    //// Changing Variables ////
    // variable for storing all installed packages
    public static String[] pre_installed = {"minecraft","fabric"};
    public static ArrayList<String> installed_packages = new ArrayList<>(Arrays.stream(pre_installed).toList());
    // variable for storing the selected package repository
    public static int selected_repo = 0;

    //// Constants ////
    // commands
    public static String[] COMMANDS = {"sudo","doas"};
    // prompt
    public static String PROMPT = "§aroot@server§r:§9~§r$";
    public static String PACKAGE_NOT_FOUND = "§cE§r: Unable to locate package ";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        // TODO: make a simpler command addition system
        // sudo + doas command
        for (String cmd : COMMANDS) {
            dispatcher.register(literal(cmd)
                    // apt
                    .then(literal("apt")
                            // install <package>
                            .then(literal("install").then(argument("package", word()).executes(sudoCommand::apt_install)))
                            // remove <package>
                            .then(literal("remove").then(argument("package", word()).executes(sudoCommand::apt_remove)))
                            // list all packages
                            .then(literal("list").executes(sudoCommand::listAll)))
                    // repository shenanigans
                    .then(literal("add-apt-repository")
                            // repo options
                            .then(literal("none").executes(sudoCommand::selectRepoNone))
                            .then(literal("dev").executes(sudoCommand::selectRepoDev))
                            .then(literal("default").executes(sudoCommand::selectRepoDefault))
                    )
                    // tree
                    .then(literal("tree").executes(sudoCommand::tree))
                    // tree <directory>
                    .then(literal("tree").then(argument("directory", word()).executes(sudoCommand::tree))));

        }
    }

    // TODO: make this send the message to all players
    public static void shout(CommandContext<ServerCommandSource> context, String text) {
        // send a message to the player executing the command
        context.getSource().sendMessage(Text.literal(text));
    }

    // install a package
    public static int apt_install(CommandContext<ServerCommandSource> context) {

        // get the package argument
        String pkg = context.getArgument("package",String.class);

        new Thread(() -> {
            // say initial command thing
            shout(context,PROMPT + " sudo apt install " + pkg );
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
        }).start();

        return 1;
    }

    // remove a package
    public static int apt_remove(CommandContext<ServerCommandSource> context) {
        // get the package argument
        String pkg = context.getArgument("package",String.class);

        new Thread(() -> {
            // say initial command thing
            shout(context, PROMPT + " sudo apt remove " + pkg );
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
            if ( installed_packages.contains(pkg) ) {
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
            } else {
                // say package not found
                shout(context, PACKAGE_NOT_FOUND + pkg);
            }
        }).start();

        return 1;
    }

    // function for listing all installed packages
    public static int listAll (CommandContext<ServerCommandSource> context) {
        // list all packages
        shout(context, "Listing... Done");

        for (String pkg : installed_packages) {
            // print package
            shout(context, "§2" + pkg + "§r");
        }

        return 1;
    }

    // repository selection function
    public static int selectRepoNone (CommandContext<ServerCommandSource> context) {
        // set the repo
        shout(context,"Added repo 'None'");
        selected_repo = 0;
        return 1;
    }
    public static int selectRepoDefault (CommandContext<ServerCommandSource> context) {
        // set the repo
        shout(context,"Added repo 'Default'");
        selected_repo = 1;
        return 1;
    }
    public static int selectRepoDev (CommandContext<ServerCommandSource> context) {
        // set the repo
        shout(context,"Added repo 'dev'");
        selected_repo = 2;
        return 1;
    }

    // tree command
    public static int tree(CommandContext<ServerCommandSource> context) {

        // split string to array
        String[] Lines = {"var", "├── backups", "├── cache", "│ ├── apt", "│ │ ├── archives", "│ │ │ ├── cmatrix_1.2a+git20181122-1_amd64.deb", "│ │ │ ├── console-setup_1.193~deb10u1_all.deb", "│ │ │ ├── console-setup-linux_1.193~deb10u1_all.deb", "│ │ │ ├── kbd_2.0.4-4_amd64.deb", "│ │ │ ├── keyboard-configuration_1.193~deb10u1_all.deb", "│ │ │ ├── liblocale-gettext-perl_1.07-3+b4_amd64.deb", "│ │ │ ├── lock", "│ │ │ ├── partial", "│ │ │ └── xkb-data_2.26-2_all.deb", "│ │ ├── pkgcache.bin", "│ │ └── srcpkgcache.bin", "│ ├── debconf", "│ │ ├── config.dat", "│ │ ├── config.dat-old", "│ │ ├── passwords.dat", "│ │ ├── templates.dat", "│ │ └── templates.dat-old", "│ └── ldconfig", "│     └── aux-cache", "├── lib", "│ ├── apt", "│ │ ├── extended_states", "│ │ ├── lists", "│ │ │ ├── auxfiles", "│ │ │ ├── deb.debian.org_debian_dists_buster_InRelease", "│ │ │ ├── deb.debian.org_debian_dists_buster_main_binary-amd64_Packages", "│ │ │ ├── deb.debian.org_debian_dists_buster_main_i18n_Translation-en", "│ │ │ ├── lock", "│ │ │ ├── partial", "│ │ │ ├── security.debian.org_debian-security_dists_buster_updates_InRelease", "│ │ │ ├── security.debian.org_debian-security_dists_buster_updates_main_binary-amd64_Packages", "│ │ │ └── security.debian.org_debian-security_dists_buster_updates_main_i18n_Translation-en", "│ │ ├── mirrors", "│ │ │ └── partial", "│ │ └── periodic", "│ ├── dbus", "│ ├── dhcp", "│ ├── dpkg", "│ │ ├── alternatives", "│ │ │ ├── awk", "│ │ │ ├── builtins.7.gz", "│ │ │ ├── editor", "│ │ │ ├── ex", "│ │ │ ├── pager", "│ │ │ ├── rcp", "│ │ │ ├── rlogin", "│ │ │ ├── rmt", "│ │ │ ├── rsh", "│ │ │ ├── rview", "│ │ │ ├── rvim", "│ │ │ ├── vi", "│ │ │ ├── view", "│ │ │ ├── vim", "│ │ │ ├── vimdiff", "│ │ │ └── w", "│ │ ├── available", "│ │ ├── cmethopt", "│ │ ├── diversions", "│ │ ├── diversions-old", "│ │ ├── info", "│ │ │ ├── adduser.conffiles", "│ │ │ ├── adduser.config", "│ │ │ ├── adduser.list", "│ │ │ ├── adduser.md5sums", "│ │ │ ├── adduser.postinst", "│ │ │ ├── adduser.postrm", "│ │ │ ├── adduser.templates", "│ │ │ ├── apt.conffiles", "│ │ │ ├── apt.list", "│ │ │ ├── apt.md5sums", "│ │ │ ├── apt.postinst", "│ │ │ ├── apt.postrm", "│ │ │ ├── apt.preinst", "│ │ │ ├── apt.prerm", "│ │ │ ├── apt.shlibs", "│ │ │ ├── apt.triggers", "│ │ │ ├── base-files.conffiles", "│ │ │ ├── base-files.list", "│ │ │ ├── base-files.md5sums", "│ │ │ ├── base-files.postinst", "│ │ │ ├── base-passwd.list", "│ │ │ ├── base-passwd.md5sums", "│ │ │ ├── base-passwd.postinst", "│ │ │ ├── base-passwd.postrm", "│ │ │ ├── base-passwd.preinst", "│ │ │ ├── base-passwd.templates", "│ │ │ ├── bash.conffiles", "│ │ │ ├── bash.list", "│ │ │ ├── bash.md5sums", "│ │ │ ├── bash.postinst", "│ │ │ ├── bash.postrm", "│ │ │ ├── bash.preinst", "│ │ │ ├── bash.prerm", "│ │ │ ├── bsdutils.list", "│ │ │ ├── bsdutils.md5sums", "│ │ │ ├── cmatrix.list", "│ │ │ ├── cmatrix.md5sums", "│ │ │ ├── console-setup.config", "│ │ │ ├── console-setup-linux.conffiles", "│ │ │ ├── console-setup-linux.list", "│ │ │ ├── console-setup-linux.md5sums", "│ │ │ ├── console-setup-linux.postinst", "│ │ │ ├── console-setup-linux.postrm", "│ │ │ ├── console-setup.list", "│ │ │ ├── console-setup.md5sums", "│ │ │ ├── console-setup.postinst", "│ │ │ ├── console-setup.postrm", "│ │ │ ├── console-setup.templates", "│ │ │ ├── coreutils.list", "│ │ │ ├── coreutils.md5sums", "│ │ │ ├── coreutils.postinst", "│ │ │ ├── coreutils.postrm", "│ │ │ ├── dash.config", "│ │ │ ├── dash.list", "│ │ │ ├── dash.md5sums", "│ │ │ ├── dash.postinst", "│ │ │ ├── dash.postrm", "│ │ │ ├── dash.prerm", "│ │ │ ├── dash.templates", "│ │ │ ├── dbus.conffiles", "│ │ │ ├── dbus.list", "│ │ │ ├── dbus.md5sums", "│ │ │ ├── dbus.postinst", "│ │ │ ├── dbus.postrm", "│ │ │ ├── dbus.preinst", "│ │ │ ├── dbus.prerm", "│ │ │ ├── dbus.triggers", "│ │ │ ├── debconf.conffiles", "│ │ │ ├── debconf.config", "│ │ │ ├── debconf.list", "│ │ │ ├── debconf.md5sums", "│ │ │ ├── debconf.postinst", "│ │ │ ├── debconf.postrm", "│ │ │ ├── debconf.preinst", "│ │ │ ├── debconf.prerm", "│ │ │ ├── debconf.templates", "│ │ │ ├── debian-archive-keyring.conffiles", "│ │ │ ├── debian-archive-keyring.list", "│ │ │ ├── debian-archive-keyring.md5sums", "│ │ │ ├── debian-archive-keyring.postinst", "│ │ │ ├── debian-archive-keyring.postrm", "│ │ │ ├── debian-archive-keyring.preinst", "│ │ │ ├── debian-archive-keyring.prerm", "│ │ │ ├── debianutils.list", "│ │ │ ├── debianutils.md5sums", "│ │ │ ├── debianutils.postinst", "│ │ │ ├── debianutils.postrm", "│ │ │ ├── dialog.list", "│ │ │ ├── dialog.md5sums", "│ │ │ ├── diffutils.list", "│ │ │ ├── diffutils.md5sums", "│ │ │ ├── dmsetup.list", "│ │ │ ├── dmsetup.md5sums", "│ │ │ ├── dmsetup.postinst", "│ │ │ ├── dpkg.conffiles", "│ │ │ ├── dpkg.list", "│ │ │ ├── dpkg.md5sums", "│ │ │ ├── dpkg.postinst", "│ │ │ ├── dpkg.postrm", "│ │ │ ├── e2fsprogs.conffiles", "│ │ │ ├── e2fsprogs.list", "│ │ │ ├── e2fsprogs.md5sums", "│ │ │ ├── e2fsprogs.postinst", "│ │ │ ├── e2fsprogs.preinst", "│ │ │ ├── fdisk.list", "│ │ │ ├── fdisk.md5sums", "│ │ │ ├── findutils.list", "│ │ │ ├── findutils.md5sums", "│ │ │ ├── format", "│ │ │ ├── gcc-8-base:amd64.list", "│ │ │ ├── gcc-8-base:amd64.md5sums", "│ │ │ ├── gpgv.list", "│ │ │ ├── gpgv.md5sums", "│ │ │ ├── grep.list", "│ │ │ ├── grep.md5sums", "│ │ │ ├── gzip.list", "│ │ │ ├── gzip.md5sums", "│ │ │ ├── hostname.list", "│ │ │ ├── hostname.md5sums", "│ │ │ ├── ifupdown.conffiles", "│ │ │ ├── ifupdown.list", "│ │ │ ├── ifupdown.md5sums", "│ │ │ ├── ifupdown.postinst", "│ │ │ ├── ifupdown.postrm", "│ │ │ ├── ifupdown.preinst", "│ │ │ ├── ifupdown.prerm", "│ │ │ ├── init.list", "│ │ │ ├── init.md5sums", "│ │ │ ├── init-system-helpers.list", "│ │ │ ├── init-system-helpers.md5sums", "│ │ │ ├── iproute2.conffiles", "│ │ │ ├── iproute2.config", "│ │ │ ├── iproute2.list", "│ │ │ ├── iproute2.md5sums", "│ │ │ ├── iproute2.postinst", "│ │ │ ├── iproute2.postrm", "│ │ │ ├── iproute2.templates", "│ │ │ ├── iputils-ping.list", "│ │ │ ├── iputils-ping.md5sums", "│ │ │ ├── iputils-ping.postinst", "│ │ │ ├── isc-dhcp-client.conffiles", "│ │ │ ├── isc-dhcp-client.list", "│ │ │ ├── isc-dhcp-client.md5sums", "│ │ │ ├── isc-dhcp-client.postrm", "│ │ │ ├── isc-dhcp-common.list", "│ │ │ ├── isc-dhcp-common.md5sums", "│ │ │ ├── kbd.list", "│ │ │ ├── kbd.md5sums", "│ │ │ ├── kbd.postinst", "│ │ │ ├── kbd.postrm", "│ │ │ ├── kbd.preinst", "│ │ │ ├── keyboard-configuration.config", "│ │ │ ├── keyboard-configuration.list", "│ │ │ ├── keyboard-configuration.md5sums", "│ │ │ ├── keyboard-configuration.postinst", "│ │ │ ├── keyboard-configuration.postrm", "│ │ │ ├── keyboard-configuration.preinst", "│ │ │ ├── keyboard-configuration.templates", "│ │ │ ├── krb5-locales.list", "│ │ │ ├── krb5-locales.md5sums", "│ │ │ ├── libacl1:amd64.list", "│ │ │ ├── libacl1:amd64.md5sums", "│ │ │ ├── libacl1:amd64.shlibs", "│ │ │ ├── libacl1:amd64.symbols", "│ │ │ ├── libacl1:amd64.triggers", "│ │ │ ├── libapparmor1:amd64.list", "│ │ │ ├── libapparmor1:amd64.md5sums", "│ │ │ ├── libapparmor1:amd64.shlibs", "│ │ │ ├── libapparmor1:amd64.symbols", "│ │ │ ├── libapparmor1:amd64.triggers", "│ │ │ ├── libapt-pkg5.0:amd64.list", "│ │ │ ├── libapt-pkg5.0:amd64.md5sums", "│ │ │ ├── libapt-pkg5.0:amd64.shlibs", "│ │ │ ├── libapt-pkg5.0:amd64.symbols", "│ │ │ ├── libapt-pkg5.0:amd64.triggers", "│ │ │ ├── libargon2-1:amd64.list", "│ │ │ ├── libargon2-1:amd64.md5sums", "│ │ │ ├── libargon2-1:amd64.shlibs", "│ │ │ ├── libargon2-1:amd64.symbols", "│ │ │ ├── libargon2-1:amd64.triggers", "│ │ │ ├── libatm1:amd64.list", "│ │ │ ├── libatm1:amd64.md5sums", "│ │ │ ├── libatm1:amd64.shlibs", "│ │ │ ├── libatm1:amd64.symbols", "│ │ │ ├── libatm1:amd64.triggers", "│ │ │ ├── libattr1:amd64.conffiles", "│ │ │ ├── libattr1:amd64.list", "│ │ │ ├── libattr1:amd64.md5sums", "│ │ │ ├── libattr1:amd64.shlibs", "│ │ │ ├── libattr1:amd64.symbols", "│ │ │ ├── libattr1:amd64.triggers", "│ │ │ ├── libaudit1:amd64.list", "│ │ │ ├── libaudit1:amd64.md5sums", "│ │ │ ├── libaudit1:amd64.shlibs", "│ │ │ ├── libaudit1:amd64.symbols", "│ │ │ ├── libaudit1:amd64.triggers", "│ │ │ ├── libaudit-common.conffiles", "│ │ │ ├── libaudit-common.list", "│ │ │ ├── libaudit-common.md5sums", "│ │ │ ├── libblkid1:amd64.list", "│ │ │ ├── libblkid1:amd64.md5sums", "│ │ │ ├── libblkid1:amd64.shlibs", "│ │ │ ├── libblkid1:amd64.symbols", "│ │ │ ├── libblkid1:amd64.triggers", "│ │ │ ├── libbsd0:amd64.list", "│ │ │ ├── libbsd0:amd64.md5sums", "│ │ │ ├── libbsd0:amd64.shlibs", "│ │ │ ├── libbsd0:amd64.symbols", "│ │ │ ├── libbsd0:amd64.triggers", "│ │ │ ├── libbz2-1.0:amd64.list", "│ │ │ ├── libbz2-1.0:amd64.md5sums", "│ │ │ ├── libbz2-1.0:amd64.shlibs", "│ │ │ ├── libbz2-1.0:amd64.triggers", "│ │ │ ├── libc6:amd64.conffiles", "│ │ │ ├── libc6:amd64.list", "│ │ │ ├── libc6:amd64.md5sums", "│ │ │ ├── libc6:amd64.postinst", "│ │ │ ├── libc6:amd64.postrm", "│ │ │ ├── libc6:amd64.preinst", "│ │ │ ├── libc6:amd64.shlibs", "│ │ │ ├── libc6:amd64.symbols", "│ │ │ ├── libc6:amd64.templates", "│ │ │ ├── libc6:amd64.triggers", "│ │ │ ├── libcap2:amd64.list", "│ │ │ ├── libcap2:amd64.md5sums", "│ │ │ ├── libcap2:amd64.shlibs", "│ │ │ ├── libcap2:amd64.symbols", "│ │ │ ├── libcap2:amd64.triggers", "│ │ │ ├── libcap2-bin.list", "│ │ │ ├── libcap2-bin.md5sums", "│ │ │ ├── libcap-ng0:amd64.list", "│ │ │ ├── libcap-ng0:amd64.md5sums", "│ │ │ ├── libcap-ng0:amd64.shlibs", "│ │ │ ├── libcap-ng0:amd64.symbols", "│ │ │ ├── libcap-ng0:amd64.triggers", "│ │ │ ├── libc-bin.conffiles", "│ │ │ ├── libc-bin.list", "│ │ │ ├── libc-bin.md5sums", "│ │ │ ├── libc-bin.postinst", "│ │ │ ├── libc-bin.triggers", "│ │ │ ├── libc-l10n.list", "│ │ │ ├── libc-l10n.md5sums", "│ │ │ ├── libcom-err2:amd64.list", "│ │ │ ├── libcom-err2:amd64.md5sums", "│ │ │ ├── libcom-err2:amd64.shlibs", "│ │ │ ├── libcom-err2:amd64.symbols", "│ │ │ ├── libcom-err2:amd64.triggers", "│ │ │ ├── libcryptsetup12:amd64.list", "│ │ │ ├── libcryptsetup12:amd64.md5sums", "│ │ │ ├── libcryptsetup12:amd64.shlibs", "│ │ │ ├── libcryptsetup12:amd64.symbols", "│ │ │ ├── libcryptsetup12:amd64.triggers", "│ │ │ ├── libdb5.3:amd64.list", "│ │ │ ├── libdb5.3:amd64.md5sums", "│ │ │ ├── libdb5.3:amd64.shlibs", "│ │ │ ├── libdb5.3:amd64.triggers", "│ │ │ ├── libdbus-1-3:amd64.list", "│ │ │ ├── libdbus-1-3:amd64.md5sums", "│ │ │ ├── libdbus-1-3:amd64.shlibs", "│ │ │ ├── libdbus-1-3:amd64.symbols", "│ │ │ ├── libdbus-1-3:amd64.triggers", "│ │ │ ├── libdebconfclient0:amd64.list", "│ │ │ ├── libdebconfclient0:amd64.md5sums", "│ │ │ ├── libdebconfclient0:amd64.shlibs", "│ │ │ ├── libdebconfclient0:amd64.symbols", "│ │ │ ├── libdebconfclient0:amd64.triggers", "│ │ │ ├── libdevmapper1.02.1:amd64.list", "│ │ │ ├── libdevmapper1.02.1:amd64.md5sums", "│ │ │ ├── libdevmapper1.02.1:amd64.shlibs", "│ │ │ ├── libdevmapper1.02.1:amd64.symbols", "│ │ │ ├── libdevmapper1.02.1:amd64.triggers", "│ │ │ ├── libdns-export1104.list", "│ │ │ ├── libdns-export1104.md5sums", "│ │ │ ├── libdns-export1104.shlibs", "│ │ │ ├── libdns-export1104.triggers", "│ │ │ ├── libedit2:amd64.list", "│ │ │ ├── libedit2:amd64.md5sums", "│ │ │ ├── libedit2:amd64.shlibs", "│ │ │ ├── libedit2:amd64.symbols", "│ │ │ ├── libedit2:amd64.triggers", "│ │ │ ├── libelf1:amd64.list", "│ │ │ ├── libelf1:amd64.md5sums", "│ │ │ ├── libelf1:amd64.shlibs", "│ │ │ ├── libelf1:amd64.symbols", "│ │ │ ├── libelf1:amd64.triggers", "│ │ │ ├── libexpat1:amd64.list", "│ │ │ ├── libexpat1:amd64.md5sums", "│ │ │ ├── libexpat1:amd64.shlibs", "│ │ │ ├── libexpat1:amd64.symbols", "│ │ │ ├── libexpat1:amd64.triggers", "│ │ │ ├── libext2fs2:amd64.list", "│ │ │ ├── libext2fs2:amd64.md5sums", "│ │ │ ├── libext2fs2:amd64.shlibs", "│ │ │ ├── libext2fs2:amd64.symbols", "│ │ │ ├── libext2fs2:amd64.triggers", "│ │ │ ├── libfdisk1:amd64.list", "│ │ │ ├── libfdisk1:amd64.md5sums", "│ │ │ ├── libfdisk1:amd64.shlibs", "│ │ │ ├── libfdisk1:amd64.symbols", "│ │ │ ├── libfdisk1:amd64.triggers", "│ │ │ ├── libffi6:amd64.list", "│ │ │ ├── libffi6:amd64.md5sums", "│ │ │ ├── libffi6:amd64.shlibs", "│ │ │ ├── libffi6:amd64.symbols", "│ │ │ ├── libffi6:amd64.triggers", "│ │ │ ├── libgcc1:amd64.list", "│ │ │ ├── libgcc1:amd64.md5sums", "│ │ │ ├── libgcc1:amd64.postinst", "│ │ │ ├── libgcc1:amd64.shlibs", "│ │ │ ├── libgcc1:amd64.symbols", "│ │ │ ├── libgcc1:amd64.triggers", "│ │ │ ├── libgcrypt20:amd64.list", "│ │ │ ├── libgcrypt20:amd64.md5sums", "│ │ │ ├── libgcrypt20:amd64.shlibs", "│ │ │ ├── libgcrypt20:amd64.symbols", "│ │ │ ├── libgcrypt20:amd64.triggers", "│ │ │ ├── libgmp10:amd64.list", "│ │ │ ├── libgmp10:amd64.md5sums", "│ │ │ ├── libgmp10:amd64.shlibs", "│ │ │ ├── libgmp10:amd64.symbols", "│ │ │ ├── libgmp10:amd64.triggers", "│ │ │ ├── libgnutls30:amd64.list", "│ │ │ ├── libgnutls30:amd64.md5sums", "│ │ │ ├── libgnutls30:amd64.shlibs", "│ │ │ ├── libgnutls30:amd64.symbols", "│ │ │ ├── libgnutls30:amd64.triggers", "│ │ │ ├── libgpg-error0:amd64.list", "│ │ │ ├── libgpg-error0:amd64.md5sums", "│ │ │ ├── libgpg-error0:amd64.shlibs", "│ │ │ ├── libgpg-error0:amd64.symbols", "│ │ │ ├── libgpg-error0:amd64.triggers", "│ │ │ ├── libgpm2:amd64.list", "│ │ │ ├── libgpm2:amd64.md5sums", "│ │ │ ├── libgpm2:amd64.shlibs", "│ │ │ ├── libgpm2:amd64.symbols", "│ │ │ ├── libgpm2:amd64.triggers", "│ │ │ ├── libgssapi-krb5-2:amd64.list", "│ │ │ ├── libgssapi-krb5-2:amd64.md5sums", "│ │ │ ├── libgssapi-krb5-2:amd64.postinst", "│ │ │ ├── libgssapi-krb5-2:amd64.postrm", "│ │ │ ├── libgssapi-krb5-2:amd64.shlibs", "│ │ │ ├── libgssapi-krb5-2:amd64.symbols", "│ │ │ ├── libgssapi-krb5-2:amd64.triggers", "│ │ │ ├── libhogweed4:amd64.list", "│ │ │ ├── libhogweed4:amd64.md5sums", "│ │ │ ├── libhogweed4:amd64.shlibs", "│ │ │ ├── libhogweed4:amd64.symbols", "│ │ │ ├── libhogweed4:amd64.triggers", "│ │ │ ├── libidn11:amd64.list", "│ │ │ ├── libidn11:amd64.md5sums", "│ │ │ ├── libidn11:amd64.shlibs", "│ │ │ ├── libidn11:amd64.symbols", "│ │ │ ├── libidn11:amd64.triggers", "│ │ │ ├── libidn2-0:amd64.list", "│ │ │ ├── libidn2-0:amd64.md5sums", "│ │ │ ├── libidn2-0:amd64.shlibs", "│ │ │ ├── libidn2-0:amd64.symbols", "│ │ │ ├── libidn2-0:amd64.triggers", "│ │ │ ├── libip4tc0:amd64.list", "│ │ │ ├── libip4tc0:amd64.md5sums", "│ │ │ ├── libip4tc0:amd64.shlibs", "│ │ │ ├── libip4tc0:amd64.symbols", "│ │ │ ├── libip4tc0:amd64.triggers", "│ │ │ ├── libisc-export1100:amd64.list", "│ │ │ ├── libisc-export1100:amd64.md5sums", "│ │ │ ├── libisc-export1100:amd64.shlibs", "│ │ │ ├── libisc-export1100:amd64.triggers", "│ │ │ ├── libjson-c3:amd64.list", "│ │ │ ├── libjson-c3:amd64.md5sums", "│ │ │ ├── libjson-c3:amd64.shlibs", "│ │ │ ├── libjson-c3:amd64.symbols", "│ │ │ ├── libjson-c3:amd64.triggers", "│ │ │ ├── libk5crypto3:amd64.list", "│ │ │ ├── libk5crypto3:amd64.md5sums", "│ │ │ ├── libk5crypto3:amd64.shlibs", "│ │ │ ├── libk5crypto3:amd64.symbols", "│ │ │ ├── libk5crypto3:amd64.triggers", "│ │ │ ├── libkeyutils1:amd64.list", "│ │ │ ├── libkeyutils1:amd64.md5sums", "│ │ │ ├── libkeyutils1:amd64.shlibs", "│ │ │ ├── libkeyutils1:amd64.symbols", "│ │ │ ├── libkeyutils1:amd64.triggers", "│ │ │ ├── libkmod2:amd64.list", "│ │ │ ├── libkmod2:amd64.md5sums", "│ │ │ ├── libkmod2:amd64.shlibs", "│ │ │ ├── libkmod2:amd64.symbols", "│ │ │ ├── libkmod2:amd64.triggers", "│ │ │ ├── libkrb5-3:amd64.list", "│ │ │ ├── libkrb5-3:amd64.md5sums", "│ │ │ ├── libkrb5-3:amd64.shlibs", "│ │ │ ├── libkrb5-3:amd64.symbols", "│ │ │ ├── libkrb5-3:amd64.triggers", "│ │ │ ├── libkrb5support0:amd64.list", "│ │ │ ├── libkrb5support0:amd64.md5sums", "│ │ │ ├── libkrb5support0:amd64.shlibs", "│ │ │ ├── libkrb5support0:amd64.symbols", "│ │ │ ├── libkrb5support0:amd64.triggers", "│ │ │ ├── liblocale-gettext-perl.list", "│ │ │ ├── liblocale-gettext-perl.md5sums", "│ │ │ ├── liblz4-1:amd64.list", "│ │ │ ├── liblz4-1:amd64.md5sums", "│ │ │ ├── liblz4-1:amd64.shlibs", "│ │ │ ├── liblz4-1:amd64.symbols", "│ │ │ ├── liblz4-1:amd64.triggers", "│ │ │ ├── liblzma5:amd64.list", "│ │ │ ├── liblzma5:amd64.md5sums", "│ │ │ ├── liblzma5:amd64.shlibs", "│ │ │ ├── liblzma5:amd64.symbols", "│ │ │ ├── liblzma5:amd64.triggers", "│ │ │ ├── libmnl0:amd64.list", "│ │ │ ├── libmnl0:amd64.md5sums", "│ │ │ ├── libmnl0:amd64.shlibs", "│ │ │ ├── libmnl0:amd64.symbols", "│ │ │ ├── libmnl0:amd64.triggers", "│ │ │ ├── libmount1:amd64.list", "│ │ │ ├── libmount1:amd64.md5sums", "│ │ │ ├── libmount1:amd64.shlibs", "│ │ │ ├── libmount1:amd64.symbols", "│ │ │ ├── libmount1:amd64.triggers", "│ │ │ ├── libncurses6:amd64.list", "│ │ │ ├── libncurses6:amd64.md5sums", "│ │ │ ├── libncurses6:amd64.shlibs", "│ │ │ ├── libncurses6:amd64.symbols", "│ │ │ ├── libncurses6:amd64.triggers", "│ │ │ ├── libncursesw6:amd64.list", "│ │ │ ├── libncursesw6:amd64.md5sums", "│ │ │ ├── libncursesw6:amd64.shlibs", "│ │ │ ├── libncursesw6:amd64.symbols", "│ │ │ ├── libncursesw6:amd64.triggers", "│ │ │ ├── libnettle6:amd64.list", "│ │ │ ├── libnettle6:amd64.md5sums", "│ │ │ ├── libnettle6:amd64.shlibs", "│ │ │ ├── libnettle6:amd64.symbols", "│ │ │ ├── libnettle6:amd64.triggers", "│ │ │ ├── libnss-systemd:amd64.list", "│ │ │ ├── libnss-systemd:amd64.md5sums", "│ │ │ ├── libnss-systemd:amd64.postinst", "│ │ │ ├── libnss-systemd:amd64.postrm", "│ │ │ ├── libnss-systemd:amd64.shlibs", "│ │ │ ├── libnss-systemd:amd64.triggers", "│ │ │ ├── libp11-kit0:amd64.list", "│ │ │ ├── libp11-kit0:amd64.md5sums", "│ │ │ ├── libp11-kit0:amd64.shlibs", "│ │ │ ├── libp11-kit0:amd64.symbols", "│ │ │ ├── libp11-kit0:amd64.triggers", "│ │ │ ├── libpam0g:amd64.list", "│ │ │ ├── libpam0g:amd64.md5sums", "│ │ │ ├── libpam0g:amd64.postinst", "│ │ │ ├── libpam0g:amd64.postrm", "│ │ │ ├── libpam0g:amd64.shlibs", "│ │ │ ├── libpam0g:amd64.symbols", "│ │ │ ├── libpam0g:amd64.templates", "│ │ │ ├── libpam0g:amd64.triggers", "│ │ │ ├── libpam-cap:amd64.conffiles", "│ │ │ ├── libpam-cap:amd64.list", "│ │ │ ├── libpam-cap:amd64.md5sums", "│ │ │ ├── libpam-cap:amd64.postinst", "│ │ │ ├── libpam-cap:amd64.prerm", "│ │ │ ├── libpam-modules:amd64.conffiles", "│ │ │ ├── libpam-modules:amd64.list", "│ │ │ ├── libpam-modules:amd64.md5sums", "│ │ │ ├── libpam-modules:amd64.postinst", "│ │ │ ├── libpam-modules:amd64.postrm", "│ │ │ ├── libpam-modules:amd64.preinst", "│ │ │ ├── libpam-modules:amd64.templates", "│ │ │ ├── libpam-modules-bin.list", "│ │ │ ├── libpam-modules-bin.md5sums", "│ │ │ ├── libpam-runtime.conffiles", "│ │ │ ├── libpam-runtime.list", "│ │ │ ├── libpam-runtime.md5sums", "│ │ │ ├── libpam-runtime.postinst", "│ │ │ ├── libpam-runtime.postrm", "│ │ │ ├── libpam-runtime.prerm", "│ │ │ ├── libpam-runtime.templates", "│ │ │ ├── libpam-systemd:amd64.list", "│ │ │ ├── libpam-systemd:amd64.md5sums", "│ │ │ ├── libpam-systemd:amd64.postinst", "│ │ │ ├── libpam-systemd:amd64.prerm", "│ │ │ ├── libpcre3:amd64.list", "│ │ │ ├── libpcre3:amd64.md5sums", "│ │ │ ├── libpcre3:amd64.shlibs", "│ │ │ ├── libpcre3:amd64.symbols", "│ │ │ ├── libpcre3:amd64.triggers", "│ │ │ ├── libprocps7:amd64.list", "│ │ │ ├── libprocps7:amd64.md5sums", "│ │ │ ├── libprocps7:amd64.shlibs", "│ │ │ ├── libprocps7:amd64.triggers", "│ │ │ ├── libseccomp2:amd64.list", "│ │ │ ├── libseccomp2:amd64.md5sums", "│ │ │ ├── libseccomp2:amd64.shlibs", "│ │ │ ├── libseccomp2:amd64.symbols", "│ │ │ ├── libseccomp2:amd64.triggers", "│ │ │ ├── libselinux1:amd64.list", "│ │ │ ├── libselinux1:amd64.md5sums", "│ │ │ ├── libselinux1:amd64.shlibs", "│ │ │ ├── libselinux1:amd64.symbols", "│ │ │ ├── libselinux1:amd64.triggers", "│ │ │ ├── libsemanage1:amd64.list", "│ │ │ ├── libsemanage1:amd64.md5sums", "│ │ │ ├── libsemanage1:amd64.shlibs", "│ │ │ ├── libsemanage1:amd64.symbols", "│ │ │ ├── libsemanage1:amd64.triggers", "│ │ │ ├── libsemanage-common.conffiles", "│ │ │ ├── libsemanage-common.list", "│ │ │ ├── libsemanage-common.md5sums", "│ │ │ ├── libsepol1:amd64.list", "│ │ │ ├── libsepol1:amd64.md5sums", "│ │ │ ├── libsepol1:amd64.shlibs", "│ │ │ ├── libsepol1:amd64.symbols", "│ │ │ ├── libsepol1:amd64.triggers", "│ │ │ ├── libsmartcols1:amd64.list", "│ │ │ ├── libsmartcols1:amd64.md5sums", "│ │ │ ├── libsmartcols1:amd64.shlibs", "│ │ │ ├── libsmartcols1:amd64.symbols", "│ │ │ ├── libsmartcols1:amd64.triggers", "│ │ │ ├── libss2:amd64.list", "│ │ │ ├── libss2:amd64.md5sums", "│ │ │ ├── libss2:amd64.shlibs", "│ │ │ ├── libss2:amd64.symbols", "│ │ │ ├── libss2:amd64.triggers", "│ │ │ ├── libssl1.1:amd64.list", "│ │ │ ├── libssl1.1:amd64.md5sums", "│ │ │ ├── libssl1.1:amd64.postinst", "│ │ │ ├── libssl1.1:amd64.postrm", "│ │ │ ├── libssl1.1:amd64.shlibs", "│ │ │ ├── libssl1.1:amd64.symbols", "│ │ │ ├── libssl1.1:amd64.templates", "│ │ │ ├── libssl1.1:amd64.triggers", "│ │ │ ├── libstdc++6:amd64.list", "│ │ │ ├── libstdc++6:amd64.md5sums", "│ │ │ ├── libstdc++6:amd64.postinst", "│ │ │ ├── libstdc++6:amd64.prerm", "│ │ │ ├── libstdc++6:amd64.shlibs", "│ │ │ ├── libstdc++6:amd64.symbols", "│ │ │ ├── libstdc++6:amd64.triggers", "│ │ │ ├── libsystemd0:amd64.list", "│ │ │ ├── libsystemd0:amd64.md5sums", "│ │ │ ├── libsystemd0:amd64.shlibs", "│ │ │ ├── libsystemd0:amd64.symbols", "│ │ │ ├── libsystemd0:amd64.triggers", "│ │ │ ├── libtasn1-6:amd64.list", "│ │ │ ├── libtasn1-6:amd64.md5sums", "│ │ │ ├── libtasn1-6:amd64.shlibs", "│ │ │ ├── libtasn1-6:amd64.symbols", "│ │ │ ├── libtasn1-6:amd64.triggers", "│ │ │ ├── libtinfo6:amd64.list", "│ │ │ ├── libtinfo6:amd64.md5sums", "│ │ │ ├── libtinfo6:amd64.shlibs", "│ │ │ ├── libtinfo6:amd64.symbols", "│ │ │ ├── libtinfo6:amd64.triggers", "│ │ │ ├── libudev1:amd64.list", "│ │ │ ├── libudev1:amd64.md5sums", "│ │ │ ├── libudev1:amd64.shlibs", "│ │ │ ├── libudev1:amd64.symbols", "│ │ │ ├── libudev1:amd64.triggers", "│ │ │ ├── libunistring2:amd64.list", "│ │ │ ├── libunistring2:amd64.md5sums", "│ │ │ ├── libunistring2:amd64.shlibs", "│ │ │ ├── libunistring2:amd64.symbols", "│ │ │ ├── libunistring2:amd64.triggers", "│ │ │ ├── libuuid1:amd64.list", "│ │ │ ├── libuuid1:amd64.md5sums", "│ │ │ ├── libuuid1:amd64.shlibs", "│ │ │ ├── libuuid1:amd64.symbols", "│ │ │ ├── libuuid1:amd64.triggers", "│ │ │ ├── libx11-6:amd64.list", "│ │ │ ├── libx11-6:amd64.md5sums", "│ │ │ ├── libx11-6:amd64.shlibs", "│ │ │ ├── libx11-6:amd64.symbols", "│ │ │ ├── libx11-6:amd64.triggers", "│ │ │ ├── libx11-data.list", "│ │ │ ├── libx11-data.md5sums", "│ │ │ ├── libxau6:amd64.list", "│ │ │ ├── libxau6:amd64.md5sums", "│ │ │ ├── libxau6:amd64.shlibs", "│ │ │ ├── libxau6:amd64.triggers", "│ │ │ ├── libxcb1:amd64.list", "│ │ │ ├── libxcb1:amd64.md5sums", "│ │ │ ├── libxcb1:amd64.shlibs", "│ │ │ ├── libxcb1:amd64.symbols", "│ │ │ ├── libxcb1:amd64.triggers", "│ │ │ ├── libxdmcp6:amd64.list", "│ │ │ ├── libxdmcp6:amd64.md5sums", "│ │ │ ├── libxdmcp6:amd64.shlibs", "│ │ │ ├── libxdmcp6:amd64.triggers", "│ │ │ ├── libxext6:amd64.list", "│ │ │ ├── libxext6:amd64.md5sums", "│ │ │ ├── libxext6:amd64.shlibs", "│ │ │ ├── libxext6:amd64.symbols", "│ │ │ ├── libxext6:amd64.triggers", "│ │ │ ├── libxmuu1:amd64.list", "│ │ │ ├── libxmuu1:amd64.md5sums", "│ │ │ ├── libxmuu1:amd64.shlibs", "│ │ │ ├── libxmuu1:amd64.triggers", "│ │ │ ├── libxtables12:amd64.list", "│ │ │ ├── libxtables12:amd64.md5sums", "│ │ │ ├── libxtables12:amd64.shlibs", "│ │ │ ├── libxtables12:amd64.symbols", "│ │ │ ├── libxtables12:amd64.triggers", "│ │ │ ├── libzstd1:amd64.list", "│ │ │ ├── libzstd1:amd64.md5sums", "│ │ │ ├── libzstd1:amd64.shlibs", "│ │ │ ├── libzstd1:amd64.symbols", "│ │ │ ├── libzstd1:amd64.triggers", "│ │ │ ├── locales.conffiles", "│ │ │ ├── locales.config", "│ │ │ ├── locales.list", "│ │ │ ├── locales.md5sums", "│ │ │ ├── locales.postinst", "│ │ │ ├── locales.postrm", "│ │ │ ├── locales.prerm", "│ │ │ ├── locales.templates", "│ │ │ ├── login.conffiles", "│ │ │ ├── login.list", "│ │ │ ├── login.md5sums", "│ │ │ ├── login.postinst", "│ │ │ ├── login.preinst", "│ │ │ ├── lsb-base.list", "│ │ │ ├── lsb-base.md5sums", "│ │ │ ├── mawk.list", "│ │ │ ├── mawk.md5sums", "│ │ │ ├── mawk.postinst", "│ │ │ ├── mawk.prerm", "│ │ │ ├── mount.list", "│ │ │ ├── mount.md5sums", "│ │ │ ├── ncurses-base.conffiles", "│ │ │ ├── ncurses-base.list", "│ │ │ ├── ncurses-base.md5sums", "│ │ │ ├── ncurses-bin.list", "│ │ │ ├── ncurses-bin.md5sums", "│ │ │ ├── netbase.conffiles", "│ │ │ ├── netbase.list", "│ │ │ ├── netbase.md5sums", "│ │ │ ├── netbase.postinst", "│ │ │ ├── netbase.postrm", "│ │ │ ├── net-tools.list", "│ │ │ ├── net-tools.md5sums", "│ │ │ ├── openssh-client.conffiles", "│ │ │ ├── openssh-client.list", "│ │ │ ├── openssh-client.md5sums", "│ │ │ ├── openssh-client.postinst", "│ │ │ ├── openssh-client.postrm", "│ │ │ ├── openssh-client.preinst", "│ │ │ ├── openssh-client.prerm", "│ │ │ ├── passwd.conffiles", "│ │ │ ├── passwd.list", "│ │ │ ├── passwd.md5sums", "│ │ │ ├── passwd.postinst", "│ │ │ ├── passwd.preinst", "│ │ │ ├── perl-base.list", "│ │ │ ├── perl-base.md5sums", "│ │ │ ├── procps.conffiles", "│ │ │ ├── procps.list", "│ │ │ ├── procps.md5sums", "│ │ │ ├── procps.postinst", "│ │ │ ├── procps.postrm", "│ │ │ ├── procps.preinst", "│ │ │ ├── procps.prerm", "│ │ │ ├── psmisc.list", "│ │ │ ├── psmisc.md5sums", "│ │ │ ├── psmisc.postinst", "│ │ │ ├── psmisc.postrm", "│ │ │ ├── sed.list", "│ │ │ ├── sed.md5sums", "│ │ │ ├── systemd.conffiles", "│ │ │ ├── systemd.list", "│ │ │ ├── systemd.md5sums", "│ │ │ ├── systemd.postinst", "│ │ │ ├── systemd.postrm", "│ │ │ ├── systemd.preinst", "│ │ │ ├── systemd.prerm", "│ │ │ ├── systemd-sysv.list", "│ │ │ ├── systemd-sysv.md5sums", "│ │ │ ├── systemd-sysv.postinst", "│ │ │ ├── systemd.triggers", "│ │ │ ├── sysvinit-utils.list", "│ │ │ ├── sysvinit-utils.md5sums", "│ │ │ ├── tar.list", "│ │ │ ├── tar.md5sums", "│ │ │ ├── tar.postinst", "│ │ │ ├── tar.prerm", "│ │ │ ├── tree.list", "│ │ │ ├── tree.md5sums", "│ │ │ ├── tzdata.config", "│ │ │ ├── tzdata.list", "│ │ │ ├── tzdata.md5sums", "│ │ │ ├── tzdata.postinst", "│ │ │ ├── tzdata.postrm", "│ │ │ ├── tzdata.templates", "│ │ │ ├── util-linux.conffiles", "│ │ │ ├── util-linux.list", "│ │ │ ├── util-linux.md5sums", "│ │ │ ├── util-linux.postinst", "│ │ │ ├── util-linux.postrm", "│ │ │ ├── util-linux.prerm", "│ │ │ ├── vim-common.conffiles", "│ │ │ ├── vim-common.list", "│ │ │ ├── vim-common.md5sums", "│ │ │ ├── vim-common.postinst", "│ │ │ ├── vim-common.postrm", "│ │ │ ├── vim-common.preinst", "│ │ │ ├── vim-common.prerm", "│ │ │ ├── vim.list", "│ │ │ ├── vim.md5sums", "│ │ │ ├── vim.postinst", "│ │ │ ├── vim.postrm", "│ │ │ ├── vim.preinst", "│ │ │ ├── vim.prerm", "│ │ │ ├── vim-runtime.list", "│ │ │ ├── vim-runtime.md5sums", "│ │ │ ├── vim-runtime.postinst", "│ │ │ ├── vim-runtime.postrm", "│ │ │ ├── vim-runtime.preinst", "│ │ │ ├── xauth.list", "│ │ │ ├── xauth.md5sums", "│ │ │ ├── xkb-data.list", "│ │ │ ├── xkb-data.md5sums", "│ │ │ ├── xxd.list", "│ │ │ ├── xxd.md5sums", "│ │ │ ├── zlib1g:amd64.list", "│ │ │ ├── zlib1g:amd64.md5sums", "│ │ │ ├── zlib1g:amd64.shlibs", "│ │ │ ├── zlib1g:amd64.symbols", "│ │ │ └── zlib1g:amd64.triggers", "│ │ ├── lock", "│ │ ├── lock-frontend", "│ │ ├── parts", "│ │ ├── statoverride", "│ │ ├── statoverride-old", "│ │ ├── status", "│ │ ├── status-old", "│ │ ├── triggers", "│ │ │ ├── File", "│ │ │ ├── ldconfig", "│ │ │ ├── Lock", "│ │ │ └── Unincorp", "│ │ └── updates", "│ ├── misc", "│ ├── pam", "│ │ ├── account", "│ │ ├── auth", "│ │ ├── password", "│ │ ├── seen", "│ │ ├── session", "│ │ └── session-noninteractive", "│ ├── polkit-1", "│ │ └── localauthority", "│ │     └── 10-vendor.d", "│ │         └── systemd-networkd.pkla", "│ ├── systemd", "│ │ ├── catalog", "│ │ │ └── database", "│ │ └── deb-systemd-helper-enabled", "│ │     ├── apt-daily.timer.dsh-also", "│ │     ├── apt-daily-upgrade.timer.dsh-also", "│ │     ├── console-setup.service.dsh-also", "│ │     ├── fstrim.timer.dsh-also", "│ │     ├── ifupdown-wait-online.service.dsh-also", "│ │     ├── keyboard-setup.service.dsh-also", "│ │     ├── multi-user.target.wants", "│ │     │ ├── console-setup.service", "│ │     │ └── networking.service", "│ │     ├── networking.service.dsh-also", "│ │     ├── network-online.target.wants", "│ │     │ └── networking.service", "│ │     ├── sysinit.target.wants", "│ │     │ └── keyboard-setup.service", "│ │     └── timers.target.wants", "│ │         ├── apt-daily.timer", "│ │         └── apt-daily-upgrade.timer", "│ └── vim", "│     └── addons", "├── local", "├── lock -> /run/lock", "├── log", "│ ├── alternatives.log", "│ ├── apt", "│ │ ├── eipp.log.xz", "│ │ ├── history.log", "│ │ └── term.log", "│ ├── bootstrap.log", "│ ├── btmp", "│ ├── dpkg.log", "│ ├── faillog", "│ ├── lastlog", "│ └── wtmp", "├── mail", "├── opt", "├── run -> /run", "├── spool", "│ └── mail -> ../mail", "└── tmp", "", "46 directories, 810 files"};

        for (String line : Lines) {
            shout(context, line);
        }

        return 1;
    }
}
