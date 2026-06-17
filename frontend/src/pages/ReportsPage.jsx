import { useEffect, useMemo, useState } from "react";
import LoadingState from "../components/LoadingState.jsx";
import {
  getMaintenanceStatusSummary,
  getPartConsumptionReport,
  getRecentCompletedWorkOrders,
  getVehicleMaintenanceCosts,
  getWorkOrderStatusSummary,
  getLowStockSummary,
} from "../services/reportService.js";

function ReportsPage() {
  const [workOrderSummary, setWorkOrderSummary] = useState(null);
  const [maintenanceSummary, setMaintenanceSummary] = useState(null);
  const [recentCompletedWorkOrders, setRecentCompletedWorkOrders] = useState([]);
  const [vehicleMaintenanceCosts, setVehicleMaintenanceCosts] = useState([]);
  const [partConsumption, setPartConsumption] = useState([]);
  const [lowStockSummary, setLowStockSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadReports() {
      try {
        setLoading(true);
        setError("");

        const [
          workOrderSummaryResponse,
          maintenanceSummaryResponse,
          recentCompletedResponse,
          vehicleCostsResponse,
          partConsumptionResponse,
          lowStockSummaryResponse,
        ] = await Promise.all([
          getWorkOrderStatusSummary(),
          getMaintenanceStatusSummary(),
          getRecentCompletedWorkOrders(),
          getVehicleMaintenanceCosts(),
          getPartConsumptionReport(),
          getLowStockSummary(),
        ]);

        setWorkOrderSummary(workOrderSummaryResponse);
        setMaintenanceSummary(maintenanceSummaryResponse);
        setRecentCompletedWorkOrders(recentCompletedResponse);
        setVehicleMaintenanceCosts(vehicleCostsResponse);
        setPartConsumption(partConsumptionResponse);
        setLowStockSummary(lowStockSummaryResponse);
      } catch (requestError) {
        setError(requestError.userMessage || "Reports could not be loaded.");
      } finally {
        setLoading(false);
      }
    }

    loadReports();
  }, []);

  const totalWorkOrders = workOrderSummary?.totalWorkOrders ?? 0;
  const totalMaintenanceSchedules = maintenanceSummary?.totalActiveSchedules ?? 0;
  const lowStockCount = lowStockSummary?.lowStockItemCount ?? 0;

  const highlightedWorkOrders = useMemo(
    () => (workOrderSummary?.items || []).slice(0, 6),
    [workOrderSummary],
  );

  const highlightedMaintenance = useMemo(
    () => (maintenanceSummary?.items || []).slice(0, 6),
    [maintenanceSummary],
  );

  if (loading) {
    return <LoadingState message="Reports are loading." />;
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
      <section className="rounded-[2rem] bg-ink-900 px-6 py-8 text-white shadow-soft sm:px-8">
        <p className="text-sm uppercase tracking-[0.25em] text-brand-100">Reports Center</p>
        <div className="mt-4 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h3 className="text-3xl font-semibold sm:text-4xl">Operational summaries</h3>
            <p className="mt-3 max-w-2xl text-sm text-slate-300 sm:text-base">
              Review work order flow, maintenance health, recent completions, vehicle cost totals,
              part consumption and low stock exposure in one place.
            </p>
          </div>
          <div className="grid gap-3 sm:grid-cols-3">
            <HeroStat label="Work Orders" value={totalWorkOrders} />
            <HeroStat label="Active Schedules" value={totalMaintenanceSchedules} />
            <HeroStat label="Low Stock" value={lowStockCount} accent />
          </div>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <SummaryTile label="Recent Completions" value={recentCompletedWorkOrders.length} />
        <SummaryTile label="Vehicles Costed" value={vehicleMaintenanceCosts.length} />
        <SummaryTile label="Consumed Parts" value={partConsumption.length} />
        <SummaryTile label="Low Stock Items" value={lowStockCount} accent />
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <ReportCard title="Work Order Status Summary" subtitle="Current workflow distribution">
          <div className="grid gap-3 sm:grid-cols-2">
            <TotalBadge label="Total work orders" value={totalWorkOrders} />
            {highlightedWorkOrders.map((item) => (
              <StatusRow
                key={item.status}
                label={toTitleCase(item.status)}
                value={item.count}
                tone="blue"
              />
            ))}
          </div>
        </ReportCard>

        <ReportCard title="Maintenance Status Summary" subtitle="Active schedule distribution">
          <div className="grid gap-3 sm:grid-cols-2">
            <TotalBadge label="Active schedules" value={totalMaintenanceSchedules} />
            {highlightedMaintenance.map((item) => (
              <StatusRow
                key={item.status}
                label={toTitleCase(item.status)}
                value={item.count}
                tone="amber"
              />
            ))}
          </div>
        </ReportCard>
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <ReportCard title="Recent Completed Work Orders" subtitle="Latest closed jobs">
          <div className="grid gap-4">
            {recentCompletedWorkOrders.map((workOrder) => (
              <article key={workOrder.workOrderId} className="rounded-2xl border border-ink-200 bg-ink-50/80 p-4">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <p className="text-lg font-semibold text-ink-900">
                      {workOrder.vehicleDisplayName || "Unnamed vehicle"}
                    </p>
                    <p className="mt-1 text-sm text-ink-600">
                      {workOrder.maintenanceTaskTitle || workOrder.title || "Completed work order"}
                    </p>
                    <p className="mt-2 text-sm text-ink-500">
                      {workOrder.completionNotes || "No completion notes"}
                    </p>
                  </div>
                  <div className="text-sm text-ink-500">
                    <div className="font-medium text-ink-900">{formatDateTime(workOrder.completedAt)}</div>
                    <div className="mt-1">WO #{workOrder.workOrderId}</div>
                  </div>
                </div>
              </article>
            ))}
            {recentCompletedWorkOrders.length === 0 ? (
              <EmptyState message="No completed work orders were returned by the API." />
            ) : null}
          </div>
        </ReportCard>

        <ReportCard title="Low Stock Summary" subtitle="Items requiring attention">
          <div className="grid gap-4">
            {(lowStockSummary?.items || []).map((item) => (
              <article key={item.inventoryItemId} className="rounded-2xl border border-amber-200 bg-amber-50/70 p-4">
                <p className="text-sm font-semibold uppercase tracking-[0.18em] text-amber-700">
                  Below threshold
                </p>
                <p className="mt-2 text-lg font-semibold text-ink-900">
                  {item.partNumber} - {item.partName}
                </p>
                <div className="mt-3 grid gap-2 text-sm text-ink-600 sm:grid-cols-2">
                  <div>
                    Quantity: <span className="font-semibold text-ink-900">{item.currentQuantity}</span>
                  </div>
                  <div>
                    Minimum: <span className="font-semibold text-ink-900">{item.minimumStockLevel}</span>
                  </div>
                  <div className="sm:col-span-2">
                    Location: <span className="font-semibold text-ink-900">{item.location || "-"}</span>
                  </div>
                </div>
              </article>
            ))}
            {(lowStockSummary?.items || []).length === 0 ? (
              <EmptyState message="No low stock items were returned by the API." />
            ) : null}
          </div>
        </ReportCard>
      </section>

      <section className="grid gap-6 xl:grid-cols-2">
        <ReportCard title="Vehicle Maintenance Costs" subtitle="Totals by vehicle">
          <div className="overflow-x-auto rounded-2xl border border-ink-200">
            <table className="min-w-[760px] divide-y divide-ink-200">
              <thead className="bg-ink-50">
                <tr className="text-left text-sm text-ink-500">
                  <th className="px-4 py-3 font-medium">Vehicle</th>
                  <th className="px-4 py-3 font-medium">Part</th>
                  <th className="px-4 py-3 font-medium">Labor</th>
                  <th className="px-4 py-3 font-medium">External</th>
                  <th className="px-4 py-3 font-medium">Misc</th>
                  <th className="px-4 py-3 font-medium">Total</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100 bg-white">
                {vehicleMaintenanceCosts.map((item) => (
                  <tr key={item.vehicleId} className="text-sm text-ink-700">
                    <td className="px-4 py-3 font-medium text-ink-900">
                      {item.vehicleDisplayName || "-"}
                    </td>
                    <td className="px-4 py-3">{formatCurrency(item.partCostTotal)}</td>
                    <td className="px-4 py-3">{formatCurrency(item.laborCostTotal)}</td>
                    <td className="px-4 py-3">{formatCurrency(item.externalServiceCostTotal)}</td>
                    <td className="px-4 py-3">{formatCurrency(item.miscCostTotal)}</td>
                    <td className="px-4 py-3 font-semibold text-ink-900">
                      {formatCurrency(item.grandTotal)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {vehicleMaintenanceCosts.length === 0 ? (
            <div className="mt-4">
              <EmptyState message="No vehicle maintenance costs were returned by the API." />
            </div>
          ) : null}
        </ReportCard>

        <ReportCard title="Part Consumption" subtitle="Usage and cost totals">
          <div className="overflow-x-auto rounded-2xl border border-ink-200">
            <table className="min-w-[480px] divide-y divide-ink-200">
              <thead className="bg-ink-50">
                <tr className="text-left text-sm text-ink-500">
                  <th className="px-4 py-3 font-medium">Part</th>
                  <th className="px-4 py-3 font-medium">Quantity</th>
                  <th className="px-4 py-3 font-medium">Cost</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100 bg-white">
                {partConsumption.map((item) => (
                  <tr key={item.partId} className="text-sm text-ink-700">
                    <td className="px-4 py-3 font-medium text-ink-900">
                      {item.partNumber} - {item.partName}
                    </td>
                    <td className="px-4 py-3">{item.totalQuantityUsed ?? 0}</td>
                    <td className="px-4 py-3 font-semibold text-ink-900">
                      {formatCurrency(item.totalCost)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {partConsumption.length === 0 ? (
            <div className="mt-4">
              <EmptyState message="No part consumption records were returned by the API." />
            </div>
          ) : null}
        </ReportCard>
      </section>
    </div>
  );
}

function ReportCard({ title, subtitle, children }) {
  return (
    <section className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
      <div className="flex flex-col gap-2">
        <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">{title}</p>
        <p className="text-sm text-ink-500">{subtitle}</p>
      </div>
      <div className="mt-6">{children}</div>
    </section>
  );
}

function HeroStat({ label, value, accent = false }) {
  return (
    <div
      className={`rounded-2xl px-4 py-3 ${
        accent ? "bg-amber-500/20 text-amber-100" : "bg-white/10 text-slate-200"
      }`}
    >
      <p className="text-xs uppercase tracking-[0.18em] opacity-80">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-white">{value}</p>
    </div>
  );
}

function SummaryTile({ label, value, accent = false }) {
  return (
    <div
      className={`rounded-2xl border px-4 py-4 shadow-soft ${
        accent ? "border-amber-200 bg-amber-50" : "border-ink-200 bg-white/90"
      }`}
    >
      <p className="text-sm text-ink-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-ink-900">{value}</p>
    </div>
  );
}

function TotalBadge({ label, value }) {
  return (
    <div className="rounded-2xl bg-ink-50 px-4 py-4">
      <p className="text-sm text-ink-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-ink-900">{value}</p>
    </div>
  );
}

function StatusRow({ label, value, tone }) {
  const toneClass = tone === "amber" ? "border-amber-200 bg-amber-50" : "border-sky-200 bg-sky-50";
  return (
    <div className={`rounded-2xl border px-4 py-4 ${toneClass}`}>
      <p className="text-sm text-ink-500">{label}</p>
      <p className="mt-2 text-xl font-semibold text-ink-900">{value}</p>
    </div>
  );
}

function EmptyState({ message }) {
  return (
    <div className="rounded-2xl border border-dashed border-ink-200 bg-ink-50 px-4 py-6 text-center text-sm text-ink-500">
      {message}
    </div>
  );
}

function formatCurrency(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }

  const numericValue = Number(value);
  if (Number.isNaN(numericValue)) {
    return String(value);
  }

  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 2,
  }).format(numericValue);
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

function toTitleCase(value) {
  if (!value) {
    return "-";
  }

  return value.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
}

export default ReportsPage;
