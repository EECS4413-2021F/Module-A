package model;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A collection of Tax records.
 * Represented as a List.
 */
@XmlRootElement(name="taxes")
public class TaxCollection implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<TaxBean> taxes;
  public TaxCollection() { }

  @XmlElement(name="tax")
  public List<TaxBean> getTaxes() {
    return taxes;
  }

  public void setTaxes(List<TaxBean> taxes) {
    this.taxes = taxes;
  }

  public String toString() {
    String output = "";
    for (TaxBean tax : taxes) {
      output += tax.toString();
    }
    return output;
  }
}
