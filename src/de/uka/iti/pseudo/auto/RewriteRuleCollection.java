package de.uka.iti.pseudo.auto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.SubtermCollector;
import de.uka.iti.pseudo.term.creation.TermUnification;

// TODO DOC

public class RewriteRuleCollection {
    
    Map<String, List<Rule>> classificationMap;
    
    public RewriteRuleCollection(List<Rule> rules, String category) throws RuleException {
        classificationMap = new HashMap<String, List<Rule>>();
        collectRules(rules, category);
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
        }
        
    }

    private boolean checkRule(Rule rule) throws RuleException {
        LocatedTerm find = rule.getFindClause();
        
//        if(find.getMatchingLocation() != MatchingLocation.BOTH) {
//            System.err.println("Rule " + rule.getName() +  
//                    " A rewrite rule must not have a top level find (" + 
//                    rule.getDeclaration() + ")");
//            return false;
//        }
        
        if(rule.getAssumptions().size() > 0) {
            System.err.println("Rule " + rule.getName() + 
                    " A rewrite rule must not have assumptions (" + 
                    rule.getDeclaration() + ")");
            return false;
        }
        
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
        
        if (term instanceof ModalityTerm) {
            // no further classification at the moment
            return "[modality]";
        }
        
        return "[generic]";
    }
    
    public RuleApplicationMaker findRuleApplication(Sequent sequent) {
        
        List<Term> ante = sequent.getAntecedent();
        RuleApplicationMaker ram = findRuleApplication(ante, TermSelector.ANTECEDENT);
        
        if(ram == null) {
            List<Term> succ = sequent.getSuccedent();
            ram = findRuleApplication(succ, TermSelector.SUCCEDENT);
        }

        return ram;
        
    }
    
    private RuleApplicationMaker findRuleApplication(List<Term> terms, boolean side) {
        for (int termno = 0; termno < terms.size(); termno++) {
            List<Term> subterms = SubtermCollector.collect(terms.get(termno));
            for (int subtermno = 0; subtermno < subterms.size(); subtermno++) {
                RuleApplicationMaker application = findRuleApplication(subterms.get(subtermno));
                if(application != null && checkLocation(application, side, termno)) {
                    application.setFindSelector(new TermSelector(side, termno, subtermno));
                    return application;
                }
            }
        }
        return null;
    }
    
    private boolean checkLocation(RuleApplicationMaker application, boolean side, int termno) {
        MatchingLocation findLocation = application.getRule().getFindClause().getMatchingLocation();
        switch(findLocation) {
        case BOTH:
            return true;
            
        case ANTECEDENT:
            return termno == 0 && side == TermSelector.ANTECEDENT;
            
        case SUCCEDENT:
            return termno == 0 && side == TermSelector.SUCCEDENT;
        }
        // unreachable
        throw new Error();
    }

    public RuleApplicationMaker findRuleApplication(Term term) {
        String classification = getClassification(term);
        
        List<Rule> candidates = classificationMap.get(classification);
        RuleApplicationMaker result = findRuleApplication(term, candidates);
        
        if(result == null && !classification.equals("[generic]")) {
            candidates = classificationMap.get(classification);
            result = findRuleApplication(term, candidates);
        }
        
        return result;
    }


    private RuleApplicationMaker findRuleApplication(Term term, List<Rule> candidates) {
        if(candidates != null) {
            for (Rule rule : candidates) {
                TermUnification mc = new TermUnification();
                if(mc.leftUnify(rule.getFindClause().getTerm(), term)) {
                    RuleApplicationMaker ram = new RuleApplicationMaker();
                    ram.setRule(rule);
                    ram.setTermUnification(mc);
                    return ram;
                }
                    
            }
        }
        return null;
    }

}
