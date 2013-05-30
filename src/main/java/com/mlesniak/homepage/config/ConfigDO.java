package com.mlesniak.homepage.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/** @author Michael Lesniak (mail@mlesniak.com) */
@Entity
public class ConfigDO {
    @Id
    @GeneratedValue
    private long pk;

    private String key;
    private String value;

    public ConfigDO() {

    }

    public ConfigDO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConfigDO{" +
                 "pk=" + pk +
                 ", key='" + key + '\'' +
                 ", value='" + value + '\'' +
                 '}';
    }
}
