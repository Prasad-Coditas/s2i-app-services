package com.snap2buy.themobilebackend.model;

/**
 * Created by sachin on 10/31/15.
 */
public class Skus {
    String upc;
    String expected_facings;
    String on_shelf_availability;
    String detected_facings;
    String promotion_label_present;
    String price;
    String promo_price;

    public String getOn_shelf_availability() {
        return on_shelf_availability;
    }

    public void setOn_shelf_availability(String on_shelf_availability) {
        this.on_shelf_availability = on_shelf_availability;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getExpected_facings() {
        return expected_facings;
    }

    public void setExpected_facings(String expected_facings) {
        this.expected_facings = expected_facings;
    }

    public String getDetected_facings() {
        return detected_facings;
    }

    public void setDetected_facings(String detected_facings) {
        this.detected_facings = detected_facings;
    }

    public String getPromotion_label_present() {
        return promotion_label_present;
    }

    public void setPromotion_label_present(String promotion_label_present) {
        this.promotion_label_present = promotion_label_present;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPromo_price() {
        return promo_price;
    }

    public void setPromo_price(String promo_price) {
        this.promo_price = promo_price;
    }

    @Override
    public String toString() {
        return "Skus{" +
                "upc='" + upc + '\'' +
                ", expected_facings='" + expected_facings + '\'' +
                ", on_shelf_availability='" + on_shelf_availability + '\'' +
                ", detected_facings='" + detected_facings + '\'' +
                ", promotion_label_present='" + promotion_label_present + '\'' +
                ", price='" + price + '\'' +
                ", promo_price='" + promo_price + '\'' +
                '}';
    }
}
