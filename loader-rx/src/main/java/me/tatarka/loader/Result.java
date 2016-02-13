package me.tatarka.loader;

/**
 * Holds either a successful result or an error.
 */
public abstract class Result<T> {

    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> Result<T> error(Throwable error) {
        return new Error<>(error);
    }

    private Result() {
    }

    /**
     * Returns a successful value or throws the error.
     */
    public T get() throws Throwable {
        if (isSuccess()) {
            return getSuccess();
        } else {
            throw getError();
        }
    }

    /**
     * Returns if the result is successful.
     */
    public abstract boolean isSuccess();

    /**
     * Returns if the result is an error.
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * Returns the successful value or throws if it is not. You should check {@link #isSuccess()}
     * before calling this.
     *
     * @throws IllegalStateException
     */
    public abstract T getSuccess();

    /**
     * Returns the error or throws if it is successful. You should check {@link #isError()} before
     * calling this.
     *
     * @throws IllegalStateException
     */
    public abstract Throwable getError();

    private static final class Success<T> extends Result<T> {
        private T value;

        Success(T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getSuccess() {
            return value;
        }

        @Override
        public Throwable getError() {
            throw new IllegalStateException("Result is success: " + value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?> success = (Success<?>) o;
            return value != null ? value.equals(success.value) : success.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Result.Success(" + value + ")";
        }
    }

    private static final class Error<T> extends Result<T> {
        private Throwable error;

        Error(Throwable error) {
            this.error = error;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getSuccess() {
            throw new IllegalStateException("Result is error", error);
        }

        @Override
        public Throwable getError() {
            return error;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Error<?> error1 = (Error<?>) o;
            return error != null ? error.equals(error1.error) : error1.error == null;
        }

        @Override
        public int hashCode() {
            return error != null ? error.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Result.Error(" + error + ")";
        }
    }
}
