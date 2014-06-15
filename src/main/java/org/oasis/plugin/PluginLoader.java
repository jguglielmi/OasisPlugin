package org.oasis.plugin;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.oasis.plugin.responders.SikuliResponder;

import fitnesse.responders.ResponderFactory;
import fitnesse.testsystems.slim.HtmlTable;
import fitnesse.testsystems.slim.tables.ScriptTable;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.SymbolProvider;

public class PluginLoader {
	
	public static void registerSlimTables() {
		System.out.println("************ registerSlimTables ************");
    	fitnesse.testsystems.slim.tables.SlimTableFactory stf = new SlimTableFactory();
    	//hack to remove old script table..
    	try {
			//myTableTypes.put("script", OasisScriptTable.class);
			//stf.addTableType("script", OasisScriptTable.class);
			//stf.addTableType("script:", OasisScriptTable.class);
			//System.out.println("slim table count: " + myTableTypes.size());
			//System.out.println("scriptTable Class: " + fitnesse.testsystems.slim.tables.SlimTableFactory.tableTypes.get("script").toString());
			//System.out.println("oasisScriptTable Class: " + myTableTypes.get("oasis").toString());
			
			//setFinalStatic(tableTypesField, myTableTypes); //this is an evil hack until fitnesse is extended
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void registerResponders(ResponderFactory factory) {

    	//insert new script table
    	//stf.addTableType("script", OasisScriptTable.class);
    	//factory.addResponder("custom1", WikiPageResponder.class);
    	//factory.addResponder("custom2", EditResponder.class);
    	System.out.println("************ registerResponders ************");
    	//System.out.println("port: " + fitnesse.testutil.FitNesseUtil.PORT + "");
    	//System.out.println(fitnesse.testutil.FitNesseUtil.makeTestContext().getRootPagePath());
    	GlobalEnv.IS_FITNESSE_PROC.set(true);
    	FitnesseCustomStartup.addCustomFitnesseStartup(GlobalEnv.getFitnessePort(), new FitnesseCustomStartup.fitnesseEvents() {
			public void startup(int port) {
				System.out.println("#### port: " + port);
		        // Add to FitNesse to initialize some sikuli stuff
		        SikuliResponder.loadProperties();
		        SikuliResponder.startupCommand();
		        SikuliResponder.setHotKey();
		        registerSlimTables();
			}
		});
    }

    public static void registerSymbolTypes(SymbolProvider provider) {
	    provider.add(new OasisSymbolType());
    	//provider.add(new Today());
    	//System.out.println("************ registerSymbolTypes ************");
    }

}
