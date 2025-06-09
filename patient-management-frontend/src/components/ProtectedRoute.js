import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = () => {
    const { isLoggedIn } = useAuth();

    if (!isLoggedIn) {
        // If not logged in, redirect to the patient login page
        return <Navigate to="/patient/login" replace />;
    }

    return <Outlet />; // If logged in, render the child routes/components
};

export default ProtectedRoute; 