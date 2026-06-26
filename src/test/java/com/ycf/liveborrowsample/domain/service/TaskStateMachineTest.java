package com.ycf.liveborrowsample.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ycf.liveborrowsample.domain.enums.TaskAction;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import org.junit.jupiter.api.Test;

class TaskStateMachineTest {

    private final TaskStateMachine taskStateMachine = new TaskStateMachine();

    @Test
    void shouldAllowConfirmReceiveWhenInTransit() {
        assertTrue(taskStateMachine.canExecute(TaskStatus.IN_TRANSIT, TaskAction.CONFIRM_RECEIVE));
    }

    @Test
    void shouldRejectConfirmPickupWhenBorrowing() {
        assertFalse(taskStateMachine.canExecute(TaskStatus.BORROWING, TaskAction.CONFIRM_PICKUP));
    }
}
