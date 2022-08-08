package com.github.royalflushdtd.pgsynchijacker.lock;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public class LockingException extends RuntimeException {

    private static final long serialVersionUID = 2256598708734964597L;

    public LockingException(String msg, Throwable e) {
        super(msg, e);
    }

    public LockingException(String msg) {
        super(msg);
    }
}
