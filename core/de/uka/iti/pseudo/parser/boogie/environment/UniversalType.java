package de.uka.iti.pseudo.parser.boogie.environment;

import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;

/**
 * This type class is used to represent used types in the boogie file. It is
 * designed to allow for easy translation into the ivil type system.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class UniversalType {

    /**
     * this is true iff this *type* is a placeholder for a polymorphic type
     * variable
     */
    final boolean isTypeVariable;

    /**
     * null iff this type represents a map, the typename else
     */
    final String name;

    /**
     * this field will be nonnull if the type equals a type synonym
     */
    final String aliasname;

    /**
     * linkage of types is done using pointers
     */
    final UniversalType[] parameters;

    /**
     * if the type was created by a typeconstructor, there can be template
     * arguments
     */
    final UniversalType[] templateArguments;

    /**
     * contains domaintypes
     */
    final UniversalType[] domain;

    final UniversalType range;

    public UniversalType(BuiltInType t){
        isTypeVariable = false;
        name = t.getPrettyName();
        aliasname = null;
        domain = templateArguments = parameters = new UniversalType[0];
        range = null;
    }

    /**
     * This can be used e.g. to check a := [5]5. this statement is valid, if
     * [int]int is a subtype of a, thus a can be declared as "[int]int" or as
     * "<a>[a]int" or as "<a>[a]a"
     * 
     * @param t
     * @return
     */
    public boolean compatible(UniversalType t) {
        /*
         * For example, the types [int]bool and α [α]bool are not compatible.
         * !!!
         */
        
        // TODO implement
        return false;
    }

    public boolean comparable(UniversalType t) {

        return false;
    }
}
