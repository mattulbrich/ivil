/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.LocalSymbolTable;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProperties;
import de.uka.iti.pseudo.parser.file.ASTPropertiesDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;

// TODO DOC
public class EnvironmentDefinitionVisitor extends ASTDefaultVisitor {

    /**
     * The environment that is being built.
     */
    private final Environment env;

    private Type resultingTypeRef;

    public EnvironmentDefinitionVisitor(Environment env) {
        this.env = env;
    }

    /*
     * default behaviour: depth visiting
     *
     * visit children
     */
    @Override
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    /*
     * create a new sort in env.
     * Do some basic testing:
     * - nullary assignables
     * - no type vars in assignables
     * - arities of fixies
     */
    @Override
    public void visit(ASTSortDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        int arity = arg.getTypeVariables().size();

        try {
            env.addSort(new Sort(name, arity, arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * create a new Function in env
     * rely upon results from children.
     */
    @Override
    public void visit(ASTFunctionDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;

        arg.getRangeType().visit(this);
        Type resultTy = resultingTypeRef;
        List<ASTType> argumentTypes = arg.getArgumentTypes();
        Type argTy[] = new Type[argumentTypes.size()];
        int arity = argTy.length;

        for (int i = 0; i < arity; i++) {
            argumentTypes.get(i).visit(this);
            argTy[i] = resultingTypeRef;
        }

        if (arg.isAssignable()) {
            if (arity != 0) {
                throw new ASTVisitException("Assignable operator " + name
                        + " is not nullary", arg);
            }

            Set<TypeVariable> typVars = TypeVariableCollector.collect(resultTy);

            if (!typVars.isEmpty()) {
                throw new ASTVisitException("Type of assignable operator "
                        + name + " contains free type variables " + typVars,
                        arg);
            }
        }

        try {
            env.addFunction(new Function(name, resultTy, argTy, arg.isUnique(),
                    arg.isAssignable(), arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }

        if (arg.isInfix()) {
            if (arity != 2) {
                throw new ASTVisitException("Arity of infix operator " + name
                        + " is not 2", arg);
            }

            String infix = arg.getOperatorIdentifier().image;
            int precedence = Integer.parseInt(arg.getPrecedence().image);
            try {
                env.addInfixOperator(new FixOperator(name, infix, precedence,
                        2, arg));
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }
        }

        if (arg.isPrefix()) {
            if (arity != 1) {
                throw new ASTVisitException("Arity of prefix operator " + name
                        + " is not 1", arg);
            }

            String prefix = arg.getOperatorIdentifier().image;
            int precedence = Integer.parseInt(arg.getPrecedence().image);
            try {
                env.addPrefixOperator(new FixOperator(name, prefix, precedence,
                        1, arg));
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }
        }
    }

    /*
     * create a binder
     */
    @Override
    public void visit(ASTBinderDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;

        arg.getRangeType().visit(this);
        Type rangeTy = resultingTypeRef;

        arg.getVariableType().visit(this);
        Type varTy = resultingTypeRef;

        List<ASTType> argumentTypes = arg.getTypeReferenceList();
        Type domTy[] = new Type[argumentTypes.size()];

        for (int i = 0; i < domTy.length; i++) {
            argumentTypes.get(i).visit(this);
            domTy[i] = resultingTypeRef;
        }

        try {
            env.addBinder(new Binder(name, rangeTy, varTy, domTy, arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * Types
     */
    @Override
    public void visitDefaultType(ASTType arg) throws ASTVisitException {
        // In environment definitions, there are no local symbols
        resultingTypeRef = TermMaker.makeType(arg, env, LocalSymbolTable.EMPTY);
    }

    @Override
    public void visit(ASTProperties plugins) throws ASTVisitException {
        for(ASTElement child : plugins.getChildren()) {
            child.visit(this);
        }
    }

    @Override
    public void visit(ASTPropertiesDeclaration property) throws ASTVisitException {
        String value = property.getValue();
        env.addProperty(property.getName(), value.substring(1, value.length()-1));
    }
}
