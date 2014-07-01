/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.script;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.Util;

/**
 * This is a node within a proof script tree captures in a {@link ProofScript}.
 *
 * Nodes are immutable.
 *
 * @see ProofScript
 */
public class ProofScriptNode {

    /**
     * My children nodes.
     */
    private final ProofScriptNode[] children;

    /**
     * The command to execute.
     */
    private final ProofScriptCommand command;

    /**
     * The arguments for the command.
     */
    private final Map<String, String> arguments;

    /**
     * The location for error reporting.
     */
    private final ASTLocatedElement location;

    /**
     * Instantiates a new proof script node.
     *
     * The arguments and children are copied to fresh data structures.
     *
     * @param command
     *            the command to execute
     * @param arguments
     *            the arguments for the command
     * @param children
     *            the children nodes to be appended to the end.
     * @param location
     *            the location, for error handling
     */
    public ProofScriptNode(@NonNull ProofScriptCommand command,
            @NonNull Map<String, String> arguments,
            @DeepNonNull List<ProofScriptNode> children,
            @NonNull ASTLocatedElement location) {
        this.command = command;
        this.arguments = new HashMap<String, String>(arguments);
        this.location = location;
        this.children = children.toArray(new ProofScriptNode[children.size()]);
    }

    /**
     * Gets the children nodes of this proof script node.
     *
     * @return the children of this node
     */
    public @DeepNonNull List<ProofScriptNode> getChildren() {
        return Util.readOnlyArrayList(children);
    }

    /**
     * Gets an argument of this node.
     *
     * @param parameter
     *            the parameter to query
     *
     * @return the argument set for the named parameter, <code>null</code> if
     *         none set.
     */
    public @Nullable String getArgument(String parameter) {
        return getArguments().get(parameter);
    }

    /**
     * Gets the associated command.
     *
     * @return the command
     */
    public @NonNull ProofScriptCommand getCommand() {
        return command;
    }

    /**
     * Gets all arguments.
     *
     * @return an immutable view on all arguments
     */
    public Map<String, String> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }

    /**
     * Gets the location of this script node.
     *
     * @return the location element
     */
    public ASTLocatedElement getLocation() {
        return location;
    }

    public List<ProofNode> execute(ProofNode proofNode) throws StrategyException {
        return command.apply(getArguments(), proofNode);
    }

    public boolean hasSameCommandAs(ProofScriptNode scriptNode) {
        return command == scriptNode.command && arguments.equals(scriptNode.arguments);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProofScriptNode) {
            ProofScriptNode node = (ProofScriptNode) obj;
            return hasSameCommandAs(node) && Arrays.equals(children, node.children);
        }
        return false;
    }

}
