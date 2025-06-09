import React, { useState, useEffect } from 'react';
import axios from 'axios';

const PatientList = ({ onPatientSelect, refreshList }) => {
  const [patients, setPatients] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPatients = async () => {
      setLoading(true);
      try {
        const response = await axios.get('http://localhost:4004/api/patients');
        setPatients(response.data);
        setError(null);
      } catch (err) {
        setError('Failed to fetch patients. Ensure the backend is running.');
        console.error('Error fetching patients:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchPatients();
  }, [refreshList]);

  if (loading) return <p>Loading patients...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div className="patient-list-container">
      <h2>Patients</h2>
      {patients.length === 0 && !loading && <p>No patients found. Add one!</p>}
      <ul>
        {patients.map((patient) => (
          <li key={patient.id} onClick={() => onPatientSelect(patient)}>
            <strong>{patient.name}</strong><br />
            Email: {patient.email}<br />
            Address: {patient.address}<br />
            DOB: {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleDateString() : 'N/A'}<br />
            Problem: {patient.problem || 'N/A'}<br />
            Location: {patient.location || 'N/A'}<br />
            Consultation Fee: ${patient.consultationFee ? parseFloat(patient.consultationFee).toFixed(2) : 'N/A'}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default PatientList; 