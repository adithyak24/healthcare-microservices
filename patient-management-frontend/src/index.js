import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import 'bootstrap/dist/css/bootstrap.min.css';

import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

// Define a simple theme (similar to receptionist-portal for consistency)
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2', // Default MUI blue
    },
    secondary: {
      main: '#009688', // A teal color
    },
    // background: {
    //   default: '#f4f6f8' // Light grey for the page background
    // }
  },
  typography: {
    // Customize typography if desired
  }
});

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <App />
    </ThemeProvider>
  </React.StrictMode>
);
