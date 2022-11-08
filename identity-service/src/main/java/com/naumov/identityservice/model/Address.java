package com.naumov.identityservice.model;

import com.naumov.identityservice.util.AbstractBuilder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.naumov.identityservice.util.JsonUtil.translateEscapes;

@Entity
@Table(name = "addresses", uniqueConstraints = {
        @UniqueConstraint(name = "region_address_uk", columnNames = {"region_id", "address"})
})
public class Address implements IdentifiableEntity {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_gen")
    @SequenceGenerator(name = "address_gen", sequenceName = "addresses_seq", allocationSize = 10)
    private Long id;
    @Getter
    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Getter
    @Setter
    @Column(length = 255, nullable = false)
    private String address;
    // REMOVE is used since we persist PersonAddress through Person
    @OneToMany(mappedBy = "address", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PersonAddress> personRecords = new ArrayList<>();

    public List<PersonAddress> getPersonRecords() {
        return personRecords;
    }

    public void setPersonRecords(List<PersonAddress> personRecords) {
        this.personRecords = Optional.ofNullable(personRecords).orElseGet(ArrayList::new);
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"region\":" + region +
                ",\"address\":\"" + translateEscapes(address) + "\"" +
                ",\"personRecords\":" + personRecords +
                "}";
    }

    // Manual builder since we want to preserve field defaults (Lombok's builder overwrites them)
    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    public static class AddressBuilder extends AbstractBuilder<Address> {
        private AddressBuilder() {
            super(Address::new);
        }

        public AddressBuilder id(Long id) {
            getInstance().id = id;
            return this;
        }

        public AddressBuilder region(Region region) {
            getInstance().region = region;
            return this;
        }

        public AddressBuilder address(String address) {
            getInstance().address = address;
            return this;
        }

        public AddressBuilder personRecords(List<PersonAddress> personRecords) {
            getInstance().personRecords = Optional.ofNullable(personRecords).orElseGet(ArrayList::new);
            return this;
        }
    }
}
