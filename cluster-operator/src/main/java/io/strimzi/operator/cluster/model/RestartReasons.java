/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects reasons to restart a pod, along with additional logging messages for each kind of restart.
 */
public class RestartReasons implements Iterable<RestartReason> {

    private final EnumMap<RestartReason, Set<String>> reasons = new EnumMap<>(RestartReason.class);


    public static RestartReasons empty() {
        return new RestartReasons();
    }

    public static RestartReasons of(RestartReason reason) {
        return new RestartReasons().add(reason);
    }

    /**
     * Add restart reason with a specific message. If multiple different messages are passed for a given reason, they'll be retained and appended (if not already present)
     * @param reason why the pod is being rolled
     * @param explicitNote note to use instead of the default message for the given reason
     * @return this instance, to make it easy to chain additions
     */
    public RestartReasons add(RestartReason reason, String explicitNote) {
        Set<String> notes = reasons.computeIfAbsent(reason, unused -> new HashSet<>());
        if (explicitNote != null) {
            notes.add(explicitNote);
        }

        reasons.put(reason, notes);
        return this;
    }

    /**
     * Add restart reason without an explicitly set note, logging and K8s events will use default for given Reason
     * @param reason why the pod is being rolled
     * @return this instance, to make it easy to chain additions
     */
    public RestartReasons add(RestartReason reason) {
        return add(reason, null);
    }

    public Set<RestartReason> getReasons() {
        return reasons.keySet();
    }

    public boolean shouldRestart() {
        return !reasons.isEmpty();
    }

    public boolean contains(RestartReason reason) {
        return reasons.containsKey(reason);
    }

    @Override
    public Iterator<RestartReason> iterator() {
        return reasons.keySet().iterator();
    }

    public String getNoteFor(RestartReason reason) {
        if (!reasons.containsKey(reason)) {
            return null;
        }

        Set<String> explicitNotes = reasons.get(reason);
        if (explicitNotes.isEmpty()) {
            return reason.getDefaultNote();
        } else {
            return String.join(", ", explicitNotes);
        }
    }

    // For logging, generally
    public List<String> getAllReasonNotes() {
        return reasons.entrySet().stream().flatMap(entry -> {
            Set<String> explicitNotes = entry.getValue();
            if (explicitNotes.isEmpty()) {
                return Stream.of(entry.getKey().getDefaultNote());
            } else {
                return explicitNotes.stream();
            }
        })
        .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestartReasons that = (RestartReasons) o;
        return Objects.equals(reasons, that.reasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reasons);
    }

    @Override
    public String toString() {
        return reasons.keySet().toString();
    }
}
