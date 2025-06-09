import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import { BrowserRouter, useNavigate } from 'react-router-dom'
import { Auth0Provider } from '@auth0/auth0-react'
import { ThemeProvider, createTheme } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'

const auth0Domain = 'dev-8n8hmdpdvrtyezom.us.auth0.com'
const auth0ClientId = 'lkJoirCl1Sm03L6HblU4yZu4dqvvFKVt'
const auth0RedirectUri = 'http://localhost:3001/callback'

// Define a simple theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2', // Default MUI blue
    },
    secondary: {
      main: '#009688', // A teal color
    },
    // You can also define background colors here if needed
    // background: {
    //   default: '#f4f6f8' // A light grey for the page background
    // }
  },
  typography: {
    // You can customize typography here if desired
    // h1: { fontSize: '2.2rem' },
  }
})

// Wrapper component to use useNavigate hook for onRedirectCallback
const Auth0ProviderWithRedirect = ({ children }) => {
  const navigate = useNavigate()

  const onRedirectCallback = (appState) => {
    // If appState.returnTo exists (e.g., from a protected route), navigate there.
    // Otherwise, navigate to the root path.
    navigate(appState?.returnTo || '/')
  }

  return (
    <Auth0Provider
      domain={auth0Domain}
      clientId={auth0ClientId}
      authorizationParams={{
        redirect_uri: auth0RedirectUri,
        audience: 'http://localhost:4004/api/patients/'
      }}
      onRedirectCallback={onRedirectCallback}
    >
      {children}
    </Auth0Provider>
  )
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Auth0ProviderWithRedirect>
          <App />
        </Auth0ProviderWithRedirect>
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>,
) 