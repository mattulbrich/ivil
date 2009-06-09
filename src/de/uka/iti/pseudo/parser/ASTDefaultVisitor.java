package de.uka.iti.pseudo.parser;

import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTGoalAction;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleAdd;
import de.uka.iti.pseudo.parser.file.ASTRuleAssume;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleRemove;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTWhereClause;
import de.uka.iti.pseudo.parser.term.ASTApplicationTerm;
import de.uka.iti.pseudo.parser.term.ASTAsType;
import de.uka.iti.pseudo.parser.term.ASTBinderTerm;
import de.uka.iti.pseudo.parser.term.ASTFixTerm;
import de.uka.iti.pseudo.parser.term.ASTIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTListTerm;
import de.uka.iti.pseudo.parser.term.ASTNumberLiteralTerm;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTProgramTerm;
import de.uka.iti.pseudo.parser.term.ASTSchemaVariableTerm;
import de.uka.iti.pseudo.parser.term.ASTTypeApplication;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;

public abstract class ASTDefaultVisitor implements ASTVisitor {

    /**
     * Extending classes need to implement a default behaviour.
     * 
     * <p>
     * This method will be called unless a visit method is overridden.
     * 
     * @param arg
     *            the file element to apply to
     * 
     * @throws ASTVisitException
     *             may be thrown by any implementation
     */
    protected abstract void visitDefault(ASTElement arg)
            throws ASTVisitException;

    @Override public void visit(ASTApplicationTerm arg)
            throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTBinderTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    @Override public void visit(ASTIdentifierTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTListTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTNumberLiteralTerm arg)
            throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTOperatorIdentifierTerm arg)
            throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTFixTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTAsType arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTSchemaVariableTerm arg)
            throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTProgramTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTTypeApplication arg)
            throws ASTVisitException {
        visitDefault(arg);
    }

    @Override public void visit(ASTTypeVar arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTFile arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTIncludeDeclarationBlock arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTSortDeclarationBlock arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTSortDeclaration arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTFunctionDeclaration arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTFunctionDeclarationBlock arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTRule arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTRuleFind arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTRuleAssume arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTRuleReplace arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTRuleAdd arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTBinderDeclarationBlock arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTBinderDeclaration arg)
            throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTWhereClause arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTGoalAction arg) throws ASTVisitException {
        visitDefault(arg);

    }

    @Override public void visit(ASTRuleRemove arg) throws ASTVisitException {
        visitDefault(arg);

    }

}
