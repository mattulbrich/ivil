package de.uka.iti.pseudo.util;

import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * This class is used to instantiate schema variables in strings.
 * 
 * The format is the following:
 * <table>
 * <tr>
 * <th>Text</th>
 * <th>Replacement</th>
 * </tr>
 * <tr>
 * <td>{%c}</td>
 * <td>The text representation of the instantiation for the schema variable,
 * "??" if not instantiated.</td>
 * </tr>
 * <tr>
 * <td>{explain %c}</td>
 * <td>The text representation of the annotation statement to which %c points
 * (if it does exists). Empty string otherwise.</td>
 * </tr>
 * <tr>
 * <td>{explainOrQuote %c}</td>
 * <td>The text representation of the annotation statement to which %c points
 * (if it does). The statement otherwise. Empty string if %c does not hold a
 * program term.</td>
 * </tr>
 * <tr>
 * <td>{property name}</td>
 * <td>The value of the named property of the rule application.</td>
 * </tr>
 * <tr>
 * <td>{upd U}</td>
 * <td>The textual representation of the value of the instantiation of the
 * schema update.</td>
 * </tr>
 * </table>
 * 
 */
public class TextInstantiator {
    
    private RuleApplication ruleApp;

    public TextInstantiator(RuleApplication ruleApp) {
        super();
        this.ruleApp = ruleApp;
    }

    /**
     * Replace schema variables in a string.
     * 
     * <p>
     * For instance <code>Assume {%c} in {%a}</code> might become
     * <code>Assume $eq(x,3) in [4; P]</code>
     * 
     * <p>
     * Term{@link Term#toString())} is used to render the term.
     * 
     * @param string
     *            the string to instantiate
     * 
     * @return the string with schema variables replaced
     */
    public @NonNull String replaceInString(@NonNull String string) {
        return replaceInString(string, null);
    }
    
    /**
     * Replace schema variables in a string.
     * 
     * <p>
     * For instance <code>Assume {%c} in {%a}</code> might become
     * <code>Assume x = 3 in [4; P]</code>
     * 
     * <p>
     * The provided pretty printer is used to render the term. If it is
     * <code>null</code>, defaults to {@link #replaceInString(String)}.
     * 
     * <p><code>{{</code> can be used to produce a singe <code>{</code>.
     * 
     * @param pp
     *            pretty printer to render the instantiations
     * @param string
     *            the string to instantiate
     * 
     * @return the string with schema variables replaced
     */
    public @NonNull String replaceInString(@NonNull String string, PrettyPrint pp) {
        
        StringBuilder retval = new StringBuilder();
        StringBuilder curley = new StringBuilder();
        
        Map<String, Term> termMap = ruleApp.getSchemaVariableMapping();
        int len = string.length();
        
        boolean inCurley = false;
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            switch(c) {
            case '{':
                // something like "{{" (or also "{xy{")
                if(inCurley) {
                    retval.append('{');
                    curley.setLength(0);
                    inCurley = false;
                } else {
                    inCurley = true;
                }
                break;
                
            case '}':
                String lookup = curley.toString();
                String display = "??";
                if(lookup.charAt(0) == '%') {
                    Term t = termMap.get(lookup);
                    if(t != null) {
                        if(pp == null) {
                            display = t.toString();
                        } else {
                            display = pp.print(t).toString();
                        }
                    }
                    
                } else if(lookup.startsWith("explain %")) {
                    // retrieve explanation, overread "explain "
                    display = extractExplanation(termMap.get(lookup.substring(8)));
                    
                } else if(lookup.startsWith("explainOrQuote %")) {
                    // retrieve explanation, overread "explain "
                    Term term = termMap.get(lookup.substring(15));
                    display = extractExplanation(term);
                    if(display.length() == 0)
                        display = quoteStatement(term, pp);
                    
                } else if(lookup.startsWith("property ")) {
                    String prop = ruleApp.getProperties().get(lookup.substring(9));
                    if(prop != null)
                        display = prop;
                    
                } else if(lookup.startsWith("upd ")) {
                    String id = lookup.substring(4);
                    Update upd = ruleApp.getSchemaUpdateMapping().get(id);
                    if(upd != null) {
                        if(pp != null) {
                            display = pp.print(upd.getAssignments()).toString();
                        } else {
                            display = upd.toString();
                        }
                    }
                }
                
                retval.append(display);
                
                inCurley = false;
                curley.setLength(0);
                break;
                
            default:
                if(inCurley)
                    curley.append(c);
                else
                    retval.append(c);
            }
        }
        
        return retval.toString();
    }
    
    private @NonNull String extractExplanation(Term term) {
        String ret = null;
        if (term instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) term;
            int index = prog.getProgramIndex();
            ret = prog.getProgram().getTextAnnotation(index);
        }
        return ret == null ? "" : ret;
    }
    
    private @NonNull String quoteStatement(Term term, PrettyPrint pp) {
        String ret = null;
        if (term instanceof LiteralProgramTerm) {
            LiteralProgramTerm prog = (LiteralProgramTerm) term;
            Statement stm = prog.getStatement();
            if(pp == null)
                ret = stm.toString();
            else
                ret = pp.print(stm).toString();
        }
        return ret == null ? "" : ret;
    }
    
    
}
