package org.oasis.plugin;

import util.Maybe;
import fitnesse.wikitext.parser.HtmlBuilder;
import fitnesse.wikitext.parser.Matcher;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.Rule;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

public class OasisVersionSymbolType extends SymbolType implements Rule {

	public OasisVersionSymbolType() {
        super("Oasis Version");
		String ver = GlobalEnv.getOasisVersion();
		String bdate = GlobalEnv.getOasisBuildDate();
        wikiMatcher(new Matcher().startLineOrCell().string("!version"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("span").body(0, "OASIS version " + ver + " date " + bdate).attribute("class", "").inline());
	}

	public static SymbolProvider getSymbolProvider() {
		return new SymbolProvider(new SymbolType[] {new OasisVersionSymbolType()});
	}

	@Override
	public Maybe<Symbol> parse(Symbol current, Parser parser) {
		// TODO Auto-generated method stub
		return new Maybe<Symbol>(current.add(""));
	}

}
