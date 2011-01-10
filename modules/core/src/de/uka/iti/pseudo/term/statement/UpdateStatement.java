//package de.uka.iti.pseudo.term.statement;
//
//import de.uka.iti.pseudo.term.TermException;
//import de.uka.iti.pseudo.term.Update;
//
///**
// * This statement was added to allow parallel assignments, as they appear in updates.
// */
//public final class UpdateStatement extends Statement {
//
//    private final Update update;
//    
//    public UpdateStatement(Update update, int sourceLineNumber) {
//        super(sourceLineNumber);
//
//        this.update = update;
//    }
//
//    @Override
//    public String toString(boolean typed) {
//        return update.toString(typed);
//    }
//
//    @Override
//    public void visit(StatementVisitor visitor) throws TermException {
//        visitor.visit(this);
//    }
//
//    public Update getUpdate() {
//        return update;
//    }
//}
