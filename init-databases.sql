-- Healthcare Microservices Database Initialization
-- This script creates databases for all microservices

-- Main patient service database (only create if it doesn't exist)
SELECT 'CREATE DATABASE patients_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'patients_db')\gexec

-- Billing service database (only create if it doesn't exist)
SELECT 'CREATE DATABASE billing_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'billing_db')\gexec

-- Analytics service database (only create if it doesn't exist)
SELECT 'CREATE DATABASE analytics_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'analytics_db')\gexec

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE patients_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE billing_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE analytics_db TO postgres;

-- Switch to analytics database and create necessary extensions
\c analytics_db;
-- Create UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Switch to billing database and create necessary extensions
\c billing_db;
-- Create UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Switch to main database and create necessary extensions
\c patients_db;
-- Create UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; 