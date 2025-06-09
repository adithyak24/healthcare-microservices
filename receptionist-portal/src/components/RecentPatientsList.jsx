import React from 'react';

// MUI Imports
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Avatar from '@mui/material/Avatar';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import PersonIcon from '@mui/icons-material/Person';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import { styled } from '@mui/material/styles';

const StyledListItem = styled(ListItem)(({ theme }) => ({
  backgroundColor: '#ffffff',
  borderRadius: theme.spacing(1.5),
  marginBottom: theme.spacing(1),
  boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
  border: `1px solid rgba(0,0,0,0.08)`,
  transition: 'all 0.2s ease-in-out',
  '&:hover': {
    transform: 'translateY(-2px)',
    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
    backgroundColor: '#f8fffe',
    border: `1px solid rgba(0,0,0,0.12)`,
  },
  '&:last-child': {
    marginBottom: 0,
  },
}));

const PatientAvatar = styled(Avatar)(({ theme }) => ({
  background: 'linear-gradient(135deg, #3498db 0%, #2980b9 100%)',
  width: 48,
  height: 48,
}));

function RecentPatientsList({ patients }) {
  if (!patients || patients.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <PersonIcon sx={{ fontSize: 48, color: 'grey.400', mb: 2 }} />
        <Typography variant="subtitle1" color="textSecondary">
          No recent patients to display.
        </Typography>
      </Box>
    );
  }

  return (
    <List sx={{ width: '100%', padding: 0 }}>
      {patients.map((patient, index) => (
        <StyledListItem 
          key={patient.id || patient.email} 
          alignItems="flex-start"
        >
          <ListItemAvatar>
            <PatientAvatar>
              <PersonIcon />
            </PatientAvatar>
          </ListItemAvatar>
          <ListItemText
            primary={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, color: '#2c3e50' }}>
                  {patient.name}
                </Typography>
                <Chip 
                  label="New" 
                  size="small" 
                  sx={{ 
                    height: 20, 
                    fontSize: '0.7rem',
                    backgroundColor: '#d5f4e6',
                    color: '#27ae60',
                    border: '1px solid #a9dfbf'
                  }}
                />
              </Box>
            }
            secondary={
              <Box sx={{ mt: 1 }}>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                  📧 {patient.email}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                  🩺 Problem: {patient.problem || 'N/A'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  📅 Registered: {patient.registeredDate ? new Date(patient.registeredDate).toLocaleDateString() : 'N/A'}
                </Typography>
              </Box>
            }
          />
        </StyledListItem>
      ))}
    </List>
  );
}

export default RecentPatientsList; 