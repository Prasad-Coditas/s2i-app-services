package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Harshal
 *
 */
@XmlRootElement(name = "CustomerRoleMenuMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerRoleMenuMap {

    @XmlElement
    String role;

    @XmlElement
    String customerCode;

	@XmlElement(name = "menuMap")
    List<MenuMap> menuMapList = new ArrayList<>();

	public CustomerRoleMenuMap() {
        super();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public List<MenuMap> getMenuMapList() {
        return menuMapList;
    }

    public void setMenuMapList(List<MenuMap> menuMap) {
        this.menuMapList = menuMap;
    }
}
