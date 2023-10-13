package com.juni.sudo;

import com.juni.sudo.command.sudoCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sudo implements ModInitializer {
	public static final String MOD_ID = "sudo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// register sudo command
		CommandRegistrationCallback.EVENT.register(sudoCommand::register);
	}
}