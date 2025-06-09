import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const PaymentCancelPage = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const timer = setTimeout(() => {
            navigate('/patient/dashboard');
        }, 3000); // 3 seconds delay

        return () => clearTimeout(timer);
    }, [navigate]);

    return (
        <div style={{ textAlign: 'center', marginTop: '50px', padding: '20px' }} className="dashboard-container">
            <h2 style={{color: 'orange'}}>Payment Cancelled</h2>
            <p>Your payment process was cancelled or was not completed.</p>
            <p>If you faced any issues, please try again or contact support.</p>
            <p>You will be redirected to your dashboard shortly.</p>
            <Link to="/patient/dashboard">Back to Dashboard</Link>
        </div>
    );
};

export default PaymentCancelPage; 