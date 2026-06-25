package heojin.simulator.global.exception;

public record ErrorResponse(
	String code,
	String message
) {
}
