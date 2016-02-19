package jjdevine.epos.startup;

import jjdevine.epos.EposContext;

public class EposStartup {


	public static void main(String[] args) throws ClassNotFoundException 
	{
		EposContext.getTransactionController().init();
	}

}
