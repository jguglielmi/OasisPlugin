//Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
//Released under the terms of the CPL Common Public License version 1.0.

/*
 
 // For this custom Script table to work we must be able to overwrite current script table in the class fitnesse.testsystems.slim.tables.SlimTableFactory
 // And need to add the script table to the plugin.properties file SlimTables=script:fitnesse.testsystems.slim.tables.OasisScriptTable
 
  public void addTableType(String nameOrPrefix, Class<? extends SlimTable> tableClass) {
    if (tableTypes.get(nameOrPrefix) != null) {
      //throw new IllegalStateException("A table type named '" + nameOrPrefix + "' already exists");
      //replace old table with new one
      System.out.println("*** Warning replacing existing table type named '" + nameOrPrefix + "'");
      tableTypes.remove(nameOrPrefix);
    }
    tableTypes.put(nameOrPrefix.toLowerCase(), tableClass);
  }
 */

package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.*;

import java.util.Arrays;

public class OasisScriptTable extends ScriptTable {
private static final String SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX = ";";

public OasisScriptTable(Table table, String tableId, SlimTestContext context) {
 super(table, tableId, context);
}

protected String getTableType() {
 return "scriptTable";
}

/**
* Template method to provide the keyword that identifies the table type.
*/
protected String getTableKeyword() {
 return "script";
}

/**
* Template method to provide the keyword for the {@code start} action.
*/
protected String getStartKeyword() {
 return "start";
}

/**
* Template method to provide the keyword for the {@code check} action.
*/
protected String getCheckKeyword() {
 return "check";
}

/**
* Template method to provide the keyword for the {@code checkNot} action.
*/
protected String getCheckNotKeyword() {
 return "check not";
}

/**
* Template method to provide the keyword for the {@code reject} action.
*/
protected String getRejectKeyword() {
 return "reject";
}

/**
* Template method to provide the keyword for the {@code ensure} action.
*/
protected String getEnsureKeyword() {
 return "ensure";
}

/**
* Template method to provide the keyword for the {@code show} action.
*/
protected String getShowKeyword() {
 return "show";
}

/**
* Template method to provide the keyword for the {@code note} action.
*/
protected String getNoteKeyword() {
 return "note";
}

public List<SlimAssertion> getAssertions() throws SyntaxError {
 int rows = table.getRowCount();
 List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
 if (isScript() && table.getColumnCountInRow(0) > 1)
   assertions.addAll(startActor(0));
 for (int row = 1; row < rows; row++)
   assertions.addAll(instructionsForRow(row));
 return assertions;
}

private boolean isScript() {
 return getTableKeyword().equalsIgnoreCase(table.getCellContents(0, 0));
}

// returns a list of statements
protected List<SlimAssertion> instructionsForRow(int row) throws SyntaxError {
 String firstCell = table.getCellContents(0, row).trim();
 List<SlimAssertion> assertions;
 String match;
 if (firstCell.equalsIgnoreCase(getStartKeyword()))
   assertions = startActor(row);
 else if (firstCell.equalsIgnoreCase(getCheckKeyword()))
   assertions = checkAction(row);
 else if (firstCell.equalsIgnoreCase(getCheckNotKeyword()))
   assertions = checkNotAction(row);
 else if (firstCell.equalsIgnoreCase(getRejectKeyword()))
   assertions = reject(row);
 else if (firstCell.equalsIgnoreCase(getEnsureKeyword()))
   assertions = ensure(row);
 else if (firstCell.equalsIgnoreCase(getShowKeyword()))
   assertions = show(row);
 else if (firstCell.equalsIgnoreCase(getRepeatKeyword()))
     assertions = repeat(row);    
 else if (firstCell.equalsIgnoreCase(getNoteKeyword()))
   assertions = note(row);
 else if ((match = ifSymbolAssignment(0, row)) != null)
   assertions = actionAndAssign(match, row);
 else if (firstCell.length() == 0)
   assertions = note(row);
 else if (firstCell.trim().startsWith("#") || firstCell.trim().startsWith("*"))
   assertions = note(row);
 else {
   // action() is for now the only function that returns a list of statements
   assertions = action(row);
 }
 return assertions;
}

//added loop functions
protected String getRepeatKeyword() {
	return "repeat";
}

//should pickup this syntax: | repeat | command(s) | 100 |
protected List<SlimAssertion> repeat(int row) throws SyntaxError {
	List<SlimAssertion> results = new ArrayList<SlimAssertion>();
	int lastCol = table.getColumnCountInRow(row) - 1;
	int repeat = 1;
	try {
	  repeat = Integer.parseInt(table.getCellContents(lastCol, row));
	} catch (Exception e) {
	  e.printStackTrace();
	}
	
	for (int i = 0; i < repeat; i++)
	{
		//results.addAll(invokeAction(1, lastCol, row, new ShowActionExpectation(0, row)));
		
		List<SlimAssertion> assertions = assertionsFromScenario(1, lastCol - 1, row); //process like a scenario first
		if (assertions.isEmpty()) //if not scenario try to run as normal fixture command
			results.addAll(invokeAction(1, lastCol - 1, row, new ShowActionExpectation(0, row)));
		else
			results.addAll(assertions);
		
	}
	return results;
}

private void fixScenarioTableRepeat(Table scenarioTable, String[] args, Set<String> inputs)
{
   String[] inputArray = new String[args.length];
   inputs.toArray(inputArray);
   //for (int i = 0 ; i < args.length ; i ++)
      //System.out.println("scenario args " + args[i] + " " + inputArray[i]);
    
   for (int r = 0 ; r < scenarioTable.getRowCount() ; r++)
   {
	   //System.out.println("scenario contents " + scenarioTable.getCellContents(0, r));
	   if (scenarioTable.getCellContents(0, r).toLowerCase().equals("repeat")) {
		   String repeats = scenarioTable.getCellContents(scenarioTable.getColumnCountInRow(r)-1, r);
		   if (repeats.contains("@")) // replace with input
		   {
			   //repeats = "1";
			   for (int i = 0 ; i < args.length ; i ++)
			   {
				   if (inputArray[i].equals(repeats.replaceAll("@", "")))
						   repeats = args[i];
			   }
			   //System.out.println("scenario args " + args[i] + " " + inputArray[i]);
		   }
		   int repeatCount = Integer.parseInt(repeats);
		   //System.out.println("repeats " + repeatCount);
		   scenarioTable.substitute(0, r, "note"); // substitute note for repeat since scenario table doesn't understand the "repeat" command
		   
		   List <String> repeatRowList = new ArrayList<String>();
		   for (int c = 1 ; c < scenarioTable.getColumnCountInRow(r) - 1 ; c ++)
			   repeatRowList.add(scenarioTable.getCellContents(c, r));
		   
		   for (int x = 0 ; x < repeatCount ; x ++) //append repeated rows to scenario table
		   {
			   scenarioTable.addRow(repeatRowList);
		   }
	   }
   }
}

@SuppressWarnings({ "unchecked", "null" })
private List<SlimAssertion> assertionsFromScenario(int startingCol, int endingCol, int row) throws SyntaxError {
 //int lastCol = table.getColumnCountInRow(row) - 1;
 String actionName = getActionNameStartingAt(startingCol + 0, endingCol, row);
 ScenarioTable scenario = getTestContext().getScenario(Disgracer.disgraceClassName(actionName));
 List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
 if (scenario != null) {
   //String firstNameCell = scenario.getTable().getCellContents(startingCol + 0, row);
   
   String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row, assertions);
   fixScenarioTableRepeat(scenario.getTable(), args, scenario.getInputs());
   /*
   Map<String, String> scenarioArguments = new HashMap<String, String>();
   for (int i = 0; (i < scenario.getInputs().size()) && (i < args.length); i++)
     scenarioArguments.put(((Map<String, String>) scenario.getInputs()).get(i), args[i]);
   assertions.addAll(scenario.call(scenarioArguments, this, row));
   */
   assertions.addAll(scenario.call(args, this, row));
   
 } else if (endingCol == 0) {
   String firstNameCell = table.getCellContents(startingCol + 0, row);
   for (ScenarioTable s : getScenariosWithMostArgumentsFirst()) {
     String[] args = s.matchParameters(firstNameCell);
     if (args != null) {
    	 assertions.addAll(scenario.call(args, this, row));
       break;
     }
   }
 }
 return assertions;
}

protected List<SlimAssertion> actionAndAssign(String symbolName, int row) {
 List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
 int lastCol = table.getColumnCountInRow(row) - 1;
 String actionName = getActionNameStartingAt(1, lastCol, row);
 if (!actionName.equals("")) {
   String[] args = getArgumentsStartingAt(1 + 1, lastCol, row, assertions);
   assertions.add(makeAssertion(callAndAssign(symbolName, getTableType() + "Actor", actionName, args),
           new SymbolAssignmentExpectation(symbolName, 0, row)));
 }
 return assertions;
}

protected List<SlimAssertion> action(int row) throws SyntaxError {
 List<SlimAssertion> assertions = assertionsFromScenario(row);
 if (assertions.isEmpty()) {
   // Invoke fixture:
   int lastCol = table.getColumnCountInRow(row) - 1;
   String actionName = getActionNameStartingAt(0, lastCol, row);
   String[] args = getArgumentsStartingAt(1, lastCol, row, assertions);
   assertions.add(makeAssertion(callFunction(getTableType() + "Actor", actionName, (Object[]) args),
           new ScriptActionExpectation(0, row)));
 }
 return assertions;
}

protected List<SlimAssertion> assertionsFromScenario(int row) throws SyntaxError {
 int lastCol = table.getColumnCountInRow(row) - 1;
 String actionName = getActionNameStartingAt(0, lastCol, row);
 ScenarioTable scenario = getTestContext().getScenario(Disgracer.disgraceClassName(actionName));
 List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
 if (scenario != null) {
   //System.out.println("assertionsFromScenario1 (" + row + ") " + scenario.getName());
   scenario.setCustomComparatorRegistry(customComparatorRegistry);
   String[] args = getArgumentsStartingAt(1, lastCol, row, assertions);
   //System.out.println("args (" + args.length + ") " + args[0]);
   //System.out.println("assertions (" + assertions.size() + ") ");
   Table scenarioTable = scenario.getTable();
   fixScenarioTableRepeat(scenarioTable, args, scenario.getInputs());
   assertions.addAll(scenario.call(args, this, row));
   //for (int i = 0 ; i < assertions.size() ; i ++)
   //  System.out.println("assertions (" + i + ") "+ assertions.get(i).getInstruction().toString() + " & " + assertions.get(i).getExpectation());
 } else if (lastCol == 0) {
	 //System.out.println("assertionsFromScenario2 (" + row + ") " + scenario.getName());
   String firstNameCell = table.getCellContents(0, row);
   for (ScenarioTable s : getScenariosWithMostArgumentsFirst()) {
     s.setCustomComparatorRegistry(customComparatorRegistry);
     String[] args = s.matchParameters(firstNameCell);
     if (args != null) {
       assertions.addAll(s.call(args, this, row));
       break;
     }
   }
 }
 return assertions;
}

private List<ScenarioTable> getScenariosWithMostArgumentsFirst() {
 Collection<ScenarioTable> scenarioMap = getTestContext().getScenarios();
 List<ScenarioTable> scenarios = new ArrayList<ScenarioTable>(scenarioMap);
 Collections.sort(scenarios, new ScenarioTableLengthComparator());
 return scenarios;
}

private static class ScenarioTableLengthComparator implements java.util.Comparator<ScenarioTable> {
 public int compare(ScenarioTable st1, ScenarioTable st2) {
   int size1 = st1.getInputs().size();
   int size2 = st2.getInputs().size();
   return size2 - size1;
 }
}


protected List<SlimAssertion> note(int row) {
 return Collections.emptyList();
}

protected List<SlimAssertion> show(int row) {
 int lastCol = table.getColumnCountInRow(row) - 1;
 return invokeAction(1, lastCol, row,
         new ShowActionExpectation(0, row));
}

protected List<SlimAssertion> ensure(int row) {
 int lastCol = table.getColumnCountInRow(row) - 1;
 return invokeAction(1, lastCol, row,
         new EnsureActionExpectation(0, row));
}

protected List<SlimAssertion> reject(int row) {
 int lastCol = table.getColumnCountInRow(row) - 1;
 return invokeAction(1, lastCol, row,
         new RejectActionExpectation(0, row));

}

protected List<SlimAssertion> checkAction(int row) {
 int lastColInAction = table.getColumnCountInRow(row) - 1;
 table.getCellContents(lastColInAction, row);
 return invokeAction(1, lastColInAction - 1, row,
         new ReturnedValueExpectation(lastColInAction, row));
}

protected List<SlimAssertion> checkNotAction(int row) {
 int lastColInAction = table.getColumnCountInRow(row) - 1;
 table.getCellContents(lastColInAction, row);
 return invokeAction(1, lastColInAction - 1, row,
         new RejectedValueExpectation(lastColInAction, row));
}

protected List<SlimAssertion> invokeAction(int startingCol, int endingCol, int row, SlimExpectation expectation) {
 String actionName = getActionNameStartingAt(startingCol, endingCol, row);
 List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
 String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row, assertions);
 assertions.add(makeAssertion(callFunction(getTableType() + "Actor", actionName, (Object[]) args),
         expectation));
 return assertions;
}

protected String getActionNameStartingAt(int startingCol, int endingCol, int row) {
 StringBuffer actionName = new StringBuffer();
 actionName.append(table.getCellContents(startingCol, row));
 int actionNameCol = startingCol + 2;
 while (actionNameCol <= endingCol &&
 !invokesSequentialArgumentProcessing(actionName.toString())) {
   actionName.append(" ").append(table.getCellContents(actionNameCol, row));
   actionNameCol += 2;
 }
 return actionName.toString().trim();
}

// Adds extra assertions to the "assertions" list!
protected String[] getArgumentsStartingAt(int startingCol, int endingCol, int row, List<SlimAssertion> assertions) {
 ArgumentExtractor extractor = new ArgumentExtractor(startingCol, endingCol, row);
 while (extractor.hasMoreToExtract()) {
   assertions.add(makeAssertion(Instruction.NOOP_INSTRUCTION,
           new ArgumentExpectation(extractor.argumentColumn, row)));
   extractor.extractNextArgument();
 }
 return extractor.getArguments();
}

protected boolean invokesSequentialArgumentProcessing(String cellContents) {
 return cellContents.endsWith(SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX);
}

protected List<SlimAssertion> startActor(int row) {
 int classNameColumn = 1;
 String cellContents = table.getCellContents(classNameColumn, row);
 String className = Disgracer.disgraceClassName(cellContents);
 List<SlimAssertion> actor = new ArrayList<SlimAssertion>();
actor.add(constructInstance(getTableType() + "Actor", className, classNameColumn, row));
 return actor;
}

class ArgumentExtractor {
 private int argumentColumn;
 private int endingCol;
 private int row;

 private List<String> arguments = new ArrayList<String>();
 private int increment = 2;
 private boolean sequentialArguments = false;

 ArgumentExtractor(int startingCol, int endingCol, int row) {
   this.argumentColumn = startingCol;
   this.endingCol = endingCol;
   this.row = row;
 }

 public boolean hasMoreToExtract() {
   return argumentColumn <= endingCol;
 }

 public void extractNextArgument() {
   arguments.add(table.getCellContents(argumentColumn, row));
   String argumentKeyword = table.getCellContents(argumentColumn - 1, row);
   boolean argumentIsSequential = invokesSequentialArgumentProcessing(argumentKeyword);
   sequentialArguments = (sequentialArguments || argumentIsSequential);
   increment = sequentialArguments ? 1 : 2;
   argumentColumn += increment;
 }

 public String[] getArguments() {
   return arguments.toArray(new String[arguments.size()]);
 }
}

private class ScriptActionExpectation extends RowExpectation {
 private ScriptActionExpectation(int col, int row) {
   super(col, row);
 }

 @Override
 protected SlimTestResult createEvaluationMessage(String actual, String expected) {
   if (actual == null)
     return SlimTestResult.fail("null", expected);
   else if (actual.equals(VoidConverter.VOID_TAG) || actual.equals("null"))
     return SlimTestResult.plain();
   else if (actual.equals(BooleanConverter.FALSE))
     return SlimTestResult.fail();
   else if (actual.equals(BooleanConverter.TRUE))
     return SlimTestResult.pass();
   else
     return SlimTestResult.plain();
 }
}

private class EnsureActionExpectation extends RowExpectation {
 public EnsureActionExpectation(int col, int row) {
   super(col, row);
 }

 @Override
 protected SlimTestResult createEvaluationMessage(String actual, String expected) {
   return (actual != null && actual.equals(BooleanConverter.TRUE)) ?
           SlimTestResult.pass() : SlimTestResult.fail();
 }
}

private class RejectActionExpectation extends RowExpectation {
 public RejectActionExpectation(int col, int row) {
   super(col, row);
 }

 @Override
 protected SlimTestResult createEvaluationMessage(String actual, String expected) {
   if (actual == null)
     return SlimTestResult.pass();
   else
     return actual.equals(BooleanConverter.FALSE) ? SlimTestResult.pass() : SlimTestResult.fail();
 }
}

private class ShowActionExpectation extends RowExpectation {
 public ShowActionExpectation(int col, int row) {
   super(col, row);
 }

 @Override
 protected SlimTestResult createEvaluationMessage(String actual, String expected) {
   try {
     table.addColumnToRow(getRow(), actual);
   } catch (Throwable e) {
     return SlimTestResult.fail(actual, e.getMessage());
   }
   return SlimTestResult.plain();
 }
}

private class ArgumentExpectation extends RowExpectation {

 private ArgumentExpectation(int col, int row) {
   super(col, row);
 }

 @Override
 public TestResult evaluateExpectation(Object returnValue) {
   table.substitute(getCol(), getRow(), replaceSymbolsWithFullExpansion(getExpected()));
   return null;
 }

 @Override
 protected SlimTestResult createEvaluationMessage(String actual, String expected) {
   return null;
 }
}
}
