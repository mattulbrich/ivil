all: sum.algo.p refinement.p

# remove old pascal rules
%.o : %.p
% : %.p

sum.algo.p : sum.algo sum.decl.p
	algoc $< $@

refinement.p : sum.algo sum.decl.p java-out/Sum.sum([I)I-1.p
	algoc -ref $< $@

java-out/Sum.sum([I)I-1.p : java-in/Sum.class
	jbc java-in java-out
	ls -la 'java-out/Sum.sum([I)I-1.p'
	perl -i.bak -np -e 's/R_(.*?)_[a-z]+/\1/g;' 'java-out/Sum.sum([I)I-1.p'

java-in/Sum.class : java-in/Sum.java
	javac $<