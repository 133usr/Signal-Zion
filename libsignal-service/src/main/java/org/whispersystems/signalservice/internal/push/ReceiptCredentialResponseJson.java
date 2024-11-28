package org.whispersystems.signalservice.internal.push;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.signal.core.util.logging.Log;
import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialResponse;
import org.signal.core.util.Base64;

import java.io.IOException;

class ReceiptCredentialResponseJson {

  private final ReceiptCredentialResponse receiptCredentialResponse;

  ReceiptCredentialResponseJson(@JsonProperty("receiptCredentialResponse") String receiptCredentialResponse) throws IOException, InvalidInputException {
    ReceiptCredentialResponse response;
    try {
      response = new ReceiptCredentialResponse(Base64.decode(receiptCredentialResponse));
    } catch (IOException | InvalidInputException e) {

      Log.e("Receipt Credential resp", "not ok but somehow trying" );
      response = new ReceiptCredentialResponse(Base64.decode(receiptCredentialResponse));;
    }

    this.receiptCredentialResponse = response;
  }

  public ReceiptCredentialResponse getReceiptCredentialResponse() {
    return receiptCredentialResponse;
  }
}
