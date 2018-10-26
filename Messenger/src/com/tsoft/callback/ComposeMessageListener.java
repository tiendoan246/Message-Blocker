package com.tsoft.callback;

public interface ComposeMessageListener<T> {
	void onMessageComposeCompleted(T item);
}
