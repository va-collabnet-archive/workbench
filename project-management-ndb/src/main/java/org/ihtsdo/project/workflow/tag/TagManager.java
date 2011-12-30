package org.ihtsdo.project.workflow.tag;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.text.html.StyleSheet;

import org.dwfa.ace.config.AceConfig;

public class TagManager {
	private static final String TODOCOLOR = "#FE9A2E";
	private static final String OUTBOXCOLOR = "#3A01DF";
	public static final String NEW_TAG_ADDED = "new tag created";
	public static final String ITEM_TAGGED = "item tagged";
	public static final String SPECIAL_TAG_ADDED = "outbox items changed";

	private static TagManager instance = null;
	private File tagFolder;
	private static final String tagHtml = "<html><body><table \"style=\"table-layout:fixed;\"><tr>" + "<td style=\"" + "background-color:${COLOR}; " + "white-space:nowrap;\">${TAGNAME}<td>"
			+ "<td style=\"white-space:nowrap;\">";
	private List<InboxTag> nameColorCache = new ArrayList<InboxTag>();

	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private InboxTag newTag;
	private InboxTag itemTagged;
	private InboxTag specialTag;

	private TagManager() {
		if (AceConfig.config != null) {
			File userFolder = AceConfig.config.getProfileFile().getParentFile();
			tagFolder = new File(userFolder, "tags");
			tagFolder.mkdir();
			try {
				createTag(new InboxTag("outbox", OUTBOXCOLOR, new ArrayList<String>()));
				createTag(new InboxTag("todo", TODOCOLOR, new ArrayList<String>()));
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
			BufferedReader br = new BufferedReader(new FileReader(file));
			String tagHeader = br.readLine();
			List<String> uuidList = new ArrayList<String>();
			while (br.ready()) {
				String uuid = br.readLine();
				uuidList.add(uuid);
			}
			InboxTag tag = new InboxTag(getName(tagHeader), getHtmlColor(getColorFromHeader(tagHeader)), uuidList);
			result.add(tag);
			br.close();
		}
		return result;
	}

	public void sendToOutbox(String outboxUuid) throws IOException {
		removeUuidFromAllTagFiles(outboxUuid);
		List<String> uuidList = new ArrayList<String>();
		uuidList.add(outboxUuid);
		InboxTag tag = new InboxTag("outbox", OUTBOXCOLOR, uuidList);
		persistTag(tag);
		setSpecialTag(tag);
	}

	public void saveAsToDo(String uuid) throws IOException {
		removeUuidFromAllTagFiles(uuid);
		List<String> uuidList = new ArrayList<String>();
		uuidList.add(uuid);
		InboxTag tag = new InboxTag("todo", TODOCOLOR, uuidList);
		persistTag(tag);
		setSpecialTag(tag);
	}

	private void removeUuidFromAllTagFiles(String uuidToRemove) throws FileNotFoundException, IOException {
		File[] tags = tagFolder.listFiles();
		for (File file : tags) {
			if (!file.getName().equals("outbox.tag")) {
				BufferedReader br = new BufferedReader(new FileReader(file));
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
		if (nameColorCache.isEmpty()) {
			File[] tags = tagFolder.listFiles();
			if (tags.length > 0) {
				result = new ArrayList<InboxTag>();
			} else {
				return result;
			}
			for (File file : tags) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String tagHeader = br.readLine();
				InboxTag tag = new InboxTag(getName(tagHeader), getHtmlColor(getColorFromHeader(tagHeader)), null);
				if (!result.contains(tag)) {
					result.add(tag);
				}
				br.close();
			}
		} else {
			result = nameColorCache;
		}
		return result;
	}

	public void tag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		setItemTagged(result);
	}

	public void createTag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		setNewTag(result);
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
			header = getHeader(tag.getTagName(), tag.getColor());
		}
		uuidList.addAll(tag.getUuidList());
		PrintWriter pw = new PrintWriter(tagFile);
		pw.println(header);
		for (String uuid : uuidList) {
			pw.println(uuid);
		}
		pw.flush();
		pw.close();
		InboxTag nameColorTag = new InboxTag(tag.getTagName(), tag.getColor(), null);
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
		} else {
			header = getHeader(tag.getTagName(), tag.getColor());
		}
		PrintWriter pw = new PrintWriter(tagFile);
		pw.println(header);
		for (String uuid : uuidList) {
			pw.println(uuid);
		}
		pw.flush();
		pw.close();
		InboxTag nameColorTag = new InboxTag(tag.getTagName(), tag.getColor(), null);
		if (!nameColorCache.contains(nameColorTag)) {
			nameColorCache.add(nameColorTag);
		}
		tag.setUuidList(uuidList);
		setItemTagged(tag);
	}

	public void setNewTag(InboxTag createdTag) {
		changes.firePropertyChange(NEW_TAG_ADDED, this.newTag, this.newTag = createdTag);
	}

	public InboxTag getNewTag() {
		return newTag;
	}

	public void setItemTagged(InboxTag itemTagged) {
		changes.firePropertyChange(ITEM_TAGGED, this.itemTagged, this.itemTagged = itemTagged);
	}

	public InboxTag getItemTagged() {
		return itemTagged;
	}

	public void setSpecialTag(InboxTag specialTag) {
		changes.firePropertyChange(SPECIAL_TAG_ADDED, this.specialTag, this.specialTag = specialTag);
	}

	public InboxTag getSpecialTag() {
		return specialTag;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.changes.addPropertyChangeListener(listener);
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
		return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
	}

	public String getHeader(InboxTag tag) {
		return getHeader(tag.getTagName(), tag.getColor());
	}

	public String getHeader(String name, String color) {
		String result = tagHtml.replace("${TAGNAME}", name);
		result = result.replace("${COLOR}", color);
		return result;
	}

	public String getHeader(String name, Color color) {
		String result = tagHtml.replace("${TAGNAME}", name);
		result = result.replace("${COLOR}", getHtmlColor(color));
		return result;
	}

	private Color getColorFromHeader(String tagHeader) {
		String colorTxt = tagHeader.split("background-color:")[1];
		String color = colorTxt.split(";")[0];
		StyleSheet ss = new StyleSheet();
		return ss.stringToColor(color);
	}

	private String getName(String tagHeader) {
		return tagHeader.replaceAll("\\<[^>]*>", "");
	}

}
