import React from 'react';
import { Routes, Route, Link as RouterLink } from 'react-router-dom';
import HomePage from './pages/HomePage';
import AllPatientsPage from './pages/AllPatientsPage';
import './index.css'; // Ensure global styles are imported

import { useAuth0 } from '@auth0/auth0-react';
import LoginButton from './components/LoginButton';
import LogoutButton from './components/LogoutButton';
// import Profile from './components/Profile'; // No longer needed here

// MUI Imports
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress'; // For loading state
import Avatar from '@mui/material/Avatar';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Chip from '@mui/material/Chip';
import PersonIcon from '@mui/icons-material/Person';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
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
  textDecoration: 'none',
  color: 'inherit',
  '&:hover': {
    textDecoration: 'none',
    color: 'inherit',
  },
}));

const NavigationSection = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  [theme.breakpoints.down('md')]: {
    display: 'none',
  },
}));

const UserSection = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(2),
  marginLeft: 'auto',
}));

const NavButton = styled(Button)(({ theme, active }) => ({
  color: 'white',
  borderRadius: theme.spacing(3),
  padding: theme.spacing(1, 2),
  textTransform: 'none',
  fontWeight: 600,
  backgroundColor: active ? 'rgba(255,255,255,0.25)' : 'transparent',
  border: active ? '1px solid rgba(255,255,255,0.3)' : '1px solid transparent',
  boxShadow: active ? '0 2px 8px rgba(0,0,0,0.15)' : 'none',
  '&:hover': {
    backgroundColor: active ? 'rgba(255,255,255,0.3)' : 'rgba(255,255,255,0.15)',
    border: '1px solid rgba(255,255,255,0.3)',
  },
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

const StyledFooter = styled(Box)(({ theme }) => ({
  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  color: 'white',
  padding: theme.spacing(3),
  textAlign: 'center',
  marginTop: 'auto',
}));

function Navbar() {
  const { isAuthenticated, user } = useAuth0();
  const [anchorEl, setAnchorEl] = React.useState(null);
  const [currentPath, setCurrentPath] = React.useState(window.location.pathname);

  React.useEffect(() => {
    setCurrentPath(window.location.pathname);
  }, [window.location.pathname]);

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  return (
    <StyledAppBar position="static" elevation={0}>
      <StyledToolbar>
        <LogoBox component={RouterLink} to="/">
          <LocalHospitalIcon sx={{ fontSize: 32 }} />
          <Box>
            <Typography variant="h5" component="div" sx={{ fontWeight: 'bold', lineHeight: 1 }}>
              HealthCare
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.9, fontSize: '0.75rem' }}>
              Receptionist Portal
            </Typography>
          </Box>
        </LogoBox>

        {isAuthenticated && (
          <>
            <NavigationSection>
              <NavButton 
                component={RouterLink} 
                to="/" 
                startIcon={<DashboardIcon />}
                active={currentPath === '/' ? 1 : 0}
              >
                Dashboard
              </NavButton>
              <NavButton 
                component={RouterLink} 
                to="/patients" 
                startIcon={<PeopleIcon />}
                active={currentPath === '/patients' ? 1 : 0}
              >
                All Patients
              </NavButton>
            </NavigationSection>

            <UserSection>
              <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
                <Typography variant="subtitle2" sx={{ opacity: 0.9, fontSize: '0.85rem' }}>
                  Logged in as
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {user?.name || user?.email}
                  </Typography>
                  <Chip 
                    label="Receptionist" 
                    size="small" 
                    sx={{ 
                      backgroundColor: 'rgba(255,255,255,0.2)', 
                      color: 'white',
                      height: 20,
                      fontSize: '0.7rem'
                    }} 
                  />
                </Box>
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
                    minWidth: 200,
                    boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
                  }
                }}
              >
                <MenuItem component={RouterLink} to="/" onClick={handleMenuClose} sx={{ gap: 1 }}>
                  <DashboardIcon fontSize="small" />
                  Dashboard
                </MenuItem>
                <MenuItem component={RouterLink} to="/patients" onClick={handleMenuClose} sx={{ gap: 1 }}>
                  <PeopleIcon fontSize="small" />
                  All Patients
                </MenuItem>
                <MenuItem onClick={handleMenuClose} sx={{ gap: 1, color: 'error.main' }}>
                  <ExitToAppIcon fontSize="small" />
                  <LogoutButton />
                </MenuItem>
              </Menu>

              <Box sx={{ display: { xs: 'none', md: 'block' } }}>
                <LogoutButton />
              </Box>
            </UserSection>
          </>
        )}
        
        {!isAuthenticated && (
          <Box sx={{ marginLeft: 'auto' }}>
            <LoginButton />
          </Box>
        )}
      </StyledToolbar>
    </StyledAppBar>
  );
}

function App() {
  const { isAuthenticated, isLoading, error } = useAuth0();

  if (isLoading) {
    return (
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh', 
        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
      }}>
        <Box sx={{ textAlign: 'center' }}>
          <CircularProgress size={60} sx={{ mb: 2 }} />
          <Typography variant="h6">Loading Application...</Typography>
        </Box>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh', 
        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
        p: 3 
      }}>
        <Box sx={{ textAlign: 'center', maxWidth: 500 }}>
          <Typography variant="h4" color="error" gutterBottom>
            Oops! Something went wrong
          </Typography>
          <Typography variant="body1" sx={{ mb: 2 }}>
            {error.message}
          </Typography>
          <Button variant="contained" onClick={() => window.location.reload()}>
            Retry
          </Button>
        </Box>
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      <Box 
        component="main" 
        sx={{ 
          flexGrow: 1,
          backgroundColor: '#f8f9fa',
        }}
      >
        {isAuthenticated ? (
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/patients" element={<AllPatientsPage />} />
          </Routes>
        ) : (
          <Box sx={{ 
            textAlign: 'center', 
            py: 8,
            px: 3,
            background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
            minHeight: 'calc(100vh - 70px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Box>
              <LocalHospitalIcon sx={{ fontSize: 80, color: 'primary.main', mb: 3 }} />
              <Typography variant="h3" gutterBottom sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                Welcome to HealthCare
              </Typography>
              <Typography variant="h5" gutterBottom sx={{ color: 'text.secondary', mb: 4 }}>
                Receptionist Portal
              </Typography>
              <Typography variant="body1" sx={{ mb: 4, maxWidth: 500, mx: 'auto' }}>
                Please log in to access the dashboard and manage patient registrations, appointments, and records.
              </Typography>
              <LoginButton />
            </Box>
          </Box>
        )}
      </Box>
      <StyledFooter>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          &copy; {new Date().getFullYear()} HealthCare Receptionist Portal. All Rights Reserved.
        </Typography>
        <Typography variant="caption" sx={{ opacity: 0.7, mt: 0.5, display: 'block' }}>
          Efficient • Secure • Professional Healthcare Management
        </Typography>
      </StyledFooter>
    </Box>
  );
}

export default App; 