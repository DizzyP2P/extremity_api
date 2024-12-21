package com.breech.extremity.web.api.error;

import com.breech.extremity.core.exception.UnauthenticatedException;
import com.breech.extremity.core.response.GlobalResult;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@RestController
public class TokenErrorController extends BasicErrorController {
    public TokenErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        super(errorAttributes, serverProperties.getError());
    }
    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GlobalResult<String> errorJson(HttpServletRequest request) {
        throw new UnauthenticatedException();
    }
}
