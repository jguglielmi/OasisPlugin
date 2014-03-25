package org.oasis.plugin;


import org.oasis.plugin.responders.SikuliResponder;

import fitnesse.responders.ResponderFactory;
import fitnesse.wikitext.parser.SymbolProvider;

public class PluginLoader {

    public static void registerResponders(ResponderFactory factory) {
    	//factory.addResponder("custom1", WikiPageResponder.class);
    	//factory.addResponder("custom2", EditResponder.class);
    	System.out.println("************ registerResponders ************");
    	//System.out.println("port: " + fitnesse.testutil.FitNesseUtil.PORT + "");
    	//System.out.println(fitnesse.testutil.FitNesseUtil.makeTestContext().getRootPagePath());
    	GlobalEnv.IS_FITNESSE_PROC.set(true);
    	FitnesseCustomStartup.addCustomFitnesseStartup(GlobalEnv.getFitnessePort(), new FitnesseCustomStartup.fitnesseEvents() {
			@Override
			public void startup(int port) {
				System.out.println("#### port: " + port);
		        // Add to FitNesse to initialize some sikuli stuff
		        SikuliResponder.loadProperties();
		        SikuliResponder.startupCommand();
		        SikuliResponder.setHotKey();
			}
		});
    }

    public static void registerSymbolTypes(SymbolProvider provider) {
    	//provider.add(new Today());
    	//System.out.println("************ registerSymbolTypes ************");
    }

}
