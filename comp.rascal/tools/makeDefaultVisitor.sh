#!/bin/sh 

cat << EOT
package de.uka.iti.pseudo.comp.rascal;

public abstract class DefaultVisitor implements RascalParserVisitor {

    protected abstract Object visitDefault(Node node, Object arg);

EOT

cd genSrc/de/uka/iti/pseudo/comp/rascal

for i in AST*.java SimpleNode.java
do
  j=${i%.java}

  echo "    public Object visit($j node, Object arg) { return visitDefault(node, arg); }"
done

echo "}"

