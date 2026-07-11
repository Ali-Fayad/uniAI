package com.uniai.chat.application.port.out;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;

public interface GraduateQueryInterpretationPort {

    GraduateQueryInterpretation interpret(GraduateQueryInterpretationRequest request);
}
