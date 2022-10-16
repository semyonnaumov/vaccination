package com.naumov.model;

import lombok.*;

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
    private String phoneNumber;

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", ownerId=" + (owner != null ? owner.getId() : null) +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
