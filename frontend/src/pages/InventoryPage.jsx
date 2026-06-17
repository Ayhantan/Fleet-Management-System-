import { useEffect, useMemo, useState } from "react";
import LoadingState from "../components/LoadingState.jsx";
import {
  createInventoryItem,
  createPart,
  deleteInventoryItem,
  deletePart,
  getInventoryItems,
  getLowStockInventoryItems,
  getParts,
  getStockMovementsByPart,
  stockInPart,
  stockOutPart,
  updateInventoryItem,
  updatePart,
} from "../services/inventoryService.js";

const emptyPartForm = {
  partNumber: "",
  name: "",
  description: "",
  unit: "",
};

const emptyInventoryForm = {
  partId: "",
  currentQuantity: "",
  minimumStockLevel: "",
  location: "",
};

const emptyMovementForm = {
  quantity: "",
  notes: "",
};

function InventoryPage() {
  const [parts, setParts] = useState([]);
  const [inventoryItems, setInventoryItems] = useState([]);
  const [lowStockItems, setLowStockItems] = useState([]);
  const [stockMovements, setStockMovements] = useState([]);
  const [selectedPartId, setSelectedPartId] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [partForm, setPartForm] = useState(emptyPartForm);
  const [inventoryForm, setInventoryForm] = useState(emptyInventoryForm);
  const [movementForm, setMovementForm] = useState(emptyMovementForm);
  const [editingPartId, setEditingPartId] = useState(null);
  const [editingInventoryId, setEditingInventoryId] = useState(null);
  const [activeMovementType, setActiveMovementType] = useState("IN");
  const [isSavingPart, setIsSavingPart] = useState(false);
  const [isSavingInventory, setIsSavingInventory] = useState(false);
  const [isSavingMovement, setIsSavingMovement] = useState(false);
  const [actionMessage, setActionMessage] = useState("");

  useEffect(() => {
    async function loadInventoryData() {
      try {
        setLoading(true);
        setError("");

        const [partsResponse, inventoryResponse, lowStockResponse] = await Promise.all([
          getParts(),
          getInventoryItems(),
          getLowStockInventoryItems(),
        ]);

        setParts(partsResponse);
        setInventoryItems(inventoryResponse);
        setLowStockItems(lowStockResponse);
        setSelectedPartId((currentSelectedPartId) => {
          if (currentSelectedPartId) {
            return currentSelectedPartId;
          }

          return partsResponse[0] ? String(partsResponse[0].id) : "";
        });
      } catch (requestError) {
        setError(requestError.userMessage || "Inventory data could not be loaded.");
      } finally {
        setLoading(false);
      }
    }

    loadInventoryData();
  }, []);

  useEffect(() => {
    if (!selectedPartId) {
      setStockMovements([]);
      return undefined;
    }

    let isCurrent = true;

    async function loadMovements() {
      try {
        const response = await getStockMovementsByPart(Number(selectedPartId));
        if (isCurrent) {
          setStockMovements(response);
        }
      } catch (requestError) {
        if (isCurrent) {
          setError(requestError.userMessage || "Stock movements could not be loaded.");
        }
      }
    }

    loadMovements();

    return () => {
      isCurrent = false;
    };
  }, [selectedPartId]);

  useEffect(() => {
    if (selectedPartId) {
      return;
    }

    if (parts.length > 0) {
      setSelectedPartId(String(parts[0].id));
    }
  }, [parts, selectedPartId]);

  const selectedPart = useMemo(
    () => parts.find((part) => String(part.id) === String(selectedPartId)) || null,
    [parts, selectedPartId],
  );

  const inventoryFormPartOptions = parts.map((part) => ({
    value: String(part.id),
    label: `${part.partNumber} - ${part.name}`,
  }));

  async function refreshData(nextSelectedPartId = selectedPartId) {
    const [partsResponse, inventoryResponse, lowStockResponse] = await Promise.all([
      getParts(),
      getInventoryItems(),
      getLowStockInventoryItems(),
    ]);

    setParts(partsResponse);
    setInventoryItems(inventoryResponse);
    setLowStockItems(lowStockResponse);

    const safeSelectedPartId =
      nextSelectedPartId || (partsResponse[0] ? String(partsResponse[0].id) : "");

    setSelectedPartId(safeSelectedPartId);

    if (safeSelectedPartId) {
      const movementsResponse = await getStockMovementsByPart(Number(safeSelectedPartId));
      setStockMovements(movementsResponse);
    } else {
      setStockMovements([]);
    }
  }

  async function handlePartSubmit(event) {
    event.preventDefault();

    try {
      setIsSavingPart(true);
      setError("");
      setActionMessage("");

      const payload = buildPartPayload(partForm);

      if (editingPartId) {
        await updatePart(editingPartId, payload);
        setActionMessage("Part updated.");
      } else {
        await createPart(payload);
        setActionMessage("Part created.");
      }

      setPartForm(emptyPartForm);
      setEditingPartId(null);
      await refreshData(selectedPartId);
    } catch (requestError) {
      setError(requestError.userMessage || "Part could not be saved.");
    } finally {
      setIsSavingPart(false);
    }
  }

  async function handleInventorySubmit(event) {
    event.preventDefault();

    try {
      setIsSavingInventory(true);
      setError("");
      setActionMessage("");

      const payload = buildInventoryPayload(inventoryForm);

      if (editingInventoryId) {
        await updateInventoryItem(editingInventoryId, payload);
        setActionMessage("Inventory item updated.");
      } else {
        await createInventoryItem(payload);
        setActionMessage("Inventory item created.");
      }

      setInventoryForm(emptyInventoryForm);
      setEditingInventoryId(null);
      await refreshData(selectedPartId);
    } catch (requestError) {
      setError(requestError.userMessage || "Inventory item could not be saved.");
    } finally {
      setIsSavingInventory(false);
    }
  }

  async function handleMovementSubmit(event) {
    event.preventDefault();

    if (!selectedPartId) {
      setError("Select a part before posting stock movements.");
      return;
    }

    try {
      setIsSavingMovement(true);
      setError("");
      setActionMessage("");

      const payload = buildMovementPayload(selectedPartId, movementForm);
      const mutation = activeMovementType === "IN" ? stockInPart : stockOutPart;
      await mutation(payload);

      setMovementForm(emptyMovementForm);
      setActionMessage(activeMovementType === "IN" ? "Stock in recorded." : "Stock out recorded.");
      const movementsResponse = await getStockMovementsByPart(Number(selectedPartId));
      setStockMovements(movementsResponse);

      const [partsResponse, inventoryResponse, lowStockResponse] = await Promise.all([
        getParts(),
        getInventoryItems(),
        getLowStockInventoryItems(),
      ]);
      setParts(partsResponse);
      setInventoryItems(inventoryResponse);
      setLowStockItems(lowStockResponse);
    } catch (requestError) {
      setError(requestError.userMessage || "Stock movement could not be saved.");
    } finally {
      setIsSavingMovement(false);
    }
  }

  function handleEditPart(part) {
    setEditingPartId(part.id);
    setPartForm({
      partNumber: part.partNumber || "",
      name: part.name || "",
      description: part.description || "",
      unit: part.unit || "",
    });
    setActionMessage("");
  }

  function handleEditInventoryItem(item) {
    setEditingInventoryId(item.id);
    setInventoryForm({
      partId: item.partId ? String(item.partId) : "",
      currentQuantity: item.currentQuantity != null ? String(item.currentQuantity) : "",
      minimumStockLevel: item.minimumStockLevel != null ? String(item.minimumStockLevel) : "",
      location: item.location || "",
    });
    setActionMessage("");
  }

  async function handleDeletePart(partId) {
    if (!window.confirm("Delete this part?")) {
      return;
    }

    try {
      setError("");
      await deletePart(partId);
      if (String(partId) === String(selectedPartId)) {
        setSelectedPartId("");
      }
      await refreshData(String(partId) === String(selectedPartId) ? "" : selectedPartId);
      setActionMessage("Part deleted.");
      if (editingPartId === partId) {
        setPartForm(emptyPartForm);
        setEditingPartId(null);
      }
    } catch (requestError) {
      setError(requestError.userMessage || "Part could not be deleted.");
    }
  }

  async function handleDeleteInventoryItem(itemId) {
    if (!window.confirm("Delete this inventory item?")) {
      return;
    }

    try {
      setError("");
      await deleteInventoryItem(itemId);
      await refreshData(selectedPartId);
      setActionMessage("Inventory item deleted.");
      if (editingInventoryId === itemId) {
        setInventoryForm(emptyInventoryForm);
        setEditingInventoryId(null);
      }
    } catch (requestError) {
      setError(requestError.userMessage || "Inventory item could not be deleted.");
    }
  }

  if (loading) {
    return <LoadingState message="Inventory data is loading." />;
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
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Inventory Control
            </p>
            <h3 className="mt-2 text-2xl font-semibold text-ink-900">Parts and stock</h3>
            <p className="mt-2 max-w-2xl text-sm text-ink-500">
              Manage parts, inventory items, low stock signals and stock movement history from a
              single responsive screen.
            </p>
          </div>
          <div className="grid gap-3 sm:grid-cols-3">
            <MiniStat label="Parts" value={parts.length} />
            <MiniStat label="Inventory Items" value={inventoryItems.length} />
            <MiniStat label="Low Stock" value={lowStockItems.length} accent />
          </div>
        </div>
        {actionMessage ? (
          <div className="mt-5 rounded-2xl border border-brand-200 bg-brand-50 px-4 py-3 text-sm text-brand-700">
            {actionMessage}
          </div>
        ) : null}
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
                Parts
              </p>
              <h4 className="mt-2 text-xl font-semibold text-ink-900">
                {editingPartId ? "Update part" : "Create part"}
              </h4>
            </div>
            {editingPartId ? (
              <button
                type="button"
                onClick={() => {
                  setEditingPartId(null);
                  setPartForm(emptyPartForm);
                }}
                className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
              >
                Cancel edit
              </button>
            ) : null}
          </div>

          <form onSubmit={handlePartSubmit} className="mt-6 grid gap-4 sm:grid-cols-2">
            <TextField
              label="Part Number"
              value={partForm.partNumber}
              onChange={(value) => setPartForm((current) => ({ ...current, partNumber: value }))}
              required
            />
            <TextField
              label="Name"
              value={partForm.name}
              onChange={(value) => setPartForm((current) => ({ ...current, name: value }))}
              required
            />
            <TextField
              label="Unit"
              value={partForm.unit}
              onChange={(value) => setPartForm((current) => ({ ...current, unit: value }))}
            />
            <TextAreaField
              label="Description"
              value={partForm.description}
              onChange={(value) => setPartForm((current) => ({ ...current, description: value }))}
            />
            <div className="sm:col-span-2 flex flex-col gap-3 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={() => {
                  setEditingPartId(null);
                  setPartForm(emptyPartForm);
                }}
                className="rounded-2xl border border-ink-200 px-5 py-3 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
              >
                Reset
              </button>
              <button
                type="submit"
                disabled={isSavingPart}
                className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSavingPart ? "Saving..." : editingPartId ? "Update Part" : "Create Part"}
              </button>
            </div>
          </form>
        </div>

        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
                Parts List
              </p>
              <h4 className="mt-2 text-xl font-semibold text-ink-900">Registered parts</h4>
            </div>
            <div className="rounded-2xl bg-ink-50 px-4 py-2 text-sm text-ink-600">
              Total: <span className="font-semibold text-ink-900">{parts.length}</span>
            </div>
          </div>

          <div className="mt-6 grid gap-4">
            {parts.map((part) => (
              <article key={part.id} className="rounded-2xl border border-ink-200 bg-ink-50/80 p-4">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <p className="text-lg font-semibold text-ink-900">{part.partNumber}</p>
                    <p className="mt-1 text-sm text-ink-600">{part.name}</p>
                    <p className="mt-2 text-sm text-ink-500">{part.description || "No description"}</p>
                    <p className="mt-2 text-xs uppercase tracking-[0.18em] text-ink-400">
                      Unit: {part.unit || "-"}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => handleEditPart(part)}
                      className="rounded-full border border-brand-200 bg-white px-4 py-2 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDeletePart(part.id)}
                      className="rounded-full border border-rose-200 bg-white px-4 py-2 text-sm font-medium text-rose-700 transition hover:bg-rose-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </article>
            ))}
            {parts.length === 0 ? (
              <EmptyState message="No parts were returned by the API." />
            ) : null}
          </div>
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1fr_1.15fr]">
        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
                Inventory Items
              </p>
              <h4 className="mt-2 text-xl font-semibold text-ink-900">
                {editingInventoryId ? "Update inventory item" : "Create inventory item"}
              </h4>
            </div>
            {editingInventoryId ? (
              <button
                type="button"
                onClick={() => {
                  setEditingInventoryId(null);
                  setInventoryForm(emptyInventoryForm);
                }}
                className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
              >
                Cancel edit
              </button>
            ) : null}
          </div>

          <form onSubmit={handleInventorySubmit} className="mt-6 grid gap-4 sm:grid-cols-2">
            <SelectField
              label="Part"
              value={inventoryForm.partId}
              onChange={(value) => setInventoryForm((current) => ({ ...current, partId: value }))}
              options={inventoryFormPartOptions}
              placeholder="Select a part"
              required
            />
            <TextField
              label="Current Quantity"
              type="number"
              value={inventoryForm.currentQuantity}
              onChange={(value) =>
                setInventoryForm((current) => ({ ...current, currentQuantity: value }))
              }
              required
            />
            <TextField
              label="Minimum Stock Level"
              type="number"
              value={inventoryForm.minimumStockLevel}
              onChange={(value) =>
                setInventoryForm((current) => ({ ...current, minimumStockLevel: value }))
              }
              required
            />
            <TextField
              label="Location"
              value={inventoryForm.location}
              onChange={(value) => setInventoryForm((current) => ({ ...current, location: value }))}
            />
            <div className="sm:col-span-2 flex flex-col gap-3 sm:flex-row sm:justify-end">
              <button
                type="button"
                onClick={() => {
                  setEditingInventoryId(null);
                  setInventoryForm(emptyInventoryForm);
                }}
                className="rounded-2xl border border-ink-200 px-5 py-3 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
              >
                Reset
              </button>
              <button
                type="submit"
                disabled={isSavingInventory}
                className="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSavingInventory
                  ? "Saving..."
                  : editingInventoryId
                    ? "Update Item"
                    : "Create Item"}
              </button>
            </div>
          </form>
        </div>

        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
                Inventory List
              </p>
              <h4 className="mt-2 text-xl font-semibold text-ink-900">Stock positions</h4>
            </div>
            <div className="rounded-2xl bg-ink-50 px-4 py-2 text-sm text-ink-600">
              Low stock: <span className="font-semibold text-ink-900">{lowStockItems.length}</span>
            </div>
          </div>

          <div className="mt-6 grid gap-4">
            {inventoryItems.map((item) => (
              <article key={item.id} className="rounded-2xl border border-ink-200 bg-ink-50/80 p-4">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div className="min-w-0">
                    <p className="truncate text-lg font-semibold text-ink-900">
                      {item.partNumber} - {item.partName}
                    </p>
                    <div className="mt-2 flex flex-wrap gap-2 text-sm text-ink-600">
                      <Pill>{item.currentQuantity} available</Pill>
                      <Pill>Min {item.minimumStockLevel}</Pill>
                      <Pill>{item.location || "No location"}</Pill>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => handleEditInventoryItem(item)}
                      className="rounded-full border border-brand-200 bg-white px-4 py-2 text-sm font-medium text-brand-700 transition hover:bg-brand-50"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDeleteInventoryItem(item.id)}
                      className="rounded-full border border-rose-200 bg-white px-4 py-2 text-sm font-medium text-rose-700 transition hover:bg-rose-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </article>
            ))}
            {inventoryItems.length === 0 ? (
              <EmptyState message="No inventory items were returned by the API." />
            ) : null}
          </div>
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1fr_1.1fr]">
        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
                Stock Movements
              </p>
              <h4 className="mt-2 text-xl font-semibold text-ink-900">Post in or out</h4>
            </div>
            <div className="inline-flex rounded-full bg-ink-100 p-1 text-xs font-semibold text-ink-600">
              <button
                type="button"
                onClick={() => setActiveMovementType("IN")}
                className={`rounded-full px-3 py-2 transition ${
                  activeMovementType === "IN" ? "bg-white text-brand-700 shadow-sm" : ""
                }`}
              >
                Stock In
              </button>
              <button
                type="button"
                onClick={() => setActiveMovementType("OUT")}
                className={`rounded-full px-3 py-2 transition ${
                  activeMovementType === "OUT" ? "bg-white text-brand-700 shadow-sm" : ""
                }`}
              >
                Stock Out
              </button>
            </div>
          </div>

          <form onSubmit={handleMovementSubmit} className="mt-6 space-y-4">
            <SelectField
              label="Part"
              value={selectedPartId}
              onChange={(value) => setSelectedPartId(value)}
              options={parts.map((part) => ({
                value: String(part.id),
                label: `${part.partNumber} - ${part.name}`,
              }))}
              placeholder="Select a part"
              required
            />
            <TextField
              label="Quantity"
              type="number"
              value={movementForm.quantity}
              onChange={(value) => setMovementForm((current) => ({ ...current, quantity: value }))}
              required
            />
            <TextAreaField
              label="Notes"
              value={movementForm.notes}
              onChange={(value) => setMovementForm((current) => ({ ...current, notes: value }))}
            />
            <button
              type="submit"
              disabled={isSavingMovement || !selectedPartId}
              className="w-full rounded-2xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isSavingMovement
                ? "Saving..."
                : activeMovementType === "IN"
                  ? "Record Stock In"
                  : "Record Stock Out"}
            </button>
          </form>
        </div>

        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
                Movement History
              </p>
              <h4 className="mt-2 text-xl font-semibold text-ink-900">
                {selectedPart ? `${selectedPart.partNumber} - ${selectedPart.name}` : "Select a part"}
              </h4>
            </div>
            <div className="rounded-2xl bg-ink-50 px-4 py-2 text-sm text-ink-600">
              Entries: <span className="font-semibold text-ink-900">{stockMovements.length}</span>
            </div>
          </div>

          <div className="mt-6 grid gap-4">
            {selectedPartId ? (
              stockMovements.map((movement) => (
                <article
                  key={movement.id}
                  className="rounded-2xl border border-ink-200 bg-ink-50/80 p-4"
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <p className="text-sm font-semibold text-ink-900">
                        {movement.type ? movement.type.replaceAll("_", " ") : "UNKNOWN"} {movement.quantity}
                      </p>
                      <p className="mt-1 text-sm text-ink-600">
                        {movement.notes || "No notes"}
                      </p>
                    </div>
                    <div className="text-xs uppercase tracking-[0.18em] text-ink-400">
                      {formatDateTime(movement.createdAt)}
                    </div>
                  </div>
                </article>
              ))
            ) : (
              <EmptyState message="Choose a part to view stock movements." />
            )}
            {selectedPartId && stockMovements.length === 0 ? (
              <EmptyState message="No stock movements were found for the selected part." />
            ) : null}
          </div>
        </div>
      </section>

      <section className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">
              Low Stock
            </p>
            <h4 className="mt-2 text-xl font-semibold text-ink-900">Items below threshold</h4>
          </div>
          <div className="rounded-2xl bg-ink-50 px-4 py-2 text-sm text-ink-600">
            Current part: <span className="font-semibold text-ink-900">{selectedPart?.name || "-"}</span>
          </div>
        </div>

        <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {lowStockItems.map((item) => (
            <article key={item.id} className="rounded-2xl border border-amber-200 bg-amber-50/70 p-4">
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-amber-700">
                Low stock
              </p>
              <p className="mt-3 text-lg font-semibold text-ink-900">
                {item.partNumber} - {item.partName}
              </p>
              <div className="mt-3 grid gap-2 text-sm text-ink-600">
                <div>Quantity: <span className="font-semibold text-ink-900">{item.currentQuantity}</span></div>
                <div>Minimum: <span className="font-semibold text-ink-900">{item.minimumStockLevel}</span></div>
                <div>Location: <span className="font-semibold text-ink-900">{item.location || "-"}</span></div>
              </div>
            </article>
          ))}
          {lowStockItems.length === 0 ? (
            <EmptyState message="No low stock items were returned by the API." />
          ) : null}
        </div>
      </section>
    </div>
  );
}

function TextField({ label, value, onChange, type = "text", required = false }) {
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

function TextAreaField({ label, value, onChange, required = false }) {
  return (
    <label className="block sm:col-span-2">
      <span className="mb-2 block text-sm font-medium text-ink-700">
        {label}
        {required ? " *" : ""}
      </span>
      <textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        rows={4}
        className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-sm text-ink-900 outline-none transition placeholder:text-ink-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
      />
    </label>
  );
}

function SelectField({ label, value, onChange, options, placeholder, required = false }) {
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
        <option value="">{placeholder || "Select an option"}</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}

function MiniStat({ label, value, accent = false }) {
  return (
    <div
      className={`rounded-2xl px-4 py-3 shadow-sm ${
        accent ? "bg-amber-50 text-amber-800" : "bg-ink-50 text-ink-700"
      }`}
    >
      <p className="text-xs uppercase tracking-[0.18em] opacity-80">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-ink-900">{value}</p>
    </div>
  );
}

function Pill({ children }) {
  return <span className="rounded-full bg-white px-3 py-1 text-xs font-medium text-ink-700">{children}</span>;
}

function EmptyState({ message }) {
  return (
    <div className="rounded-2xl border border-dashed border-ink-200 bg-ink-50 px-4 py-6 text-center text-sm text-ink-500">
      {message}
    </div>
  );
}

function buildPartPayload(form) {
  return {
    partNumber: form.partNumber.trim(),
    name: form.name.trim(),
    description: normalizeString(form.description),
    unit: normalizeString(form.unit),
  };
}

function buildInventoryPayload(form) {
  return {
    partId: toNumber(form.partId),
    currentQuantity: toNumber(form.currentQuantity),
    minimumStockLevel: toNumber(form.minimumStockLevel),
    location: normalizeString(form.location),
  };
}

function buildMovementPayload(partId, form) {
  return {
    partId: toNumber(partId),
    quantity: toNumber(form.quantity),
    notes: normalizeString(form.notes),
  };
}

function normalizeString(value) {
  return value?.trim() ? value.trim() : null;
}

function toNumber(value) {
  return Number(value);
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export default InventoryPage;
