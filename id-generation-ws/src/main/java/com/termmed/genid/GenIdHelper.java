package com.termmed.genid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.termmed.genid.data.SnomedIdRange;
import com.termmed.genid.util.MyBatisUtil;

public class GenIdHelper {

	private static final Logger logger = Logger.getLogger(GenIdHelper.class);
	private static int[][] FnF = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	static {
		for (int i = 2; i < 8; i++) {
			for (int j = 0; j < 10; j++) {
				FnF[i][j] = FnF[i - 1][FnF[1][j]];
			}
		}
	}
	private static int[][] Dihedral = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 },
			{ 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 },
			{ 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	private static int[] InverseD5 = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	public static long verhoeffCompute(String idAsString) {
		int check = 0;
		for (int i = idAsString.length() - 1; i >= 0; i--) {
			check = Dihedral[check][FnF[((idAsString.length() - i) % 8)][new Integer(new String(new char[] { idAsString.charAt(i) }))]];

		}
		return InverseD5[check];
	}

	public static String getNewCTV3ID(SqlSession session) {
		String lcode = (String) session.selectOne("com.termmed.genid.data.IDBaseMapper.selectIdBase");
		Long decimalCode = BaseConverterUtil.fromBase62(lcode);
		decimalCode++;
		lcode = BaseConverterUtil.toBase62(decimalCode);
		session.update("com.termmed.genid.data.IDBaseMapper.updateIdBase", lcode);
		return lcode;
	}

	public static void main(String[] args) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		System.out.println(getNewSNOMEDID("P1-06005", sqlSession));
	}

	public static String getNewSNOMEDID(String parentSnomedId, SqlSession session) {
		logger.debug("#################### Get New SNOMEDID Start ############################");
		String result = "";

		String tempParent = "";
		String prefix = "";
		String suffix = "";
		int pos = 0;
		int lenSuffix = 0;
		int suffixToNum = 0;

		if (parentSnomedId != null && !parentSnomedId.trim().equals("")) {
			if (parentSnomedId.contains("M-8") || parentSnomedId.contains("M-9")) {
				logger.debug("M-8/9 parent " + parentSnomedId);
				tempParent = "R-10000";
				prefix = "R";
				suffix = "100000";
				suffixToNum = 65536;
				pos = 2;
				lenSuffix = 5;
				logger.debug("Getting id form id_base.");
				String idbase = (String) session.selectOne("com.termmed.genid.data.IDBaseMapper.selectSnomedIdBase");
				logger.debug("ID_BASE result: " + idbase);
				long hexValue = BaseConverterUtil.fromBase16(idbase);
				hexValue++;
				String incremented = BaseConverterUtil.toBase16(hexValue);
				logger.debug("ID_BASE result incremented: " + incremented);
				session.update("com.termmed.genid.data.IDBaseMapper.updateSnomedIdBase", incremented);
				return incremented;
			} else {
				logger.debug("Parent snomed id:" + parentSnomedId);
				pos = parentSnomedId.indexOf('-');
				tempParent = parentSnomedId;
				prefix = parentSnomedId.split("-")[0];
				suffix = parentSnomedId.split("-")[1];
				suffixToNum = Integer.parseInt(suffix, 16);
				logger.debug("Prefix: " + prefix);
				logger.debug("Suffix: " + suffix);
				logger.debug("suffixToNum: " + suffixToNum);
				if (prefix.equals("G")) {
					lenSuffix = 4;
				} else {
					lenSuffix = 5;
				}
				logger.debug("lenSuffix: " + lenSuffix);

				int digits = 1;
				String endId = prefix + "-";
				for (int i = 0; i < lenSuffix; i++) {
					endId = endId + "F";
				}

				String startId = parentSnomedId;
				logger.debug("End Id: " + endId);
				SnomedIdRange idRange = new SnomedIdRange(parentSnomedId, endId);
				logger.debug("Id Range " + idRange);

				Integer total = (Integer) session.selectOne("com.termmed.genid.data.ConidMapMapper.countBySnomedId", idRange);
				logger.debug("REAL TOTAL: " + total);
				int endDecimal = Integer.parseInt(endId.split("-")[1], 16);
				int startDecimal = Integer.parseInt(parentSnomedId.split("-")[1], 16);
				logger.debug("Full Total: " + (startDecimal - startDecimal));
				int k = 1;
				while (endDecimal - startDecimal == total && k < lenSuffix) {
					startId = parentSnomedId.substring(0, lenSuffix - k);
					for (int j = 0; j < k; j++) {
						startId = startId + "0";
					}
					logger.debug("End Id: " + endId);
					idRange = new SnomedIdRange(startId, endId);
					logger.debug("Id Range " + idRange);
					total = (Integer) session.selectOne("com.termmed.genid.data.ConidMapMapper.countBySnomedId", idRange);
					logger.debug("New REAL TOTAL: " + total);
					endDecimal = Integer.parseInt(endId.split("-")[1], 16);
					startDecimal = Integer.parseInt(startId);
					logger.debug("New Full Total: " + (startDecimal - startDecimal));
					k++;
				}

				int page = 0;
				int startLine = 0;
				int pageLenght = 5000;
				idRange.setPageLenght(pageLenght);
				while (startLine < total) {
					idRange.setStartLine(startLine);
					@SuppressWarnings("unchecked")
					List<String> lista = session.selectList("com.termmed.genid.data.ConidMapMapper.selectSnomedIdList", idRange);
					if (!lista.contains(startId) && page == 0) {
						String[] startSplit = startId.split("-");
						int startNum = Integer.parseInt(startSplit[1], 16);
						startNum--;
						lista.add(startSplit[0] + "-" + Integer.toString(startNum, 16));
						Collections.sort(lista);
					}
					if (lista != null && !lista.isEmpty() && lista.size() < pageLenght) {
						int resultNum = findFirstAvailableNumber(lista, digits, lenSuffix);
						logger.debug("First Avalable Number: " + resultNum);

						char[] newNumArray = new char[lenSuffix];
						for (int i = 0; i < newNumArray.length; i++) {
							newNumArray[i] = '0';
						}
						String tmpNum = Integer.toString(resultNum, 16).toUpperCase();
						int len = lenSuffix;
						int tmpLen = tmpNum.length();
						while (tmpLen > 0) {
							newNumArray[len - 1] = tmpNum.charAt(tmpLen - 1);
							len--;
							tmpLen--;
						}
						result = prefix + "-" + new String(newNumArray);
						logger.debug("RESULT: " + result);
					}
					page++;
					startLine = startLine + pageLenght;
				}
			}
		}
		logger.debug("#################### Get New SNOMEDID End ############################");
		return result;
	}

	public static int findFirstAvailableNumber(List<String> lista, int digits, int lenSuffix) {
		int resultNum = 0;
		int antNum = 0;
		if (lista != null && !lista.isEmpty()) {
			if (lista.size() == 1) {
				String id = lista.get(0);
				String antSuffix = id.split("-")[1];
				antNum = Integer.parseInt(antSuffix, 16);
				if (lenSuffix == 4 && !lista.get(0).endsWith("FFFF")) {
					return ++antNum;
				} else if (lenSuffix == 5 && !lista.get(0).endsWith("FFFFF")) {
					return ++antNum;
				} else if (lenSuffix == 4 && lista.get(0).endsWith("FFFF")) {
					return --antNum;
				} else if (lenSuffix == 5 && lista.get(0).endsWith("FFFFF")) {
					return --antNum;
				}
			} else {
				String firstId = lista.get(0);
				String lastId = lista.get(lista.size() - 1);
				String firstSuffix = firstId.split("-")[1];
				String lastSuffix = lastId.split("-")[1];

				int firstNum = Integer.parseInt(firstSuffix, 16);
				int lastNum = Integer.parseInt(lastSuffix, 16);

				if (lastNum - firstNum == lista.size() + 1) {
					if (lenSuffix == 4 && lastSuffix.equalsIgnoreCase("ffff")) {
						return --firstNum;
					} else if (lenSuffix == 5 && lastSuffix.equalsIgnoreCase("fffff")) {
						return --firstNum;
					} else if (lenSuffix == 4 && !lastSuffix.equalsIgnoreCase("ffff")) {
						return ++lastNum;
					} else if (lenSuffix == 5 && !lastSuffix.equalsIgnoreCase("fffff")) {
						return --lastNum;
					}
				} else {
					antNum = firstNum;
					String actSuffix = "";
					// lista.remove(0);
					for (String string : lista) {
						actSuffix = string.split("-")[1];
						int actNum = Integer.parseInt(actSuffix, 16);
						logger.debug("Actual Number: " + actNum);
						if (actNum - antNum > 1) {
							resultNum = antNum + 1;
							break;
						}
						antNum++;
					}
				}
			}
		}
		if (resultNum == 0) {
			resultNum = antNum + 1;
		}
		return resultNum;
	}

}
