package de.qfotografie.akm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataPoint implements Serializable {
    private Date time;

    private String type;
    private float value;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "time='" + time + '\'' +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
