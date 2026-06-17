import { useEffect, useMemo, useState } from "react";
import LoadingState from "../components/LoadingState.jsx";
import { getParts } from "../services/inventoryService.js";
import {
  addWorkOrderExpense,
  addWorkOrderPart,
  cancelWorkOrder,
  completeWorkOrder,
  createWorkOrder,
  getWorkOrderCostSummary,
  getWorkOrderExpenses,
  getWorkOrderParts,
  getWorkOrders,
  startWorkOrder,
} from "../services/workOrderService.js";
import { getVehicles } from "../services/vehicleService.js";
import {
  formatCurrency,
  formatDateTime,
  formatEnumLabel,
  formatNumber,
} from "../utils/formatters.js";

const workOrderStatuses = ["OPEN", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"];
const costTypes = ["LABOR", "EXTERNAL_SERVICE", "MISC", "PART"];

const statusClasses = {
  OPEN: "bg-sky-50 text-sky-700",
  ASSIGNED: "bg-amber-50 text-amber-700",
  IN_PROGRESS: "bg-violet-50 text-violet-700",
  COMPLETED: "bg-brand-50 text-brand-700",
  CANCELLED: "bg-rose-50 text-rose-700",
};

const defaultCreateForm = {
  vehicleId: "",
  title: "",
  description: "",
  maintenanceTaskId: "",
  assignedUserId: "",
};

const defaultCompleteForm = {
  completionNotes: "",
  actualCost: "",
  laborHours: "",
};

const defaultExpenseForm = {
  costType: "LABOR",
  description: "",
  amount: "",
};

const defaultPartForm = {
  partId: "",
  quantityUsed: "",
  unitCost: "",
  notes: "",
};

function WorkOrdersPage() {
  const [workOrders, setWorkOrders] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [parts, setParts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [createOpen, setCreateOpen] = useState(false);
  const [createError, setCreateError] = useState("");
  const [creating, setCreating] = useState(false);
  const [createForm, setCreateForm] = useState(defaultCreateForm);
  const [selectedWorkOrder, setSelectedWorkOrder] = useState(null);
  const [partsUsage, setPartsUsage] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [costSummary, setCostSummary] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState("");
  const [completeForm, setCompleteForm] = useState(defaultCompleteForm);
  const [expenseForm, setExpenseForm] = useState(defaultExpenseForm);
  const [partForm, setPartForm] = useState(defaultPartForm);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError("");
        const [workOrderResponse, vehicleResponse, partResponse] = await Promise.all([
          getWorkOrders(),
          getVehicles(),
          getParts(),
        ]);
        setWorkOrders(workOrderResponse);
        setVehicles(vehicleResponse);
        setParts(partResponse);
      } catch (requestError) {
        setError(requestError.userMessage || "Work orders could not be loaded.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  useEffect(() => {
    if (!selectedWorkOrder && !createOpen) {
      return undefined;
    }

    function handleKeyDown(event) {
      if (event.key === "Escape") {
        setSelectedWorkOrder(null);
        setCreateOpen(false);
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [selectedWorkOrder, createOpen]);

  const filteredWorkOrders = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    return workOrders.filter((workOrder) => {
      const matchesSearch =
        !normalizedSearch ||
        [workOrder.title, workOrder.vehicleName, workOrder.assignedUsername, workOrder.description]
          .filter(Boolean)
          .some((value) => value.toLowerCase().includes(normalizedSearch));

      const matchesStatus = statusFilter === "ALL" || workOrder.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [searchTerm, statusFilter, workOrders]);

  async function openDetails(workOrder) {
    try {
      setSelectedWorkOrder(workOrder);
      setDetailLoading(true);
      setDetailError("");
      setCompleteForm(defaultCompleteForm);
      setExpenseForm(defaultExpenseForm);
      setPartForm(defaultPartForm);

      const [partResponse, expenseResponse, summaryResponse] = await Promise.all([
        getWorkOrderParts(workOrder.id),
        getWorkOrderExpenses(workOrder.id),
        getWorkOrderCostSummary(workOrder.id),
      ]);

      setPartsUsage(partResponse);
      setExpenses(expenseResponse);
      setCostSummary(summaryResponse);
    } catch (requestError) {
      setDetailError(requestError.userMessage || "Work order details could not be loaded.");
    } finally {
      setDetailLoading(false);
    }
  }

  async function reloadWorkOrders(nextSelectedId = null) {
    const list = await getWorkOrders();
    setWorkOrders(list);

    if (nextSelectedId) {
      const nextSelected = list.find((item) => item.id === nextSelectedId) || null;
      setSelectedWorkOrder(nextSelected);
      return nextSelected;
    }

    return null;
  }

  async function refreshSelectedDetails(workOrderId) {
    const [partResponse, expenseResponse, summaryResponse] = await Promise.all([
      getWorkOrderParts(workOrderId),
      getWorkOrderExpenses(workOrderId),
      getWorkOrderCostSummary(workOrderId),
    ]);

    setPartsUsage(partResponse);
    setExpenses(expenseResponse);
    setCostSummary(summaryResponse);
  }

  async function handleCreate(event) {
    event.preventDefault();

    try {
      setCreating(true);
      setCreateError("");
      const created = await createWorkOrder({
        vehicleId: Number(createForm.vehicleId),
        title: createForm.title.trim(),
        description: normalizeString(createForm.description),
        maintenanceTaskId: toOptionalNumber(createForm.maintenanceTaskId),
        assignedUserId: toOptionalNumber(createForm.assignedUserId),
      });

      setWorkOrders((current) => [created, ...current]);
      setCreateForm(defaultCreateForm);
      setCreateOpen(false);
    } catch (requestError) {
      setCreateError(requestError.userMessage || "Work order could not be created.");
    } finally {
      setCreating(false);
    }
  }

  async function handleStartSelected() {
    if (!selectedWorkOrder) {
      return;
    }

    try {
      setActionLoading(true);
      await startWorkOrder(selectedWorkOrder.id);
      const nextSelected = await reloadWorkOrders(selectedWorkOrder.id);
      if (nextSelected) {
        await refreshSelectedDetails(nextSelected.id);
      }
    } catch (requestError) {
      setDetailError(requestError.userMessage || "Work order could not be started.");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleCompleteSelected(event) {
    event.preventDefault();

    if (!selectedWorkOrder) {
      return;
    }

    try {
      setActionLoading(true);
      setDetailError("");
      await completeWorkOrder(selectedWorkOrder.id, {
        completionNotes: completeForm.completionNotes.trim(),
        actualCost: completeForm.actualCost === "" ? 0 : Number(completeForm.actualCost),
        laborHours: completeForm.laborHours === "" ? 0 : Number(completeForm.laborHours),
      });
      setCompleteForm(defaultCompleteForm);
      const nextSelected = await reloadWorkOrders(selectedWorkOrder.id);
      if (nextSelected) {
        await refreshSelectedDetails(nextSelected.id);
      }
    } catch (requestError) {
      setDetailError(requestError.userMessage || "Work order could not be completed.");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleCancelSelected() {
    if (!selectedWorkOrder) {
      return;
    }

    try {
      setActionLoading(true);
      setDetailError("");
      await cancelWorkOrder(selectedWorkOrder.id);
      await reloadWorkOrders();
      setSelectedWorkOrder(null);
    } catch (requestError) {
      setDetailError(requestError.userMessage || "Work order could not be cancelled.");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleAddExpense(event) {
    event.preventDefault();

    if (!selectedWorkOrder) {
      return;
    }

    try {
      setActionLoading(true);
      setDetailError("");
      await addWorkOrderExpense(selectedWorkOrder.id, {
        costType: expenseForm.costType,
        description: expenseForm.description.trim(),
        amount: Number(expenseForm.amount),
      });
      setExpenseForm(defaultExpenseForm);
      await refreshSelectedDetails(selectedWorkOrder.id);
    } catch (requestError) {
      setDetailError(requestError.userMessage || "Expense could not be created.");
    } finally {
      setActionLoading(false);
    }
  }

  async function handleAddPartUsage(event) {
    event.preventDefault();

    if (!selectedWorkOrder) {
      return;
    }

    try {
      setActionLoading(true);
      setDetailError("");
      await addWorkOrderPart(selectedWorkOrder.id, {
        partId: Number(partForm.partId),
        quantityUsed: Number(partForm.quantityUsed),
        unitCost: partForm.unitCost === "" ? null : Number(partForm.unitCost),
        notes: normalizeString(partForm.notes),
      });
      setPartForm(defaultPartForm);
      await refreshSelectedDetails(selectedWorkOrder.id);
    } catch (requestError) {
      setDetailError(requestError.userMessage || "Part usage could not be created.");
    } finally {
      setActionLoading(false);
    }
  }

  if (loading) {
    return <LoadingState message="Work orders are loading." />;
  }

  if (error) {
    return (
      <div className="rounded-3xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <section className="rounded-[2rem] border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Maintenance Queue
            </p>
            <h3 className="mt-2 text-2xl font-semibold text-ink-900">Work Orders</h3>
            <p className="mt-2 text-sm text-ink-500">
              Create work orders, track lifecycle and register part and expense usage.
            </p>
          </div>
          <div className="flex flex-col gap-3 sm:items-end">
            <div className="rounded-2xl bg-ink-50 px-4 py-3 text-sm text-ink-600">
              Showing: <span className="font-semibold text-ink-900">{filteredWorkOrders.length}</span>
              <span className="text-ink-400"> / {workOrders.length}</span>
            </div>
            <button
              type="button"
              onClick={() => {
                setCreateError("");
                setCreateForm(defaultCreateForm);
                setCreateOpen(true);
              }}
              className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            >
              Create Work Order
            </button>
          </div>
        </div>

        <div className="mt-6 grid gap-3 lg:grid-cols-[1.4fr_0.8fr]">
          <label className="block">
            <span className="mb-2 block text-sm font-medium text-ink-700">Search</span>
            <input
              type="text"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Title, vehicle or assigned user"
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
              {workOrderStatuses.map((status) => (
                <option key={status} value={status}>
                  {formatEnumLabel(status)}
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
              <th className="px-6 py-4 font-medium">Status</th>
              <th className="px-6 py-4 font-medium">Vehicle</th>
              <th className="px-6 py-4 font-medium">Title</th>
              <th className="px-6 py-4 font-medium">Assigned</th>
              <th className="px-6 py-4 font-medium">Created At</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100">
            {filteredWorkOrders.map((workOrder) => (
              <tr
                key={workOrder.id}
                className="cursor-pointer text-sm text-ink-700 transition hover:bg-ink-50"
                onClick={() => openDetails(workOrder)}
              >
                <td className="px-6 py-4">
                  <StatusBadge status={workOrder.status} />
                </td>
                <td className="px-6 py-4 font-medium text-ink-900">{workOrder.vehicleName}</td>
                <td className="px-6 py-4">{workOrder.title || "-"}</td>
                <td className="px-6 py-4">{workOrder.assignedUsername || "-"}</td>
                <td className="px-6 py-4">{formatDateTime(workOrder.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="grid gap-4 lg:hidden">
        {filteredWorkOrders.map((workOrder) => (
          <article
            key={workOrder.id}
            className="cursor-pointer rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft transition hover:border-brand-200"
            onClick={() => openDetails(workOrder)}
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-lg font-semibold text-ink-900">{workOrder.vehicleName}</p>
                <p className="mt-1 text-sm text-ink-500">{workOrder.title || "Untitled work order"}</p>
              </div>
              <StatusBadge status={workOrder.status} />
            </div>
            <div className="mt-4 grid gap-3 sm:grid-cols-2">
              <InfoRow label="Assigned" value={workOrder.assignedUsername || "-"} />
              <InfoRow label="Created" value={formatDateTime(workOrder.createdAt)} />
            </div>
          </article>
        ))}
      </section>

      {filteredWorkOrders.length === 0 ? (
        <div className="rounded-3xl border border-ink-200 bg-white/90 p-8 text-center text-sm text-ink-500 shadow-soft">
          No work orders matched the current filters.
        </div>
      ) : null}

      <ModalShell open={createOpen} onClose={() => setCreateOpen(false)}>
        <form onSubmit={handleCreate} className="space-y-5">
          <HeaderBlock
            eyebrow="Work Order Create"
            title="Create Work Order"
            description="Open a new maintenance or repair item for a vehicle."
            onClose={() => setCreateOpen(false)}
          />

          <div className="grid gap-4 sm:grid-cols-2">
            <SelectField
              label="Vehicle"
              value={createForm.vehicleId}
              onChange={(value) => setCreateForm((current) => ({ ...current, vehicleId: value }))}
              required
              options={vehicles.map((vehicle) => ({ value: String(vehicle.id), label: vehicle.name }))}
            />
            <FormField
              label="Title"
              value={createForm.title}
              onChange={(value) => setCreateForm((current) => ({ ...current, title: value }))}
              required
            />
            <FormField
              label="Maintenance Task Id"
              type="number"
              value={createForm.maintenanceTaskId}
              onChange={(value) =>
                setCreateForm((current) => ({ ...current, maintenanceTaskId: value }))
              }
            />
            <FormField
              label="Assigned User Id"
              type="number"
              value={createForm.assignedUserId}
              onChange={(value) => setCreateForm((current) => ({ ...current, assignedUserId: value }))}
            />
          </div>

          <TextAreaField
            label="Description"
            value={createForm.description}
            onChange={(value) => setCreateForm((current) => ({ ...current, description: value }))}
          />

          {createError ? (
            <ErrorBanner>{createError}</ErrorBanner>
          ) : null}

          <ActionRow
            primaryLabel={creating ? "Creating..." : "Create"}
            primaryDisabled={creating}
            onCancel={() => setCreateOpen(false)}
          />
        </form>
      </ModalShell>

      <ModalShell open={Boolean(selectedWorkOrder)} onClose={() => setSelectedWorkOrder(null)} wide>
        {selectedWorkOrder ? (
          <div className="space-y-6">
            <HeaderBlock
              eyebrow="Work Order Details"
              title={selectedWorkOrder.title || "Work Order"}
              description={`${selectedWorkOrder.vehicleName || "-"} / ${formatEnumLabel(selectedWorkOrder.status)}`}
              onClose={() => setSelectedWorkOrder(null)}
            />

            {detailLoading ? (
              <LoadingState message="Work order details are loading." />
            ) : (
              <>
                <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
                  <div className="space-y-4">
                    <PanelCard title="Overview">
                      <div className="grid gap-3 sm:grid-cols-2">
                        <InfoRow label="Status" value={<StatusBadge status={selectedWorkOrder.status} />} />
                        <InfoRow label="Vehicle" value={selectedWorkOrder.vehicleName} />
                        <InfoRow label="Assigned User" value={selectedWorkOrder.assignedUsername || "-"} />
                        <InfoRow label="Created" value={formatDateTime(selectedWorkOrder.createdAt)} />
                        <InfoRow label="Updated" value={formatDateTime(selectedWorkOrder.updatedAt)} />
                        <InfoRow label="Completed" value={formatDateTime(selectedWorkOrder.completedAt)} />
                      </div>
                      <div className="mt-4 rounded-2xl bg-ink-50 px-4 py-4">
                        <p className="text-sm text-ink-500">Description</p>
                        <p className="mt-2 text-sm font-medium text-ink-900">
                          {selectedWorkOrder.description || "-"}
                        </p>
                      </div>
                    </PanelCard>

                    <PanelCard title="Part Usage">
                      <form onSubmit={handleAddPartUsage} className="grid gap-3 md:grid-cols-2">
                        <SelectField
                          label="Part"
                          value={partForm.partId}
                          onChange={(value) => setPartForm((current) => ({ ...current, partId: value }))}
                          required
                          options={parts.map((part) => ({
                            value: String(part.id),
                            label: `${part.partNumber} / ${part.name}`,
                          }))}
                        />
                        <FormField
                          label="Quantity Used"
                          type="number"
                          value={partForm.quantityUsed}
                          onChange={(value) =>
                            setPartForm((current) => ({ ...current, quantityUsed: value }))
                          }
                          required
                        />
                        <FormField
                          label="Unit Cost"
                          type="number"
                          value={partForm.unitCost}
                          onChange={(value) => setPartForm((current) => ({ ...current, unitCost: value }))}
                        />
                        <FormField
                          label="Notes"
                          value={partForm.notes}
                          onChange={(value) => setPartForm((current) => ({ ...current, notes: value }))}
                        />
                        <div className="md:col-span-2">
                          <button
                            type="submit"
                            disabled={actionLoading}
                            className="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
                          >
                            Add Part Usage
                          </button>
                        </div>
                      </form>

                      <div className="mt-4 space-y-3">
                        {partsUsage.map((usage) => (
                          <div key={usage.id} className="rounded-2xl bg-ink-50 px-4 py-4">
                            <div className="flex items-center justify-between gap-4">
                              <div>
                                <p className="text-sm font-semibold text-ink-900">{usage.partName}</p>
                                <p className="text-sm text-ink-500">
                                  {usage.partNumber} / Qty {formatNumber(usage.quantityUsed)}
                                </p>
                              </div>
                              <p className="text-sm font-semibold text-ink-900">
                                {formatCurrency(usage.totalCost)}
                              </p>
                            </div>
                          </div>
                        ))}
                        {partsUsage.length === 0 ? (
                          <p className="text-sm text-ink-500">No part usage entries yet.</p>
                        ) : null}
                      </div>
                    </PanelCard>
                  </div>

                  <div className="space-y-4">
                    <PanelCard title="Actions">
                      <div className="flex flex-wrap gap-3">
                        {selectedWorkOrder.status === "OPEN" || selectedWorkOrder.status === "ASSIGNED" ? (
                          <button
                            type="button"
                            onClick={handleStartSelected}
                            disabled={actionLoading}
                            className="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
                          >
                            Start Work Order
                          </button>
                        ) : null}
                        {selectedWorkOrder.status !== "COMPLETED" &&
                        selectedWorkOrder.status !== "CANCELLED" ? (
                          <button
                            type="button"
                            onClick={handleCancelSelected}
                            disabled={actionLoading}
                            className="rounded-2xl border border-rose-200 px-4 py-3 text-sm font-semibold text-rose-700 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-70"
                          >
                            Cancel Work Order
                          </button>
                        ) : null}
                      </div>

                      {selectedWorkOrder.status !== "COMPLETED" &&
                      selectedWorkOrder.status !== "CANCELLED" ? (
                        <form onSubmit={handleCompleteSelected} className="mt-5 space-y-3">
                          <TextAreaField
                            label="Completion Notes"
                            value={completeForm.completionNotes}
                            onChange={(value) =>
                              setCompleteForm((current) => ({ ...current, completionNotes: value }))
                            }
                            required
                          />
                          <div className="grid gap-3 sm:grid-cols-2">
                            <FormField
                              label="Actual Cost"
                              type="number"
                              value={completeForm.actualCost}
                              onChange={(value) =>
                                setCompleteForm((current) => ({ ...current, actualCost: value }))
                              }
                            />
                            <FormField
                              label="Labor Hours"
                              type="number"
                              value={completeForm.laborHours}
                              onChange={(value) =>
                                setCompleteForm((current) => ({ ...current, laborHours: value }))
                              }
                            />
                          </div>
                          <button
                            type="submit"
                            disabled={actionLoading}
                            className="rounded-2xl bg-ink-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-ink-700 disabled:cursor-not-allowed disabled:opacity-70"
                          >
                            Complete Work Order
                          </button>
                        </form>
                      ) : null}
                    </PanelCard>

                    <PanelCard title="Expenses">
                      <form onSubmit={handleAddExpense} className="grid gap-3">
                        <SelectField
                          label="Cost Type"
                          value={expenseForm.costType}
                          onChange={(value) =>
                            setExpenseForm((current) => ({ ...current, costType: value }))
                          }
                          options={costTypes}
                          required
                        />
                        <FormField
                          label="Description"
                          value={expenseForm.description}
                          onChange={(value) =>
                            setExpenseForm((current) => ({ ...current, description: value }))
                          }
                          required
                        />
                        <FormField
                          label="Amount"
                          type="number"
                          value={expenseForm.amount}
                          onChange={(value) => setExpenseForm((current) => ({ ...current, amount: value }))}
                          required
                        />
                        <button
                          type="submit"
                          disabled={actionLoading}
                          className="rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
                        >
                          Add Expense
                        </button>
                      </form>

                      <div className="mt-4 space-y-3">
                        {expenses.map((expense) => (
                          <div key={expense.id} className="rounded-2xl bg-ink-50 px-4 py-4">
                            <div className="flex items-center justify-between gap-4">
                              <div>
                                <p className="text-sm font-semibold text-ink-900">{expense.description}</p>
                                <p className="text-sm text-ink-500">{formatEnumLabel(expense.costType)}</p>
                              </div>
                              <p className="text-sm font-semibold text-ink-900">
                                {formatCurrency(expense.amount)}
                              </p>
                            </div>
                          </div>
                        ))}
                        {expenses.length === 0 ? (
                          <p className="text-sm text-ink-500">No expenses recorded yet.</p>
                        ) : null}
                      </div>
                    </PanelCard>

                    <PanelCard title="Cost Summary">
                      <div className="grid gap-3 sm:grid-cols-2">
                        <InfoRow label="Part Cost" value={formatCurrency(costSummary?.partCostTotal)} />
                        <InfoRow label="Labor Cost" value={formatCurrency(costSummary?.laborCostTotal)} />
                        <InfoRow
                          label="External Service"
                          value={formatCurrency(costSummary?.externalServiceCostTotal)}
                        />
                        <InfoRow label="Misc Cost" value={formatCurrency(costSummary?.miscCostTotal)} />
                      </div>
                      <div className="mt-4 rounded-2xl bg-ink-900 px-4 py-4 text-white">
                        <p className="text-sm text-slate-300">Grand Total</p>
                        <p className="mt-2 text-2xl font-semibold">
                          {formatCurrency(costSummary?.grandTotal)}
                        </p>
                      </div>
                    </PanelCard>
                  </div>
                </div>

                {detailError ? <ErrorBanner>{detailError}</ErrorBanner> : null}
              </>
            )}
          </div>
        ) : null}
      </ModalShell>
    </div>
  );
}

function ModalShell({ open, onClose, children, wide = false }) {
  if (!open) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-end bg-ink-900/55 p-0 sm:items-center sm:justify-center sm:p-6"
      onClick={onClose}
    >
      <div
        className={`max-h-[92vh] w-full overflow-y-auto rounded-t-[2rem] bg-white p-6 shadow-soft sm:rounded-[2rem] sm:p-8 ${
          wide ? "sm:max-w-6xl" : "sm:max-w-3xl"
        }`}
        onClick={(event) => event.stopPropagation()}
      >
        {children}
      </div>
    </div>
  );
}

function HeaderBlock({ eyebrow, title, description, onClose }) {
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

function PanelCard({ title, children }) {
  return (
    <section className="rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft">
      <h4 className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">{title}</h4>
      <div className="mt-4">{children}</div>
    </section>
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

function SelectField({ label, value, onChange, options, required = false }) {
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
        <option value="">Select...</option>
        {options.map((option) => {
          const normalizedOption =
            typeof option === "string"
              ? { value: option, label: formatEnumLabel(option) }
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

function TextAreaField({ label, value, onChange, required = false }) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-ink-700">
        {label}
        {required ? " *" : ""}
      </span>
      <textarea
        rows="4"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition placeholder:text-ink-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
      />
    </label>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="rounded-2xl bg-ink-50 px-4 py-4">
      <p className="text-sm text-ink-500">{label}</p>
      <div className="mt-2 text-sm font-semibold text-ink-900">{value ?? "-"}</div>
    </div>
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

function ErrorBanner({ children }) {
  return (
    <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
      {children}
    </div>
  );
}

function ActionRow({ primaryLabel, primaryDisabled, onCancel }) {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
      <button
        type="button"
        onClick={onCancel}
        className="rounded-2xl border border-ink-200 px-5 py-3 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
      >
        Cancel
      </button>
      <button
        type="submit"
        disabled={primaryDisabled}
        className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
      >
        {primaryLabel}
      </button>
    </div>
  );
}

function normalizeString(value) {
  return value?.trim() ? value.trim() : null;
}

function toOptionalNumber(value) {
  return value === "" || value === null || value === undefined ? null : Number(value);
}

export default WorkOrdersPage;
