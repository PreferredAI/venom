package ai.preferred.venom.examples.propertyListingCrawler.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This class allows you to store your entities. Define the
 * properties of your entities in this class.
 */
public class Property {

    private String url;
    private String title;
    private String price;
    private String address;
    private String type;
    private String area;
    private String psf;
    private Integer numBeds;
    private Integer numBaths;
    private String carpark;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getNumBaths() {
        return numBaths;
    }

    public void setNumBaths(Integer numBaths) {
        this.numBaths = numBaths;
    }

    public Integer getNumBeds() {
        return numBeds;
    }

    public void setNumBeds(Integer numBeds) {
        this.numBeds = numBeds;
    }

    public String getCarpark() {
        return carpark;
    }

    public void setCarpark(String carpark) {
        this.carpark = carpark;
    }

    public String getPsf() {
        return psf;
    }

    public void setPsf(String psf) {
        this.psf = psf;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}