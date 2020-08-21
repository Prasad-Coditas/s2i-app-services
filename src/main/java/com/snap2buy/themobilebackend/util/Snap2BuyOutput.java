package com.snap2buy.themobilebackend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


@XmlType(propOrder = {"metaDetail", "row"})
@XmlRootElement(name = "S2PResponse")
public class Snap2BuyOutput {

    private static Logger LOGGER = LoggerFactory.getLogger(Snap2BuyOutput.class);

    private MyHashMapType metaDetail = new MyHashMapType();

    @XmlElementWrapper(name = "ResultSet")
    private List<MyHashMapType> row = new ArrayList<MyHashMapType>();

    public Snap2BuyOutput(List<LinkedHashMap<String, String>> resultSet, HashMap inputList) {
        LOGGER.info("Inside Snap2BuyOutput");

        this.setRow(resultSet);
        this.setMetaDetail(inputList);
    }

    public Snap2BuyOutput() {
        super();
        LOGGER.debug("I reached default no-args constructor of Class Snap2BuyOutput.");
    }

    @XmlElement(name = "MetaInfo")
    public MyHashMapType getMetaDetail() {
        return metaDetail;
    }

    public void setMetaDetail(HashMap<String, String> inputMap) {
        metaDetail.setMapProperty(inputMap);
    }

    public List<MyHashMapType> getRow() {
        return row;
    }

    public void setRow(List<LinkedHashMap<String, String>> resultSet) {

        if (resultSet != null && !resultSet.isEmpty()) {
            for (LinkedHashMap<String, String> myMap : resultSet) {
                MyHashMapType m = new MyHashMapType();
                m.setLinkedMapProperty(myMap);
                row.add(m);
            }
        } else {
            LinkedHashMap<String, String> myMap = new LinkedHashMap<String, String>();
            myMap.put("Message", "No Data Returned");
            MyHashMapType m = new MyHashMapType();
            m.setLinkedMapProperty(myMap);
            row.add(m);
        }

    }

    @Override
    public String toString() {
        return "Snap2BuyOutput{" +
                "metaDetail=" + metaDetail +
                ", row=" + row +
                '}';
    }
}

