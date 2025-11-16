import { useEffect, useState } from "react";
import { checkIn, type CheckInResponse } from "../api/ticketApi";

function formatDateTime(date: string | Date): string {
  let d: Date;
  if (typeof date === "string") {
    const normalized = date.endsWith("Z") || date.includes("+")
      ? date
      : `${date}Z`;
    d = new Date(normalized);
  } else {
    d = date;
  }

  const day = d.getDate().toString().padStart(2, "0");
  const monthNames = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "Mei",
    "Jun",
    "Jul",
    "Agu",
    "Sep",
    "Okt",
    "Nov",
    "Des",
  ];
  const month = monthNames[d.getMonth()];
  const year = d.getFullYear();

  const hh = d.getHours().toString().padStart(2, "0");
  const mm = d.getMinutes().toString().padStart(2, "0");
  const ss = d.getSeconds().toString().padStart(2, "0");

  return `${day} ${month} ${year} ${hh}:${mm}:${ss}`;
}

export default function CheckInPage() {
  const [now, setNow] = useState<Date>(new Date());

  const [plateNumber, setPlateNumber] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [lastTicket, setLastTicket] = useState<CheckInResponse | null>(null);

  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(id);
  }, []);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    const cleanPlate = plateNumber.trim().toUpperCase();
    if (!cleanPlate) {
      setErrorMessage("Plate number is required.");
      return;
    }

    try {
      setLoading(true);
      const res = await checkIn(cleanPlate);
      setLastTicket(res);
      setSuccessMessage(
        `Check-in ${res.plateNumber} successful at ${formatDateTime(
          res.checkInTime
        )}`
      );
      setPlateNumber("");
    } catch (err) {
      let message = err instanceof Error ? err.message : "Unknown error";

      if (message.toLowerCase().includes("already")) {
        message =
          "This vehicle is already inside. Please process Check-out instead.";
      }

      setErrorMessage(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[1.1fr,1fr] gap-6">
      {/* LEFT CAMERA PANEL */}
      <section className="bg-white/80 backdrop-blur rounded-3xl shadow-lg p-5 border border-slate-200">
        <div className="bg-slate-50 border border-dashed border-slate-300 rounded-2xl h-72 flex items-center justify-center flex-col">
          <div className="w-20 h-20 rounded-2xl border-2 border-slate-300 flex items-center justify-center bg-white shadow-sm">
            <span className="text-3xl">📷</span>
          </div>
          <p className="mt-3 text-xs text-slate-500">
            Camera preview placeholder (Entry)
          </p>
        </div>
        <div className="mt-3 flex items-center justify-between text-xs text-slate-500">
          <span className="uppercase tracking-wide">Entry Camera</span>
          <span>Lane 1</span>
        </div>
      </section>

      {/* RIGHT PANEL */}
      <section className="flex flex-col gap-4">
        {/* HEADER INFO */}
        <div className="bg-white/80 backdrop-blur rounded-3xl shadow-md border border-slate-200 px-5 py-4">
          <div className="flex items-start justify-between gap-4">
            <div className="text-xs text-slate-500 space-y-1">
              <div className="flex justify-between gap-4">
                <span>Gate</span>
                <span className="font-medium text-slate-800">Entrance A</span>
              </div>
              <div className="flex justify-between gap-4">
                <span>Parking Type</span>
                <span className="text-slate-700">Conventional</span>
              </div>
              <div className="flex justify-between gap-4">
                <span>Last Ticket</span>
                <span className="text-slate-700">
                  {lastTicket ? lastTicket.plateNumber : "-"}
                </span>
              </div>
            </div>

            <div className="text-right">
              <p className="text-xs uppercase text-slate-400 tracking-wide">
                Current Time
              </p>
              <p className="text-sm font-semibold text-slate-800">
                {formatDateTime(now)}
              </p>
            </div>
          </div>
        </div>

        {/* FORM CARD */}
        <div className="bg-white/90 backdrop-blur rounded-3xl shadow-md border border-slate-200 px-6 py-5">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <div className="flex items-center justify-between">
                <label className="text-sm text-slate-700 font-medium">
                  Vehicle Plate Number
                </label>
                <span className="text-[11px] text-slate-400">
                  Example: B1234DE
                </span>
              </div>
              <input
                className="w-full mt-2 text-center text-2xl tracking-[0.35em] border-2 border-rose-200 rounded-2xl py-3 focus:border-rose-500 focus:ring-2 focus:ring-rose-200 outline-none bg-rose-50/40 placeholder:text-slate-300"
                placeholder="B1234DE"
                value={plateNumber}
                onChange={(e) => {
                  const raw = e.target.value.toUpperCase();
                  const filtered = raw.replace(/[^A-Z0-9]/g, "");
                  setPlateNumber(filtered);
                }}
              />
              <p className="mt-1 text-[11px] text-slate-400">
                Press <span className="font-semibold">Enter</span> or click{" "}
                <span className="font-semibold">Submit Ticket</span>.
              </p>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`mt-2 w-full py-3 rounded-2xl text-white font-semibold shadow-sm transition-transform ${
                loading
                  ? "bg-rose-300 cursor-not-allowed"
                  : "bg-rose-500 hover:bg-rose-600 hover:-translate-y-[1px]"
              }`}
            >
              {loading ? "Submitting..." : "Submit Ticket"}
            </button>
          </form>

          {errorMessage && (
            <div className="mt-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl px-3 py-2">
              {errorMessage}
            </div>
          )}

          {successMessage && (
            <div className="mt-4 text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-xl px-3 py-2">
              {successMessage}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
