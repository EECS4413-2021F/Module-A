package model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tax")
public class TaxBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;
  private String code;
  private String type;
  private double pst;
  private double gst;

  public TaxBean() { }

  // Getters

  public String getName() { return name; }
  public String getCode() { return code; }
  public String getType() { return type; }
  public double getPst()  { return pst; }
  public double getGst()  { return gst; }

  // Setters

  public void setName(String name) { this.name = name; }
  public void setCode(String code) { this.code = code; }
  public void setType(String type) { this.type = type; }
  public void setPst(double pst)   { this.pst  = pst; }
  public void setGst(double gst)   { this.gst  = gst; }

  public String toString() {
    return String.format("Taxes in %s (%s):\n"
      + "- Type = %s\n"
      + "- GST  = %.2f%%\n"
      + "- PST  = %.2f%%\n", name, code, type, gst, pst);
  }
}
