package de.uka.iti.pseudo.auto.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.auto.strategy.hint.HintRuleAppFinder;
import de.uka.iti.pseudo.auto.strategy.hint.HintParser;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Term;

public class HintStrategy extends AbstractStrategy {

    private static final String HINTS_ON_BRANCHES_PROPERTY = "hintsOnBranches";
    private Map<ProofNode, List<HintRuleAppFinder>> hintMap = 
            new HashMap<ProofNode, List<HintRuleAppFinder>>();
    private Environment env;
    private HintParser hintParser;

    @Override
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        super.init(proof, env, strategyManager);
        this.env = env;
        this.hintParser = new HintParser(env);
    }
    
    @Override
    public void endSearch() {
        hintMap.clear();
    }
    
    @Override
    public RuleApplication findRuleApplication(ProofNode node)
            throws StrategyException {
        ProofNode reasonNode = node;
        while(reasonNode != null) {
            List<HintRuleAppFinder> hints = hintMap.get(reasonNode);
            if(hints != null) {
                for (HintRuleAppFinder hint : hints) {
                    RuleApplication ruleApp = followHint(hint, node, reasonNode);
                    if(ruleApp != null) {
                        return ruleApp;
                    }
                }
            }
            reasonNode = reasonNode.getParent();
        }
        
        return null;
    }
    
    private RuleApplication followHint(HintRuleAppFinder hint, ProofNode node, ProofNode reasonNode) {
        return hint.findRuleApplication(node, reasonNode);
    }

    @Override
    public void notifyRuleApplication(RuleApplication ruleApp) throws StrategyException {

        try {
            Rule rule = ruleApp.getRule();
            ProofNode node = ruleApp.getProofNode();
            String hintOn = rule.getProperty(HINTS_ON_BRANCHES_PROPERTY);
            if(hintOn == null) {
                return;
            }

            for (String string : hintOn.split(" *, *")) {
                int branchNo = Integer.parseInt(string);
                ProofNode branch = node.getChildren().get(branchNo);
                TermSelector findSel = ruleApp.getFindSelector();
            
                Term findTerm = findSel.selectSubterm(node.getSequent());
                if (findTerm instanceof LiteralProgramTerm) {
                    LiteralProgramTerm lpt = (LiteralProgramTerm) findTerm;
                    String annotation = lpt.getProgram().getTextAnnotation(lpt.getProgramIndex());
                    if(annotation == null) {
                        return;
                    }
                    List<HintRuleAppFinder> hints = hintParser.parse(annotation);
                    hintMap.put(branch, hints);
                } 
            }
        } catch (Exception e) {
            throw new StrategyException("Error while processing hints", e);
        }
    };

}