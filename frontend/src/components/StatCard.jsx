import { formatCurrency, formatNumber } from "../utils/formatters.js";

function StatCard({ label, value, type = "number", accent = "default" }) {
  const accentClass =
    accent === "success"
      ? "bg-brand-50 text-brand-700"
      : accent === "warning"
        ? "bg-amber-50 text-amber-700"
        : "bg-ink-100 text-ink-700";

  const formattedValue =
    type === "currency" ? formatCurrency(value) : formatNumber(value);

  return (
    <article className="rounded-3xl border border-ink-200 bg-white/90 p-5 shadow-soft">
      <div className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${accentClass}`}>
        {label}
      </div>
      <p className="mt-5 text-3xl font-semibold tracking-tight text-ink-900">{formattedValue}</p>
    </article>
  );
}

export default StatCard;
