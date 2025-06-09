import React, { useState, useEffect } from 'react';
import {
  Button, Modal, Box, TextField, Typography, CircularProgress, Alert
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'; // Using DateTimePicker for date and time
import { isValid, format } from 'date-fns';

const modalStyle = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 450,
  bgcolor: 'background.paper',
  border: '1px solid #ccc',
  boxShadow: 24,
  p: 4,
  borderRadius: '8px',
};

function ScheduleVisitAppointmentModal({ open, onClose, patient, visit, onSubmitVisitAppointment, submissionError, isSubmitting }) {
  const [doctorName, setDoctorName] = useState('');
  const [appointmentDateTime, setAppointmentDateTime] = useState(null);
  const [internalError, setInternalError] = useState('');

  useEffect(() => {
    if (open) {
      // Reset form when modal opens or patient/visit changes
      setDoctorName('');
      setAppointmentDateTime(null);
      setInternalError('');
    }
  }, [open, patient, visit]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setInternalError('');

    if (!doctorName.trim()) {
      setInternalError('Doctor name is required.');
      return;
    }
    if (!appointmentDateTime || !isValid(appointmentDateTime)) {
      setInternalError('A valid appointment date and time are required.');
      return;
    }
    if (appointmentDateTime <= new Date()) {
      setInternalError('Appointment date and time must be in the future.');
      return;
    }

    // Check if appointment is during business hours (9 AM - 6 PM)
    const hour = appointmentDateTime.getHours();
    if (hour < 9 || hour >= 18) {
      setInternalError('Appointments can only be scheduled between 9:00 AM and 6:00 PM.');
      return;
    }

    // Check if appointment is on weekends
    const day = appointmentDateTime.getDay();
    if (day === 0 || day === 6) {
      setInternalError('Appointments can only be scheduled on weekdays (Monday - Friday).');
      return;
    }

    const appointmentData = {
      doctorName: doctorName.trim(),
      // Backend expects LocalDateTime format without timezone (e.g., "2024-03-15T10:30:00")
      appointmentDateTime: format(appointmentDateTime, "yyyy-MM-dd'T'HH:mm:ss"), 
    };

    await onSubmitVisitAppointment(patient.id, visit.id, appointmentData);
    // Parent component (AllPatientsPage) will handle closing modal on success and error display from API
  };

  if (!open || !patient || !visit) return null; // Don't render if not open or patient/visit not provided

  return (
    <Modal
      open={open}
      onClose={onClose}
      aria-labelledby="schedule-visit-appointment-modal-title"
    >
      <Box sx={modalStyle} component="form" onSubmit={handleSubmit}>
        <Typography id="schedule-visit-appointment-modal-title" variant="h6" component="h2" gutterBottom>
          Schedule Appointment for Visit
        </Typography>
        <Typography variant="subtitle1">Patient: {patient.name}</Typography>
        <Typography variant="body2" gutterBottom>
          Visit Date: {visit.visitDate ? new Date(visit.visitDate).toLocaleDateString() : 'N/A'}<br/>
          Problem: {visit.problemDescription}
        </Typography>
        
        {(internalError || submissionError) && (
            <Alert severity="error" sx={{ mb: 2 }}>
                {internalError || submissionError}
            </Alert>
        )}

        <TextField
          margin="normal"
          required
          fullWidth
          id="doctorName"
          label="Doctor Name"
          name="doctorName"
          value={doctorName}
          onChange={(e) => setDoctorName(e.target.value)}
          autoFocus
          disabled={isSubmitting}
        />

        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <DateTimePicker
            label="Appointment Date & Time"
            value={appointmentDateTime}
            onChange={(newValue) => setAppointmentDateTime(newValue)}
            slotProps={{ textField: { fullWidth: true, margin: 'normal', required: true } }}
            disablePast
            disabled={isSubmitting}
          />
        </LocalizationProvider>

        <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
          <Button 
            onClick={onClose} 
            sx={{ 
              mr: 1,
              '&:hover': {
                backgroundColor: 'transparent',
              },
            }} 
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button 
            type="submit" 
            variant="contained" 
            color="primary" 
            disabled={isSubmitting}
            sx={{
              '&:hover': {
                backgroundColor: 'primary.main',
              },
            }}
          >
            {isSubmitting ? <CircularProgress size={24} /> : 'Schedule Appointment'}
          </Button>
        </Box>

        <Box sx={{ mt: 2, p: 2, bgcolor: 'info.light', borderRadius: 2 }}>
          <Typography variant="body2" color="info.dark">
            <strong>Business Hours:</strong> Monday-Friday, 9:00 AM - 6:00 PM
          </Typography>
        </Box>
      </Box>
    </Modal>
  );
}

export default ScheduleVisitAppointmentModal; 