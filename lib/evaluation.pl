
eval(_,[],[]) :- !,true.
eval(Sev,Failed,[]) :- eval(Sev+1,[],Failed).
eval(Sev,Failed,[Goal|Goals]) :-
	Goal =.. List,!,append(List,[Sev,Res],NList),NGoal =.. NList
	,NGoal,(Res -> NFailed = Failed ; NFailed = [Goal|Failed])
	,eval(Sev,NFailed,Goals).
eval(Sev,Failed,[Goal|Goals]) :- Goal,eval(Sev+1,Failed,Goals).

and(List) :- is_list(List),!,eval(0,[],List).
and(List) :- List.

myprint(Call) :- functor(Call,Func,N),write(Func),write('/'),write(N),nl.

solve(true).
solve(Goal) :- 
	\+ Goal = true,
	\+ Goal = and(_),
	\+ Goal = (_ , _),
	\+ Goal = (_ ; _),
	clause(Goal,Body),
	solve(Body).
solve(and(List)) :- solve_and([],List).
solve((Goal1,Goal2)) :-
	solve(Goal1),
	solve(Goal2).
solve((Goal1;Goal2)) :-
	solve(Goal1);
	solve(Goal2).

solve_and([],[]) :- write('done'),nl,true.
solve_and(Failed,[]) :- \+Failed = [],write('Continue with '),write(Failed),nl,solve_and([],Failed).
solve_and(Failed,[Goal|Goals]) :-
	write('Solving: '),write(Goal),write(Failed),write(Goals),nl
	,catch((Goal,write('Succeded: '),write(Goal),nl,NFailed=Failed)
	      ,_
	      ,(write('Failed: '),write(Goal),nl,NFailed=[Goal|Failed]))
	,write('And go on with '),write(NFailed),write(Goals),nl
	,solve_and(NFailed,Goals).

%and(List) :- solve_and([],List).
