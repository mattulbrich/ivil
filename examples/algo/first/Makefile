all: first.algo.p jbc/First.first(I)I-1.p

#remove pascal rules ...
%.o : %.p
% : %.p

%.algo.p : %.algo
	algoc $< $@

jbc/First.first(I)I-1.p : java/First.java
	jbc java jbc

%.class : %.java
	javac $<
