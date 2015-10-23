package retrofit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

public class AuthRxJavaCallAdapterFactory implements CallAdapter.Factory {

	private AuthRxJavaCallAdapterFactory() {
	}

	public static AuthRxJavaCallAdapterFactory create() {
		return new AuthRxJavaCallAdapterFactory();
	}

	@Override
	public CallAdapter<?> get(Type returnType) {
		Class<?> rawType = Utils.getRawType(returnType);
		boolean isSingle = "rx.Single".equals(rawType.getCanonicalName());
		if (rawType != Observable.class && !isSingle) {
			return null;
		}
		if (!(returnType instanceof ParameterizedType)) {
			String name = isSingle ? "Single" : "Observable";
			throw new IllegalStateException(name + " return type must be parameterized"
					+ " as " + name + "<Foo> or " + name + "<? extends Foo>");
		}

		CallAdapter<Object> callAdapter = getCallAdapter(returnType);
		if (isSingle) {
			// Add Single-converter wrapper from a separate class. This defers classloading such that
			// regular Observable operation can be leveraged without relying on this unstable RxJava API.
			callAdapter = SingleHelper.makeSingle(callAdapter);
		}
		return callAdapter;
	}

	private CallAdapter<Object> getCallAdapter(Type returnType) {
		Type observableType = Utils.getSingleParameterUpperBound((ParameterizedType) returnType);
		Class<?> rawObservableType = Utils.getRawType(observableType);
		if (rawObservableType == Response.class) {
			if (!(observableType instanceof ParameterizedType)) {
				throw new IllegalStateException("Response must be parameterized"
						+ " as Response<Foo> or Response<? extends Foo>");
			}
			Type responseType = Utils.getSingleParameterUpperBound((ParameterizedType) observableType);
			return new ResponseCallAdapter<>(responseType);
		}

		if (rawObservableType == Result.class) {
			if (!(observableType instanceof ParameterizedType)) {
				throw new IllegalStateException("Result must be parameterized"
						+ " as Result<Foo> or Result<? extends Foo>");
			}
			Type responseType = Utils.getSingleParameterUpperBound((ParameterizedType) observableType);
			return new ResultCallAdapter<>(responseType);
		}

		return new SimpleCallAdapter<>(observableType);
	}

	static final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
		private final Call<T> originalCall;

		private CallOnSubscribe(Call<T> originalCall) {
			this.originalCall = originalCall;
		}

		@Override
		public void call(final Subscriber<? super Response<T>> subscriber) {
			// Since Call is a one-shot type, clone it for each new subscriber.
			final Call<T> call = originalCall.clone();

			// Attempt to cancel the call if it is still in-flight on unsubscription.
			subscriber.add(Subscriptions.create(new Action0() {
				@Override
				public void call() {
					call.cancel();
				}
			}));

			call.enqueue(new Callback<T>() {
				@Override
				public void onResponse(Response<T> response) {
					if (subscriber.isUnsubscribed()) {
						return;
					}
					try {
						subscriber.onNext(response);
					} catch (Throwable t) {
						subscriber.onError(t);
						return;
					}
					subscriber.onCompleted();
				}

				@Override
				public void onFailure(Throwable t) {
					if (subscriber.isUnsubscribed()) {
						return;
					}
					subscriber.onError(t);
				}
			});
		}
	}

	static final class ResponseCallAdapter<T> implements CallAdapter<T> {
		private final Type responseType;

		ResponseCallAdapter(Type responseType) {
			this.responseType = responseType;
		}

		@Override
		public Type responseType() {
			return responseType;
		}

		@Override
		public Observable<Response<T>> adapt(Call<T> call) {
			return Observable.create(new CallOnSubscribe<>(call));
		}
	}

	static final class SimpleCallAdapter<T> implements CallAdapter<T> {
		private final Type responseType;

		SimpleCallAdapter(Type responseType) {
			this.responseType = responseType;
		}

		@Override
		public Type responseType() {
			return responseType;
		}

		@Override
		public Observable<T> adapt(Call<T> call) {
			return Observable.create(new CallOnSubscribe<>(call)) //
					.flatMap(new Func1<Response<T>, Observable<T>>() {
						@Override
						public Observable<T> call(Response<T> response) {
							if (response.isSuccess()) {
								return Observable.just(response.body());
							}
							return Observable.error(new HttpException(response));
						}
					});
		}
	}

	static final class ResultCallAdapter<T> implements CallAdapter<T> {
		private final Type responseType;

		ResultCallAdapter(Type responseType) {
			this.responseType = responseType;
		}

		@Override
		public Type responseType() {
			return responseType;
		}

		@Override
		public Observable<Result<T>> adapt(Call<T> call) {
			return Observable.create(new CallOnSubscribe<>(call)) //
					.map(new Func1<Response<T>, Result<T>>() {
						@Override
						public Result<T> call(Response<T> response) {
							return Result.response(response);
						}
					})
					.onErrorReturn(new Func1<Throwable, Result<T>>() {
						@Override
						public Result<T> call(Throwable throwable) {
							return Result.error(throwable);
						}
					});
		}
	}
}
