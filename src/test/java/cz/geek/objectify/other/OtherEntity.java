package cz.geek.objectify.other;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class OtherEntity {

    @Id
    private Long id;

    public OtherEntity() {
    }
}
