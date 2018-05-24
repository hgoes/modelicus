/* 
 * This file was part of OpenModelica.
 * 
 * Copyright (c) 1998-2008, Linkopings University,
 * Department of Computer and Information Science, 
 * SE-58183 Linkoping, Sweden. 
 * 
 * All rights reserved.
 * 
 * THIS PROGRAM IS PROVIDED UNDER THE TERMS OF THIS OSMC PUBLIC 
 * LICENSE (OSMC-PL). ANY USE, REPRODUCTION OR DISTRIBUTION OF 
 * THIS PROGRAM CONSTITUTES RECIPIENT'S ACCEPTANCE OF THE OSMC 
 * PUBLIC LICENSE. 
 * 
 * The OpenModelica software and the Open Source Modelica 
 * Consortium (OSMC) Public License (OSMC-PL) are obtained 
 * from Linkopings University, either from the above address, 
 * from the URL: http://www.ida.liu.se/projects/OpenModelica
 * and in the OpenModelica distribution.
 * 
 * This program is distributed  WITHOUT ANY WARRANTY; without 
 * even the implied warranty of  MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE, EXCEPT AS EXPRESSLY SET FORTH 
 * IN THE BY RECIPIENT SELECTED SUBSIDIARY LICENSE CONDITIONS 
 * OF OSMC-PL. 
 * 
 * See the full OSMC Public License conditions for more details.
 * 
 */

header {
	package modelica.parser;
	import java.util.List;
	import java.util.LinkedList;
	import java.math.BigInteger;
	import modelica.ast.*;
}

options {
	language = "Java";
}

class ModelicaParser extends Parser;

options {
    codeGenMakeSwitchThreshold = 3;
    codeGenBitsetTestThreshold = 4;
	importVocab = Modelica;
    defaultErrorHandler = false;
	k = 2;
}

tokens {
    ALGORITHM_STATEMENT;
	ARGUMENT_LIST;
	CLASS_DEFINITION	;
    CLASS_EXTENDS ;
	CLASS_MODIFICATION;
	CODE_EXPRESSION;
	CODE_MODIFICATION;
	CODE_ELEMENT;
	CODE_EQUATION;
	CODE_INITIALEQUATION;
	CODE_ALGORITHM;
	CODE_INITIALALGORITHM;
	COMMENT;
    COMPONENT_DEFINITION;
	DECLARATION	; 
	DEFINITION ;
	ENUMERATION_LITERAL;
	ELEMENT		;
	ELEMENT_MODIFICATION		;
	ELEMENT_REDECLARATION	;
    EQUATION_STATEMENT;
    EXTERNAL_ANNOTATION ;
 	INITIAL_EQUATION;
	INITIAL_ALGORITHM;
    IMPORT_DEFINITION;
    IDENT_LIST;
	EXPRESSION_LIST;
	EXTERNAL_FUNCTION_CALL;
    FOR_INDICES ;
    FOR_ITERATOR ;
	FUNCTION_CALL		;
	INITIAL_FUNCTION_CALL		;
	FUNCTION_ARGUMENTS;
	NAMED_ARGUMENTS;
	QUALIFIED;
	RANGE2		;
	RANGE3		;
  	STORED_DEFINITION;
	STRING_COMMENT;
	UNARY_MINUS	;
	UNARY_PLUS	;
	UNQUALIFIED;
	FLAT_IDENT;
	TYPE_LIST;
	EMPTY;
}

/**
 * start parser
 */
startEquation returns [Equation eq]
	: eq=equation
	;

startModification returns [ClassModification.Modification mod]
	: mod=modification
	;

/*
 * 2.2.1 Stored definition
 */


stored_definition returns [StoredDef def=new StoredDef()]
	{
		int line,col;
		List<String> w = null;
		ClassDef cl_def;
	}
	: {
		line = LT(1).getLine();
		col  = LT(1).getColumn();
	  } (w=within_clause)? 
		{
			def.within = w;
		}
	  ((i:FINAL)? cl_def=class_definition
	  	{
			cl_def.isFinal = i != null;
			def.classes.add(cl_def);
		} SEMICOLON)*

	;

/*
 * 2.2.2 Class definition
 */
class_definition returns [ClassDef def]
	{
		int line,col;
		ClassDef.ClassType tp;
		ClassDef.ClassSpec csp;
	}
	: {
		line = LT(1).getLine();
		col = LT(1).getColumn();
	  } (encap:ENCAPSULATED)? (part:PARTIAL)? tp=class_type csp=class_spec
		{
			def = new ClassDef(line,col);
			def.isEncapsulated = encap!=null;
			def.isPartial = part!=null;
			def.classType = tp;
			def.spec = csp;
		}
	;

class_spec returns [ClassDef.ClassSpec spec]
	: i:IDENT spec=class_spec2[i.getText()]
	;

class_spec2[String name] returns [ClassDef.ClassSpec spec]
	{
		List<String> com;
		ClassDef.Composition comp;
		TypePrefix pref;
		List<String> name2;
		List<ArraySubscript> subscr = null;
		ClassModification mod = null;
		Comment com2;
	}
	: com=string_comment comp=composition END i2:IDENT
		{
			if(name.equals(i2.getText())) {
				ClassDef.NormalClassSpec nspec = new ClassDef.NormalClassSpec();
				nspec.name = name;
				nspec.comment = com;
				nspec.composition = comp;
				spec = nspec;
			} else {
				throw new RecognitionException("The identifier at start and end are different","<some source>",0,0);
			}
		}
	| EQUALS pref=type_prefix name2=name_path (subscr=array_subscripts)? (mod=class_modification)? com2=comment
		{
			ClassDef.ShortClassSpec nspec = new ClassDef.ShortClassSpec();
			nspec.name = name;
			nspec.prefix = pref;
			nspec.base = name2;
			nspec.subscripts = subscr;
			nspec.modifications = mod;
			nspec.comment = com2;
			spec = nspec;
		}
	| EQUALS spec=enumeration[name]
	;

class_type returns [ClassDef.ClassType tp]
	: CLASS
		{ tp = ClassDef.ClassType.Class; }
	| MODEL
		{ tp = ClassDef.ClassType.Model; }
	| RECORD
		{ tp = ClassDef.ClassType.Record; }
	| BLOCK
		{ tp = ClassDef.ClassType.Block; }
	| EXPANDABLE CONNECTOR
		{ tp = ClassDef.ClassType.ExpandableConnector; }
	| CONNECTOR
		{ tp = ClassDef.ClassType.Connector; }
	| TYPE
		{ tp = ClassDef.ClassType.Type; }
	| PACKAGE
		{ tp = ClassDef.ClassType.Package; }
	| FUNCTION
		{ tp = ClassDef.ClassType.Function; }
	| UNIONTYPE
		{ tp = ClassDef.ClassType.UnionType; }
	;

enumeration[String name] returns [ClassDef.EnumerationSpec spec]
	{
		Comment com;
		List<ClassDef.EnumerationSpec.Literal> lits;
	}
	: ENUMERATION LPAR
		{
			spec = new ClassDef.EnumerationSpec();
			spec.name = name;
		}
	  (lits=enum_list
		{
			spec.literals = lits;
		}
	  | COLON
	  	{
			spec.literals = null;
		}) RPAR com=comment
		{
			spec.comment = com;
		}
	;

enum_list returns [LinkedList<ClassDef.EnumerationSpec.Literal> lits]
	{
		ClassDef.EnumerationSpec.Literal lit;
	}
	: lit=enumeration_literal
		{
			lits = new LinkedList<ClassDef.EnumerationSpec.Literal>();
			lits.add(lit);
		}
	  (COMMA lit=enumeration_literal
	  	{
			lits.addLast(lit);
		})*
	;

enumeration_literal returns [ClassDef.EnumerationSpec.Literal lit]
	{
		Comment com;
	}
	: i:IDENT com=comment
		{
			lit = new ClassDef.EnumerationSpec.Literal();
			lit.name = i.getText();
			lit.comment = com;
		}
	;

composition returns [ClassDef.Composition comp]
	{
		List<ClassDef.ElementOrAnnotation> elems;
		ClassDef.EquationSection eq;
		ClassDef.AlgorithmSection alg;
	}
	: elems=element_list
		{
			comp = new ClassDef.Composition();
			comp.defaultElements = elems;
		}
	  ((PUBLIC elems=element_list
	  	{
			comp.sections.add(new ClassDef.PublicSection(elems));
		})
	  |(PROTECTED elems=element_list
	  	{
			comp.sections.add(new ClassDef.ProtectedSection(elems));
		})
	  |(eq=equation_clause
		{
			comp.sections.add(eq);
		})
	  |(alg=algorithm_clause
	  	{
			comp.sections.add(alg);
		}))*
	;

element_list returns [List<ClassDef.ElementOrAnnotation> elems]
	{
		ClassDef.Element e;
		Annotation a;
		elems = new LinkedList<ClassDef.ElementOrAnnotation>();
	}
	: (e=element SEMICOLON
		{
			elems.add(e);
		}
	  | a=annotation SEMICOLON
		{
			elems.add(a);
		}
	  )*
	;

element returns [ClassDef.Element el]
	: el=import_clause
	| el=extends_clause
	| el=definition_or_declaration
	;

definition_or_declaration returns [ClassDef.DefinitionOrDeclarationClause el]
	{
		ClassDef def;
		TypePrefix pre;
		TypeSpecifier spec;
		List<ComponentDeclaration> comps;
		ClassDef.ExtendsClause ext = null;
		Comment ext_com = null;
	}
	: (ired:REDECLARE)? (ifin:FINAL)? (iinn:INNER)? (iout:OUTER)? (irepl:REPLACEABLE)?
		(def=class_definition
			{
				ClassDef.DefinitionClause dc = new ClassDef.DefinitionClause();
				dc.definition = def;
				el = dc;
			}
		| pre=type_prefix spec=type_specifier comps=component_list
			{
				ClassDef.DeclarationClause dc = new ClassDef.DeclarationClause();
				dc.prefix = pre;
				dc.specifier = spec;
				dc.declarations = comps;
				el = dc;
			})
		(ext=constraining_clause ext_com=comment)?
		{
			el.isRedeclare = ired != null;
			el.isFinal = ifin != null;
			el.isInner = iinn != null;
			el.isOuter = iout != null;
			if(irepl == null && ext!=null) {
				throw new RecognitionException("constraining clause without \"replaceable\" modifier","<somesource>",0,0);
			}
			el.isReplaceable = irepl != null;
			el.replaceable = ext;
			el.replaceable_comment = ext_com;
		}
	;

import_clause returns [ClassDef.ImportClause cl]
	{
		Comment com;
	}
	: IMPORT (cl=explicit_import_clause | cl = implicit_import_clause) com=comment
		{
			cl.comment = com;
		}
	;

extends_clause returns [ClassDef.ExtendsClause clause]
	: EXTENDS clause=extends_content
	;

constraining_clause returns [ClassDef.ExtendsClause clause]
	: clause=extends_clause
	| CONSTRAINEDBY clause=extends_content
	;

extends_content returns [ClassDef.ExtendsClause clause]
	{
		List<String> n;
		ClassModification mod = null;
		Annotation ann = null;
	}
	: n=name_path (mod=class_modification)? //(ann=annotation)? TODO: The annotation makes ANTLR burp
		{
			clause = new ClassDef.ExtendsClause();
			clause.from = n;
			clause.modifications = mod;
			clause.annotation = ann;
		}
	;


explicit_import_clause returns [ClassDef.ExplicitImportClause cl]
	{
		List<String> path;
	}
	: i:IDENT EQUALS path=name_path
		{
			cl = new ClassDef.ExplicitImportClause();
			cl.path = path;
			cl.as = i.getText();
		}
	;

implicit_import_clause returns [ClassDef.ImplicitImportClause cl]
	{
		List<String> path;
	}
	: path=name_path (star:DOT STAR)?
		{
			cl = new ClassDef.ImplicitImportClause();
			cl.path = path;
			cl.hasStar = star!=null;
		}
	;

/*
 * 2.2.4 Component clause
 */

type_prefix returns [TypePrefix pre]
	{
		pre = new TypePrefix();
		pre.kind = TypePrefix.TypePrefixKind.NormalPrefix;
		pre.direction = TypePrefix.TypePrefixDirection.Default;
	}
	: (FLOW
		{
			pre.isFlow = true;
		})?
	  ((DISCRETE
	  	{
			pre.kind = TypePrefix.TypePrefixKind.Discrete;
		})
	  |(PARAMETER
	  	{
			pre.kind = TypePrefix.TypePrefixKind.Parameter;
		})
	  |(CONSTANT
	  	{
			pre.kind = TypePrefix.TypePrefixKind.Constant;
		}))?
	  ((INPUT
	  	{
			pre.direction = TypePrefix.TypePrefixDirection.Input;
		})
	  |(OUTPUT
	  	{
			pre.direction = TypePrefix.TypePrefixDirection.Output;
		}))?
	;

type_specifier returns [TypeSpecifier ts]
	{
		List<String> n;
		List<TypeSpecifier> specs = null;
		List<ArraySubscript> subs = null;
	}
	: n=name_path (specs=type_specifier_list)? (subs=array_subscripts)?
		{
			ts = new TypeSpecifier();
			ts.name = n;
			ts.specifiers = specs;
			ts.subscripts = subs;
		}
	;

type_specifier_list returns [LinkedList<TypeSpecifier> specs]
	{
		TypeSpecifier tp;
	}
	: LESS (tp=type_specifier
		{
			specs = new LinkedList<TypeSpecifier>();
			specs.addLast(tp);
		}) (COMMA tp=type_specifier
		{
			specs.addLast(tp);
		})* GREATER
	;

component_list returns [LinkedList<ComponentDeclaration> decls]
	{
		ComponentDeclaration comp;
	}
	: comp=component_declaration
		{
			decls = new LinkedList<ComponentDeclaration>();
			decls.addLast(comp);
		}
	  (COMMA comp=component_declaration
	  	{
			decls.addLast(comp);
		})*
	;

component_declaration returns [ComponentDeclaration comp]
	{
		Declaration decl;
		Expression cond=null;
		Comment com;
	}
	: decl=declaration (cond=conditional_attribute)? com=comment
		{
			comp = new ComponentDeclaration();
			comp.declaration = decl;
			comp.conditional = cond;
			comp.comment = com;
		}
	;

conditional_attribute returns [Expression expr]
	: IF expr=expression
	;

declaration returns [Declaration decl]
	{
		List<ArraySubscript> subs=null;
		ClassModification.Modification mod=null;
	}
	: i:IDENT (subs=array_subscripts)? (mod=modification)?
		{
			decl = new Declaration();
			decl.name = i.getText();
			decl.subscripts = subs;
			decl.modification = mod;
		}
	;

/*
 * 2.2.5 Modification
 */

modification returns [ClassModification.Modification mod]
	{
		ClassModification cmod;
		Expression expr = null;
	}
	: cmod=class_modification (EQUALS expr=expression)?
		{
			ClassModification.DeepModification dmod = new ClassModification.DeepModification();
			dmod.modification = cmod;
			dmod.assigned = expr;
			mod = dmod;
		}
	| EQUALS expr=expression
		{
			ClassModification.EqualsModification emod = new ClassModification.EqualsModification();
			emod.assigned = expr;
			mod = emod;
		}
	| ASSIGN expr=expression
		{
			ClassModification.AssignModification amod = new ClassModification.AssignModification();
			amod.assigned = expr;
			mod = amod;
		}
	;

class_modification returns [ClassModification mod]
	{
		List<ClassModification.Argument> args = null;
	}
	: LPAR (args=argument_list)? RPAR
		{
			mod = new ClassModification();
			mod.arguments = args;
		}
	;

argument_list returns [LinkedList<ClassModification.Argument> args]
	{
		ClassModification.Argument arg;
	}
	: (arg=argument
		{
			args = new LinkedList<ClassModification.Argument>();
			args.add(arg);
		})
	  ((COMMA arg=argument)
	  	{
			args.addLast(arg);
		})*
	;

argument returns [ClassModification.Argument obj]
	{
		ClassDef def;
		TypePrefix prefix;
		TypeSpecifier spec;
		Declaration decl;
		Comment com;
	}
	: (iEach1:EACH)? (iFinal1:FINAL)? (obj=element_modification | obj=element_replaceable)
		{
			obj.isEach = iEach1 != null;
			obj.isFinal = iFinal1 != null;
		}
	| REDECLARE (iEach2:EACH)? (iFinal2:FINAL)? ((def=class_definition
		{
			ClassModification.ElementRedeclaration.NewClass res = new ClassModification.ElementRedeclaration.NewClass();
			res.content = def;
			obj = res;
		}) | (prefix=type_prefix spec=type_specifier decl=declaration com=comment
		{
			ClassModification.ElementRedeclaration.NewDeclaration res = new ClassModification.ElementRedeclaration.NewDeclaration();
			res.prefix = prefix;
			res.specifier = spec;
			res.declaration = decl;
			res.comment = com;
			obj = res;
		}))
	;

//element_redeclaration 

element_modification returns [ClassModification.ElementModification emod]
	{
		ComponentReference comp;
		ClassModification.Modification mod = null;
		List<String> com;
	}
	: comp=component_reference (mod=modification)? com=string_comment
		{
			emod = new ClassModification.ElementModification();
			emod.element = comp;
			emod.modification = mod;
			emod.comment = com;
		}
	;

element_replaceable returns [ClassModification.ElementReplaceable er]
	{
		ClassDef cdef;
		ClassDef.ExtendsClause clause = null;
		Comment com = null;
		TypePrefix pre;
		TypeSpecifier spec;
		Declaration decl;
	}
	: REPLACEABLE ((cdef=class_definition
		{
			ClassModification.ElementReplaceableClass erc = new ClassModification.ElementReplaceableClass();
			erc.classDefinition = cdef;
			er = erc;
		})
	  | (pre=type_prefix spec=type_specifier decl=declaration com=comment
	  	{
			ClassModification.ElementReplaceableComponent erc = new ClassModification.ElementReplaceableComponent();
			erc.prefix = pre;
			erc.specifier = spec;
			erc.declaration = decl;
			erc.comment = com;
			er = erc;
			com = null;
		})) (clause=extends_clause com=comment)?
		{
			if(clause != null) {
				er.extend = clause;
				er.comment = com;
			}
		}
	;

/*
 * 2.2.6 Equations
 */

equation_clause returns [ClassDef.EquationSection sect]
	{
		List<ClassDef.EquationOrAnnotation> cont;
	}
	: EQUATION cont=equation_annotation_list
		{
			sect = new ClassDef.EquationSection();
			sect.isInitial = false;
			sect.equations = cont;
		}
	;

equation_annotation_list returns [LinkedList<ClassDef.EquationOrAnnotation> list]
	{
		Equation eq;
		Annotation an;
		list = new LinkedList<ClassDef.EquationOrAnnotation>();
	}
	: (((eq=equation
		{
			list.addLast(eq);
		})
	   |(an=annotation
	   	{
			list.addLast(an);
		})) SEMICOLON)*
	;

algorithm_clause returns [ClassDef.AlgorithmSection sect]
	{
		List<ClassDef.AlgorithmOrAnnotation> cont;
	}
	: ALGORITHM cont=algorithm_annotation_list
		{
			sect = new ClassDef.AlgorithmSection();
			sect.isInitial = false;
			sect.algorithms = cont;
		}
	;

algorithm_annotation_list returns [LinkedList<ClassDef.AlgorithmOrAnnotation> list]
	{
		Algorithm alg;
		Annotation an;
		list = new LinkedList<ClassDef.AlgorithmOrAnnotation>();
	}
	: (((alg=algorithm
		{
			list.addLast(alg);
		})
	   |(an=annotation
	   	{
			list.addLast(an);
		})) SEMICOLON)*
	;

equation returns [Equation eq]
	: eq=equality_equation
	;

algorithm returns [Algorithm alg]
	: BREAK
		{
			alg = new Algorithm.Break();
		}
	;

equality_equation returns [Equation.NormalEquation eq]
	{
		Expression l,r;
	}
	: l=simple_expression EQUALS r=expression
		{
			eq = new Equation.NormalEquation();
			eq.leftHand = l;
			eq.rightHand = r;
		}
	;

/*
 * 2.2.7 Expressions
 */
expression returns [Expression expr]
	: expr=if_expression
	| expr=simple_expression
	;

if_expression returns [Expression.If res]
	{
		Expression e1,e2;
	}
	: IF e1=expression THEN e2=expression
		{
			res = new Expression.If();
			res.addNode(e1,e2);
		}
	  (ELSEIF e1=expression THEN e2=expression
	  	{
			res.addNode(e1,e2);
		})*
	  ELSE e1=expression
	  	{
			res.elseExpr = e1;
		}
	;

simple_expression returns [Expression.Simple expr]
	{
		Logical.Expression expr1,expr2=null,expr3=null;
	}
	: expr1=logical_expression (COLON expr2=logical_expression (COLON expr3=logical_expression)?)?
		{
			if(expr2!=null) {
				if(expr3!=null) {
					expr = new Expression.Range3();
					((Expression.Range3)expr).expr3 = expr3;
				} else {
					expr = new Expression.Range2();
				}
				((Expression.Range2)expr).expr2 = expr2;
			} else {
				expr = new Expression.Simple();
			}
			expr.expr1 = expr1;
		}
	;

logical_expression returns [Logical.Expression expr]
	{
		Logical.Term term;
	}
	: term=logical_term
		{
			expr = new Logical.Expression();
			expr.terms.add(term);
		} (OR term=logical_term
			{
				expr.terms.add(term);
			})*
	;

logical_term returns [Logical.Term term]
	{
		Logical.Factor factor;
	}
	: factor=logical_factor
		{
			term = new Logical.Term();
			term.factors.add(factor);
		}
		(AND factor=logical_factor
			{
				term.factors.add(factor);
			})*
	;

logical_factor returns [Logical.Factor factor]
	{
		Logical.Relation rel;
	}
	: (i:NOT)? rel=relation
		{
			factor = new Logical.Factor();
			factor.isNegated = i != null;
			factor.relation  = rel;
		}
	;

relation returns [Logical.Relation rel]
	{
		Expression.Arithmetic expr;
	}
	: expr=arithmetic_expression
		{
			rel = new Logical.Relation();
			rel.leftHand = expr;
			rel.kind = Logical.Relation.Kind.UnaryRelation;
		} ( (LESS      {rel.kind=Logical.Relation.Kind.Less;}
		   | LESSEQ    {rel.kind=Logical.Relation.Kind.LessEq;}
		   | GREATER   {rel.kind=Logical.Relation.Kind.Greater;}
		   | GREATEREQ {rel.kind=Logical.Relation.Kind.GreaterEq;}
		   | EQEQ      {rel.kind=Logical.Relation.Kind.Eq;}
		   | LESSGT    {rel.kind=Logical.Relation.Kind.LessGt;}
		   ) expr=arithmetic_expression
		   	{
				rel.rightHand = expr;
			})?
	;

arithmetic_expression returns [Expression.Arithmetic arith]
	{
		Expression.Arithmetic.Term trm;
		Expression.Arithmetic.AddOp op;
		LinkedList<Expression.Arithmetic.TermElement> lst;
		Expression.Arithmetic.TermElement ak;
	}
	: (i1:MINUS)? trm=term
		{
			arith = new Expression.Arithmetic();
			arith.startsWithMinus = i1 != null;
			arith.start = trm;
			lst = new LinkedList<Expression.Arithmetic.TermElement>();
			arith.terms = lst;
		}
	  (op=add_op trm=term
		{
			ak = new Expression.Arithmetic.TermElement();
			ak.op = op;
			ak.term = trm;
			lst.addLast(ak);
		})*
	;

add_op returns [Expression.Arithmetic.AddOp op]
	: PLUS { op = Expression.Arithmetic.AddOp.Plus; }
	| MINUS { op = Expression.Arithmetic.AddOp.Minus; }
	;

term returns [Expression.Arithmetic.Term trm]
	{
		Expression.Arithmetic.Factor fac;
		Expression.Arithmetic.MulOp op;
		LinkedList<Expression.Arithmetic.FactorElement> lst;
		Expression.Arithmetic.FactorElement ak;
	}
	: fac=factor
		{
			trm = new Expression.Arithmetic.Term();
			trm.start = fac;
			lst = new LinkedList<Expression.Arithmetic.FactorElement>();
			trm.factors = lst;
		}
	  (op=mul_op fac=factor
		{
			ak = new Expression.Arithmetic.FactorElement();
			ak.op = op;
			ak.factor = fac;
			lst.addLast(ak);
		})*
	;

mul_op returns [Expression.Arithmetic.MulOp op]
	: STAR	{ op = Expression.Arithmetic.MulOp.Mul; }
	| DIV	{ op = Expression.Arithmetic.MulOp.Div; }
	;

factor returns [Expression.Arithmetic.Factor fac]
	{
		Expression.Arithmetic.Primary prim;
	}
	: prim=primary
		{
			fac = new Expression.Arithmetic.Factor();
			fac.value = prim;
		}
	  (POWER prim=primary
		{
			fac.exponent = prim;
		})?
	;

primary returns [Expression.Arithmetic.Primary prim]
	: i1:UNSIGNED_INTEGER
		{
			Expression.Arithmetic.UnsignedNum num = new Expression.Arithmetic.UnsignedNum();
			num.content = new BigInteger(i1.getText());
			prim = num;
		}
	| i2:STRING
		{
			Expression.Arithmetic.String str = new Expression.Arithmetic.String();
			str.content = i2.getText();
			prim = str;
		}
	| prim=component_reference
	;

within_clause returns [List<String> path]
	: WITHIN path=name_path SEMICOLON
	| WITHIN SEMICOLON
		{ path = new LinkedList<String>(); }
	;

component_reference returns [ComponentReference ref]
	{
		List<ArraySubscript> subs = null;
		ComponentReference rest = null;
	}
	: i:IDENT (subs=array_subscripts)? (DOT rest=component_reference)?
		{
			ComponentSpecific spec;
			spec = new ComponentSpecific();
			spec.name = i.getText();
			spec.subscripts = subs;
			spec.next = rest;
			ref = spec;
		}
	| WILD
		{
			ref = new ComponentWild();
		}
	;

name_path returns [LinkedList<String> path]
	: { LA(2)!=DOT }? i1:IDENT
		{
			path = new LinkedList<String>();
			path.add(i1.getText());
		}
        | i2:IDENT DOT path=name_path
		{
			path.addFirst(i2.getText());
		}
        ;

name_path_star returns [LinkedList<String> path]
	: { LA(2)!=DOT }? i1:IDENT
		{
			path = new LinkedList<String>();
			path.add(i1.getText());
		}
	| { LA(2)!=DOT }? STAR
		{
			path = new LinkedList<String>();
			path.add("*");
		}
	| i2:IDENT DOT path=name_path_star
		{
			path.addFirst(i2.getText());
		}
	;

array_subscripts returns [List<ArraySubscript> subs]
	{
		ArraySubscript sub;
	}
	: LBRACK (sub=subscript
		{
			subs = new LinkedList<ArraySubscript>();
			subs.add(sub);
		})
	  (COMMA sub=subscript
		{
			subs.add(sub);
		})*
	  RBRACK
	;

subscript returns [ArraySubscript sub]
	{
		Expression expr;
	}
	: expr=expression
		{
			sub = new ArraySubscript();
			sub.expression = expr;
		}
	| COLON
		{
			sub = new ArraySubscript();
			sub.expression = null;
		}
	;

string_comment returns [LinkedList<String> com]
	: s1:STRING
		{
			com = new LinkedList<String>();
			com.addFirst(s1.getText());
		}
	  (PLUS s2:STRING
	  	{
			com.addLast(s2.getText());
		})*
	|
		{
			com = null;
		}
	;

comment returns [Comment com]
	{
		List<String> strCom;
		Annotation ann = null;
	}
	: strCom=string_comment (ann=annotation)?
		{
			com = new Comment();
			com.stringComment = strCom;
			com.annotation = ann;
		}
	;

annotation returns [Annotation an]
	{
		ClassModification mod;
	}
	: ANNOTATION mod=class_modification
		{
			an = new Annotation();
			an.modification = mod;
		}
	;
