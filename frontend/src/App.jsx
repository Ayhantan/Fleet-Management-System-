import { Navigate, Route, Routes } from "react-router-dom";
import AppShell from "./components/AppShell.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import InventoryPage from "./pages/InventoryPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import MaintenancePage from "./pages/MaintenancePage.jsx";
import RegisterPage from "./pages/RegisterPage.jsx";
import ReportsPage from "./pages/ReportsPage.jsx";
import VehiclesPage from "./pages/VehiclesPage.jsx";
import WorkOrdersPage from "./pages/WorkOrdersPage.jsx";
import { useAuth } from "./hooks/useAuth.jsx";

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />
      <Route
        path="/register"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <RegisterPage />}
      />
      <Route
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/maintenance" element={<MaintenancePage />} />
        <Route path="/inventory" element={<InventoryPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/vehicles" element={<VehiclesPage />} />
        <Route path="/work-orders" element={<WorkOrdersPage />} />
      </Route>
      <Route
        path="*"
        element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />}
      />
    </Routes>
  );
}

export default App;
