import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import LoadingState from "../components/LoadingState.jsx";
import LowStockWidget from "../components/LowStockWidget.jsx";
import StatCard from "../components/StatCard.jsx";
import { getDashboardSummary, getLowStockSummary } from "../services/reportService.js";

function DashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [lowStock, setLowStock] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadDashboard() {
      try {
        setLoading(true);
        setError("");

        const [dashboardResponse, lowStockResponse] = await Promise.all([
          getDashboardSummary(),
          getLowStockSummary(),
        ]);

        setDashboard(dashboardResponse);
        setLowStock(lowStockResponse);
      } catch (requestError) {
        setError(requestError.userMessage || "Dashboard data could not be loaded.");
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, []);

  if (loading) {
    return <LoadingState message="Dashboard data is loading." />;
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
        <p className="text-sm uppercase tracking-[0.25em] text-brand-100">Fleet Snapshot</p>
        <div className="mt-4 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h3 className="text-3xl font-semibold sm:text-4xl">Operational overview</h3>
            <p className="mt-3 max-w-2xl text-sm text-slate-300 sm:text-base">
              Track vehicles, maintenance backlog and stock pressure from a single responsive
              dashboard.
            </p>
          </div>
          <div className="rounded-2xl bg-white/10 px-5 py-4">
            <p className="text-xs uppercase tracking-[0.25em] text-slate-300">Open work orders</p>
            <p className="mt-2 text-3xl font-semibold">{dashboard?.openWorkOrders ?? 0}</p>
          </div>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        <DashboardLinkCard to="/vehicles">
          <StatCard label="Total Vehicles" value={dashboard?.totalVehicles} />
        </DashboardLinkCard>
        <DashboardLinkCard to="/work-orders">
          <StatCard label="Total Work Orders" value={dashboard?.totalWorkOrders} />
        </DashboardLinkCard>
        <DashboardLinkCard to="/maintenance">
          <StatCard
            label="Overdue Maintenance"
            value={dashboard?.overdueMaintenanceCount}
            accent="warning"
          />
        </DashboardLinkCard>
        <DashboardLinkCard to="/inventory">
          <StatCard label="Low Stock Items" value={dashboard?.lowStockItemCount} accent="warning" />
        </DashboardLinkCard>
        <DashboardLinkCard to="/reports">
          <StatCard
            label="Maintenance Cost"
            value={dashboard?.grandTotalMaintenanceCost}
            type="currency"
            accent="success"
          />
        </DashboardLinkCard>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.4fr_1fr]">
        <div className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
                Maintenance Flow
              </p>
              <h3 className="mt-2 text-xl font-semibold text-ink-900">Work order status</h3>
            </div>
            <Link
              to="/work-orders"
              className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
            >
              Open Work Orders
            </Link>
          </div>

          <div className="mt-6 grid gap-4 sm:grid-cols-2">
            <SummaryRow label="Open" value={dashboard?.openWorkOrders} />
            <SummaryRow label="Assigned" value={dashboard?.assignedWorkOrders} />
            <SummaryRow label="In Progress" value={dashboard?.inProgressWorkOrders} />
            <SummaryRow label="Completed" value={dashboard?.completedWorkOrders} />
            <SummaryRow label="Cancelled" value={dashboard?.cancelledWorkOrders} />
            <SummaryRow label="Upcoming Maintenance" value={dashboard?.upcomingMaintenanceCount} />
          </div>
        </div>

        <LowStockWidget data={lowStock} />
      </section>
    </div>
  );
}

function DashboardLinkCard({ to, children }) {
  return (
    <Link to={to} className="block rounded-[1.75rem] transition hover:-translate-y-0.5">
      {children}
    </Link>
  );
}

function SummaryRow({ label, value }) {
  return (
    <div className="rounded-2xl bg-ink-50 px-4 py-4">
      <p className="text-sm text-ink-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-ink-900">{value ?? 0}</p>
    </div>
  );
}

export default DashboardPage;
