import { useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth.jsx";

const navigationItems = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/maintenance", label: "Maintenance" },
  { to: "/inventory", label: "Inventory" },
  { to: "/reports", label: "Reports" },
  { to: "/vehicles", label: "Vehicles" },
  { to: "/work-orders", label: "Work Orders" },
];

const pageMeta = {
  "/dashboard": { eyebrow: "Overview", title: "Dashboard" },
  "/maintenance": { eyebrow: "Planning", title: "Maintenance" },
  "/inventory": { eyebrow: "Stock", title: "Inventory" },
  "/reports": { eyebrow: "Insights", title: "Reports" },
  "/vehicles": { eyebrow: "Fleet", title: "Vehicles" },
  "/work-orders": { eyebrow: "Maintenance", title: "Work Orders" },
};

function AppShell() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const currentPage = pageMeta[location.pathname] || pageMeta["/dashboard"];
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  function handleMobileNavigate() {
    setMobileMenuOpen(false);
  }

  return (
    <div className="min-h-screen lg:flex">
      <aside className="hidden w-72 border-r border-ink-200 bg-white/85 p-6 backdrop-blur lg:flex lg:flex-col">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.3em] text-brand-600">
            Fleet System
          </p>
          <h1 className="mt-3 text-2xl font-semibold text-ink-900">Operations Panel</h1>
          <p className="mt-2 text-sm text-ink-500">
            Vehicles, work orders and stock visibility in one place.
          </p>
        </div>

        <nav className="mt-10 space-y-2">
          {navigationItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                [
                  "block rounded-2xl px-4 py-3 text-sm font-medium transition",
                  isActive
                    ? "bg-brand-600 text-white shadow-soft"
                    : "text-ink-600 hover:bg-ink-100 hover:text-ink-900",
                ].join(" ")
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="mt-auto rounded-2xl border border-ink-200 bg-ink-50 p-4">
          <p className="text-xs uppercase tracking-[0.2em] text-ink-500">Signed in as</p>
          <p className="mt-2 text-sm font-semibold text-ink-900">{user?.username ?? "User"}</p>
          <p className="text-sm text-ink-500">{user?.email ?? "No email"}</p>
          <button
            type="button"
            onClick={logout}
            className="mt-4 w-full rounded-xl border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-white"
          >
            Sign out
          </button>
        </div>
      </aside>

      <div className="flex-1">
        <header className="sticky top-0 z-10 border-b border-ink-200 bg-white/85 backdrop-blur">
          <div className="mx-auto max-w-7xl px-4 py-4 sm:px-6 lg:px-8">
            <div className="flex items-start justify-between gap-4">
              <div className="min-w-0 flex-1">
                <p className="text-xs uppercase tracking-[0.25em] text-ink-500">
                  {currentPage.eyebrow}
                </p>
                <h2 className="text-lg font-semibold text-ink-900 sm:text-xl">{currentPage.title}</h2>
              </div>

              <button
                type="button"
                onClick={() => setMobileMenuOpen((current) => !current)}
                className="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-ink-200 bg-white text-ink-700 transition hover:bg-ink-50 lg:hidden"
                aria-label={mobileMenuOpen ? "Close menu" : "Open menu"}
                aria-expanded={mobileMenuOpen}
              >
                <span className="text-lg leading-none">{mobileMenuOpen ? "×" : "≡"}</span>
              </button>

              <button
                type="button"
                onClick={logout}
                className="hidden rounded-full border border-ink-200 px-4 py-2 text-sm font-medium text-ink-700 transition hover:bg-ink-50 lg:inline-flex"
              >
                Sign out
              </button>
            </div>

            {mobileMenuOpen ? (
              <div className="mt-4 lg:hidden">
                <div className="rounded-3xl border border-ink-200 bg-white/90 p-3 shadow-soft">
                  <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                    {navigationItems.map((item) => (
                      <NavLink
                        key={item.to}
                        to={item.to}
                        onClick={handleMobileNavigate}
                        className={({ isActive }) =>
                          [
                            "flex min-h-11 items-center justify-center rounded-2xl px-3 py-2 text-center text-sm font-medium transition",
                            isActive
                              ? "bg-brand-600 text-white"
                              : "bg-ink-100 text-ink-700 hover:bg-ink-200",
                          ].join(" ")
                        }
                      >
                        {item.label}
                      </NavLink>
                    ))}
                  </div>

                  <div className="mt-3 flex items-center justify-between rounded-2xl bg-ink-50 px-4 py-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold text-ink-900">
                        {user?.username ?? "User"}
                      </p>
                      <p className="truncate text-xs text-ink-500">{user?.email ?? "No email"}</p>
                    </div>
                    <button
                      type="button"
                      onClick={logout}
                      className="rounded-full border border-ink-200 bg-white px-4 py-2 text-sm font-medium text-ink-600 transition hover:bg-ink-50"
                    >
                      Sign out
                    </button>
                  </div>
                </div>
              </div>
            ) : null}
          </div>
        </header>

        <main className="mx-auto max-w-7xl overflow-x-hidden px-4 py-6 sm:px-6 lg:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppShell;
