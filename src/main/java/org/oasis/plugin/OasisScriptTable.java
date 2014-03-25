//Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
//Released under the terms of the CPL Common Public License version 1.0.

package org.oasis.plugin;

import static fitnesse.testsystems.slim.tables.ComparatorUtil.approximatelyEqual;
import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.toUpperCase;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TableCell;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.SlimTestContext;
//import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.ScenarioTable;
//import fitnesse.testsystems.slim.tables.ScriptTable;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimExpectation;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SyntaxError;
//import fitnesse.testsystems.slim.tables.SlimTable.RowExpectation;


public class OasisScriptTable extends SlimTable {
  private static final String SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX = ";";
  private CustomComparatorRegistry customComparatorRegistry;
  public OasisScriptTable(Table table, String tableId, SlimTestContext context) {
    super(table, tableId, context);
  }

  protected String getTableType() {
    return "tscriptTable";
  }

  /**
   * Template method to provide the keyword that identifies the table type.
   */
  protected String getTableKeyword() {
    return "tscript";
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
  
  // added loop functions
  protected String getRepeatKeyword() {
	return "repeat";
  }
  
  // should pickup this syntax: | repeat | command(s) | 100 |
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
		List<SlimAssertion> assertions = assertionsFromScenario(1, lastCol - 1, row); //process like a scenario first
		if (assertions.isEmpty()) //if not scenario try to run as normal fixture command
			results.addAll(invokeAction(1, lastCol - 1, row, new ShowActionExpectation(0, row)));
		else
			results.addAll(assertions);
	}
	return results;
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
    int lastCol = table.getColumnCountInRow(row) - 1;
    List<SlimAssertion> assertions = assertionsFromScenario(0, lastCol, row);
    if (assertions.isEmpty()) {
      // Invoke fixture:
      String actionName = getActionNameStartingAt(0, lastCol, row);
      String[] args = getArgumentsStartingAt(1, lastCol, row, assertions);
      assertions.add(makeAssertion(callFunction(getTableType() + "Actor", actionName, (Object[]) args),
              new ScriptActionExpectation(0, row)));
    }
    return assertions;
  }

  @SuppressWarnings({ "unchecked", "null" })
private List<SlimAssertion> assertionsFromScenario(int startingCol, int endingCol, int row) throws SyntaxError {
    //int lastCol = table.getColumnCountInRow(row) - 1;
    String actionName = getActionNameStartingAt(startingCol + 0, endingCol, row);
    ScenarioTable scenario = getTestContext().getScenario(Disgracer.disgraceClassName(actionName));
    List<SlimAssertion> assertions = new ArrayList<SlimAssertion>();
    if (scenario != null) {
      String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row, assertions);
      Map<String, String> scenarioArguments = new HashMap<String, String>();
      for (int i = 0; (i < scenario.getInputs().size()) && (i < args.length); i++)
        scenarioArguments.put(((Map<String, String>) scenario.getInputs()).get(i), args[i]);
      assertions.addAll(scenario.call(scenarioArguments, this, row));
    } else if (endingCol == 0) {
      String firstNameCell = table.getCellContents(startingCol + 0, row);
      for (ScenarioTable s : getScenariosWithMostArgumentsFirst()) {
        String[] args = s.matchParameters(firstNameCell);
        if (args != null) {
            Map<String, String> scenarioArguments = new HashMap<String, String>();
            for (int i = 0; (i < scenario.getInputs().size()) && (i < args.length); i++)
              scenarioArguments.put(((Map<String, String>) scenario.getInputs()).get(i), args[i]);
            assertions.addAll(s.call(scenarioArguments, this, row));
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

  private String getActionNameStartingAt(int startingCol, int endingCol, int row) {
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
  private String[] getArgumentsStartingAt(int startingCol, int endingCol, int row, List<SlimAssertion> assertions) {
    ArgumentExtractor extractor = new ArgumentExtractor(startingCol, endingCol, row);
    while (extractor.hasMoreToExtract()) {
      assertions.add(makeAssertion(Instruction.NOOP_INSTRUCTION,
              new ArgumentExpectation(extractor.argumentColumn, row)));
      extractor.extractNextArgument();
    }
    return extractor.getArguments();
  }

  private boolean invokesSequentialArgumentProcessing(String cellContents) {
    return cellContents.endsWith(SEQUENTIAL_ARGUMENT_PROCESSING_SUFFIX);
  }

  protected List<SlimAssertion> startActor(int row) {
    int classNameColumn = 1;
    String cellContents = table.getCellContents(classNameColumn, row);
    String className = Disgracer.disgraceClassName(cellContents);
    return list(constructInstance(getTableType() + "Actor", className, classNameColumn, row));
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

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  static class Disgracer {
    public boolean capitalizeNextWord;
    public StringBuffer disgracedName;
    private String name;

    public Disgracer(String name) {
      this.name = name;
    }

    public static String disgraceClassName(String name) {
      return new Disgracer(name).disgraceClassNameIfNecessary();
    }

    public static String disgraceMethodName(String name) {
      return new Disgracer(name).disgraceMethodNameIfNecessary();
    }

    private String disgraceMethodNameIfNecessary() {
      if (isGraceful()) {
        return disgraceMethodName();
      } else {
        return name;
      }
    }

    private String disgraceMethodName() {
      capitalizeNextWord = false;
      return disgraceName();
    }

    private String disgraceClassNameIfNecessary() {
      if (nameHasDotsBeforeEnd() || nameHasDollars())
        return name;
      else if (isGraceful()) {
        return disgraceClassName();
      } else {
        return name;
      }
    }

    private boolean nameHasDollars() {
      return name.indexOf("$") != -1;
    }

    private String disgraceClassName() {
      capitalizeNextWord = true;
      return disgraceName();
    }

    private boolean nameHasDotsBeforeEnd() {
      int dotIndex = name.indexOf(".");
      return dotIndex != -1 && dotIndex != name.length() - 1;
    }

    private String disgraceName() {
      disgracedName = new StringBuffer();
      for (char c : name.toCharArray())
        appendCharInProperCase(c);

      return disgracedName.toString();
    }

    private void appendCharInProperCase(char c) {
      if (isGraceful(c)) {
        capitalizeNextWord = true;
      } else {
        appendProperlyCapitalized(c);
      }
    }

    private void appendProperlyCapitalized(char c) {
      disgracedName.append(capitalizeNextWord ? toUpperCase(c) : c);
      capitalizeNextWord = false;
    }

    private boolean isGraceful() {
      boolean isGraceful = false;
      for (char c : name.toCharArray()) {
        if (isGraceful(c))
          isGraceful = true;
      }
      return isGraceful;
    }

    private boolean isGraceful(char c) {
      return !(isLetterOrDigit(c) || c == '_');
    }
  }

  /** SlimExpectation base class for row based expectations. */
  public abstract class RowExpectation implements SlimExpectation, TableCell {
    private final int col;
    private final int row;
    private final String originalContent;

    public RowExpectation(int col, int row) {
      this(col, row, col >= 0 ? table.getCellContents(col, row) : null);
    }

    public RowExpectation(int col, int row, String originalContent) {
      this.row = row;
      this.col = col;
      this.originalContent = originalContent;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      SlimTestResult testResult;
      if (returnValue == null) {
        testResult = SlimTestResult.ignore("Test not run");
      } else {
        String value;
        value = returnValue.toString();
        testResult = evaluationMessage(value, originalContent);
      }
      if (testResult != null) {
        table.updateContent(col, row, testResult);
        if (testResult.doesCount())
          getTestContext().increment(testResult.getExecutionResult());
      }
      return testResult;
    }

    SlimTestResult evaluationMessage(String actual, String expected) {
      return createEvaluationMessage(actual, expected);
    }

    protected abstract SlimTestResult createEvaluationMessage(String actual, String expected);

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
      table.updateContent(col, row, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }

    public int getCol() {
      return col;
    }

    public int getRow() {
      return row;
    }

    // Used only by TestXmlFormatter.SlimTestXmlFormatter
    public String getExpected() {
      return originalContent;
    }
  }

  class SymbolReplacer {
    protected String replacedString;
    private Matcher symbolMatcher;
    private final Pattern symbolPattern = Pattern.compile("\\$([a-zA-Z]\\w*)");
    private int startingPosition;

    SymbolReplacer(String s) {
      this.replacedString = s;
      symbolMatcher = symbolPattern.matcher(s);
    }

    String replace() {
      replaceAllSymbols();
      return replacedString;
    }

    private void replaceAllSymbols() {
      startingPosition = 0;
      while (symbolFound())
        replaceSymbol();
    }

    private void replaceSymbol() {
      String symbolName = symbolMatcher.group(1);
      String value = formatSymbol(symbolName);
      String prefix = replacedString.substring(0, symbolMatcher.start());
      String suffix = replacedString.substring(symbolMatcher.end());
      replacedString = prefix + value + suffix;
      int replacementEnd = symbolMatcher.start() + value.length();
      startingPosition = Math.min(replacementEnd, replacedString.length());
    }

    private String formatSymbol(String symbolName) {
      String value = getSymbol(symbolName);
      if (value == null) {
        for (int i = symbolName.length() - 1; i > 0; i--) {
          String str = symbolName.substring(0, i);
          if ((value = getSymbol(str)) != null)
            return formatSymbolValue(str, value) + symbolName.substring(i, symbolName.length());
        }

        return "$" + symbolName;
      } else
        return formatSymbolValue(symbolName, value);
    }


    private boolean symbolFound() {
      symbolMatcher = symbolPattern.matcher(replacedString);
      return symbolMatcher.find(startingPosition);
    }

    protected String formatSymbolValue(String name, String value) {
      return value;
    }
  }

  class FullExpansionSymbolReplacer extends SymbolReplacer {
    FullExpansionSymbolReplacer(String s) {
      super(s);
    }

    @Override
    protected String formatSymbolValue(String name, String value) {
      return String.format("$%s->[%s]", name, value);
    }
  }

  class VoidReturnExpectation extends RowExpectation {
    public VoidReturnExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      table.substitute(getCol(), getRow(), replaceSymbolsWithFullExpansion(expected));
      return SlimTestResult.plain();
    }
  }

  class SilentReturnExpectation implements SlimExpectation {
    private final int col;
    private final int row;

    public SilentReturnExpectation(int col, int row) {
      this.col = col;
      this.row = row;
    }

    @Override
    public TestResult evaluateExpectation(Object returnValue) {
      return null;
    }

    @Override
    public SlimExceptionResult evaluateException(SlimExceptionResult exceptionResult) {
      if (exceptionResult.isNoMethodInClassException() || exceptionResult.isNoInstanceException()) {
        return null;
      }
      table.updateContent(col, row, exceptionResult);
      getTestContext().incrementErroredTestsCount();
      return exceptionResult;
    }
  }

  class ConstructionExpectation extends RowExpectation {
    public ConstructionExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      if ("OK".equalsIgnoreCase(actual))
        return SlimTestResult.ok(replaceSymbolsWithFullExpansion(expected));
      else
        return SlimTestResult.error("Unknown construction message", actual);
    }
  }

  class SymbolAssignmentExpectation extends RowExpectation {
    private String symbolName;

    SymbolAssignmentExpectation(String symbolName, int col, int row) {
      super(col, row);
      this.symbolName = symbolName;
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      setSymbol(symbolName, actual);
      return SlimTestResult.plain(String.format("$%s<-[%s]", symbolName, actual));
    }
  }

  class ReturnedValueExpectation extends RowExpectation {
    public ReturnedValueExpectation(int col, int row) {
      super(col, row, table.getCellContents(col, row));
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      SlimTestResult testResult;
      String replacedExpected = replaceSymbols(expected);

      if (actual == null)
        testResult = SlimTestResult.fail("null", replacedExpected); //todo can't be right message.
      else if (actual.equals(replacedExpected))
        testResult = SlimTestResult.pass(announceBlank(replaceSymbolsWithFullExpansion(expected)));
      else if (replacedExpected.length() == 0)
        testResult = SlimTestResult.ignore(actual);
      else {
        testResult = new Comparator(replacedExpected, actual, expected).evaluate();
        if (testResult == null)
          testResult = SlimTestResult.fail(actual, replaceSymbolsWithFullExpansion(expected));
      }

      return testResult;
    }

    private String announceBlank(String originalValue) {
      return originalValue.length() == 0 ? "BLANK" : originalValue;
    }

  }

  class RejectedValueExpectation extends ReturnedValueExpectation {
    public RejectedValueExpectation(int col, int row) {
      super(col, row);
    }

    @Override
    protected SlimTestResult createEvaluationMessage(String actual, String expected) {
      SlimTestResult testResult = super.createEvaluationMessage(actual, expected);
      if (testResult != null)
        return testResult.negateTestResult();
      return null;
    }
  }

  class Comparator {
    private final String expression;
    private final String actual;
    private final String expected;
    private final Pattern simpleComparison = Pattern.compile(
      "\\A\\s*_?\\s*(!?(?:(?:[<>]=?)|(?:[~]?=)))\\s*(-?\\d*\\.?\\d+)\\s*\\Z"
    );
    private final Pattern range = Pattern.compile(
      "\\A\\s*(-?\\d*\\.?\\d+)\\s*<(=?)\\s*_\\s*<(=?)\\s*(-?\\d*\\.?\\d+)\\s*\\Z"
    );

    private Pattern regexPattern = Pattern.compile("\\s*=~/(.*)/");
    private Pattern customComparatorPattern = Pattern.compile("\\s*(\\w*):(.*)");
    private double v;
    private double arg1;
    private double arg2;
    public String operation;
    private String arg1Text;

    public Comparator(String actual, String expected) {
      this.expression = replaceSymbols(expected);
      this.actual = actual;
      this.expected = expected;
    }

    public Comparator(String expression, String actual, String expected) {
      this.expression = expression;
      this.actual = actual;
      this.expected = expected;
    }

    public boolean matches() {
      TestResult testResult = evaluate();
      return testResult != null && testResult.getExecutionResult() == ExecutionResult.PASS;
    }

    public SlimTestResult evaluate() {
      SlimTestResult message = evaluateRegularExpressionIfPresent();
      if (message != null)
        return message;

      message = evaluateCustomComparatorIfPresent();
      if (message != null)
        return message;

      operation = matchSimpleComparison();
      if (operation != null)
        return doSimpleComparison();

      Matcher matcher = range.matcher(expression);
      if (matcher.matches() && canUnpackRange(matcher)) {
        return doRange(matcher);
      } else
        return null;
    }

    private SlimTestResult evaluateCustomComparatorIfPresent() {
      SlimTestResult message = null;
      if (customComparatorRegistry == null) {
        return null;
      }
      Matcher customComparatorMatcher = customComparatorPattern.matcher(expression);
      if (customComparatorMatcher.matches()) {
        String prefix = customComparatorMatcher.group(1);
        CustomComparator customComparator = customComparatorRegistry.getCustomComparatorForPrefix(prefix);
        if (customComparator != null) {
          String expectedString = customComparatorMatcher.group(2);
          try {
            if (customComparator.matches(actual, expectedString)) {
              message = SlimTestResult.pass(expectedString + " matches " + actual);
            } else {
              message = SlimTestResult.fail(expectedString + " doesn't match " + actual);
            }
          } catch (Throwable t) {
            message = SlimTestResult.fail(expectedString + " doesn't match " + actual + ":\n" + t.getMessage());
          }
        }
      }
      return message;
    }

    private SlimTestResult evaluateRegularExpressionIfPresent() {
      Matcher regexMatcher = regexPattern.matcher(expression);
      SlimTestResult message = null;
      if (regexMatcher.matches()) {
        String pattern = regexMatcher.group(1);
        message = evaluateRegularExpression(pattern);
      }
      return message;
    }

    private SlimTestResult evaluateRegularExpression(String pattern) {
      SlimTestResult message;
      Matcher patternMatcher = Pattern.compile(pattern).matcher(actual);
      if (patternMatcher.find()) {
        message = SlimTestResult.pass(String.format("/%s/ found in: %s", pattern, actual));
      } else {
        message = SlimTestResult.fail(String.format("/%s/ not found in: %s", pattern, actual));
      }
      return message;
    }

    private SlimTestResult doRange(Matcher matcher) {
      boolean closedLeft = matcher.group(2).equals("=");
      boolean closedRight = matcher.group(3).equals("=");
      boolean pass = (arg1 < v && v < arg2) || (closedLeft && arg1 == v) || (closedRight && arg2 == v);
      return rangeMessage(pass);
    }

    private SlimTestResult rangeMessage(boolean pass) {
      String[] fragments = expected.replaceAll(" ", "").split("_");
      String message = String.format("%s%s%s", fragments[0], actual, fragments[1]);
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? SlimTestResult.pass(message) : SlimTestResult.fail(message);
    }

    private boolean canUnpackRange(Matcher matcher) {
      try {
        arg1 = Double.parseDouble(matcher.group(1));
        arg2 = Double.parseDouble(matcher.group(4));
        v = Double.parseDouble(actual);
      } catch (NumberFormatException e) {
        return false;
      }
      return true;
    }

    private SlimTestResult doSimpleComparison() {
      if (operation.equals("<") || operation.equals("!>="))
        return simpleComparisonMessage(v < arg1);
      else if (operation.equals(">") || operation.equals("!<="))
        return simpleComparisonMessage(v > arg1);
      else if (operation.equals(">=") || operation.equals("!<"))
        return simpleComparisonMessage(v >= arg1);
      else if (operation.equals("<=") || operation.equals("!>"))
        return simpleComparisonMessage(v <= arg1);
      else if (operation.equals("!="))
        return simpleComparisonMessage(v != arg1);
      else if (operation.equals("="))
        return simpleComparisonMessage(v == arg1);
      else if (operation.equals("~="))
        return simpleComparisonMessage(approximatelyEqual(arg1Text, actual));
      else if (operation.equals("!~="))
        return simpleComparisonMessage(!approximatelyEqual(arg1Text, actual));
      else
        return null;
    }

    private SlimTestResult simpleComparisonMessage(boolean pass) {
      String message = String.format("%s%s", actual, expected.replaceAll(" ", ""));
      message = replaceSymbolsWithFullExpansion(message);
      return pass ? SlimTestResult.pass(message) : SlimTestResult.fail(message);

    }

    private String matchSimpleComparison() {
      Matcher matcher = simpleComparison.matcher(expression);
      if (matcher.matches()) {
        try {
          v = Double.parseDouble(actual);
          arg1Text = matcher.group(2);
          arg1 = Double.parseDouble(arg1Text);
          return matcher.group(1);
        } catch (NumberFormatException e1) {
          return null;
        }
      }
      return null;
    }
  }
  

}

