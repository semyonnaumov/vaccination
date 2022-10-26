package com.naumov.model;

import com.naumov.util.AbstractBuilder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static com.naumov.util.JsonUtil.extractId;

@Getter
@Setter
@Entity
@Table(name = "people_addresses", uniqueConstraints = {
        @UniqueConstraint(name = "person_id_address_id_uk", columnNames = {"person_id", "address_id"})
})
public class PersonAddress implements IdentifiableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "people_addresses_gen")
    @SequenceGenerator(name = "people_addresses_gen", sequenceName = "people_addresses_seq", allocationSize = 10)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id", referencedColumnName = "id", nullable = false)
    private Person person;
    @ManyToOne(optional = false)
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    private Address address;
    @Column(name = "is_registration", nullable = false)
    private Boolean isRegistration = false;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"personId\":" + extractId(person) +
                ",\"addressId\":" + extractId(address) +
                ",\"isRegistration\":" + isRegistration +
                "}";
    }

    // Manual builder since we want to preserve field defaults (Lombok's builder overwrites them)
    public static PersonAddressBuilder builder() {
        return new PersonAddressBuilder();
    }

    public static class PersonAddressBuilder extends AbstractBuilder<PersonAddress> {
        private PersonAddressBuilder() {
            super(PersonAddress::new);
        }

        public PersonAddressBuilder id(Long id) {
            getInstance().id = id;
            return this;
        }

        public PersonAddressBuilder person(Person person) {
            getInstance().person = person;
            return this;
        }

        public PersonAddressBuilder address(Address address) {
            getInstance().address = address;
            return this;
        }

        public PersonAddressBuilder isRegistration(Boolean isRegistration) {
            getInstance().isRegistration = isRegistration;
            return this;
        }
    }
}
