package cz.exodus.jsend.network.model;

import java.util.function.Consumer;
import java.util.function.Function;

public class Result<S, F> {
    private final S success;
    private final F failure;

    private Result(S success, F failure) {
        this.success = success;
        this.failure = failure;
    }

    public static <S, F> Result<S, F> success(S success) {
        return new Result<>(success, null);
    }

    public static <S, F> Result<S, F> failure(F failure) {
        return new Result<>(null, failure);
    }

    public void withSuccess(Consumer<S> consumer) {
        if (isSuccess()) {
            consumer.accept(success);
        }
    }

    public void withFailure(Consumer<F> consumer) {
        if (isFailure()) {
            consumer.accept(failure);
        }
    }

    public <R> Result<R, F> mapSuccess(Function<S, R> function) {
        if (isSuccess()) {
            return Result.success(function.apply(success));
        } else {
            return Result.failure(failure);
        }
    }

    public <R> Result<S, R> mapFailure(Function<F, R> function) {
        if (isFailure()) {
            return Result.failure(function.apply(failure));
        } else {
            return Result.success(success);
        }
    }

    public boolean isSuccess() {
        return success != null;
    }

    public boolean isFailure() {
        return failure != null;
    }

    public S getSuccess() {
        return success;
    }

    public F getFailure() {
        return failure;
    }
}
