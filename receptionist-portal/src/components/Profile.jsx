import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const Profile = () => {
  const { user, isAuthenticated, isLoading } = useAuth0();

  if (isLoading) {
    return <div>Loading user profile...</div>;
  }

  return (
    isAuthenticated && (
      <div>
        {/* {user.picture && <img src={user.picture} alt={user.name} style={{ width: '50px', height: '50px', borderRadius: '50%' }}/>} Removed profile picture */}
        <h2>Welcome, {user.name}</h2>
        <p>Email: {user.email}</p>
        {/* <pre>{JSON.stringify(user, null, 2)}</pre> */}
      </div>
    )
  );
};

export default Profile; 