﻿# 🏥 Healthcare Patient Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.0-blue.svg)](https://reactjs.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 💡 Project Philosophy

**What started as a simple healthcare management idea evolved into a showcase of enterprise-grade system design principles.** Rather than building just another CRUD application, this project demonstrates how to architect a **robust, scalable, and fault-tolerant distributed system** using industry best practices and cutting-edge technologies.

This system exemplifies **perfect system design implementation** featuring:

🔄 **Event-Driven Architecture** with **Apache Kafka** for real-time data streaming  
⚡ **High-Performance Communication** via **gRPC** for inter-service calls  
🚀 **Intelligent Caching** with **Redis** and strategic TTL management  
🛡️ **Advanced Security** including **Bloom Filters** for brute-force protection  
🔧 **Fault Tolerance** with **Circuit Breaker** patterns and graceful degradation  
📊 **Performance Optimization** through **Database Indexing** and query optimization  
🌐 **Traffic Management** with **Rate Limiting** and API protection  
📈 **Comprehensive Monitoring** using **ELK Stack** (Elasticsearch, Logstash, Kibana)  
🏗️ **Microservices Excellence** with proper service boundaries and communication patterns

A comprehensive microservices-based healthcare management system that demonstrates production-ready enterprise architecture, handling patient registration, appointment scheduling, billing with Stripe integration, real-time analytics, and automated notifications.

## 🌟 Features

### 📊 **Patient Management**
- **Patient Registration & Authentication** with JWT security
- **Medical History Tracking** with visit records
- **Appointment Scheduling** with business hours validation
- **Real-time Dashboard** with visit history and payment status

### 💳 **Billing & Payments**
- **Stripe Integration** for secure payment processing
- **Visit-based Billing** with consultation fees
- **Payment History** with detailed transaction records
- **Automated Payment Notifications** via email

### 👩‍⚕️ **Receptionist Portal**
- **Patient Overview** with advanced filtering (Paid/Unpaid/Scheduled)
- **Appointment Management** with calendar integration
- **Visit Creation** and medical record management
- **Payment Status Tracking** and scheduling workflows

### 📈 **Analytics & Monitoring**
- **Elasticsearch Integration** for real-time data indexing
- **Kibana Dashboards** for patient analytics
- **Circuit Breaker Pattern** for fault tolerance
- **Redis Caching** for improved performance

### 🔔 **Notifications**
- **Email Notifications** for appointment confirmations
- **Payment Confirmations** with receipt details
- **Kafka Event Streaming** for real-time updates

## 🏗️ Architecture

```mermaid
graph TB
    subgraph "Frontend Layer"
        PP[Patient Portal<br/>React - Port 3000]
        RP[Receptionist Portal<br/>React - Port 3001]
    end
    
    subgraph "API Gateway"
        AG[Spring Cloud Gateway<br/>Port 4004]
    end
    
    subgraph "Microservices Layer"
        PS[Patient Service<br/>Spring Boot - Port 4000]
        BS[Billing Service<br/>Spring Boot + gRPC<br/>Ports 9000/9001]
        AS[Analytics Service<br/>Spring Boot - Port 9002]
        NS[Notification Service<br/>Spring Boot - Port 9003]
    end
    
    subgraph "Data Storage"
        PG[(PostgreSQL<br/>Databases)]
        RD[(Redis<br/>Cache)]
        ES[(Elasticsearch<br/>Search & Analytics)]
    end
    
    subgraph "Event Streaming"
        KF[Apache Kafka<br/>Message Broker]
    end
    
    subgraph "External APIs"
        ST[Stripe<br/>Payment Gateway]
        EM[SMTP<br/>Email Service]
    end
    
    %% Frontend to API Gateway
    PP --> AG
    RP --> AG
    
    %% API Gateway to Services
    AG --> PS
    AG --> BS
    
    %% Service to Database connections
    PS --> PG
    PS --> RD
    BS --> PG
    AS --> PG
    AS --> ES
    
    %% Service to Kafka connections
    PS --> KF
    BS --> KF
    AS --> KF
    NS --> KF
    
    %% External service connections
    BS --> ST
    NS --> EM
    
    %% Inter-service communication
    PS -.->|gRPC| BS
```

## 🔄 Data Flow

### Core Workflow
1. **Patient Registration** → Patient Service → Database + Cache + Kafka Event
2. **Visit Creation** → Billing Service → Payment Attempt + Event Notification  
3. **Payment Processing** → Stripe → Webhook → Email Notification
4. **Analytics** → Kafka Events → Elasticsearch → Kibana Dashboards

### Communication Patterns
- **Synchronous**: gRPC for real-time inter-service calls
- **Asynchronous**: Kafka for event streaming and notifications
- **Caching**: Redis for performance optimization
- **Monitoring**: Circuit breakers for fault tolerance

## 🚀 Quick Start

### Prerequisites

- **Docker & Docker Compose** (Latest version)
- **Java 21** (for local development)
- **Node.js 18+** (for frontend development)
- **Stripe Account** (for payment processing)
- **Gmail Account** (for email notifications)

### 1. Clone Repository

```bash
git clone https://github.com/adithyak24/healthcare-microservices.git
cd healthcare-microservices
```

### 2. Environment Setup

Create a `.env` file in the root directory:

```env
# Stripe Configuration (Required)
STRIPE_API_KEY=sk_test_your_stripe_secret_key_here

# Email Configuration (Required for notifications)
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_gmail_app_password
SPRING_MAIL_FROM=your_email@gmail.com

```

### 3. Start All Services

```bash
# Start complete healthcare system
docker compose -f docker-compose.hub.yml up -d

# View logs
docker compose -f docker-compose.hub.yml logs -f
```

### 4. Access Applications

| Service | URL | Purpose |
|---------|-----|---------|
| **Patient Portal** | http://localhost:3000 | Patient registration, appointments, payments |
| **Receptionist Portal** | http://localhost:3001 | Staff management, scheduling, patient overview |
| **API Gateway** | http://localhost:4004 | API routing and rate limiting |
| **Kibana Analytics** | http://localhost:5601 | Patient data analytics and insights |

## 📱 Application Workflows

### **Patient Journey**
1. **Registration**: Patient creates account with medical details
2. **Payment**: Secure consultation fee payment via Stripe
3. **Scheduling**: Book appointments with available doctors
4. **Visit Management**: Track visit history and medical records
5. **Notifications**: Receive email confirmations and reminders

### **Receptionist Workflow**
1. **Patient Overview**: View all patients with status filtering
2. **Visit Creation**: Add new visits and medical problems
3. **Appointment Scheduling**: Schedule appointments with business hours validation
4. **Payment Tracking**: Monitor payment status and send reminders

## 🛠️ Development

### Local Development Setup

```bash
# Backend Services (requires Java 21)
./mvnw spring-boot:run -pl patient-service
./mvnw spring-boot:run -pl billing-service
./mvnw spring-boot:run -pl analytics-service
./mvnw spring-boot:run -pl notification-service

# Frontend Applications (requires Node.js 18+)
cd patient-management-frontend && npm install && npm start
cd receptionist-portal && npm install && npm start
```

### Service Ports

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| Patient Service | 4000 | patients_db | Core patient management |
| Billing Service | 9000 | billing_db | Payment processing |
| Analytics Service | 9002 | analytics_db | Data analytics |
| Notification Service | 9003 | - | Email notifications |
| API Gateway | 4004 | - | Request routing |
| PostgreSQL | 5432 | Multiple | Data persistence |
| Redis | 6379 | - | Caching layer |
| Elasticsearch | 9200 | - | Search & analytics |
| Kafka | 9092 | - | Event streaming |

## 🔧 System Design Implementation

### 🏛️ Enterprise Architecture Stack

**Backend Framework**
- **Spring Boot 3.4.1** with Spring Security, Spring Data JPA
- **gRPC** for high-performance inter-service communication
- **Apache Kafka** for event-driven messaging and data streaming

**Frontend Technology**
- **React 18** with Material-UI for modern responsive interfaces
- **Auth0 React SDK** for authentication flows

**Data & Storage Layer**
- **PostgreSQL 15** with strategic indexing for optimal query performance
- **Redis 7** with intelligent 30-minute TTL caching strategy
- **Elasticsearch 8.14** with Kibana for real-time analytics and log aggregation

**Security & Resilience**
- **JWT Authentication** with secure token management
- **Auth0 Integration** for enterprise-grade authentication and user management
- **Bloom Filters** for efficient brute-force attack prevention
- **Circuit Breaker Pattern** (Resilience4j) for fault tolerance
- **Rate Limiting** with customizable API protection
- **Stripe Payment Processing** with secure webhook integration and PCI compliance

### 🏗️ Advanced System Design Patterns

**Event-Driven Architecture**
- **Apache Kafka** for asynchronous event streaming
- **Event Sourcing** for complete audit trails
- **CQRS Pattern** separation for read/write operations

**Performance Optimization**
- **Database Indexing**: Strategic indexes on email, dates, and status fields
- **Redis Caching**: Multi-layer caching with intelligent invalidation
- **Connection Pooling**: Optimized database connection management
- **Lazy Loading**: Efficient data fetching strategies

**Fault Tolerance & Monitoring**
- **Circuit Breakers**: Graceful degradation for external service failures
- **Health Checks**: Comprehensive service monitoring with actuators
- **ELK Stack Integration**: Centralized logging with Elasticsearch, Logstash, and Kibana
- **Distributed Tracing**: Request flow tracking across microservices

**Security Implementation**
- **Multi-layer Security**: JWT + Role-based access control
- **Bloom Filter**: Efficient password attack prevention
- **Rate Limiting**: Distributed rate limiting with Redis backend
- **Input Validation**: Comprehensive sanitization and validation

## 📊 Monitoring & Analytics

### Health Checks

```bash
# Service health status
curl http://localhost:4000/actuator/health  # Patient Service
curl http://localhost:9000/actuator/health  # Billing Service
curl http://localhost:9002/health           # Analytics Service

# Circuit breaker status
curl http://localhost:4000/actuator/circuitbreakers
```

### Elasticsearch Analytics

- **Patient Registration Trends**: Track signup patterns
- **Payment Analytics**: Revenue and payment success rates
- **Visit Patterns**: Appointment and visit analytics
- **System Performance**: Service response times and errors

Access Kibana at http://localhost:5601 to create custom dashboards.

## 🚨 Troubleshooting

### Common Issues

**Services Not Starting**
```bash
# Check Docker resources
docker system df
docker compose -f docker-compose.hub.yml ps

# View specific service logs
docker compose -f docker-compose.hub.yml logs patient-service
```

**Database Connection Issues**
```bash
# Test database connectivity
docker compose -f docker-compose.hub.yml exec postgres pg_isready
docker compose -f docker-compose.hub.yml exec postgres psql -U postgres -l
```

**Elasticsearch Not Receiving Data**
```bash
# Check analytics service logs
docker compose -f docker-compose.hub.yml logs analytics-service

# Test Elasticsearch
curl http://localhost:9200/_cluster/health
curl http://localhost:9002/test/create-sample-document
```

## 🔄 Updates & Maintenance

### Updating Services

```bash
# Update specific service
docker compose -f docker-compose.hub.yml build --no-cache patient-service
docker compose -f docker-compose.hub.yml up -d patient-service

# Update all services
docker compose -f docker-compose.hub.yml build --no-cache
docker compose -f docker-compose.hub.yml up -d
```

### Database Migrations

```bash
# Connect to database
docker compose -f docker-compose.hub.yml exec postgres psql -U postgres -d patients_db

# Backup database
docker compose -f docker-compose.hub.yml exec postgres pg_dump -U postgres patients_db > backup.sql
```

## 🚀 Future Roadmap & Enhancements

This project serves as a **foundation for enterprise-scale healthcare systems**. The following improvements are planned to demonstrate **advanced distributed systems concepts** and **cloud-native architectures**:

### 🔧 **Advanced Functionalities**
- **Video Consultation Integration** with WebRTC for telemedicine
- **AI-Powered Diagnostic Assistant** using machine learning models
- **Real-time Chat System** between patients and healthcare providers
- **Multi-language Support** for global healthcare accessibility

### 🗄️ **Scalability & Data Management**
- **Database Sharding** for horizontal scaling across multiple PostgreSQL instances
- **Data Partitioning** strategies for patient records based on geographical regions
- **Consistent Hashing** for efficient data distribution and load balancing
- **Read Replicas** implementation for improved query performance
- **Database Federation** across multiple data centers
- **Event Store** implementation for complete audit trails and data recovery

### 🌐 **Distributed Systems Theory**
- **CAP Theorem Implementation**: Demonstrating Consistency, Availability, and Partition tolerance trade-offs
- **SAGA Pattern**: Distributed transaction management across microservices

### ☁️ **Cloud-Native & Kubernetes**
- **Kubernetes Deployment** with Helm charts and operators
- **Service Mesh** implementation with Istio for advanced traffic management
- **Horizontal Pod Autoscaling** based on custom metrics
- **Kubernetes Secrets Management** with Vault integration
- **Multi-cluster Deployment** for disaster recovery and global availability
- **GitOps Workflow** with ArgoCD for continuous deployment

### 🔗 **AWS Integration & Cloud Services**
- **Amazon EKS** deployment with managed Kubernetes
- **AWS RDS** with Multi-AZ deployment for high availability
- **Amazon ElastiCache** for Redis clustering
- **AWS Lambda** for serverless computing workflows
- **Amazon S3** for medical document storage with encryption
- **AWS CloudFront** for global content delivery
- **Amazon SQS/SNS** for additional messaging patterns
- **Amazon CloudWatch** for comprehensive monitoring and alerting

This roadmap demonstrates a progression from a **functional healthcare system** to a **world-class, enterprise-grade platform** that can handle millions of users while maintaining the highest standards of **security, performance, and reliability**.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Adithya Reddy Koppula**
- GitHub: [@adithyak24](https://github.com/adithyak24)
- Email: adithya3698@gmail.com

## 🙏 Acknowledgments

- Spring Boot community for excellent documentation
- React and Material-UI teams for UI components
- Stripe for secure payment processing
- Elasticsearch team for powerful search capabilities

---

**⭐ Star this repository if you find it helpful!**
