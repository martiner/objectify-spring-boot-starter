package cz.geek.objectify;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
class RoundTripEntity {

    @Id
    Long id;
    String value;

    RoundTripEntity() {
    }

    RoundTripEntity(String value) {
        this.value = value;
    }
}
