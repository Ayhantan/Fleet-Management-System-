import { useEffect, useMemo, useState } from "react";
import LoadingState from "../components/LoadingState.jsx";
import { getVehicles } from "../services/vehicleService.js";
import {
  completeMaintenanceTask,
  createMaintenanceDefinition,
  createMaintenanceSchedule,
  createMaintenanceTask,
  deleteMaintenanceDefinition,
  deleteMaintenanceSchedule,
  deleteMaintenanceTask,
  getMaintenanceDefinitions,
  getMaintenanceSchedules,
  getMaintenanceTasks,
  recalculateMaintenanceSchedule,
  updateMaintenanceDefinition,
} from "../services/maintenanceService.js";
import { formatDate, formatDateTime, formatEnumLabel } from "../utils/formatters.js";

const maintenanceTriggerTypes = ["TIME", "HOURS", "DISTANCE", "TIME_AND_HOURS", "TIME_AND_DISTANCE"];
const timeIntervalUnits = ["DAY", "WEEK", "MONTH", "YEAR"];
const maintenancePriorities = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
const taskStatuses = ["PLANNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"];

const defaultDefinitionForm = {
  name: "",
  description: "",
  category: "",
  applicableAssetType: "",
  active: true,
};

const defaultScheduleForm = {
  vehicleId: "",
  maintenanceDefinitionId: "",
  triggerType: "DISTANCE",
  intervalHour: "",
  intervalDistance: "",
  intervalTimeValue: "",
  intervalTimeUnit: "MONTH",
  active: true,
};

const defaultTaskForm = {
  vehicleId: "",
  maintenanceScheduleId: "",
  maintenanceDefinitionId: "",
  title: "",
  description: "",
  priority: "MEDIUM",
  plannedDate: "",
  dueDate: "",
  dueHourMeter: "",
  dueDistanceReading: "",
  notes: "",
};

const defaultCompleteForm = {
  completedDate: "",
  completedHourMeter: "",
  completedDistanceReading: "",
  notes: "",
};

const emptyDataState = {
  definitions: [],
  schedules: [],
  tasks: [],
  vehicles: [],
};

function MaintenancePage() {
  const [definitions, setDefinitions] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [modal, setModal] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [definitionForm, setDefinitionForm] = useState(defaultDefinitionForm);
  const [scheduleForm, setScheduleForm] = useState(defaultScheduleForm);
  const [taskForm, setTaskForm] = useState(defaultTaskForm);
  const [completeForm, setCompleteForm] = useState(defaultCompleteForm);
  const [formError, setFormError] = useState("");

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === "Escape") {
        closeModal();
      }
    }

    if (modal || deleteTarget) {
      window.addEventListener("keydown", handleKeyDown);
      return () => window.removeEventListener("keydown", handleKeyDown);
    }

    return undefined;
  }, [modal, deleteTarget]);

  const vehicleOptions = useMemo(
    () =>
      vehicles
        .map((vehicle) => ({
          value: String(vehicle.id),
          label: [vehicle.name, vehicle.plateNumber ? `(${vehicle.plateNumber})` : ""]
            .filter(Boolean)
            .join(" "),
        }))
        .sort((left, right) => left.label.localeCompare(right.label)),
    [vehicles],
  );

  const definitionOptions = useMemo(
    () =>
      definitions
        .map((definition) => ({
          value: String(definition.id),
          label: [definition.name, definition.category ? `- ${definition.category}` : ""]
            .filter(Boolean)
            .join(" "),
        }))
        .sort((left, right) => left.label.localeCompare(right.label)),
    [definitions],
  );

  const scheduleOptions = useMemo(
    () =>
      schedules.map((schedule) => ({
        value: String(schedule.id),
        label: `${schedule.vehicleName || "Vehicle"} - ${schedule.maintenanceDefinitionName || "Schedule"}`,
      })),
    [schedules],
  );

  const stats = useMemo(
    () => ({
      definitions: definitions.length,
      schedules: schedules.length,
      tasks: tasks.length,
      activeDefinitions: definitions.filter((definition) => definition.active !== false).length,
      activeSchedules: schedules.filter((schedule) => schedule.active !== false).length,
      openTasks: tasks.filter((task) => task.status !== "COMPLETED" && task.status !== "CANCELLED").length,
    }),
    [definitions, schedules, tasks],
  );

  async function loadData() {
    try {
      setLoading(true);
      setError("");

      const results = await Promise.allSettled([
        getMaintenanceDefinitions(),
        getMaintenanceSchedules(),
        getMaintenanceTasks(),
        getVehicles(),
      ]);

      const [definitionsResult, schedulesResult, tasksResult, vehiclesResult] = results;

      if (definitionsResult.status === "fulfilled") {
        setDefinitions(Array.isArray(definitionsResult.value) ? definitionsResult.value : []);
      }
      if (schedulesResult.status === "fulfilled") {
        setSchedules(Array.isArray(schedulesResult.value) ? schedulesResult.value : []);
      }
      if (tasksResult.status === "fulfilled") {
        setTasks(Array.isArray(tasksResult.value) ? tasksResult.value : []);
      }
      if (vehiclesResult.status === "fulfilled") {
        setVehicles(Array.isArray(vehiclesResult.value) ? vehiclesResult.value : []);
      }

      const failedSections = [];
      if (definitionsResult.status === "rejected") failedSections.push("definitions");
      if (schedulesResult.status === "rejected") failedSections.push("schedules");
      if (tasksResult.status === "rejected") failedSections.push("tasks");
      if (vehiclesResult.status === "rejected") failedSections.push("vehicles");

      if (failedSections.length > 0) {
        setError(`Some data could not be loaded: ${failedSections.join(", ")}.`);
      }
    } catch (requestError) {
      setError(requestError.userMessage || "Maintenance data could not be loaded.");
    } finally {
      setLoading(false);
    }
  }

  function openDefinitionCreate() {
    setFormError("");
    setDefinitionForm(defaultDefinitionForm);
    setModal({ type: "definition", mode: "create" });
  }

  function openDefinitionEdit(definition) {
    setFormError("");
    setDefinitionForm({
      name: definition.name || "",
      description: definition.description || "",
      category: definition.category || "",
      applicableAssetType: definition.applicableAssetType || "",
      active: definition.active !== false,
    });
    setModal({ type: "definition", mode: "edit", item: definition });
  }

  function openScheduleCreate() {
    setFormError("");
    setScheduleForm(defaultScheduleForm);
    setModal({ type: "schedule", mode: "create" });
  }

  function openTaskCreate() {
    setFormError("");
    setTaskForm(defaultTaskForm);
    setModal({ type: "task", mode: "create" });
  }

  function openTaskComplete(task) {
    setFormError("");
    setCompleteForm({
      completedDate: task.completedDate || "",
      completedHourMeter: task.completedHourMeter ?? "",
      completedDistanceReading: task.completedDistanceReading ?? "",
      notes: task.notes || "",
    });
    setModal({ type: "complete-task", item: task });
  }

  function requestDelete(entityType, item) {
    setFormError("");
    setDeleteTarget({ entityType, item });
  }

  function closeModal() {
    if (saving) {
      return;
    }

    setModal(null);
    setDeleteTarget(null);
    setFormError("");
  }

  function updateDefinitionForm(field, value) {
    setDefinitionForm((current) => ({ ...current, [field]: value }));
  }

  function updateScheduleForm(field, value) {
    setScheduleForm((current) => ({ ...current, [field]: value }));
  }

  function updateTaskForm(field, value) {
    setTaskForm((current) => ({ ...current, [field]: value }));
  }

  function updateCompleteForm(field, value) {
    setCompleteForm((current) => ({ ...current, [field]: value }));
  }

  async function handleDefinitionSubmit(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setFormError("");

      const payload = {
        name: definitionForm.name.trim(),
        description: normalizeOptionalString(definitionForm.description),
        category: normalizeOptionalString(definitionForm.category),
        applicableAssetType: normalizeOptionalString(definitionForm.applicableAssetType),
        active: Boolean(definitionForm.active),
      };

      if (modal?.mode === "edit" && modal.item?.id) {
        await updateMaintenanceDefinition(modal.item.id, payload);
      } else {
        await createMaintenanceDefinition(payload);
      }

      closeModal();
      await loadData();
    } catch (requestError) {
      setFormError(requestError.userMessage || "Definition could not be saved.");
    } finally {
      setSaving(false);
    }
  }

  async function handleScheduleSubmit(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setFormError("");

      const payload = {
        vehicleId: toOptionalNumber(scheduleForm.vehicleId),
        maintenanceDefinitionId: toOptionalNumber(scheduleForm.maintenanceDefinitionId),
        triggerType: scheduleForm.triggerType,
        intervalHour: toOptionalNumber(scheduleForm.intervalHour),
        intervalDistance: toOptionalNumber(scheduleForm.intervalDistance),
        intervalTimeValue: toOptionalNumber(scheduleForm.intervalTimeValue),
        intervalTimeUnit: scheduleForm.intervalTimeValue === "" ? null : scheduleForm.intervalTimeUnit,
        active: Boolean(scheduleForm.active),
      };

      await createMaintenanceSchedule(payload);
      closeModal();
      await loadData();
    } catch (requestError) {
      setFormError(requestError.userMessage || "Schedule could not be created.");
    } finally {
      setSaving(false);
    }
  }

  async function handleTaskSubmit(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setFormError("");

      const payload = {
        vehicleId: toOptionalNumber(taskForm.vehicleId),
        maintenanceScheduleId: toOptionalNumber(taskForm.maintenanceScheduleId),
        maintenanceDefinitionId: toOptionalNumber(taskForm.maintenanceDefinitionId),
        title: taskForm.title.trim(),
        description: normalizeOptionalString(taskForm.description),
        priority: taskForm.priority,
        plannedDate: normalizeOptionalString(taskForm.plannedDate),
        dueDate: normalizeOptionalString(taskForm.dueDate),
        dueHourMeter: toOptionalNumber(taskForm.dueHourMeter),
        dueDistanceReading: toOptionalNumber(taskForm.dueDistanceReading),
        notes: normalizeOptionalString(taskForm.notes),
      };

      await createMaintenanceTask(payload);
      closeModal();
      await loadData();
    } catch (requestError) {
      setFormError(requestError.userMessage || "Task could not be created.");
    } finally {
      setSaving(false);
    }
  }

  async function handleCompleteSubmit(event) {
    event.preventDefault();

    if (!modal?.item?.id) {
      return;
    }

    try {
      setSaving(true);
      setFormError("");

      await completeMaintenanceTask(modal.item.id, {
        completedDate: normalizeOptionalString(completeForm.completedDate),
        completedHourMeter: toOptionalNumber(completeForm.completedHourMeter),
        completedDistanceReading: toOptionalNumber(completeForm.completedDistanceReading),
        notes: normalizeOptionalString(completeForm.notes),
      });

      closeModal();
      await loadData();
    } catch (requestError) {
      setFormError(requestError.userMessage || "Task could not be completed.");
    } finally {
      setSaving(false);
    }
  }

  async function handleConfirmDelete() {
    if (!deleteTarget) {
      return;
    }

    try {
      setSaving(true);
      setFormError("");

      const { entityType, item } = deleteTarget;

      if (entityType === "definition") {
        await deleteMaintenanceDefinition(item.id);
      } else if (entityType === "schedule") {
        await deleteMaintenanceSchedule(item.id);
      } else if (entityType === "task") {
        await deleteMaintenanceTask(item.id);
      }

      closeModal();
      await loadData();
    } catch (requestError) {
      setFormError(requestError.userMessage || "Item could not be deleted.");
    } finally {
      setSaving(false);
    }
  }

  async function handleRecalculate(schedule) {
    try {
      setSaving(true);
      setFormError("");
      await recalculateMaintenanceSchedule(schedule.id);
      await loadData();
    } catch (requestError) {
      setFormError(requestError.userMessage || "Schedule could not be recalculated.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <LoadingState message="Maintenance module is loading." />;
  }

  return (
    <div className="space-y-6">
      <section className="rounded-[2rem] border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Maintenance Control Center
            </p>
            <h3 className="mt-2 text-2xl font-semibold text-ink-900">Definitions, schedules and tasks</h3>
            <p className="mt-2 max-w-2xl text-sm text-ink-500">
              Manage the core maintenance modules from one responsive screen.
            </p>
          </div>
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <StatPill label="Definitions" value={stats.definitions} />
            <StatPill label="Schedules" value={stats.schedules} />
            <StatPill label="Tasks" value={stats.tasks} />
          </div>
        </div>
        {error ? (
          <div className="mt-5 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
            {error}
          </div>
        ) : null}
      </section>

      <section className="grid gap-4 xl:grid-cols-3">
        <SummaryCard label="Active Definitions" value={stats.activeDefinitions} />
        <SummaryCard label="Active Schedules" value={stats.activeSchedules} />
        <SummaryCard label="Open Tasks" value={stats.openTasks} />
      </section>

      <section className="rounded-[2rem] border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Maintenance Definitions
            </p>
            <h4 className="mt-2 text-xl font-semibold text-ink-900">Master data</h4>
          </div>
          <button
            type="button"
            onClick={openDefinitionCreate}
            className="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
          >
            Add Definition
          </button>
        </div>

        <div className="mt-6 hidden overflow-hidden rounded-3xl border border-ink-200 md:block">
          <table className="min-w-full divide-y divide-ink-200">
            <thead className="bg-ink-50">
              <tr className="text-left text-sm text-ink-500">
                <th className="px-5 py-4 font-medium">Name</th>
                <th className="px-5 py-4 font-medium">Category</th>
                <th className="px-5 py-4 font-medium">Asset Type</th>
                <th className="px-5 py-4 font-medium">Status</th>
                <th className="px-5 py-4 font-medium">Updated</th>
                <th className="px-5 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {definitions.map((definition) => (
                <tr key={definition.id} className="text-sm text-ink-700">
                  <td className="px-5 py-4">
                    <div className="font-semibold text-ink-900">{definition.name}</div>
                    <div className="mt-1 text-xs text-ink-500">{definition.description || "-"}</div>
                  </td>
                  <td className="px-5 py-4">{definition.category || "-"}</td>
                  <td className="px-5 py-4">{definition.applicableAssetType || "-"}</td>
                  <td className="px-5 py-4">
                    <StatusBadge active={definition.active !== false} />
                  </td>
                  <td className="px-5 py-4">{formatDateTime(definition.updatedAt || definition.createdAt)}</td>
                  <td className="px-5 py-4">
                    <div className="flex justify-end gap-2">
                      <ActionButton onClick={() => openDefinitionEdit(definition)}>Edit</ActionButton>
                      <ActionButton onClick={() => requestDelete("definition", definition)} tone="danger">
                        Delete
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-6 grid gap-4 md:hidden">
          {definitions.map((definition) => (
            <article key={definition.id} className="rounded-3xl border border-ink-200 bg-white p-5 shadow-soft">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-lg font-semibold text-ink-900">{definition.name}</p>
                  <p className="mt-1 text-sm text-ink-500">{definition.description || "No description"}</p>
                </div>
                <StatusBadge active={definition.active !== false} />
              </div>
              <div className="mt-4 grid gap-3 text-sm text-ink-600">
                <InfoLine label="Category" value={definition.category} />
                <InfoLine label="Asset Type" value={definition.applicableAssetType} />
                <InfoLine label="Updated" value={formatDateTime(definition.updatedAt || definition.createdAt)} />
              </div>
              <div className="mt-4 flex gap-2">
                <ActionButton onClick={() => openDefinitionEdit(definition)}>Edit</ActionButton>
                <ActionButton onClick={() => requestDelete("definition", definition)} tone="danger">
                  Delete
                </ActionButton>
              </div>
            </article>
          ))}
        </div>

        {definitions.length === 0 ? <EmptyState text="No maintenance definitions were returned." /> : null}
      </section>

      <section className="rounded-[2rem] border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Maintenance Schedules
            </p>
            <h4 className="mt-2 text-xl font-semibold text-ink-900">Vehicle-linked planning</h4>
          </div>
          <button
            type="button"
            onClick={openScheduleCreate}
            className="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
          >
            Add Schedule
          </button>
        </div>

        <div className="mt-6 hidden overflow-hidden rounded-3xl border border-ink-200 lg:block">
          <table className="min-w-full divide-y divide-ink-200">
            <thead className="bg-ink-50">
              <tr className="text-left text-sm text-ink-500">
                <th className="px-5 py-4 font-medium">Vehicle</th>
                <th className="px-5 py-4 font-medium">Definition</th>
                <th className="px-5 py-4 font-medium">Trigger</th>
                <th className="px-5 py-4 font-medium">Next Due</th>
                <th className="px-5 py-4 font-medium">Status</th>
                <th className="px-5 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {schedules.map((schedule) => (
                <tr key={schedule.id} className="text-sm text-ink-700">
                  <td className="px-5 py-4 font-semibold text-ink-900">{schedule.vehicleName || "-"}</td>
                  <td className="px-5 py-4">
                    <div className="font-medium text-ink-900">{schedule.maintenanceDefinitionName || "-"}</div>
                    <div className="mt-1 text-xs text-ink-500">{schedule.maintenanceDefinitionCategory || "-"}</div>
                  </td>
                  <td className="px-5 py-4">
                    <div>{formatEnumLabel(schedule.triggerType)}</div>
                    <div className="mt-1 text-xs text-ink-500">{renderScheduleIntervals(schedule)}</div>
                  </td>
                  <td className="px-5 py-4">{renderScheduleDue(schedule)}</td>
                  <td className="px-5 py-4">
                    <ScheduleStatusBadge status={schedule.calculatedStatus} active={schedule.active !== false} />
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex justify-end gap-2">
                      <ActionButton onClick={() => handleRecalculate(schedule)}>Recalculate</ActionButton>
                      <ActionButton onClick={() => requestDelete("schedule", schedule)} tone="danger">
                        Delete
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-6 grid gap-4 lg:hidden">
          {schedules.map((schedule) => (
            <article key={schedule.id} className="rounded-3xl border border-ink-200 bg-white p-5 shadow-soft">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-lg font-semibold text-ink-900">{schedule.vehicleName || "Vehicle"}</p>
                  <p className="mt-1 text-sm text-ink-500">{schedule.maintenanceDefinitionName || "-"}</p>
                </div>
                <ScheduleStatusBadge status={schedule.calculatedStatus} active={schedule.active !== false} />
              </div>
              <div className="mt-4 grid gap-3 text-sm text-ink-600">
                <InfoLine label="Trigger" value={formatEnumLabel(schedule.triggerType)} />
                <InfoLine label="Intervals" value={renderScheduleIntervals(schedule)} />
                <InfoLine label="Next Due" value={renderScheduleDue(schedule)} />
              </div>
              <div className="mt-4 flex gap-2">
                <ActionButton onClick={() => handleRecalculate(schedule)}>Recalculate</ActionButton>
                <ActionButton onClick={() => requestDelete("schedule", schedule)} tone="danger">
                  Delete
                </ActionButton>
              </div>
            </article>
          ))}
        </div>

        {schedules.length === 0 ? <EmptyState text="No maintenance schedules were returned." /> : null}
      </section>

      <section className="rounded-[2rem] border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Maintenance Tasks
            </p>
            <h4 className="mt-2 text-xl font-semibold text-ink-900">Planning and completion</h4>
          </div>
          <button
            type="button"
            onClick={openTaskCreate}
            className="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
          >
            Add Task
          </button>
        </div>

        <div className="mt-6 hidden overflow-hidden rounded-3xl border border-ink-200 xl:block">
          <table className="min-w-full divide-y divide-ink-200">
            <thead className="bg-ink-50">
              <tr className="text-left text-sm text-ink-500">
                <th className="px-5 py-4 font-medium">Title</th>
                <th className="px-5 py-4 font-medium">Vehicle</th>
                <th className="px-5 py-4 font-medium">Priority</th>
                <th className="px-5 py-4 font-medium">Status</th>
                <th className="px-5 py-4 font-medium">Due Date</th>
                <th className="px-5 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {tasks.map((task) => (
                <tr key={task.id} className="text-sm text-ink-700">
                  <td className="px-5 py-4">
                    <div className="font-semibold text-ink-900">{task.title}</div>
                    <div className="mt-1 text-xs text-ink-500">{task.maintenanceDefinitionName || "-"}</div>
                  </td>
                  <td className="px-5 py-4 font-medium text-ink-900">{task.vehicleName || "-"}</td>
                  <td className="px-5 py-4">
                    <PriorityBadge priority={task.priority} />
                  </td>
                  <td className="px-5 py-4">
                    <TaskStatusBadge status={task.status} />
                  </td>
                  <td className="px-5 py-4">
                    <div>{formatDate(task.dueDate)}</div>
                    <div className="mt-1 text-xs text-ink-500">{renderTaskDue(task)}</div>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex justify-end gap-2">
                      <ActionButton onClick={() => openTaskComplete(task)}>Complete</ActionButton>
                      <ActionButton onClick={() => requestDelete("task", task)} tone="danger">
                        Delete
                      </ActionButton>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-6 grid gap-4 xl:hidden">
          {tasks.map((task) => (
            <article key={task.id} className="rounded-3xl border border-ink-200 bg-white p-5 shadow-soft">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-lg font-semibold text-ink-900">{task.title}</p>
                  <p className="mt-1 text-sm text-ink-500">{task.vehicleName || "Vehicle"}</p>
                </div>
                <TaskStatusBadge status={task.status} />
              </div>
              <div className="mt-4 grid gap-3 text-sm text-ink-600">
                <InfoLine label="Priority" value={formatEnumLabel(task.priority)} />
                <InfoLine label="Due Date" value={renderTaskDue(task)} />
                <InfoLine label="Definition" value={task.maintenanceDefinitionName} />
              </div>
              <div className="mt-4 flex gap-2">
                <ActionButton onClick={() => openTaskComplete(task)}>Complete</ActionButton>
                <ActionButton onClick={() => requestDelete("task", task)} tone="danger">
                  Delete
                </ActionButton>
              </div>
            </article>
          ))}
        </div>

        {tasks.length === 0 ? <EmptyState text="No maintenance tasks were returned." /> : null}
      </section>

      <Modal open={Boolean(modal)} onClose={closeModal}>
        {modal?.type === "definition" ? (
          <form className="space-y-6" onSubmit={handleDefinitionSubmit}>
            <ModalHeader
              eyebrow={modal.mode === "edit" ? "Edit Definition" : "Create Definition"}
              title={modal.mode === "edit" ? "Update maintenance definition" : "Add maintenance definition"}
              description="These records drive schedules and task planning."
              onClose={closeModal}
            />

            <div className="grid gap-4 sm:grid-cols-2">
              <TextField
                label="Name"
                value={definitionForm.name}
                onChange={(value) => updateDefinitionForm("name", value)}
                required
              />
              <TextField
                label="Category"
                value={definitionForm.category}
                onChange={(value) => updateDefinitionForm("category", value)}
              />
              <TextField
                label="Applicable Asset Type"
                value={definitionForm.applicableAssetType}
                onChange={(value) => updateDefinitionForm("applicableAssetType", value)}
              />
              <label className="block sm:col-span-2">
                <span className="mb-2 block text-sm font-medium text-ink-700">Active</span>
                <select
                  value={definitionForm.active ? "true" : "false"}
                  onChange={(event) => updateDefinitionForm("active", event.target.value === "true")}
                  className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
                >
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </select>
              </label>
              <TextAreaField
                label="Description"
                value={definitionForm.description}
                onChange={(value) => updateDefinitionForm("description", value)}
                className="sm:col-span-2"
              />
            </div>

            <ModalFooter
              error={formError}
              isSaving={saving}
              saveLabel={modal.mode === "edit" ? "Save Changes" : "Create Definition"}
            />
          </form>
        ) : null}

        {modal?.type === "schedule" ? (
          <form className="space-y-6" onSubmit={handleScheduleSubmit}>
            <ModalHeader
              eyebrow="Create Schedule"
              title="Add maintenance schedule"
              description="Link a vehicle and a maintenance definition."
              onClose={closeModal}
            />

            <div className="grid gap-4 sm:grid-cols-2">
              <SelectField
                label="Vehicle"
                value={scheduleForm.vehicleId}
                onChange={(value) => updateScheduleForm("vehicleId", value)}
                options={vehicleOptions}
                placeholder="Choose a vehicle"
                required
              />
              <SelectField
                label="Maintenance Definition"
                value={scheduleForm.maintenanceDefinitionId}
                onChange={(value) => updateScheduleForm("maintenanceDefinitionId", value)}
                options={definitionOptions}
                placeholder="Choose a definition"
                required
              />
              <SelectField
                label="Trigger Type"
                value={scheduleForm.triggerType}
                onChange={(value) => updateScheduleForm("triggerType", value)}
                options={maintenanceTriggerTypes}
                required
              />
              <SelectField
                label="Time Interval Unit"
                value={scheduleForm.intervalTimeUnit}
                onChange={(value) => updateScheduleForm("intervalTimeUnit", value)}
                options={timeIntervalUnits}
                helperText="Used when a time-based interval is supplied."
              />
              <TextField
                label="Hour Interval"
                type="number"
                value={scheduleForm.intervalHour}
                onChange={(value) => updateScheduleForm("intervalHour", value)}
              />
              <TextField
                label="Distance Interval"
                type="number"
                value={scheduleForm.intervalDistance}
                onChange={(value) => updateScheduleForm("intervalDistance", value)}
              />
              <TextField
                label="Time Interval Value"
                type="number"
                value={scheduleForm.intervalTimeValue}
                onChange={(value) => updateScheduleForm("intervalTimeValue", value)}
              />
              <label className="block">
                <span className="mb-2 block text-sm font-medium text-ink-700">Active</span>
                <select
                  value={scheduleForm.active ? "true" : "false"}
                  onChange={(event) => updateScheduleForm("active", event.target.value === "true")}
                  className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
                >
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </select>
              </label>
            </div>

            <ModalFooter error={formError} isSaving={saving} saveLabel="Create Schedule" />
          </form>
        ) : null}

        {modal?.type === "task" ? (
          <form className="space-y-6" onSubmit={handleTaskSubmit}>
            <ModalHeader
              eyebrow="Create Task"
              title="Add maintenance task"
              description="Tasks can be linked to a schedule or a definition."
              onClose={closeModal}
            />

            <div className="grid gap-4 sm:grid-cols-2">
              <SelectField
                label="Vehicle"
                value={taskForm.vehicleId}
                onChange={(value) => updateTaskForm("vehicleId", value)}
                options={vehicleOptions}
                placeholder="Choose a vehicle"
                required
              />
              <SelectField
                label="Maintenance Schedule"
                value={taskForm.maintenanceScheduleId}
                onChange={(value) => updateTaskForm("maintenanceScheduleId", value)}
                options={scheduleOptions}
                placeholder="Optional"
              />
              <SelectField
                label="Maintenance Definition"
                value={taskForm.maintenanceDefinitionId}
                onChange={(value) => updateTaskForm("maintenanceDefinitionId", value)}
                options={definitionOptions}
                placeholder="Optional"
              />
              <SelectField
                label="Priority"
                value={taskForm.priority}
                onChange={(value) => updateTaskForm("priority", value)}
                options={maintenancePriorities}
                required
              />
              <TextField
                label="Title"
                value={taskForm.title}
                onChange={(value) => updateTaskForm("title", value)}
                required
                className="sm:col-span-2"
              />
              <TextAreaField
                label="Description"
                value={taskForm.description}
                onChange={(value) => updateTaskForm("description", value)}
                className="sm:col-span-2"
              />
              <TextField
                label="Planned Date"
                type="date"
                value={taskForm.plannedDate}
                onChange={(value) => updateTaskForm("plannedDate", value)}
              />
              <TextField
                label="Due Date"
                type="date"
                value={taskForm.dueDate}
                onChange={(value) => updateTaskForm("dueDate", value)}
              />
              <TextField
                label="Due Hour Meter"
                type="number"
                value={taskForm.dueHourMeter}
                onChange={(value) => updateTaskForm("dueHourMeter", value)}
              />
              <TextField
                label="Due Distance Reading"
                type="number"
                value={taskForm.dueDistanceReading}
                onChange={(value) => updateTaskForm("dueDistanceReading", value)}
              />
              <TextAreaField
                label="Notes"
                value={taskForm.notes}
                onChange={(value) => updateTaskForm("notes", value)}
                className="sm:col-span-2"
              />
            </div>

            <ModalFooter error={formError} isSaving={saving} saveLabel="Create Task" />
          </form>
        ) : null}

        {modal?.type === "complete-task" ? (
          <form className="space-y-6" onSubmit={handleCompleteSubmit}>
            <ModalHeader
              eyebrow="Complete Task"
              title={modal.item?.title || "Mark task complete"}
              description="Store completion details for the selected task."
              onClose={closeModal}
            />

            <div className="grid gap-4 sm:grid-cols-2">
              <TextField
                label="Completed Date"
                type="date"
                value={completeForm.completedDate}
                onChange={(value) => updateCompleteForm("completedDate", value)}
              />
              <TextField
                label="Completed Hour Meter"
                type="number"
                value={completeForm.completedHourMeter}
                onChange={(value) => updateCompleteForm("completedHourMeter", value)}
              />
              <TextField
                label="Completed Distance Reading"
                type="number"
                value={completeForm.completedDistanceReading}
                onChange={(value) => updateCompleteForm("completedDistanceReading", value)}
              />
              <TextAreaField
                label="Notes"
                value={completeForm.notes}
                onChange={(value) => updateCompleteForm("notes", value)}
                className="sm:col-span-2"
              />
            </div>

            <ModalFooter error={formError} isSaving={saving} saveLabel="Complete Task" />
          </form>
        ) : null}
      </Modal>

      {deleteTarget ? (
        <Modal open onClose={closeModal}>
          <div className="space-y-6">
            <ModalHeader
              eyebrow="Delete Item"
              title="Confirm deletion"
              description={`This will remove the selected ${deleteTarget.entityType}.`}
              onClose={closeModal}
            />

            <div className="rounded-3xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-900">
              <div className="font-semibold">{renderDeleteLabel(deleteTarget)}</div>
              <p className="mt-2">This action cannot be undone.</p>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={closeModal}
                className="rounded-2xl border border-ink-200 px-4 py-3 text-sm font-semibold text-ink-700 transition hover:bg-ink-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirmDelete}
                disabled={saving}
                className="rounded-2xl bg-rose-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {saving ? "Deleting..." : "Delete"}
              </button>
            </div>

            {formError ? <p className="text-sm text-rose-600">{formError}</p> : null}
          </div>
        </Modal>
      ) : null}
    </div>
  );
}

function Modal({ open, onClose, children }) {
  if (!open) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-end bg-ink-900/55 p-0 sm:items-center sm:justify-center sm:p-6"
      onClick={onClose}
    >
      <div
        className="max-h-[92vh] w-full overflow-y-auto rounded-t-[2rem] bg-white p-6 shadow-soft sm:max-w-4xl sm:rounded-[2rem] sm:p-8"
        onClick={(event) => event.stopPropagation()}
      >
        {children}
      </div>
    </div>
  );
}

function ModalHeader({ eyebrow, title, description, onClose }) {
  return (
    <div className="flex items-start justify-between gap-4">
      <div>
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">{eyebrow}</p>
        <h3 className="mt-2 text-2xl font-semibold text-ink-900">{title}</h3>
        <p className="mt-2 text-sm text-ink-500">{description}</p>
      </div>
      <button
        type="button"
        onClick={onClose}
        className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
      >
        Close
      </button>
    </div>
  );
}

function ModalFooter({ error, isSaving, saveLabel }) {
  return (
    <div className="space-y-3">
      {error ? <p className="text-sm text-rose-600">{error}</p> : null}
      <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
        <button
          type="submit"
          disabled={isSaving}
          className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSaving ? "Saving..." : saveLabel}
        </button>
      </div>
    </div>
  );
}

function TextField({ label, value, onChange, type = "text", required = false, className = "", helperText }) {
  return (
    <label className={`block ${className}`}>
      <span className="mb-2 block text-sm font-medium text-ink-700">
        {label}
        {required ? " *" : ""}
      </span>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition placeholder:text-ink-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
      />
      {helperText ? <p className="mt-2 text-xs text-ink-500">{helperText}</p> : null}
    </label>
  );
}

function TextAreaField({ label, value, onChange, className = "" }) {
  return (
    <label className={`block ${className}`}>
      <span className="mb-2 block text-sm font-medium text-ink-700">{label}</span>
      <textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        rows={4}
        className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition placeholder:text-ink-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
      />
    </label>
  );
}

function SelectField({
  label,
  value,
  onChange,
  options = [],
  placeholder = "Select an option",
  required = false,
  helperText,
}) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-ink-700">
        {label}
        {required ? " *" : ""}
      </span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
      >
        {!required ? <option value="">{placeholder}</option> : null}
        {Array.isArray(options) && options.length > 0 && typeof options[0] === "string"
          ? options.map((option) => (
              <option key={option} value={option}>
                {formatEnumLabel(option)}
              </option>
            ))
          : options.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
      </select>
      {helperText ? <p className="mt-2 text-xs text-ink-500">{helperText}</p> : null}
    </label>
  );
}

function StatPill({ label, value }) {
  return (
    <div className="rounded-2xl bg-ink-50 px-4 py-3">
      <p className="text-xs uppercase tracking-[0.18em] text-ink-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-ink-900">{value}</p>
    </div>
  );
}

function SummaryCard({ label, value }) {
  return (
    <div className="rounded-3xl border border-ink-200 bg-white p-5 shadow-soft">
      <p className="text-sm font-medium text-ink-500">{label}</p>
      <p className="mt-3 text-3xl font-semibold text-ink-900">{value}</p>
    </div>
  );
}

function StatusBadge({ active }) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        active ? "bg-brand-50 text-brand-700" : "bg-ink-100 text-ink-600"
      }`}
    >
      {active ? "ACTIVE" : "INACTIVE"}
    </span>
  );
}

function ScheduleStatusBadge({ status, active }) {
  const colorClass = {
    ON_TRACK: "bg-brand-50 text-brand-700",
    UPCOMING: "bg-amber-50 text-amber-700",
    OVERDUE: "bg-rose-50 text-rose-700",
  }[status];

  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${colorClass || "bg-ink-100 text-ink-700"}`}>
      {active === false ? "INACTIVE" : status ? formatEnumLabel(status) : "UNKNOWN"}
    </span>
  );
}

function TaskStatusBadge({ status }) {
  const colorClass = {
    PLANNED: "bg-sky-50 text-sky-700",
    IN_PROGRESS: "bg-amber-50 text-amber-700",
    COMPLETED: "bg-brand-50 text-brand-700",
    CANCELLED: "bg-rose-50 text-rose-700",
  }[status];

  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${colorClass || "bg-ink-100 text-ink-700"}`}>
      {status ? formatEnumLabel(status) : "UNKNOWN"}
    </span>
  );
}

function PriorityBadge({ priority }) {
  const colorClass = {
    LOW: "bg-emerald-50 text-emerald-700",
    MEDIUM: "bg-sky-50 text-sky-700",
    HIGH: "bg-amber-50 text-amber-700",
    CRITICAL: "bg-rose-50 text-rose-700",
  }[priority];

  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${colorClass || "bg-ink-100 text-ink-700"}`}>
      {priority ? formatEnumLabel(priority) : "UNKNOWN"}
    </span>
  );
}

function ActionButton({ children, onClick, tone = "default" }) {
  const baseClass =
    tone === "danger"
      ? "border-rose-200 text-rose-700 hover:bg-rose-50"
      : "border-ink-200 text-ink-700 hover:bg-ink-50";

  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-2xl border px-3 py-2 text-xs font-semibold transition ${baseClass}`}
    >
      {children}
    </button>
  );
}

function InfoLine({ label, value }) {
  return (
    <div className="rounded-2xl bg-ink-50 px-4 py-4">
      <p className="text-xs uppercase tracking-[0.16em] text-ink-400">{label}</p>
      <p className="mt-2 text-sm font-semibold text-ink-900">{value || "-"}</p>
    </div>
  );
}

function EmptyState({ text }) {
  return (
    <div className="mt-6 rounded-3xl border border-dashed border-ink-200 bg-ink-50/70 p-8 text-center text-sm text-ink-500">
      {text}
    </div>
  );
}

function renderScheduleIntervals(schedule) {
  const parts = [];

  if (schedule.intervalHour != null) {
    parts.push(`${schedule.intervalHour} hr`);
  }
  if (schedule.intervalDistance != null) {
    parts.push(`${schedule.intervalDistance} dist`);
  }
  if (schedule.intervalTimeValue != null) {
    parts.push(`${schedule.intervalTimeValue} ${formatEnumLabel(schedule.intervalTimeUnit)}`);
  }

  return parts.length > 0 ? parts.join(" · ") : "-";
}

function renderScheduleDue(schedule) {
  const parts = [];

  if (schedule.nextDueDate) {
    parts.push(formatDate(schedule.nextDueDate));
  }
  if (schedule.nextDueHourMeter != null) {
    parts.push(`${schedule.nextDueHourMeter} hm`);
  }
  if (schedule.nextDueDistanceReading != null) {
    parts.push(`${schedule.nextDueDistanceReading} km`);
  }

  return parts.length > 0 ? parts.join(" · ") : "-";
}

function renderTaskDue(task) {
  const parts = [];

  if (task.dueHourMeter != null) {
    parts.push(`${task.dueHourMeter} hm`);
  }
  if (task.dueDistanceReading != null) {
    parts.push(`${task.dueDistanceReading} dist`);
  }
  if (task.completedDate) {
    parts.push(`completed ${formatDate(task.completedDate)}`);
  }

  return parts.length > 0 ? parts.join(" · ") : "-";
}

function renderDeleteLabel(deleteTarget) {
  const { entityType, item } = deleteTarget;

  if (entityType === "definition") {
    return item?.name || "Selected definition";
  }
  if (entityType === "schedule") {
    return [item?.vehicleName, item?.maintenanceDefinitionName].filter(Boolean).join(" - ") || "Selected schedule";
  }
  if (entityType === "task") {
    return item?.title || "Selected task";
  }

  return "Selected item";
}

function normalizeOptionalString(value) {
  const trimmed = value?.trim();
  return trimmed ? trimmed : null;
}

function toOptionalNumber(value) {
  return value === "" || value === null || value === undefined ? null : Number(value);
}

export default MaintenancePage;
