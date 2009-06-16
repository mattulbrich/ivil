package de.uka.iti.pseudo.auto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplicationFinder;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.ProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;

// TODO DOC

public class RewriteRuleCollection {
    
    Map<String, List<Rule>> classificationMap;
    Environment env;
    String category;
    private int size;
    
    public RewriteRuleCollection(List<Rule> rules, String category, Environment env) throws RuleException {
        this.category = category;
        classificationMap = new HashMap<String, List<Rule>>();
        this.size = 0;
        collectRules(rules, category);
        this.env = env;
    }
    
    private void collectRules(List<Rule> rules, String category) throws RuleException {
        
        for (Rule rule : rules) {
            
            String rwProperty = rule.getProperty("rewrite");
            if(rwProperty == null || !category.equals(rwProperty))
                continue;
            
            if(!checkRule(rule))
                continue;
            String classification = getClassification(rule.getFindClause().getTerm());
            
            List<Rule> targetList = classificationMap.get(classification);
            if(targetList == null) {
                targetList = new LinkedList<Rule>();
                classificationMap.put(classification, targetList);
            }
            
            targetList.add(rule);
            size ++;
        }
        
    }

    private boolean checkRule(Rule rule) throws RuleException {

        return true;
        
    }

    private String getClassification(Term term) {
        
        if (term instanceof Binding) {
            Binding binding = (Binding) term;
            return binding.getBinder().getName();
        }
        
        if (term instanceof Application) {
            Application app = (Application) term;
            return app.getFunction().getName();
        }
        
        if (term instanceof ProgramTerm) {
            // no further classification at the moment
            return "[modality]";
        }
        
        return "[generic]";
    }
    
    public RuleApplicationMaker findRuleApplication(Proof proof, int goalNo) {
        
        RuleApplicationFinder finder = new RuleApplicationFinder(proof, goalNo, env);
        Sequent seq = proof.getGoal(goalNo).getSequent();
        
        List<Term> ante = seq.getAntecedent();
        RuleApplicationMaker ram = findRuleApplication(finder, ante, TermSelector.ANTECEDENT);
        
        if(ram == null) {
            List<Term> succ = seq.getSuccedent();
            ram = findRuleApplication(finder, succ, TermSelector.SUCCEDENT);
        }

        return ram;
        
    }
    
    private RuleApplicationMaker findRuleApplication(RuleApplicationFinder finder, List<Term> terms, boolean side) {
        for (int termno = 0; termno < terms.size(); termno++) {
            List<Term> subterms = SubtermCollector.collect(terms.get(termno));
            for (int subtermno = 0; subtermno < subterms.size(); subtermno++) {
                Term term = subterms.get(subtermno);
                try {
                    TermSelector selector = new TermSelector(side, termno, subtermno);
                    List<Rule> ruleset = getRuleSet(term);
                    if(ruleset != null && ruleset.size() > 0) {
                        RuleApplicationMaker ram = finder.findOne(selector, ruleset);
                        if(ram != null) {
                            return ram;
                        }
                    }
                } catch (ProofException e) {
                    System.err.println("Error while finding rules for " + term);
                    System.err.println("Continuing anyway");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    private List<Rule> getRuleSet(Term term) {
        String classif = getClassification(term);
        return classificationMap.get(classif);
    }
    
    @Override public String toString() {
        return "RuleCollection[" + category + "] with " + size + " rules";
    }
    
}
