package indi.dkx.laocai.service;

import indi.dkx.laocai.pojo.CallbackValidationRequest;
import indi.dkx.laocai.pojo.CallbackValidationResponse;

public interface CallbackValidationService {
    CallbackValidationResponse verify(CallbackValidationRequest request);
}
