package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Building name must be atleast 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 5, message = "City must be atleast 5 characters")
    private String city;

    @NotBlank
    @Size(min = 5, message = "Street  must be atleast 5 characters")
    private String street;

    @NotBlank
    @Size(min = 2, message = "Country must be atleast 2 characters")
    private String country;

    @NotBlank
    @Size(min = 2, message = "State must be atleast 2 characters")
    private String state;

    @NotBlank
    @Size(min = 6, message = "Pincode must be atleast 6 characters")
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();

    public Address(String state, String pincode, String street, String country, String city, String buildingName) {
        this.state = state;
        this.pincode = pincode;
        this.street = street;
        this.country = country;
        this.city = city;
        this.buildingName = buildingName;
    }
}
