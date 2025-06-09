const API_BASE_URL = 'http://localhost:4004/api/billing'; // Assuming Vite proxy is set up for /api -> API Gateway

// Helper function for handling API responses
const handleResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: response.statusText || 'An unknown error occurred' }));
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
  if (response.status === 204) {
    return null; 
  }
  return response.json();
};

// Helper function to create headers, including Authorization if token is provided
const createHeaders = () => {
  const headers = {
    'Content-Type': 'application/json',
  };
  // No authorization header needed for billing service
  return headers;
};

/**
 * Fetches all payment attempts for a given patient.
 * Endpoint: GET /api/billing/payments/my-attempts?patientId={patientId}
 */
export const apiGetMyPaymentAttempts = async (patientId) => {
  if (!patientId) {
    throw new Error("Patient ID is required to fetch payment attempts.");
  }
  const response = await fetch(`${API_BASE_URL}/payments/my-attempts?patientId=${patientId}`, {
    method: 'GET',
    headers: createHeaders(),
  });
  return handleResponse(response);
};

/**
 * Creates a Stripe checkout session for a specific visit fee.
 * Endpoint: POST /api/billing/payments/visit/{visitId}/create-checkout-session
 * Note: patientId is now sent in the request body.
 */
export const apiCreateCheckoutSessionForVisitFee = async (visitId, patientId) => {
  const response = await fetch(`${API_BASE_URL}/payments/visit/${visitId}/create-checkout-session`, {
    method: 'POST',
    headers: createHeaders(),
    body: JSON.stringify({ patientId }),
  });
  return handleResponse(response);
}; 