const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export interface CheckInResponse {
  ticketId: number;
  plateNumber: string;
  checkInTime: string;
  status: string;
}

export interface CheckOutPreviewResponse {
  ticketId: number;
  plateNumber: string;
  checkInTime: string;
  checkOutTime: string;
  totalPrice: number;
}

export interface ConfirmCheckoutResponse {
  ticketId: number;
  plateNumber: string;
  checkInTime: string;
  checkOutTime: string;
  totalPrice: number;
  status: string;
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const errorMessage = await res.text();
    throw new Error(errorMessage || `Request failed with status ${res.status}`);
  }
  return res.json() as Promise<T>;
}

/**
 * Call POST /api/checkin
 */
export async function checkIn(plateNumber: string): Promise<CheckInResponse> {
  const res = await fetch(`${API_BASE_URL}/checkin`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ plateNumber }),
  });
  return handleResponse<CheckInResponse>(res);
}

/**
 * Call POST /api/checkout/preview
 */
export async function checkOutPreview(
  plateNumber: string
): Promise<CheckOutPreviewResponse> {
  const res = await fetch(`${API_BASE_URL}/checkout/preview`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ plateNumber }),
  });
  return handleResponse<CheckOutPreviewResponse>(res);
}

/**
 * Call POST /api/checkout/confirm
 * ticketId from previewCheckOut
 */
export async function confirmCheckOut(
  ticketId: number
): Promise<ConfirmCheckoutResponse> {
  const res = await fetch(`${API_BASE_URL}/checkout/confirm`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ ticketId }),
  });
  return handleResponse<ConfirmCheckoutResponse>(res);
}
