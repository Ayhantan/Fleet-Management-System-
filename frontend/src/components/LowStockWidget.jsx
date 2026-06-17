import { Link } from "react-router-dom";
import { formatNumber } from "../utils/formatters.js";

function LowStockWidget({ data }) {
  const items = data?.items ?? [];

  return (
    <section className="rounded-3xl border border-ink-200 bg-white/90 p-6 shadow-soft">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
            Inventory Alert
          </p>
          <h3 className="mt-2 text-xl font-semibold text-ink-900">Low Stock Items</h3>
          <p className="mt-1 text-sm text-ink-500">
            {formatNumber(data?.lowStockItemCount ?? 0)} item needs replenishment.
          </p>
        </div>
        <Link
          to="/inventory"
          className="rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50"
        >
          Open Inventory
        </Link>
      </div>

      <div className="mt-6 space-y-3">
        {items.length === 0 ? (
          <div className="rounded-2xl bg-ink-50 px-4 py-5 text-sm text-ink-500">
            No low stock items were returned by the API.
          </div>
        ) : (
          items.slice(0, 5).map((item) => (
            <div
              key={item.inventoryItemId}
              className="flex items-center justify-between gap-4 rounded-2xl border border-ink-100 px-4 py-4"
            >
              <div className="min-w-0">
                <p className="truncate text-sm font-semibold text-ink-900">{item.partName}</p>
                <p className="text-sm text-ink-500">
                  {item.partNumber}
                  {item.location ? ` • ${item.location}` : ""}
                </p>
              </div>
              <div className="text-right text-sm">
                <p className="font-semibold text-rose-600">{formatNumber(item.currentQuantity)}</p>
                <p className="text-ink-500">Min: {formatNumber(item.minimumStockLevel)}</p>
              </div>
            </div>
          ))
        )}
      </div>
    </section>
  );
}

export default LowStockWidget;
