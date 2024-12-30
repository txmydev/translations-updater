package com.github.txmy.translations;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public interface IExecutor<T> {

    T execute();

    enum Result {
        SUCCESS,
        FAILED
    }

    @AllArgsConstructor
    @NoArgsConstructor
    class PhaseResult<T> {

        public Result result;
        public Exception thrown;
        public T object;

        public boolean hasFailed() {
            return result == Result.FAILED;
        }

    }

}
