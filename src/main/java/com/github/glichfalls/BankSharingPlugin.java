package com.github.glichfalls;

import com.google.gson.Gson;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Bank Sharing",
	description = "Example plugin to share bank",
	tags = {"bank", "sharing"}
)
public class BankSharingPlugin extends Plugin
{
	@Inject
	private Client client;

	private final Gson gson = new Gson();
	private final OkHttpClient httpClient = new OkHttpClient();

	// A list to store all the items in the bank
	private final List<Item> bankItems = new ArrayList<>();

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == 95)  // Bank container ID
		{
			ItemContainer bankContainer = client.getItemContainer(95);
			if (bankContainer != null)
			{
				// Add items to the bankItems list
				bankItems.addAll(Arrays.asList(bankContainer.getItems()));
				System.out.println("Bank items collected: " + bankItems.size());
			}
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		// The bank widget ID for when the bank interface closes
		if (event.getGroupId() == 12)  // Bank widget ID (12) for the standard bank interface
		{
			System.out.println("Bank closed, sending collected items to API");
			sendBankItemsAsJson();
			// Clear the buffer after sending
			bankItems.clear();
		}
	}

	public void sendBankItemsAsJson()
	{
		// Convert the collected bank items to JSON
		String json = gson.toJson(bankItems);
		System.out.println("Sending bank items JSON: " + json);

		// Send the JSON to the API
		sendJsonToApi(json);
	}

	public void sendJsonToApi(String json)
	{
		String url = "http://localhost:8000/upload-bank";

		RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				if (response.isSuccessful())
				{
					System.out.println("Successfully sent bank items to API!");
					showMessage("Successfully sent bank items to the API!");
				}
				else
				{
					System.err.println("Failed to send bank items: " + response.code());
					showMessage("Failed to send bank items to the API. Error code: " + response.code());
				}
			}
		});
	}

	// Method to display a message in the chatbox
	private void showMessage(String message)
	{
		// Send a message to the game chatbox
		final String formattedMessage = Text.removeTags(message);
		client.addChatMessage(net.runelite.api.ChatMessageType.GAMEMESSAGE, "", formattedMessage, null);
	}
}
