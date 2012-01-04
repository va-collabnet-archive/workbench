package org.ihtsdo.project.workflow.tag;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import javax.swing.text.html.StyleSheet;

import org.dwfa.ace.config.AceConfig;

public class TagManager extends Observable {
	private static final String TODOCOLOR = "#FE9A2E";
	private static final String OUTBOXCOLOR = "#3A01DF";

	private static TagManager instance = null;
	private File tagFolder;
	private static final String tagHtml = "<html><body><table \"style=\"table-layout:fixed;\"><tr>" + "<td style=\"" + "background-color:${COLOR}; "
			+ "white-space:nowrap;color:${TEXT_COLOR};\"><B>${TAGNAME}<B><td>" + "<td style=\"white-space:nowrap;\">";
	private List<InboxTag> nameColorCache = new ArrayList<InboxTag>();

	private TagManager() {
		if (AceConfig.config != null) {
			File userFolder = AceConfig.config.getProfileFile().getParentFile();
			tagFolder = new File(userFolder, "tags");
			tagFolder.mkdir();
			try {
				createTag(new InboxTag("outbox", OUTBOXCOLOR, "#000000", new ArrayList<String>()));
				createTag(new InboxTag("todo", TODOCOLOR, "#000000", new ArrayList<String>()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<InboxTag> getAllTagsContent() throws IOException {
		List<InboxTag> result = null;
		File[] tags = tagFolder.listFiles();
		if (tags.length > 0) {
			result = new ArrayList<InboxTag>();
		} else {
			return result;
		}
		for (File file : tags) {
			if (!file.isHidden()) {
				FileInputStream ifis = new FileInputStream(file);
				InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
				BufferedReader br = new BufferedReader(iisr);
				String tagHeader = br.readLine();
				List<String> uuidList = new ArrayList<String>();
				while (br.ready()) {
					String uuid = br.readLine();
					uuidList.add(uuid);
				}
				InboxTag tag = new InboxTag(getName(tagHeader), getHtmlColor(getColorFromHeader(tagHeader)), getHtmlColor(getTextColorFromHeader(tagHeader)), uuidList);
				result.add(tag);
			}
		}
		return result;
	}

	public static void main(String[] args) {
		File f = new File("/Users/vahram/Desktop/wb-bundle/profiles/sebastian-pm/tags/outbox.tag");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(f));
			String tagHeader = br.readLine();
			System.out.println(tagHeader);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendToOutbox(String outboxUuid) throws IOException {
		removeUuidFromAllTagFiles(outboxUuid);
		List<String> uuidList = new ArrayList<String>();
		uuidList.add(outboxUuid);
		InboxTag tag = new InboxTag("outbox", OUTBOXCOLOR, "#000000", uuidList);
		persistTag(tag);
		setChanged();
		notifyObservers(new TagChange(TagChange.SPECIAL_TAG_ADDED, tag));
	}

	public void saveAsToDo(String uuid) throws IOException {
		removeUuidFromAllTagFiles(uuid);
		List<String> uuidList = new ArrayList<String>();
		uuidList.add(uuid);
		InboxTag tag = new InboxTag("todo", TODOCOLOR, "#000000", uuidList);
		persistTag(tag);
		setChanged();
		notifyObservers(new TagChange(TagChange.SPECIAL_TAG_ADDED, tag));
	}

	private void removeUuidFromAllTagFiles(String uuidToRemove) throws FileNotFoundException, IOException {
		File[] tags = tagFolder.listFiles();
		for (File file : tags) {
			if (!file.getName().equals("outbox.tag") && !file.isHidden()) {
				FileInputStream ifis = new FileInputStream(file);
				InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
				BufferedReader br = new BufferedReader(iisr);
				String tagHeader = br.readLine();
				List<String> uuidList = new ArrayList<String>();
				while (br.ready()) {
					String uuid = br.readLine();
					uuidList.add(uuid);
				}
				br.close();
				PrintWriter pw = new PrintWriter(file);
				pw.println(tagHeader);
				if (uuidList.contains(uuidToRemove)) {
					uuidList.remove(uuidToRemove);
				}
				for (String uuid : uuidList) {
					pw.println(uuid);
				}
			}
		}
	}

	public List<InboxTag> getTagNames() throws IOException {
		List<InboxTag> result = null;
		File[] tags = tagFolder.listFiles();
		if (tags.length > 0) {
			result = new ArrayList<InboxTag>();
		} else {
			return result;
		}
		for (File file : tags) {
			if (!file.isHidden()) {
				FileInputStream ifis = new FileInputStream(file);
				InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
				BufferedReader br = new BufferedReader(iisr);
				String tagHeader = br.readLine();
				InboxTag tag = new InboxTag(getName(tagHeader), getHtmlColor(getColorFromHeader(tagHeader)), getHtmlColor(getTextColorFromHeader(tagHeader)), null);
				if (!result.contains(tag)) {
					result.add(tag);
				}
				br.close();
			}
		}
		return result;
	}

	public void tag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		setChanged();
		notifyObservers(new TagChange(TagChange.ITEM_TAGGED, result));
	}

	public void createSpecialTag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		setChanged();
		notifyObservers(new TagChange(TagChange.SPECIAL_TAG_ADDED, result));
	}

	public void createTag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		setChanged();
		notifyObservers(new TagChange(TagChange.NEW_TAG_ADDED, result));
	}

	public InboxTag persistTag(InboxTag tag) throws IOException {
		File tagFile = new File(tagFolder, tag.getTagName() + ".tag");
		String header = "";
		List<String> uuidList = new ArrayList<String>();
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			while (reader.ready()) {
				String uuid = reader.readLine();
				uuidList.add(uuid);
			}
			reader.close();
		} else {
			header = getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
		}
		uuidList.addAll(tag.getUuidList());
		PrintWriter pw = new PrintWriter(tagFile);
		pw.println(header);
		for (String uuid : uuidList) {
			pw.println(uuid);
		}
		pw.flush();
		pw.close();
		InboxTag nameColorTag = new InboxTag(tag.getTagName(), tag.getColor(), tag.getTextColor(), null);
		if (nameColorCache.contains(nameColorTag)) {
			nameColorCache.add(nameColorTag);
		}
		tag.setUuidList(uuidList);
		return tag;
	}

	public void removeTag(InboxTag tag, String uuidToRemove) throws IOException {
		File tagFile = new File(tagFolder, tag.getTagName() + ".tag");
		String header = "";
		List<String> uuidList = new ArrayList<String>();
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			while (reader.ready()) {
				String uuid = reader.readLine();
				if (!uuid.equals(uuidToRemove)) {
					uuidList.add(uuid);
				}
			}
			reader.close();
		}
		if (uuidList.isEmpty()) {
			tagFile.delete();
			InboxTag nameColorTag = new InboxTag(tag.getTagName(), tag.getColor(), tag.getTextColor(), null);
			if (nameColorCache.contains(nameColorTag)) {
				nameColorCache.remove(nameColorTag);
			}
		} else {
			PrintWriter pw = new PrintWriter(tagFile);
			pw.println(header);
			for (String uuid : uuidList) {
				pw.println(uuid);
			}
			pw.flush();
			pw.close();
		}
		tag.setUuidList(uuidList);
		setChanged();
		notifyObservers(new TagChange(TagChange.TAG_REMOVED, tag));
	}

	public HashMap<String, List<String>> getTagContent(String tagName) throws IOException {
		HashMap<String, List<String>> result = null;
		File[] tags = tagFolder.listFiles();
		if (tags.length > 0) {
			result = new HashMap<String, List<String>>();
		} else {
			return result;
		}
		for (File file : tags) {
			if (file.getName().equals(tagName + ".tag")) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String tagHeader = br.readLine();
				List<String> uuidList = new ArrayList<String>();
				while (br.ready()) {
					String uuid = br.readLine();
					uuidList.add(uuid);
				}
				result.put(tagHeader, uuidList);
				br.close();
				break;
			}
		}
		return result;
	}

	public static TagManager getInstance() {
		if (instance == null) {
			instance = new TagManager();
			return instance;
		}
		return instance;
	}

	public String getHtmlColor(Color c) {
		if (c != null) {
			return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
		} else {
			return "";
		}
	}

	public String getHeader(InboxTag tag) {
		return getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
	}

	public String getHeader(String name, String color, String textColor) {
		String result = tagHtml.replace("${TAGNAME}", name);
		result = result.replace("${COLOR}", color);
		result = result.replace("${TEXT_COLOR}", textColor);
		return result;
	}

	public String getHeader(String name, Color color, Color textColor) {
		String result = tagHtml.replace("${TAGNAME}", name);
		result = result.replace("${COLOR}", getHtmlColor(color));
		result = result.replace("${TEXT_COLOR", getHtmlColor(textColor));
		return result;
	}

	private Color getColorFromHeader(String tagHeader) {
		String colorTxt = tagHeader.split("background-color:")[1];
		String color = colorTxt.split(";")[0];
		StyleSheet ss = new StyleSheet();
		return ss.stringToColor(color);
	}

	private Color getTextColorFromHeader(String tagHeader) {
		String colorTxt = tagHeader.split(";color:")[1];
		String color = colorTxt.split(";")[0];
		StyleSheet ss = new StyleSheet();
		return ss.stringToColor(color);
	}

	private String getName(String tagHeader) {
		return tagHeader.replaceAll("\\<[^>]*>", "");
	}

}
