import React, { useState } from 'react';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Alert from '@mui/material/Alert';
import Grid from '@mui/material/Grid';
import { styled } from '@mui/material/styles';
// Consider using a DatePicker for dateOfBirth if you add @mui/x-date-pickers
// import { DatePicker } from '@mui/x-date-pickers/DatePicker';
// import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
// import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns'; // or AdapterDayjs, AdapterLuxon

const StyledTextField = styled(TextField)(({ theme }) => ({
  '& .MuiOutlinedInput-root': {
    borderRadius: theme.spacing(1),
    backgroundColor: '#fafafa',
    border: '1px solid rgba(0,0,0,0.08)',
    '&:hover': {
      backgroundColor: '#f5f5f5',
      '& fieldset': {
        borderColor: '#27ae60',
      },
    },
    '&.Mui-focused': {
      backgroundColor: '#ffffff',
      '& fieldset': {
        borderColor: '#27ae60',
        borderWidth: 2,
      },
    },
  },
  '& .MuiInputLabel-outlined': {
    color: '#2c3e50',
    '&.Mui-focused': {
      color: '#27ae60',
    },
  },
  '& .MuiInputLabel-outlined.MuiInputLabel-shrink': {
    backgroundColor: theme.palette.background.paper,
    paddingLeft: '8px',
    paddingRight: '8px',
  },
}));

const StyledButton = styled(Button)(({ theme }) => ({
  borderRadius: theme.spacing(1.5),
  padding: theme.spacing(1.5, 3),
  fontSize: '1rem',
  fontWeight: 600,
  textTransform: 'none',
  background: 'linear-gradient(135deg, #27ae60 0%, #2ecc71 100%)',
  boxShadow: '0 4px 12px rgba(39, 174, 96, 0.3)',
  border: '1px solid rgba(39, 174, 96, 0.2)',
  '&:hover': {
    background: 'linear-gradient(135deg, #219a52 0%, #28b463 100%)',
    transform: 'translateY(-2px)',
    boxShadow: '0 6px 20px rgba(39, 174, 96, 0.4)',
  },
  transition: 'all 0.2s ease-in-out',
}));

function AddPatientForm({ onSubmit }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState(''); // Keep as string for HTML5 date input or MUI TextField type="date"
  const [problem, setProblem] = useState('');
  const [location, setLocation] = useState('');
  const [consultationFee, setConsultationFee] = useState('50.00');
  const [formError, setFormError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    if (!name || !email || !dateOfBirth || !problem || !consultationFee) { // Address and Location can be optional based on requirements
      setFormError('Please fill in all required fields: Name, Email, DOB, Problem, Fee.');
      return;
    }
    // Validate that consultationFee is a valid number format (but keep as string for backend)
    const fee = parseFloat(consultationFee);
    if (isNaN(fee) || fee <= 0) {
      setFormError('Consultation fee must be a positive number.');
      return;
    }
    const patientData = {
      name, email, address, dateOfBirth, problem, location,
      consultationFee: consultationFee, // Send as string, not converted to number
      registeredDate: new Date().toISOString().split('T')[0],
    };
    const success = await onSubmit(patientData);
    if (success) {
      setName(''); setEmail(''); setAddress(''); setDateOfBirth('');
      setProblem(''); setLocation(''); setConsultationFee('50.00');
      setFormError('');
    }
  };

  // Common props for TextFields to ensure labels are always shrunk (on top)
  const commonTextFieldProps = {
    margin: "dense",
    size: "small",
    fullWidth: true,
    InputLabelProps: { shrink: true },
    variant: "outlined",
  };

  // For the date field, type="date" handles label shrinkage well, but we ensure consistency.
  const dateFieldProps = {
    ...commonTextFieldProps, // Inherit common styles
    type: "date",
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
      
      <Box sx={{ flexGrow: 1, overflowY: 'auto', paddingRight: 1 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <StyledTextField
              {...commonTextFieldProps}
              required
              id="name"
              label="Full Name"
              name="name"
              autoComplete="name"
              autoFocus
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <StyledTextField
              {...commonTextFieldProps}
              required
              id="email"
              label="Email Address"
              name="email"
              autoComplete="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </Grid>
          <Grid item xs={12}>
            <StyledTextField
              {...commonTextFieldProps}
              id="address"
              label="Address (Optional)"
              name="address"
              autoComplete="street-address"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <StyledTextField
              {...dateFieldProps} // Use specific dateFieldProps
              required
              id="dateOfBirth"
              label="Date of Birth"
              name="dateOfBirth"
              value={dateOfBirth}
              onChange={(e) => setDateOfBirth(e.target.value)}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <StyledTextField
              {...commonTextFieldProps}
              id="location"
              label="Location (City/Area, Optional)"
              name="location"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
            />
          </Grid>
          <Grid item xs={12}>
            <StyledTextField
              {...commonTextFieldProps}
              required
              id="problem"
              label="Problem/Reason for Visit"
              name="problem"
              multiline
              rows={3}
              value={problem}
              onChange={(e) => setProblem(e.target.value)}
            />
          </Grid>
          <Grid item xs={12}>
            <StyledTextField
              {...commonTextFieldProps}
              required
              id="consultationFee"
              label="Consultation Fee ($)"
              name="consultationFee"
              type="number"
              value={consultationFee}
              onChange={(e) => setConsultationFee(e.target.value)}
              inputProps={{ min: "0", step: "0.01" }}
            />
          </Grid>
        </Grid>
      </Box>
      
      <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid rgba(0,0,0,0.1)', borderColor: '#e0e0e0' }}>
        <StyledButton
          type="submit"
          fullWidth
          variant="contained"
        >
          Add Patient
        </StyledButton>
      </Box>
    </Box>
  );
}

export default AddPatientForm; 