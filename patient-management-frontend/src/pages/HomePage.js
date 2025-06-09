import React, { useState, useEffect } from 'react';
import axios from 'axios';
import AddPatientForm from '../components/AddPatientForm';
import { Link } from 'react-router-dom'; // For linking to full patient list or individual patients
import '../App.css';

const HomePage = ({ selectedPatient, setSelectedPatient, onFormSubmit, refreshListTrigger }) => {
  const [recentPatients, setRecentPatients] = useState([]);
  const [loadingRecent, setLoadingRecent] = useState(true);
  const [errorRecent, setErrorRecent] = useState(null);

  useEffect(() => {
    const fetchRecentPatients = async () => {
      setLoadingRecent(true);
      setErrorRecent(null);
      try {
        const response = await axios.get('http://localhost:4004/api/patients');
        // Sort patients by registeredDate descending, then take top 3
        const sortedPatients = response.data.sort((a, b) => new Date(b.registeredDate) - new Date(a.registeredDate));
        setRecentPatients(sortedPatients.slice(0, 3));
      } catch (err) {
        setErrorRecent('Failed to fetch recent patients.');
        console.error('Error fetching recent patients:', err);
      } finally {
        setLoadingRecent(false);
      }
    };

    fetchRecentPatients();
  }, [refreshListTrigger]); // Refreshes when the main list refreshes (e.g., after adding a patient)

  const clearSelection = () => {
    setSelectedPatient(null);
  };

  return (
    <div className="container">
      <div className="home-layout">
        <div className="form-section">
          <AddPatientForm 
            onFormSubmit={onFormSubmit} 
            selectedPatient={selectedPatient} 
            clearSelection={clearSelection} 
          />
        </div>
        <div className="recent-patients-section">
          <h2>Recently Added Patients</h2>
          {loadingRecent && <p>Loading recent patients...</p>}
          {errorRecent && <p style={{ color: 'red' }}>{errorRecent}</p>}
          {!loadingRecent && recentPatients.length === 0 && !errorRecent && <p>No patients found yet.</p>}
          {recentPatients.length > 0 && (
            <ul className="recent-patients-list">
              {recentPatients.map(patient => (
                <li key={patient.id} className="recent-patient-item">
                  <strong>{patient.name}</strong> ({patient.email})<br />
                  Problem: {patient.problem || 'N/A'}<br />
                  Registered: {patient.registeredDate ? new Date(patient.registeredDate).toLocaleDateString() : 'N/A'}
                  {/* Link to view full details if needed */}
                  {/* <Link to={`/patients/${patient.id}`}>View Details</Link> */}
                </li>
              ))}
            </ul>
          )}
           {recentPatients.length > 0 &&  <Link to="/patients" className="view-all-link">View All Patients &rarr;</Link>}
        </div>
      </div>
    </div>
  );
};

export default HomePage; 