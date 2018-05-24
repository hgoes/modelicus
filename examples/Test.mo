class Real
end Real;

partial model Correlation
	input Real x;
	      Real y = 19;
end Correlation;

model UseCorrelation
	replaceable Correlation corr;
equation
	corr.y=2+time;
end UseCorrelation;

model LineCorrelation
	extends Correlation(x=3);
equation
	x+y=0;
end LineCorrelation;

model Complete = UseCorrelation(redeclare LineCorrelation corr);

connector Broken
	Real x;
	flow Real y;
	flow Real y2;
end Broken;

connector Right
	Real hallo;
	flow Real klops;
end Right;

model ModTest
	Real val(start=10);
end ModTest;

model Blub
    Real x,y,z;
    Right test;
equation
    x = y;
    x = z;
end Blub;

