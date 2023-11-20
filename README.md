# Sudo

A Minecraft mod that adds a little command from linux called sudo.

* All of these commands don't actually do anything. It's just a joke command.

## Usage:
```
# pretends to install a package
/sudo apt install <package name>
# pretends to remove a package
/sudo apt remove <package name>
# lists all installed packages
/sudo apt list

# set the package repo (none = no packages being installable, dev = sometimes, default = all)
/sudo add-apt-repository [none/dev/default]

# prints a directory tree
/sudo tree
```

The sudo alternatives `doas` are supported now to. 