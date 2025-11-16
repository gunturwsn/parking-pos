import { useState } from "react";
import {
  checkOutPreview,
  confirmCheckOut,
  type CheckOutPreviewResponse,
} from "../api/ticketApi";

function formatDateTime(date: string | Date): string {
  let d: Date;
  if (typeof date === "string") {
    const normalized =
      date.endsWith("Z") || date.includes("+") ? date : `${date}Z`;
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

function calcDuration(start?: string, end?: string): string {
  if (!start || !end) return "-";
  const s = new Date(start);
  const e = new Date(end);
  const diffMs = e.getTime() - s.getTime();
  if (diffMs <= 0) return "0 minutes";
  const totalMinutes = Math.floor(diffMs / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (!hours) return `${minutes} minutes`;
  return `${hours} hours ${minutes} minutes`;
}

export default function CheckOutPage() {
  const [plateNumber, setPlateNumber] = useState("");
  const [loading, setLoading] = useState(false);
  const [ticket, setTicket] = useState<CheckOutPreviewResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  async function handleLoadTicket() {
    setErrorMessage(null);
    setSuccessMessage(null);

    const cleanPlate = plateNumber.trim().toUpperCase();
    if (!cleanPlate) {
      setErrorMessage("Plate number is required.");
      return;
    }

    try {
      setLoading(true);
      const result = await checkOutPreview(cleanPlate);
      setTicket(result);
    } catch (err) {
      let message = err instanceof Error ? err.message : "Unknown error";

      if (message.toLowerCase().includes("not found")) {
        message = "Ticket not found or already completed.";
      }

      setErrorMessage(message);
      setTicket(null);
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirm() {
    if (!ticket) return;

    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      setLoading(true);
      const result = await confirmCheckOut(ticket.ticketId);
      setSuccessMessage(
        `Payment for ${
          result.plateNumber
        } completed. Total: Rp ${result.totalPrice.toLocaleString("id-ID")}`
      );
      setTicket(null);
      setPlateNumber("");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unknown error";
      setErrorMessage(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[1.1fr,1fr] gap-6">
      {/* LEFT CAMERA GRID */}
      <section className="grid grid-cols-2 gap-4">
        {["Entry Cam", "Exit Cam", "Face Entry", "Face Exit"].map((label) => (
          <div
            key={label}
            className="bg-white/80 backdrop-blur rounded-3xl shadow-md p-4 border border-slate-200"
          >
            <div className="bg-slate-50 border border-dashed border-slate-300 rounded-2xl h-32 flex items-center justify-center">
              <div className="w-14 h-14 rounded-2xl border border-slate-300 flex items-center justify-center bg-white shadow-sm opacity-70">
                <span className="text-2xl">📷</span>
              </div>
            </div>
            <p className="mt-2 text-xs text-slate-500 uppercase tracking-wide">
              {label}
            </p>
          </div>
        ))}
      </section>

      {/* RIGHT PANEL */}
      <section className="flex flex-col gap-4">
        {/* TICKET SUMMARY */}
        <div className="bg-white/90 backdrop-blur rounded-3xl shadow-md border px-6 py-5">
          <div className="flex items-center justify-between mb-3">
            <div>
              <p className="text-xs uppercase text-slate-400 tracking-wide">
                Ticket Summary
              </p>
              <p className="text-sm text-slate-700">
                Preview before completing payment
              </p>
            </div>
            {ticket && (
              <span className="inline-flex items-center rounded-full bg-emerald-50 px-3 py-1 text-[11px] font-medium text-emerald-700 border border-emerald-100">
                ACTIVE
              </span>
            )}
          </div>

          {!ticket ? (
            <p className="text-sm text-slate-500">
              No ticket loaded. Enter a plate number below and click{" "}
              <span className="font-medium text-slate-700">Load Ticket</span>.
            </p>
          ) : (
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-500">Plate Number</span>
                <span className="font-semibold tracking-[0.25em]">
                  {ticket.plateNumber}
                </span>
              </div>

              <div className="flex justify-between">
                <span className="text-slate-500">Check-in</span>
                <span>{formatDateTime(ticket.checkInTime)}</span>
              </div>

              <div className="flex justify-between">
                <span className="text-slate-500">Check-out (now)</span>
                <span>{formatDateTime(ticket.checkOutTime)}</span>
              </div>

              <div className="flex justify-between">
                <span className="text-slate-500">Duration</span>
                <span>
                  {calcDuration(ticket.checkInTime, ticket.checkOutTime)}
                </span>
              </div>

              <div className="flex justify-between pt-2 border-t mt-2">
                <span className="font-semibold text-slate-700">
                  Total Price
                </span>
                <span className="font-semibold text-rose-600 text-lg">
                  Rp {ticket.totalPrice.toLocaleString("id-ID")}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* SEARCH FORM */}
        <div className="bg-white/90 backdrop-blur rounded-3xl shadow-md border px-6 py-5">
          <label className="text-sm text-slate-700 font-medium">
            Vehicle Plate Number
          </label>
          <input
            className="w-full mt-2 text-center text-2xl border-2 border-rose-200 rounded-2xl py-3 focus:border-rose-500 focus:ring-2 focus:ring-rose-200 bg-rose-50/40 placeholder:text-slate-300"
            placeholder="B1234DE"
            value={plateNumber}
            onChange={(e) => {
              const raw = e.target.value.toUpperCase();
              const filtered = raw.replace(/[^A-Z0-9]/g, "");
              setPlateNumber(filtered);
            }}
          />

          <button
            onClick={handleLoadTicket}
            disabled={loading}
            className={`mt-4 w-full py-3 rounded-2xl font-medium text-white transition-transform ${
              loading
                ? "bg-slate-300 cursor-not-allowed"
                : "bg-slate-900 hover:bg-slate-800 hover:-translate-y-[1px]"
            }`}
          >
            {loading ? "Loading..." : "Load Ticket"}
          </button>
        </div>

        {/* CONFIRM BUTTON */}
        <div className="bg-white/90 backdrop-blur rounded-3xl shadow-md border px-6 py-5">
          <div className="flex justify-between items-center mb-3">
            <span className="text-sm text-slate-600">
              Complete this ticket when payment is received
            </span>
          </div>
          <button
            onClick={handleConfirm}
            disabled={!ticket || loading}
            className={`w-full py-3 rounded-2xl font-semibold text-white transition-transform ${
              !ticket || loading
                ? "bg-rose-200 cursor-not-allowed"
                : "bg-rose-500 hover:bg-rose-600 hover:-translate-y-[1px]"
            }`}
          >
            Pay for Rp.{" "}
            {ticket
              ? ticket.totalPrice.toLocaleString("id-ID") + ",00"
              : "0.00"}
          </button>

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
