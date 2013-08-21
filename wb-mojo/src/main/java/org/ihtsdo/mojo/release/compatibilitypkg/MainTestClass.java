package org.ihtsdo.mojo.release.compatibilitypkg;

import org.ihtsdo.rf2.util.I_amFilter;

public class MainTestClass {

	public static void main(String[] args){
		try {
			Class test=Class.forName("org.ihtsdo.rf2.util.ModuleFilter");
			I_amFilter t= (I_amFilter) test.newInstance();
			
			System.out.println( t.toString());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
