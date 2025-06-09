import React, { useEffect, useState, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { apiGetMyPaymentAttempts, apiCreateCheckoutSessionForVisitFee } from '../services/billingApi';
import { apiGetPatientDetails } from '../services/patientApi';
import usePaymentNotificationPolling from '../hooks/usePaymentNotificationPolling';

// MUI Imports
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import PaymentIcon from '@mui/icons-material/Payment';
import HistoryIcon from '@mui/icons-material/History';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import PersonIcon from '@mui/icons-material/Person';
import CardHeader from '@mui/material/CardHeader';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Chip from '@mui/material/Chip';
import Avatar from '@mui/material/Avatar';
import { styled } from '@mui/material/styles';

// Styled components for better visual appeal
const GradientContainer = styled(Container)(({ theme }) => ({
  background: 'linear-gradient(135deg, #f8fffe 0%, #e8f5e8 100%)',
  minHeight: '100vh',
  paddingTop: theme.spacing(4),
  paddingBottom: theme.spacing(4),
}));

const WelcomeCard = styled(Card)(({ theme }) => ({
  background: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
  color: 'white',
  marginBottom: theme.spacing(3),
  borderRadius: theme.spacing(2),
  boxShadow: '0 10px 30px rgba(0,0,0,0.15)',
  border: '1px solid rgba(255,255,255,0.1)',
}));

const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  borderRadius: theme.spacing(2),
  boxShadow: '0 8px 25px rgba(0,0,0,0.08)',
  transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
  border: '1px solid rgba(0,0,0,0.06)',
  '&:hover': {
    transform: 'translateY(-4px)',
    boxShadow: '0 12px 35px rgba(0,0,0,0.12)',
    border: '1px solid rgba(0,0,0,0.1)',
  },
}));

const StyledCardHeader = styled(CardHeader)(({ theme, headercolor }) => ({
  background: headercolor || theme.palette.primary.main,
  color: 'white',
  borderBottom: '1px solid rgba(255,255,255,0.2)',
  '& .MuiCardHeader-title': {
    fontWeight: 600,
    fontSize: '1.1rem',
  },
  '& .MuiCardHeader-avatar': {
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: '50%',
  },
}));

const PatientDashboardPage = () => {
    const { currentPatient: authContextPatient, logout, token, isLoggedIn } = useAuth();
    const navigate = useNavigate();
    
    // Declare all hooks first
    const [patientDetails, setPatientDetails] = useState(null);
    const [isLoadingPatientDetails, setIsLoadingPatientDetails] = useState(false);
    const [paymentAttempts, setPaymentAttempts] = useState([]);
    const [isLoadingBills, setIsLoadingBills] = useState(false);
    const [billsError, setBillsError] = useState('');
    const [actionError, setActionError] = useState('');
    const [isProcessingPayment, setIsProcessingPayment] = useState(null);
    const [paymentNotification, setPaymentNotification] = useState(null);

    const fetchPatientData = useCallback(async () => {
        if (!token) {
            console.warn("[PatientDashboardPage] fetchPatientData: No token available, cannot fetch.");
            return;
        }
        setIsLoadingPatientDetails(true);
        try {
            const details = await apiGetPatientDetails(token);
            setPatientDetails(details);
        } catch (error) {
            console.error("Error fetching patient details:", error);
            setActionError("Failed to load patient information.");
        } finally {
            setIsLoadingPatientDetails(false);
        }
    }, [token]);

    const fetchMyBills = useCallback(async () => {
        if (!authContextPatient?.patientId) {
            console.warn("[PatientDashboardPage] fetchMyBills: No patient ID available, cannot fetch bills.");
            return;
        }
        setIsLoadingBills(true);
        setBillsError('');
        try {
            // Pass only the patient ID - no authentication needed for billing service
            const attempts = await apiGetMyPaymentAttempts(authContextPatient.patientId);
            setPaymentAttempts(attempts || []);
        } catch (error) {
            console.error("Error fetching payment attempts:", error);
            setBillsError(error.message || "Failed to load billing information.");
        } finally {
            setIsLoadingBills(false);
        }
    }, [authContextPatient]);

    // Handle real-time payment status updates
    const handlePaymentStatusUpdate = useCallback((notification) => {
        console.log('Payment status update received:', notification);
        setPaymentNotification(notification);
        
        // Auto-refresh data when payment is completed
        if (notification.status === 'PAID') {
            setTimeout(() => {
                fetchPatientData();
                fetchMyBills();
            }, 1000);
        }
        
        // Clear notification after 5 seconds
        setTimeout(() => {
            setPaymentNotification(null);
        }, 5000);
    }, [fetchMyBills, fetchPatientData]);

    // Initialize WebSocket connection
    usePaymentNotificationPolling(handlePaymentStatusUpdate);

    useEffect(() => {
        console.log('[PatientDashboardPage] useEffect - isLoggedIn:', isLoggedIn, 'token:', token);
        if (!isLoggedIn) {
            console.log('[PatientDashboardPage] Not logged in, navigating to login.');
            navigate('/patient/login', { replace: true });
            return;
        }

        if (token) {
            console.log('[PatientDashboardPage] Making API calls with token:', token);
            fetchPatientData();
            fetchMyBills();
        } else {
            console.log('[PatientDashboardPage] Logged in, but no token yet. Waiting for token.');
        }
    }, [isLoggedIn, token, navigate, fetchPatientData, fetchMyBills]);

    const handleLogout = () => {
        logout();
    };

    const handlePayVisitFee = async (visitId) => {
        if (!authContextPatient?.patientId) {
            setActionError('Patient identifier is missing.');
            return;
        }
        setIsProcessingPayment(visitId);
        setActionError('');
        try {
            // Pass only visitId and patientId - no authentication needed for billing service
            const sessionData = await apiCreateCheckoutSessionForVisitFee(visitId, authContextPatient.patientId);
            if (sessionData && sessionData.checkoutUrl) {
                window.location.href = sessionData.checkoutUrl;
            } else {
                setActionError('Failed to get checkout URL for visit fee.');
            }
        } catch (error) {
            console.error('Visit fee payment initiation failed:', error);
            setActionError(`Visit fee payment failed: ${error.message}`);
        } finally {
            setIsProcessingPayment(null);
        }
    };

    if (isLoadingPatientDetails || !authContextPatient) {
        return (
            <GradientContainer>
                <Box sx={{ textAlign: 'center', color: 'white', mt: 10 }}>
                    <CircularProgress sx={{ color: 'white', mb: 2 }} size={60} />
                    <Typography variant="h5">Loading your dashboard...</Typography>
                </Box>
            </GradientContainer>
        );
    }

    const getStatusChipColor = (status) => {
        switch (status) {
            case 'COMPLETED':
            case 'PAID':
                return 'success';
            case 'PENDING':
            case 'AWAITING_PAYMENT':
                return 'warning';
            case 'FAILED':
            case 'EXPIRED':
                return 'error';
            default:
                return 'default';
        }
    };

    return (
        <GradientContainer maxWidth="lg">
            {/* Welcome Header */}
            <WelcomeCard>
                <CardContent sx={{ p: 4 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', width: 56, height: 56 }}>
                                <PersonIcon sx={{ fontSize: 30 }} />
                            </Avatar>
                            <Box>
                                <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', mb: 1 }}>
                                    Welcome back, {authContextPatient.name}!
                                </Typography>
                                <Typography variant="h6" sx={{ opacity: 0.9 }}>
                                    Patient Dashboard
                                </Typography>
                            </Box>
                        </Box>
                        <Button 
                            variant="outlined" 
                            startIcon={<ExitToAppIcon />} 
                            onClick={handleLogout}
                            sx={{ 
                                color: 'white', 
                                borderColor: 'white',
                                '&:hover': {
                                    borderColor: 'white',
                                    backgroundColor: 'rgba(255,255,255,0.1)'
                                }
                            }}
                        >
                            Logout
                        </Button>
                    </Box>
                    {actionError && <Alert severity="error" sx={{ mt: 2 }}>{actionError}</Alert>}
                    {paymentNotification && (
                        <Alert 
                            severity={paymentNotification.status === 'PAID' ? 'success' : 'info'} 
                            sx={{ mt: 2 }}
                            onClose={() => setPaymentNotification(null)}
                        >
                            {paymentNotification.status === 'PAID' 
                                ? `🎉 Payment successful! Your ${paymentNotification.paymentType.replace('_', ' ').toLowerCase()} payment has been processed.`
                                : `Payment status updated: ${paymentNotification.status}`
                            }
                        </Alert>
                    )}
                </CardContent>
            </WelcomeCard>

            <Grid container spacing={3}>
                {/* My Bills Section */}
                <Grid item xs={12} lg={8}>
                    <StyledCard>
                        <StyledCardHeader 
                            title="My Bills & Payments"
                            avatar={<PaymentIcon />}
                            headercolor="linear-gradient(135deg, #27ae60 0%, #2ecc71 100%)"
                        />
                        <CardContent sx={{ p: 3 }}>
                            {isLoadingBills ? (
                                <Box sx={{ textAlign: 'center', py: 4 }}>
                                    <CircularProgress />
                                    <Typography sx={{ mt: 2 }}>Loading billing information...</Typography>
                                </Box>
                            ) : billsError ? (
                                <Alert severity="error">{billsError}</Alert>
                            ) : paymentAttempts.length > 0 ? (
                                <TableContainer sx={{ 
                                    border: '1px solid rgba(0,0,0,0.08)', 
                                    borderRadius: 2,
                                    backgroundColor: '#fafafa'
                                }}>
                                    <Table>
                                        <TableHead>
                                            <TableRow sx={{ 
                                                '& th': { 
                                                    fontWeight: 'bold', 
                                                    backgroundColor: '#f0f8f0',
                                                    borderBottom: '2px solid #e0e0e0',
                                                    color: '#2c3e50'
                                                } 
                                            }}>
                                                <TableCell>Description</TableCell>
                                                <TableCell align="right">Amount</TableCell>
                                                <TableCell>Date</TableCell>
                                                <TableCell>Status</TableCell>
                                                <TableCell>Type</TableCell>
                                                <TableCell>Action</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {paymentAttempts.map((attempt, index) => (
                                                <TableRow 
                                                    key={attempt.id} 
                                                    sx={{ 
                                                        '&:hover': { backgroundColor: '#f5f5f5' },
                                                        backgroundColor: index % 2 === 0 ? 'white' : '#fafafa',
                                                        borderBottom: '1px solid rgba(0,0,0,0.05)'
                                                    }}
                                                >
                                                    <TableCell component="th" scope="row">
                                                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                                                            {attempt.productName}
                                                            {attempt.paymentType === 'VISIT_FEE' && attempt.visitId && ` (Visit ID: ${attempt.visitId})`}
                                                        </Typography>
                                                    </TableCell>
                                                    <TableCell align="right">
                                                        <Typography variant="body2" sx={{ fontWeight: 600, color: 'primary.main' }}>
                                                            ${parseFloat(attempt.amount).toFixed(2)} {attempt.currency.toUpperCase()}
                                                        </Typography>
                                                    </TableCell>
                                                    <TableCell>{new Date(attempt.createdTimestamp).toLocaleDateString()}</TableCell>
                                                    <TableCell>
                                                        <Chip label={attempt.status} color={getStatusChipColor(attempt.status)} size="small" />
                                                    </TableCell>
                                                    <TableCell>{attempt.paymentType ? attempt.paymentType.replace('_', ' ') : 'N/A'}</TableCell>
                                                    <TableCell>
                                                        {attempt.paymentType === 'VISIT_FEE' && attempt.status === 'AWAITING_PAYMENT' && (
                                                            <Button 
                                                                variant="contained" 
                                                                color="secondary" 
                                                                size="small"
                                                                onClick={() => handlePayVisitFee(attempt.visitId)}
                                                                disabled={isProcessingPayment === attempt.visitId}
                                                                sx={{ borderRadius: 2 }}
                                                            >
                                                                {isProcessingPayment === attempt.visitId ? <CircularProgress size={20} color="inherit" /> : 'Pay Now'}
                                                            </Button>
                                                        )}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </TableContainer>
                            ) : (
                                <Box sx={{ textAlign: 'center', py: 4 }}>
                                    <PaymentIcon sx={{ fontSize: 48, color: 'grey.400', mb: 2 }} />
                                    <Typography color="textSecondary">No payment history found.</Typography>
                                </Box>
                            )}
                        </CardContent>
                    </StyledCard>
                </Grid>

                {/* Right Column - Appointment & History */}
                <Grid item xs={12} lg={4}>
                    <Grid container spacing={3}>
                        {/* Main Appointment Section */}
                        <Grid item xs={12}>
                            <StyledCard>
                                <StyledCardHeader 
                                    title="Main Appointment"
                                    avatar={<EventAvailableIcon />}
                                    headercolor="linear-gradient(135deg, #3498db 0%, #2980b9 100%)"
                                />
                                <CardContent sx={{ p: 3 }}>
                                    {isLoadingPatientDetails ? (
                                        <CircularProgress size={20}/>
                                    ) : patientDetails && patientDetails.appointmentDateTime ? (
                                        <Box>
                                            <Typography variant="body1" sx={{ fontWeight: 500, mb: 1 }}>
                                                Dr. {patientDetails.appointmentDoctorName}
                                            </Typography>
                                            <Typography variant="body2" color="textSecondary">
                                                {new Date(patientDetails.appointmentDateTime).toLocaleString()}
                                            </Typography>
                                        </Box>
                                    ) : (
                                        <Box sx={{ textAlign: 'center', py: 2 }}>
                                            <EventAvailableIcon sx={{ fontSize: 36, color: 'grey.400', mb: 1 }} />
                                            <Typography color="textSecondary">No main appointment scheduled.</Typography>
                                        </Box>
                                    )}
                                </CardContent>
                            </StyledCard>
                        </Grid>

                        {/* Visit History Section */}
                        <Grid item xs={12}>
                            <StyledCard>
                                <StyledCardHeader 
                                    title="Visit History & Appointments"
                                    avatar={<HistoryIcon />}
                                    headercolor="linear-gradient(135deg, #e67e22 0%, #d35400 100%)"
                                />
                                <CardContent sx={{ p: 3, maxHeight: '400px', overflowY: 'auto' }}>
                                    {isLoadingPatientDetails ? (
                                        <CircularProgress />
                                    ) : patientDetails && patientDetails.visits && patientDetails.visits.length > 0 ? (
                                        <List dense sx={{ 
                                            backgroundColor: 'rgba(248, 255, 254, 0.3)',
                                            borderRadius: 2,
                                            border: '1px solid rgba(0,0,0,0.08)',
                                            padding: 1
                                        }}>
                                            {patientDetails.visits.map((visit, index) => (
                                                <React.Fragment key={visit.id}>
                                                    <ListItem sx={{ 
                                                        flexDirection: 'column', 
                                                        alignItems: 'flex-start', 
                                                        p: 2, 
                                                        borderRadius: 1, 
                                                        backgroundColor: index % 2 === 0 ? 'rgba(255,255,255,0.8)' : 'rgba(240,248,240,0.5)',
                                                        border: '1px solid rgba(0,0,0,0.05)',
                                                        mb: 1
                                                    }}>
                                                        <ListItemText 
                                                            primary={
                                                                <Typography variant="subtitle2" sx={{ fontWeight: 600, color: 'primary.main' }}>
                                                                    Problem: {visit.problemDescription}
                                                                </Typography>
                                                            }
                                                            secondaryTypographyProps={{ component: 'div' }}
                                                            secondary={
                                                                <Box sx={{ mt: 1 }}>
                                                                    <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                                                                        <strong>Date:</strong> {new Date(visit.visitDate).toLocaleDateString()}
                                                                    </Typography>
                                                                    <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                                                                        <strong>Fee:</strong> {visit.consultationFee ? `$${visit.consultationFee}` : 'N/A'} 
                                                                        <Chip label={visit.visitPaymentStatus} color={getStatusChipColor(visit.visitPaymentStatus)} size="small" sx={{ ml: 1, height: 16 }}/>
                                                                    </Typography>
                                                                    <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                                                                        <strong>Notes:</strong> {visit.notes || 'No notes available'}
                                                                    </Typography>
                                                                    <Typography variant="caption" display="block">
                                                                        <strong>Appointment:</strong> {visit.appointmentDateTime ? `Dr. ${visit.appointmentDoctorName} on ${new Date(visit.appointmentDateTime).toLocaleString()}` : 'Not Scheduled'}
                                                                    </Typography>
                                                                </Box>
                                                            }
                                                        />
                                                    </ListItem>
                                                    {index < patientDetails.visits.length - 1 && <Divider variant="inset" component="li" />}
                                                </React.Fragment>
                                            ))}
                                        </List>
                                    ) : (
                                        <Box sx={{ textAlign: 'center', py: 4 }}>
                                            <HistoryIcon sx={{ fontSize: 36, color: 'grey.400', mb: 1 }} />
                                            <Typography color="textSecondary">No visit history found.</Typography>
                                        </Box>
                                    )}
                                </CardContent>
                            </StyledCard>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </GradientContainer>
    );
};

export default PatientDashboardPage; 