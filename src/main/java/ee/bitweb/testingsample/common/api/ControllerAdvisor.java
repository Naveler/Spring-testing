package ee.bitweb.testingsample.common.api;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import ee.bitweb.testingsample.common.api.model.exception.FieldErrorResponse;
import ee.bitweb.testingsample.common.api.model.exception.GenericErrorResponse;
import ee.bitweb.testingsample.common.api.model.exception.PersistenceErrorResponse;
import ee.bitweb.testingsample.common.api.model.exception.ValidationErrorResponse;
import ee.bitweb.testingsample.common.exception.persistence.PersistenceException;
import ee.bitweb.testingsample.common.exception.validation.InvalidFormatValidationException;
import ee.bitweb.testingsample.common.trace.TraceId;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.MethodNotAllowedException;

@Slf4j
@ControllerAdvice
public class ControllerAdvisor {

    private static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    @ResponseBody
    @ExceptionHandler(PersistenceException.class)
    public PersistenceErrorResponse handleConflictException(
            HttpServletResponse response,
            PersistenceException e
    ) {
        setDefaultHeaders(response, e.getCode());

        return new PersistenceErrorResponse(getResponseId(), e);
    }

    @ResponseBody
    @ExceptionHandler(MultipartException.class)
    public GenericErrorResponse handleMultipartException(MultipartException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log.warn(e.getMessage(), e);

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.CONTENT_TYPE_NOT_VALID.toString()
        );
    }

    @ResponseBody
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public GenericErrorResponse handleException(HttpMediaTypeNotSupportedException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        log.warn(e.getMessage(), e);

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.MESSAGE_NOT_READABLE.toString()
        );
    }

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public GenericErrorResponse handleException(HttpMessageNotReadableException e, HttpServletResponse response) {
        log.warn(e.getMessage());
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        InvalidFormatValidationException newException = null;
        if (e.getCause() instanceof InvalidFormatException) {
            newException = new InvalidFormatValidationException(
                    (InvalidFormatException) e.getCause()
            );
        } else if (e.getCause() instanceof MismatchedInputException) {
            newException = new InvalidFormatValidationException(
                    (MismatchedInputException) e.getCause()
            );
        }

        if (newException != null && InvalidFormatExceptionConverter.canConvert(newException)) {
            return new ValidationErrorResponse(
                    getResponseId(),
                    InvalidFormatExceptionConverter.convert(newException)
            );
        }

        return new GenericErrorResponse(getResponseId(), ErrorMessage.MESSAGE_NOT_READABLE.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(
            HttpServletResponse response,
            ConstraintViolationException e
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        // todo: debug leveliga response koos valuega ja kui ei saa siis request body ka

        return new ValidationErrorResponse(getResponseId(), ExceptionConverter.convert(e));
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(
            BindException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        // todo: debug leveliga response koos valuega ja kui ei saa siis request body ka

        return new ValidationErrorResponse(
                getResponseId(),
                ExceptionConverter.translateBindingResult(e.getBindingResult())
        );
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ValidationErrorResponse handleException(
            MissingServletRequestParameterException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ErrorMessage.INVALID_ARGUMENT.toString(),
                List.of(
                        new FieldErrorResponse(
                                e.getParameterName(),
                                "MissingValue",
                                "Request parameter is required"
                        )
                )
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(MethodArgumentNotValidException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ExceptionConverter.translateBindingResult(e.getBindingResult())
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(MethodArgumentTypeMismatchException e, HttpServletResponse response) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ErrorMessage.INVALID_ARGUMENT.toString(),
                Collections.singletonList(
                        new FieldErrorResponse(
                                e.getParameter().getParameterName(),
                                "InvalidType",
                                "Request parameter is invalid"
                        )
                )
        ));
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ValidationErrorResponse handleException(
            MissingServletRequestPartException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.BAD_REQUEST);

        return logAndReturn(new ValidationErrorResponse(
                getResponseId(),
                ErrorMessage.INVALID_ARGUMENT.toString(),
                Collections.singletonList(
                        new FieldErrorResponse(
                                e.getRequestPartName(),
                                "RequestPartPresent",
                                e.getMessage()
                        )
                )
        ));
    }

    @ResponseBody
    @ExceptionHandler(MethodNotAllowedException.class)
    public GenericErrorResponse handleMethodNotAllowed(
            MethodNotAllowedException e,
            HttpServletResponse response
    ) {
        setDefaultHeaders(response, HttpStatus.METHOD_NOT_ALLOWED);

        log.warn(e.getMessage(), e);

        // set Allow header
        e.getResponseHeaders().forEach((key, value) -> {
            for (String s : value) {
                response.setHeader(key, s);
            }
        });

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.METHOD_NOT_ALLOWED.toString()
        );
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public GenericErrorResponse handleGeneralException(
            HttpServletResponse response,
            Throwable e
    ) {
        setDefaultHeaders(response, HttpStatus.INTERNAL_SERVER_ERROR);

        log.error(e.getMessage(), e);

        return new GenericErrorResponse(
                getResponseId(),
                ErrorMessage.INTERNAL_SERVER_ERROR.toString()
        );
    }

    private String getResponseId() {
        return TraceId.get();
    }

    private <T> T logAndReturn(T body) {
        log.debug("{}", body);

        return body;
    }

    private void setDefaultHeaders(HttpServletResponse response, HttpStatus status) {
        setDefaultHeaders(response, status.value());
    }

    private void setDefaultHeaders(HttpServletResponse response, int status) {
        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.setStatus(status);
    }
}
