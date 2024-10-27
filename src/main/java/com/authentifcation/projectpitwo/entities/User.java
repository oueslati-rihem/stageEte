package com.authentifcation.projectpitwo.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id ;
    private String userName;
    private String userFirstName;
    private String userLastName;
    private String userPassword;

    @Setter
    private boolean banned;
    @Lob
   @Column(name = "cv",length = 33554432)
    private byte[] cv ;

    //@Lob
   // @Column(name = "image",length = 1000000000)
   // private byte[] image ;
    private String image;
    private String contactNumber ;
    @Column(name = "failedLoginAttempts")
    private int failedLoginAttempts;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "USER_ROLE",
            joinColumns = {
                    @JoinColumn(name = "USER_ID")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "ROLE_ID")
            }
    )
    private Set<Role> role;
    @Column(name = "activation_token")
    private String activationToken;

    @OneToMany(mappedBy = "tuteur")
    @ToString.Exclude
    @JsonIgnore
    List<Offer> Offers;
    @JsonIgnore
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private Set<Room> createdRooms = new HashSet<>();
    @JsonIgnore
    @ManyToMany(mappedBy = "users")
    private Set<Room> joinedRooms = new HashSet<>();


    @OneToMany(mappedBy ="user")
    @JsonIgnore
    Set <Cours> cours ;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy="user")
    private Set<Tentative> tentatives;
}
