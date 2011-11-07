package com.termmed.genid;

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

	public static void main(String[] args) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		System.out.println(getNewSNOMEDID("G-FFFF", sqlSession));
	}

	public static String getNewCTV3ID(SqlSession session) {
		String lcode = (String) session.selectOne("com.termmed.genid.data.IDBaseMapper.selectIdBase");
		Long decimalCode = BaseConverterUtil.fromBase62(lcode);
		decimalCode++;
		lcode = BaseConverterUtil.toBase62(decimalCode);
		session.update("com.termmed.genid.data.IDBaseMapper.updateIdBase", lcode);
		return lcode;
	}

	public static String getNewSNOMEDID(String parentSnomedId, SqlSession session) {
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
			} else {
				logger.debug("Parent snomed id:" + parentSnomedId);
				pos = parentSnomedId.indexOf('-');
				tempParent = parentSnomedId;
				prefix = parentSnomedId.split("-")[0];
				suffix = parentSnomedId.split("-")[1];
				suffixToNum = Integer.parseInt(suffix, 16);
				logger.debug("Prefix: " + prefix);
				if (prefix.equals("G")) {
					lenSuffix = 4;
				} else {
					lenSuffix = 5;
				}

				int digits = 1;
				String endId = parentSnomedId.substring(0, parentSnomedId.length() - 1) + "F";
				String startId = parentSnomedId.substring(0, parentSnomedId.length() - 1) + "0";
				logger.debug("End Id: " + endId);
				SnomedIdRange idRange = new SnomedIdRange(startId, endId);
				logger.debug("Id Range" + idRange);

				Integer total = (Integer) session.selectOne("com.termmed.genid.data.ConidMapMapper.countBySnomedId", idRange);
				logger.debug("Count By Snomed id total: " + total);
				Integer number = Integer.parseInt(startId.substring(parentSnomedId.length() - digits), 16);
				logger.debug("Number: " + number);
				logger.debug("POW: " + (total == Math.pow(16d, digits)));
				while (total == Math.pow(16d, digits) - number && digits < lenSuffix) {
					digits++;

					String fs = "";
					for (int i = 1; i <= digits; i++) {
						fs = fs + "F";
					}
					endId = parentSnomedId.substring(0, parentSnomedId.length() - digits) + fs;

					String ss = "";
					for (int i = 1; i <= digits; i++) {
						ss = ss + "0";
					}
					startId = parentSnomedId.substring(0, parentSnomedId.length() - digits) + ss;

					idRange.setEnd(endId);
					idRange.setStart(startId);

					logger.debug("ID RANGE: " + idRange);
					total = (Integer) session.selectOne("com.termmed.genid.data.ConidMapMapper.countBySnomedId", idRange);
					logger.debug("Count By Snomed id total: " + total);
					number = Integer.parseInt(parentSnomedId.substring(startId.length() - digits), 16);
					logger.debug("Number: " + number);
				}

				List<String> lista = session.selectList("com.termmed.genid.data.ConidMapMapper.selectSnomedIdList", idRange);
				if (lista != null && !lista.isEmpty()) {
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
			}
		}
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
				}else if(lenSuffix == 4 && lista.get(0).endsWith("FFFF")){
					return --antNum;
				}else if(lenSuffix == 5 && lista.get(0).endsWith("FFFFF")) {
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
					if(lenSuffix == 4 && lastSuffix.equalsIgnoreCase("ffff")){
						return --firstNum;
					}else if(lenSuffix == 5 && lastSuffix.equalsIgnoreCase("fffff")){
						return --firstNum;
					}else if(lenSuffix == 4 && !lastSuffix.equalsIgnoreCase("ffff")){
						return ++lastNum;
					}else if(lenSuffix == 5 && !lastSuffix.equalsIgnoreCase("fffff")){
						return --lastNum;
					}
				} else {
					antNum = firstNum;
					String actSuffix = "";
					lista.remove(0);
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
