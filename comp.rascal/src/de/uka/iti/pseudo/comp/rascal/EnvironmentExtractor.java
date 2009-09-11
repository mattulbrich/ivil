package de.uka.iti.pseudo.comp.rascal;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentExtractor extends DefaultVisitor {
    
    private Environment env = new Environment();
    
    private TypeMaker typeMaker = new TypeMaker();

    private ASTProcDecl procedureUnderInspection;
    
    private String searchProcName;
    
    public EnvironmentExtractor(String searchProcName) {
        super();
        this.searchProcName = searchProcName;
    }

    // in general ... do nothing.
    @Override 
    protected Object visitDefault(Node node, Object arg) {
        return null;
    }
    
    private void deepVisit(Node node, Object arg) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, arg);
        }
    }

    @Override 
    public Object visit(ASTProgram node, Object arg) {
        deepVisit(node, arg);
        return null;
    }
    
    //
    // Records

    @Override 
    public Object visit(ASTRecordDecl node, Object arg) {
        String id = node.jjtGetChild(0).getImage();
  
        if(env.recordMap.get(id) != null)
            throw new RuntimeException("Record " + id + " already declared");

        RecordDefinition currentRecord = new RecordDefinition(id);
        
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            TokenNode n = node.jjtGetChild(i);
            String field = n.jjtGetChild(0).getImage();
            Type type = (Type) n.jjtGetChild(1).jjtAccept(typeMaker , null);
            currentRecord.addField(field, type);
        }
        
        env.recordMap.put(id, currentRecord);
        currentRecord = null;

        return null;
    }
    
    //
    // Procedures (go deep if name is correct to find var decls)
    
    @Override 
    public Object visit(ASTProcDecl node, Object arg) {
        String name = node.jjtGetChild(0).getImage();
        Procedure contract = new Procedure(name);
        
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            TokenNode n = node.jjtGetChild(i);
            if(n instanceof ASTPrecondition)
                contract.setPrecondition(n.jjtGetChild(0).getToken());
            if(n instanceof ASTPostcondition)
                contract.setPostcondition(n.jjtGetChild(0).getToken());
            if(n instanceof ASTModifies)
                contract.setModifies(n.jjtGetChild(0).getToken());
            
            // it is a shame ... but this is to detect the return type
            if(n instanceof ASTIdentifier || n instanceof ASTArrayType)
                contract.setReturnType((Type) n.jjtAccept(typeMaker , null));
            
            if(n instanceof ASTFormalPars)
                for(int j = 0; j < n.jjtGetNumChildren(); j++) {
                    TokenNode d = n.jjtGetChild(j);
                    String param = d.jjtGetChild(0).getImage();
                    Type type = (Type) d.jjtGetChild(1).jjtAccept(typeMaker , null);
                    contract.addParameter(param, type);
                }
        }
        
        env.contractMap.put(name, contract);
        
        if(name.equals(searchProcName)) {
            deepVisit(node, arg);
            procedureUnderInspection = node;
        }
        return null;
    }
    
    @Override 
    public Object visit(ASTFormalPars node, Object arg) {
        
        Procedure contract = (Procedure) arg;
        
        
        
        return null;
    }
    
    //
    // Variable Declarations

    @Override 
    public Object visit(ASTVarDecl node, Object arg) {
        String id = node.jjtGetChild(0).getImage();
        
        if(env.varMap.get(id) != null)
            throw new RuntimeException("Variable " + id + " already declared");
        
        Type type = (Type) node.jjtGetChild(1).jjtAccept(typeMaker , null);
        
        env.varMap.put(id, type);
        return null;
    }

    public Environment getEnv() {
        return env;
    }

    public ASTProcDecl getProcedureUnderInspection() {
        return procedureUnderInspection;
    }

}
