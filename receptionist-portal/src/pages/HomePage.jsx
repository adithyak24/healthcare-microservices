import React, { useState, useEffect } from 'react';
import AddPatientForm from '../components/AddPatientForm';
import RecentPatientsList from '../components/RecentPatientsList';
import { apiAddPatient, apiGetRecentPatients } from '../services/api'; 
import Profile from '../components/Profile';
import { useAuth0 } from '@auth0/auth0-react';

// MUI Imports
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PeopleIcon from '@mui/icons-material/People';
import { styled } from '@mui/material/styles';

// Styled components for better visual appeal
const GradientContainer = styled(Container)(({ theme }) => ({
  background: 'linear-gradient(135deg, #f8fffe 0%, #e8f5e8 100%)',
  minHeight: '100vh',
  paddingTop: theme.spacing(3),
  paddingBottom: theme.spacing(3),
}));

const WelcomeHeader = styled(Paper)(({ theme }) => ({
  background: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
  color: 'white',
  borderRadius: theme.spacing(2),
  padding: theme.spacing(3),
  marginBottom: theme.spacing(3),
  boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
  border: '1px solid rgba(255,255,255,0.1)',
}));

const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  borderRadius: theme.spacing(2),
  boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
  transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
  display: 'flex',
  flexDirection: 'column',
  border: '1px solid rgba(0,0,0,0.08)',
  backgroundColor: '#ffffff',
  '&:hover': {
    transform: 'translateY(-4px)',
    boxShadow: '0 12px 35px rgba(0,0,0,0.12)',
    border: '1px solid rgba(0,0,0,0.12)',
  },
}));

const StyledCardHeader = styled(CardHeader)(({ theme, headercolor }) => ({
  background: headercolor || theme.palette.primary.main,
  color: 'white',
  textAlign: 'center',
  borderBottom: '1px solid rgba(255,255,255,0.2)',
  '& .MuiCardHeader-title': {
    fontWeight: 600,
    fontSize: '1.2rem',
  },
  '& .MuiCardHeader-avatar': {
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: '50%',
    margin: '0 auto',
  },
}));

const StyledCardContent = styled(CardContent)(({ theme }) => ({
  flexGrow: 1,
  padding: theme.spacing(3),
  display: 'flex',
  flexDirection: 'column',
  height: 'calc(100% - 64px)', // Subtract header height
  maxHeight: '600px', // Set a maximum height for consistent sizing
}));

const AlertContainer = styled(Box)(({ theme }) => ({
  minHeight: '60px',
  marginBottom: theme.spacing(2),
  display: 'flex',
  alignItems: 'center',
}));

function HomePage() {
  const [recentPatients, setRecentPatients] = useState([]);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoadingRecent, setIsLoadingRecent] = useState(false);
  const { getAccessTokenSilently } = useAuth0();

  const fetchRecentPatients = async () => {
    setIsLoadingRecent(true);
    setError('');
    try {
      const accessToken = await getAccessTokenSilently();
      const data = await apiGetRecentPatients(accessToken);
      setRecentPatients(data || []);
    } catch (err) {
      console.error("Error fetching recent patients or getting token:", err);
      setError(err.message || 'Failed to fetch recent patients.');
      setRecentPatients([]);
    } finally {
      setIsLoadingRecent(false);
    }
  };

  useEffect(() => {
    fetchRecentPatients();
  }, []);

  const handleAddPatient = async (patientData) => {
    setError('');
    setSuccessMessage('');
    try {
      const accessToken = await getAccessTokenSilently();
      const newPatient = await apiAddPatient(patientData, accessToken);
      setSuccessMessage(`Patient ${newPatient.name} added successfully.`);
      fetchRecentPatients();
      setTimeout(() => setSuccessMessage(''), 4000);
      return true;
    } catch (err) {
      console.error("Error adding patient or getting token:", err);
      const errorMessage = err.response?.data?.message || err.message || 'Failed to add patient. Please try again.';
      setError(errorMessage);
      setTimeout(() => setError(''), 5000);
      return false;
    }
  };

  return (
    <GradientContainer maxWidth="lg">
      {/* Welcome Header */}
      <WelcomeHeader elevation={0}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', textAlign: 'center', mb: 1 }}>
          Receptionist Dashboard
        </Typography>
        <Typography variant="h6" sx={{ textAlign: 'center', opacity: 0.9 }}>
          Manage patient registrations and appointments
        </Typography>
      </WelcomeHeader>
      
      {/* Profile Section */}
      <Box sx={{ mb: 3 }}>
        <Profile />
      </Box>

      {/* Alert Messages */}
      <AlertContainer>
        {error && <Alert severity="error" onClose={() => setError('')} sx={{ width: '100%' }}>{error}</Alert>}
        {successMessage && <Alert severity="success" onClose={() => setSuccessMessage('')} sx={{ width: '100%' }}>{successMessage}</Alert>}
      </AlertContainer>

      {/* Main Content Cards */}
      <Grid container spacing={4} sx={{ height: '100%' }}>
        {/* Add New Patient Card */}
        <Grid item xs={12} lg={6}>
          <StyledCard>
            <StyledCardHeader 
              title="Add New Patient"
              avatar={<PersonAddIcon sx={{ fontSize: 30 }} />}
              headercolor="linear-gradient(135deg, #27ae60 0%, #2ecc71 100%)"
            />
            <StyledCardContent>
              <AddPatientForm onSubmit={handleAddPatient} />
            </StyledCardContent>
          </StyledCard>
        </Grid>

        {/* Recently Added Patients Card */}
        <Grid item xs={12} lg={6}>
          <StyledCard>
            <StyledCardHeader 
              title="Recently Added Patients"
              avatar={<PeopleIcon sx={{ fontSize: 30 }} />}
              headercolor="linear-gradient(135deg, #3498db 0%, #2980b9 100%)"
            />
            <StyledCardContent>
              {isLoadingRecent ? (
                <Box sx={{ 
                  display: 'flex', 
                  justifyContent: 'center', 
                  alignItems: 'center', 
                  flexGrow: 1,
                  minHeight: '200px'
                }}>
                  <Box sx={{ textAlign: 'center' }}>
                    <CircularProgress sx={{ mb: 2 }} />
                    <Typography>Loading recent patients...</Typography>
                  </Box>
                </Box>
              ) : recentPatients.length === 0 ? (
                <Box sx={{ 
                  display: 'flex', 
                  justifyContent: 'center', 
                  alignItems: 'center', 
                  flexGrow: 1,
                  textAlign: 'center'
                }}>
                  <Box>
                    <PeopleIcon sx={{ fontSize: 48, color: 'grey.400', mb: 2 }} />
                    <Typography color="textSecondary">No recent patients to display.</Typography>
                  </Box>
                </Box>
              ) : (
                <Box sx={{ 
                  flexGrow: 1, 
                  overflowY: 'auto',
                  paddingRight: 1 
                }}>
                  <RecentPatientsList patients={recentPatients} />
                </Box>
              )}
            </StyledCardContent>
          </StyledCard>
        </Grid>
      </Grid>
    </GradientContainer>
  );
}

export default HomePage; 