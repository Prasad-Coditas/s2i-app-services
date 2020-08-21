package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by sachin on 1/23/16.
 */
@XmlRootElement(name = "ProductMaster")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductMaster {
	@XmlElement
	String upc;
	@XmlElement
    String mfg_name;
	@XmlElement
	String brand_name;
	@XmlElement
	String product_type;
	@XmlElement
	String product_short_name;
	@XmlElement
	String product_long_name;
	@XmlElement
	String attribute_1;
	@XmlElement
	String attribute_2;
	@XmlElement
	String attribute_3;
	@XmlElement
	String attribute_4;
	@XmlElement
	String brandLevel;
	@XmlElement
	String why_buy_1;
	@XmlElement
	String why_buy_2;
	@XmlElement
	String why_buy_3;
	@XmlElement
	String why_buy_4;
	@XmlElement
	String romance_copy_1;
	@XmlElement
	String romance_copy_2;
	@XmlElement
	String romance_copy_3;
	@XmlElement
	String romance_copy_4;
	@XmlElement
	String height;
	@XmlElement
	String width;
	@XmlElement
	String depth;
	@XmlElement
	String product_rating;
	@XmlElement
	String product_sub_type;

    public ProductMaster() {
        super();
    }

    public ProductMaster(String upc, String mfg_name, String brand_name, String product_type, String product_short_name, String product_long_name, String attribute_1, String attribute_2, String attribute_3, String attribute_4, String brandLevel, String why_buy_1, String why_buy_2, String why_buy_3, String why_buy_4, String romance_copy_1, String romance_copy_2, String romance_copy_3, String romance_copy_4, String height, String width, String depth, String product_rating, String product_sub_type) {
        this.upc = upc;
        this.mfg_name = mfg_name;
        this.brand_name = brand_name;
        this.product_type = product_type;
        this.product_short_name = product_short_name;
        this.product_long_name = product_long_name;
        this.attribute_1 = attribute_1;
        this.attribute_2 = attribute_2;
        this.attribute_3 = attribute_3;
        this.attribute_4 = attribute_4;
        this.brandLevel = brandLevel;
        this.why_buy_1 = why_buy_1;
        this.why_buy_2 = why_buy_2;
        this.why_buy_3 = why_buy_3;
        this.why_buy_4 = why_buy_4;
        this.romance_copy_1 = romance_copy_1;
        this.romance_copy_2 = romance_copy_2;
        this.romance_copy_3 = romance_copy_3;
        this.romance_copy_4 = romance_copy_4;
        this.height = height;
        this.width = width;
        this.depth = depth;
        this.product_rating = product_rating;
        this.product_sub_type = product_sub_type;
    }


    public String getAttribute_1() {
        return attribute_1;
    }

    public void setAttribute_1(String attribute_1) {
        this.attribute_1 = attribute_1;
    }

    public String getAttribute_2() {
        return attribute_2;
    }

    public void setAttribute_2(String attribute_2) {
        this.attribute_2 = attribute_2;
    }

    public String getAttribute_3() {
        return attribute_3;
    }

    public void setAttribute_3(String attribute_3) {
        this.attribute_3 = attribute_3;
    }

    public String getAttribute_4() {
        return attribute_4;
    }

    public void setAttribute_4(String attribute_4) {
        this.attribute_4 = attribute_4;
    }

    public String getBrandLevel() {
        return brandLevel;
    }

    public void setBrandLevel(String brandLevel) {
        this.brandLevel = brandLevel;
    }

    public String getProduct_rating() {
        return product_rating;
    }

    public void setProduct_rating(String product_rating) {
        this.product_rating = product_rating;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getMfg_name() {
        return mfg_name;
    }

    public void setMfg_name(String mfg_name) {
        this.mfg_name = mfg_name;
    }

    public String getBrand_name() {
        return brand_name;
    }

    public void setBrand_name(String brand_name) {
        this.brand_name = brand_name;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getProduct_short_name() {
        return product_short_name;
    }

    public void setProduct_short_name(String product_short_name) {
        this.product_short_name = product_short_name;
    }

    public String getProduct_long_name() {
        return product_long_name;
    }

    public void setProduct_long_name(String product_long_name) {
        this.product_long_name = product_long_name;
    }

    public String getWhy_buy_1() {
        return why_buy_1;
    }

    public void setWhy_buy_1(String why_buy_1) {
        this.why_buy_1 = why_buy_1;
    }

    public String getWhy_buy_2() {
        return why_buy_2;
    }

    public void setWhy_buy_2(String why_buy_2) {
        this.why_buy_2 = why_buy_2;
    }

    public String getWhy_buy_3() {
        return why_buy_3;
    }

    public void setWhy_buy_3(String why_buy_3) {
        this.why_buy_3 = why_buy_3;
    }

    public String getWhy_buy_4() {
        return why_buy_4;
    }

    public void setWhy_buy_4(String why_buy_4) {
        this.why_buy_4 = why_buy_4;
    }

    public String getRomance_copy_1() {
        return romance_copy_1;
    }

    public void setRomance_copy_1(String romance_copy_1) {
        this.romance_copy_1 = romance_copy_1;
    }

    public String getRomance_copy_2() {
        return romance_copy_2;
    }

    public void setRomance_copy_2(String romance_copy_2) {
        this.romance_copy_2 = romance_copy_2;
    }

    public String getRomance_copy_3() {
        return romance_copy_3;
    }

    public void setRomance_copy_3(String romance_copy_3) {
        this.romance_copy_3 = romance_copy_3;
    }

    public String getRomance_copy_4() {
        return romance_copy_4;
    }

    public void setRomance_copy_4(String romance_copy_4) {
        this.romance_copy_4 = romance_copy_4;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }
    
    public String getProduct_sub_type() {
		return product_sub_type;
	}

	public void setProduct_sub_type(String product_sub_type) {
		this.product_sub_type = product_sub_type;
	}

	@Override
	public String toString() {
		return "ProductMaster [upc=" + upc + ", mfg_name=" + mfg_name + ", brand_name=" + brand_name + ", product_type="
				+ product_type + ", product_short_name=" + product_short_name + ", product_long_name="
				+ product_long_name + ", attribute_1=" + attribute_1 + ", attribute_2=" + attribute_2 + ", attribute_3="
				+ attribute_3 + ", attribute_4=" + attribute_4 + ", brandLevel=" + brandLevel + ", why_buy_1="
				+ why_buy_1 + ", why_buy_2=" + why_buy_2 + ", why_buy_3=" + why_buy_3 + ", why_buy_4=" + why_buy_4
				+ ", romance_copy_1=" + romance_copy_1 + ", romance_copy_2=" + romance_copy_2 + ", romance_copy_3="
				+ romance_copy_3 + ", romance_copy_4=" + romance_copy_4 + ", height=" + height + ", width=" + width
				+ ", depth=" + depth + ", product_rating=" + product_rating + ", product_sub_type=" + product_sub_type
				+ "]";
	}
}
