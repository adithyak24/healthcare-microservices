package com.pm.analyticsservice.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(indexName = "patient-events-analytics")
public class PatientEventDocument {

    @Id
    private String id; // Elasticsearch document ID

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Keyword)
    private UUID patientId;

    @Field(type = FieldType.Text, fielddata = true) // fielddata=true for Kibana visualizations/aggregations on text
    private String name;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Date)
    private LocalDate dateOfBirth;

    @Field(type = FieldType.Date)
    private LocalDate registeredDate;

    @Field(type = FieldType.Text, fielddata = true)
    private String problem;

    @Field(type = FieldType.Keyword) // Or Text if you need to search parts of location names
    private String location;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100) // Stores as long, e.g., 123.45 becomes 12345
    private BigDecimal consultationFee;

    @Field(type = FieldType.Date)
    private LocalDateTime eventTimestamp; // Timestamp from the event DTO

    @Field(type = FieldType.Date)
    private LocalDateTime ingestedTimestamp; // Timestamp when analytics-service processed it

    public PatientEventDocument() {
        this.ingestedTimestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public LocalDateTime getIngestedTimestamp() {
        return ingestedTimestamp;
    }

    public void setIngestedTimestamp(LocalDateTime ingestedTimestamp) {
        this.ingestedTimestamp = ingestedTimestamp;
    }
} 