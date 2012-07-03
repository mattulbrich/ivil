all: sum.algo.p refinement.p

# remove old pascal rules
%.o : %.p
% : %.p

sum.algo.p : sum.algo sum.decl.p
	algoc $< $@

refinement.p : sum.algo sum.decl.p java-out/Sum.sum([I)-1.p
	algoc -ref $< $@

java-out/Sum.sum([I)-1.p : java-in/Sum.class
	jbc java-in java-out

java-in/Sum.class : java-in/Sum.java
	javac $<