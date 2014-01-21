/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.view.tag;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.StyleSheet;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.ItemTaggedEvent;
import org.ihtsdo.project.view.event.NewTagEvent;
import org.ihtsdo.project.view.event.OutboxContentChangeEvent;
import org.ihtsdo.project.view.event.TagRemovedEvent;
import org.ihtsdo.project.view.event.TodoContentChangeEvent;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class TagManager.
 */
public class TagManager {

	/** The Constant TODO. */
	public static final String TODO = "todo";

	/** The Constant OUTBOX_TEXT_COLOR. */
	public static final String OUTBOX_TEXT_COLOR = "#ffffff";

	/** The Constant OUTBOX. */
	public static final String OUTBOX = "outbox";

	/** The Constant TODOCOLOR. */
	public static final String TODOCOLOR = "#FE9A2E";

	/** The Constant OUTBOXCOLOR. */
	public static final String OUTBOXCOLOR = "#3A01DF";

	/** The instance. */
	private static TagManager instance = null;

	/** The tag folder. */
	private File tagFolder;

	/** The Constant tagHtml. */
	private static final String tagHtml = "<html><body><table style=\"table-layout:fixed;\"><tr>" + "<td style=\"background-color:${COLOR}; "
			+ "white-space:nowrap;color:${TEXT_COLOR};\"><B>${TAGNAME}</B></td>" + "<td style=\"white-space:nowrap;\">";

	/** The name color cache. */
	private List<InboxTag> nameColorCache = new ArrayList<InboxTag>();

	/**
	 * Instantiates a new tag manager.
	 */
	private TagManager() {
		if (AceConfig.config != null) {
			File userFolder = AceConfig.config.getProfileFile().getParentFile();
			File tagBaseFolder = new File("tags");
			tagBaseFolder.mkdir();
			tagFolder = new File(tagBaseFolder.getPath(), userFolder.getName());
			tagFolder.mkdir();
			try {
				createTag(new InboxTag(OUTBOX, OUTBOXCOLOR, OUTBOX_TEXT_COLOR, new ArrayList<String[]>()));
				createTag(new InboxTag(TODO, TODOCOLOR, OUTBOX_TEXT_COLOR, new ArrayList<String[]>()));
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

	public static String[] getTagWorklistConceptUuids(WfProcessInstanceBI instance2) {
		String[] uuid = new String[2];
		uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] = instance2.getWorkList().getUuid().toString();
		uuid[InboxTag.TERM_UUID_INDEX] = instance2.getComponentPrimUuid().toString();
		return uuid;
	}

	/**
	 * Gets the all tags content.
	 * 
	 * @return the all tags content
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public List<InboxTag> getAllTagsContent() throws IOException {
		List<InboxTag> result = null;
		File[] tags = tagFolder.listFiles();
		if (tags.length > 0) {
			result = new ArrayList<InboxTag>();
		} else {
			return result;
		}
		for (File file : tags) {
			String name = file.getName();
			String decodedName = URLDecoder.decode(name, "UTF-8");
			if (!file.isHidden() && !decodedName.contains(".svn") && !decodedName.contains("svn")) {
				FileInputStream ifis = new FileInputStream(file);
				InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
				BufferedReader br = new BufferedReader(iisr);
				String tagHeader = br.readLine();
				List<String[]> uuidList = new ArrayList<String[]>();
				while (br.ready()) {
					String uuid = br.readLine();
					uuidList.add(uuid.split(","));
				}
				br.close();
				InboxTag tag = new InboxTag(getName(tagHeader), getHtmlColor(getColorFromHeader(tagHeader)),
						getHtmlColor(getTextColorFromHeader(tagHeader)), uuidList);
				result.add(tag);
			}
		}
		return result;
	}

	/**
	 * Send to outbox.
	 * 
	 * @param outboxUuid
	 *            the outbox uuid
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void sendToOutbox(String[] outboxUuid) throws IOException {
		removeUuidFromAllTagFiles(outboxUuid);
		List<String[]> uuidList = new ArrayList<String[]>();
		uuidList.add(outboxUuid);
		InboxTag tag = new InboxTag(OUTBOX, OUTBOXCOLOR, OUTBOX_TEXT_COLOR, uuidList);
		InboxTag result = persistTag(tag);
		EventMediator.getInstance().fireEvent(new OutboxContentChangeEvent(result.getUuidList().size()));
	}

	/**
	 * Save as to do.
	 * 
	 * @param uuid
	 *            the uuid
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void saveAsToDo(String[] uuid) throws IOException {
		removeUuidFromAllTagFiles(uuid);
		List<String[]> uuidList = new ArrayList<String[]>();
		uuidList.add(uuid);
		InboxTag tag = new InboxTag(TODO, TODOCOLOR, OUTBOX_TEXT_COLOR, uuidList);
		InboxTag result = persistTag(tag);
		EventMediator.getInstance().fireEvent(new TodoContentChangeEvent(result.getUuidList().size()));
	}

	/**
	 * Removes the uuid from all tag files.
	 * 
	 * @param uuidToRemove
	 *            the uuid to remove
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void removeUuidFromAllTagFiles(String[] uuidToRemove) throws FileNotFoundException, IOException {
		File[] tags = tagFolder.listFiles();
		for (File file : tags) {
			String name = file.getName();
			String decodedName = URLDecoder.decode(name, "UTF-8");
			if (!file.isHidden() && !decodedName.contains(".svn") && !decodedName.contains("svn")) {
				FileInputStream ifis = new FileInputStream(file);
				InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
				BufferedReader br = new BufferedReader(iisr);
				String tagHeader = br.readLine();
				List<String[]> uuidList = new ArrayList<String[]>();
				while (br.ready()) {
					String uuid = br.readLine();
					uuidList.add(uuid.split(","));
				}
				br.close();
				FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				PrintWriter pw = new PrintWriter(osw);
				pw.println(tagHeader);
				List<String[]> tmpUuidList = new ArrayList<String[]>();
				for (String[] strings : uuidList) {
					if (!tagItemEquals(uuidToRemove, strings)) {
						tmpUuidList.add(strings);
					}
				}

				for (String[] uuid : tmpUuidList) {
					pw.print(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + ",");
					pw.println(uuid[InboxTag.TERM_UUID_INDEX]);
				}
				pw.close();
			}
		}
	}

	/**
	 * Checks parameter arrays contains same worlist uuid and conceptUuid
	 * 
	 * @param uuidToRemove
	 * @param strings
	 * @return
	 */
	private boolean tagItemEquals(String[] uuidToRemove, String[] strings) {
		return (strings[InboxTag.TERM_WORKLIST_UUID_INDEX].equals(uuidToRemove[InboxTag.TERM_WORKLIST_UUID_INDEX]) && strings[InboxTag.TERM_UUID_INDEX]
				.equals(uuidToRemove[InboxTag.TERM_UUID_INDEX]));
	}

	/**
	 * Empty outbox tag.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void emptyOutboxTag() throws IOException {
		File tagFile = new File(tagFolder, OUTBOX + ".tag");
		String header = "";
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			reader.close();
		}
		FileOutputStream fos = new FileOutputStream(tagFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter pw = new PrintWriter(osw);
		pw.println(header);
		pw.flush();
		pw.close();
	}

	/**
	 * Gets the tag names.
	 * 
	 * @return the tag names
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public List<InboxTag> getTagNames() throws IOException {
		List<InboxTag> result = null;
		File[] tags = tagFolder.listFiles();
		if (tags.length > 0) {
			result = new ArrayList<InboxTag>();
		} else {
			return result;
		}
		for (File file : tags) {
			String name = file.getName();
			String decodedName = URLDecoder.decode(name, "UTF-8");
			
			if (!file.isHidden() && !name.contains(".svn") && !name.contains("svn")) {
				FileInputStream ifis = new FileInputStream(file);
				InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
				BufferedReader br = new BufferedReader(iisr);
				String tagHeader = br.readLine();
				InboxTag tag = new InboxTag(getName(tagHeader), getHtmlColor(getColorFromHeader(tagHeader)),
						getHtmlColor(getTextColorFromHeader(tagHeader)), null);
				if (!result.contains(tag)) {
					result.add(tag);
				}
				br.close();
			}
		}
		return result;
	}

	/**
	 * Tag.
	 * 
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void tag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		EventMediator.getInstance().fireEvent(new ItemTaggedEvent(result));
	}

	/**
	 * Creates the special tag.
	 * 
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void createSpecialTag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
	}

	/**
	 * Creates the tag.
	 * 
	 * @param tag
	 *            the tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void createTag(InboxTag tag) throws IOException {
		InboxTag result = persistTag(tag);
		EventMediator.getInstance().fireEvent(new NewTagEvent(result));
	}

	/**
	 * Persist tag.
	 * 
	 * @param tag
	 *            the tag
	 * @return the inbox tag
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public InboxTag persistTag(InboxTag tag) throws IOException {
		File tagFile = new File(tagFolder, URLEncoder.encode(tag.getTagName(),"UTF-8") + ".tag");
		String header = "";
		List<String[]> uuidList = new ArrayList<String[]>();
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			while (reader.ready()) {
				String uuid = reader.readLine();
				uuidList.add(uuid.split(","));
			}
			reader.close();
		} else {
			header = getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
		}
		List<String[]> tmpList = new ArrayList<String[]>();
		tmpList.addAll(uuidList);
		for (String[] string : tag.getUuidList()) {
			boolean alreadyContains = false;
			for (String[] string2 : uuidList) {
				if (tagItemEquals(string, string2)) {
					alreadyContains = true;
				}
			}
			if (!alreadyContains) {
				tmpList.add(string);
			}
		}
		FileOutputStream fos = new FileOutputStream(tagFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter pw = new PrintWriter(osw);
		pw.println(header);
		for (String[] uuid : tmpList) {
			pw.print(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + ",");
			pw.println(uuid[InboxTag.TERM_UUID_INDEX]);
		}
		pw.flush();
		pw.close();
		InboxTag nameColorTag = new InboxTag(tag.getTagName(), tag.getColor(), tag.getTextColor(), null);
		if (!nameColorCache.contains(nameColorTag)) {
			nameColorCache.add(nameColorTag);
		}
		tag.setUuidList(tmpList);
		return tag;
	}

	/**
	 * Send back to inbox.
	 * 
	 * @param tag
	 *            the tag
	 * @param wfInstance
	 *            the wf instance
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void sendBackToInbox(InboxTag tag, WfInstance wfInstance) throws IOException {
		File tagFile = new File(tagFolder, URLEncoder.encode(tag.getTagName(),"UTF-8") + ".tag");
		String header = "";
		List<String[]> uuidList = new ArrayList<String[]>();
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			while (reader.ready()) {
				String[] uuid = reader.readLine().split(",");
				if (!uuid.equals(wfInstance.getComponentId().toString())) {
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
			FileOutputStream fos = new FileOutputStream(tagFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter pw = new PrintWriter(osw);
			pw.println(header);
			for (String[] uuid : uuidList) {
				pw.print(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + ",");
				pw.println(uuid[InboxTag.TERM_UUID_INDEX]);
			}
			pw.flush();
			pw.close();
		}
	}

	/**
	 * Removes the tag.
	 * 
	 * @param tag
	 *            the tag
	 * @param uuidToRemove
	 *            the uuid to remove
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public InboxTag removeTag(InboxTag tag, String[] uuidToRemove) throws IOException {
		File tagFile = new File(tagFolder, URLEncoder.encode(tag.getTagName(),"UTF-8") + ".tag");
		String header = "";
		List<String[]> uuidList = new ArrayList<String[]>();
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			while (reader.ready()) {
				String[] uuid = reader.readLine().split(",");
				if (!tagItemEquals(uuid, uuidToRemove)) {
					uuidList.add(uuid);
				}
			}
			reader.close();
		}
		if (uuidList.isEmpty() && !tag.getTagName().equals(OUTBOX) && !tag.getTagName().equals(TODO)) {
			tagFile.delete();
			InboxTag nameColorTag = new InboxTag(tag.getTagName(), tag.getColor(), tag.getTextColor(), null);
			if (nameColorCache.contains(nameColorTag)) {
				nameColorCache.remove(nameColorTag);
			}
		} else {
			FileOutputStream fos = new FileOutputStream(tagFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter pw = new PrintWriter(osw);
			pw.println(header);
			for (String[] uuid : uuidList) {
				pw.print(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + ",");
				pw.println(uuid[InboxTag.TERM_UUID_INDEX]);
			}
			pw.flush();
			pw.close();
		}
		tag.setUuidList(uuidList);
		EventMediator.getInstance().fireEvent(new TagRemovedEvent(tag));
		return tag;
	}

	public InboxTag emptyTag(InboxTag tag) throws IOException {
		File tagFile = new File(tagFolder, tag.getTagName() + ".tag");
		String header = "";
		List<String[]> uuidList = new ArrayList<String[]>();
		if (tagFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(tagFile));
			header = reader.readLine();
			reader.close();
		}
		PrintWriter pw = new PrintWriter(tagFile);
		pw.println(header);
		pw.flush();
		pw.close();
		tag.setUuidList(uuidList);
		return tag;
	}

	/**
	 * Gets the tag content.
	 * 
	 * @param tagName
	 *            the tag name
	 * @return the tag content
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public InboxTag getTagContent(String tagName) throws IOException {
		InboxTag result = null;
		File[] tags = tagFolder.listFiles();
		for (File file : tags) {
			String name = file.getName();
			String decodedName = URLDecoder.decode(name, "UTF-8");
			if (decodedName.equals(tagName + ".tag")) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String tagHeader = br.readLine();
				String color = getHtmlColor(getColorFromHeader(tagHeader));
				String textColor = getHtmlColor(getTextColorFromHeader(tagHeader));
				List<String[]> uuidList = new ArrayList<String[]>();
				while (br.ready()) {
					String[] uuid = br.readLine().split(",");
					uuidList.add(uuid);
				}
				br.close();
				result = new InboxTag(tagName, color, textColor, uuidList);
				break;
			}
		}
		return result;
	}

	/**
	 * Gets the single instance of TagManager.
	 * 
	 * @return single instance of TagManager
	 */
	public static TagManager getInstance() {
		if (instance == null) {
			instance = new TagManager();
			return instance;
		}
		return instance;
	}

	/**
	 * Gets the html color.
	 * 
	 * @param c
	 *            the c
	 * @return the html color
	 */
	public String getHtmlColor(Color c) {
		if (c != null) {
			return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
		} else {
			return "";
		}
	}

	/**
	 * Gets the header.
	 * 
	 * @param tag
	 *            the tag
	 * @return the header
	 */
	public String getHeader(InboxTag tag) {
		return getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
	}

	/**
	 * Gets the header.
	 * 
	 * @param name
	 *            the name
	 * @param color
	 *            the color
	 * @param textColor
	 *            the text color
	 * @return the header
	 */
	public String getHeader(String name, String color, String textColor) {
		String result = tagHtml.replace("${TAGNAME}", name);
		result = result.replace("${COLOR}", color);
		result = result.replace("${TEXT_COLOR}", textColor);
		return result;
	}

	/**
	 * Gets the header.
	 * 
	 * @param name
	 *            the name
	 * @param color
	 *            the color
	 * @param textColor
	 *            the text color
	 * @return the header
	 */
	public String getHeader(String name, Color color, Color textColor) {
		String result = tagHtml.replace("${TAGNAME}", name);
		result = result.replace("${COLOR}", getHtmlColor(color));
		result = result.replace("${TEXT_COLOR", getHtmlColor(textColor));
		return result;
	}

	/**
	 * Gets the color from header.
	 * 
	 * @param tagHeader
	 *            the tag header
	 * @return the color from header
	 */
	private Color getColorFromHeader(String tagHeader) {
		String colorTxt = tagHeader.split("background-color:")[1];
		String color = colorTxt.split(";")[0];
		StyleSheet ss = new StyleSheet();
		return ss.stringToColor(color);
	}

	/**
	 * Gets the text color from header.
	 * 
	 * @param tagHeader
	 *            the tag header
	 * @return the text color from header
	 */
	private Color getTextColorFromHeader(String tagHeader) {
		String colorTxt = tagHeader.split(";color:")[1];
		String color = colorTxt.split(";")[0];
		StyleSheet ss = new StyleSheet();
		return ss.stringToColor(color);
	}

	/**
	 * Gets the name.
	 * 
	 * @param tagHeader
	 *            the tag header
	 * @return the name
	 */
	private String getName(String tagHeader) {
		return tagHeader.replaceAll("\\<[^>]*>", "");
	}

}
