import React, { useState } from 'react';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import Chip from '@mui/material/Chip';
import Avatar from '@mui/material/Avatar';
import Divider from '@mui/material/Divider';
import Collapse from '@mui/material/Collapse';
import Grid from '@mui/material/Grid';
import Badge from '@mui/material/Badge';
import PersonIcon from '@mui/icons-material/Person';
import EmailIcon from '@mui/icons-material/Email';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import PaymentIcon from '@mui/icons-material/Payment';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import ScheduleIcon from '@mui/icons-material/Schedule';
import MedicalServicesIcon from '@mui/icons-material/MedicalServices';
import { styled } from '@mui/material/styles';

// Styled components
const PatientCard = styled(Card)(({ theme }) => ({
  marginBottom: theme.spacing(2),
  borderRadius: theme.spacing(2),
  boxShadow: '0 4px 12px rgba(0,0,0,0.06)',
  transition: 'all 0.2s ease-in-out',
  border: '1px solid rgba(0,0,0,0.08)',
  backgroundColor: '#ffffff',
  '&:hover': {
    boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
    transform: 'translateY(-2px)',
    border: '1px solid rgba(0,0,0,0.12)',
  },
}));

const PatientHeader = styled(CardContent)(({ theme }) => ({
  background: 'linear-gradient(135deg, #ecf0f1 0%, #d5dbdb 100%)',
  borderRadius: `${theme.spacing(2)} ${theme.spacing(2)} 0 0`,
  borderBottom: '1px solid rgba(0,0,0,0.08)',
}));

const StatusChip = styled(Chip)(({ status, theme }) => {
  const getStatusColor = () => {
    switch (status?.toLowerCase()) {
      case 'paid':
        return { bg: '#d5f4e6', color: '#27ae60', border: '#a9dfbf' };
      case 'not_paid':
      case 'payment_failed':
      case 'payment_pending':
        return { bg: '#fadbd8', color: '#e74c3c', border: '#f1948a' };
      default:
        return { bg: '#ebf3fd', color: '#3498db', border: '#aed6f1' };
    }
  };
  
  const colors = getStatusColor();
  return {
    backgroundColor: colors.bg,
    color: colors.color,
    border: `1px solid ${colors.border}`,
    fontWeight: 600,
    fontSize: '0.75rem',
  };
});

const VisitCard = styled(Card)(({ theme }) => ({
  minWidth: 320,
  height: 'fit-content',
  borderRadius: theme.spacing(1.5),
  border: `1px solid rgba(0,0,0,0.1)`,
  boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
  transition: 'all 0.2s ease-in-out',
  backgroundColor: '#ffffff',
  '&:hover': {
    boxShadow: '0 4px 16px rgba(0,0,0,0.1)',
    border: `1px solid rgba(0,0,0,0.15)`,
  },
}));

const VisitHeader = styled(Box)(({ theme }) => ({
  background: 'linear-gradient(135deg, #fef9e7 0%, #f8c471 100%)',
  padding: theme.spacing(1.5),
  borderRadius: `${theme.spacing(1.5)} ${theme.spacing(1.5)} 0 0`,
  borderBottom: '1px solid rgba(0,0,0,0.08)',
}));

const InfoRow = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  marginBottom: theme.spacing(0.5),
  '& .MuiSvgIcon-root': {
    fontSize: '1rem',
    color: '#5d6d7e',
  },
}));

function PatientList({ patients, onScheduleAppointment, onAddVisit, onScheduleVisitAppointment }) {
  const [expandedPatientId, setExpandedPatientId] = useState(null);

  if (!patients || patients.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <PersonIcon sx={{ fontSize: 64, color: 'grey.400', mb: 2 }} />
        <Typography variant="h6" color="textSecondary">
          No patients found for the current filter
        </Typography>
        <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
          Try adjusting your filter criteria
        </Typography>
      </Box>
    );
  }

  const formatAppointmentDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'Not Scheduled';
    try {
      return new Date(dateTimeString).toLocaleString();
    } catch (e) {
      console.error("Error formatting date:", e);
      return 'Invalid Date';
    }
  };

  const toggleVisits = (patientId) => {
    setExpandedPatientId(expandedPatientId === patientId ? null : patientId);
  };

  return (
    <Box sx={{ p: 3 }}>
      {patients.map((patient) => (
        <PatientCard key={patient.id}>
          <PatientHeader>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56 }}>
                <PersonIcon sx={{ fontSize: 28 }} />
              </Avatar>
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>
                  {patient.name}
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  <StatusChip 
                    label={patient.consultationPaymentStatus || 'N/A'} 
                    size="small"
                    status={patient.consultationPaymentStatus}
                  />
                  {patient.appointmentDateTime && (
                    <Chip 
                      label="Appointed" 
                      size="small" 
                      color="success"
                      variant="outlined"
                    />
                  )}
                </Box>
              </Box>
              <IconButton 
                onClick={() => toggleVisits(patient.id)}
                sx={{ 
                  backgroundColor: 'white',
                  boxShadow: 1,
                  '&:hover': { backgroundColor: 'grey.100' }
                }}
              >
                {expandedPatientId === patient.id ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </IconButton>
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <InfoRow>
                  <EmailIcon />
                  <Typography variant="body2">{patient.email}</Typography>
                </InfoRow>
                <InfoRow>
                  <CalendarTodayIcon />
                  <Typography variant="body2">
                    Registered: {patient.registeredDate ? new Date(patient.registeredDate).toLocaleDateString() : 'N/A'}
                  </Typography>
                </InfoRow>
              </Grid>
              <Grid item xs={12} md={6}>
                <InfoRow>
                  <PaymentIcon />
                  <Typography variant="body2">
                    Payment: {patient.consultationPaymentStatus || 'N/A'}
                  </Typography>
                </InfoRow>
                <InfoRow>
                  <ScheduleIcon />
                  <Typography variant="body2">
                    {patient.appointmentDateTime 
                      ? `Dr. ${patient.appointmentDoctorName || 'N/A'} - ${formatAppointmentDateTime(patient.appointmentDateTime)}` 
                      : 'No main appointment scheduled'}
                  </Typography>
                </InfoRow>
              </Grid>
            </Grid>
          </PatientHeader>

          <CardActions sx={{ px: 3, py: 2, justifyContent: 'space-between' }}>
            <Box>
              {patient.consultationPaymentStatus === 'PAID' && patient.appointmentDateTime && (
                <Chip 
                  label="Main Appointment Scheduled" 
                  color="success" 
                  size="small"
                  icon={<ScheduleIcon />}
                />
              )}
              {patient.consultationPaymentStatus !== 'PAID' && (
                <Chip 
                  label="Payment Required (Last Visit)" 
                  color="warning" 
                  size="small"
                  icon={<PaymentIcon />}
                />
              )}
            </Box>
            
            <Badge 
              badgeContent={patient.visits?.length || 0} 
              color="primary"
              sx={{ '& .MuiBadge-badge': { fontSize: '0.7rem' } }}
            >
              <Typography variant="caption" color="textSecondary">
                Visits
              </Typography>
            </Badge>
          </CardActions>

          <Collapse in={expandedPatientId === patient.id} timeout="auto" unmountOnExit>
            <Divider />
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <MedicalServicesIcon />
                  Visit History
                </Typography>
                <Button 
                  variant="contained" 
                  color="secondary" 
                  size="small" 
                  onClick={() => onAddVisit(patient)}
                  startIcon={<AddCircleOutlineIcon />}
                >
                  Add New Visit
                </Button>
              </Box>

              {patient.visits && patient.visits.length > 0 ? (
                <Box sx={{ 
                  display: 'flex', 
                  gap: 2, 
                  overflowX: 'auto', 
                  pb: 1, 
                  '&::-webkit-scrollbar': {
                    height: 8,
                  },
                  '&::-webkit-scrollbar-track': {
                    backgroundColor: '#f1f1f1',
                    borderRadius: 4,
                  },
                  '&::-webkit-scrollbar-thumb': {
                    backgroundColor: '#c1c1c1',
                    borderRadius: 4,
                    '&:hover': {
                      backgroundColor: '#a8a8a8',
                    },
                  },
                }}>
                  {patient.visits.map((visit, index) => (
                    <VisitCard key={visit.id || index}>
                      <VisitHeader>
                        <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#856404' }}>
                          Visit #{index + 1}
                        </Typography>
                      </VisitHeader>
                      
                      <CardContent sx={{ pb: 2 }}>
                        <Typography variant="body2" sx={{ fontWeight: 600, mb: 1, color: 'primary.main' }}>
                          {visit.problemDescription || 'N/A'}
                        </Typography>
                        
                        <Box sx={{ mb: 1 }}>
                          <InfoRow>
                            <CalendarTodayIcon />
                            <Typography variant="caption">
                              {visit.visitDate ? new Date(visit.visitDate).toLocaleDateString() : 'N/A'}
                            </Typography>
                          </InfoRow>
                          <InfoRow>
                            <PaymentIcon />
                            <Typography variant="caption">
                              ${visit.consultationFee || 'N/A'} - 
                            </Typography>
                            <StatusChip 
                              label={visit.visitPaymentStatus || 'NOT_PAID'} 
                              size="small"
                              status={visit.visitPaymentStatus}
                            />
                          </InfoRow>
                        </Box>

                        <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mb: 1 }}>
                          Notes: {visit.notes || 'No notes available'}
                        </Typography>

                        <Typography variant="caption" color="textSecondary" sx={{ display: 'block', mb: 2 }}>
                          Appointment: {visit.appointmentDateTime 
                            ? `Dr. ${visit.appointmentDoctorName || 'N/A'} - ${formatAppointmentDateTime(visit.appointmentDateTime)}` 
                            : 'Not scheduled'}
                        </Typography>

                        {visit.visitPaymentStatus === 'PAID' && !visit.appointmentDateTime && (
                          <Button 
                            variant="outlined" 
                            color="primary" 
                            size="small" 
                            fullWidth
                            onClick={() => onScheduleVisitAppointment(patient, visit)}
                            startIcon={<ScheduleIcon />}
                            sx={{
                              '&:hover': {
                                backgroundColor: 'transparent',
                                borderColor: 'primary.main',
                              },
                            }}
                          >
                            Schedule Appointment
                          </Button>
                        )}
                        
                        {visit.visitPaymentStatus === 'PAID' && visit.appointmentDateTime && (
                          <Chip 
                            label="Appointment Scheduled" 
                            color="success" 
                            size="small"
                            sx={{ width: '100%' }}
                          />
                        )}
                        
                        {visit.visitPaymentStatus !== 'PAID' && (
                          <Chip 
                            label="Payment Required" 
                            color="warning" 
                            size="small"
                            sx={{ width: '100%' }}
                          />
                        )}
                      </CardContent>
                    </VisitCard>
                  ))}
                </Box>
              ) : (
                <Box sx={{ 
                  textAlign: 'center', 
                  py: 4, 
                  backgroundColor: 'grey.50', 
                  borderRadius: 2,
                  border: '1px dashed',
                  borderColor: 'grey.300'
                }}>
                  <MedicalServicesIcon sx={{ fontSize: 40, color: 'grey.400', mb: 1 }} />
                  <Typography variant="body2" color="textSecondary">
                    No visit history recorded
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Collapse>
        </PatientCard>
      ))}
    </Box>
  );
}

export default PatientList; 