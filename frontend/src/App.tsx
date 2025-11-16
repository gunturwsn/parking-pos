import { Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "./components/Layout/AppLayout";
import CheckInPage from "./pages/CheckInPage";
import CheckOutPage from "./pages/CheckOutPage";

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/check-in" replace />} />
        <Route path="/check-in" element={<CheckInPage />} />
        <Route path="/check-out" element={<CheckOutPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/check-in" replace />} />
    </Routes>
  );
}

export default App;
