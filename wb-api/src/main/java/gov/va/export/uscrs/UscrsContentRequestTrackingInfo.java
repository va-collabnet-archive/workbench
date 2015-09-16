package gov.va.export.uscrs;

import java.io.File;

/**
 * USCRS implementation of a {@link ContentRequestTrackingInfo}.
 *
 * @author jefron
 */
public class UscrsContentRequestTrackingInfo  {

  /** The name. */
  private String name;

  /** The detail. */
  private String detail;

  /** The is successful. */
  private boolean isSuccessful;
  
  /**  The file. */
  private File file;

  /**
   * Instantiates an empty {@link UscrsContentRequestTrackingInfo}.
   */
  public UscrsContentRequestTrackingInfo() {
    // do nothing
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType() {
    return "USCRS";
  }

  /**
   * Returns the url.
   *
   * @return the url
   */
  public String getUrl() {
    return "https://uscrs.nlm.nih.gov/";
  }

  /**
   * Returns the detail.
   *
   * @return the detail
   */
  public String getDetail() {
    return detail;
  }

  /**
   * Sets the detail.
   *
   * @param detail the detail
   */
  public void setDetail(String detail) {
    this.detail = detail;
  }

  /**
   * Indicates whether or not successful is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSuccessful() {
    return isSuccessful;
  }

  /**
   * Sets the is successful.
   *
   * @param isSuccessful the is successful
   */
  public void setIsSuccessful(boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
  }

  /**
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * @param outputFile the file to set
   */
  public void setFile(File outputFile) {
    this.file = outputFile;
  }

}
