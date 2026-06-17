import { useEffect, useState } from "react";
import LoadingState from "../components/LoadingState.jsx";
import {
  assignVehicleGroup,
  createVehicle,
  deleteVehicle,
  getVehicleGroups,
  getVehicles,
  removeVehicleGroup,
  updateVehicle,
  updateVehicleUsage,
} from "../services/vehicleService.js";
import { formatDateTime, formatNumber } from "../utils/formatters.js";

const statusClasses = {
  ACTIVE: "bg-brand-50 text-brand-700",
  IN_MAINTENANCE: "bg-amber-50 text-amber-700",
  OUT_OF_SERVICE: "bg-rose-50 text-rose-700",
};

const vehicleStatuses = ["ACTIVE", "IN_MAINTENANCE", "OUT_OF_SERVICE"];
const maintenanceTriggerTypes = ["TIME", "HOURS", "DISTANCE", "TIME_AND_HOURS", "TIME_AND_DISTANCE"];
const timeIntervalUnits = ["DAY", "WEEK", "MONTH", "YEAR"];
const distanceUnits = ["KILOMETER", "MILE"];

const defaultVehicleForm = {
  name: "",
  plateNumber: "",
  brand: "",
  model: "",
  modelYear: "",
  type: "",
  status: "ACTIVE",
  imageUrl: "",
  vehicleGroupId: "",
  category: "",
  currentHourMeter: "",
  currentDistanceReading: "",
  lastMaintenanceDate: "",
  lastMaintenanceHourMeter: "",
  lastMaintenanceDistanceReading: "",
  maintenanceTriggerType: "DISTANCE",
  hourIntervalValue: "",
  distanceIntervalValue: "",
  timeIntervalValue: "",
  timeIntervalUnit: "MONTH",
  distanceUnit: "KILOMETER",
};

function VehiclesPage() {
  const [vehicles, setVehicles] = useState([]);
  const [vehicleGroups, setVehicleGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [groupFilter, setGroupFilter] = useState("ALL");
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [vehicleForm, setVehicleForm] = useState(defaultVehicleForm);
  const [isEditing, setIsEditing] = useState(false);
  const [editError, setEditError] = useState("");
  const [editForm, setEditForm] = useState(defaultVehicleForm);
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [usageForm, setUsageForm] = useState({
    currentHourMeter: "",
    currentDistanceReading: "",
  });
  const [usageError, setUsageError] = useState("");
  const [isUpdatingUsage, setIsUpdatingUsage] = useState(false);
  const [groupForm, setGroupForm] = useState({ vehicleGroupId: "" });
  const [groupError, setGroupError] = useState("");
  const [isUpdatingGroup, setIsUpdatingGroup] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError("");
        const [vehicleResponse, groupResponse] = await Promise.all([
          getVehicles(),
          getVehicleGroups(),
        ]);
        setVehicles(vehicleResponse);
        setVehicleGroups(groupResponse);
      } catch (requestError) {
        setError(requestError.userMessage || "Vehicles could not be loaded.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  useEffect(() => {
    if (!selectedVehicle && !isCreateOpen && !isEditing) {
      return undefined;
    }

    function handleKeyDown(event) {
      if (event.key === "Escape") {
        setSelectedVehicle(null);
        setIsCreateOpen(false);
        setIsEditing(false);
        setCreateError("");
        setEditError("");
        setUsageError("");
        setGroupError("");
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [selectedVehicle, isCreateOpen, isEditing]);

  useEffect(() => {
    if (!selectedVehicle) {
      setIsEditing(false);
      return;
    }

    setUsageForm({
      currentHourMeter:
        selectedVehicle.currentHourMeter !== null && selectedVehicle.currentHourMeter !== undefined
          ? String(selectedVehicle.currentHourMeter)
          : "",
      currentDistanceReading:
        selectedVehicle.currentDistanceReading !== null &&
        selectedVehicle.currentDistanceReading !== undefined
          ? String(selectedVehicle.currentDistanceReading)
          : "",
    });
    setGroupForm({
      vehicleGroupId: selectedVehicle.vehicleGroupId ? String(selectedVehicle.vehicleGroupId) : "",
    });
    setEditForm(buildVehicleFormFromVehicle(selectedVehicle));
    setEditError("");
    setUsageError("");
    setGroupError("");
  }, [selectedVehicle]);

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const availableGroups = Array.from(
    new Set(vehicles.map((vehicle) => vehicle.vehicleGroupName).filter(Boolean)),
  ).sort((left, right) => left.localeCompare(right));

  const filteredVehicles = vehicles.filter((vehicle) => {
    const matchesSearch =
      !normalizedSearch ||
      [
        vehicle.name,
        vehicle.plateNumber,
        vehicle.brand,
        vehicle.model,
        vehicle.type,
        vehicle.vehicleGroupName,
      ]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalizedSearch));

    const matchesStatus = statusFilter === "ALL" || vehicle.status === statusFilter;
    const matchesGroup =
      groupFilter === "ALL" || (vehicle.vehicleGroupName || "Ungrouped") === groupFilter;

    return matchesSearch && matchesStatus && matchesGroup;
  });

  if (loading) {
    return <LoadingState message="Vehicles are loading." />;
  }

  if (error) {
    return (
      <div className="rounded-3xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
        {error}
      </div>
    );
  }

  const triggerConfig = getTriggerConfig(vehicleForm.maintenanceTriggerType);

  async function handleCreateVehicle(event) {
    event.preventDefault();

    try {
      setIsCreating(true);
      setCreateError("");

      const createdVehicle = await createVehicle(buildVehiclePayload(vehicleForm));
      setVehicles((current) => [createdVehicle, ...current]);
      setIsCreateOpen(false);
      setVehicleForm(defaultVehicleForm);
    } catch (requestError) {
      setCreateError(requestError.userMessage || "Vehicle could not be created.");
    } finally {
      setIsCreating(false);
    }
  }

  function handleOpenCreate() {
    setCreateError("");
    setVehicleForm(defaultVehicleForm);
    setIsCreateOpen(true);
  }

  function handleFormChange(field, value) {
    setVehicleForm((current) => ({ ...current, [field]: value }));
  }

  function handleOpenEdit() {
    if (!selectedVehicle) {
      return;
    }

    setEditError("");
    setEditForm(buildVehicleFormFromVehicle(selectedVehicle));
    setIsEditing(true);
  }

  function handleEditFormChange(field, value) {
    setEditForm((current) => ({ ...current, [field]: value }));
  }

  async function handleUpdateVehicle(event) {
    event.preventDefault();

    if (!selectedVehicle) {
      return;
    }

    try {
      setIsSavingEdit(true);
      setEditError("");

      const updatedVehicle = await updateVehicle(selectedVehicle.id, buildVehiclePayload(editForm));
      setVehicles((current) =>
        current.map((vehicle) => (vehicle.id === updatedVehicle.id ? updatedVehicle : vehicle)),
      );
      setSelectedVehicle(updatedVehicle);
      setIsEditing(false);
    } catch (requestError) {
      setEditError(requestError.userMessage || "Vehicle could not be updated.");
    } finally {
      setIsSavingEdit(false);
    }
  }

  async function handleUpdateUsage(event) {
    event.preventDefault();

    if (!selectedVehicle) {
      return;
    }

    if (usageForm.currentHourMeter === "" && usageForm.currentDistanceReading === "") {
      setUsageError("Enter at least one usage value.");
      return;
    }

    try {
      setIsUpdatingUsage(true);
      setUsageError("");

      const updatedVehicle = await updateVehicleUsage(selectedVehicle.id, {
        currentHourMeter: toOptionalNumber(usageForm.currentHourMeter),
        currentDistanceReading: toOptionalNumber(usageForm.currentDistanceReading),
      });
      setVehicles((current) =>
        current.map((vehicle) => (vehicle.id === updatedVehicle.id ? updatedVehicle : vehicle)),
      );
      setSelectedVehicle(updatedVehicle);
    } catch (requestError) {
      setUsageError(requestError.userMessage || "Vehicle usage could not be updated.");
    } finally {
      setIsUpdatingUsage(false);
    }
  }

  async function handleAssignGroup(event) {
    event.preventDefault();

    if (!selectedVehicle || !groupForm.vehicleGroupId) {
      setGroupError("Select a group to assign.");
      return;
    }

    try {
      setIsUpdatingGroup(true);
      setGroupError("");

      const updatedVehicle = await assignVehicleGroup(
        selectedVehicle.id,
        Number(groupForm.vehicleGroupId),
      );
      setVehicles((current) =>
        current.map((vehicle) => (vehicle.id === updatedVehicle.id ? updatedVehicle : vehicle)),
      );
      setSelectedVehicle(updatedVehicle);
    } catch (requestError) {
      setGroupError(requestError.userMessage || "Vehicle group could not be assigned.");
    } finally {
      setIsUpdatingGroup(false);
    }
  }

  async function handleRemoveGroup() {
    if (!selectedVehicle || !selectedVehicle.vehicleGroupId) {
      return;
    }

    try {
      setIsUpdatingGroup(true);
      setGroupError("");

      const updatedVehicle = await removeVehicleGroup(selectedVehicle.id);
      setVehicles((current) =>
        current.map((vehicle) => (vehicle.id === updatedVehicle.id ? updatedVehicle : vehicle)),
      );
      setSelectedVehicle(updatedVehicle);
      setGroupForm({ vehicleGroupId: "" });
    } catch (requestError) {
      setGroupError(requestError.userMessage || "Vehicle group could not be removed.");
    } finally {
      setIsUpdatingGroup(false);
    }
  }

  async function handleDeleteVehicle() {
    if (!selectedVehicle) {
      return;
    }

    const confirmed = window.confirm(
      `Delete "${selectedVehicle.name}"? This action cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setIsDeleting(true);
      await deleteVehicle(selectedVehicle.id);
      setVehicles((current) => current.filter((vehicle) => vehicle.id !== selectedVehicle.id));
      setSelectedVehicle(null);
      setIsEditing(false);
      setEditError("");
      setUsageError("");
      setGroupError("");
    } catch (requestError) {
      setGroupError(requestError.userMessage || "Vehicle could not be deleted.");
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <div className="space-y-6">
      <section className="rounded-[2rem] border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Fleet Registry
            </p>
            <h3 className="mt-2 text-2xl font-semibold text-ink-900">Vehicles</h3>
            <p className="mt-2 text-sm text-ink-500">
              Browse active assets, maintenance state and latest usage values.
            </p>
          </div>
          <div className="flex flex-col gap-3 sm:items-end">
            <div className="rounded-2xl bg-ink-50 px-4 py-3 text-sm text-ink-600">
              Showing: <span className="font-semibold text-ink-900">{filteredVehicles.length}</span>
              <span className="text-ink-400"> / {vehicles.length}</span>
            </div>
            <button
              type="button"
              onClick={handleOpenCreate}
              className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            >
              Add Vehicle
            </button>
          </div>
        </div>

        <div className="mt-6 grid gap-3 lg:grid-cols-[1.4fr_0.8fr_0.8fr]">
          <label className="block">
            <span className="mb-2 block text-sm font-medium text-ink-700">Search</span>
            <input
              type="text"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Name, plate, brand or group"
              className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition placeholder:text-ink-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
            />
          </label>

          <label className="block">
            <span className="mb-2 block text-sm font-medium text-ink-700">Status</span>
            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
            >
              <option value="ALL">All statuses</option>
              {vehicleStatuses.map((status) => (
                <option key={status} value={status}>
                  {toTitleCase(status)}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="mb-2 block text-sm font-medium text-ink-700">Group</span>
            <select
              value={groupFilter}
              onChange={(event) => setGroupFilter(event.target.value)}
              className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
            >
              <option value="ALL">All groups</option>
              <option value="Ungrouped">Ungrouped</option>
              {availableGroups.map((groupName) => (
                <option key={groupName} value={groupName}>
                  {groupName}
                </option>
              ))}
            </select>
          </label>
        </div>
      </section>

      <section className="hidden overflow-hidden rounded-3xl border border-ink-200 bg-white/90 shadow-soft lg:block">
        <table className="min-w-full divide-y divide-ink-200">
          <thead className="bg-ink-50">
            <tr className="text-left text-sm text-ink-500">
              <th className="px-6 py-4 font-medium">Vehicle</th>
              <th className="px-6 py-4 font-medium">Plate</th>
              <th className="px-6 py-4 font-medium">Status</th>
              <th className="px-6 py-4 font-medium">Group</th>
              <th className="px-6 py-4 font-medium">Distance</th>
              <th className="px-6 py-4 font-medium">Updated</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100">
            {filteredVehicles.map((vehicle) => (
              <tr
                key={vehicle.id}
                className="cursor-pointer text-sm text-ink-700 transition hover:bg-ink-50"
                onClick={() => setSelectedVehicle(vehicle)}
              >
                <td className="px-6 py-4">
                  <div>
                    <p className="font-semibold text-ink-900">{vehicle.name}</p>
                    <p className="text-ink-500">
                      {[vehicle.brand, vehicle.model, vehicle.modelYear].filter(Boolean).join(" / ") || "-"}
                    </p>
                  </div>
                </td>
                <td className="px-6 py-4">{vehicle.plateNumber || "-"}</td>
                <td className="px-6 py-4">
                  <StatusBadge status={vehicle.status} />
                </td>
                <td className="px-6 py-4">{vehicle.vehicleGroupName || "-"}</td>
                <td className="px-6 py-4">
                  {formatNumber(vehicle.currentDistanceReading)} {vehicle.distanceUnit || ""}
                </td>
                <td className="px-6 py-4">{formatDateTime(vehicle.updatedAt || vehicle.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="grid gap-4 lg:hidden">
        {filteredVehicles.map((vehicle) => (
          <article
            key={vehicle.id}
            className="cursor-pointer rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft transition hover:border-brand-200"
            onClick={() => setSelectedVehicle(vehicle)}
          >
            <div className="flex items-start justify-between gap-4">
              <div className="min-w-0">
                <p className="truncate text-lg font-semibold text-ink-900">{vehicle.name}</p>
                <p className="mt-1 text-sm text-ink-500">
                  {[vehicle.brand, vehicle.model, vehicle.modelYear].filter(Boolean).join(" / ") || "-"}
                </p>
              </div>
              <StatusBadge status={vehicle.status} />
            </div>

            <div className="mt-5 grid gap-3 sm:grid-cols-2">
              <InfoRow label="Plate" value={vehicle.plateNumber} />
              <InfoRow label="Group" value={vehicle.vehicleGroupName} />
              <InfoRow label="Type" value={vehicle.type} />
              <InfoRow
                label="Distance"
                value={joinParts(formatNumber(vehicle.currentDistanceReading), vehicle.distanceUnit)}
              />
              <InfoRow
                label="Hour Meter"
                value={vehicle.currentHourMeter != null ? formatNumber(vehicle.currentHourMeter) : "-"}
              />
              <InfoRow label="Updated" value={formatDateTime(vehicle.updatedAt || vehicle.createdAt)} />
            </div>
          </article>
        ))}
      </section>

      {filteredVehicles.length === 0 ? (
        <div className="rounded-3xl border border-ink-200 bg-white/90 p-8 text-center text-sm text-ink-500 shadow-soft">
          No vehicles matched the current search and filters.
        </div>
      ) : null}

      <VehicleDetailsModal
        vehicle={selectedVehicle}
        vehicleGroups={vehicleGroups}
        editForm={editForm}
        isEditing={isEditing}
        editError={editError}
        isSavingEdit={isSavingEdit}
        onEdit={() => handleOpenEdit()}
        onEditFormChange={handleEditFormChange}
        onSubmitEdit={handleUpdateVehicle}
        usageForm={usageForm}
        usageError={usageError}
        isUpdatingUsage={isUpdatingUsage}
        onUsageFormChange={(field, value) =>
          setUsageForm((current) => ({ ...current, [field]: value }))
        }
        onSubmitUsage={handleUpdateUsage}
        groupForm={groupForm}
        groupError={groupError}
        isUpdatingGroup={isUpdatingGroup}
        onGroupFormChange={(field, value) =>
          setGroupForm((current) => ({ ...current, [field]: value }))
        }
        onAssignGroup={handleAssignGroup}
        onRemoveGroup={handleRemoveGroup}
        isDeleting={isDeleting}
        onDelete={handleDeleteVehicle}
        onClose={() => setSelectedVehicle(null)}
        onCloseEdit={() => setIsEditing(false)}
      />

      <ModalShell open={isCreateOpen} onClose={() => setIsCreateOpen(false)}>
        <form onSubmit={handleCreateVehicle} className="space-y-6">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
                Vehicle Create
              </p>
              <h3 className="mt-2 text-2xl font-semibold text-ink-900">Add Vehicle</h3>
              <p className="mt-2 text-sm text-ink-500">
                Create a new asset and define its maintenance trigger configuration.
              </p>
            </div>
            <button
              type="button"
              onClick={() => setIsCreateOpen(false)}
              className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
            >
              Close
            </button>
          </div>

          <VehicleFormFields
            form={vehicleForm}
            vehicleGroups={vehicleGroups}
            triggerConfig={triggerConfig}
            onChange={handleFormChange}
          />

          {createError ? (
            <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {createError}
            </div>
          ) : null}

          <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={() => setIsCreateOpen(false)}
              className="rounded-2xl border border-ink-200 px-5 py-3 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isCreating}
              className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isCreating ? "Creating..." : "Create Vehicle"}
            </button>
          </div>
        </form>
      </ModalShell>
    </div>
  );
}

function FormField({ label, value, onChange, type = "text", required = false }) {
  return (
    <label className="block">
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
    </label>
  );
}

function SelectField({ label, value, onChange, options, emptyLabel, required = false }) {
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
        {emptyLabel ? <option value="">{emptyLabel}</option> : null}
        {options.map((option) => {
          const normalizedOption =
            typeof option === "string"
              ? { value: option, label: toTitleCase(option) }
              : option;

          return (
            <option key={normalizedOption.value} value={normalizedOption.value}>
              {normalizedOption.label}
            </option>
          );
        })}
      </select>
    </label>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="rounded-2xl bg-ink-50 px-4 py-4">
      <p className="text-sm text-ink-500">{label}</p>
      <p className="mt-2 text-sm font-semibold text-ink-900">{value || "-"}</p>
    </div>
  );
}

function VehicleFormFields({ form, vehicleGroups, triggerConfig, onChange }) {
  return (
    <>
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField label="Name" value={form.name} onChange={(value) => onChange("name", value)} required />
        <FormField
          label="Plate Number"
          value={form.plateNumber}
          onChange={(value) => onChange("plateNumber", value)}
          required
        />
        <FormField label="Brand" value={form.brand} onChange={(value) => onChange("brand", value)} required />
        <FormField label="Model" value={form.model} onChange={(value) => onChange("model", value)} required />
        <FormField
          label="Model Year"
          type="number"
          value={form.modelYear}
          onChange={(value) => onChange("modelYear", value)}
          required
        />
        <FormField label="Type" value={form.type} onChange={(value) => onChange("type", value)} required />
        <FormField
          label="Category"
          value={form.category}
          onChange={(value) => onChange("category", value)}
          required
        />
        <SelectField
          label="Status"
          value={form.status}
          onChange={(value) => onChange("status", value)}
          options={vehicleStatuses}
          required
        />
        <SelectField
          label="Vehicle Group"
          value={form.vehicleGroupId}
          onChange={(value) => onChange("vehicleGroupId", value)}
          options={vehicleGroups.map((group) => ({ value: String(group.id), label: group.name }))}
          emptyLabel="No group"
        />
        <FormField label="Image URL" value={form.imageUrl} onChange={(value) => onChange("imageUrl", value)} />
      </div>

      <div className="rounded-3xl border border-ink-200 bg-ink-50/70 p-5">
        <h4 className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
          Maintenance Trigger
        </h4>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <SelectField
            label="Trigger Type"
            value={form.maintenanceTriggerType}
            onChange={(value) => onChange("maintenanceTriggerType", value)}
            options={maintenanceTriggerTypes}
            required
          />
          {(triggerConfig.requiresDistance || triggerConfig.optionalDistance) ? (
            <SelectField
              label="Distance Unit"
              value={form.distanceUnit}
              onChange={(value) => onChange("distanceUnit", value)}
              options={distanceUnits}
              required={triggerConfig.requiresDistance}
            />
          ) : null}
          {(triggerConfig.requiresTime || triggerConfig.optionalTime) ? (
            <SelectField
              label="Time Interval Unit"
              value={form.timeIntervalUnit}
              onChange={(value) => onChange("timeIntervalUnit", value)}
              options={timeIntervalUnits}
              required={triggerConfig.requiresTime}
            />
          ) : null}
          {(triggerConfig.requiresHours || triggerConfig.optionalHours) ? (
            <FormField
              label="Current Hour Meter"
              type="number"
              value={form.currentHourMeter}
              onChange={(value) => onChange("currentHourMeter", value)}
              required={triggerConfig.requiresHours}
            />
          ) : null}
          {(triggerConfig.requiresDistance || triggerConfig.optionalDistance) ? (
            <FormField
              label="Current Distance Reading"
              type="number"
              value={form.currentDistanceReading}
              onChange={(value) => onChange("currentDistanceReading", value)}
              required={triggerConfig.requiresDistance}
            />
          ) : null}
          {(triggerConfig.requiresHours || triggerConfig.optionalHours) ? (
            <FormField
              label="Hour Interval Value"
              type="number"
              value={form.hourIntervalValue}
              onChange={(value) => onChange("hourIntervalValue", value)}
              required={triggerConfig.requiresHours}
            />
          ) : null}
          {(triggerConfig.requiresDistance || triggerConfig.optionalDistance) ? (
            <FormField
              label="Distance Interval Value"
              type="number"
              value={form.distanceIntervalValue}
              onChange={(value) => onChange("distanceIntervalValue", value)}
              required={triggerConfig.requiresDistance}
            />
          ) : null}
          {(triggerConfig.requiresTime || triggerConfig.optionalTime) ? (
            <FormField
              label="Time Interval Value"
              type="number"
              value={form.timeIntervalValue}
              onChange={(value) => onChange("timeIntervalValue", value)}
              required={triggerConfig.requiresTime}
            />
          ) : null}
        </div>
      </div>

      <div className="rounded-3xl border border-ink-200 bg-ink-50/70 p-5">
        <h4 className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
          Optional History
        </h4>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <FormField
            label="Last Maintenance Date"
            type="date"
            value={form.lastMaintenanceDate}
            onChange={(value) => onChange("lastMaintenanceDate", value)}
          />
          <FormField
            label="Last Maintenance Hour Meter"
            type="number"
            value={form.lastMaintenanceHourMeter}
            onChange={(value) => onChange("lastMaintenanceHourMeter", value)}
          />
          <FormField
            label="Last Maintenance Distance"
            type="number"
            value={form.lastMaintenanceDistanceReading}
            onChange={(value) => onChange("lastMaintenanceDistanceReading", value)}
          />
        </div>
      </div>
    </>
  );
}

function StatusBadge({ status }) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
        statusClasses[status] || "bg-ink-100 text-ink-700"
      }`}
    >
      {status ? status.replaceAll("_", " ") : "UNKNOWN"}
    </span>
  );
}

function VehicleDetailsModal({
  vehicle,
  vehicleGroups,
  editForm,
  isEditing,
  editError,
  isSavingEdit,
  onEdit,
  onEditFormChange,
  onSubmitEdit,
  usageForm,
  usageError,
  isUpdatingUsage,
  onUsageFormChange,
  onSubmitUsage,
  groupForm,
  groupError,
  isUpdatingGroup,
  onGroupFormChange,
  onAssignGroup,
  onRemoveGroup,
  isDeleting,
  onDelete,
  onClose,
  onCloseEdit,
}) {
  if (!vehicle) {
    return null;
  }

  return (
    <ModalShell open={Boolean(vehicle)} onClose={onClose}>
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
            Vehicle Details
          </p>
          <h3 className="mt-2 text-2xl font-semibold text-ink-900">{vehicle.name}</h3>
          <p className="mt-2 text-sm text-ink-500">
            {[vehicle.brand, vehicle.model, vehicle.modelYear].filter(Boolean).join(" / ") || "-"}
          </p>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
        >
          Close
        </button>
      </div>

      <div className="mt-6 grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <section className="space-y-4">
          <div className="rounded-3xl bg-ink-900 p-6 text-white">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm uppercase tracking-[0.25em] text-brand-100">Current Status</p>
                <div className="mt-3">
                  <StatusBadge status={vehicle.status} />
                </div>
              </div>
              <div className="text-right text-sm text-slate-300">
                <p>Plate</p>
                <p className="mt-2 text-lg font-semibold text-white">{vehicle.plateNumber || "-"}</p>
              </div>
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <InfoRow label="Type" value={vehicle.type} />
            <InfoRow label="Category" value={vehicle.category} />
            <InfoRow label="Group" value={vehicle.vehicleGroupName} />
            <InfoRow label="Trigger Type" value={vehicle.maintenanceTriggerType} />
            <InfoRow
              label="Current Distance"
              value={joinParts(formatNumber(vehicle.currentDistanceReading), vehicle.distanceUnit)}
            />
            <InfoRow
              label="Current Hour Meter"
              value={vehicle.currentHourMeter != null ? formatNumber(vehicle.currentHourMeter) : "-"}
            />
            <InfoRow label="Last Maintenance Date" value={vehicle.lastMaintenanceDate || "-"} />
            <InfoRow
              label="Last Distance"
              value={
                vehicle.lastMaintenanceDistanceReading != null
                  ? formatNumber(vehicle.lastMaintenanceDistanceReading)
                  : "-"
              }
            />
            <InfoRow
              label="Last Hour Meter"
              value={
                vehicle.lastMaintenanceHourMeter != null
                  ? formatNumber(vehicle.lastMaintenanceHourMeter)
                  : "-"
              }
            />
            <InfoRow label="Created" value={formatDateTime(vehicle.createdAt)} />
            <InfoRow label="Updated" value={formatDateTime(vehicle.updatedAt || vehicle.createdAt)} />
          </div>
        </section>

        <section className="space-y-4">
          <div className="flex flex-col gap-3 sm:flex-row">
            <button
              type="button"
              onClick={onEdit}
              className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            >
              Edit Vehicle
            </button>
            <button
              type="button"
              onClick={onDelete}
              disabled={isDeleting}
              className="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-3 text-sm font-semibold text-rose-700 transition hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isDeleting ? "Deleting..." : "Delete Vehicle"}
            </button>
          </div>

          <details open={isEditing} className="rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft">
            <summary className="cursor-pointer list-none text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
              Edit Vehicle
            </summary>
            <form onSubmit={onSubmitEdit} className="mt-4 space-y-4">
              <VehicleFormFields
                form={editForm}
                vehicleGroups={vehicleGroups}
                triggerConfig={getTriggerConfig(editForm.maintenanceTriggerType)}
                onChange={onEditFormChange}
              />
              {editError ? (
                <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                  {editError}
                </div>
              ) : null}
              <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
                <button
                  type="button"
                  onClick={onCloseEdit}
                  className="rounded-2xl border border-ink-200 px-5 py-3 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
                >
                  Close Edit
                </button>
                <button
                  type="submit"
                  disabled={isSavingEdit}
                  className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {isSavingEdit ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </details>

          <form
            onSubmit={onSubmitUsage}
            className="rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft"
          >
            <h4 className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
              Update Usage
            </h4>
            <div className="mt-4 grid gap-4 sm:grid-cols-2">
              <FormField
                label="Current Hour Meter"
                type="number"
                value={usageForm.currentHourMeter}
                onChange={(value) => onUsageFormChange("currentHourMeter", value)}
              />
              <FormField
                label="Current Distance Reading"
                type="number"
                value={usageForm.currentDistanceReading}
                onChange={(value) => onUsageFormChange("currentDistanceReading", value)}
              />
            </div>
            {usageError ? (
              <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                {usageError}
              </div>
            ) : null}
            <div className="mt-4 flex justify-end">
              <button
                type="submit"
                disabled={isUpdatingUsage}
                className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isUpdatingUsage ? "Updating..." : "Update Usage"}
              </button>
            </div>
          </form>

          <form
            onSubmit={onAssignGroup}
            className="rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft"
          >
            <h4 className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
              Group Assignment
            </h4>
            <div className="mt-4 space-y-4">
              <SelectField
                label="Vehicle Group"
                value={groupForm.vehicleGroupId}
                onChange={(value) => onGroupFormChange("vehicleGroupId", value)}
                options={vehicleGroups.map((group) => ({ value: String(group.id), label: group.name }))}
                emptyLabel="Select a group"
              />
              {groupError ? (
                <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                  {groupError}
                </div>
              ) : null}
              <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
                <button
                  type="button"
                  onClick={onRemoveGroup}
                  disabled={isUpdatingGroup || !vehicle.vehicleGroupId}
                  className="rounded-2xl border border-ink-200 px-5 py-3 text-sm font-medium text-ink-700 transition hover:bg-ink-50 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {isUpdatingGroup ? "Updating..." : "Remove Group"}
                </button>
                <button
                  type="submit"
                  disabled={isUpdatingGroup || !groupForm.vehicleGroupId}
                  className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {isUpdatingGroup ? "Updating..." : "Assign Group"}
                </button>
              </div>
            </div>
          </form>

          <DetailPanel
            title="Maintenance Intervals"
            items={[
              { label: "Time Interval", value: joinParts(vehicle.timeIntervalValue, vehicle.timeIntervalUnit) },
              { label: "Distance Interval", value: joinParts(vehicle.distanceIntervalValue, vehicle.distanceUnit) },
              { label: "Hour Interval", value: vehicle.hourIntervalValue },
            ]}
          />
          <DetailPanel
            title="Identity"
            items={[
              { label: "Vehicle ID", value: vehicle.id },
              { label: "Group ID", value: vehicle.vehicleGroupId },
              { label: "Image URL", value: vehicle.imageUrl || "-" },
            ]}
          />
        </section>
      </div>
    </ModalShell>
  );
}

function ModalShell({ open, onClose, children }) {
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

function DetailPanel({ title, items }) {
  return (
    <div className="rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft">
      <h4 className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">{title}</h4>
      <div className="mt-4 space-y-3">
        {items.map((item) => (
          <div key={item.label} className="rounded-2xl bg-ink-50 px-4 py-4">
            <p className="text-sm text-ink-500">{item.label}</p>
            <p className="mt-2 text-sm font-semibold text-ink-900">{item.value || "-"}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

function buildVehiclePayload(form) {
  const triggerConfig = getTriggerConfig(form.maintenanceTriggerType);

  return {
    name: form.name.trim(),
    plateNumber: form.plateNumber.trim(),
    brand: form.brand.trim(),
    model: form.model.trim(),
    modelYear: toNumber(form.modelYear),
    type: form.type.trim(),
    status: form.status,
    imageUrl: normalizeString(form.imageUrl),
    vehicleGroupId: toOptionalNumber(form.vehicleGroupId),
    category: form.category.trim(),
    currentHourMeter: triggerConfig.requiresHours || form.currentHourMeter !== "" ? toOptionalNumber(form.currentHourMeter) : null,
    currentDistanceReading:
      triggerConfig.requiresDistance || form.currentDistanceReading !== ""
        ? toOptionalNumber(form.currentDistanceReading)
        : null,
    lastMaintenanceDate: normalizeString(form.lastMaintenanceDate),
    lastMaintenanceHourMeter: toOptionalNumber(form.lastMaintenanceHourMeter),
    lastMaintenanceDistanceReading: toOptionalNumber(form.lastMaintenanceDistanceReading),
    maintenanceTriggerType: form.maintenanceTriggerType,
    hourIntervalValue:
      triggerConfig.requiresHours || form.hourIntervalValue !== "" ? toOptionalNumber(form.hourIntervalValue) : null,
    distanceIntervalValue:
      triggerConfig.requiresDistance || form.distanceIntervalValue !== ""
        ? toOptionalNumber(form.distanceIntervalValue)
        : null,
    timeIntervalValue:
      triggerConfig.requiresTime || form.timeIntervalValue !== "" ? toOptionalNumber(form.timeIntervalValue) : null,
    timeIntervalUnit: triggerConfig.requiresTime || form.timeIntervalValue !== "" ? form.timeIntervalUnit : null,
    distanceUnit:
      triggerConfig.requiresDistance || form.currentDistanceReading !== "" || form.distanceIntervalValue !== ""
        ? form.distanceUnit
        : null,
  };
}

function buildVehicleFormFromVehicle(vehicle) {
  return {
    name: vehicle.name || "",
    plateNumber: vehicle.plateNumber || "",
    brand: vehicle.brand || "",
    model: vehicle.model || "",
    modelYear: vehicle.modelYear != null ? String(vehicle.modelYear) : "",
    type: vehicle.type || "",
    status: vehicle.status || "ACTIVE",
    imageUrl: vehicle.imageUrl || "",
    vehicleGroupId: vehicle.vehicleGroupId != null ? String(vehicle.vehicleGroupId) : "",
    category: vehicle.category || "",
    currentHourMeter: vehicle.currentHourMeter != null ? String(vehicle.currentHourMeter) : "",
    currentDistanceReading:
      vehicle.currentDistanceReading != null ? String(vehicle.currentDistanceReading) : "",
    lastMaintenanceDate: vehicle.lastMaintenanceDate || "",
    lastMaintenanceHourMeter:
      vehicle.lastMaintenanceHourMeter != null ? String(vehicle.lastMaintenanceHourMeter) : "",
    lastMaintenanceDistanceReading:
      vehicle.lastMaintenanceDistanceReading != null
        ? String(vehicle.lastMaintenanceDistanceReading)
        : "",
    maintenanceTriggerType: vehicle.maintenanceTriggerType || "DISTANCE",
    hourIntervalValue: vehicle.hourIntervalValue != null ? String(vehicle.hourIntervalValue) : "",
    distanceIntervalValue:
      vehicle.distanceIntervalValue != null ? String(vehicle.distanceIntervalValue) : "",
    timeIntervalValue: vehicle.timeIntervalValue != null ? String(vehicle.timeIntervalValue) : "",
    timeIntervalUnit: vehicle.timeIntervalUnit || "MONTH",
    distanceUnit: vehicle.distanceUnit || "KILOMETER",
  };
}

function getTriggerConfig(triggerType) {
  return {
    requiresHours: triggerType === "HOURS" || triggerType === "TIME_AND_HOURS",
    requiresDistance: triggerType === "DISTANCE" || triggerType === "TIME_AND_DISTANCE",
    requiresTime:
      triggerType === "TIME" || triggerType === "TIME_AND_HOURS" || triggerType === "TIME_AND_DISTANCE",
    optionalHours: false,
    optionalDistance: false,
    optionalTime: false,
  };
}

function normalizeString(value) {
  return value?.trim() ? value.trim() : null;
}

function toNumber(value) {
  return Number(value);
}

function toOptionalNumber(value) {
  return value === "" || value === null || value === undefined ? null : Number(value);
}

function joinParts(...parts) {
  const filteredParts = parts.filter((part) => part !== null && part !== undefined && part !== "");

  if (filteredParts.length === 0) {
    return "-";
  }

  return filteredParts.join(" ");
}

function toTitleCase(value) {
  return value.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
}

export default VehiclesPage;
