package cz.geek.objectify.test;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class SampleEntity {

    @Id
    private Long id;
    private String value;

    public SampleEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
