import React, { useState, useEffect } from 'react';
import {
  Button, Modal, Box, TextField, Typography, CircularProgress, Alert
} from '@mui/material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { isValid, formatISO } from 'date-fns'; // For date formatting and validation

const modalStyle = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 450,
  bgcolor: 'background.paper',
  border: '2px solid #000',
  boxShadow: 24,
  p: 4,
  borderRadius: '8px',
};

function AddVisitModal({ patient, onClose, onAddVisit }) {
  const [visitDate, setVisitDate] = useState(new Date());
  const [problemDescription, setProblemDescription] = useState('');
  const [consultationFee, setConsultationFee] = useState('');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    // Reset form when patient context changes (though typically modal remounts)
    setVisitDate(new Date());
    setProblemDescription('');
    setConsultationFee('');
    setNotes('');
    setError('');
  }, [patient]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!problemDescription.trim()) {
      setError('Problem description is required.');
      return;
    }
    if (!isValid(visitDate)) {
        setError('Invalid visit date selected.');
        return;
    }

    const visitData = {
      visitDate: formatISO(visitDate, { representation: 'date' }), // YYYY-MM-DD
      problemDescription,
      consultationFee,
      notes,
    };

    setIsSubmitting(true);
    const success = await onAddVisit(visitData);
    setIsSubmitting(false);

    if (success) {
      onClose(); // Close modal on successful submission
    }
    // If not successful, onAddVisit in AllPatientsPage should set an error message
  };

  return (
    <Modal
      open={true} // Controlled by parent through props/conditional rendering
      onClose={onClose}
      aria-labelledby="add-visit-modal-title"
      aria-describedby="add-visit-modal-description"
    >
      <Box sx={modalStyle} component="form" onSubmit={handleSubmit}>
        <Typography id="add-visit-modal-title" variant="h6" component="h2" gutterBottom>
          Add New Visit for {patient?.name || 'Patient'}
        </Typography>
        
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <LocalizationProvider dateAdapter={AdapterDateFns}>
          <DatePicker
            label="Visit Date"
            value={visitDate}
            onChange={(newValue) => setVisitDate(newValue)}
            slotProps={{ textField: { fullWidth: true, margin: 'normal' } }}
          />
        </LocalizationProvider>

        <TextField
          margin="normal"
          required
          fullWidth
          id="problemDescription"
          label="Problem Description"
          name="problemDescription"
          multiline
          rows={2}
          value={problemDescription}
          onChange={(e) => setProblemDescription(e.target.value)}
          autoFocus
        />
        <TextField
          margin="normal"
          fullWidth
          id="consultationFee"
          label="Consultation Fee (e.g., 50.00)"
          name="consultationFee"
          value={consultationFee}
          onChange={(e) => setConsultationFee(e.target.value)}
          inputProps={{ type: 'text', pattern: "^[0-9]+(\\.[0-9]{1,2})?$" }}
        />
        <TextField
          margin="normal"
          fullWidth
          id="notes"
          label="Notes (Optional)"
          name="notes"
          multiline
          rows={2}
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />
        <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
          <Button onClick={onClose} sx={{ mr: 1 }} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button 
            type="submit" 
            variant="contained" 
            color="primary" 
            disabled={isSubmitting}
          >
            {isSubmitting ? <CircularProgress size={24} /> : 'Add Visit'}
          </Button>
        </Box>
      </Box>
    </Modal>
  );
}

export default AddVisitModal; 