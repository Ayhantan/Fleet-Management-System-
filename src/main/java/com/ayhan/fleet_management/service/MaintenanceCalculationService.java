package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.entity.MaintenanceSchedule;
import com.ayhan.fleet_management.entity.MaintenanceTask;
import com.ayhan.fleet_management.entity.Vehicle;
import com.ayhan.fleet_management.entity.enums.CalculatedMaintenanceStatus;
import com.ayhan.fleet_management.entity.enums.MaintenanceTaskStatus;
import com.ayhan.fleet_management.entity.enums.MaintenanceTriggerType;
import com.ayhan.fleet_management.repository.MaintenanceTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MaintenanceCalculationService {

    private static final int UPCOMING_HOUR_THRESHOLD = 50;
    private static final int UPCOMING_DISTANCE_THRESHOLD = 500;
    private static final int UPCOMING_DAY_THRESHOLD = 30;

    private final MaintenanceTaskRepository maintenanceTaskRepository;

    public void recalculateSchedule(MaintenanceSchedule schedule) {
        Vehicle vehicle = schedule.getVehicle();
        MaintenanceTask lastCompletedTask = maintenanceTaskRepository
                .findTopByMaintenanceScheduleIdAndStatusOrderByCompletedDateDescCreatedAtDesc(
                        schedule.getId(),
                        MaintenanceTaskStatus.COMPLETED
                )
                .orElse(null);

        LocalDate baselineDate = resolveBaselineDate(vehicle, lastCompletedTask);
        Integer baselineHour = resolveBaselineHour(vehicle, lastCompletedTask);
        Integer baselineDistance = resolveBaselineDistance(vehicle, lastCompletedTask);

        schedule.setNextDueDate(null);
        schedule.setNextDueHourMeter(null);
        schedule.setNextDueDistanceReading(null);

        if (requiresTime(schedule.getTriggerType()) && baselineDate != null && schedule.getIntervalTimeValue() != null) {
            schedule.setNextDueDate(addTimeInterval(baselineDate, schedule.getIntervalTimeValue(), schedule.getIntervalTimeUnit()));
        }

        if (requiresHours(schedule.getTriggerType()) && baselineHour != null && schedule.getIntervalHour() != null) {
            schedule.setNextDueHourMeter(baselineHour + schedule.getIntervalHour());
        }

        if (requiresDistance(schedule.getTriggerType())
                && baselineDistance != null
                && schedule.getIntervalDistance() != null) {
            schedule.setNextDueDistanceReading(baselineDistance + schedule.getIntervalDistance());
        }
    }

    public CalculatedMaintenanceStatus calculateStatus(Vehicle vehicle, MaintenanceSchedule schedule) {
        if (isOverdue(vehicle, schedule)) {
            return CalculatedMaintenanceStatus.OVERDUE;
        }

        if (isUpcoming(vehicle, schedule)) {
            return CalculatedMaintenanceStatus.UPCOMING;
        }

        return CalculatedMaintenanceStatus.ON_TRACK;
    }

    private boolean isOverdue(Vehicle vehicle, MaintenanceSchedule schedule) {
        return (schedule.getNextDueDate() != null && LocalDate.now().isAfter(schedule.getNextDueDate()))
                || (schedule.getNextDueHourMeter() != null
                && vehicle.getCurrentHourMeter() != null
                && vehicle.getCurrentHourMeter() >= schedule.getNextDueHourMeter())
                || (schedule.getNextDueDistanceReading() != null
                && vehicle.getCurrentDistanceReading() != null
                && vehicle.getCurrentDistanceReading() >= schedule.getNextDueDistanceReading());
    }

    private boolean isUpcoming(Vehicle vehicle, MaintenanceSchedule schedule) {
        return (schedule.getNextDueDate() != null
                && !LocalDate.now().isAfter(schedule.getNextDueDate())
                && ChronoUnit.DAYS.between(LocalDate.now(), schedule.getNextDueDate()) <= UPCOMING_DAY_THRESHOLD)
                || (schedule.getNextDueHourMeter() != null
                && vehicle.getCurrentHourMeter() != null
                && schedule.getNextDueHourMeter() - vehicle.getCurrentHourMeter() <= UPCOMING_HOUR_THRESHOLD)
                || (schedule.getNextDueDistanceReading() != null
                && vehicle.getCurrentDistanceReading() != null
                && schedule.getNextDueDistanceReading() - vehicle.getCurrentDistanceReading()
                <= UPCOMING_DISTANCE_THRESHOLD);
    }

    private LocalDate resolveBaselineDate(Vehicle vehicle, MaintenanceTask task) {
        if (task != null && task.getCompletedDate() != null) {
            return task.getCompletedDate();
        }
        return vehicle.getLastMaintenanceDate() != null ? vehicle.getLastMaintenanceDate() : LocalDate.now();
    }

    private Integer resolveBaselineHour(Vehicle vehicle, MaintenanceTask task) {
        if (task != null && task.getCompletedHourMeter() != null) {
            return task.getCompletedHourMeter();
        }
        return vehicle.getLastMaintenanceHourMeter() != null
                ? vehicle.getLastMaintenanceHourMeter()
                : vehicle.getCurrentHourMeter();
    }

    private Integer resolveBaselineDistance(Vehicle vehicle, MaintenanceTask task) {
        if (task != null && task.getCompletedDistanceReading() != null) {
            return task.getCompletedDistanceReading();
        }
        return vehicle.getLastMaintenanceDistanceReading() != null
                ? vehicle.getLastMaintenanceDistanceReading()
                : vehicle.getCurrentDistanceReading();
    }

    private LocalDate addTimeInterval(LocalDate date, Integer value, com.ayhan.fleet_management.entity.enums.TimeIntervalUnit unit) {
        if (unit == null) {
            return date;
        }
        return switch (unit) {
            case DAY -> date.plusDays(value);
            case WEEK -> date.plusWeeks(value);
            case MONTH -> date.plusMonths(value);
            case YEAR -> date.plusYears(value);
        };
    }

    private boolean requiresHours(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.HOURS
                || triggerType == MaintenanceTriggerType.TIME_AND_HOURS;
    }

    private boolean requiresDistance(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.DISTANCE
                || triggerType == MaintenanceTriggerType.TIME_AND_DISTANCE;
    }

    private boolean requiresTime(MaintenanceTriggerType triggerType) {
        return triggerType == MaintenanceTriggerType.TIME
                || triggerType == MaintenanceTriggerType.TIME_AND_HOURS
                || triggerType == MaintenanceTriggerType.TIME_AND_DISTANCE;
    }
}
