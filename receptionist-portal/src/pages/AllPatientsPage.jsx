import React, { useState, useEffect, useMemo } from 'react';
import PatientList from '../components/PatientList';
import AppointmentModal from '../components/AppointmentModal';
import AddVisitModal from '../components/AddVisitModal';
import ScheduleVisitAppointmentModal from '../components/ScheduleVisitAppointmentModal';
import { apiGetAllPatientsForReceptionist, apiScheduleAppointment, apiAddPatientVisit, apiScheduleVisitAppointment } from '../services/api';
import { useAuth0 } from '@auth0/auth0-react';

// MUI Imports
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Paper from '@mui/material/Paper';
import ButtonGroup from '@mui/material/ButtonGroup';
import Chip from '@mui/material/Chip';
import FilterListIcon from '@mui/icons-material/FilterList';
import PeopleIcon from '@mui/icons-material/People';
import Pagination from '@mui/material/Pagination';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { styled } from '@mui/material/styles';

// Styled components for better visual appeal
const GradientContainer = styled(Container)(({ theme }) => ({
  background: 'linear-gradient(135deg, #f8fffe 0%, #e8f5e8 100%)',
  minHeight: '100vh',
  paddingTop: theme.spacing(4),
  paddingBottom: theme.spacing(4),
}));

const HeaderSection = styled(Paper)(({ theme }) => ({
  background: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
  color: 'white',
  borderRadius: theme.spacing(2),
  padding: theme.spacing(4),
  marginBottom: theme.spacing(3),
  boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
  border: '1px solid rgba(255,255,255,0.1)',
}));

const FilterSection = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(3),
  marginBottom: theme.spacing(3),
  borderRadius: theme.spacing(2),
  boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
  border: '1px solid rgba(0,0,0,0.08)',
  backgroundColor: '#ffffff',
}));

const PatientListContainer = styled(Paper)(({ theme }) => ({
  borderRadius: theme.spacing(2),
  overflow: 'hidden',
  boxShadow: '0 8px 32px rgba(0,0,0,0.08)',
  border: '1px solid rgba(0,0,0,0.08)',
  backgroundColor: '#ffffff',
}));

const StyledButtonGroup = styled(ButtonGroup)(({ theme }) => ({
  borderRadius: theme.spacing(3),
  overflow: 'hidden',
  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  border: '1px solid rgba(0,0,0,0.1)',
}));

const FilterButton = styled(Button)(({ theme, selected }) => ({
  borderRadius: theme.spacing(3),
  padding: theme.spacing(1, 2),
  textTransform: 'none',
  fontWeight: selected ? 600 : 400,
  backgroundColor: selected ? '#27ae60' : 'transparent',
  color: selected ? 'white' : '#27ae60',
  border: `1px solid #27ae60`,
  '&:hover': {
    backgroundColor: selected ? '#219a52' : 'rgba(39, 174, 96, 0.1)',
    color: selected ? 'white' : '#219a52',
  },
  '&:first-of-type': {
    borderTopLeftRadius: theme.spacing(3),
    borderBottomLeftRadius: theme.spacing(3),
  },
  '&:last-of-type': {
    borderTopRightRadius: theme.spacing(3),
    borderBottomRightRadius: theme.spacing(3),
  },
}));

const StatCard = styled(Box)(({ theme }) => ({
  padding: theme.spacing(2),
  borderRadius: theme.spacing(1),
  backgroundColor: 'rgba(255,255,255,0.25)',
  textAlign: 'center',
  minWidth: 120,
  border: '1px solid rgba(255,255,255,0.2)',
}));

const FILTER_OPTIONS = {
  ALL: 'All Patients',
  PAID_NEEDS_APPOINTMENT: 'Paid & Needs Appointment',
  PAID_SCHEDULED: 'Paid & Appointment Scheduled',
  PAYMENT_NOT_COMPLETED: 'Payment Not Completed',
};

function AllPatientsPage() {
  const [allPatients, setAllPatients] = useState([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  // Pagination state
  const [page, setPage] = useState(0); // 0-based for backend
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [sortBy, setSortBy] = useState('registeredDate');
  const [sortDir, setSortDir] = useState('desc');
  
  const [showAppointmentModal, setShowAppointmentModal] = useState(false);
  const [selectedPatientForAppointment, setSelectedPatientForAppointment] = useState(null);
  const [showAddVisitModal, setShowAddVisitModal] = useState(false);
  const [selectedPatientForVisit, setSelectedPatientForVisit] = useState(null);
  const [actionMessage, setActionMessage] = useState({ type: '', text: '' });
  const [activeFilter, setActiveFilter] = useState(FILTER_OPTIONS.ALL);
  const { getAccessTokenSilently } = useAuth0();

  // State for main appointment modal
  const [isSubmittingMainAppointment, setIsSubmittingMainAppointment] = useState(false);
  const [mainAppointmentError, setMainAppointmentError] = useState('');

  // State for add visit modal
  const [isSubmittingAddVisit, setIsSubmittingAddVisit] = useState(false);
  const [addVisitError, setAddVisitError] = useState('');

  // State for schedule visit appointment modal
  const [showScheduleVisitAppointmentModal, setShowScheduleVisitAppointmentModal] = useState(false);
  const [selectedPatientForVisitAppointment, setSelectedPatientForVisitAppointment] = useState(null);
  const [selectedVisitForAppointment, setSelectedVisitForAppointment] = useState(null);
  const [isSubmittingVisitAppointment, setIsSubmittingVisitAppointment] = useState(false);
  const [visitAppointmentError, setVisitAppointmentError] = useState('');

  const fetchAllPatients = async (currentPage = page, currentSize = size, currentSortBy = sortBy, currentSortDir = sortDir) => {
    setIsLoading(true);
    setError('');
    setMainAppointmentError('');
    setAddVisitError('');
    setVisitAppointmentError('');
    try {
      const accessToken = await getAccessTokenSilently();
      const data = await apiGetAllPatientsForReceptionist(accessToken, currentPage, currentSize, currentSortBy, currentSortDir);
      
      // Handle paginated response
      if (data.content) {
        setAllPatients(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } else {
        // Fallback for non-paginated response
        setAllPatients(data || []);
      }
    } catch (err) {
      console.error("Error fetching all patients or getting token:", err);
      setError(err.message || 'Failed to fetch patients.');
      setAllPatients([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAllPatients();
  }, [page, size, sortBy, sortDir]);

  const handlePageChange = (event, newPage) => {
    setPage(newPage - 1); // Convert to 0-based
  };

  const handleSizeChange = (event) => {
    setSize(event.target.value);
    setPage(0); // Reset to first page
  };

  const handleSortChange = (event) => {
    setSortBy(event.target.value);
    setPage(0); // Reset to first page
  };

  const handleSortDirChange = (event) => {
    setSortDir(event.target.value);
    setPage(0); // Reset to first page
  };

  const filteredPatients = useMemo(() => {
    if (activeFilter === FILTER_OPTIONS.ALL) {
      return allPatients;
    }
    return allPatients.filter(patient => {
      const isPaid = patient.consultationPaymentStatus === 'PAID';
      const isScheduled = patient.appointmentDateTime && patient.appointmentDateTime.trim() !== '';

      if (activeFilter === FILTER_OPTIONS.PAID_NEEDS_APPOINTMENT) {
        // Check if patient has paid visits but no main appointment, OR
        // Check if last visit is paid but not scheduled
        const hasUnscheduledPaidVisits = patient.visits && patient.visits.some(visit => 
          visit.visitPaymentStatus === 'PAID' && 
          (!visit.appointmentDateTime || visit.appointmentDateTime.trim() === '')
        );
        return isPaid && (!isScheduled || hasUnscheduledPaidVisits);
      }
      if (activeFilter === FILTER_OPTIONS.PAID_SCHEDULED) {
        return isPaid && isScheduled;
      }
      if (activeFilter === FILTER_OPTIONS.PAYMENT_NOT_COMPLETED) {
        // Show patients who don't have their last visit paid
        const hasUnpaidVisits = !patient.visits || patient.visits.length === 0 || 
          patient.visits.some(visit => visit.visitPaymentStatus !== 'PAID');
        return patient.consultationPaymentStatus !== 'PAID' || hasUnpaidVisits;
      }
      return true;
    });
  }, [allPatients, activeFilter]);

  // Calculate statistics - use totalElements for accurate total count
  const stats = useMemo(() => {
    const total = totalElements || allPatients.length;
    const paid = allPatients.filter(p => p.consultationPaymentStatus === 'PAID').length;
    const scheduled = allPatients.filter(p => p.appointmentDateTime && p.appointmentDateTime.trim() !== '').length;
    const pending = allPatients.filter(p => 
      p.consultationPaymentStatus === 'NOT_PAID' ||
      p.consultationPaymentStatus === 'PAYMENT_FAILED' ||
      p.consultationPaymentStatus === 'PAYMENT_PENDING'
    ).length;
    
    return { total, paid, scheduled, pending };
  }, [allPatients, totalElements]);

  const handleOpenAppointmentModal = (patient) => {
    setActionMessage({ type: '', text: '' });
    setMainAppointmentError('');
    if (patient.consultationPaymentStatus !== 'PAID') {
      setActionMessage({ type: 'error', text: 'Main appointment can only be scheduled if initial consultation is PAID.' });
      setTimeout(() => setActionMessage({ type: '', text: '' }), 4000);
      return;
    }
    setSelectedPatientForAppointment(patient);
    setShowAppointmentModal(true);
  };

  const handleCloseAppointmentModal = () => {
    setShowAppointmentModal(false);
    setSelectedPatientForAppointment(null);
    setMainAppointmentError('');
  };

  const handleScheduleMainAppointment = async (appointmentData) => {
    if (!selectedPatientForAppointment) return false;
    setIsSubmittingMainAppointment(true);
    setMainAppointmentError('');
    setActionMessage({ type: '', text: '' });
    
    try {
      const accessToken = await getAccessTokenSilently();
      const updatedPatient = await apiScheduleAppointment(selectedPatientForAppointment.id, appointmentData, accessToken);
      
      // Immediately update the patient in the local state
      setAllPatients(prevPatients => 
        prevPatients.map(p => 
          p.id === selectedPatientForAppointment.id 
            ? { ...p, ...updatedPatient } 
            : p
        )
      );
      
      setActionMessage({ type: 'success', text: 'Main appointment scheduled successfully!' });
      handleCloseAppointmentModal();
      
      // Also refresh all patients to ensure consistency
      fetchAllPatients();
      
      setTimeout(() => setActionMessage({ type: '', text: '' }), 3000);
      return true; // Indicate success
      
    } catch (err) {
      console.error("Error scheduling main appointment:", err);
      setMainAppointmentError(err.message || 'Failed to schedule main appointment.');
      return false; // Indicate failure
    } finally {
      setIsSubmittingMainAppointment(false);
    }
  };

  const handleOpenAddVisitModal = (patient) => {
    setActionMessage({ type: '', text: '' });
    setAddVisitError('');
    setSelectedPatientForVisit(patient);
    setShowAddVisitModal(true);
  };

  const handleCloseAddVisitModal = () => {
    setShowAddVisitModal(false);
    setSelectedPatientForVisit(null);
    setAddVisitError('');
  };

  const handleAddVisit = async (visitData) => {
    if (!selectedPatientForVisit) return;
    setIsSubmittingAddVisit(true);
    setAddVisitError('');
    setActionMessage({ type: '', text: '' });
    try {
      const accessToken = await getAccessTokenSilently();
      await apiAddPatientVisit(selectedPatientForVisit.id, visitData, accessToken);
      setActionMessage({ type: 'success', text: 'Visit added successfully!' });
      fetchAllPatients();
      handleCloseAddVisitModal();
      setTimeout(() => setActionMessage({ type: '', text: '' }), 3000);
    } catch (err) {
      console.error("Error adding visit:", err);
      setAddVisitError(err.message || 'Failed to add visit.');
    } finally {
      setIsSubmittingAddVisit(false);
    }
  };

  const handleOpenScheduleVisitAppointmentModal = (patient, visit) => {
    setActionMessage({ type: '', text: '' });
    setVisitAppointmentError('');
    if (visit.visitPaymentStatus !== 'PAID') {
        setActionMessage({ type: 'error', text: 'Appointment can only be scheduled for PAID visits.' });
        setTimeout(() => setActionMessage({ type: '', text: '' }), 4000);
        return;
    }
    setSelectedPatientForVisitAppointment(patient);
    setSelectedVisitForAppointment(visit);
    setShowScheduleVisitAppointmentModal(true);
  };

  const handleCloseScheduleVisitAppointmentModal = () => {
    setShowScheduleVisitAppointmentModal(false);
    setSelectedPatientForVisitAppointment(null);
    setSelectedVisitForAppointment(null);
    setVisitAppointmentError('');
  };

  const handleSubmitVisitAppointment = async (patientId, visitId, appointmentData) => {
    setIsSubmittingVisitAppointment(true);
    setVisitAppointmentError('');
    setActionMessage({ type: '', text: '' });
    try {
      const accessToken = await getAccessTokenSilently();
      const updatedVisit = await apiScheduleVisitAppointment(patientId, visitId, appointmentData, accessToken);
      
      setAllPatients(prevPatients => 
        prevPatients.map(p => {
          if (p.id === patientId) {
            return {
              ...p,
              visits: p.visits.map(v => 
                v.id === visitId ? { ...v, ...updatedVisit } : v
              )
            };
          }
          return p;
        })
      );
      setActionMessage({ type: 'success', text: 'Visit appointment scheduled successfully!' });
      handleCloseScheduleVisitAppointmentModal();
      setTimeout(() => setActionMessage({ type: '', text: '' }), 3000);
    } catch (err) {
      console.error("Error scheduling visit appointment:", err);
      setVisitAppointmentError(err.message || 'Failed to schedule visit appointment.');
    } finally {
      setIsSubmittingVisitAppointment(false);
    }
  };

  if (isLoading && !allPatients.length) {
    return (
      <GradientContainer maxWidth="lg">
        <Box sx={{ textAlign: 'center', py: 10 }}>
          <CircularProgress size={60} sx={{ mb: 2 }} />
          <Typography variant="h6">Loading patients...</Typography>
        </Box>
      </GradientContainer>
    );
  }

  return (
    <GradientContainer maxWidth="lg">
      {/* Header Section */}
      <HeaderSection elevation={0}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <PeopleIcon sx={{ fontSize: 40 }} />
          <Box>
            <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', mb: 1 }}>
              All Patients
            </Typography>
            <Typography variant="h6" sx={{ opacity: 0.9 }}>
              Manage patient records, appointments, and visits
            </Typography>
          </Box>
        </Box>

        {/* Statistics */}
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <StatCard>
            <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 0.5 }}>{stats.total}</Typography>
            <Typography variant="caption">Total Patients</Typography>
          </StatCard>
          <StatCard>
            <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 0.5 }}>{stats.paid}</Typography>
            <Typography variant="caption">Paid</Typography>
          </StatCard>
          <StatCard>
            <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 0.5 }}>{stats.scheduled}</Typography>
            <Typography variant="caption">Scheduled</Typography>
          </StatCard>
          <StatCard>
            <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 0.5 }}>{stats.pending}</Typography>
            <Typography variant="caption">Pending Payment</Typography>
          </StatCard>
        </Box>
      </HeaderSection>
      
      {/* Filter Section */}
      <FilterSection elevation={0}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
          <FilterListIcon color="primary" />
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Filter & Sort Patients
          </Typography>
          <Chip 
            label={`${filteredPatients.length} of ${totalElements} shown`} 
            color="primary" 
            size="small" 
          />
        </Box>
        
        {/* Sorting Controls */}
        <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
          <FormControl size="small" sx={{ minWidth: 140 }}>
            <InputLabel>Sort By</InputLabel>
            <Select value={sortBy} onChange={handleSortChange} label="Sort By">
              <MenuItem value="registeredDate">Registration Date</MenuItem>
              <MenuItem value="name">Name</MenuItem>
              <MenuItem value="email">Email</MenuItem>
              <MenuItem value="consultationPaymentStatus">Payment Status</MenuItem>
            </Select>
          </FormControl>
          
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Order</InputLabel>
            <Select value={sortDir} onChange={handleSortDirChange} label="Order">
              <MenuItem value="desc">Descending</MenuItem>
              <MenuItem value="asc">Ascending</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 100 }}>
            <InputLabel>Per Page</InputLabel>
            <Select value={size} onChange={handleSizeChange} label="Per Page">
              <MenuItem value={5}>5</MenuItem>
              <MenuItem value={10}>10</MenuItem>
              <MenuItem value={20}>20</MenuItem>
              <MenuItem value={50}>50</MenuItem>
            </Select>
          </FormControl>
        </Box>
        
        {/* Filter Buttons */}
        <Box sx={{ display: 'flex', justifyContent: 'center', flexWrap: 'wrap' }}>
          <StyledButtonGroup variant="outlined">
            {Object.entries(FILTER_OPTIONS).map(([key, filterName]) => (
              <FilterButton 
                key={key}
                onClick={() => setActiveFilter(filterName)}
                selected={activeFilter === filterName ? 1 : 0}
              >
                {filterName}
              </FilterButton>
            ))}
          </StyledButtonGroup>
        </Box>
      </FilterSection>

      {/* Alert Messages */}
      {actionMessage.text && (
        <Alert 
          severity={actionMessage.type || 'info'} 
          sx={{ 
            mb: 3,
            borderRadius: 2,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
          }} 
          onClose={() => setActionMessage({ type: '', text: '' })}
        >
          {actionMessage.text}
        </Alert>
      )}
      {error && (
        <Alert 
          severity="error" 
          sx={{ 
            mb: 3,
            borderRadius: 2,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
          }}
        >
          {error}
        </Alert>
      )}
      
      {/* Patient List */}
      <PatientListContainer elevation={0}>
        {isLoading && allPatients.length > 0 && (
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center', 
            p: 2, 
            position: 'absolute', 
            top: '50%', 
            left: '50%', 
            transform: 'translate(-50%, -50%)', 
            backgroundColor: 'rgba(255,255,255,0.9)', 
            borderRadius: 2,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
            zIndex: 1000
          }}>
            <CircularProgress size={24} sx={{ mr: 1 }} /> 
            <Typography>Refreshing patient list...</Typography>
          </Box>
        )}
        <PatientList 
          patients={filteredPatients} 
          onScheduleAppointment={handleOpenAppointmentModal} 
          onAddVisit={handleOpenAddVisitModal}
          onScheduleVisitAppointment={handleOpenScheduleVisitAppointmentModal}
        />
        
        {/* Pagination */}
        <Pagination 
              count={totalPages} 
              page={page + 1} // Convert back to 1-based for display
              onChange={handlePageChange}
              color="primary"
              showFirstButton 
              showLastButton
              size="medium"
              sx={{
                '& .MuiPaginationItem-root': {
                  backgroundColor: 'white',
                  border: '1px solid rgba(0,0,0,0.12)',
                  '&:hover': {
                    backgroundColor: 'rgba(25, 118, 210, 0.04)',
                  },
                  '&.Mui-selected': {
                    backgroundColor: '#1976d2',
                    color: 'white',
                    '&:hover': {
                      backgroundColor: '#1565c0',
                    },
                  },
                },
              }}
            />
      </PatientListContainer>

      {/* Modals */}
      {showAppointmentModal && selectedPatientForAppointment && (
        <AppointmentModal
          open={showAppointmentModal}
          patient={selectedPatientForAppointment}
          onClose={handleCloseAppointmentModal}
          onSchedule={handleScheduleMainAppointment}
          isSubmitting={isSubmittingMainAppointment}
          submissionError={mainAppointmentError}
        />
      )}

      {showAddVisitModal && selectedPatientForVisit && (
        <AddVisitModal
          open={showAddVisitModal}
          patient={selectedPatientForVisit}
          onClose={handleCloseAddVisitModal}
          onAddVisit={handleAddVisit}
          isSubmitting={isSubmittingAddVisit}
          submissionError={addVisitError}
        />
      )}

      {showScheduleVisitAppointmentModal && selectedPatientForVisitAppointment && selectedVisitForAppointment && (
        <ScheduleVisitAppointmentModal
          open={showScheduleVisitAppointmentModal}
          onClose={handleCloseScheduleVisitAppointmentModal}
          patient={selectedPatientForVisitAppointment}
          visit={selectedVisitForAppointment}
          onSubmitVisitAppointment={handleSubmitVisitAppointment}
          isSubmitting={isSubmittingVisitAppointment}
          submissionError={visitAppointmentError}
        />
      )}
    </GradientContainer>
  );
}

export default AllPatientsPage; 