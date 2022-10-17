package com.naumov.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

import static com.naumov.util.JsonUtil.translateEscapes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "addresses", uniqueConstraints = {
        @UniqueConstraint(name = "region_address_uk", columnNames = {"region_id", "address"})
})
public class Address implements IdentifiableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_gen")
    @SequenceGenerator(name = "address_gen", sequenceName = "addresses_seq", allocationSize = 10)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Column(length = 255, nullable = false)
    private String address;
    // REMOVE is used since we persist PersonAddress through Person
    @OneToMany(mappedBy = "address", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<PersonAddress> personRecords;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"region\":" + region +
                ",\"address\":\"" + translateEscapes(address) + "\"" +
                ",\"personRecords\":" + personRecords +
                "}";
    }
}
