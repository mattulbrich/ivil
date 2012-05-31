all: selectionSort.algo.p

#remove pascal rules ...
%.o : %.p
% : %.p

selectionSort.algo.p : selectionSort.algo selectionSort.decl.p
	algoc $< $@

%.class : %.java
	javac $<
