package com.naumov.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static com.naumov.util.JsonUtil.translateEscapes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "regions")
public class Region implements IdentifiableEntity {
    @Id
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"name\":\"" + translateEscapes(name) + "\"" +
                "}";
    }
}
