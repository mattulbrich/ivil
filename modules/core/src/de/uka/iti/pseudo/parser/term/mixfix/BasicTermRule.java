//package de.uka.iti.pseudo.parser.term.mixfix;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.uka.iti.mixfix.ADTList;
//import de.uka.iti.mixfix.MixFixRule;
//import de.uka.iti.mixfix.ParseContext;
//import de.uka.iti.pseudo.parser.ParserConstants;
//import de.uka.iti.pseudo.parser.Token;
//import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
//import de.uka.iti.pseudo.parser.term.ASTExplicitVariableTerm;
//import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
//import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
//import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
//import de.uka.iti.pseudo.parser.term.ASTTerm;
//
//public class BasicTermRule implements MixFixRule<ASTTerm, Token> {
//
//    private static final int MAX = Integer.MAX_VALUE;
//
//    /**
//     * Basic terms are not recursive.
//     *
//     * @return <code>false</code>
//     */
//    @Override public boolean isLeftRecursive() {
//        return false;
//    }
//
//    @Override public ADTList<ParseContext<ASTTerm, Token>> parse(
//            ParseContext<ASTTerm, Token> context, int minBinding) {
//
//        Token token = context.getCurrentToken();
//        context = context.consumeToken();
//
//        switch(token.kind) {
//        case ParserConstants.LEFTANYMOD:
//        case ParserConstants.LEFTDIAMOND:
//        case ParserConstants.LEFTDSQUARE:
//        case ParserConstants.LEFTSQUARE:
//            return parseModality(context);
//
//        case ParserConstants.LEFTCURLY:
//            return parseUpdate(context);
//
//        case ParserConstants.LEFTPAREN:
//            return parseParen(context);
//
//        case ParserConstants.VAR:
//            return parseExplicitVar(context, token);
//
//        case ParserConstants.SCHEMA_IDENTIFIER:
//            return parseSchemaVar(context, token);
//
//        case ParserConstants.NATURAL:
//            return parseNatural(context, token);
//
//        case ParserConstants.IDENTIFIER:
//            return parseApplication(context, token);
//
//        default: return ADTList.nil();
//        }
//
//    }
//
//    private ADTList<ParseContext<ASTTerm, Token>> parseUpdate(ParseContext<ASTTerm, Token> context) {
//        // TODO Implement BasicTermRule.parseUpdate
//        return null;
//    }
//
//    private ADTList<ParseContext<ASTTerm, Token>> parseParen(ParseContext<ASTTerm, Token> context) {
//        // TODO Implement BasicTermRule.parseParen
//        return null;
//    }
//
//    private ADTList<ParseContext<ASTTerm, Token>> parseExplicitVar(
//            ParseContext<ASTTerm, Token> context, Token token) {
//        assert token.kind == ParserConstants.VAR;
//        Token nextToken = context.getCurrentToken();
//        if(nextToken.kind == ParserConstants.IDENTIFIER) {
//            return ADTList.singleton(context.setResult(
//                    new ASTExplicitVariableTerm(token, nextToken)));
//        } else {
//            // TODO perhaps throw an exception already here?!
//            return ADTList.nil();
//        }
//    }
//
//    /*
//     * simply create a schema variable from the token ...
//     */
//    private ADTList<ParseContext<ASTTerm, Token>>
//            parseSchemaVar(ParseContext<ASTTerm, Token> context, Token token) {
//        assert token.kind == ParserConstants.SCHEMA_IDENTIFIER;
//        return ADTList.singleton(context.setResult(new ASTSchemaVariableTerm(token)));
//    }
//
//    /*
//     * simply create a number literal from the token ...
//     */
//    private ADTList<ParseContext<ASTTerm, Token>>
//            parseNatural(ParseContext<ASTTerm, Token> context, Token token) {
//        assert token.kind == ParserConstants.NATURAL;
//        return ADTList.singleton(context.setResult(new ASTNumberLiteralTerm(token)));
//    }
//
//    private ADTList<ParseContext<ASTTerm, Token>>
//            parseApplication(ParseContext<ASTTerm, Token> context, Token token) {
//        assert token.kind == ParserConstants.IDENTIFIER;
//        Token nextToken = context.getCurrentToken();
//        if(nextToken.kind == ParserConstants.LEFTPAREN) {
//            // it IS a function application
//            context = context.consumeToken();
//            List<ASTTerm> subterms = parseSeparatedList(context, ParserConstants.COMMA);
//            ADTList.singleton(context.setResult(new ASTApplicationTerm(token, subterms)));
//        } else {
//            // it is a single identifier ... might be a variable
//            return ADTList.singleton(context.setResult(new ASTIdentifierTerm(token)));
//        }
//    }
//
//    private List<ASTTerm> parseSeparatedList(
//            ParseContext<ASTTerm, Token> context, int separator) {
//        ArrayList<ASTTerm> list = new ArrayList<ASTTerm>();
//        do {
//            ADTList<ParseContext<ASTTerm, Token>> result = context.parseExpression(0);
//            for (ParseContext<ASTTerm, Token> ctx : result) {
//                if(ctx.getCurrentToken().kind == ParserConstants.COMMA) {
//                    ctx.consumeToken();
//
//                }
//            }
//        }
//    }
//
//    private ADTList<ParseContext<ASTTerm, Token>> parseModality(ParseContext<ASTTerm, Token> context) {
//        // TODO Implement BasicTermRule.parseModality
//        return null;
//    }
//
//}
