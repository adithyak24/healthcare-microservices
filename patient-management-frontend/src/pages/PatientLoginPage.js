import React, { useState } from 'react';
import axios from 'axios';
import PatientLoginForm from '../components/PatientLoginForm';
import { useAuth } from '../context/AuthContext';

// MUI Imports
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Paper from '@mui/material/Paper'; // For a card-like effect for the form

const PatientLoginPage = () => {
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false); // This is for form submission loading
    // const navigate = useNavigate(); // navigate might not be needed here anymore
    const { login, isLoadingAuth } = useAuth();

    const handleLogin = async (credentials) => {
        setError('');
        setIsLoading(true);
        try {
            const response = await axios.post('http://localhost:4004/api/patients/auth/login', credentials);
            setIsLoading(false);
            
            console.log('[PatientLoginPage] Full login response:', response.data);
            
            if (response.data && response.data.patientId) {
                console.log('[PatientLoginPage] Login successful, calling login with:', response.data);
                login(response.data);
                // REMOVED: navigate('/patient/dashboard', { replace: true });
                // AppRoutesInternal will handle navigation when isLoggedIn changes
            } else {
                setError(response.data.message || 'Login failed. Please check your credentials.');
            }
        } catch (err) {
            setIsLoading(false);
            console.error("Login error:", err);
            
            // Handle different types of errors with specific messages
            if (err.response) {
                // Server responded with an error status
                const statusCode = err.response.status;
                const errorData = err.response.data;
                
                if (statusCode === 401) {
                    // Authentication failed - wrong credentials
                    if (errorData && errorData.message) {
                        setError(errorData.message);
                    } else {
                        setError('Invalid email or password. Please check your credentials and try again.');
                    }
                } else if (statusCode === 400) {
                    // Bad request - validation errors
                    if (errorData && errorData.message) {
                        setError(errorData.message);
                    } else {
                        setError('Invalid request. Please check your email and password format.');
                    }
                } else if (statusCode >= 500) {
                    // Server error
                    setError('Server error. Please try again later or contact support.');
                } else {
                    // Other HTTP errors
                    setError(errorData?.message || `Login failed: HTTP ${statusCode}`);
                }
            } else if (err.request) {
                // Request was made but no response received (network/connection issues)
                setError('Unable to connect to the server. Please check your internet connection and ensure the Patient Service is running.');
            } else {
                // Something else happened in making the request
                setError('An unexpected error occurred during login. Please try again.');
            }
        }
    };

    if (isLoadingAuth) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
                <Typography sx={{ ml: 2 }}>Loading authentication...</Typography>
            </Box>
        );
    }

    return (
        <Container component="main" maxWidth="xs"> {/* xs for a smaller, centered form container */}
            <Paper elevation={3} sx={{ marginTop: 8, padding: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Typography component="h1" variant="h5">
                    Patient Portal Login
                </Typography>
                <PatientLoginForm onSubmit={handleLogin} />
                {isLoading && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                        <CircularProgress />
                    </Box>
                )}
                {error && (
                    <Alert severity="error" sx={{ width: '100%', mt: 2 }}>
                        {error}
                    </Alert>
                )}
            </Paper>
        </Container>
    );
};

export default PatientLoginPage; 