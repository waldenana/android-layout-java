package cn.appsdream.layoutcode.widget;

/**
 * Created by zewei on 2016-05-05.
 */
public class InflateException extends RuntimeException {

    public InflateException() {
        super();
    }

    public InflateException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InflateException(String detailMessage) {
        super(detailMessage);
    }

    public InflateException(Throwable throwable) {
        super(throwable);
    }

}