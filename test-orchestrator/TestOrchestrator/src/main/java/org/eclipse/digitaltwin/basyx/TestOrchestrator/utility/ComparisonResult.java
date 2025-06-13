package org.eclipse.digitaltwin.basyx.TestOrchestrator.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


/**
 * This class represents the result of a comparison between two environments.
 * It holds lists of errors, warnings, differences, and informational messages found during the comparison.
 */
public class ComparisonResult {


    // List to store critical errors (mismatches or critical validation failures)
    private List<String> errors = new ArrayList<>();

    // List to store warnings (non-critical issues such as spaces or optional elements)
    private List<String> warnings = new ArrayList<>();

    // List to store differences (non-blocking differences in data)
    private List<String> differences = new ArrayList<>();

    // List to store informational messages (e.g., for allowed flexible elements)
    private List<String> infos = new ArrayList<>();

    private Set<String> correctElements = new HashSet<>();
    private Set<String> errorElements = new HashSet<>();
    private Set<String> warningElements = new HashSet<>();

    public void markError(String semanticId) {
        errorElements.add(semanticId);
    }

    public void markWarning(String semanticId) {
        if (!errorElements.contains(semanticId)) {
            warningElements.add(semanticId);
        }
    }

    public void markCorrect(String semanticId) {
        if (!errorElements.contains(semanticId) && !warningElements.contains(semanticId)) {
            correctElements.add(semanticId);
        }
    }



    /**
     * Adds an error to the list of errors (critical validation issues).
     *
     * @param error the error message to add
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * Adds a warning to the list of warnings (non-blocking issues).
     *
     * @param warning the warning message to add
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * Adds a difference to the list of differences (non-blocking differences).
     *
     * @param difference the difference message to add
     */
    public void addDifference(String difference) {
        differences.add(difference);
    }

    /**
     * Adds an informational message (for non-critical allowed elements).
     *
     * @param info the informational message to add
     */
    public void addInfo(String info) {
        infos.add(info);
    }

    /**
     * Gets the list of errors.
     *
     * @return the list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Gets the list of warnings.
     *
     * @return the list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Gets the list of differences.
     *
     * @return the list of differences
     */
    public List<String> getDifferences() {
        return differences;
    }

    /**
     * Gets the list of informational messages.
     *
     * @return the list of informational messages
     */
    public List<String> getInfos() {
        return infos;
    }


    /**
     * Checks if the comparison result is valid, i.e., there are no critical errors.
     *
     * @return true if there are no errors, false otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Returns a string representation of the comparison result, categorizing errors, warnings, differences, and informational messages.
     *
     * @return a string representation of the comparison result
     */
    @Override
    public String toString() {
        return "ComparisonResult{" +
                "errors=" + errors +
                ", warnings=" + warnings +
                ", differences=" + differences +
                ", infos=" + infos +
                ", valid=" + isValid() +
                '}';
    }
}
