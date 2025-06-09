import React, { useState, useEffect } from 'react';
import axios from 'axios';

const AddPatientForm = ({ onFormSubmit, selectedPatient, clearSelection }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [problem, setProblem] = useState('');
  const [location, setLocation] = useState('');
  const [consultationFee, setConsultationFee] = useState('');
  // registeredDate will be set to today for new patients, not directly editable in this form for simplicity

  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    if (selectedPatient) {
      setName(selectedPatient.name || '');
      setEmail(selectedPatient.email || '');
      setAddress(selectedPatient.address || '');
      setDateOfBirth(selectedPatient.dateOfBirth ? selectedPatient.dateOfBirth.split('T')[0] : '');
      setProblem(selectedPatient.problem || '');
      setLocation(selectedPatient.location || '');
      setConsultationFee(selectedPatient.consultationFee || '');
    } else {
      setName('');
      setEmail('');
      setAddress('');
      setDateOfBirth('');
      setProblem('');
      setLocation('');
      setConsultationFee('');
    }
  }, [selectedPatient]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!name.trim() || !email.trim() || !address.trim() || !dateOfBirth.trim() || 
        !problem.trim() || !location.trim() || !consultationFee.trim()) {
      setError('All fields (Name, Email, Address, Date of Birth, Problem, Location, Consultation Fee) are required.');
      return;
    }

    if (!/\S+@\S+\.\S+/.test(email)) {
        setError('Please enter a valid email address.');
        return;
    }

    if (!/^\d{4}-\d{2}-\d{2}$/.test(dateOfBirth)) {
        setError('Date of Birth must be in YYYY-MM-DD format.');
        return;
    }

    if (isNaN(parseFloat(consultationFee)) || !isFinite(consultationFee) || parseFloat(consultationFee) < 0) {
        setError('Consultation Fee must be a valid positive number.');
        return;
    }

    const patientData = {
      name,
      email,
      address,
      dateOfBirth,
      problem,
      location,
      consultationFee,
    };

    try {
      if (selectedPatient && selectedPatient.id) {
        await axios.put(`http://localhost:4004/api/patients/${selectedPatient.id}`, patientData);
        setSuccess('Patient updated successfully!');
      } else {
        patientData.registeredDate = new Date().toISOString().split('T')[0];
        await axios.post('http://localhost:4004/api/patients', patientData);
        setSuccess('Patient added successfully!');
      }
      onFormSubmit(); 
      setName('');
      setEmail('');
      setAddress('');
      setDateOfBirth('');
      setProblem('');
      setLocation('');
      setConsultationFee('');
      clearSelection(); 
    } catch (err) {
      let errorMessage = 'Failed to save patient. Ensure the backend is running.';
      if (err.response && err.response.data) {
        const backendErrors = err.response.data;
        if (typeof backendErrors === 'object' && backendErrors !== null) {
          errorMessage = Object.entries(backendErrors)
            .map(([field, message]) => `${field}: ${message}`)
            .join('; ');
        } else if (typeof backendErrors === 'string') {
          errorMessage = backendErrors;
        }
      }
      setError(errorMessage);
      console.error('Error saving patient:', err);
    }
  };

  return (
    <div className="patient-form-container">
      <h2>{selectedPatient ? 'Edit Patient' : 'Add New Patient'}</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="name">Name:</label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Patient Name"
            required
          />
        </div>
        <div>
          <label htmlFor="email">Email:</label>
          <input
            type="email" 
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="patient@example.com"
            required
          />
        </div>
        <div>
          <label htmlFor="address">Address:</label>
          <input
            type="text"
            id="address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="123 Main St, City"
            required
          />
        </div>
        <div>
          <label htmlFor="dateOfBirth">Date of Birth (YYYY-MM-DD):</label>
          <input
            type="text" 
            id="dateOfBirth"
            value={dateOfBirth}
            onChange={(e) => setDateOfBirth(e.target.value)}
            placeholder="YYYY-MM-DD"
            required
          />
        </div>
        <div>
          <label htmlFor="problem">Problem:</label>
          <input
            type="text"
            id="problem"
            value={problem}
            onChange={(e) => setProblem(e.target.value)}
            placeholder="e.g., Flu, Check-up"
            required
          />
        </div>
        <div>
          <label htmlFor="location">Location (City/Area):</label>
          <input
            type="text"
            id="location"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="e.g., New York, Downtown"
            required
          />
        </div>
        <div>
          <label htmlFor="consultationFee">Consultation Fee:</label>
          <input
            type="number"
            id="consultationFee"
            value={consultationFee}
            onChange={(e) => setConsultationFee(e.target.value)}
            placeholder="e.g., 50.00"
            step="0.01"
            min="0"
            required
          />
        </div>
        <button type="submit">{selectedPatient ? 'Update Patient' : 'Add Patient'}</button>
        {selectedPatient && (
          <button type="button" onClick={() => { 
            clearSelection(); 
            setName(''); setEmail(''); setAddress(''); setDateOfBirth('');
            setProblem(''); setLocation(''); setConsultationFee('');
          }} style={{ marginLeft: '10px' }}>
            Cancel Edit
          </button>
        )}
      </form>
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}
      {success && <p style={{ color: 'green' }}>{success}</p>}
    </div>
  );
};

export default AddPatientForm; 