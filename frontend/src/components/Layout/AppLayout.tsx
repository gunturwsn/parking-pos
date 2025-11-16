import { NavLink, Outlet } from "react-router-dom";

export default function AppLayout() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-100 via-slate-50 to-slate-200">
      {/* NAVBAR */}
      <nav className="sticky top-0 z-20 bg-white/90 backdrop-blur border-b border-slate-200 px-6 py-3 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-rose-500 flex items-center justify-center text-white text-sm font-bold">
            P
          </div>
          <div>
            <h1 className="text-base font-semibold text-slate-800">
              Parking POS
            </h1>
            <p className="text-[11px] text-slate-500 leading-tight">
              Check-in & Check-out Management
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 bg-slate-100 rounded-full p-1">
          <NavLink
            to="/check-in"
            className={({ isActive }) =>
              `px-4 py-2 rounded-full text-xs font-medium transition ${
                isActive
                  ? "bg-white shadow text-rose-600"
                  : "text-slate-600 hover:text-slate-800"
              }`
            }
          >
            Check-in
          </NavLink>

          <NavLink
            to="/check-out"
            className={({ isActive }) =>
              `px-4 py-2 rounded-full text-xs font-medium transition ${
                isActive
                  ? "bg-white shadow text-rose-600"
                  : "text-slate-600 hover:text-slate-800"
              }`
            }
          >
            Check-out
          </NavLink>
        </div>
      </nav>

      {/* CONTENT */}
      <main className="px-4 sm:px-6 py-6">
        <div className="max-w-6xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
