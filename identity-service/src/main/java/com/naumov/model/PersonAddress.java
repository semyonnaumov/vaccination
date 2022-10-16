package com.naumov.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "people_addresses", uniqueConstraints = {
        @UniqueConstraint(name = "person_id_address_id_uk", columnNames = {"person_id", "address_id"})
})
public class PersonAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "people_addresses_gen")
    @SequenceGenerator(name = "people_addresses_gen", sequenceName = "people_addresses_seq", allocationSize = 10)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id", nullable = false)
    private Person person;
    @ManyToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    private Address address;
    @Column(name = "is_registration", nullable = false)
    private Boolean isRegistration;

    @Override
    public String toString() {
        return "PersonAddress{" +
                "id=" + id +
                ", personId=" + (person != null ? person.getId() : null) +
                ", addressId=" + (address != null ? address.getId() : null) +
                ", isRegistration=" + isRegistration +
                '}';
    }
}
