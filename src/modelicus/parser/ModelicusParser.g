header {
	package modelicus.parser;
	import modelicus.ast.*;
	import java.util.List;
	import java.util.LinkedList;
}

options {
	language = "Java";
}

class ModelicusParser extends Parser;

options {
    codeGenMakeSwitchThreshold = 3;
    codeGenBitsetTestThreshold = 4;
	importVocab = Modelicus;
    defaultErrorHandler = false;
	k = 2;
}

document returns [Document doc]
    {
        doc = new Document();
        LinkedList<Rule> rules = new LinkedList<Rule>();
        LinkedList<Function> functions = new LinkedList<Function>();
        doc.rules = rules;
        doc.functions = functions;
        Rule r;
        Function func;
    }
    : (r=rule
        {
            rules.addLast(r);
        }
      |func=function
        {
            functions.addLast(func);
        })*
    ;

rule returns [Rule rule]
	{
		Expression expr;
		Advice adv;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } expr=expression FOLLOWS adv=advice SEMICOL
		{
			rule = new Rule(expr,adv);
			rule.line = line;
			rule.column = col;
		}
	;

function returns [Function fun]
	{
		Expression bd;
		LinkedList<Function.Argument> args;
		int line,col;
        List<String> decl;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } FUN i:IDENT LPAR
		{
			args = new LinkedList<Function.Argument>();
		} (v1:VAR COL decl=type_decl
			{
				args.addLast(new Function.Argument(v1.getText(),decl));
			} (COMMA v2:VAR COL decl=type_decl
			{
				args.addLast(new Function.Argument(v2.getText(),decl));
			})*)?
		RPAR FOLLOWS bd=expression SEMICOL
		{
			fun = new Function();
			fun.line = line;
			fun.column = col;
			fun.name = i.getText();
			fun.arguments = args;
			fun.body = bd;
		}
	;

type_decl returns [LinkedList<String> tp]
    : LBRACK
        {
            tp = new LinkedList<String>();
        }
      (name:VAR
        {
            tp.addLast(name.getText());
        }
      (COMMA name2:VAR
        {
            tp.addLast(name2.getText());
        })*)?
      RBRACK
    ;

expression returns [Expression expr]
	{
		Term t;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } t=term
		{
			LinkedList<Term> terms = new LinkedList<Term>();
			expr = new Expression();
			expr.line = line;
			expr.column = col;
			expr.terms = terms;
			terms.addLast(t);
		} (OR t=term
			{
				terms.addLast(t);
			})*
	;

term returns [Term trm]
	{
		Factor f;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } f=factor
		{
			LinkedList<Factor> factors = new LinkedList<Factor>();
			trm = new Term();
			trm.line = line;
			trm.column = col;
			trm.factors = factors;
			factors.addLast(f);
		} (AND f=factor
			{
				factors.addLast(f);
			})*
	;

factor returns [Factor fac]
	{
		Relation r;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } (i:NOT)? r=relation
		{
			fac = new Factor();
			fac.line = line;
			fac.column = col;
			fac.isNegated = i!=null;
			fac.relation = r;
		}
	;

relation returns [Relation rel = null]
	{
		Relation.RelOp op;
		Sum left,right;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } left=sum (((LESS
		{
			op = Relation.RelOp.LESS;
		} | GREATER
		{
			op = Relation.RelOp.GREATER;
		} | EQUAL
		{
			op = Relation.RelOp.EQUAL;
		} | NEQUAL
		{
			op = Relation.RelOp.NEQUAL;
		}) right=sum
		{
			Relation.Binary bin = new Relation.Binary();
			bin.line = line;
			bin.column = col;
			bin.leftHand = left;
			bin.rightHand = right;
			bin.operator = op;
			rel = bin;
		}) | (EOF)?
		{
				rel = left;
		})
	;

sum returns [Sum sum]
	{
		Product p;
		Sum.SumElement elem;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } (i:MINUS)? p=product
		{
			LinkedList<Sum.SumElement> elements;
			sum = new Sum();
			sum.line = line;
			sum.column = col;
			elements = new LinkedList<Sum.SumElement>();
			sum.elements = elements;
			elem = new Sum.SumElement();
			if(i!=null) {
				elem.sign = Sum.SumSign.MINUS;
			} else {
				elem.sign = Sum.SumSign.PLUS;
			}
			elem.product = p;
			elements.addLast(elem);
		}
		((MINUS
			{
				elem = new Sum.SumElement();
				elem.sign = Sum.SumSign.MINUS;
			}| PLUS
			{
				elem = new Sum.SumElement();
				elem.sign = Sum.SumSign.PLUS;
			}) p=product
			{
				elem.product = p;
				elements.addLast(elem);
			})*
	;

product returns [Product prod]
	{
		Primary p;
		Product.ProductElement pe;
		int line,col;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } p=primary
		{
			LinkedList<Product.ProductElement> elements
				= new LinkedList<Product.ProductElement>();
			prod = new Product();
			prod.line = line;
			prod.column = col;
			prod.first = p;
			prod.elements = elements;
		} ((MULT
		{
			pe = new Product.Mult();
		}| DIV
		{
			pe = new Product.Div();
		}) p=primary
		{
			pe.value = p;
			elements.addLast(pe);
		})*
	;

primary returns [Primary prim]
	{
		Expression expr;
		Expression expr2;
		int line = LT(1).getLine();
		int col = LT(1).getColumn();
	}
	: v:VAR
		{
			Primary.Variable var = new Primary.Variable();
			var.line = line;
			var.column = col;
			var.name = v.getText();
			prim = var;			
		}
	| LPAR expr=expression RPAR
		{
			prim = expr;
			prim.line = line;
			prim.column = col;
		}
	| i:IDENT LPAR 
		{
			LinkedList<Expression> arguments
				= new LinkedList<Expression>();
			//pred.name = i.getText();
			//pred.arguments = arguments;
			//prim = pred;
		}
		(expr=expression
		{
			arguments.addLast(expr);
		} (COMMA expr=expression
		{
			arguments.addLast(expr);
		})*)? RPAR
		{
			prim = new Primary.Predicate(i.getText(),arguments);
			prim.line = line;
			prim.column = col;
		}
	| n:NUMBER
		{
			prim = new Primary.Number(new Integer(n.getText()));
			prim.line = line;
			prim.column = col;
		}
	| s:STRING
		{
			prim = new Primary.String(s.getText());
			prim.line = line;
			prim.column = col;
		}
	| LBRACK v2:VAR PIPE expr=expression RBRACK
		{
			prim = new Primary.Count(v2.getText(),expr);
			prim.line = line;
			prim.column = col;
		}
	| str:SSTRING
		{
			prim = new Primary.NameRef(str.getText());
			prim.line = line;
			prim.column = col;
		}
	| FORALL LBRACK expr=expression PIPE expr2=expression RBRACK
		{
			prim = new Primary.Forall(expr,expr2);
			prim.line = line;
			prim.column = col;
		}
	| ast:ASTNODE
		{
			prim = new Primary.ASTPrimary(ast.getText());
			prim.line = line;
			prim.column = col;
		}
    | UNDERSCORE
        {
            prim = new Primary.DontCare();
            prim.line = line;
            prim.column = col;
        }
	;

simple_advice returns [Advice adv]
	: adv=advice_var
	| adv=advice_str
	| adv=advice_prop
	;

advice_var returns [Advice.Variable var]
	: v:VAR
		{
			var = new Advice.Variable(v.getText());
			var.line = v.getLine();
			var.column = v.getColumn();
		}
	;

advice_str returns [Advice.String str]
	: s:STRING
		{
			str = new Advice.String(s.getText());
			str.line = s.getLine();
			str.column = s.getColumn();
		}
	;

advice_prop returns [Advice.Property prop]
	{
		Advice.Variable var;
	}
	: v:IDENT LPAR var=advice_var RPAR
		{
			prop = new Advice.Property(v.getText(),var);
			prop.line = v.getLine();
			prop.column = v.getColumn();
		}
	;

advice returns [Advice.Append app]
	{
		Advice adv;
		app = new Advice.Append();
	}
	: adv=simple_advice
		{
			app.elements.addLast(adv);
		}
		(COMMA adv=simple_advice
		{
			app.elements.addLast(adv);
		})*
	;
