package services.model;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="taxes")
public class TaxCollection implements Serializable {

  private List<TaxBean> taxes;
  public TaxCollection() { }

  @XmlElement(name="tax")
  public List<TaxBean> getTaxes() {
    return taxes;
  }

  public void setTaxes(List<TaxBean> taxes) {
    this.taxes = taxes;
  }
}
