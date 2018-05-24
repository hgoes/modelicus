header {
	package modelicus.parser;
}

options {
  language = "Java";
}

class ModelicusLexer extends Lexer; 

options {
    k=2;
    charVocabulary = '\3'..'\377';
    exportVocab = Modelicus;
    testLiterals = false;
    defaultErrorHandler = false;
    caseSensitive = true;
}

tokens {
	AND		= "and";
	OR		= "or";
	NOT		= "not";
	FOLLOWS;
	FORALL		= "forall";
	FUN		= "fun";
}

LPAR : '(';
RPAR : ')';
LESS : '<';
MINUS: '-';
PLUS: '+';
MULT: '*';
DIV: '/';
GREATER : '>';
COMMA : ',';
COL : ':';
SEMICOL : ';';
PIPE : '|';
LBRACK : '[';
RBRACK : ']';
EQUAL : '='
	('>' { $setType(FOLLOWS); })?
	;
NEQUAL : '!' '=';
UNDERSCORE : '_';

IDENT options {testLiterals = true; paraphrase="an identifier"; } :
	SMALL (NONDIGIT | DIGIT)*;
VAR : BIG (NONDIGIT | DIGIT)*;

protected
SMALL : 'a'..'z';

protected
BIG : 'A'..'Z';

protected
NONDIGIT : (SMALL | BIG);

protected
DIGIT : '0'..'9';

STRING : '"'! (SCHAR | SESCAPE)* '"'!;
SSTRING : '\''! (NONDIGIT | DIGIT | '.')* '\''!;

NUMBER : (DIGIT)+;

ASTNODE : '{'! (~'}')* '}'!;

protected
SCHAR :	(options { generateAmbigWarnings=false; } : ('\n' | "\r\n"))	{ newline(); }
	| '\t'
	| ~('\n' | '\t' | '\r' | '\\' | '"');

protected
SESCAPE : '\\' ('\\' | '"' | "'" | '?' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v');

WS :
	(	' '
	|	'\t'
	|	( "\r\n" | '\r' |	'\n' ) { newline(); }
	)
	{ $setType(antlr.Token.SKIP); }
	;
