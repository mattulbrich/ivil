/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import de.uka.iti.pseudo.parser.boogie.ast.*;
import de.uka.iti.pseudo.parser.boogie.ast.type.*;
import de.uka.iti.pseudo.parser.boogie.ast.expression.*;
// used for main
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationState;
// we dont want warnings here, as the code is created by a generator
@ SuppressWarnings("all") public class BPLParser implements BPLParserConstants {
  public CompilationUnit parseFile(File file) throws FileNotFoundException, ParseException, MalformedURLException {
        return parseFile(new FileReader(file), file.toURI().toURL());
    }

    public CompilationUnit parseURL(URL url) throws ParseException, IOException {
        Reader reader = new InputStreamReader(url.openStream());
        return parseFile(reader, url);
    }

    public CompilationUnit parseFile(Reader reader, URL location) throws ParseException {
        ReInit(reader);
        CompilationUnit result = parse(location);
        result.setFilename(location.toString());
        return result;
    }

    /**
     * Try to prove all problems supplied in args as paths to their defining
     * files.
     */
    public static void main(String args[]) throws FileNotFoundException, ParseException {
        for (int i = 0; i < args.length; i++) {
            BPLParser p = new BPLParser(new FileInputStream(args[i]));
            EnvironmentCreationState creator;
            try {
                creator = new EnvironmentCreationState(p.parse(new File(args[i]).toURI().toURL()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }
            creator.make();
        }
    }

/*---------------------------------------------------------------------------
// BoogiePL -
//--------------------------------------------------------------------------*/
/*------------------------------------------------------------------------*/
  final public CompilationUnit parse(URL location) throws ParseException {
  DeclarationBlock block;
  List < DeclarationBlock > declarations = new LinkedList < DeclarationBlock > ();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VAR:
      case CONST:
      case FUNCTION:
      case AXIOM:
      case TYPE:
      case PROCEDURE:
      case IMPLEMENTATION:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CONST:
        block = Consts();
        break;
      case FUNCTION:
        block = Function();
        break;
      case AXIOM:
        block = Axiom();
        break;
      case TYPE:
        block = UserDefinedTypes();
        break;
      case VAR:
        block = GlobalVars();
        break;
      case PROCEDURE:
        block = Procedure();
        break;
      case IMPLEMENTATION:
        block = Implementation();
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      declarations.add(block);
    }
    jj_consume_token(0);
    {if (true) return new CompilationUnit(location, declarations);}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public GlobalVariableDeclaration GlobalVars() throws ParseException {
  Token first;
  List < Attribute > attr;
  List < VariableDeclaration > vars;
    first = jj_consume_token(VAR);
    attr = AttributeList();
    vars = IdsTypeWheres(false, false, false);
    jj_consume_token(73);
    {if (true) return new GlobalVariableDeclaration(first, attr, vars);}
    throw new Error("Missing return statement in function");
  }

  final public LocalVariableDeclaration LocalVars() throws ParseException {
  Token first;
  List < Attribute > attr;
  List < VariableDeclaration > vars;
    first = jj_consume_token(VAR);
    attr = AttributeList();
    vars = IdsTypeWheres(false, false, false);
    jj_consume_token(73);
    {if (true) return new LocalVariableDeclaration(first, attr, vars);}
    throw new Error("Missing return statement in function");
  }

  final public List < VariableDeclaration > ProcFormals(boolean isConstant) throws ParseException {
  List < VariableDeclaration > vars = new LinkedList < VariableDeclaration > ();
    jj_consume_token(74);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
      vars = IdsTypeWheres(isConstant, false, false);
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    jj_consume_token(75);
    {if (true) return vars;}
    throw new Error("Missing return statement in function");
  }

  final public List < VariableDeclaration > BoundVars() throws ParseException {
  List < VariableDeclaration > rval;
    rval = IdsTypeWheres(false, false, true);
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
/* IdsType is used with const declarations */
  final public List < VariableDeclaration > IdsType(boolean isConstant, boolean isUnique) throws ParseException {
  List < Token > names;
  ASTType t;
    names = Idents();
    jj_consume_token(76);
    t = ASTType();
    List < VariableDeclaration > rval = new LinkedList < VariableDeclaration > ();
    for (Token name : names) rval.add(new VariableDeclaration(name, t, isConstant, isUnique, false, null));
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/* IdsTypeWheres is used with the declarations of global and local variables,
   procedure parameters, and quantifier bound variables. */
  final public List < VariableDeclaration > IdsTypeWheres(boolean isConstant, boolean isUnique, boolean isQuantified) throws ParseException {
  List < VariableDeclaration > rval, tmp;
    rval = IdsTypeWhere(isConstant, isUnique, isQuantified);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 77:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_2;
      }
      jj_consume_token(77);
      tmp = IdsTypeWhere(isConstant, isUnique, isQuantified);
      rval.addAll(tmp);
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public List < VariableDeclaration > IdsTypeWhere(boolean isConstant, boolean isUnique, boolean isQuantified) throws ParseException {
  List < Token > names;
  ASTType t;
  Expression e = null;
    names = Idents();
    jj_consume_token(76);
    t = ASTType();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case WHERE:
      jj_consume_token(WHERE);
      e = Expression();
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    List < VariableDeclaration > rval = new LinkedList < VariableDeclaration > ();
    for (Token name : names) rval.add(new VariableDeclaration(name, t, isConstant, isUnique, isQuantified, e));
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public ASTType ASTType() throws ParseException {
  ASTType t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INT:
    case BOOL:
    case BVTYPE:
    case 74:
      t = TypeAtom();
      break;
    case IDENT:
        Token name;
        //types needs initialization because TypeArgs is optional
        List < ASTType > types = new LinkedList < ASTType > ();
      name = jj_consume_token(IDENT);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INT:
      case BOOL:
      case BVTYPE:
      case SEP_LBRACKET:
      case IDENT:
      case 74:
      case 78:
        types = TypeArgs();
        break;
      default:
        jj_la1[5] = jj_gen;
        ;
      }
        {if (true) return new ASTTypeApplication(name, types);}
      break;
    case SEP_LBRACKET:
    case 78:
      t = MapType();
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return t;}
    throw new Error("Missing return statement in function");
  }

  final public List < ASTType > TypeArgs() throws ParseException {
  ASTType t;
  List < ASTType > rval = new LinkedList < ASTType > (), tmp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INT:
    case BOOL:
    case BVTYPE:
    case 74:
      t = TypeAtom();
      rval.add(t);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INT:
      case BOOL:
      case BVTYPE:
      case SEP_LBRACKET:
      case IDENT:
      case 74:
      case 78:
        tmp = TypeArgs();
        rval.addAll(tmp);
        break;
      default:
        jj_la1[7] = jj_gen;
        ;
      }
      break;
    case IDENT:
      Token name;
      name = jj_consume_token(IDENT);
      rval.add(new ASTTypeApplication(name, new LinkedList < ASTType > ()));
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INT:
      case BOOL:
      case BVTYPE:
      case SEP_LBRACKET:
      case IDENT:
      case 74:
      case 78:
        tmp = TypeArgs();
        rval.addAll(tmp);
        break;
      default:
        jj_la1[8] = jj_gen;
        ;
      }
      break;
    case SEP_LBRACKET:
    case 78:
      t = MapType();
      rval.add(t);
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public ASTType TypeAtom() throws ParseException {
  Token t;
  ASTType rval;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INT:
    case BOOL:
    case BVTYPE:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INT:
        t = jj_consume_token(INT);
        break;
      case BOOL:
        t = jj_consume_token(BOOL);
        break;
      case BVTYPE:
        t = jj_consume_token(BVTYPE);
        break;
      default:
        jj_la1[10] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      {if (true) return new BuiltInType(t);}
      break;
    case 74:
      jj_consume_token(74);
      rval = ASTType();
      jj_consume_token(75);
      {if (true) return rval;}
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public MapType MapType() throws ParseException {
  Token first = null, tmp;
  List < Token > params = new LinkedList < Token > ();
  List < ASTType > domain = new LinkedList < ASTType > ();
  ASTType range;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 78:
      first = jj_consume_token(78);
      params = Idents();
      jj_consume_token(79);
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
    tmp = jj_consume_token(SEP_LBRACKET);
    if (null == first) first = tmp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INT:
    case BOOL:
    case BVTYPE:
    case SEP_LBRACKET:
    case IDENT:
    case 74:
    case 78:
      domain = Types();
      break;
    default:
      jj_la1[13] = jj_gen;
      ;
    }
    jj_consume_token(SEP_RBRACKET);
    range = ASTType();
    {if (true) return new MapType(first, params, domain, range);}
    throw new Error("Missing return statement in function");
  }

  final public List < Token > TypeParams() throws ParseException {
  List < Token > L;
    jj_consume_token(78);
    L = Idents();
    jj_consume_token(79);
    {if (true) return L;}
    throw new Error("Missing return statement in function");
  }

  final public List < ASTType > Types() throws ParseException {
  ASTType t;
  List < ASTType > types = new LinkedList < ASTType > ();
    t = ASTType();
    types.add(t);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 77:
        ;
        break;
      default:
        jj_la1[14] = jj_gen;
        break label_3;
      }
      jj_consume_token(77);
      t = ASTType();
      types.add(t);
    }
    {if (true) return types;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public ConstantDeclaration Consts() throws ParseException {
  Token first;
  List < Attribute > attributes;
  Boolean unique = false;
  List < VariableDeclaration > varnames;
  // used if extends is present
  Token ext, name;
  List < ExtendsParent > parents = null;
  boolean unique_edge = false;
  boolean complete = false;
    first = jj_consume_token(CONST);
    attributes = AttributeList();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case UNIQUE:
      jj_consume_token(UNIQUE);
    unique = true;
      break;
    default:
      jj_la1[15] = jj_gen;
      ;
    }
    varnames = IdsType(true, unique);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EXTENDS:
      ext = jj_consume_token(EXTENDS);
      parents = new LinkedList < ExtendsParent > ();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case UNIQUE:
      case IDENT:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case UNIQUE:
          jj_consume_token(UNIQUE);
        unique_edge = true;
          break;
        default:
          jj_la1[16] = jj_gen;
          ;
        }
        name = jj_consume_token(IDENT);
        parents.add(new ExtendsParent(unique_edge, name));
        unique_edge = false;
        label_4:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 77:
            ;
            break;
          default:
            jj_la1[17] = jj_gen;
            break label_4;
          }
          jj_consume_token(77);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case UNIQUE:
            jj_consume_token(UNIQUE);
          unique_edge = true;
            break;
          default:
            jj_la1[18] = jj_gen;
            ;
          }
          name = jj_consume_token(IDENT);
          parents.add(new ExtendsParent(unique_edge, name));
          unique_edge = false;
        }
        break;
      default:
        jj_la1[19] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMPLETE:
        jj_consume_token(COMPLETE);
      complete = true;
        break;
      default:
        jj_la1[20] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[21] = jj_gen;
      ;
    }
    jj_consume_token(73);
    {if (true) return new ConstantDeclaration(first, attributes, unique, varnames, parents, complete);}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public FunctionDeclaration Function() throws ParseException {
  //throw new ParseException("implement signature");
  Token first;
  List < Attribute > attributes;
  Token name;
  List < Token > typeParameters = new LinkedList < Token > ();
  VariableDeclaration var;
  List < VariableDeclaration > inParam = new LinkedList < VariableDeclaration > ();
  VariableDeclaration outParam;
  ASTType tmp;
  Expression expression = null;
    first = jj_consume_token(FUNCTION);
    attributes = AttributeList();
    name = jj_consume_token(IDENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 78:
      typeParameters = TypeParams();
      break;
    default:
      jj_la1[22] = jj_gen;
      ;
    }
    jj_consume_token(74);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INT:
    case BOOL:
    case BVTYPE:
    case SEP_LBRACKET:
    case IDENT:
    case 74:
    case 78:
      var = VarOrType("in_"+ inParam.size());
      inParam.add(var);
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 77:
          ;
          break;
        default:
          jj_la1[23] = jj_gen;
          break label_5;
        }
        jj_consume_token(77);
        var = VarOrType("in_"+ inParam.size());
        inParam.add(var);
      }
      break;
    default:
      jj_la1[24] = jj_gen;
      ;
    }
    jj_consume_token(75);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case RETURNS:
      jj_consume_token(RETURNS);
      jj_consume_token(74);
      outParam = VarOrType("rval");
      jj_consume_token(75);
      break;
    case 76:
      jj_consume_token(76);
      tmp = ASTType();
      outParam = new VariableDeclaration("rval", tmp, false, false, false, null);
      break;
    default:
      jj_la1[25] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SEP_LCURLY:
      jj_consume_token(SEP_LCURLY);
      expression = Expression();
      jj_consume_token(SEP_RCURLY);
      break;
    case 73:
      jj_consume_token(73);
      break;
    default:
      jj_la1[26] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return new FunctionDeclaration(first, attributes, name, typeParameters, inParam, outParam, expression);}
    throw new Error("Missing return statement in function");
  }

  final public VariableDeclaration VarOrType(String optName) throws ParseException {
  ASTElement rval;
  ASTType tmp;
    rval = ASTType();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 76:
      jj_consume_token(76);
      tmp = ASTType();
      if (!(rval instanceof ASTTypeApplication)) {if (true) throw new ParseException("At " + rval.getLocation() + ":: Expected Identifier but found " + rval.getClass().toString());}
      rval = new VariableDeclaration(((ASTTypeApplication) rval).getLocationToken(), tmp, false, false, false, null);
      break;
    default:
      jj_la1[27] = jj_gen;
      ;
    }
    if (!(rval instanceof VariableDeclaration)) rval = new VariableDeclaration(optName, (ASTType) rval, false, false, false, null);
    {if (true) return (VariableDeclaration) rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public AxiomDeclaration Axiom() throws ParseException {
  Token first;
  List < Attribute > attributes;
  Expression axiom;
    first = jj_consume_token(AXIOM);
    attributes = AttributeList();
    axiom = Expression();
    jj_consume_token(73);
    {if (true) return new AxiomDeclaration(first, attributes, axiom);}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public UserDefinedTypeDeclaration UserDefinedTypes() throws ParseException {
  Token first;
  List < Attribute > attr;
  List < UserTypeDefinition > typedefs = new LinkedList < UserTypeDefinition > ();
  UserTypeDefinition t;
    first = jj_consume_token(TYPE);
    attr = AttributeList();
    t = UserDefinedType();
    typedefs.add(t);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 77:
        ;
        break;
      default:
        jj_la1[28] = jj_gen;
        break label_6;
      }
      jj_consume_token(77);
      t = UserDefinedType();
      typedefs.add(t);
    }
    jj_consume_token(73);
    {if (true) return new UserDefinedTypeDeclaration(first, attr, typedefs);}
    throw new Error("Missing return statement in function");
  }

  final public UserTypeDefinition UserDefinedType() throws ParseException {
  Token name;
  List < Token > argnames = new LinkedList < Token > ();
  ASTType parent = null;
    name = jj_consume_token(IDENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
      argnames = WhiteSpaceIdents();
      break;
    default:
      jj_la1[29] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 80:
      jj_consume_token(80);
      parent = ASTType();
      break;
    default:
      jj_la1[30] = jj_gen;
      ;
    }
    {if (true) return new UserTypeDefinition(name, argnames, parent);}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public ProcedureDeclaration Procedure() throws ParseException {
  Token first, name;
  List < Attribute > attr;
  List < Token > typeParameters = new LinkedList < Token > ();
  List < VariableDeclaration > inParam, outParam = new LinkedList < VariableDeclaration > ();
  Specification S;
  List < Specification > specs = new LinkedList < Specification > ();
  ProcedureBody body = null;
    first = jj_consume_token(PROCEDURE);
    attr = AttributeList();
    name = jj_consume_token(IDENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 78:
      typeParameters = TypeParams();
      break;
    default:
      jj_la1[31] = jj_gen;
      ;
    }
    inParam = ProcFormals(true);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case RETURNS:
      jj_consume_token(RETURNS);
      outParam = ProcFormals(false);
      break;
    default:
      jj_la1[32] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 73:
      jj_consume_token(73);
      label_7:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case MODIFIES:
        case FREE:
        case REQUIRES:
        case ENSURES:
          ;
          break;
        default:
          jj_la1[33] = jj_gen;
          break label_7;
        }
        S = Spec();
        specs.add(S);
      }
      break;
    default:
      jj_la1[36] = jj_gen;
      label_8:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case MODIFIES:
        case FREE:
        case REQUIRES:
        case ENSURES:
          ;
          break;
        default:
          jj_la1[34] = jj_gen;
          break label_8;
        }
        S = Spec();
        specs.add(S);
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SEP_LCURLY:
        body = ImplBody();
        break;
      default:
        jj_la1[35] = jj_gen;
        ;
      }
    }
    {if (true) return new ProcedureDeclaration(first, attr, name, typeParameters, inParam, outParam, specs, body);}
    throw new Error("Missing return statement in function");
  }

  final public ProcedureImplementation Implementation() throws ParseException {
  Token first, name;
  List < Attribute > attr;
  List < Token > typeParameters = new LinkedList < Token > ();
  List < VariableDeclaration > inParam, outParam = new LinkedList < VariableDeclaration > ();
  ProcedureBody body;
    first = jj_consume_token(IMPLEMENTATION);
    attr = AttributeList();
    name = jj_consume_token(IDENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 78:
      typeParameters = TypeParams();
      break;
    default:
      jj_la1[37] = jj_gen;
      ;
    }
    inParam = ProcFormals(true);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case RETURNS:
      jj_consume_token(RETURNS);
      outParam = ProcFormals(false);
      break;
    default:
      jj_la1[38] = jj_gen;
      ;
    }
    body = ImplBody();
    {if (true) return new ProcedureImplementation(first, attr, name, typeParameters, inParam, outParam, body);}
    throw new Error("Missing return statement in function");
  }

  final public Specification Spec() throws ParseException {
  Token first = null, tmp;
  List < Token > targets = null;
  Expression expr;
  List < Attribute > attr;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case MODIFIES:
      first = jj_consume_token(MODIFIES);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENT:
        targets = Idents();
        break;
      default:
        jj_la1[39] = jj_gen;
        ;
      }
      jj_consume_token(73);
      {if (true) return new ModifiesClause(first, targets);}
      break;
    case FREE:
    case REQUIRES:
    case ENSURES:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FREE:
        first = jj_consume_token(FREE);
        break;
      default:
        jj_la1[40] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case REQUIRES:
        tmp = jj_consume_token(REQUIRES);
        attr = AttributeList();
        expr = Expression();
        jj_consume_token(73);
          if (null == first) {if (true) return new Precondition(tmp, false, attr, expr);} // checked condition
          else {if (true) return new Precondition(first, true, attr, expr);}
        break;
      case ENSURES:
        tmp = jj_consume_token(ENSURES);
        attr = AttributeList();
        expr = Expression();
        jj_consume_token(73);
          if (null == first) {if (true) return new Postcondition(tmp, false, attr, expr);} // checked condition
          else {if (true) return new Postcondition(first, true, attr, expr);}
        break;
      default:
        jj_la1[41] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[42] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public ProcedureBody ImplBody() throws ParseException {
  Token first;
  LocalVariableDeclaration var;
  List < LocalVariableDeclaration > vars = new LinkedList < LocalVariableDeclaration > ();
  List < Statement > statements;
    first = jj_consume_token(SEP_LCURLY);
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VAR:
        ;
        break;
      default:
        jj_la1[43] = jj_gen;
        break label_9;
      }
      var = LocalVars();
      vars.add(var);
    }
    statements = StmtList();
    {if (true) return new ProcedureBody(first, vars, statements);}
    throw new Error("Missing return statement in function");
  }

/* the StmtList also reads the final curly brace */
  final public List < Statement > StmtList() throws ParseException {
  Statement s;
  List < Statement > rval = new LinkedList < Statement > ();
    label_10:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case GOTO:
      case RETURN:
      case IF:
      case WHILE:
      case BREAK:
      case ASSERT:
      case ASSUME:
      case HAVOC:
      case CALL:
      case IDENT:
        ;
        break;
      default:
        jj_la1[44] = jj_gen;
        break label_10;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASSERT:
      case ASSUME:
      case HAVOC:
      case CALL:
      case IDENT:
        s = LabelOrCmd();
        break;
      case IF:
      case WHILE:
      case BREAK:
        s = StructuredCmd();
        break;
      case GOTO:
      case RETURN:
        s = TransferCmd();
        break;
      default:
        jj_la1[45] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      rval.add(s);
    }
    jj_consume_token(SEP_RCURLY);
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public Statement TransferCmd() throws ParseException {
  Statement rval;
  Token first;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case GOTO:
      List < Token > destinations;
      first = jj_consume_token(GOTO);
      destinations = Idents();
      rval = new GotoStatement(first, destinations);
      break;
    case RETURN:
      first = jj_consume_token(RETURN);
      rval = new ReturnStatement(first);
      break;
    default:
      jj_la1[46] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(73);
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public Statement StructuredCmd() throws ParseException {
  Statement rval;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IF:
      rval = IfCmd();
      break;
    case WHILE:
      rval = WhileCmd();
      break;
    case BREAK:
      rval = BreakCmd();
      break;
    default:
      jj_la1[47] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public IfStatement IfCmd() throws ParseException {
  Token first;
  List < Statement > then, _else = null;
  Expression guard;
    first = jj_consume_token(IF);
    guard = Guard();
    jj_consume_token(SEP_LCURLY);
    then = StmtList();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ELSE:
      jj_consume_token(ELSE);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
        Statement s;
        s = IfCmd();
        _else = new LinkedList < Statement > ();
        _else.add(s);
        break;
      case SEP_LCURLY:
        jj_consume_token(SEP_LCURLY);
        _else = StmtList();
        break;
      default:
        jj_la1[48] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[49] = jj_gen;
      ;
    }
    {if (true) return new IfStatement(first, guard, then, _else);}
    throw new Error("Missing return statement in function");
  }

  final public WhileStatement WhileCmd() throws ParseException {
  Token first;
  Expression guard;
  List < LoopInvariant > invariants = new LinkedList < LoopInvariant > ();
  List < Statement > body;
    first = jj_consume_token(WHILE);
    guard = Guard();
    label_11:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FREE:
      case INVARIANT:
        ;
        break;
      default:
        jj_la1[50] = jj_gen;
        break label_11;
      }
      boolean free = false;
      Expression expr;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case FREE:
        jj_consume_token(FREE);
      free = true;
        break;
      default:
        jj_la1[51] = jj_gen;
        ;
      }
      jj_consume_token(INVARIANT);
      expr = Expression();
      jj_consume_token(73);
      invariants.add(new LoopInvariant(free, expr));
    }
    jj_consume_token(SEP_LCURLY);
    body = StmtList();
    {if (true) return new WhileStatement(first, guard, invariants, body);}
    throw new Error("Missing return statement in function");
  }

//! returns null iff the guard is a wildcard and an expression else
  final public Expression Guard() throws ParseException {
  Token first;
  Expression e;
    jj_consume_token(74);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OP_MUL:
      first = jj_consume_token(OP_MUL);
      e = new WildcardExpression(first);
      break;
    case IF:
    case FALSE:
    case TRUE:
    case OLD:
    case OP_NEGATION:
    case IDENT:
    case BVLIT:
    case INTEGER:
    case 74:
    case 85:
    case 88:
      e = Expression();
      break;
    default:
      jj_la1[52] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(75);
    {if (true) return e;}
    throw new Error("Missing return statement in function");
  }

  final public BreakStatement BreakCmd() throws ParseException {
  Token first;
  Token target = null;
    first = jj_consume_token(BREAK);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
      target = jj_consume_token(IDENT);
      break;
    default:
      jj_la1[53] = jj_gen;
      ;
    }
    jj_consume_token(73);
    {if (true) return new BreakStatement(first, target);}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Statement LabelOrCmd() throws ParseException {
  Statement rval;
  Token first;
  List < Attribute > attr;
  Expression expr;
  List < Token > vars;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
      rval = LabelOrAssign();
      break;
    case ASSERT:
      first = jj_consume_token(ASSERT);
      attr = AttributeList();
      expr = Expression();
      jj_consume_token(73);
      rval = new AssertionStatement(first, attr, expr);
      break;
    case ASSUME:
      first = jj_consume_token(ASSUME);
      expr = Expression();
      jj_consume_token(73);
      rval = new AssumptionStatement(first, expr);
      break;
    case HAVOC:
      first = jj_consume_token(HAVOC);
      vars = Idents();
      jj_consume_token(73);
      rval = new HavocStatement(first, vars);
      break;
    case CALL:
      rval = CallCmd();
      jj_consume_token(73);
      break;
    default:
      jj_la1[54] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Statement LabelOrAssign() throws ParseException {
  Token tmp;
  List < Token > locations = new LinkedList < Token > ();
  Expression expr;
  List < Expression > arguments, RValues = new LinkedList < Expression > (), LValues = new LinkedList < Expression > ();
    tmp = jj_consume_token(IDENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 76:
      jj_consume_token(76);
      {if (true) return new LabelStatement(tmp);}
      break;
    case OP_ASSIGN:
    case SEP_LBRACKET:
    case 77:
      locations.add(tmp);
      expr = new VariableUsageExpression(tmp);
      label_12:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case SEP_LBRACKET:
          ;
          break;
        default:
          jj_la1[55] = jj_gen;
          break label_12;
        }
        arguments = MapAssignIndex();
        expr = new MapAccessExpression(expr, arguments);
      }
      LValues.add(expr);
      label_13:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 77:
          ;
          break;
        default:
          jj_la1[56] = jj_gen;
          break label_13;
        }
        jj_consume_token(77);
        tmp = jj_consume_token(IDENT);
        locations.add(tmp);
        expr = new VariableUsageExpression(tmp);
        label_14:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case SEP_LBRACKET:
            ;
            break;
          default:
            jj_la1[57] = jj_gen;
            break label_14;
          }
          arguments = MapAssignIndex();
          expr = new MapAccessExpression(expr, arguments);
        }
        LValues.add(expr);
      }
      jj_consume_token(OP_ASSIGN);
      expr = Expression();
      RValues.add(expr);
      label_15:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 77:
          ;
          break;
        default:
          jj_la1[58] = jj_gen;
          break label_15;
        }
        jj_consume_token(77);
        expr = Expression();
        RValues.add(expr);
      }
      jj_consume_token(73);
      if (LValues.size() != RValues.size()) {if (true) throw new ParseException("found missmatched AssignmentStatement");}
      List < SimpleAssignment > rval = new LinkedList < SimpleAssignment > ();
      for (int i = 0; i < LValues.size(); i++) rval.add(new SimpleAssignment(locations.get(i), LValues.get(i), RValues.get(i)));
      {if (true) return new AssignmentStatement(rval);}
      break;
    default:
      jj_la1[59] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public List < Expression > MapAssignIndex() throws ParseException {
  Expression ex;
  List < Expression > rval = new LinkedList < Expression > ();
    jj_consume_token(SEP_LBRACKET);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IF:
    case FALSE:
    case TRUE:
    case OLD:
    case OP_NEGATION:
    case IDENT:
    case BVLIT:
    case INTEGER:
    case 74:
    case 85:
    case 88:
      ex = Expression();
      rval.add(ex);
      label_16:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 77:
          ;
          break;
        default:
          jj_la1[60] = jj_gen;
          break label_16;
        }
        jj_consume_token(77);
        ex = Expression();
        rval.add(ex);
      }
      break;
    default:
      jj_la1[61] = jj_gen;
      ;
    }
    jj_consume_token(SEP_RBRACKET);
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
/*
  Note '*'s in outParams of ordinary call statements mean "dont care". They are
  used to accsess single returnvalues of multidimensional results without storing
  unwanted garbage anywhere.
*/
  final public Statement CallCmd() throws ParseException {
  Token first;
  List < Attribute > attr;
  String name;
  Token tmp;
  Expression arg;
  List < Expression > arglist = new LinkedList < Expression > ();
  Token out;
  List < Token > outParam = new LinkedList < Token > ();
    first = jj_consume_token(CALL);
    attr = AttributeList();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
      tmp = jj_consume_token(IDENT);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 74:
        name = tmp.image;
        jj_consume_token(74);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IF:
        case FALSE:
        case TRUE:
        case OLD:
        case OP_NEGATION:
        case OP_MUL:
        case IDENT:
        case BVLIT:
        case INTEGER:
        case 74:
        case 85:
        case 88:
          arg = CallForallArg();
          arglist.add(arg);
          label_17:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case 77:
              ;
              break;
            default:
              jj_la1[62] = jj_gen;
              break label_17;
            }
            jj_consume_token(77);
            arg = CallForallArg();
            arglist.add(arg);
          }
          break;
        default:
          jj_la1[63] = jj_gen;
          ;
        }
        jj_consume_token(75);
        break;
      case OP_ASSIGN:
      case 77:
        outParam.add(tmp);
        label_18:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 77:
            ;
            break;
          default:
            jj_la1[64] = jj_gen;
            break label_18;
          }
          jj_consume_token(77);
          out = CallOutIdent();
          outParam.add(out);
        }
        jj_consume_token(OP_ASSIGN);
        tmp = jj_consume_token(IDENT);
        jj_consume_token(74);
        name = tmp.image;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IF:
        case FALSE:
        case TRUE:
        case OLD:
        case OP_NEGATION:
        case OP_MUL:
        case IDENT:
        case BVLIT:
        case INTEGER:
        case 74:
        case 85:
        case 88:
          arg = CallForallArg();
          arglist.add(arg);
          label_19:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case 77:
              ;
              break;
            default:
              jj_la1[65] = jj_gen;
              break label_19;
            }
            jj_consume_token(77);
            arg = CallForallArg();
            arglist.add(arg);
          }
          break;
        default:
          jj_la1[66] = jj_gen;
          ;
        }
        jj_consume_token(75);
        break;
      default:
        jj_la1[67] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      {if (true) return new CallStatement(first, attr, name, outParam, arglist);}
      break;
    case OP_FORALL:
      jj_consume_token(OP_FORALL);
      tmp = jj_consume_token(IDENT);
      name = tmp.image;
      jj_consume_token(74);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case FALSE:
      case TRUE:
      case OLD:
      case OP_NEGATION:
      case OP_MUL:
      case IDENT:
      case BVLIT:
      case INTEGER:
      case 74:
      case 85:
      case 88:
        arg = CallForallArg();
        arglist.add(arg);
        label_20:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 77:
            ;
            break;
          default:
            jj_la1[68] = jj_gen;
            break label_20;
          }
          jj_consume_token(77);
          arg = CallForallArg();
          arglist.add(arg);
        }
        break;
      default:
        jj_la1[69] = jj_gen;
        ;
      }
      jj_consume_token(75);
      {if (true) return new CallForallStatement(first, attr, name, arglist);}
      break;
    case OP_MUL:
      out = jj_consume_token(OP_MUL);
      outParam.add(out);
      label_21:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 77:
          ;
          break;
        default:
          jj_la1[70] = jj_gen;
          break label_21;
        }
        jj_consume_token(77);
        out = CallOutIdent();
        outParam.add(out);
      }
      jj_consume_token(OP_ASSIGN);
      tmp = jj_consume_token(IDENT);
      jj_consume_token(74);
      name = tmp.image;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case FALSE:
      case TRUE:
      case OLD:
      case OP_NEGATION:
      case OP_MUL:
      case IDENT:
      case BVLIT:
      case INTEGER:
      case 74:
      case 85:
      case 88:
        arg = CallForallArg();
        arglist.add(arg);
        label_22:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 77:
            ;
            break;
          default:
            jj_la1[71] = jj_gen;
            break label_22;
          }
          jj_consume_token(77);
          arg = CallForallArg();
          arglist.add(arg);
        }
        break;
      default:
        jj_la1[72] = jj_gen;
        ;
      }
      jj_consume_token(75);
      {if (true) return new CallStatement(first, attr, name, outParam, arglist);}
      break;
    default:
      jj_la1[73] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

/**
<b>Note</b>: * means throw away variable here.
*/
  final public Token CallOutIdent() throws ParseException {
  Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OP_MUL:
      t = jj_consume_token(OP_MUL);
      break;
    case IDENT:
      t = jj_consume_token(IDENT);
      break;
    default:
      jj_la1[74] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return t;}
    throw new Error("Missing return statement in function");
  }

  final public Expression CallForallArg() throws ParseException {
  Token first;
  Expression expr;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OP_MUL:
      first = jj_consume_token(OP_MUL);
      expr = new WildcardExpression(first);
      break;
    case IF:
    case FALSE:
    case TRUE:
    case OLD:
    case OP_NEGATION:
    case IDENT:
    case BVLIT:
    case INTEGER:
    case 74:
    case 85:
    case 88:
      expr = Expression();
      break;
    default:
      jj_la1[75] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public List < Token > Idents() throws ParseException {
  Token n;
  List < Token > rval = new LinkedList < Token > ();
    n = jj_consume_token(IDENT);
    rval.add(n);
    label_23:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 77:
        ;
        break;
      default:
        jj_la1[76] = jj_gen;
        break label_23;
      }
      jj_consume_token(77);
      n = jj_consume_token(IDENT);
      rval.add(n);
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public List < Token > WhiteSpaceIdents() throws ParseException {
  Token t;
  List < Token > rval = new LinkedList < Token > ();
    label_24:
    while (true) {
      t = jj_consume_token(IDENT);
      rval.add(t);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENT:
        ;
        break;
      default:
        jj_la1[77] = jj_gen;
        break label_24;
      }
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression Expression() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = ImpliesExpression();
    label_25:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OP_EQUIV:
        ;
        break;
      default:
        jj_la1[78] = jj_gen;
        break label_25;
      }
      loc = jj_consume_token(OP_EQUIV);
      tmp = ImpliesExpression();
      rval = new EquivalenceExpression(loc, rval, tmp);
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression ImpliesExpression() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = LogicalExpression();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OP_IMPL:
    case OP_EXPL:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OP_IMPL:
        loc = jj_consume_token(OP_IMPL);
        /* recurse because implication is right-associative */tmp = ImpliesExpression();
      rval = new ImpliesExpression(loc, rval, tmp);
        break;
      case OP_EXPL:
        label_26:
        while (true) {
          loc = jj_consume_token(OP_EXPL);
          tmp = LogicalExpression();
        rval = new ImpliesExpression(loc, tmp, rval);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case OP_EXPL:
            ;
            break;
          default:
            jj_la1[79] = jj_gen;
            break label_26;
          }
        }
        break;
      default:
        jj_la1[80] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[81] = jj_gen;
      ;
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression LogicalExpression() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = RelationalExpression();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OP_AND:
    case OP_OR:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OP_AND:
        label_27:
        while (true) {
          loc = jj_consume_token(OP_AND);
          tmp = RelationalExpression();
        rval = new AndExpression(loc, rval, tmp);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case OP_AND:
            ;
            break;
          default:
            jj_la1[82] = jj_gen;
            break label_27;
          }
        }
        break;
      case OP_OR:
        label_28:
        while (true) {
          loc = jj_consume_token(OP_OR);
          tmp = RelationalExpression();
        rval = new OrExpression(loc, rval, tmp);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case OP_OR:
            ;
            break;
          default:
            jj_la1[83] = jj_gen;
            break label_28;
          }
        }
        break;
      default:
        jj_la1[84] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[85] = jj_gen;
      ;
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression RelationalExpression() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = BvTerm();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OP_NEQ:
    case OP_LTE:
    case OP_GTE:
    case 78:
    case 79:
    case 81:
    case 82:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 81:
        loc = jj_consume_token(81);
        tmp = BvTerm();
      {if (true) return new EqualsExpression(loc, rval, tmp, false);}
        break;
      case OP_NEQ:
        loc = jj_consume_token(OP_NEQ);
        tmp = BvTerm();
      {if (true) return new EqualsExpression(loc, rval, tmp, true);}
        break;
      case 78:
        loc = jj_consume_token(78);
        tmp = BvTerm();
      {if (true) return new RelationExpression(loc, rval, tmp, "$lt");}
        break;
      case OP_LTE:
        loc = jj_consume_token(OP_LTE);
        tmp = BvTerm();
      {if (true) return new RelationExpression(loc, rval, tmp, "$lte");}
        break;
      case 79:
        loc = jj_consume_token(79);
        tmp = BvTerm();
      {if (true) return new RelationExpression(loc, rval, tmp, "$gt");}
        break;
      case OP_GTE:
        loc = jj_consume_token(OP_GTE);
        tmp = BvTerm();
      {if (true) return new RelationExpression(loc, rval, tmp, "$gte");}
        break;
      case 82:
        loc = jj_consume_token(82);
        tmp = BvTerm();
      {if (true) return new ExtendsExpression(loc, rval, tmp);}
        break;
      default:
        jj_la1[86] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    default:
      jj_la1[87] = jj_gen;
      ;
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression BvTerm() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = Term();
    label_29:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 83:
        ;
        break;
      default:
        jj_la1[88] = jj_gen;
        break label_29;
      }
      loc = jj_consume_token(83);
      tmp = Term();
      rval = new ConcatenationExpression(loc, rval, tmp);
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression Term() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = Factor();
    label_30:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 84:
      case 85:
        ;
        break;
      default:
        jj_la1[89] = jj_gen;
        break label_30;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 84:
        loc = jj_consume_token(84);
        tmp = Factor();
      rval = new BinaryIntegerExpression(loc, rval, tmp, "$plus");
        break;
      case 85:
        loc = jj_consume_token(85);
        tmp = Factor();
      rval = new BinaryIntegerExpression(loc, rval, tmp, "$minus");
        break;
      default:
        jj_la1[90] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression Factor() throws ParseException {
  Token loc;
  Expression rval, tmp;
    rval = UnaryExpression();
    label_31:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OP_MUL:
      case 86:
      case 87:
        ;
        break;
      default:
        jj_la1[91] = jj_gen;
        break label_31;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OP_MUL:
        loc = jj_consume_token(OP_MUL);
        tmp = UnaryExpression();
      rval = new BinaryIntegerExpression(loc, rval, tmp, "$mult");
        break;
      case 86:
        loc = jj_consume_token(86);
        tmp = UnaryExpression();
      rval = new BinaryIntegerExpression(loc, rval, tmp, "$div");
        break;
      case 87:
        loc = jj_consume_token(87);
        tmp = UnaryExpression();
      rval = new BinaryIntegerExpression(loc, rval, tmp, "$mod");
        break;
      default:
        jj_la1[92] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression UnaryExpression() throws ParseException {
  Token loc;
  Expression rval;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 85:
      loc = jj_consume_token(85);
      rval = UnaryExpression();
      rval = new UnaryMinusExpression(loc, rval);
      break;
    case OP_NEGATION:
      loc = jj_consume_token(OP_NEGATION);
      rval = UnaryExpression();
      rval = new NegationExpression(loc, rval);
      break;
    case IF:
    case FALSE:
    case TRUE:
    case OLD:
    case IDENT:
    case BVLIT:
    case INTEGER:
    case 74:
    case 88:
      rval = CoercionExpression();
      break;
    default:
      jj_la1[93] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
/* This production creates ambiguities, because types can start with "<"
   (polymorphic map types), but can also be followed by "<" (inequalities).
   Coco deals with these ambiguities in a reasonable way by preferring to read
   further types (type arguments) over relational symbols. E.g., "5 : C < 0"
   will cause a parse error because "<" is treated as the beginning of a
   map type. */
  final public Expression CoercionExpression() throws ParseException {
  Expression rval, tmp;
  ASTType T;
    rval = ArrayExpression();
    label_32:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 76:
        ;
        break;
      default:
        jj_la1[94] = jj_gen;
        break label_32;
      }
      jj_consume_token(76);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INT:
      case BOOL:
      case BVTYPE:
      case SEP_LBRACKET:
      case IDENT:
      case 74:
      case 78:
        T = ASTType();
        {if (true) return new CoercionExpression(rval, T);}
        break;
      case INTEGER:
        Token i;
        i = jj_consume_token(INTEGER);
        {if (true) return new BitvectorSelectExpression(rval, new IntegerExpression(i));}
        break;
      default:
        jj_la1[95] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression ArrayExpression() throws ParseException {
  Expression rval, tmp, update;
  List < Expression > arguments;
  boolean hasArguments;
    rval = AtomExpression();
    label_33:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SEP_LBRACKET:
        ;
        break;
      default:
        jj_la1[96] = jj_gen;
        break label_33;
      }
      jj_consume_token(SEP_LBRACKET);
      tmp = update = null;
      arguments = new LinkedList < Expression > ();
      hasArguments = false;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case FALSE:
      case TRUE:
      case OLD:
      case OP_ASSIGN:
      case OP_NEGATION:
      case IDENT:
      case BVLIT:
      case INTEGER:
      case 74:
      case 85:
      case 88:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IF:
        case FALSE:
        case TRUE:
        case OLD:
        case OP_NEGATION:
        case IDENT:
        case BVLIT:
        case INTEGER:
        case 74:
        case 85:
        case 88:
       hasArguments = true;
          tmp = Expression();
        arguments.add(tmp);
          label_34:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case 77:
              ;
              break;
            default:
              jj_la1[97] = jj_gen;
              break label_34;
            }
            jj_consume_token(77);
            tmp = Expression();
          arguments.add(tmp);
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case OP_ASSIGN:
            jj_consume_token(OP_ASSIGN);
            update = Expression();
            break;
          default:
            jj_la1[98] = jj_gen;
            ;
          }
        if (arguments.size() == 1 && tmp instanceof BitvectorSelectExpression) rval = new BitvectorAccessSelectionExpression(rval, tmp);
        else if (null == update) rval = new MapAccessExpression(rval, arguments);
        else rval = new MapUpdateExpression(rval, arguments, update);
          break;
        case OP_ASSIGN:
          jj_consume_token(OP_ASSIGN);
          update = Expression();
      hasArguments = true;

        rval = new MapUpdateExpression(rval, arguments, update);
          break;
        default:
          jj_la1[99] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[100] = jj_gen;
        ;
      }
      if(!hasArguments)
      rval = new MapAccessExpression(rval, arguments);
      jj_consume_token(SEP_RBRACKET);
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

/*------------------------------------------------------------------------*/
  final public Expression AtomExpression() throws ParseException {
  Token first;
  Expression rval;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case FALSE:
      first = jj_consume_token(FALSE);
      {if (true) return new FalseExpression(first);}
      break;
    case TRUE:
      first = jj_consume_token(TRUE);
      {if (true) return new TrueExpression(first);}
      break;
    case INTEGER:
      first = jj_consume_token(INTEGER);
      {if (true) return new IntegerExpression(first);}
      break;
    case BVLIT:
      first = jj_consume_token(BVLIT);
      {if (true) return new BitvectorLiteralExpression(first);}
      break;
    case IDENT:
      first = jj_consume_token(IDENT);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 74:
        jj_consume_token(74);
        Expression expr;
        List < Expression > args = new LinkedList < Expression > ();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IF:
        case FALSE:
        case TRUE:
        case OLD:
        case OP_NEGATION:
        case IDENT:
        case BVLIT:
        case INTEGER:
        case 74:
        case 85:
        case 88:
          expr = Expression();
          args.add(expr);
          label_35:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case 77:
              ;
              break;
            default:
              jj_la1[101] = jj_gen;
              break label_35;
            }
            jj_consume_token(77);
            expr = Expression();
            args.add(expr);
          }
          break;
        default:
          jj_la1[102] = jj_gen;
          ;
        }
        jj_consume_token(75);
        {if (true) return new FunctionCallExpression(first, args);}
        break;
      default:
        jj_la1[103] = jj_gen;
        ;
      }
      {if (true) return new VariableUsageExpression(first);}
      break;
    case OLD:
      first = jj_consume_token(OLD);
      jj_consume_token(74);
      rval = Expression();
      jj_consume_token(75);
      {if (true) return new OldExpression(first, rval);}
      break;
    case 74:
      jj_consume_token(74);
      QuantifierBody body;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case FALSE:
      case TRUE:
      case OLD:
      case OP_NEGATION:
      case IDENT:
      case BVLIT:
      case INTEGER:
      case 74:
      case 85:
      case 88:
        rval = Expression();
        break;
      case OP_FORALL:
        first = jj_consume_token(OP_FORALL);
        body = QuantifierBody();
        rval = new ForallExpression(first, body);
        break;
      case OP_EXISTS:
        first = jj_consume_token(OP_EXISTS);
        body = QuantifierBody();
        rval = new ExistsExpression(first, body);
        break;
      case OP_LAMBDA:
        // unlike other quantifiers, lambdas have returntype [arglist]typeof(body)
              first = jj_consume_token(OP_LAMBDA);
        body = QuantifierBody();
        rval = new LambdaExpression(first, body);
        break;
      default:
        jj_la1[104] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      jj_consume_token(75);
      {if (true) return rval;}
      break;
    case IF:
      rval = IfThenElseExpression();
      {if (true) return rval;}
      break;
    case 88:
      rval = CodeExpression();
      {if (true) return rval;}
      break;
    default:
      jj_la1[105] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public CodeExpression CodeExpression() throws ParseException {
  Token loc;
  LocalVariableDeclaration var;
  List < LocalVariableDeclaration > vars = new LinkedList < LocalVariableDeclaration > ();
  CodeBlock block;
  List < CodeBlock > blocks = new LinkedList < CodeBlock > ();
    loc = jj_consume_token(88);
    label_36:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case VAR:
        ;
        break;
      default:
        jj_la1[106] = jj_gen;
        break label_36;
      }
      var = LocalVars();
      vars.add(var);
    }
    label_37:
    while (true) {
      block = CodeBlock();
      blocks.add(block);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENT:
        ;
        break;
      default:
        jj_la1[107] = jj_gen;
        break label_37;
      }
    }
    jj_consume_token(89);
    {if (true) return new CodeExpression(loc, vars, blocks);}
    throw new Error("Missing return statement in function");
  }

/*
  Code blocks are used to form the program that specifies the desired property. 
*/
  final public CodeBlock CodeBlock() throws ParseException {
  Token tmp;
  List < Token > targets;
  Expression expr;
  Statement cmd;
  List < Statement > body = new LinkedList < Statement > ();
    tmp = jj_consume_token(IDENT);
    jj_consume_token(76);
    cmd = new LabelStatement(tmp);
    body.add(cmd);
    label_38:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASSERT:
      case ASSUME:
      case HAVOC:
      case CALL:
      case IDENT:
        ;
        break;
      default:
        jj_la1[108] = jj_gen;
        break label_38;
      }
      cmd = LabelOrCmd();
      body.add(cmd);
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case GOTO:
      tmp = jj_consume_token(GOTO);
      targets = Idents();
      body.add(new GotoStatement(tmp, targets));
      break;
    case RETURN:
      jj_consume_token(RETURN);
      expr = Expression();
      body.add(new CodeExpressionReturn(expr));
      break;
    default:
      jj_la1[109] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(73);
    {if (true) return new CodeBlock(body);}
    throw new Error("Missing return statement in function");
  }

  final public List < Attribute > AttributeList() throws ParseException {
  Attribute attr;
  List < Attribute > rval = new LinkedList < Attribute > ();
    label_39:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SEP_LCURLY:
        ;
        break;
      default:
        jj_la1[110] = jj_gen;
        break label_39;
      }
      attr = Attribute();
      rval.add(attr);
    }
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public Attribute Attribute() throws ParseException {
  ASTElement rval;
    rval = AttributeOrTrigger();
    if (!(rval instanceof Attribute)) {if (true) throw new ParseException("Attribute expected but found Trigger @" + rval.getLocation());}
    {if (true) return (Attribute) rval;}
    throw new Error("Missing return statement in function");
  }

  final public ASTElement AttributeOrTrigger() throws ParseException {
  ASTElement rval;
    jj_consume_token(SEP_LCURLY);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 76:
      Token name;
      AttributeParameter param;
      List < AttributeParameter > params = new LinkedList < AttributeParameter > ();
      jj_consume_token(76);
      name = jj_consume_token(IDENT);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IF:
      case FALSE:
      case TRUE:
      case OLD:
      case OP_NEGATION:
      case IDENT:
      case BVLIT:
      case STRING:
      case INTEGER:
      case 74:
      case 85:
      case 88:
        param = AttributeParameter();
        params.add(param);
        label_40:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 77:
            ;
            break;
          default:
            jj_la1[111] = jj_gen;
            break label_40;
          }
          jj_consume_token(77);
          param = AttributeParameter();
          params.add(param);
        }
        break;
      default:
        jj_la1[112] = jj_gen;
        ;
      }
      rval = new Attribute(name, params);
      break;
    case IF:
    case FALSE:
    case TRUE:
    case OLD:
    case OP_NEGATION:
    case IDENT:
    case BVLIT:
    case INTEGER:
    case 74:
    case 85:
    case 88:
      Expression expr;
      List < Expression > args = new LinkedList < Expression > ();
      expr = Expression();
      args.add(expr);
      label_41:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 77:
          ;
          break;
        default:
          jj_la1[113] = jj_gen;
          break label_41;
        }
        jj_consume_token(77);
        expr = Expression();
        args.add(expr);
      }
      rval = new Trigger(args);
      break;
    default:
      jj_la1[114] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(SEP_RCURLY);
    {if (true) return rval;}
    throw new Error("Missing return statement in function");
  }

  final public AttributeParameter AttributeParameter() throws ParseException {
  Token t;
  Expression expr;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STRING:
      t = jj_consume_token(STRING);
      {if (true) return new AttributeParameter(t);}
      break;
    case IF:
    case FALSE:
    case TRUE:
    case OLD:
    case OP_NEGATION:
    case IDENT:
    case BVLIT:
    case INTEGER:
    case 74:
    case 85:
    case 88:
      expr = Expression();
      {if (true) return new AttributeParameter(expr);}
      break;
    default:
      jj_la1[115] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Expression IfThenElseExpression() throws ParseException {
  Token first;
  Expression condition, _then, _else;
    first = jj_consume_token(IF);
    condition = Expression();
    jj_consume_token(THEN);
    _then = Expression();
    jj_consume_token(ELSE);
    _else = Expression();
    {if (true) return new IfThenElseExpression(first, condition, _then, _else);}
    throw new Error("Missing return statement in function");
  }

  final public QuantifierBody QuantifierBody() throws ParseException {
  Token location;
  List < Token > typeArgs = new LinkedList < Token > ();
  List < VariableDeclaration > vars = new LinkedList < VariableDeclaration > ();
  ASTElement attrOrTrigg;
  List < Trigger > triggers = new LinkedList < Trigger > ();
  List < Attribute > attributes = new LinkedList < Attribute > ();
  Expression body;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 78:
      typeArgs = TypeParams();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENT:
        vars = BoundVars();
        break;
      default:
        jj_la1[116] = jj_gen;
        ;
      }
      break;
    case IDENT:
      vars = BoundVars();
      break;
    default:
      jj_la1[117] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    location = jj_consume_token(OP_SEP);
    label_42:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SEP_LCURLY:
        ;
        break;
      default:
        jj_la1[118] = jj_gen;
        break label_42;
      }
      attrOrTrigg = AttributeOrTrigger();
      if (attrOrTrigg instanceof Attribute) attributes.add((Attribute) attrOrTrigg);
      else triggers.add((Trigger) attrOrTrigg);
    }
    body = Expression();
    {if (true) return new QuantifierBody(location, attributes, triggers, typeArgs, vars, body);}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public BPLParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[119];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
      jj_la1_init_2();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x3d108000,0x3d108000,0x0,0x0,0x10000,0xe0000,0xe0000,0xe0000,0xe0000,0xe0000,0xe0000,0xe0000,0x0,0xe0000,0x0,0x200000,0x200000,0x0,0x200000,0x200000,0x800000,0x400000,0x0,0x0,0xe0000,0x2000000,0x0,0x0,0x0,0x0,0x0,0x0,0x2000000,0xc0000000,0xc0000000,0x0,0x0,0x0,0x2000000,0x0,0x80000000,0x0,0xc0000000,0x8000,0x0,0x0,0x0,0x0,0x0,0x0,0x80000000,0x80000000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0xe0000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x8000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x3,0x3,0x0,0x0,0x0,0x0,0x0,0x0,0x3,0x3,0x0,0x3e9c,0x3e9c,0xc,0x290,0x10,0x40,0x100,0x0,0x881c010,0x0,0x3c00,0x0,0x0,0x0,0x0,0x20000,0x0,0x81c010,0x0,0x881c010,0x0,0x0,0x881c010,0x20000,0x0,0x881c010,0x0,0x0,0x881c010,0x18000000,0x8000000,0x881c010,0x0,0x0,0x40000,0x100000,0x180000,0x180000,0x200000,0x400000,0x600000,0x600000,0x7000000,0x7000000,0x0,0x0,0x0,0x8000000,0x8000000,0x81c010,0x0,0x0,0x0,0x0,0x20000,0x83c010,0x83c010,0x0,0x81c010,0x0,0x7081c010,0x1c010,0x0,0x0,0x3c00,0xc,0x0,0x0,0x81c010,0x0,0x81c010,0x81c010,0x0,0x0,0x0,};
   }
   private static void jj_la1_init_2() {
      jj_la1_2 = new int[] {0x0,0x0,0x10,0x2000,0x0,0x4418,0x4418,0x4418,0x4418,0x4418,0x0,0x400,0x4000,0x4418,0x2000,0x0,0x0,0x2000,0x0,0x10,0x0,0x0,0x4000,0x2000,0x4418,0x1000,0x202,0x1000,0x2000,0x10,0x10000,0x4000,0x0,0x0,0x0,0x2,0x200,0x4000,0x0,0x10,0x0,0x0,0x0,0x0,0x10,0x10,0x0,0x0,0x2,0x0,0x0,0x0,0x1200530,0x10,0x10,0x8,0x2000,0x8,0x2000,0x3008,0x2000,0x1200530,0x2000,0x1200530,0x2000,0x2000,0x1200530,0x2400,0x2000,0x1200530,0x2000,0x2000,0x1200530,0x10,0x10,0x1200530,0x2000,0x10,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x6c000,0x6c000,0x80000,0x300000,0x300000,0xc00000,0xc00000,0x1200530,0x1000,0x4518,0x8,0x2000,0x0,0x1200530,0x1200530,0x2000,0x1200530,0x400,0x1200530,0x1000530,0x0,0x10,0x10,0x0,0x2,0x2000,0x1200570,0x2000,0x1201530,0x1200570,0x10,0x4010,0x2,};
   }

  /** Constructor with InputStream. */
  public BPLParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public BPLParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new BPLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 119; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 119; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public BPLParser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new BPLParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 119; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 119; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public BPLParser(BPLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 119; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(BPLParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 119; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[90];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 119; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 90; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
