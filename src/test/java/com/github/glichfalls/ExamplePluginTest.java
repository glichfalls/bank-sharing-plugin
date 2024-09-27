package com.github.glichfalls;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		try {
			ExternalPluginManager.loadBuiltin(BankSharingPlugin.class);
			RuneLite.main(args);
		} catch (Exception e) {
			System.out.println("Error loading plugin: " + e.getMessage());
		}
	}
}