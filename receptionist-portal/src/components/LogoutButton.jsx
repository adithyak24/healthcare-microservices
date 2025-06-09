import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import Button from '@mui/material/Button';

const LogoutButton = () => {
  const { logout } = useAuth0();

  return (
    <Button color="inherit" onClick={() => logout({ logoutParams: { returnTo: 'http://localhost:3001' } })}>
      Log Out
    </Button>
  );
};

export default LogoutButton; 