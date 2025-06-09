import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const PaymentSuccessPage = () => {
    const navigate = useNavigate();

    useEffect(() => {
        // Automatically redirect to dashboard after a few seconds
        const timer = setTimeout(() => {
            navigate('/patient/dashboard');
        }, 3000); // 3 seconds delay

        return () => clearTimeout(timer); // Cleanup timer
    }, [navigate]);

    return (
        <div style={{ textAlign: 'center', marginTop: '50px', padding: '20px' }} className="dashboard-container">
            <h2 style={{color: 'green'}}>Payment Successful!</h2>
            <p>Your payment has been processed successfully.</p>
            <p>You will be redirected to your dashboard shortly.</p>
            <Link to="/patient/dashboard">Go to Dashboard Now</Link>
        </div>
    );
};

export default PaymentSuccessPage; 