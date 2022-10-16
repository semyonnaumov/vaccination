package com.naumov.model;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contacts_gen")
    @SequenceGenerator(name = "contacts_gen", sequenceName = "contacts_seq", allocationSize = 10)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Person owner;
    @Column(name = "phone_number", length = 12, nullable = false, unique = true)
    @Type(type = "")
    private String phoneNumber;
}
