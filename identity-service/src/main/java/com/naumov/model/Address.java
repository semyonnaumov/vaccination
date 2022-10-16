package com.naumov.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "addresses", uniqueConstraints = {
        @UniqueConstraint(name = "region_address_uk", columnNames = {"region_id", "address"})
})
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_gen")
    @SequenceGenerator(name = "address_gen", sequenceName = "addresses_seq", allocationSize = 10)
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL) // todo cascade?
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Column(length = 255, nullable = false)
    private String address;
    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL) // todo cascade?
    private List<PersonAddress> personRecords;
}
