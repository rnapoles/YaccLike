package com.parsergenerator.lalr;

import com.parsergenerator.grammar.Production;

import java.util.Objects;

/**
 * Represents an action in the LALR parsing table.
 * Actions can be SHIFT, REDUCE, ACCEPT, or ERROR.
 */
public class Action {
    private final ActionType type;
    private final int parameter;
    private final Production production; // For REDUCE actions

    /**
     * Creates a SHIFT action.
     *
     * @param stateNumber the state to shift to
     * @return a new SHIFT action
     */
    public static Action shift(int stateNumber) {
        return new Action(ActionType.SHIFT, stateNumber, null);
    }

    /**
     * Creates a REDUCE action.
     *
     * @param production the production to reduce by
     * @return a new REDUCE action
     */
    public static Action reduce(Production production) {
        if (production == null) {
            throw new IllegalArgumentException("Production cannot be null for REDUCE action");
        }
        return new Action(ActionType.REDUCE, production.getId(), production);
    }

    /**
     * Creates an ACCEPT action.
     *
     * @return a new ACCEPT action
     */
    public static Action accept() {
        return new Action(ActionType.ACCEPT, 0, null);
    }

    /**
     * Creates an ERROR action.
     *
     * @return a new ERROR action
     */
    public static Action error() {
        return new Action(ActionType.ERROR, 0, null);
    }

    /**
     * Private constructor for creating actions.
     *
     * @param type the action type
     * @param parameter the parameter (state number for SHIFT, production ID for REDUCE)
     * @param production the production for REDUCE actions
     */
    private Action(ActionType type, int parameter, Production production) {
        this.type = type;
        this.parameter = parameter;
        this.production = production;
    }

    /**
     * Returns the action type.
     *
     * @return the action type
     */
    public ActionType getType() {
        return type;
    }

    /**
     * Returns the parameter for this action.
     * For SHIFT actions, this is the state number to shift to.
     * For REDUCE actions, this is the production ID.
     *
     * @return the action parameter
     */
    public int getParameter() {
        return parameter;
    }

    /**
     * Returns the production for REDUCE actions.
     *
     * @return the production, or null for non-REDUCE actions
     */
    public Production getProduction() {
        return production;
    }

    /**
     * Checks if this is a SHIFT action.
     *
     * @return true if this is a SHIFT action
     */
    public boolean isShift() {
        return type == ActionType.SHIFT;
    }

    /**
     * Checks if this is a REDUCE action.
     *
     * @return true if this is a REDUCE action
     */
    public boolean isReduce() {
        return type == ActionType.REDUCE;
    }

    /**
     * Checks if this is an ACCEPT action.
     *
     * @return true if this is an ACCEPT action
     */
    public boolean isAccept() {
        return type == ActionType.ACCEPT;
    }

    /**
     * Checks if this is an ERROR action.
     *
     * @return true if this is an ERROR action
     */
    public boolean isError() {
        return type == ActionType.ERROR;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Action action = (Action) obj;
        return parameter == action.parameter &&
               type == action.type &&
               Objects.equals(production, action.production);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, parameter, production);
    }

    @Override
    public String toString() {
        switch (type) {
            case SHIFT:
                return "s" + parameter;
            case REDUCE:
                return "r" + parameter;
            case ACCEPT:
                return "acc";
            case ERROR:
                return "err";
            default:
                return "unknown";
        }
    }

    /**
     * Returns a detailed string representation of this action.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        switch (type) {
            case SHIFT:
                return "SHIFT " + parameter;
            case REDUCE:
                return "REDUCE " + parameter + " (" + production + ")";
            case ACCEPT:
                return "ACCEPT";
            case ERROR:
                return "ERROR";
            default:
                return "UNKNOWN";
        }
    }
}