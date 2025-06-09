import React from 'react';
import { Link } from 'react-router-dom';
import '../App.css'; // Assuming some basic styles might be in App.css

const Navbar = () => {
  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/" className="navbar-item brand-text">Patient Management</Link>
      </div>
      <div className="navbar-menu">
        <div className="navbar-start">
          <Link to="/" className="navbar-item">
            Home
          </Link>
          <Link to="/patients" className="navbar-item">
            Patients
          </Link>
        </div>
      </div>
    </nav>
  );
};

export default Navbar; 