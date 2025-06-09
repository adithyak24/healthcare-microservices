package com.pm.patientservice.dto;

import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;

public class PatientRequestDTO {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name cannot exceed 100 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  // Address is optional - removed @NotBlank to match frontend UI
  private String address;

  @NotBlank(message = "Date of birth is required")
  private String dateOfBirth;

  @NotBlank(groups = CreatePatientValidationGroup.class, message =
      "Registered date is required")
  private String registeredDate;

  @NotBlank(message = "Problem is required")
  @Size(max = 255, message = "Problem description cannot exceed 255 characters")
  private String problem;

  @NotBlank(message = "Location is required")
  @Size(max = 150, message = "Location cannot exceed 150 characters")
  private String location;

  @NotBlank(message = "Consultation fee is required")
  @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Consultation fee must be a valid number with up to two decimal places")
  @DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee must be zero or positive")
  private String consultationFee;

  public @NotBlank(message = "Name is required") @Size(max = 100, message = "Name cannot exceed 100 characters") String getName() {
    return name;
  }

  public void setName(
      @NotBlank(message = "Name is required") @Size(max = 100, message = "Name cannot exceed 100 characters") String name) {
    this.name = name;
  }

  public @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String getEmail() {
    return email;
  }

  public void setEmail(
      @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email) {
    this.email = email;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(
      String address) {
    this.address = address;
  }

  public @NotBlank(message = "Date of birth is required") String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(
      @NotBlank(message = "Date of birth is required") String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getRegisteredDate() {
    return registeredDate;
  }

  public void setRegisteredDate(String registeredDate) {
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

  public String getConsultationFee() {
    return consultationFee;
  }

  public void setConsultationFee(String consultationFee) {
    this.consultationFee = consultationFee;
  }

}
