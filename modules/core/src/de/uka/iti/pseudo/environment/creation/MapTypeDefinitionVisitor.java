package de.uka.iti.pseudo.environment.creation;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTSortDeclaration;
import de.uka.iti.pseudo.parser.file.ASTSortDeclarationBlock;
import de.uka.iti.pseudo.parser.term.ASTMapType;
import de.uka.iti.pseudo.parser.term.ASTType;
import de.uka.iti.pseudo.parser.term.ASTTypeVar;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;

public class MapTypeDefinitionVisitor extends ASTDefaultVisitor {

    private final Environment env;

    public MapTypeDefinitionVisitor(Environment env) {
        this.env = env;
    }

    protected void visitDefault(ASTElement arg) {
    }
    
    private void visitChildren(ASTElement arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    @Override
    public void visit(ASTFile arg) throws ASTVisitException {
        visitChildren(arg);
    }

    @Override
    public void visit(ASTSortDeclarationBlock arg) throws ASTVisitException {
        visitChildren(arg);
    }
    
    @Override
    public void visit(ASTSortDeclaration arg) throws ASTVisitException {
        if(arg.isMapAlias()) {
            registerMapType(arg);
        }
    }
    
    /*
     * For a map type alias: add function symbols and according rules to the
     * environment.
     */
    private void registerMapType(ASTSortDeclaration arg) throws ASTVisitException {

        MapTypeRuleCreator mapType = new MapTypeRuleCreator(arg);
        ASTMapType alias = arg.getAlias();
        
        {
            Sort sort = env.getSort(arg.getName().image);
            assert sort != null : "The sort must have been registered earlier";

            mapType.setSort(sort, retrieveArgumentTypes(arg));
        }
        {
            List<TypeVariable> bound =
                new ArrayList<TypeVariable>(alias.getBoundVars().size());

            for (ASTTypeVar t : alias.getBoundVars()) {
                Type type = TermMaker.makeType(t, env);
                // this is the case due to the parser.
                assert type instanceof TypeVariable;
                bound.add((TypeVariable) type);
            }
            mapType.setBoundVariables(bound);
        }
        {
            List<Type> domain = new ArrayList<Type>(alias.getDomain().size());
            for (ASTType t : alias.getDomain()) {
                Type type = TermMaker.makeType(t, env);
                domain.add(type);
            }
            mapType.setDomain(domain);
        }
        {
            Type range = TermMaker.makeType(alias.getRange(), env);
            mapType.setRange(range);
        }
        
        mapType.check();

        mapType.addFunctionSymbols(env);
        mapType.addRules(env);
    }

    private List<TypeVariable> retrieveArgumentTypes(ASTSortDeclaration arg) {
        List<TypeVariable> result = new ArrayList<TypeVariable>();
        for (Token tyvarToken : arg.getTypeVariables()) {
            result.add(TypeVariable.getInst(tyvarToken.image.substring(1)));
        }
        return result;
    }
    
    
}
