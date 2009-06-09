package de.uka.iti.pseudo.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclaration;
import de.uka.iti.pseudo.parser.file.ASTBinderDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTFileDefaultVisitor;
import de.uka.iti.pseudo.parser.file.ASTFileElement;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclaration;
import de.uka.iti.pseudo.parser.file.ASTFunctionDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTIncludeDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTRawTerm;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.file.ASTType;
import de.uka.iti.pseudo.parser.file.ASTTypeRef;
import de.uka.iti.pseudo.parser.file.ASTTypeVar;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.parser.file.ParseException;
import de.uka.iti.pseudo.parser.file.Token;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermMaker;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.util.SelectList;

public class EnvironmentMaker extends ASTFileDefaultVisitor {

    // WHERE?
    private static File SYS_DIR = new File(".");

    private Environment env;

//    private TranslationStack stack;

    private Type resultingTypeRef;
    
    private FileParser parser;

	private Term resultingTerm;

    public EnvironmentMaker(FileParser parser, File file)
            throws FileNotFoundException, ParseException, ASTVisitException {
        this.parser = parser;
        env = new Environment();
        ASTFile f = parser.parseFile(file);
        visit(f);
    }
    
    public Environment getEnvironment() {
        return env;
    }

    private String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    private File mkFile(String filename) {
        File ret;
        if (filename.charAt(0) == '$') {
            ret = new File(SYS_DIR, filename.substring(1));
        } else {
            ret = new File(filename);
        }
        return ret;
    }

    protected void visitDefault(ASTFileElement arg) throws ASTVisitException {
        for (ASTFileElement child : arg) {
            child.visit(this);
        }
    }

    public void visit(ASTFile arg) throws ASTVisitException {

        List<ASTDeclarationBlock> blocks = arg.getDeclarationBlocks();

        for (ASTIncludeDeclarationBlock include : SelectList.select(
                ASTIncludeDeclarationBlock.class, blocks)) {
            include.visit(this);
        }

        for (ASTSortDeclarationBlock sortDecl : SelectList.select(
                ASTSortDeclarationBlock.class, blocks)) {
            sortDecl.visit(this);
        }

        for (ASTFunctionDeclarationBlock funDecl : SelectList.select(
                ASTFunctionDeclarationBlock.class, blocks)) {
            funDecl.visit(this);
        }

        for (ASTBinderDeclarationBlock bindDecl : SelectList.select(
                ASTBinderDeclarationBlock.class, blocks)) {
            bindDecl.visit(this);
        }

        for (ASTRule rule : SelectList.select(ASTRule.class, blocks)) {
            rule.visit(this);
        }

    }

    public void visit(ASTIncludeDeclarationBlock arg) throws ASTVisitException {

        for (Token token : arg.getIncludeStrings()) {
            String filename = stripQuotes(token.image);
            File file = mkFile(filename);
            try {
                visit(parser.parseFile(file));
            } catch (FileNotFoundException e) {
                throw new ASTVisitException("Cannot include " + file
                        + " (not found):", arg);
            } catch (ParseException e) {
                throw new ASTVisitException("Error while including file "
                        + file, e);
            }
        }

    }

    public void visit(ASTSortDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        int arity = arg.getTypeVariables().size();

        env.addSort(new Sort(name, arity, arg));
    }

    public void visit(ASTFunctionDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;

        arg.getRangeType().visit(this);
        Type rangeTy = resultingTypeRef;
        List<ASTType> argumentTypes = arg.getArgumentTypes();
        Type domTy[] = new Type[argumentTypes.size()];

        for (int i = 0; i < domTy.length; i++) {
            argumentTypes.get(i).visit(this);
            domTy[i] = resultingTypeRef;
        }

        env.addFunction(new Function(name, rangeTy, domTy, arg));

        if (arg.isInfix()) {
            String infix = arg.getInfixOperator().image;
            int precedence = Integer.parseInt(arg.getPrecedence().image);
            env
                    .addInfixOperator(new InfixOperator(name, infix,
                            precedence, arg));
        }
    }

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

        env.addBinder(new Binder(name, rangeTy, varTy, domTy, arg));
    }
    
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
        } catch (EnvironmentException e) {
            throw new ASTVisitException("Unknown sort near" + arg.getLocation());
        }
    }
    
    public void visit(ASTTypeVar arg) throws ASTVisitException {
        resultingTypeRef = new TypeVariable(arg.getTypeVarToken().image);
    }

    public void visit(ASTRawTerm arg) throws ASTVisitException {
    	Token token = arg.getTermToken();
    	String content = stripQuotes(token.image);
    	TermMaker termMaker = new TermMaker(content, env, arg.getFileName(), token.beginLine, token.beginColumn);
    	resultingTerm = termMaker.getTerm();
    }

}
