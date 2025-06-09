import React from 'react';
import PatientList from '../components/PatientList';
import '../App.css';

const PatientsPage = ({ setSelectedPatient, refreshListTrigger, navigate }) => {
  
  const handlePatientSelect = (patient) => {
    setSelectedPatient(patient);
    // Potentially navigate back to the form on the HomePage or a dedicated edit page
    // For now, selecting a patient here makes it available for the form on HomePage if user navigates back
    // Or, if AddPatientForm was also on this page, it would populate.
    // Consider navigating to an edit route: navigate(`/edit-patient/${patient.id}`);
    // For now, we assume the AddPatientForm is on HomePage and will pick up selectedPatient
    navigate('/'); // Navigate to home page where the form is, to edit the selected patient
  };

  return (
    <div className="container">
      <h1>All Patients</h1>
      <PatientList 
        onPatientSelect={handlePatientSelect} 
        refreshList={refreshListTrigger} 
      />
    </div>
  );
};

export default PatientsPage; 