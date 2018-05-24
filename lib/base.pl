:- module(base,
	[class_check/1,class_not_check/1,class_find/1,class_not_find/1
	,model_check/1,model_not_check/1,model_find/1,model_not_find/1
	,connector_check/1,connector_not_check/1,connector_find/1,connector_not_find/1
	,function_check/1,function_not_check/1,function_find/1,function_not_find/1
	,block_check/1,block_not_check/1,block_find/1,block_not_find/1
	,package_check/1,package_not_check/1,package_find/1,package_not_find/1
	,record_check/1,record_not_check/1,record_find/1,record_not_find/1
	,flow_check/1,flow_not_check/1,flow_find/1,flow_not_find/1
	,input_check/1,input_not_check/1,input_find/1,input_not_find/1
	,output_check/1,output_not_check/1,output_find/1,output_not_find/1
	,constant_check/1,constant_not_check/1,constant_find/1,constant_not_find/1
	,parameter_check/1,parameter_not_check/1,parameter_find/1,parameter_not_find/1
	,extends_check/2,extends_not_check/2,extends_by_sub/2,extends_not_by_sub/2,extends_find/2,extends_not_find/2
	,member_by_class_key/3,member_not_by_class_key/3,member_by_class/3,member_not_by_class/3
	,member_by_key/3,member_not_by_key/3,member_find/3,member_not_find/3
	,equation_check/2,equation_by_equation/2,equation_by_class/2,equation_find/2
	,equation_not_check/2,equation_not_by_equation/2,equation_not_by_class/2,equation_not_find/2
	,variable_by_eq/2,variable_not_check/2,variable_find/2,variable_not_by_var/2,variable_not_by_eq/2
	,modification_check/2,modification_find/2,modification_by_modification/2,modification_by_decl/2
    ,modification_not_check/2,modification_not_by_modification/2
    ,typeOf_by_decl/2,typeOf_find/2,typeOf_not_check/2,typeOf_not_by_decl/2,typeOf_not_find/2
    ,name_check/2,name_not_check/2,name_by_name/2,name_not_by_name/2,name_find/2
    ,definition_by_def/2,definition_find/2,definition_not_check/2,definition_not_by_class/2
    ,outer_check/1,outer_not_check/1,outer_find/1,outer_not_find/1
    ,inner_check/1,inner_not_check/1,inner_find/1,inner_not_find/1
    ,assign_modification_check/2,assign_modification_by_mod/2,assign_modification_by_expr/2,assign_modification_find/2
    ,assign_modification_not_check/2,assign_modification_not_by_expr/2
	,(#>)/2
	,(#<)/2
	,(#=)/2
	,(#\=)/2
	,count/3
	,resolve/2
	]).

:- use_module(library(clpfd)).

%class primitive

class_check(N) :- 
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Class',Type).

class_not_check(N) :- \+ class_check(N).

class_find(N) :- any(N)
	,class_check(N).

class_not_find(N) :- any(N)
	,class_not_check(N).

%model primitive

model_check(N) :- 
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Model',Type).

model_not_check(N) :- \+ model_check(N).

model_find(N) :- any(N)
	,model_check(N).

model_not_find(N) :- any(N)
	,model_not_check(N).

%connector primitive

connector_check(N) :- 
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Connector',Type).

connector_not_check(N) :- \+ connector_check(N).

connector_find(N) :- any(N)
	,connector_check(N).

connector_not_find(N) :- any(N)
	,connector_not_check(N).

%function primitive

function_check(N) :-
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Function',Type).

function_not_check(N) :- \+ function_check(N).

function_find(N) :- any(N)
	,function_check(N).

function_not_find(N) :- any(N)
	,function_not_check(N).

%block primitive

block_check(N) :-
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Block',Type).

block_not_check(N) :- \+ block_check(N).

block_find(N) :- any(N)
	,block_check(N).

block_not_find(N) :- any(N)
	,block_not_check(N).

%package primitive

package_check(N) :-
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Package',Type).

package_not_check(N) :- \+ package_check(N).

package_find(N) :- any(N)
	,package_check(N).

package_not_find(N) :- any(N)
	,package_not_check(N).

%record primitive

record_check(N) :-
    jpl_ref_to_type(N,class([modelica,ast],['ClassDef']))
    ,jpl_get(N,classType,Type)
	,jpl_get(class([modelica,ast],['ClassDef','ClassType']),'Record',Type).

record_not_check(N) :- \+ record_check(N).

record_find(N) :- any(N)
	,record_check(N).

record_not_find(N) :- any(N)
	,record_not_check(N).

%extends primitive

%extends_check(Sub,Sup) :- default_element(Sub,Elem)
%	,jpl_ref_to_type(Elem,class([modelica,ast],['ClassDef','ExtendsClause']))
%	,jpl_call(Elem,getResolvedFrom,[],Sup).
extends_check(Sub,Sup) :- get_composition(Sub,Comp)
    ,jpl_get(Comp,resolvedExtended,Ext)
    ,jpl_call(Ext,contains,[Sup],Res)
    ,jpl_true(Res).

extends_by_sub(Sub,Sup) :- get_composition(Sub,Comp)
    ,jpl_get(Comp,resolvedExtended,Ext)
    ,iterable_element(Ext,Sup).

extends_not_check(Sub,Sup) :- \+ extends_check(Sub,Sup).

extends_not_by_sub(Sub,Sup) :- \+ extends_by_sub(Sub,Sup).

extends_find(Sub,Sup) :- any(Sub)
	,extends_by_sub(Sub,Sup).

extends_not_find(Sub,Sup) :- any(Sub)
	,extends_not_by_sub(Sub,Sup).

%member primitive

%flat_to_classdef(Flat,Res) :- 
%	jpl_ref_to_type(Flat
%		,class([modelica,ast],['Flattened','Definition']))
%	,!,jpl_get(Flat,definition,Res).
%flat_to_classdef(Flat,Res) :- Flat=Res.

member_by_class_key(Class,Key,Val) :- get_composition(Class,Comp)
	,jpl_get(Comp,flattened,Flat)
	,jpl_call(Flat,get,[Key],Val)
    ,\+jpl_null(Val).

member_not_by_class_key(Class,Key,Val) :- \+ member_by_class_key(Class,Key,Val).

member_by_class(Class,Key,Val) :- flat_member(Class,-(Key,Val)).

member_not_by_class(Class,Key,Val) :- any(OtherClass)
	,Class\=OtherClass
	,flat_member(OtherClass,-(Key,Val))
	,member_not_by_class_key(Class,Key,Val).

member_by_key(Class,Key,Val) :- any(Class)
	,get_composition(Class,Comp)
	,jpl_get(Comp,flattened,[],Flat)
	,jpl_call(Flat,get,[Key],Val)
    ,\+jpl_null(Val).

member_not_by_key(Class,Key,Val) :- any(Class)
	,get_composition(Class,Comp)
	,jpl_get(Comp,flattened,Flat)
	,\+ jpl_call(Flat,get,[Key],Val).

member_find(Class,Key,Val) :- any(Class)
	,member_by_class(Class,Key,Val).

member_not_find(Class,Key,Val) :- any(Class)
	,member_not_by_class(Class,Key,Val).

%flow primitive

flow_check(Decl) :-
	jpl_ref_to_type(Decl,class([modelica,ast],['Flattened','Declaration']))
	,jpl_get(Decl,prefix,Prefix)
	,jpl_get(Prefix,isFlow,IsFlow)
	,jpl_true(IsFlow).

flow_not_check(Flow) :- \+ flow_check(Flow).

flow_find(Flow) :- any(Class)
	,flat_member(Class,-(_,Flow))
	,flow_check(Flow).

flow_not_find(Flow) :- any(Class)
	,flat_member(Class,-(_,Flow))
	,flow_not_check(Flow).

%general direction primitive, not exported

direction_check(Decl,DirName) :- 
	jpl_ref_to_type(Decl,class([modelica,ast],['Flattened','Declaration']))
	,jpl_get(Decl,prefix,Prefix)
	,jpl_get(Prefix,direction,Dir)
	,jpl_get(class([modelica,ast],['TypePrefix','TypePrefixDirection']),DirName,Dir).

%input primitive

input_check(Inp) :- direction_check(Inp,'Input').

input_not_check(Inp) :- \+ input_check(Inp).

input_find(Inp) :- any(Class)
	,flat_member(Class,-(_,Inp))
	,input_check(Inp).

input_not_find(Inp) :- any(Class)
	,flat_member(Class,-(_,Inp))
	,input_not_check(Inp).

%output primitive

output_check(Outp) :- direction_check(Outp,'Output').

output_not_check(Outp) :- \+ output_check(Outp).

output_find(Outp) :- any(Class)
	,flat_member(Class,-(_,Outp))
	,output_check(Outp).

output_not_find(Outp) :- any(Class)
	,flat_member(Class,-(_,Outp))
	,output_not_check(Outp).

%kind primitive, not exported

kind_check(Decl,KindName) :-
	jpl_ref_to_type(Decl,class([modelica,ast],['Flattened','Declaration']))
	,jpl_get(Decl,prefix,Prefix)
	,jpl_get(Prefix,kind,Kind)
	,jpl_get(class([modelica,ast],['TypePrefix','TypePrefixKind']),KindName,Kind).

%constant primitive

constant_check(Const) :- kind_check(Const,'Constant').

constant_not_check(Const) :- \+ constant_check(Const).

constant_find(Const) :- any(Class)
	,flat_member(Class,-(_,Const))
	,constant_check(Const).

constant_not_find(Const) :- any(Class)
	,flat_member(Class,-(_,Const))
	,constant_not_check(Const).

%parameter primitive

parameter_check(Par) :- kind_check(Par,'Parameter').

parameter_not_check(Par) :- \+ parameter_check(Par).

parameter_find(Par) :- any(Class)
	,flat_member(Class,-(_,Par))
	,parameter_check(Par).

parameter_not_find(Par) :- any(Class)
	,flat_member(Class,-(_,Par))
	,parameter_not_check(Par).

%equation primitive

equation_check(Class,Eq) :- get_composition(Class,Comp)
	,jpl_get(Comp,sections,Sections)
	,iterable_element(Sections,Section)
	,jpl_ref_to_type(Section,class([modelica,ast],['ClassDef','EquationSection']))
	,jpl_get(Section,equations,Equations)
	,iterable_element(Equations,TEq)
	,jpl_call(Eq,equals,[TEq],Res)
	,jpl_true(Res).

equation_by_equation(Class,Eq) :- any(Class)
	,equation_by_class(Class,TEq)
	,jpl_call(Eq,equals,[TEq],Res)
	,jpl_true(Res).

equation_by_class(Class,Eq) :- get_composition(Class,Comp)
	,jpl_get(Comp,sections,Sections)
	,iterable_element(Sections,Section)
	,jpl_ref_to_type(Section,class([modelica,ast],['ClassDef','EquationSection']))
	,jpl_get(Section,equations,Equations)
	,iterable_element(Equations,Eq)
	,jpl_ref_to_type(Eq,class([modelica,ast],['Equation','NormalEquation'])).

equation_find(Class,Eq) :- any(Class)
	,equation_by_class(Class,Eq).

equation_not_check(Class,Eq) :- \+ equation_check(Class,Eq).

equation_not_by_equation(Class,Eq) :- any(Class)
	,equation_by_class(Class,TEq)
	,jpl_call(Eq,equals,[TEq],Res)
	,jpl_false(Res).

equation_not_by_class(Class,Eq) :- any(Class2)
    ,jpl_call(Class,equals,[Class2],Res)
    ,jpl_false(Res)
    ,equation_by_class(Class2,Eq).

equation_not_find(Class,Eq) :- any(Class)
    ,equation_not_by_class(Class,Eq).

%variable primitive

variable_by_eq(Eq,Var) :- jpl_call(Eq,variables,[],Vars)
	,iterable_element(Vars,Var).

variable_not_check(Eq,Var) :- \+ variable_by_eq(Eq,Var).

variable_find(Eq,Var) :- equation_find(_,Eq)
	,variable_by_eq(Eq,Var).

variable_not_by_var(Eq,Var) :- equation_find(_,Eq)
	,variable_not_check(Eq,Var).

variable_not_by_eq(Eq,Var) :- equation_find(_,Eq2)
    ,jpl_call(Eq,equals,[Eq2],Res)
    ,jpl_false(Res)
    ,variable_by_eq(Eq2,Var)
    ,variable_not_check(Eq,Var).

variable_not_find(Eq,Var) :- equation_find(_,Eq)
    ,variable_not_by_eq(Eq,Var).

%modification primitive

modification_check(Decl,Mod) :-
	modification_by_decl(Decl,Mod2)
	,jpl_call(Mod,equals,[Mod2],Res)
	,jpl_true(Res).

modification_not_check(Decl,Mod) :- \+ modification_check(Decl,Mod).

modification_by_modification(Decl,Mod) :-
	member_find(_,_,Decl)
	,modification_check(Decl,Mod).

modification_not_by_modification(Decl,Mod) :-
    member_find(_,_,Decl)
    ,modification_not_check(Decl,Mod).

modification_by_decl(Decl,Mod) :-
	jpl_ref_to_type(Decl,class([modelica,ast],['Flattened','Declaration']))
	,jpl_get(Decl,modification,Mod)
	,\+ jpl_null(Mod).

modification_find(Decl,Mod) :-
	member_find(_,_,Decl)
	,modification_by_decl(Decl,Mod).

%assign_modification primitive

assign_modification_check(Mod,Expr) :- assign_modification_by_mod(Mod,Expr2)
    ,jpl_call(Expr2,equals,[Expr],Res)
    ,jpl_true(Res).

assign_modification_by_mod(Mod,Expr) :- jpl_ref_to_type(Mod,class([modelica,ast],['ClassModification','EqualsModification']))
    ,jpl_get(Mod,assigned,Expr).

assign_modification_by_expr(Mod,Expr) :- modification_find(_,Mod)
    ,assign_modification_check(Mod,Expr).

assign_modification_find(Mod,Expr) :- modification_find(_,Mod)
    ,assign_modification_by_mod(Mod,Expr).

assign_modification_not_check(Mod,Expr) :- \+ assign_modification_check(Mod,Expr).

assign_modification_not_by_expr(Mod,Expr) :- modification_find(_,Mod)
    ,assign_modification_not_check(Mod,Expr).


%typeOf primitive

typeOf_by_decl(Decl,Type) :-
	jpl_ref_to_type(Decl,class([modelica,ast],['Flattened','Declaration']))
    ,jpl_get(Decl,resolvedType,Type).

typeOf_find(Decl,Type) :-
    member_find(_,_,Decl)
    ,typeOf_by_decl(Decl,Type).

typeOf_not_check(Decl,Type) :- \+typeOf_by_decl(Decl,Type).

typeOf_not_by_decl(Decl,Type) :- any(Type)
    ,typeOf_not_check(Decl,Type).

typeOf_not_find(Decl,Type) :- member(_,_,Decl)
    ,typeOf_not_by_decl(Decl,Type).

%name primitive

name_check(Class,Name) :- jpl_call(Class,thisName,[],Name).

name_not_check(Class,Name) :- \+ name_check(Class,Name).

name_by_name(Class,Name) :- any(Class)
    ,name_check(Class,Name).

name_not_by_name(Class,Name) :- any(Class)
    ,name_not_check(Class,Name).

name_find(Class,Name) :- any(Class)
    ,name_by_class(Class,Name).

%definition primitive

definition_by_def(Def,Class) :- jpl_ref_to_type(Def,class([modelica,ast],['Flattened','Definition']))
    ,jpl_get(Def,definition,Class).

definition_find(Def,Class) :- member(_,_,Def)
    ,definition_by_def(Def,Class).

definition_not_check(Def,Class) :- \+ definition_by_def(Def,Class).

definition_not_by_class(Def,Class) :- member(_,_,Def)
    ,definition_not_check(Def,Class).

%outer primitive

outer_check(Def) :- jpl_get(Def,isOuter,Res), jpl_true(Res).
outer_not_check(Def) :- jpl_get(Def,isOuter,Res), jpl_false(Res).

outer_find(Def) :- member(_,_,Def), outer_check(Def).
outer_not_find(Def) :- member(_,_,Def), outer_not_check(Def).

%inner primitive

inner_check(Def) :- jpl_get(Def,isInner,Res), jpl_true(Res).
inner_not_check(Def) :- jpl_get(Def,isInner,Res), jpl_true(Res).

inner_find(Def) :- member(_,_,Def), inner_check(Def).
inner_not_find(Def) :- member(_,_,Def), \+inner_check(Def).

resolve(Name,Res) :-
	ast(Ast),jpl_call(Ast,resolveClass,[Name],Res).

anyDef(Ast,Class) :- jpl_get(Ast,classes,Cls),iterable_element(Cls,Class).

iterable_element(List,Element) :- 
	jpl_call(List,iterator,[],It),jpl_iterator_element(It,Element).

any(Class) :- ast(Ast),anyDef(Ast,Class).

get_composition(Class,Comp) :- jpl_get(Class,spec,Spec)
	,jpl_ref_to_type(Spec,class([modelica,ast],['ClassDef','NormalClassSpec']))
	,jpl_get(Spec,composition,Comp).

default_element(Class,Elem) :- get_composition(Class,Comp)
	,jpl_get(Comp,defaultElements,Elements)
	,iterable_element(Elements,Elem).

flat_member(Class,Member) :- get_composition(Class,Comp)
	,jpl_get(Comp,flattened,Flat)
	,jpl_map_element(Flat,Member).

%count(Targ,Expr,Res) :-
%    setof(Targ,Expr,List),length(List,Res).

count(Targ,Expr,Res) :-
    setof(Targ,Expr,List) *-> length(List,Res) ; Res=0.

%count(Targ,Expr,Res) :-
%	findall(Targ,Expr,List)
%	,sort(List,ListS)
%	,length(ListS,Res).
