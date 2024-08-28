/**
 * 
 */
package com.distocraft.dc5000.etl.gui.afjupgrade;

/**
 * @author eheijun
 * 
 */
public class MessageBean {

  public enum MessageType { INFO, RUNNING, WARNING, ERROR  };

  private MessageType type;

  private String text;

  public MessageBean(final String message, final MessageType messageType) {
    this.text = message;
    this.type = messageType;
  }

  /**
   * @return the messageType
   */
  public MessageType getType() {
    return type;
  }

//  /**
//   * @param messageType
//   *          the messageType to set
//   */
//  public void setType(final MessageType type) {
//    this.type = type;
//  }

  /**
   * @return the message
   */
  public String getText() {
    return text;
  }

  /**
   * @return the message as HTML format
   */
  public String getTextAsHTML() {
    final String LF = "\n";
    int count = 0;
    int lf = text.indexOf(LF, 0);
    if (lf > 0) {
      while (lf > 0) {
        count++;
        lf = text.indexOf(LF, lf + LF.length());
      }
    }
    switch (count) {
    case 0:
      return text;
    case 1:
      return text.replaceFirst("\n", "<br/>");
    default:
      text = text.replaceFirst("\n", "<ul><li>") + "</ul>";
      return text.replaceAll("\n", "<li>");
    }
  }

//  /**
//   * @param text
//   *          the text to set
//   */
//  public void setText(final String text) {
//    this.text = text;
//  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return text;
  }
  
  /**
   * Check if message type is ERROR
   * @return
   */
  public Boolean isError() {
    return Boolean.valueOf(this.type == MessageType.ERROR);
  }

  /**
   * Check if message type is WARNING
   * @return
   */
  public Boolean isWarning() {
    return Boolean.valueOf(this.type == MessageType.WARNING);
  }

  /**
   * Check if message type is INFO
   * @return
   */
  public Boolean isInfo() {
    return Boolean.valueOf(this.type == MessageType.INFO);
  }

  /**
   * Check if message type is RUNNING
   * @return
   */
  public Boolean isRunning() {
    return Boolean.valueOf(this.type == MessageType.RUNNING);
  }

}
