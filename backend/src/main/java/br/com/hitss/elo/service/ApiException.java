package br.com.hitss.elo.service;

/** Erro de negócio com status HTTP. Convertido em JSON pelo ApiExceptionMapper. */
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
