package de.uka.iti.pseudo.environment;

import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTType;
import de.uka.iti.pseudo.parser.file.ASTTypeRef;
import de.uka.iti.pseudo.parser.file.ASTTypeVar;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;


public class EnvironmentDefinitionVisitor extends ASTFileDefaultVisitor {

    /**
     * The environment that is being built.
     */
    private Environment env;

    private Type resultingTypeRef;

    public EnvironmentDefinitionVisitor(Environment env) {
        this.env = env;
    }

    /*
     * default behaviour
     * 
     * visit children
     */
    protected void visitDefault(ASTFileElement arg) throws ASTVisitException {
        for (ASTFileElement child : arg.getChildren()) {
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
            if (arity != 0)
                throw new ASTVisitException("Assignable operator " + name
                        + " is not nullary", arg);

            Set<TypeVariable> typVars = TypeVariableCollector.collect(resultTy);

            if (!typVars.isEmpty())
                throw new ASTVisitException("Type of assignable operator "
                        + name + " contains free type variables " + typVars,
                        arg);
        }

        try {
            env.addFunction(new Function(name, resultTy, argTy, arg.isUnique(),
                    arg.isAssignable(), arg));
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }

        if (arg.isInfix()) {
            if (arity != 2)
                throw new ASTVisitException("Arity of infix operator " + name
                        + " is not 2", arg);

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
            if (arity != 1)
                throw new ASTVisitException("Arity of prefix operator " + name
                        + " is not 1", arg);

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
     * Type application
     */
    public void visit(ASTTypeRef arg) throws ASTVisitException {

        String name = arg.getTypeToken().image;

        List<ASTType> argumentTypes = arg.getArgTypes();
        Type domTy[] = new Type[argumentTypes.size()];

        for (int i = 0; i < domTy.length; i++) {
            argumentTypes.get(i).visit(this);
            domTy[i] = resultingTypeRef;
        }

        try {
            resultingTypeRef = env.mkType(name, domTy);
        } catch (Exception e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * type variable
     */
    public void visit(ASTTypeVar arg) throws ASTVisitException {
        resultingTypeRef = new TypeVariable(arg.getTypeVarToken().image
                .substring(1));
    }

}
