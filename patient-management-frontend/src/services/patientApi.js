const API_BASE_URL = 'http://localhost:4004/api/patients'; // Assuming Vite proxy is set up for /api -> API Gateway

// Helper function for handling API responses
const handleResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: response.statusText || 'An unknown error occurred' }));
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
  if (response.status === 204) { // No Content
    return null; 
  }
  return response.json();
};

// Helper function to create headers, including Authorization if token is provided
const createHeaders = (accessToken) => {
  const headers = {
    'Content-Type': 'application/json',
  };
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }
  return headers;
};

/**
 * Logs in a patient.
 * Endpoint: POST /api/patients/auth/login
 * @param {object} loginData - { email, password }
 * @returns {object} - { token, patientId, name, email, ...other patient details including appointments and visits if login response provides them }
 */
export const apiPatientLogin = async (loginData) => {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: createHeaders(), // No token needed for login itself
    body: JSON.stringify(loginData),
  });
  return handleResponse(response); // Expects a response like PatientLoginResponseDTO
};

/**
 * Fetches detailed information for the currently logged-in patient.
 * Endpoint: GET /api/patients/me/details (Protected by JWT)
 * @param {string} accessToken - The JWT token for authorization.
 * @returns {object} Patient details including main appointment and list of visits with their appointments.
 */
export const apiGetPatientDetails = async (accessToken) => {
  const response = await fetch(`${API_BASE_URL}/me/details`, {
    method: 'GET',
    headers: createHeaders(accessToken),
  });
  return handleResponse(response);
}; 