/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.vk.longpoll;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.longpoll.LongPollResult;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:23 AM
 */
public class VkGetLongPollingDataHttpTransaction extends AbstractHttpTransaction<LongPollResult> {

	@Nonnull
	private final LongPollServerData longPollServerData;

	VkGetLongPollingDataHttpTransaction(@Nonnull LongPollServerData longPollServerData) {
		super("http://" + longPollServerData.getServerUri(), HttpMethod.GET);
		this.longPollServerData = longPollServerData;
	}

	@Override
	public LongPollResult getResponse(@Nonnull HttpResponse response) {
		try {
			final HttpEntity httpEntity = response.getEntity();
			final String json = EntityUtils.toString(httpEntity);
			Log.i("LongPolling", json);

			// prepare builder
			final GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(LongPollUpdate.class, new LongPollUpdate.Adapter());
			final Gson gson = builder.create();

			final JsonLongPollData jsonLongPollData = gson.fromJson(json, JsonLongPollData.class);
			return jsonLongPollData.toResult();
		} catch (IOException e) {
			throw new HttpRuntimeIoException(e);
		} catch (IllegalJsonException e) {
			throw new IllegalJsonRuntimeException(e);
		}
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = new ArrayList<NameValuePair>();

		result.add(new BasicNameValuePair("act", "a_check"));
		result.add(new BasicNameValuePair("key", longPollServerData.getKey()));
		result.add(new BasicNameValuePair("ts", String.valueOf(longPollServerData.getTimeStamp())));
		result.add(new BasicNameValuePair("wait", "20"));
		result.add(new BasicNameValuePair("mode", "0"));
		//todo serso: check if necessary
		result.add(new BasicNameValuePair(CoreConnectionPNames.SO_TIMEOUT, "30000"));

		return result;
	}
}
