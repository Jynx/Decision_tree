
public class informationObject implements Comparable<informationObject> {
    private String attributeName = null;
    private double attributeValue = 0.0;
    private int index = 0;
    
    public informationObject(String label, double value, int index) {
        this.attributeName = label;
        this.attributeValue = value;
        this.index = index;
    }

    public double getAttributeValue() {
        return this.attributeValue;
    }
    
    public String getAttributeName() {
        return this.attributeName;
    }
    
    @Override
    public int compareTo(informationObject object1) {
        if (object1.getAttributeValue() > this.getAttributeValue()) {
            return 1;
        }
        if (object1.getAttributeValue() < this.getAttributeValue()) {
            return -1;
        }
        else { return 0; }
    }
    
    public int getIndex() {
        return this.index;
    }
}

