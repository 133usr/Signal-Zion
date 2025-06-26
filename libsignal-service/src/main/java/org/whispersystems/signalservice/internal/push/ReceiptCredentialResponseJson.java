package org.whispersystems.signalservice.internal.push;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialResponse;
import org.signal.core.util.Base64;

import java.io.IOException;
import java.util.Arrays;

class ReceiptCredentialResponseJson {

  private final ReceiptCredentialResponse receiptCredentialResponse;

  ReceiptCredentialResponseJson(@JsonProperty("receiptCredentialResponse") String receiptCredentialResponse) {
    if (receiptCredentialResponse == null || receiptCredentialResponse.isEmpty()) {
      throw new IllegalArgumentException("receiptCredentialResponse cannot be null or empty");
    }

    ReceiptCredentialResponse response;
    try {
      response = new ReceiptCredentialResponse(Base64.decode(receiptCredentialResponse));
    } catch (IOException | InvalidInputException e) {
//      throw new IllegalArgumentException("Invalid receiptCredentialResponse: " + e.getMessage(), e);
      response = null;
    }
    this.receiptCredentialResponse = response;
  }

  public ReceiptCredentialResponse getReceiptCredentialResponse() {
    return receiptCredentialResponse;
  }
}
