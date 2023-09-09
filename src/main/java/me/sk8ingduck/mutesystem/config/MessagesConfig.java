package me.sk8ingduck.mutesystem.config;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MessagesConfig extends Config {

	private static final String COMPONENT_REGEX
			= "(.*?)\\s*\\{(?:(hovertext:\\s*((?s:.)*?))?(?:,\\s*)?(command:\\s*((?s:.)*?))?(?:,\\s*)?(hovertext:\\s*((?s:.)*?))?)?}";

	private static final String COMPONENT_PATH_REGEX = "\\{(.+?)}";
	protected LinkedHashMap<String, String> messages;

	public MessagesConfig(String name, String path) {
		super(name, path);

		this.messages = new LinkedHashMap<>();
		loadMessages();
		loadTextComponents();

		messages.forEach((messagePath, message) -> messages.put(messagePath, (String) getPathOrSet(messagePath, message)));
	}

	public abstract void loadMessages();

	public abstract void loadTextComponents();

	private TextComponent parseChatComponent(String message, String... replacements) {
		if (message == null) return null;

		// Pattern to match the format: TEXT {hovertext: TEXT, command: /command}
		Pattern pattern = Pattern.compile(COMPONENT_REGEX, Pattern.DOTALL);

		Matcher matcher = pattern.matcher(message);

		if (!matcher.find()) return null;
		String text = replacePlaceholders(matcher.group(1), replacements);
		String hoverText = matcher.group(3) != null ? matcher.group(3) : matcher.group(7);

		TextComponent textComponent = new TextComponent(text);

		String command = matcher.group(5);
		if (command != null) {
			command = replacePlaceholders(command, replacements);
			textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		}

		if (hoverText != null) {
			hoverText = replacePlaceholders(hoverText, replacements);
			textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
		}

		return textComponent;

	}

	public BaseComponent[] get(String path, boolean prefix, String... replacements) {
		List<BaseComponent> finalComponents = new ArrayList<>();

		// Add the prefix if requested
		if (prefix) {
			finalComponents.add(new TextComponent(messages.get("mutesystem.prefix")));
		}

		String content = replacePlaceholders(messages.get(path), replacements);

		// Check if content contains pattern {path.to.textcomponent}
		Pattern pattern = Pattern.compile(COMPONENT_PATH_REGEX);
		Matcher matcher = pattern.matcher(content);

		int lastIndex = 0;

		// Replace all occurrences of the pattern with the corresponding TextComponent
		while (matcher.find()) {
			String beforeMatch = content.substring(lastIndex, matcher.start());
			String textComponentPath = matcher.group(1);
			lastIndex = matcher.end();

			// Replace all replacements in the text before the match
			beforeMatch = replacePlaceholders(beforeMatch, replacements);

			// Add the text before the match
			if (!beforeMatch.isEmpty()) {
				finalComponents.add(new TextComponent(beforeMatch));
			}

			// Add the TextComponent from parseChatComponent
			TextComponent textComponent = parseChatComponent(messages.get("textcomponents." + textComponentPath), replacements);
			if (textComponent != null)
				finalComponents.add(textComponent);
			else
				System.out.println("§c[MuteSystem] Failed to parse TextComponent {" + textComponentPath + "}. " +
						"Most likely is that it has a wrong format! Format should be:\n" +
						"Your text {hovertext: Your text on hover, command: /yourCommand}");
		}

		// Add the remaining text after the last match
		String afterLastMatch = content.substring(lastIndex);
		if (!afterLastMatch.isEmpty()) {
			finalComponents.add(new TextComponent(afterLastMatch));
		}

		return finalComponents.toArray(new BaseComponent[0]);
	}

	public BaseComponent[] get(String path, String... replacements) {
		return get(path, true, replacements);
	}

	private String replacePlaceholders(String text, String... replacements) {
		for (int i = 0; i < replacements.length; i += 2) {
			String toReplace = replacements[i];
			String replacement = replacements[i + 1];
			text = text.replaceAll(toReplace, replacement);
		}
		return text;
	}

	public String getString(String path) {
		return messages.get(path);
	}

	public void reload() {
		super.reload();

		messages.forEach((messagePath, message) -> messages.put(messagePath, (String) getPathOrSet(messagePath, message)));
	}
}
