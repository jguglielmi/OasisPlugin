package org.oasis.plugin;

import java.util.ArrayList;
import java.util.Collection;

import util.Maybe;
import fitnesse.wikitext.parser.HtmlBuilder;
import fitnesse.wikitext.parser.Matcher;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.PathsProvider;
import fitnesse.wikitext.parser.Rule;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Translator;

public class OasisSymbolType extends SymbolType implements Rule, PathsProvider {

	public OasisSymbolType() {
        super("Oasis");
        wikiMatcher(new Matcher().startLineOrCell().string("!oasis"));
        wikiRule(this);
        htmlTranslation(new HtmlBuilder("span").body(0, "!oasis classpaths").attribute("class", "meta").inline());
	}

	public static SymbolProvider getSymbolProvider() {
		return new SymbolProvider(new SymbolType[] {new OasisSymbolType()});
	}

	@Override
	public Collection<String> providePaths(Translator translator, Symbol symbol) {
		/*
		!path build
		!path lib/ivy/*
		!path lib/mvn/*
		!path plugins/*
		*/
		//System.out.println("! adding oasis paths !");
		Collection<String> paths = new ArrayList<String>();
		paths.add("build");
		paths.add("lib/*");
		paths.add("lib/ivy/*");
		paths.add("lib/mvn/*");
		paths.add("plugins/*");
		return paths;
	}

	@Override
	public Maybe<Symbol> parse(Symbol current, Parser parser) {
		// TODO Auto-generated method stub
		return new Maybe<Symbol>(current.add(""));
	}

}
