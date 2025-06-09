import React, { useState, useEffect } from 'react';
import {
  Button, Modal, Box, TextField, Typography, CircularProgress, Alert, Grid
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { Person as PersonIcon, CalendarToday as CalendarTodayIcon, AccessTime as AccessTimeIcon } from '@mui/icons-material';
import { isValid, formatISO, parseISO, addDays } from 'date-fns';

const modalStyle = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 500,
  bgcolor: 'background.paper',
  border: '1px solid #ccc',
  boxShadow: 24,
  p: 4,
  borderRadius: '12px',
  maxHeight: '90vh',
  overflowY: 'auto',
};

function AppointmentModal({ open, patient, onClose, onSchedule, isSubmitting, submissionError }) {
  const [doctor, setDoctor] = useState('');
  const [appointmentDateTime, setAppointmentDateTime] = useState(null);
  const [modalError, setModalError] = useState('');

  // Predefined doctor options for better UX
  const doctorSuggestions = [
    'Dr. Smith (Cardiology)',
    'Dr. Johnson (General Medicine)',
    'Dr. Williams (Neurology)',
    'Dr. Brown (Orthopedics)',
    'Dr. Davis (Dermatology)',
    'Dr. Miller (Pediatrics)',
    'Dr. Wilson (Psychiatry)',
    'Dr. Moore (Oncology)',
  ];

  useEffect(() => {
    if (open) {
      // Reset form when modal opens
      setDoctor('');
      setAppointmentDateTime(addDays(new Date(), 1)); // Default to tomorrow
      setModalError('');
    }
  }, [open, patient]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError('');

    if (!doctor.trim()) {
      setModalError('Doctor name is required.');
      return;
    }

    if (!appointmentDateTime || !isValid(appointmentDateTime)) {
      setModalError('A valid appointment date and time are required.');
      return;
    }

    if (appointmentDateTime <= new Date()) {
      setModalError('Appointment date and time must be in the future.');
      return;
    }

    // Check if appointment is during business hours (9 AM - 6 PM)
    const hour = appointmentDateTime.getHours();
    if (hour < 9 || hour >= 18) {
      setModalError('Appointments can only be scheduled between 9:00 AM and 6:00 PM.');
      return;
    }

    // Check if appointment is on weekends
    const day = appointmentDateTime.getDay();
    if (day === 0 || day === 6) {
      setModalError('Appointments can only be scheduled on weekdays (Monday - Friday).');
      return;
    }

    const appointmentData = {
      doctorName: doctor.trim(),
      appointmentDateTime: formatISO(appointmentDateTime),
    };

    const success = await onSchedule(appointmentData);
    
    if (success !== false) {
      // Reset form only if successful
      setDoctor('');
      setAppointmentDateTime(null);
    }
  };

  if (!open || !patient) return null;

  return (
    <Modal
      open={open}
      onClose={onClose}
      aria-labelledby="schedule-appointment-modal-title"
    >
      <Box sx={modalStyle} component="form" onSubmit={handleSubmit}>
        <Typography id="schedule-appointment-modal-title" variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
          <CalendarTodayIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Schedule Main Appointment
        </Typography>

        <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item>
              <PersonIcon color="primary" />
            </Grid>
            <Grid item xs>
              <Typography variant="h6" color="primary">
                {patient.name}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Email: {patient.email}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                Status: {patient.consultationPaymentStatus}
              </Typography>
            </Grid>
          </Grid>
        </Box>
        
        {(modalError || submissionError) && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {modalError || submissionError}
          </Alert>
        )}

        <TextField
          margin="normal"
          required
          fullWidth
          id="doctor"
          label="Doctor Name"
          name="doctor"
          value={doctor}
          onChange={(e) => setDoctor(e.target.value)}
          placeholder="Enter doctor's name or select from list"
          autoFocus
          disabled={isSubmitting}
          select
          SelectProps={{
            native: false,
          }}
          sx={{ mb: 2 }}
        >
          <option value="" disabled>
            Select a doctor or type custom name
          </option>
          {doctorSuggestions.map((suggestion) => (
            <option key={suggestion} value={suggestion}>
              {suggestion}
            </option>
          ))}
        </TextField>

        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <DateTimePicker
            label="Appointment Date & Time"
            value={appointmentDateTime}
            onChange={(newValue) => setAppointmentDateTime(newValue)}
            slotProps={{ 
              textField: { 
                fullWidth: true, 
                margin: 'normal', 
                required: true,
                helperText: 'Business hours: 9:00 AM - 6:00 PM (Mon-Fri)'
              } 
            }}
            disablePast
            disabled={isSubmitting}
            shouldDisableDate={(date) => {
              const day = date.getDay();
              return day === 0 || day === 6; // Disable weekends
            }}
            shouldDisableTime={(timeValue, clockType) => {
              if (clockType === 'hours') {
                const hour = timeValue;
                return hour < 9 || hour >= 18; // Disable outside business hours
              }
              return false;
            }}
          />
        </LocalizationProvider>

        <Box sx={{ mt: 4, display: 'flex', justifyContent: 'space-between', gap: 2 }}>
          <Button 
            onClick={onClose} 
            variant="outlined"
            size="large"
            disabled={isSubmitting}
            sx={{ minWidth: 120 }}
          >
            Cancel
          </Button>
          <Button 
            type="submit" 
            variant="contained" 
            color="primary"
            size="large"
            disabled={isSubmitting || !doctor.trim() || !appointmentDateTime}
            startIcon={isSubmitting ? <CircularProgress size={20} /> : <AccessTimeIcon />}
            sx={{ minWidth: 180 }}
          >
            {isSubmitting ? 'Scheduling...' : 'Schedule Appointment'}
          </Button>
        </Box>

        <Box sx={{ mt: 3, p: 2, bgcolor: 'info.light', borderRadius: 2 }}>
          <Typography variant="body2" color="info.dark">
            <strong>Note:</strong> Main appointments can only be scheduled for patients with PAID consultation status.
            Appointments are available Monday-Friday, 9:00 AM - 6:00 PM.
          </Typography>
        </Box>
      </Box>
    </Modal>
  );
}

export default AppointmentModal;