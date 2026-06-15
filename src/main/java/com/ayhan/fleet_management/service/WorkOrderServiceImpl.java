package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.CreateWorkOrderFromMaintenanceTaskRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderAssignmentRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderCompletionRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderRequestDto;
import com.ayhan.fleet_management.dto.WorkOrderResponseDto;
import com.ayhan.fleet_management.entity.MaintenanceTask;
import com.ayhan.fleet_management.entity.User;
import com.ayhan.fleet_management.entity.Vehicle;
import com.ayhan.fleet_management.entity.WorkOrder;
import com.ayhan.fleet_management.entity.enums.WorkOrderStatus;
import com.ayhan.fleet_management.exception.ActiveWorkOrderExistsException;
import com.ayhan.fleet_management.exception.InvalidMaintenanceConfigurationException;
import com.ayhan.fleet_management.exception.InvalidWorkOrderStateException;
import com.ayhan.fleet_management.exception.ResourceNotFoundException;
import com.ayhan.fleet_management.repository.MaintenanceTaskRepository;
import com.ayhan.fleet_management.repository.UserRepository;
import com.ayhan.fleet_management.repository.VehicleRepository;
import com.ayhan.fleet_management.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final Set<WorkOrderStatus> ACTIVE_STATUSES = EnumSet.of(
            WorkOrderStatus.OPEN,
            WorkOrderStatus.ASSIGNED,
            WorkOrderStatus.IN_PROGRESS
    );

    private final WorkOrderRepository workOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final UserRepository userRepository;

    @Override
    public WorkOrderResponseDto createWorkOrder(WorkOrderRequestDto requestDto) {
        Vehicle vehicle = findVehicleById(requestDto.getVehicleId());
        MaintenanceTask maintenanceTask = resolveMaintenanceTask(requestDto.getMaintenanceTaskId());
        validateTaskVehicleAssociation(vehicle, maintenanceTask);
        validateMaintenanceTaskWorkOrderAvailability(maintenanceTask);
        User assignedUser = resolveUser(requestDto.getAssignedUserId());

        WorkOrder workOrder = WorkOrder.builder()
                .vehicle(vehicle)
                .maintenanceTask(maintenanceTask)
                .assignedUser(assignedUser)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .status(assignedUser != null ? WorkOrderStatus.ASSIGNED : WorkOrderStatus.OPEN)
                .build();

        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDto> getAllWorkOrders() {
        return workOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponseDto getWorkOrderById(Long id) {
        return mapToResponseDto(findWorkOrderById(id));
    }

    @Override
    public WorkOrderResponseDto updateWorkOrder(Long id, WorkOrderRequestDto requestDto) {
        WorkOrder workOrder = findWorkOrderById(id);
        ensureNotTerminal(workOrder, "update");

        Vehicle vehicle = findVehicleById(requestDto.getVehicleId());
        MaintenanceTask maintenanceTask = resolveMaintenanceTask(requestDto.getMaintenanceTaskId());
        validateTaskVehicleAssociation(vehicle, maintenanceTask);

        if (maintenanceTask != null && !maintenanceTask.getId().equals(getMaintenanceTaskId(workOrder))) {
            validateMaintenanceTaskWorkOrderAvailability(maintenanceTask);
        }

        User assignedUser = resolveUser(requestDto.getAssignedUserId());

        workOrder.setVehicle(vehicle);
        workOrder.setMaintenanceTask(maintenanceTask);
        workOrder.setAssignedUser(assignedUser);
        workOrder.setTitle(requestDto.getTitle());
        workOrder.setDescription(requestDto.getDescription());

        if (assignedUser != null && workOrder.getStatus() == WorkOrderStatus.OPEN) {
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        }

        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    @Override
    public WorkOrderResponseDto cancelWorkOrder(Long id) {
        WorkOrder workOrder = findWorkOrderById(id);
        transitionToCancelled(workOrder);
        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    @Override
    public WorkOrderResponseDto assignWorkOrder(Long id, WorkOrderAssignmentRequestDto requestDto) {
        WorkOrder workOrder = findWorkOrderById(id);

        if (isTerminal(workOrder.getStatus())) {
            throw new InvalidWorkOrderStateException(
                    "Cannot assign work order with status: " + workOrder.getStatus()
            );
        }

        User assignedUser = findUserById(requestDto.getAssignedUserId());
        workOrder.setAssignedUser(assignedUser);

        if (workOrder.getStatus() == WorkOrderStatus.OPEN) {
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        }

        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    @Override
    public WorkOrderResponseDto startWorkOrder(Long id) {
        WorkOrder workOrder = findWorkOrderById(id);

        if (workOrder.getStatus() != WorkOrderStatus.OPEN && workOrder.getStatus() != WorkOrderStatus.ASSIGNED) {
            throw new InvalidWorkOrderStateException(
                    "Work order can only be started from OPEN or ASSIGNED status"
            );
        }

        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    @Override
    public WorkOrderResponseDto completeWorkOrder(Long id, WorkOrderCompletionRequestDto requestDto) {
        WorkOrder workOrder = findWorkOrderById(id);

        if (!ACTIVE_STATUSES.contains(workOrder.getStatus())) {
            throw new InvalidWorkOrderStateException(
                    "Work order can only be completed from OPEN, ASSIGNED, or IN_PROGRESS status"
            );
        }

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setCompletionNotes(requestDto.getCompletionNotes());
        workOrder.setActualCost(requestDto.getActualCost());
        workOrder.setLaborHours(requestDto.getLaborHours());
        workOrder.setCompletedAt(LocalDateTime.now());

        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDto> getWorkOrdersByVehicle(Long vehicleId) {
        findVehicleById(vehicleId);
        return workOrderRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public WorkOrderResponseDto createWorkOrderFromMaintenanceTask(
            CreateWorkOrderFromMaintenanceTaskRequestDto requestDto
    ) {
        MaintenanceTask maintenanceTask = findMaintenanceTaskById(requestDto.getMaintenanceTaskId());
        validateMaintenanceTaskWorkOrderAvailability(maintenanceTask);
        User assignedUser = resolveUser(requestDto.getAssignedUserId());

        WorkOrder workOrder = WorkOrder.builder()
                .vehicle(maintenanceTask.getVehicle())
                .maintenanceTask(maintenanceTask)
                .assignedUser(assignedUser)
                .title(resolveWorkOrderTitle(requestDto, maintenanceTask))
                .description(resolveWorkOrderDescription(requestDto, maintenanceTask))
                .status(assignedUser != null ? WorkOrderStatus.ASSIGNED : WorkOrderStatus.OPEN)
                .build();

        return mapToResponseDto(workOrderRepository.save(workOrder));
    }

    private String resolveWorkOrderTitle(
            CreateWorkOrderFromMaintenanceTaskRequestDto requestDto,
            MaintenanceTask maintenanceTask
    ) {
        if (requestDto.getTitle() != null && !requestDto.getTitle().isBlank()) {
            return requestDto.getTitle();
        }

        return maintenanceTask.getTitle();
    }

    private String resolveWorkOrderDescription(
            CreateWorkOrderFromMaintenanceTaskRequestDto requestDto,
            MaintenanceTask maintenanceTask
    ) {
        if (requestDto.getDescription() != null && !requestDto.getDescription().isBlank()) {
            return requestDto.getDescription();
        }

        return maintenanceTask.getDescription();
    }

    private void validateMaintenanceTaskWorkOrderAvailability(MaintenanceTask maintenanceTask) {
        if (maintenanceTask != null
                && workOrderRepository.existsByMaintenanceTaskIdAndStatusIn(maintenanceTask.getId(), ACTIVE_STATUSES)) {
            throw new ActiveWorkOrderExistsException(
                    "An active work order already exists for maintenance task id: " + maintenanceTask.getId()
            );
        }
    }

    private void validateTaskVehicleAssociation(Vehicle vehicle, MaintenanceTask maintenanceTask) {
        if (maintenanceTask != null && !maintenanceTask.getVehicle().getId().equals(vehicle.getId())) {
            throw new InvalidMaintenanceConfigurationException(
                    "Maintenance task does not belong to the specified vehicle"
            );
        }
    }

    private void transitionToCancelled(WorkOrder workOrder) {
        if (!ACTIVE_STATUSES.contains(workOrder.getStatus())) {
            throw new InvalidWorkOrderStateException(
                    "Work order can only be cancelled from OPEN, ASSIGNED, or IN_PROGRESS status"
            );
        }

        workOrder.setStatus(WorkOrderStatus.CANCELLED);
    }

    private void ensureNotTerminal(WorkOrder workOrder, String action) {
        if (isTerminal(workOrder.getStatus())) {
            throw new InvalidWorkOrderStateException(
                    "Cannot " + action + " work order with status: " + workOrder.getStatus()
            );
        }
    }

    private boolean isTerminal(WorkOrderStatus status) {
        return status == WorkOrderStatus.COMPLETED || status == WorkOrderStatus.CANCELLED;
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private MaintenanceTask findMaintenanceTaskById(Long id) {
        return maintenanceTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance task not found with id: " + id));
    }

    private MaintenanceTask resolveMaintenanceTask(Long id) {
        return id != null ? findMaintenanceTaskById(id) : null;
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private User resolveUser(Long id) {
        return id != null ? findUserById(id) : null;
    }

    private WorkOrder findWorkOrderById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
    }

    private Long getMaintenanceTaskId(WorkOrder workOrder) {
        return workOrder.getMaintenanceTask() != null ? workOrder.getMaintenanceTask().getId() : null;
    }

    private WorkOrderResponseDto mapToResponseDto(WorkOrder workOrder) {
        return WorkOrderResponseDto.builder()
                .id(workOrder.getId())
                .vehicleId(workOrder.getVehicle().getId())
                .vehicleName(workOrder.getVehicle().getName())
                .maintenanceTaskId(getMaintenanceTaskId(workOrder))
                .maintenanceTaskTitle(
                        workOrder.getMaintenanceTask() != null ? workOrder.getMaintenanceTask().getTitle() : null
                )
                .assignedUserId(workOrder.getAssignedUser() != null ? workOrder.getAssignedUser().getId() : null)
                .assignedUsername(
                        workOrder.getAssignedUser() != null ? workOrder.getAssignedUser().getUsername() : null
                )
                .status(workOrder.getStatus())
                .title(workOrder.getTitle())
                .description(workOrder.getDescription())
                .completionNotes(workOrder.getCompletionNotes())
                .actualCost(workOrder.getActualCost())
                .laborHours(workOrder.getLaborHours())
                .completedAt(workOrder.getCompletedAt())
                .createdAt(workOrder.getCreatedAt())
                .updatedAt(workOrder.getUpdatedAt())
                .build();
    }
}
