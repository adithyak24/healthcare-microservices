const API_BASE_URL = 'http://localhost:4004/api'; // Proxied by Vite to API Gateway

// Helper function for handling API responses
const handleResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: response.statusText || 'An unknown error occurred' }));
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
  // For 204 No Content, response.json() will fail. Handle it by returning null or an empty object.
  if (response.status === 204) {
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

// ================= PATIENT API CALLS =================

/**
 * Adds a new patient.
 * The actual endpoint on patient-service will be POST /patients
 * API Gateway will route /api/patients -> patient-service/patients
 */
export const apiAddPatient = async (patientData, accessToken) => {
  const response = await fetch(`${API_BASE_URL}/patients`, {
    method: 'POST',
    headers: createHeaders(accessToken),
    body: JSON.stringify(patientData),
  });
  return handleResponse(response);
};

/**
 * Fetches all patients for the receptionist portal with pagination.
 * The actual endpoint on patient-service will be GET /patients/all
 * API Gateway will route /api/patients/all -> patient-service/patients/all
 */
export const apiGetAllPatientsForReceptionist = async (accessToken, page = 0, size = 20, sortBy = 'registeredDate', sortDir = 'desc') => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sortBy,
    sortDir
  });
  
  const response = await fetch(`${API_BASE_URL}/patients/all?${params}`, {
    headers: createHeaders(accessToken),
  });
  return handleResponse(response);
};

/**
 * Fetches the 5 most recently registered patients.
 * The actual endpoint on patient-service will be GET /patients/recent
 * API Gateway will route /api/patients/recent -> patient-service/patients/recent
 */
export const apiGetRecentPatients = async (accessToken) => {
  const response = await fetch(`${API_BASE_URL}/patients/recent`, {
    headers: createHeaders(accessToken),
  });
  return handleResponse(response);
};

// ================= PATIENT VISIT API CALLS =================

/**
 * Adds a new visit for a patient.
 * Endpoint: POST /api/patients/{patientId}/visits
 */
export const apiAddPatientVisit = async (patientId, visitData, accessToken) => {
  const response = await fetch(`${API_BASE_URL}/patients/${patientId}/visits`, {
    method: 'POST',
    headers: createHeaders(accessToken),
    body: JSON.stringify(visitData),
  });
  return handleResponse(response);
};

// ================= APPOINTMENT API CALLS =================

/**
 * Schedules an appointment for a patient's initial consultation.
 * Endpoint: PUT /api/patients/{patientId}/appointment
 */
export const apiScheduleAppointment = async (patientId, appointmentData, accessToken) => {
  const response = await fetch(`${API_BASE_URL}/patients/${patientId}/appointment`, {
    method: 'PUT',
    headers: createHeaders(accessToken),
    body: JSON.stringify(appointmentData),
  });
  return handleResponse(response);
};

/**
 * Schedules an appointment for a specific visit of a patient.
 * Endpoint: POST /api/patients/{patientId}/visits/{visitId}/schedule-appointment
 */
export const apiScheduleVisitAppointment = async (patientId, visitId, appointmentData, accessToken) => {
  const response = await fetch(`${API_BASE_URL}/patients/${patientId}/visits/${visitId}/schedule-appointment`, {
    method: 'POST',
    headers: createHeaders(accessToken),
    body: JSON.stringify(appointmentData),
  });
  return handleResponse(response);
}; 