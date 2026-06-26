package com.ycf.liveborrowsample.domain.service;

import com.ycf.liveborrowsample.domain.enums.TaskAction;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TaskStateMachine {

    private final Map<TaskStatus, Set<TaskAction>> allowedTransitions = new EnumMap<>(TaskStatus.class);

    public TaskStateMachine() {
        allowedTransitions.put(TaskStatus.CREATED, EnumSet.of(TaskAction.SUBMIT_APPLY));
        allowedTransitions.put(TaskStatus.AUDITING, EnumSet.of(TaskAction.APPROVE_APPLY, TaskAction.REJECT_APPLY));
        allowedTransitions.put(TaskStatus.SOURCING, EnumSet.of(TaskAction.CREATE_OUTBOUND_TRANSFER));
        allowedTransitions.put(TaskStatus.WAIT_SHIP, EnumSet.of(TaskAction.CONFIRM_RECEIVE));
        allowedTransitions.put(TaskStatus.WAIT_PICKUP, EnumSet.of(TaskAction.CONFIRM_PICKUP));
        allowedTransitions.put(TaskStatus.IN_TRANSIT, EnumSet.of(TaskAction.CONFIRM_RECEIVE));
        allowedTransitions.put(TaskStatus.BORROWING, EnumSet.of(TaskAction.CREATE_RETURN_BATCH));
        allowedTransitions.put(TaskStatus.RETURNING, EnumSet.of(TaskAction.FILL_RETURN_LOGISTICS, TaskAction.CONFIRM_STORE_RECEIVE));
        allowedTransitions.put(TaskStatus.QUALITY_CHECKING, EnumSet.of(TaskAction.COMPLETE_QUALITY_CHECK));
    }

    public boolean canExecute(TaskStatus currentStatus, TaskAction action) {
        return allowedTransitions.getOrDefault(currentStatus, EnumSet.noneOf(TaskAction.class)).contains(action);
    }
}
