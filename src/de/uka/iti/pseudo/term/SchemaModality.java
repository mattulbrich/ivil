package de.uka.iti.pseudo.term;

public class SchemaModality extends Modality {
    
    private String schemaIdentifier;

    public SchemaModality(String id) {
        this.schemaIdentifier = id;
    }

    @Override 
    public boolean equals(Object object) {
        if (object instanceof SchemaModality) {
            SchemaModality mod = (SchemaModality) object;
            return mod.schemaIdentifier.equals(schemaIdentifier);
        }
        return false;
    }

    @Override 
    public String toString(boolean typed) {
        return schemaIdentifier;
    }

    @Override 
    public void visit(ModalityVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public String getName() {
        return schemaIdentifier;
    }

}
