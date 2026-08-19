package one.digitalinnovation.gof.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(EnderecoManualIncompletoException.class)
	public ResponseEntity<Map<String, Object>> handleEnderecoManualIncompleto(EnderecoManualIncompletoException exception) {
		return ResponseEntity.unprocessableEntity().body(Map.of(
				"status", HttpStatus.UNPROCESSABLE_ENTITY.value(),
				"mensagem", exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> campos = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

		return ResponseEntity.badRequest().body(Map.of(
				"status", HttpStatus.BAD_REQUEST.value(),
				"erros", campos));
	}
}
