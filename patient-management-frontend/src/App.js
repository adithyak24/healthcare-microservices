import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, Link as RouterLink, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import PatientLoginPage from './pages/PatientLoginPage';
import PatientDashboardPage from './pages/PatientDashboardPage';
import PaymentSuccessPage from './pages/PaymentSuccessPage';
import PaymentCancelPage from './pages/PaymentCancelPage';
import './index.css';

// MUI Imports
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Avatar from '@mui/material/Avatar';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import PersonIcon from '@mui/icons-material/Person';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import DashboardIcon from '@mui/icons-material/Dashboard';
import LocalHospitalIcon from '@mui/icons-material/LocalHospital';
import { styled } from '@mui/material/styles';

// Styled Navigation Components
const StyledAppBar = styled(AppBar)(({ theme }) => ({
  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
}));

const StyledToolbar = styled(Toolbar)(({ theme }) => ({
  minHeight: '70px',
  padding: `0 ${theme.spacing(2)}`,
  [theme.breakpoints.up('sm')]: {
    padding: `0 ${theme.spacing(3)}`,
  },
}));

const LogoBox = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  flexGrow: 1,
  textDecoration: 'none',
  color: 'inherit',
  '&:hover': {
    textDecoration: 'none',
    color: 'inherit',
  },
}));

const UserSection = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(2),
}));

const UserAvatar = styled(Avatar)(({ theme }) => ({
  backgroundColor: 'rgba(255,255,255,0.2)',
  width: 40,
  height: 40,
  cursor: 'pointer',
  transition: 'all 0.2s ease-in-out',
  '&:hover': {
    backgroundColor: 'rgba(255,255,255,0.3)',
    transform: 'scale(1.05)',
  },
}));

const LogoutButton = styled(Button)(({ theme }) => ({
  color: 'white',
  borderColor: 'rgba(255,255,255,0.5)',
  borderRadius: theme.spacing(3),
  padding: theme.spacing(1, 2),
  textTransform: 'none',
  fontWeight: 600,
  '&:hover': {
    borderColor: 'white',
    backgroundColor: 'rgba(255,255,255,0.1)',
  },
}));

const Navbar = () => {
  const { isLoggedIn, currentPatient, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = React.useState(null);

  const handleLogout = () => {
    logout();
    navigate('/patient/login');
    setAnchorEl(null);
  };

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleDashboard = () => {
    navigate('/patient/dashboard');
    setAnchorEl(null);
  };

  return (
    <StyledAppBar position="static" elevation={0}>
      <StyledToolbar>
        <LogoBox 
          component={RouterLink} 
          to={isLoggedIn ? "/patient/dashboard" : "/patient/login"}
        >
          <LocalHospitalIcon sx={{ fontSize: 32 }} />
          <Box>
            <Typography variant="h5" component="div" sx={{ fontWeight: 'bold', lineHeight: 1 }}>
              HealthCare
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.9, fontSize: '0.75rem' }}>
              Patient Portal
            </Typography>
          </Box>
        </LogoBox>

        {isLoggedIn && currentPatient && (
          <UserSection>
            <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
              <Typography variant="subtitle2" sx={{ opacity: 0.9, fontSize: '0.85rem' }}>
                Welcome back
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {currentPatient.name}
              </Typography>
            </Box>
            
            <UserAvatar onClick={handleMenuOpen}>
              <PersonIcon />
            </UserAvatar>

            <Menu
              anchorEl={anchorEl}
              open={Boolean(anchorEl)}
              onClose={handleMenuClose}
              PaperProps={{
                sx: {
                  borderRadius: 2,
                  mt: 1,
                  minWidth: 180,
                  boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
                }
              }}
            >
              <MenuItem onClick={handleDashboard} sx={{ gap: 1 }}>
                <DashboardIcon fontSize="small" />
                Dashboard
              </MenuItem>
              <MenuItem onClick={handleLogout} sx={{ gap: 1, color: 'error.main' }}>
                <ExitToAppIcon fontSize="small" />
                Logout
              </MenuItem>
            </Menu>

            <LogoutButton 
              variant="outlined" 
              startIcon={<ExitToAppIcon />} 
              onClick={handleLogout}
              sx={{ display: { xs: 'none', md: 'flex' } }}
            >
              Logout
            </LogoutButton>
          </UserSection>
        )}
      </StyledToolbar>
    </StyledAppBar>
  );
};

const AppRoutesInternal = () => {
  const { isLoggedIn, isLoadingAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  console.log('[AppRoutesInternal] Rendering with isLoggedIn:', isLoggedIn, 'isLoadingAuth:', isLoadingAuth, 'current path:', location.pathname);

  useEffect(() => {
    if (!isLoadingAuth) {
      console.log('[AppRoutesInternal] Auth loading complete, checking navigation needs');
      
      if (location.pathname === '/' || location.pathname === '/patient') {
        const targetPath = isLoggedIn ? '/patient/dashboard' : '/patient/login';
        console.log('[AppRoutesInternal] Redirecting from root to:', targetPath);
        navigate(targetPath, { replace: true });
      } else if (location.pathname === '/patient/login' && isLoggedIn) {
        console.log('[AppRoutesInternal] User is logged in but on login page, redirecting to dashboard');
        navigate('/patient/dashboard', { replace: true });
      }
    }
  }, [isLoggedIn, isLoadingAuth, location.pathname, navigate]);

  if (isLoadingAuth) {
    console.log('[AppRoutesInternal] Showing loading indicator');
    return (
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        flexGrow: 1, 
        height: 'calc(100vh - 70px)',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
      }}>
        <Box sx={{ textAlign: 'center' }}>
          <CircularProgress size={60} sx={{ mb: 2 }} />
          <Typography variant="h6">Loading application...</Typography>
        </Box>
      </Box>
    );
  }

  console.log('[AppRoutesInternal] Auth loading complete, rendering routes');

  return (
    <Routes>
      <Route path="/patient/login" element={<PatientLoginPage />} />
      <Route path="/patient/dashboard" element={<PatientDashboardPage />} />
      <Route path="/patient/payment/success" element={<PaymentSuccessPage />} />
      <Route path="/patient/payment/cancel" element={<PaymentCancelPage />} />
      <Route path="*" element={<Navigate to={isLoggedIn ? "/patient/dashboard" : "/patient/login"} replace />} />
    </Routes>
  );
};

const StyledFooter = styled(Box)(({ theme }) => ({
  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  color: 'white',
  padding: theme.spacing(3),
  textAlign: 'center',
  marginTop: 'auto',
}));

function App() {
  return (
    <AuthProvider>
      <Router>
        <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
          <Navbar />
          <Box component="main" sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
            <AppRoutesInternal />
          </Box>
          <StyledFooter>
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              &copy; {new Date().getFullYear()} HealthCare Patient Portal. All Rights Reserved.
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.7, mt: 0.5, display: 'block' }}>
              Secure • Private • Reliable Healthcare Management
            </Typography>
          </StyledFooter>
        </Box>
      </Router>
    </AuthProvider>
  );
}

export default App;
