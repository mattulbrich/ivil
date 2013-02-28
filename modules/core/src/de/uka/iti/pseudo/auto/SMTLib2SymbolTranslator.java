package de.uka.iti.pseudo.auto;

import static de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType.UNIVERSE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import de.uka.iti.pseudo.auto.SMTLib2Translator.ExpressionType;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.util.Util;

class SMTLib2SymbolTranslator {

    private enum SymbolType { FUNCTION, BINDER };
    private final SymbolType type;
    private final String name;
    private final SortedSet<TypeVariable> allTypeVariables;
    private final ExpressionType resultType;
    private final Type[] symbArgTypes;
    private final ExpressionType[] argTypes;
    private final Type symbResultType;
    private final SMTLib2Translator translator;
    private final ExpressionType bindervarType;

    public SMTLib2SymbolTranslator(SMTLib2Translator translator, Function symbol) {
        this.translator = translator;
        this.type = SymbolType.FUNCTION;

        String fctName = symbol.getName();
        String name = "fct." + fctName;
        this.name = name.replace('$', '.');

        this.symbResultType = symbol.getResultType();
        this.resultType = SMTLib2Translator.typeToExpressionType(symbResultType);
        this.symbArgTypes = symbol.getArgumentTypes();
        this.argTypes = makeFunctionArgTypes();
        this.allTypeVariables = SMTLib2Translator.collectTypeVars(symbol);
        this.bindervarType = null;
    }



    public SMTLib2SymbolTranslator(SMTLib2Translator translator, Binder binder) {
        this.translator = translator;
        this.type = SymbolType.BINDER;

        String binderName = binder.getName();
        // drop the initial "\\"
        String name = "bnd." + binderName.substring(1);
        this.name = name.replace('$', '.');

        // var type
        this.bindervarType = SMTLib2Translator.typeToExpressionType(binder.getVarType());

        // result type
        this.symbResultType = binder.getResultType();
        this.resultType = SMTLib2Translator.typeToExpressionType(symbResultType);
        this.symbArgTypes = binder.getArgumentTypes();
        this.argTypes = makeFunctionArgTypes();
        this.allTypeVariables = SMTLib2Translator.collectTypeVars(binder);

    }

    public String getDefinition() {
        List<String> types = new ArrayList<String>();
        types.addAll(Collections.nCopies(allTypeVariables.size(), "Type"));
        switch(type) {
        case FUNCTION:
            for (ExpressionType exTy : argTypes) {
                types.add(exTy.toString());
            }
            break;
        case BINDER:
            for (ExpressionType exTy : argTypes) {
                types.add("(Array " + bindervarType + " " + exTy.toString() + ")");
            }
            break;
        }

        return name +" (" + Util.join(types, " ") + ") " + resultType;
    }

    private ExpressionType[] makeFunctionArgTypes() {
        ExpressionType[] argTypes = new ExpressionType[symbArgTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = SMTLib2Translator.typeToExpressionType(symbArgTypes[i]);
        }
        return argTypes;
    }

    private SExpr getVariableBank() {
        SExpr allVariables = new SExpr();
        for (TypeVariable typeVariable : allTypeVariables) {
            allVariables.add(
                    new SExpr("?Type." + typeVariable.getVariableName()).add("Type"));
        }

        for (int i = 0; i < argTypes.length; i++) {
            ExpressionType exTy = argTypes[i];
            SExpr t;
            switch(type) {
            case FUNCTION:
                t = new SExpr(exTy.toString());
                break;
            case BINDER:
                t = new SExpr("Array").
                    add(bindervarType.toString()).
                    add(exTy.toString());
                break;
            default: throw new Error("unreachable");
            }

            allVariables.add(new SExpr("?x" + i).add(t));
        }

        return allVariables;
    }

    private SExpr getInvocation() {
        SExpr invocation = new SExpr(name);
        for (TypeVariable typeVariable : allTypeVariables) {
            invocation.add("?Type." + typeVariable.getVariableName());
        }
        for (int i = 0; i < argTypes.length; i++) {
            invocation.add("?x" + i);
        }
        return invocation;
    }

    public boolean isUniverseFunction() {
        return resultType == ExpressionType.UNIVERSE;
    }

    public SExpr makeTypingAxiom() throws TermException {

        if (allTypeVariables.isEmpty() && argTypes.length == 0) {

            // for a monomorphic constant symbols "bool c" add
            // "(= (ty fct.c) ty.bool)"
            return new SExpr("ty").
                    add(name).
                    add(symbResultType.accept(translator.typeToTerm, false));

        } else {

            String resType = symbResultType.accept(translator.typeToTerm, true);
            SExpr invocation = getInvocation();
            return new SExpr("forall").
                    add(getVariableBank()).
                    add(new SExpr("!").
                            add(new SExpr("ty").add(invocation).add(resType)).
                            add(":pattern").
                            add(new SExpr(invocation)));
        }
    }

    public String getName() {
        return name;
    }

    /*
     * For a unique function symbol U return the assumption that
     *
     * u(...) = val in which counter is a value value
     */
    public SExpr makeUniqueMapAssumption(int uniqueCounter) {
        SExpr sexpr;


        if (argTypes.length == 0 && allTypeVariables.isEmpty()) {
            sexpr = new SExpr("=").
                      add(new SExpr("unique").add(name)).
                      add(Integer.toString(uniqueCounter));

        } else {


            SExpr allVariables = new SExpr();
            for (TypeVariable typeVariable : allTypeVariables) {
                allVariables.add(
                        new SExpr("?Type." + typeVariable.getVariableName()).add("Type"));
            }
            for (int i = 0; i < argTypes.length; i++) {
                allVariables.add(new SExpr("?x" + i).add(argTypes[i].toString()));
            }
            SExpr invoc = new SExpr(name);
            for (TypeVariable typeVariable : allTypeVariables) {
                invoc.add("?Type." + typeVariable.getVariableName());
            }
            for (int i = 0; i < argTypes.length; i++) {
                invoc.add("?x" + i);
            }

            SExpr eq = new SExpr("=").
                    add(new SExpr("unique").add(invoc)).
                    add(Integer.toString(uniqueCounter));

            SExpr trigger = new SExpr("!").add(eq).add(":pattern").add(new SExpr(invoc));
            sexpr = new SExpr("forall").add(allVariables).add(trigger);
        }

        return sexpr;
    }

    /*
     * for a unique function symbol f(.,.) and arg = 0 add
     * (forall ((?x) (?y)) (! (=> (ty ?x TY) (= (invf.f.0 (f ?x ?y)) ?x) :pattern (f ?x ?y))))
     */
    public SExpr makeUniquenessInArg(int arg) throws TermException {
        SExpr invocation = getInvocation();
        String inv = "inv" + name + "." + arg;
        translator.extrafuncs.add(inv + " (" + resultType + ") " + argTypes[arg]);

        String xarg = "?x" + arg;
        String ty = symbArgTypes[arg].accept(translator.typeToTerm, false);
        SExpr equality = new SExpr("=").
                add(new SExpr(inv).add(invocation)).
                add(xarg);

        SExpr triggered;
        if(argTypes[arg] == UNIVERSE) {
            // (=> (ty ?xARG TY) (equality))
            triggered = new SExpr("=>").add(new SExpr("ty").add(xarg).add(ty)).add(equality);
        } else {
            // implication only if of "universe" type
            triggered = equality;
        }

        SExpr trigger = new SExpr("!").add(triggered).add(":pattern").add(new SExpr(invocation));

        SExpr forall = new SExpr("forall").add(getVariableBank()).add(trigger);
        return forall;
    }
}
