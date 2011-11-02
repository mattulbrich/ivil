package de.uka.iti.pseudo.gui.editor;

import java.io.*;
import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.*;

import de.uka.iti.pseudo.util.Log;

/**
 * Scanner for IVIL.<p>
 *
 * This file is an adaption of the DelphiTokenMaker found in the lib rsyntaxarea.
 *
 * <P>
 * This implementation was created using
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1; however, the generated file
 * was modified for performance.  Memory allocation needs to be almost
 * completely removed to be competitive with the handwritten lexers (subclasses
 * of <code>AbstractTokenMaker</code>, so this class has been modified so that
 * Strings are never allocated (via yytext()), and the scanner never has to
 * worry about refilling its buffer (needlessly copying chars around).
 * We can achieve this because RText always scans exactly 1 line of tokens at a
 * time, and hands the scanner this line as an array of characters (a Segment
 * really).  Since tokens contain pointers to char arrays instead of Strings
 * holding their contents, there is no need for allocating new memory for
 * Strings.<p>
 *
 * The actual algorithm generated for scanning has, of course, not been
 * modified.<p>
 *
 * If you wish to regenerate this file yourself, keep in mind the following:
 * <ul>
 *   <li>The generated IvilTokenMaker.java</code> file will contain two
 *       definitions of both <code>zzRefill</code> and <code>yyreset</code>.
 *       You should hand-delete the second of each definition (the ones
 *       generated by the lexer), as these generated methods modify the input
 *       buffer, which we'll never have to do.</li>
 *   <li>add {@code @SuppressWarnings("fallthrough")} to the function yylex().
 *   <li>You should also change the declaration/definition of zzBuffer to NOT
 *       be initialized.  This is a needless memory allocation for us since we
 *       will be pointing the array somewhere else anyway.</li>
 *   <li>You should NOT call <code>yylex()</code> on the generated scanner
 *       directly; rather, you should use <code>getTokenList</code> as you would
 *       with any other <code>TokenMaker</code> instance.</li>
 * </ul>
 *
 */

%%

%public
%class IvilTokenMaker
%extends AbstractJFlexTokenMaker
%unicode
%type org.fife.ui.rsyntaxtextarea.Token


%{
        /**
         * Token type specific to IvilTokenMaker; denotes an unterminated String
         */
        public static final int INTERNAL_STRING = -2;
        
	/**
	 * Token type specific to IvilTokenMaker; denotes a line ending
	 * with an unterminated "(*" comment.
	 */
	public static final int INTERNAL_MLC_LEVEL = -1000;

	/**
	 * Constructor.  This must be here because JFlex does not generate a
	 * no-parameter constructor.
	 */
	public IvilTokenMaker() {
	}
	
	private int commentLevel = 0;

	/**
	 * Adds the token specified to the current linked list of tokens as an
	 * "end token;" that is, at <code>zzMarkedPos</code>.
	 *
	 * @param tokenType The token's type.
	 */
	private void addEndToken(int tokenType) {
		addToken(zzMarkedPos,zzMarkedPos, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addToken(int, int, int)
	 */
//	private void addHyperlinkToken(int start, int end, int tokenType) {
//		int so = start + offsetShift;
//		addToken(zzBuffer, start,end, tokenType, so, true);
//	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 */
	private void addToken(int tokenType) {
		addToken(zzStartRead, zzMarkedPos-1, tokenType);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param tokenType The token's type.
	 * @see #addHyperlinkToken(int, int, int)
	 */
	private void addToken(int start, int end, int tokenType) {
		int so = start + offsetShift;
		addToken(zzBuffer, start,end, tokenType, so, false);
	}


	/**
	 * Adds the token specified to the current linked list of tokens.
	 *
	 * @param array The character array.
	 * @param start The starting offset in the array.
	 * @param end The ending offset in the array.
	 * @param tokenType The token's type.
	 * @param startOffset The offset in the document at which this token
	 *                    occurs.
	 * @param hyperlink Whether this token is a hyperlink.
	 */
	public void addToken(char[] array, int start, int end, int tokenType,
						int startOffset, boolean hyperlink) {
		super.addToken(array, start,end, tokenType, startOffset, hyperlink);
		zzStartRead = zzMarkedPos;
	}


	/**
	 * Returns the text to place at the beginning and end of a
	 * line to "comment" it in a this programming language.
	 *
	 * @return The start and end strings to add to a line to "comment"
	 *         it out.
	 */
	public String[] getLineCommentStartAndEnd() {
		return new String[] { "# ", null };
	}


	/**
	 * Returns the first token in the linked list of tokens generated
	 * from <code>text</code>.  This method must be implemented by
	 * subclasses so they can correctly implement syntax highlighting.
	 *
	 * @param text The text from which to get tokens.
	 * @param initialTokenType The token type we should start with.
	 * @param startOffset The offset into the document at which
	 *        <code>text</code> starts.
	 * @return The first <code>Token</code> in a linked list representing
	 *         the syntax highlighted text.
	 */
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

		resetTokenList();
		this.offsetShift = -text.offset + startOffset;
                this.commentLevel = 0;
                
		// Start off in the proper state.
		int state = Token.NULL;
		if(initialTokenType <= INTERNAL_MLC_LEVEL) {
		      state = MLC;
                      commentLevel = (INTERNAL_MLC_LEVEL - initialTokenType);
                      start = text.offset;
		} else if(initialTokenType == INTERNAL_STRING) {
		      state = STRING;
		      start = text.offset;
		} else {
		      state = Token.NULL;
		}

		s = text;
		try {
			yyreset(zzReader);
			yybegin(state);
			return yylex();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new DefaultToken();
		}

	}


	/**
	 * Refills the input buffer.
	 *
	 * @return      <code>true</code> if EOF was reached, otherwise
	 *              <code>false</code>.
	 * @exception   IOException  if any I/O-Error occurs.
	 */
	private boolean zzRefill() throws java.io.IOException {
		return zzCurrentPos>=s.offset+s.count;
	}


	/**
	 * Resets the scanner to read from a new input stream.
	 * Does not close the old reader.
	 *
	 * All internal variables are reset, the old input stream 
	 * <b>cannot</b> be reused (internal buffer is discarded and lost).
	 * Lexical state is set to <tt>YY_INITIAL</tt>.
	 *
	 * @param reader   the new input stream 
	 */
	public final void yyreset(java.io.Reader reader) throws java.io.IOException {
		// 's' has been updated.
		zzBuffer = s.array;
		/*
		 * We replaced the line below with the two below it because zzRefill
		 * no longer "refills" the buffer (since the way we do it, it's always
		 * "full" the first time through, since it points to the segment's
		 * array).  So, we assign zzEndRead here.
		 */
		//zzStartRead = zzEndRead = s.offset;
		zzStartRead = s.offset;
		zzEndRead = zzStartRead + s.count - 1;
		zzCurrentPos = zzMarkedPos/* = zzPushbackPos*/ = s.offset;
		zzLexicalState = YYINITIAL;
		zzReader = reader;
		zzAtBOL  = true;
		zzAtEOF  = false;
	}


%}

Letter                = [A-Za-z]
LetterOrUnderscore    = ({Letter} | "_")
Digit                 = [0-9]
IdentifierPart        = ({LetterOrUnderscore}|{Digit})

Identifier            = ({LetterOrUnderscore}{IdentifierPart}*)
OpIdentifier          = ([\\\+\-<>&|\=\*\/!\^\@.\:]+)
MetaIdentifier        = ("$$"{Identifier})
InternalIdentifier    = ("$"{Identifier})
SchemaIdentifier      = ("%"{Identifier})
SchemaTypeIdentifier  = ("%'"{Identifier})
TyvarIdentifier       = ("'"{Identifier})
BinderIdentifier      = ("\\"{Identifier})

Brackets              = [\{\}\[\]\(\)\?]
Separators            = [,;]

LineTerminator				= (\n)
WhiteSpace				= ([ \t\f])

MLCBegin              = "(*"
MLCEnd                = "*)"
LineCommentBegin      = "#"

BooleanLiteral        = ("true"|"false")
Natural               = {Digit}+

%state MLC
%state SLC
%state STRING


%%

<YYINITIAL> {

	/* Keywords */
// TODO ONLY IN DEFAULT MODE
   "add" 
|  "assignable" 
|  "axiom"
// assume is listed below
|  "binder"
|  "closegoal"
|  "description"
|  "find"
|  "function"
|  "include"
|  "infix"
|  "newgoal"
|  "not"
|  "plugin"
|  "prefix"
|  "problem"
|  "program"
|  "properties"
|  "remove"
|  "replace"
|  "rule"
|  "samegoal"
|  "sort"
|  "tags"
|  "unique"
|  "where"
// Logics tokens are visible also in protected mode
|  "as"
|  "|-"

// type variable binders
|  "\\T_all"
|  "\\T_ex"

// Programs
|  "assume"
|  "assert"
|  "goto"
|  "havoc"
|  "end"
|  "skip"
|  "skip_loopinv"
|  "source"
|  "sourceline"	
	                                        { addToken(Token.RESERVED_WORD); }

	{BooleanLiteral}			{ addToken(Token.LITERAL_BOOLEAN); }

	{LineTerminator}				{ addNullToken(); return firstToken; }

	{Identifier}
|       {BinderIdentifier}
|	{MetaIdentifier}
|	{InternalIdentifier}                          { addToken(Token.IDENTIFIER); }

        {TyvarIdentifier}                               { addToken(Token.DATA_TYPE); }

        {SchemaIdentifier}
|       {SchemaTypeIdentifier}                          { addToken(Token.VARIABLE); }

	{WhiteSpace}+					{ addToken(Token.WHITESPACE); }

	/* String/Character literals. */
	\"				        { start = zzMarkedPos-1; yybegin(STRING); }
        {Natural}                               { addToken(Token.LITERAL_NUMBER_DECIMAL_INT); }
        {BooleanLiteral}                        { addToken(Token.LITERAL_BOOLEAN); }
        
	/* Comment literals. */
	{MLCBegin}					{ start = zzMarkedPos-2; yybegin(MLC);commentLevel++; Log.log(Log.VERBOSE, start + " increments to " + commentLevel);}
	{LineCommentBegin}				{ start = zzMarkedPos-1; yybegin(SLC); }

	{OpIdentifier}					{ addToken(Token.OPERATOR); }
	
	{Separators}
|	{Brackets}                                      { addToken(Token.SEPARATOR); }

	/* Ended with a line not in a string or comment. */
	<<EOF>>						{ addNullToken(); return firstToken; }

	/* Catch any other (unhandled) characters. */
	.							{ addToken(Token.ERROR_CHAR); }

}

<MLC> {

	[^\n\*]+    {}
	
	{MLCEnd}    { commentLevel --;
	Log.log(Log.VERBOSE, start + " decrements to " + commentLevel);
	              if(commentLevel == 0) {
	                yybegin(YYINITIAL); 
	                addToken(start,zzStartRead+1, Token.COMMENT_MULTILINE); 
	              }
	            }
	{MLCBegin}  { commentLevel ++; }
	\*          {}
	\n |
        <<EOF>>     { addToken(start,zzStartRead-1, Token.COMMENT_MULTILINE);
                      int leveledState = (INTERNAL_MLC_LEVEL - commentLevel);
                      Log.log(Log.VERBOSE, start + " ends with level " + leveledState);
                      addEndToken(leveledState);
                      return firstToken; }
}

<SLC> {
        [^\n]+      {}
	\n |
	<<EOF>>     { addToken(start,zzStartRead-1, Token.COMMENT_EOL); addNullToken(); return firstToken; }
}

// TODO Have \" accepted
<STRING> {
        [^\"]+        {}
        \"           { addToken(start, zzStartRead, Token.LITERAL_STRING_DOUBLE_QUOTE); yybegin(YYINITIAL);}
        \n |
        <<EOF>>      { addToken(start, zzStartRead-1, Token.LITERAL_STRING_DOUBLE_QUOTE); 
                       addEndToken(INTERNAL_STRING); return firstToken;}
}        